package com.tnyx.data.workout

import androidx.room.withTransaction
import com.tnyx.data.workout.local.WorkoutDao
import com.tnyx.data.workout.local.WorkoutDatabase
import com.tnyx.shared.workout.domain.logic.WorkoutMutationRejection
import com.tnyx.shared.workout.domain.logic.WorkoutReducer
import com.tnyx.shared.workout.domain.logic.WorkoutReductionResult
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.WorkoutEngineState
import com.tnyx.shared.workout.domain.model.WorkoutMutation
import com.tnyx.shared.workout.domain.model.WorkoutMutationOrigin
import com.tnyx.shared.workout.domain.model.WorkoutRoutine
import com.tnyx.shared.workout.domain.model.WorkoutSession
import com.tnyx.shared.workout.domain.model.WorkoutSessionStatus
import com.tnyx.shared.workout.domain.repository.WorkoutMutationApplyResult
import com.tnyx.shared.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class RoomWorkoutRepository(
    private val database: WorkoutDatabase,
    private val dao: WorkoutDao,
    private val codec: WorkoutPersistenceCodec
) : WorkoutRepository {
    override fun observeExerciseCatalog(): Flow<List<ExerciseDefinition>> =
        dao.observeExerciseDefinitions()
            .map { entities -> entities.map { codec.decodeExerciseDefinition(it.definitionJson) } }
            .distinctUntilChanged()

    override suspend fun getExerciseDefinition(id: String): ExerciseDefinition? =
        dao.getExerciseDefinitionById(id)?.let { codec.decodeExerciseDefinition(it.definitionJson) }

    override fun observeRoutines(): Flow<List<WorkoutRoutine>> =
        dao.observeRoutines()
            .map { entities -> entities.map { codec.decodeRoutine(it.routineJson) } }
            .distinctUntilChanged()

    override suspend fun getRoutineById(id: String): WorkoutRoutine? =
        dao.getRoutineById(id)?.let { codec.decodeRoutine(it.routineJson) }

    override fun observeEngineState(): Flow<WorkoutEngineState> =
        dao.observeEngineState()
            .map { entity -> entity?.let { codec.decodeEngineState(it.stateJson) } ?: WorkoutEngineState() }
            .distinctUntilChanged()

    override suspend fun nextMutationSequence(
        origin: WorkoutMutationOrigin,
        originDeviceId: String
    ): Long {
        val latest = dao.getLatestOriginSequence(
            origin = origin.name,
            originDeviceId = originDeviceId
        ) ?: return 0L
        check(latest < Long.MAX_VALUE) { "Workout mutation sequence exhausted" }
        return latest + 1L
    }

    override suspend fun applyMutation(mutation: WorkoutMutation): WorkoutMutationApplyResult =
        database.withTransaction {
            val currentState = readCurrentState()
            val persistedMutation = dao.getMutationById(mutation.mutationId)
            if (persistedMutation != null) {
                return@withTransaction if (codec.decodeMutation(persistedMutation.mutationJson) == mutation) {
                    WorkoutMutationApplyResult.AlreadyApplied(currentState)
                } else {
                    WorkoutMutationApplyResult.Rejected(
                        state = currentState,
                        reason = WorkoutMutationRejection.MUTATION_ID_CONFLICT
                    )
                }
            }

            when (val reduction = WorkoutReducer.reduce(currentState, mutation)) {
                is WorkoutReductionResult.Rejected -> WorkoutMutationApplyResult.Rejected(
                    state = reduction.state,
                    reason = reduction.reason
                )

                is WorkoutReductionResult.Applied -> {
                    val latestSequence = dao.getLatestOriginSequence(
                        origin = mutation.origin.name,
                        originDeviceId = mutation.originDeviceId
                    )
                    if (latestSequence != null && mutation.originSequence <= latestSequence) {
                        return@withTransaction WorkoutMutationApplyResult.Rejected(
                            state = currentState,
                            reason = WorkoutMutationRejection.OUT_OF_ORDER_MUTATION
                        )
                    }

                    val resultingState = reduction.state
                    dao.upsertEngineState(
                        resultingState.toEntity(codec, updatedAtMs = mutation.occurredAtMs)
                    )
                    dao.insertMutation(mutation.toOutboxEntity(codec, resultingState))
                    resultingState.session
                        ?.takeIf { it.status == WorkoutSessionStatus.COMPLETED }
                        ?.let { completedSession ->
                            dao.upsertSessionHistory(
                                completedSession.toHistoryEntity(codec, mutation.occurredAtMs)
                            )
                        }
                    WorkoutMutationApplyResult.Applied(resultingState)
                }
            }
        }

    override fun observeSessionHistory(): Flow<List<WorkoutSession>> =
        dao.observeSessionHistory()
            .map { entities -> entities.map { codec.decodeSession(it.sessionJson) } }
            .distinctUntilChanged()

    override suspend fun getSessionById(id: String): WorkoutSession? {
        val current = readCurrentState().session
        if (current?.id == id) return current
        return dao.getSessionHistoryById(id)?.let { codec.decodeSession(it.sessionJson) }
    }

    private suspend fun readCurrentState(): WorkoutEngineState =
        dao.getEngineState()?.let { codec.decodeEngineState(it.stateJson) } ?: WorkoutEngineState()
}
