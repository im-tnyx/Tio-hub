package com.tnyx.features.workout.domain

import com.tnyx.shared.workout.domain.logic.WorkoutMutationRejection
import com.tnyx.shared.workout.domain.logic.WorkoutReducer
import com.tnyx.shared.workout.domain.logic.WorkoutReductionResult
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseTrackingType
import com.tnyx.shared.workout.domain.model.WorkoutEngineState
import com.tnyx.shared.workout.domain.model.WorkoutMutation
import com.tnyx.shared.workout.domain.model.WorkoutRoutine
import com.tnyx.shared.workout.domain.model.WorkoutSession
import com.tnyx.shared.workout.domain.model.WorkoutSessionStatus
import com.tnyx.shared.workout.domain.repository.WorkoutMutationApplyResult
import com.tnyx.shared.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultWorkoutSessionCoordinatorTest {
    @Test
    fun thinSliceAppliesOrderedMutationsAndCreatesOneHistorySession() = runTest {
        val repository = FakeWorkoutRepository()
        val coordinator = coordinator(repository)

        assertChanged(coordinator.startBlankWorkout())
        assertChanged(coordinator.addStarterExercise())
        assertChanged(coordinator.completeOnlySet(reps = 12))
        assertChanged(coordinator.finishWorkout())

        assertEquals(listOf(0L, 1L, 2L, 3L), repository.mutations.map { it.originSequence })
        val dashboard = coordinator.observeDashboard().first()
        assertFalse(dashboard.engineState.session?.isActive == true)
        assertEquals(WorkoutSessionStatus.COMPLETED, dashboard.engineState.session?.status)
        assertEquals(1, dashboard.history.size)
        assertEquals(12, dashboard.history.single().sets.single().reps)
        assertEquals(
            ExerciseTrackingType.BODYWEIGHT_REPS,
            dashboard.history.single().exercises.single().trackingTypeSnapshot,
        )
    }

    @Test
    fun invalidRepsAndPrematureFinishDoNotWriteMutations() = runTest {
        val repository = FakeWorkoutRepository()
        val coordinator = coordinator(repository)
        assertChanged(coordinator.startBlankWorkout())
        assertChanged(coordinator.addStarterExercise())
        val mutationCountBeforeInvalidActions = repository.mutations.size

        val invalidReps = coordinator.completeOnlySet(reps = 0)
        val prematureFinish = coordinator.finishWorkout()

        assertTrue(invalidReps is WorkoutCommandResult.InvalidInput)
        invalidReps as WorkoutCommandResult.InvalidInput
        assertEquals(WorkoutInputError.REPS_OUT_OF_RANGE, invalidReps.error)
        assertTrue(prematureFinish is WorkoutCommandResult.InvalidInput)
        prematureFinish as WorkoutCommandResult.InvalidInput
        assertEquals(WorkoutInputError.COMPLETE_A_SET_FIRST, prematureFinish.error)
        assertEquals(mutationCountBeforeInvalidActions, repository.mutations.size)
    }

    @Test
    fun newCoordinatorRestoresActiveSessionAndCompletedSetFromRepository() = runTest {
        val repository = FakeWorkoutRepository()
        val firstCoordinator = coordinator(repository)
        assertChanged(firstCoordinator.startBlankWorkout())
        assertChanged(firstCoordinator.addStarterExercise())
        assertChanged(firstCoordinator.completeOnlySet(reps = 8))

        val restoredCoordinator = coordinator(repository, idStart = 100)
        val restored = restoredCoordinator.observeDashboard().first().engineState.session

        assertTrue(restored?.isActive == true)
        assertEquals(1, restored?.completedSets)
        assertEquals(8, restored?.sets?.single()?.reps)
        assertEquals(3L, restored?.revision)
    }

    @Test
    fun repeatedFinishIsNoOpAndDoesNotDuplicateHistory() = runTest {
        val repository = FakeWorkoutRepository()
        val coordinator = coordinator(repository)
        assertChanged(coordinator.startBlankWorkout())
        assertChanged(coordinator.addStarterExercise())
        assertChanged(coordinator.completeOnlySet(reps = 10))
        assertChanged(coordinator.finishWorkout())
        val mutationCountAfterFinish = repository.mutations.size

        val repeatedFinish = coordinator.finishWorkout()

        assertTrue(repeatedFinish is WorkoutCommandResult.Success)
        repeatedFinish as WorkoutCommandResult.Success
        assertFalse(repeatedFinish.changed)
        assertEquals(mutationCountAfterFinish, repository.mutations.size)
        assertEquals(1, repository.history.value.size)
    }

    @Test
    fun secondSessionContinuesPersistedDeviceSequence() = runTest {
        val repository = FakeWorkoutRepository()
        val coordinator = coordinator(repository)
        assertChanged(coordinator.startBlankWorkout())
        assertChanged(coordinator.addStarterExercise())
        assertChanged(coordinator.completeOnlySet(reps = 10))
        assertChanged(coordinator.finishWorkout())

        assertChanged(coordinator.startBlankWorkout())
        assertChanged(coordinator.addStarterExercise())

        assertEquals(listOf(0L, 1L, 2L, 3L, 4L, 5L), repository.mutations.map { it.originSequence })
        assertTrue(repository.observeEngineState().first().session?.isActive == true)
    }

    private fun coordinator(
        repository: WorkoutRepository,
        idStart: Int = 1,
    ): DefaultWorkoutSessionCoordinator = DefaultWorkoutSessionCoordinator(
        repository = repository,
        runtimeValues = FakeRuntimeValues(idStart = idStart),
    )

    private fun assertChanged(result: WorkoutCommandResult): WorkoutEngineState {
        assertTrue(result is WorkoutCommandResult.Success)
        result as WorkoutCommandResult.Success
        assertTrue(result.changed)
        return result.state
    }
}

