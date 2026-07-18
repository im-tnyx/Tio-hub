package com.tnyx.features.workout.presentation

import com.tnyx.features.workout.domain.WorkoutCommandResult
import com.tnyx.features.workout.domain.WorkoutDashboard
import com.tnyx.features.workout.domain.WorkoutSessionCoordinator
import com.tnyx.shared.workout.domain.model.ExerciseTrackingType
import com.tnyx.shared.workout.domain.model.WorkoutEngineState
import com.tnyx.shared.workout.domain.model.WorkoutExercise
import com.tnyx.shared.workout.domain.model.WorkoutSession
import com.tnyx.shared.workout.domain.model.WorkoutSessionStatus
import com.tnyx.shared.workout.domain.model.WorkoutSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutRpeAndPreviousTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun previousSetIsMappedAndCopiedIntoCurrentDraft() =
        runTest(mainDispatcherRule.testDispatcher) {
            val coordinator = RpeCoordinator(previousDashboard())
            val viewModel = WorkoutViewModel(coordinator)
            advanceUntilIdle()

            val exercise = viewModel.uiState.value.exercises.single()
            val initialSet = exercise.sets.single()
            assertEquals("× 12 · RPE 8", initialSet.previousSummary)
            assertEquals("12", initialSet.metrics.single().previousValue)
            assertEquals("8", initialSet.previousRpe)

            viewModel.handleAction(
                WorkoutAction.PreviousSetClicked(
                    exerciseEntryId = exercise.id,
                    setId = initialSet.id,
                ),
            )

            val copiedSet = viewModel.uiState.value.exercises.single().sets.single()
            assertEquals("12", copiedSet.metrics.single().value)
            assertEquals("8", copiedSet.rpeValue)
        }

    @Test
    fun selectedRpeIsSentWhenSetIsCompleted() =
        runTest(mainDispatcherRule.testDispatcher) {
            val coordinator = RpeCoordinator(previousDashboard())
            val viewModel = WorkoutViewModel(coordinator)
            advanceUntilIdle()
            val exercise = viewModel.uiState.value.exercises.single()
            val set = exercise.sets.single()

            viewModel.handleAction(WorkoutAction.RpeClicked(exercise.id, set.id))
            viewModel.handleAction(WorkoutAction.RpeSelected(9))
            viewModel.handleAction(WorkoutAction.CompleteSetClicked(exercise.id, set.id))
            advanceUntilIdle()

            assertEquals(10, coordinator.completedReps)
            assertEquals(9, coordinator.completedRpe)
            assertNull(viewModel.uiState.value.rpePicker)
        }
}

private class RpeCoordinator(initialDashboard: WorkoutDashboard) : WorkoutSessionCoordinator {
    private val dashboard = MutableStateFlow(initialDashboard)
    var completedReps: Int? = null
    var completedRpe: Int? = null

    override fun observeDashboard(): Flow<WorkoutDashboard> = dashboard

    override suspend fun startBlankWorkout(): WorkoutCommandResult = success()

    override suspend fun addStarterExercise(): WorkoutCommandResult = success()

    override suspend fun completeSet(
        exerciseEntryId: String,
        setId: String?,
        reps: Int,
    ): WorkoutCommandResult = completeSet(exerciseEntryId, setId, reps, null)

    override suspend fun completeSet(
        exerciseEntryId: String,
        setId: String?,
        reps: Int,
        rpe: Int?,
    ): WorkoutCommandResult {
        completedReps = reps
        completedRpe = rpe
        return success()
    }

    override suspend fun finishWorkout(): WorkoutCommandResult = success()

    private fun success(): WorkoutCommandResult = WorkoutCommandResult.Success(
        state = dashboard.value.engineState,
        changed = true,
    )
}

private fun previousDashboard(): WorkoutDashboard {
    val exerciseId = "starter-exercise"
    val activeEntryId = "active-entry"
    val historyEntryId = "history-entry"
    return WorkoutDashboard(
        engineState = WorkoutEngineState(
            session = WorkoutSession(
                id = "active-session",
                startedAtMs = 2_000L,
                exercises = listOf(
                    WorkoutExercise(
                        id = activeEntryId,
                        exerciseId = exerciseId,
                        exerciseNameSnapshot = "Bodyweight Squat",
                        order = 0,
                        trackingTypeSnapshot = ExerciseTrackingType.BODYWEIGHT_REPS,
                    ),
                ),
            ),
        ),
        history = listOf(
            WorkoutSession(
                id = "history-session",
                startedAtMs = 500L,
                endedAtMs = 1_500L,
                status = WorkoutSessionStatus.COMPLETED,
                exercises = listOf(
                    WorkoutExercise(
                        id = historyEntryId,
                        exerciseId = exerciseId,
                        exerciseNameSnapshot = "Bodyweight Squat",
                        order = 0,
                        trackingTypeSnapshot = ExerciseTrackingType.BODYWEIGHT_REPS,
                        sets = listOf(
                            WorkoutSet(
                                id = "history-set",
                                exerciseEntryId = historyEntryId,
                                setNumber = 1,
                                reps = 12,
                                rpe = 8,
                                isCompleted = true,
                                completedAtMs = 1_000L,
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )
}
