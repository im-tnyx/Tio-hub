package com.tnyx.features.workout.domain

import com.tnyx.shared.workout.domain.logic.WorkoutMutationRejection
import com.tnyx.shared.workout.domain.model.ExerciseAdded
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseTrackingType
import com.tnyx.shared.workout.domain.model.SessionFinished
import com.tnyx.shared.workout.domain.model.SessionStarted
import com.tnyx.shared.workout.domain.model.SetUpserted
import com.tnyx.shared.workout.domain.model.WorkoutEngineState
import com.tnyx.shared.workout.domain.model.WorkoutExercise
import com.tnyx.shared.workout.domain.model.WorkoutMutation
import com.tnyx.shared.workout.domain.model.WorkoutMutationOrigin
import com.tnyx.shared.workout.domain.model.WorkoutMutationPayload
import com.tnyx.shared.workout.domain.model.WorkoutSession
import com.tnyx.shared.workout.domain.model.WorkoutSessionStatus
import com.tnyx.shared.workout.domain.model.WorkoutSet
import com.tnyx.shared.workout.domain.repository.WorkoutMutationApplyResult
import com.tnyx.shared.workout.domain.repository.WorkoutRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class WorkoutDashboard(
    val engineState: WorkoutEngineState,
    val history: List<WorkoutSession>,
    val exerciseCatalog: List<ExerciseDefinition> = emptyList(),
)

enum class WorkoutInputError {
    REPS_OUT_OF_RANGE,
    RPE_OUT_OF_RANGE,
    COMPLETE_A_SET_FIRST,
}

sealed interface WorkoutCommandResult {
    val state: WorkoutEngineState

    data class Success(
        override val state: WorkoutEngineState,
        val changed: Boolean,
    ) : WorkoutCommandResult

    data class InvalidInput(
        override val state: WorkoutEngineState,
        val error: WorkoutInputError,
    ) : WorkoutCommandResult

    data class Rejected(
        override val state: WorkoutEngineState,
        val reason: WorkoutMutationRejection,
    ) : WorkoutCommandResult
}

interface WorkoutSessionCoordinator {
    fun observeDashboard(): Flow<WorkoutDashboard>

    suspend fun startBlankWorkout(): WorkoutCommandResult

    suspend fun addStarterExercise(): WorkoutCommandResult

    suspend fun addSet(exerciseEntryId: String): WorkoutCommandResult =
        error("Adding sets is not supported by this coordinator.")

    suspend fun completeSet(
        exerciseEntryId: String,
        setId: String?,
        reps: Int,
    ): WorkoutCommandResult

    suspend fun completeSet(
        exerciseEntryId: String,
        setId: String?,
        reps: Int,
        rpe: Int?,
    ): WorkoutCommandResult = completeSet(exerciseEntryId, setId, reps)

    suspend fun finishWorkout(): WorkoutCommandResult
}

interface WorkoutRuntimeValues {
    fun nowMs(): Long

    fun newId(prefix: String): String
}

class SystemWorkoutRuntimeValues @Inject constructor() : WorkoutRuntimeValues {
    override fun nowMs(): Long = System.currentTimeMillis()

    override fun newId(prefix: String): String = "$prefix-${UUID.randomUUID()}"
}