private suspend fun WorkoutSessionCoordinator.completeOnlySet(reps: Int): WorkoutCommandResult {
    val exercise = requireNotNull(observeDashboard().first().engineState.session)
        .exercises
        .single()
    return completeSet(
        exerciseEntryId = exercise.id,
        setId = exercise.sets.firstOrNull()?.id,
        reps = reps,
    )
}

private class FakeRuntimeValues(
    idStart: Int,
) : WorkoutRuntimeValues {
    private var nextId = idStart
    private var nowMs = 1_000L

    override fun nowMs(): Long = nowMs.also { nowMs += 1_000L }

    override fun newId(prefix: String): String = "$prefix-${nextId++}"
}

private class FakeWorkoutRepository : WorkoutRepository {
    private val engineState = MutableStateFlow(WorkoutEngineState())
    val history = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val mutations = mutableListOf<WorkoutMutation>()
    private val mutationsById = mutableMapOf<String, WorkoutMutation>()

    override fun observeExerciseCatalog(): Flow<List<ExerciseDefinition>> = emptyFlow()

    override suspend fun getExerciseDefinition(id: String): ExerciseDefinition? = null

    override fun observeRoutines(): Flow<List<WorkoutRoutine>> = emptyFlow()

    override suspend fun getRoutineById(id: String): WorkoutRoutine? = null

    override fun observeEngineState(): Flow<WorkoutEngineState> = engineState

    override suspend fun nextMutationSequence(
        origin: com.tnyx.shared.workout.domain.model.WorkoutMutationOrigin,
        originDeviceId: String,
    ): Long = mutations
        .filter { it.origin == origin && it.originDeviceId == originDeviceId }
        .maxOfOrNull(WorkoutMutation::originSequence)
        ?.plus(1L)
        ?: 0L

    override suspend fun applyMutation(mutation: WorkoutMutation): WorkoutMutationApplyResult {
        val existing = mutationsById[mutation.mutationId]
        if (existing != null) {
            return if (existing == mutation) {
                WorkoutMutationApplyResult.AlreadyApplied(engineState.value)
            } else {
                WorkoutMutationApplyResult.Rejected(
                    state = engineState.value,
                    reason = WorkoutMutationRejection.MUTATION_ID_CONFLICT,
                )
            }
        }
        val latestSequence = mutations
            .filter { it.origin == mutation.origin && it.originDeviceId == mutation.originDeviceId }
            .maxOfOrNull(WorkoutMutation::originSequence)
        if (latestSequence != null && mutation.originSequence <= latestSequence) {
            return WorkoutMutationApplyResult.Rejected(
                state = engineState.value,
                reason = WorkoutMutationRejection.OUT_OF_ORDER_MUTATION,
            )
        }

        return when (val reduction = WorkoutReducer.reduce(engineState.value, mutation)) {
            is WorkoutReductionResult.Rejected -> WorkoutMutationApplyResult.Rejected(
                state = reduction.state,
                reason = reduction.reason,
            )

            is WorkoutReductionResult.Applied -> {
                engineState.value = reduction.state
                mutations += mutation
                mutationsById[mutation.mutationId] = mutation
                reduction.state.session
                    ?.takeIf { it.status == WorkoutSessionStatus.COMPLETED }
                    ?.let { completed -> history.value = listOf(completed) }
                WorkoutMutationApplyResult.Applied(reduction.state)
            }
        }
    }

    override fun observeSessionHistory(): Flow<List<WorkoutSession>> = history

    override suspend fun getSessionById(id: String): WorkoutSession? =
        engineState.value.session?.takeIf { it.id == id }
            ?: history.value.firstOrNull { it.id == id }
}
