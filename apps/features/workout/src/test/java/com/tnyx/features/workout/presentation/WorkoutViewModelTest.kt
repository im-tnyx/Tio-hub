package com.tnyx.features.workout.presentation

import com.tnyx.features.workout.domain.WorkoutCommandResult
import com.tnyx.features.workout.domain.WorkoutDashboard
import com.tnyx.features.workout.domain.WorkoutSessionCoordinator
import com.tnyx.shared.workout.domain.model.WorkoutEngineState
import com.tnyx.shared.workout.domain.model.ExerciseTrackingType
import com.tnyx.shared.workout.domain.model.WorkoutExercise
import com.tnyx.shared.workout.domain.model.WorkoutSession
import com.tnyx.shared.workout.domain.model.WorkoutSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun restoredDashboardMapsActiveSessionAndCompletedSetToUiState() =
        runTest(mainDispatcherRule.testDispatcher) {
            val coordinator = FakeWorkoutSessionCoordinator()
            val viewModel = WorkoutViewModel(coordinator)
            coordinator.dashboard.value = restoredDashboard()

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.hasActiveSession)
            val exercise = state.exercises.single()
            assertEquals("Bodyweight Squat", exercise.name)
            val set = exercise.sets.single()
            assertTrue(set.isCompleted)
            assertEquals(
                "8",
                set.metrics.single { it.field == WorkoutMetricField.REPS }.value,
            )
        }

    @Test
    fun repsMetricIsSanitizedBeforeCoordinatorCommand() =
        runTest(mainDispatcherRule.testDispatcher) {
            val coordinator = FakeWorkoutSessionCoordinator()
            val viewModel = WorkoutViewModel(coordinator)
            coordinator.dashboard.value = restoredDashboard(isCompleted = false)
            advanceUntilIdle()

            val exercise = viewModel.uiState.value.exercises.single()
            val set = exercise.sets.single()
            viewModel.handleAction(
                WorkoutAction.MetricChanged(
                    exerciseEntryId = exercise.id,
                    setId = set.id,
                    field = WorkoutMetricField.REPS,
                    value = "12a4b5",
                ),
            )
            viewModel.handleAction(
                WorkoutAction.CompleteSetClicked(
                    exerciseEntryId = exercise.id,
                    setId = set.id,
                ),
            )
            advanceUntilIdle()

            assertEquals(
                "124",
                viewModel.uiState.value.exercises
                    .single()
                    .sets
                    .single()
                    .metrics
                    .single { it.field == WorkoutMetricField.REPS }
                    .value,
            )
            assertEquals(124, coordinator.completedMetricReps)
            assertFalse(viewModel.uiState.value.isMutating)
        }

    @Test
    fun metricChangeTargetsOnlyTheSelectedExercise() =
        runTest(mainDispatcherRule.testDispatcher) {
            val coordinator = FakeWorkoutSessionCoordinator()
            val base = restoredDashboard(isCompleted = false)
            val session = requireNotNull(base.engineState.session)
            val first = session.exercises.single()
            coordinator.dashboard.value = base.copy(
                engineState = base.engineState.copy(
                    session = session.copy(
                        exercises = listOf(
                            first,
                            first.copy(
                                id = "exercise-entry-2",
                                exerciseId = "starter-exercise-2",
                                exerciseNameSnapshot = "Push-up",
                                order = 1,
                            ),
                        ),
                    ),
                ),
            )
            val viewModel = WorkoutViewModel(coordinator)
            advanceUntilIdle()

            val firstExercise = viewModel.uiState.value.exercises.first()
            viewModel.handleAction(
                WorkoutAction.MetricChanged(
                    exerciseEntryId = firstExercise.id,
                    setId = firstExercise.sets.single().id,
                    field = WorkoutMetricField.REPS,
                    value = "12",
                ),
            )

            val exercises = viewModel.uiState.value.exercises
            assertEquals("12", exercises.first().sets.single().metrics.single().value)
            assertEquals("10", exercises.last().sets.single().metrics.single().value)
        }
}

private class FakeWorkoutSessionCoordinator : WorkoutSessionCoordinator {
    val dashboard = MutableStateFlow(
        WorkoutDashboard(
            engineState = WorkoutEngineState(),
            history = emptyList(),
        ),
    )
    var completedMetricReps: Int? = null

    override fun observeDashboard(): Flow<WorkoutDashboard> = dashboard

    override suspend fun startBlankWorkout(): WorkoutCommandResult = success()

    override suspend fun addStarterExercise(): WorkoutCommandResult = success()

    override suspend fun completeSet(
        exerciseEntryId: String,
        setId: String?,
        reps: Int,
    ): WorkoutCommandResult {
        completedMetricReps = reps
        return success()
    }

    override suspend fun finishWorkout(): WorkoutCommandResult = success()

    private fun success(): WorkoutCommandResult = WorkoutCommandResult.Success(
        state = dashboard.value.engineState,
        changed = true,
    )
}

private fun restoredDashboard(isCompleted: Boolean = true): WorkoutDashboard {
    val exerciseEntryId = "exercise-entry-1"
    return WorkoutDashboard(
        engineState = WorkoutEngineState(
            session = WorkoutSession(
                id = "session-1",
                startedAtMs = 1_000L,
                revision = 3L,
                exercises = listOf(
                    WorkoutExercise(
                        id = exerciseEntryId,
                        exerciseId = "starter-exercise",
                        exerciseNameSnapshot = "Bodyweight Squat",
                        order = 0,
                        trackingTypeSnapshot = ExerciseTrackingType.BODYWEIGHT_REPS,
                        sets = listOf(
                            WorkoutSet(
                                id = "set-1",
                                exerciseEntryId = exerciseEntryId,
                                setNumber = 1,
                                reps = 8,
                                isCompleted = isCompleted,
                                completedAtMs = if (isCompleted) 2_000L else null,
                            ),
                        ),
                    ),
                ),
            ),
        ),
        history = emptyList(),
    )
}