class DefaultWorkoutSessionCoordinator @Inject constructor(
    private val repository: WorkoutRepository,
    private val runtimeValues: WorkoutRuntimeValues,
) : WorkoutSessionCoordinator {
    private val mutationMutex = Mutex()

    override fun observeDashboard(): Flow<WorkoutDashboard> = combine(
        repository.observeEngineState(),
        repository.observeSessionHistory(),
        repository.observeExerciseCatalog().onStart { emit(emptyList()) },
    ) { engineState, history, exerciseCatalog ->
        val effectiveCatalog = if (exerciseCatalog.any { it.id == STARTER_EXERCISE_ID }) {
            exerciseCatalog
        } else {
            listOf(STARTER_EXERCISE_DEFINITION) + exerciseCatalog
        }
        WorkoutDashboard(
            engineState = engineState,
            history = history,
            exerciseCatalog = effectiveCatalog,
        )
    }

    override suspend fun startBlankWorkout(): WorkoutCommandResult = mutationMutex.withLock {
        val state = repository.observeEngineState().first()
        if (state.session?.isActive == true) {
            return@withLock WorkoutCommandResult.Success(state = state, changed = false)
        }

        val startedAtMs = runtimeValues.nowMs().coerceAtLeast(0L)
        val sessionId = runtimeValues.newId(SESSION_ID_PREFIX)
        applyMutation(
            state = state,
            sessionId = sessionId,
            occurredAtMs = startedAtMs,
            payload = SessionStarted(startedAtMs = startedAtMs),
        )
    }

    override suspend fun addStarterExercise(): WorkoutCommandResult = mutationMutex.withLock {
        val state = repository.observeEngineState().first()
        val session = state.activeSessionOrNull()
            ?: return@withLock WorkoutCommandResult.Rejected(
                state = state,
                reason = WorkoutMutationRejection.NO_ACTIVE_SESSION,
            )
        if (session.exercises.isNotEmpty()) {
            return@withLock WorkoutCommandResult.Success(state = state, changed = false)
        }

        val entryId = runtimeValues.newId(EXERCISE_ENTRY_ID_PREFIX)
        applyMutation(
            state = state,
            sessionId = session.id,
            occurredAtMs = runtimeValues.nowMs(),
            payload = ExerciseAdded(
                exercise = WorkoutExercise(
                    id = entryId,
                    exerciseId = STARTER_EXERCISE_ID,
                    exerciseNameSnapshot = STARTER_EXERCISE_NAME,
                    order = 0,
                    trackingTypeSnapshot = ExerciseTrackingType.BODYWEIGHT_REPS,
                ),
            ),
        )
    }

    override suspend fun addSet(exerciseEntryId: String): WorkoutCommandResult = mutationMutex.withLock {
        val state = repository.observeEngineState().first()
        val session = state.activeSessionOrNull()
            ?: return@withLock WorkoutCommandResult.Rejected(
                state = state,
                reason = WorkoutMutationRejection.NO_ACTIVE_SESSION,
            )
        val exercise = session.exercises.firstOrNull { it.id == exerciseEntryId }
            ?: return@withLock WorkoutCommandResult.Rejected(
                state = state,
                reason = WorkoutMutationRejection.EXERCISE_NOT_FOUND,
            )
        val nextSetNumber = (exercise.sets.maxOfOrNull(WorkoutSet::setNumber) ?: 0) + 1
        val occurredAtMs = runtimeValues.nowMs().coerceAtLeast(session.startedAtMs)
        applyMutation(
            state = state,
            sessionId = session.id,
            occurredAtMs = occurredAtMs,
            payload = SetUpserted(
                exerciseEntryId = exercise.id,
                set = WorkoutSet(
                    id = runtimeValues.newId(SET_ID_PREFIX),
                    exerciseEntryId = exercise.id,
                    setNumber = nextSetNumber,
                ),
            ),
        )
    }

    override suspend fun completeSet(
        exerciseEntryId: String,
        setId: String?,
        reps: Int,
    ): WorkoutCommandResult = completeSet(
        exerciseEntryId = exerciseEntryId,
        setId = setId,
        reps = reps,
        rpe = null,
    )

    override suspend fun completeSet(
        exerciseEntryId: String,
        setId: String?,
        reps: Int,
        rpe: Int?,
    ): WorkoutCommandResult = mutationMutex.withLock {
        val state = repository.observeEngineState().first()
        if (reps !in MIN_REPS..MAX_REPS) {
            return@withLock WorkoutCommandResult.InvalidInput(
                state = state,
                error = WorkoutInputError.REPS_OUT_OF_RANGE,
            )
        }
        if (rpe != null && rpe !in MIN_RPE..MAX_RPE) {
            return@withLock WorkoutCommandResult.InvalidInput(
                state = state,
                error = WorkoutInputError.RPE_OUT_OF_RANGE,
            )
        }
        val session = state.activeSessionOrNull()
            ?: return@withLock WorkoutCommandResult.Rejected(
                state = state,
                reason = WorkoutMutationRejection.NO_ACTIVE_SESSION,
            )
        val exercise = session.exercises.firstOrNull { it.id == exerciseEntryId }
            ?: return@withLock WorkoutCommandResult.Rejected(
                state = state,
                reason = WorkoutMutationRejection.EXERCISE_NOT_FOUND,
            )
        val existingSet = when {
            setId != null -> exercise.sets.firstOrNull { it.id == setId }
            exercise.sets.isEmpty() -> null
            else -> return@withLock WorkoutCommandResult.Rejected(
                state = state,
                reason = WorkoutMutationRejection.SET_NOT_FOUND,
            )
        }
        if (setId != null && existingSet == null) {
            return@withLock WorkoutCommandResult.Rejected(
                state = state,
                reason = WorkoutMutationRejection.SET_NOT_FOUND,
            )
        }
        if (existingSet?.isCompleted == true) {
            return@withLock WorkoutCommandResult.Success(state = state, changed = false)
        }

        val completedAtMs = runtimeValues.nowMs().coerceAtLeast(session.startedAtMs)
        applyMutation(
            state = state,
            sessionId = session.id,
            occurredAtMs = completedAtMs,
            payload = SetUpserted(
                exerciseEntryId = exercise.id,
                set = WorkoutSet(
                    id = existingSet?.id ?: runtimeValues.newId(SET_ID_PREFIX),
                    exerciseEntryId = exercise.id,
                    setNumber = existingSet?.setNumber ?: 1,
                    reps = reps,
                    rpe = rpe,
                    isCompleted = true,
                    completedAtMs = completedAtMs,
                ),
            ),
        )
    }

    override suspend fun finishWorkout(): WorkoutCommandResult = mutationMutex.withLock {
        val state = repository.observeEngineState().first()
        val currentSession = state.session
        if (currentSession?.status == WorkoutSessionStatus.COMPLETED) {
            return@withLock WorkoutCommandResult.Success(state = state, changed = false)
        }
        val session = state.activeSessionOrNull()
            ?: return@withLock WorkoutCommandResult.Rejected(
                state = state,
                reason = WorkoutMutationRejection.NO_ACTIVE_SESSION,
            )
        if (session.completedSets == 0) {
            return@withLock WorkoutCommandResult.InvalidInput(
                state = state,
                error = WorkoutInputError.COMPLETE_A_SET_FIRST,
            )
        }

        val endedAtMs = runtimeValues.nowMs().coerceAtLeast(session.startedAtMs)
        applyMutation(
            state = state,
            sessionId = session.id,
            occurredAtMs = endedAtMs,
            payload = SessionFinished(endedAtMs = endedAtMs),
        )
    }

    private suspend fun applyMutation(
        state: WorkoutEngineState,
        sessionId: String,
        occurredAtMs: Long,
        payload: WorkoutMutationPayload,
    ): WorkoutCommandResult {
        val safeOccurredAtMs = occurredAtMs.coerceAtLeast(0L)
        val mutation = WorkoutMutation(
            mutationId = runtimeValues.newId(MUTATION_ID_PREFIX),
            sessionId = sessionId,
            origin = WorkoutMutationOrigin.PHONE,
            originDeviceId = LOCAL_PHONE_ORIGIN_ID,
            originSequence = repository.nextMutationSequence(
                origin = WorkoutMutationOrigin.PHONE,
                originDeviceId = LOCAL_PHONE_ORIGIN_ID,
            ),
            occurredAtMs = safeOccurredAtMs,
            payload = payload,
        )
        return when (val result = repository.applyMutation(mutation)) {
            is WorkoutMutationApplyResult.Applied -> WorkoutCommandResult.Success(
                state = result.state,
                changed = true,
            )

            is WorkoutMutationApplyResult.AlreadyApplied -> WorkoutCommandResult.Success(
                state = result.state,
                changed = false,
            )

            is WorkoutMutationApplyResult.Rejected -> WorkoutCommandResult.Rejected(
                state = result.state,
                reason = result.reason,
            )
        }
    }

    private fun WorkoutEngineState.activeSessionOrNull(): WorkoutSession? =
        session?.takeIf { it.isActive }

    companion object {
        const val STARTER_EXERCISE_ID = "tio.starter.bodyweight-squat.v1"
        const val STARTER_EXERCISE_NAME = "Bodyweight Squat"
        private const val STARTER_PRIMARY_BODY_PART = "quadriceps"
        private val STARTER_EXERCISE_DEFINITION = ExerciseDefinition(
            id = STARTER_EXERCISE_ID,
            name = STARTER_EXERCISE_NAME,
            primaryMuscleGroups = listOf(STARTER_PRIMARY_BODY_PART),
            trackingType = ExerciseTrackingType.BODYWEIGHT_REPS,
        )
        private const val LOCAL_PHONE_ORIGIN_ID = "tio-local-phone"
        private const val SESSION_ID_PREFIX = "session"
        private const val EXERCISE_ENTRY_ID_PREFIX = "exercise-entry"
        private const val SET_ID_PREFIX = "set"
        private const val MUTATION_ID_PREFIX = "mutation"
        private const val MIN_REPS = 1
        private const val MAX_REPS = 999
        private const val MIN_RPE = 5
        private const val MAX_RPE = 10
    }
}
