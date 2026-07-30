package com.tnyx.features.onboarding.presentation

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initCreatesAndPersistsFreshCheckpoint() = runTest {
        val repository = TestOnboardingRepository()
        val viewModel = OnboardingViewModel(repository)

        viewModel.handleAction(OnboardingAction.Init)
        advanceUntilIdle()

        assertEquals(
            DefaultOnboardingFlow.definition.firstPosition(),
            viewModel.uiState.value.position,
        )
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(repository.checkpoint, repository.savedCheckpoints.single())
    }

    @Test
    fun initResumesCompatibleCheckpointWithoutRewritingIt() = runTest {
        val expected = checkpoint(
            position = position(
                OnboardingSectionIds.BodyGoal,
                OnboardingStepIds.BodyGoalCurrentWeight,
            ),
        )
        val repository = TestOnboardingRepository(expected)
        val viewModel = OnboardingViewModel(repository)

        viewModel.handleAction(OnboardingAction.Init)
        advanceUntilIdle()

        assertEquals(expected.progress.position, viewModel.uiState.value.position)
        assertTrue(repository.savedCheckpoints.isEmpty())
    }

    @Test
    fun answerChangePersistsDraftAndEnablesRequiredStep() = runTest {
        val repository = TestOnboardingRepository()
        val viewModel = initializedViewModel(repository)
        val answer = OnboardingAnswer.Text("Santosh")

        viewModel.handleAction(OnboardingAction.AnswerChanged(answer))
        advanceUntilIdle()

        assertEquals(answer, viewModel.uiState.value.currentAnswer)
        assertTrue(viewModel.uiState.value.canContinue)
        assertEquals(
            answer,
            repository.checkpoint?.draft?.answerFor(OnboardingStepIds.ProfileName),
        )
    }

    @Test
    fun continueWithoutRequiredAnswerStaysOnStepAndShowsValidation() = runTest {
        val repository = TestOnboardingRepository()
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(OnboardingStepIds.ProfileName, viewModel.uiState.value.position?.stepId)
        assertEquals(
            OnboardingValidationError.RequiredAnswerInvalid,
            viewModel.uiState.value.validationError,
        )
        assertTrue(repository.savedCheckpoints.isEmpty())
    }

    @Test
    fun profileNameRequiresTwoToThirtyCharacters() = runTest {
        val repository = TestOnboardingRepository()
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("S")))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("Santosh")))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun profileGenderRequiresSupportedStableId() = runTest {
        val genderPosition = position(
            OnboardingSectionIds.Profile,
            OnboardingStepIds.ProfileGender,
        )
        val repository = TestOnboardingRepository(checkpoint(position = genderPosition))
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("unknown")))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(OnboardingAnswer.Text("prefer_not_to_say")),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun profileDateOfBirthRequiresSupportedPastDate() = runTest {
        val dateOfBirthPosition = position(
            OnboardingSectionIds.Profile,
            OnboardingStepIds.ProfileDateOfBirth,
        )
        val repository = TestOnboardingRepository(checkpoint(position = dateOfBirthPosition))
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(
                OnboardingAnswer.Text(LocalDate.now().plusDays(1).toString()),
            ),
        )
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(
                OnboardingAnswer.Text(LocalDate.of(1990, 1, 1).toString()),
            ),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun bodyGoalPrimaryGoalRequiresSupportedStableId() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.BodyGoal,
                    OnboardingStepIds.BodyGoalPrimaryGoal,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("unknown")))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(OnboardingAnswer.Text("build_muscle")),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun bodyGoalHeightRequiresSupportedRange() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.BodyGoal,
                    OnboardingStepIds.BodyGoalHeight,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Decimal(50.0)))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Decimal(170.0)))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun bodyGoalCurrentWeightRequiresSupportedRange() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.BodyGoal,
                    OnboardingStepIds.BodyGoalCurrentWeight,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Decimal(10.0)))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Decimal(72.5)))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun bodyGoalTargetWeightRequiresSupportedRange() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.BodyGoal,
                    OnboardingStepIds.BodyGoalTargetWeight,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Decimal(0.0)))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Decimal(68.0)))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun bodyGoalActivityLevelRequiresSupportedStableId() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.BodyGoal,
                    OnboardingStepIds.BodyGoalActivityLevel,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("unknown")))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("active")))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun bodyGoalFinalStepContinuesToWorkoutSection() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.BodyGoal,
            OnboardingStepIds.BodyGoalActivityLevel,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Text("active"),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.WorkoutExperience,
            viewModel.uiState.value.position?.stepId,
        )
        assertTrue(
            repository.checkpoint
                ?.progress
                ?.completedSectionIds
                .orEmpty()
                .contains(OnboardingSectionIds.BodyGoal),
        )
    }

    @Test
    fun workoutExperienceRequiresSupportedStableId() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Workout,
                    OnboardingStepIds.WorkoutExperience,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("unknown")))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("beginner")))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun workoutLocationRequiresSupportedStableId() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Workout,
                    OnboardingStepIds.WorkoutLocation,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("outside")))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("both")))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun workoutEquipmentRemainsOptionalWithoutAnswer() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Workout,
                    OnboardingStepIds.WorkoutEquipment,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        assertTrue(viewModel.uiState.value.canContinue)
        assertNull(viewModel.uiState.value.currentAnswer)
    }

    @Test
    fun workoutTrainingDaysRequiresAtLeastOneSupportedDay() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Workout,
                    OnboardingStepIds.WorkoutTrainingDays,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(OnboardingAnswer.Selections(listOf("holiday"))),
        )
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(
                OnboardingAnswer.Selections(listOf("monday", "wednesday")),
            ),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun workoutDurationRequiresSupportedMinutes() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Workout,
                    OnboardingStepIds.WorkoutDuration,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Decimal(15.0)))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Decimal(60.0)))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun workoutFinalStepContinuesToReviewSection() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Workout,
            OnboardingStepIds.WorkoutDuration,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Decimal(60.0),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.ReviewSummary,
            viewModel.uiState.value.position?.stepId,
        )
        assertTrue(
            repository.checkpoint
                ?.progress
                ?.completedSectionIds
                .orEmpty()
                .contains(OnboardingSectionIds.Workout),
        )
    }

    @Test
    fun reviewSummaryRequiresExplicitConfirmation() = runTest {
        val reviewPosition = position(
            OnboardingSectionIds.Review,
            OnboardingStepIds.ReviewSummary,
        )
        val repository = TestOnboardingRepository(
            checkpoint(position = reviewPosition),
        )
        val viewModel = initializedViewModel(repository)

        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Toggle(false)))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Toggle(true)))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun continueAcrossSectionBoundaryMarksPreviousSectionCompleted() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Profile,
            OnboardingStepIds.ProfileDateOfBirth,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Text("1990-01-01"),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.BodyGoalPrimaryGoal,
            viewModel.uiState.value.position?.stepId,
        )
        assertTrue(
            repository.checkpoint
                ?.progress
                ?.completedSectionIds
                .orEmpty()
                .contains(OnboardingSectionIds.Profile),
        )
    }

    @Test
    fun skipWorkoutMovesDirectlyToReviewWithoutCompletingWorkout() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Workout,
                    OnboardingStepIds.WorkoutExperience,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.SkipSectionClicked)
        advanceUntilIdle()

        assertEquals(OnboardingStepIds.ReviewSummary, viewModel.uiState.value.position?.stepId)
        assertFalse(
            repository.checkpoint
                ?.progress
                ?.completedSectionIds
                .orEmpty()
                .contains(OnboardingSectionIds.Workout),
        )
    }

    @Test
    fun backFromFirstStepEmitsExit() = runTest {
        val viewModel = initializedViewModel(TestOnboardingRepository())
        val effects = mutableListOf<OnboardingEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(OnboardingAction.BackClicked)
        advanceUntilIdle()

        assertEquals(listOf(OnboardingEffect.Exit), effects)
        collectJob.cancel()
    }

    @Test
    fun finalAnsweredStepPersistsLocalCompletionAndEmitsCompleted() = runTest {
        val reviewPosition = position(
            OnboardingSectionIds.Review,
            OnboardingStepIds.ReviewSummary,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = reviewPosition,
                draft = OnboardingDraft().withAnswer(
                    reviewPosition.stepId,
                    OnboardingAnswer.Toggle(true),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)
        val effects = mutableListOf<OnboardingEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertTrue(repository.checkpoint?.progress?.isCompleted == true)
        assertEquals(listOf(OnboardingEffect.Completed), effects)
        collectJob.cancel()
    }

    @Test
    fun completedCheckpointEmitsCompletedAfterResume() = runTest {
        val reviewPosition = position(
            OnboardingSectionIds.Review,
            OnboardingStepIds.ReviewSummary,
        )
        val repository = TestOnboardingRepository(
            checkpoint(position = reviewPosition).copy(
                progress = OnboardingProgress(
                    flowVersion = DefaultOnboardingFlow.VERSION,
                    position = reviewPosition,
                    completedSectionIds = DefaultOnboardingFlow.definition.sections
                        .map { section -> section.id }
                        .toSet(),
                    isCompleted = true,
                ),
            ),
        )
        val viewModel = OnboardingViewModel(repository)
        val effects = mutableListOf<OnboardingEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(OnboardingAction.Init)
        advanceUntilIdle()

        assertEquals(listOf(OnboardingEffect.Completed), effects)
        collectJob.cancel()
    }

    @Test
    fun failedAnswerSaveKeepsDraftAndRetryPersistsIt() = runTest {
        val repository = TestOnboardingRepository()
        val viewModel = initializedViewModel(repository)
        repository.failSaves = true
        val answer = OnboardingAnswer.Text("Santosh")

        viewModel.handleAction(OnboardingAction.AnswerChanged(answer))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasPersistenceError)
        assertEquals(answer, viewModel.uiState.value.currentAnswer)
        assertNull(repository.checkpoint?.draft?.answerFor(OnboardingStepIds.ProfileName))

        repository.failSaves = false
        viewModel.handleAction(OnboardingAction.Retry)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasPersistenceError)
        assertEquals(
            answer,
            repository.checkpoint?.draft?.answerFor(OnboardingStepIds.ProfileName),
        )
    }

    @Test
    fun failedInitialLoadCanRetry() = runTest {
        val repository = TestOnboardingRepository().apply {
            failReads = true
        }
        val viewModel = OnboardingViewModel(repository)

        viewModel.handleAction(OnboardingAction.Init)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.hasPersistenceError)
        assertNull(viewModel.uiState.value.position)

        repository.failReads = false
        viewModel.handleAction(OnboardingAction.Retry)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasPersistenceError)
        assertEquals(OnboardingStepIds.ProfileName, viewModel.uiState.value.position?.stepId)
    }

    @Test
    fun backAfterFailedInitialLoadStillEmitsExit() = runTest {
        val repository = TestOnboardingRepository().apply {
            failReads = true
        }
        val viewModel = OnboardingViewModel(repository)
        val effects = mutableListOf<OnboardingEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }
        viewModel.handleAction(OnboardingAction.Init)
        advanceUntilIdle()

        viewModel.handleAction(OnboardingAction.BackClicked)
        advanceUntilIdle()

        assertEquals(listOf(OnboardingEffect.Exit), effects)
        collectJob.cancel()
    }

    private fun TestScope.initializedViewModel(
        repository: TestOnboardingRepository,
    ): OnboardingViewModel {
        val viewModel = OnboardingViewModel(repository)
        viewModel.handleAction(OnboardingAction.Init)
        advanceUntilIdle()
        repository.savedCheckpoints.clear()
        return viewModel
    }

    private fun checkpoint(
        position: OnboardingPosition = DefaultOnboardingFlow.definition.firstPosition(),
        draft: OnboardingDraft = OnboardingDraft(),
    ): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = draft,
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = position,
            ),
        )
    }

    private fun position(
        sectionId: com.tnyx.features.onboarding.domain.model.OnboardingSectionId,
        stepId: com.tnyx.features.onboarding.domain.model.OnboardingStepId,
    ): OnboardingPosition {
        return OnboardingPosition(
            sectionId = sectionId,
            stepId = stepId,
        )
    }
}

private class TestOnboardingRepository(
    initialCheckpoint: OnboardingCheckpoint? = null,
) : OnboardingRepository {
    var checkpoint: OnboardingCheckpoint? = initialCheckpoint
        private set
    var failReads: Boolean = false
    var failSaves: Boolean = false
    val savedCheckpoints = mutableListOf<OnboardingCheckpoint>()

    override fun observeCheckpoint(): Flow<OnboardingCheckpoint?> {
        return if (failReads) {
            flow { error("Expected test read failure") }
        } else {
            flowOf(checkpoint)
        }
    }

    override suspend fun saveCheckpoint(checkpoint: OnboardingCheckpoint) {
        if (failSaves) error("Expected test save failure")
        this.checkpoint = checkpoint
        savedCheckpoints += checkpoint
    }

    override suspend fun clearCheckpoint() {
        checkpoint = null
    }
}
