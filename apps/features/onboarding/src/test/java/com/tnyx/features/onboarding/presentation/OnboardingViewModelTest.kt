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
import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
        val viewModel = OnboardingViewModel(repository, TestProfileRepository())

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
        val viewModel = OnboardingViewModel(repository, TestProfileRepository())

        viewModel.handleAction(OnboardingAction.Init)
        advanceUntilIdle()

        assertEquals(expected.progress.position, viewModel.uiState.value.position)
        assertTrue(repository.savedCheckpoints.isEmpty())
    }

    @Test
    fun answerChangePersistsDraftAndEnablesRequiredStep() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Profile,
                    OnboardingStepIds.ProfileName,
                ),
            ),
        )
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
        assertNull(viewModel.uiState.value.validationError)
        assertEquals(1, repository.savedCheckpoints.size)
    }

    @Test
    fun profileNameRequiresTwoToThirtyCharacters() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Profile,
                    OnboardingStepIds.ProfileName,
                ),
            ),
        )
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
    fun bodyGoalActivityLevelContinuesToHealthCondition() = runTest {
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
            OnboardingStepIds.BodyGoalHealthCondition,
            viewModel.uiState.value.position?.stepId,
        )
    }

    @Test
    fun healthConditionRequiresSupportedSelection() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.BodyGoal,
                    OnboardingStepIds.BodyGoalHealthCondition,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(OnboardingAnswer.Selections(listOf("unknown"))),
        )
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(OnboardingAnswer.Selections(listOf("none"))),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun bodyGoalFinalStepContinuesToMobileSection() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.BodyGoal,
            OnboardingStepIds.BodyGoalHealthCondition,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Selections(listOf("none")),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.MobileNumber,
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
    fun mobileNumberRequiresSupportedLength() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Mobile,
                    OnboardingStepIds.MobileNumber,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("12345")))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(OnboardingAnswer.Text("+91 9876543210")),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun mobileStepContinuesToWorkoutIntroSection() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Mobile,
            OnboardingStepIds.MobileNumber,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Text("+91 9876543210"),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.WorkoutIntroChoice,
            viewModel.uiState.value.position?.stepId,
        )
        assertTrue(
            repository.checkpoint
                ?.progress
                ?.completedSectionIds
                .orEmpty()
                .contains(OnboardingSectionIds.Mobile),
        )
    }

    @Test
    fun workoutIntroRequiresExplicitChoice() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.WorkoutIntro,
                    OnboardingStepIds.WorkoutIntroChoice,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Toggle(false)))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun workoutIntroYesContinuesToWorkoutSection() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.WorkoutIntro,
            OnboardingStepIds.WorkoutIntroChoice,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Toggle(true),
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
    }

    @Test
    fun workoutIntroNoContinuesToTargetsSection() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.WorkoutIntro,
            OnboardingStepIds.WorkoutIntroChoice,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Toggle(false),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.TargetsStepsTarget,
            viewModel.uiState.value.position?.stepId,
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
    fun workoutExperienceContinuesToGymAccess() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Workout,
            OnboardingStepIds.WorkoutExperience,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Text("beginner"),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.WorkoutGymAccess,
            viewModel.uiState.value.position?.stepId,
        )
    }

    @Test
    fun workoutGymAccessRequiresSupportedStableId() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Workout,
                    OnboardingStepIds.WorkoutGymAccess,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("office")))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("both")))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun workoutGymAccessContinuesToLocation() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Workout,
            OnboardingStepIds.WorkoutGymAccess,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Text("both"),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.WorkoutLocation,
            viewModel.uiState.value.position?.stepId,
        )
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
    fun workoutLocationContinuesToFocusAreas() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Workout,
            OnboardingStepIds.WorkoutLocation,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Text("both"),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.WorkoutFocusAreas,
            viewModel.uiState.value.position?.stepId,
        )
    }

    @Test
    fun workoutFocusAreasRequireSupportedSelectionShape() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Workout,
                    OnboardingStepIds.WorkoutFocusAreas,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(OnboardingAnswer.Selections(listOf("full_body", "arms"))),
        )
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(OnboardingAnswer.Selections(listOf("arms", "back"))),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun workoutFocusAreasContinueToEquipment() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Workout,
            OnboardingStepIds.WorkoutFocusAreas,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft()
                    .withAnswer(OnboardingStepIds.WorkoutGymAccess, OnboardingAnswer.Text("both"))
                    .withAnswer(
                        currentPosition.stepId,
                        OnboardingAnswer.Selections(listOf("arms", "back")),
                    ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.WorkoutEquipment,
            viewModel.uiState.value.position?.stepId,
        )
    }

    @Test
    fun gymOnlyFocusAreasSkipEquipment() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Workout,
            OnboardingStepIds.WorkoutFocusAreas,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft()
                    .withAnswer(OnboardingStepIds.WorkoutGymAccess, OnboardingAnswer.Text("gym"))
                    .withAnswer(
                        currentPosition.stepId,
                        OnboardingAnswer.Selections(listOf("arms", "back")),
                    ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.WorkoutTrainingDays,
            viewModel.uiState.value.position?.stepId,
        )
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
    fun workoutDurationContinuesToSplit() = runTest {
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
            OnboardingStepIds.WorkoutSplit,
            viewModel.uiState.value.position?.stepId,
        )
    }

    @Test
    fun workoutSplitRequiresSupportedStableId() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Workout,
                    OnboardingStepIds.WorkoutSplit,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("bro_split")))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("auto")))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun workoutSplitContinuesToHealthConcerns() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Workout,
            OnboardingStepIds.WorkoutSplit,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Text("auto"),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.WorkoutHealthConcerns,
            viewModel.uiState.value.position?.stepId,
        )
    }

    @Test
    fun workoutHealthConcernsRemainsOptionalWithoutAnswer() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Workout,
                    OnboardingStepIds.WorkoutHealthConcerns,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        assertTrue(viewModel.uiState.value.canContinue)
        assertNull(viewModel.uiState.value.currentAnswer)
    }

    @Test
    fun workoutHealthConcernsContinuesToSpecialEventGoal() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Workout,
            OnboardingStepIds.WorkoutHealthConcerns,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.WorkoutSpecialEventGoal,
            viewModel.uiState.value.position?.stepId,
        )
    }

    @Test
    fun workoutSpecialEventRemainsOptionalWithoutAnswer() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Workout,
                    OnboardingStepIds.WorkoutSpecialEventGoal,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        assertTrue(viewModel.uiState.value.canContinue)
        assertNull(viewModel.uiState.value.currentAnswer)
    }

    @Test
    fun workoutFinalStepContinuesToTargetsSection() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Workout,
            OnboardingStepIds.WorkoutSpecialEventGoal,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.TargetsStepsTarget,
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
    fun targetsStepsTargetRequiresSupportedRange() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Targets,
                    OnboardingStepIds.TargetsStepsTarget,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Decimal(1500.0)))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Decimal(8000.0)))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun targetsSleepTargetRequiresSupportedStableId() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Targets,
                    OnboardingStepIds.TargetsSleepTarget,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("night_owl")))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(OnboardingAnswer.Text("balanced_evenings")),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun targetsWaterTargetRequiresSupportedRange() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Targets,
                    OnboardingStepIds.TargetsWaterTarget,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Decimal(250.0)))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Decimal(2500.0)))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun targetsGoalPaceRequiresSupportedStableId() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Targets,
                    OnboardingStepIds.TargetsGoalPace,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("fast")))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("steady")))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun targetsNutritionSummaryRequiresSupportedStableId() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Targets,
                    OnboardingStepIds.TargetsNutritionSummary,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(OnboardingAnswer.Text("meal_skipper")),
        )
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(OnboardingAnswer.Text("protein_priority")),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun targetsRecommendationSummaryIsAutoSeededAndContinuable() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Targets,
                    OnboardingStepIds.TargetsRecommendationSummary,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        assertEquals(
            OnboardingAnswer.Toggle(true),
            viewModel.uiState.value.currentAnswer,
        )
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun targetsRecommendationSummaryContinuesToGoalPace() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Targets,
            OnboardingStepIds.TargetsRecommendationSummary,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Toggle(true),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.TargetsGoalPace,
            viewModel.uiState.value.position?.stepId,
        )
    }

    @Test
    fun targetsGoalPaceContinuesToNutritionSummary() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Targets,
            OnboardingStepIds.TargetsGoalPace,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Text("steady"),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.TargetsNutritionSummary,
            viewModel.uiState.value.position?.stepId,
        )
    }

    @Test
    fun targetsFinalStepContinuesToSourceSection() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Targets,
            OnboardingStepIds.TargetsNutritionSummary,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Text("protein_priority"),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.SourceChannel,
            viewModel.uiState.value.position?.stepId,
        )
        assertTrue(
            repository.checkpoint
                ?.progress
                ?.completedSectionIds
                .orEmpty()
                .contains(OnboardingSectionIds.Targets),
        )
    }

    @Test
    fun sourceChannelRequiresSupportedStableId() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Source,
                    OnboardingStepIds.SourceChannel,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("newspaper")))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(OnboardingAnswer.Text("social_media")),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun sourceReasonRequiresSupportedStableId() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Source,
                    OnboardingStepIds.SourceReason,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("just_testing")))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.handleAction(
            OnboardingAction.AnswerChanged(OnboardingAnswer.Text("complete_reset")),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun sourceReferralDetailAllowsBlankOrMeaningfulValue() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Source,
                    OnboardingStepIds.SourceReferralDetail,
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        assertTrue(viewModel.uiState.value.canContinue)

        viewModel.handleAction(OnboardingAction.AnswerChanged(OnboardingAnswer.Text("x")))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun sourceChannelContinuesToReasonStep() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Source,
            OnboardingStepIds.SourceChannel,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Text("friend_referral"),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.SourceReason,
            viewModel.uiState.value.position?.stepId,
        )
    }

    @Test
    fun sourceReasonContinuesToReferralDetailStep() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Source,
            OnboardingStepIds.SourceReason,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Text("complete_reset"),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.SourceReferralDetail,
            viewModel.uiState.value.position?.stepId,
        )
    }

    @Test
    fun sourceFinalStepContinuesToReviewSection() = runTest {
        val currentPosition = position(
            OnboardingSectionIds.Source,
            OnboardingStepIds.SourceReferralDetail,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = currentPosition,
                draft = OnboardingDraft().withAnswer(
                    currentPosition.stepId,
                    OnboardingAnswer.Text("Coach Neha"),
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
                .contains(OnboardingSectionIds.Source),
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
    fun skipWorkoutMovesDirectlyToTargetsWithoutCompletingWorkout() = runTest {
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

        assertEquals(OnboardingStepIds.TargetsStepsTarget, viewModel.uiState.value.position?.stepId)
        assertFalse(
            repository.checkpoint
                ?.progress
                ?.completedSectionIds
                .orEmpty()
                .contains(OnboardingSectionIds.Workout),
        )
    }

    @Test
    fun backFromTargetsReturnsWorkoutIntroWhenWorkoutWasDeclined() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Targets,
                    OnboardingStepIds.TargetsStepsTarget,
                ),
                draft = OnboardingDraft().withAnswer(
                    OnboardingStepIds.WorkoutIntroChoice,
                    OnboardingAnswer.Toggle(false),
                ),
            ),
        )
        val viewModel = initializedViewModel(repository)

        viewModel.handleAction(OnboardingAction.BackClicked)
        advanceUntilIdle()

        assertEquals(
            OnboardingStepIds.WorkoutIntroChoice,
            viewModel.uiState.value.position?.stepId,
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
    fun finalAnsweredStepPersistsLocalCompletionAndShowsReadyCompletionState() = runTest {
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

        assertTrue(repository.savedCheckpoints.last().progress.isCompleted)
        assertEquals(OnboardingCompletionStage.Ready, viewModel.uiState.value.completionStage)
        assertTrue(effects.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun completionReadyStateEmitsCompletedAfterSecondContinue() = runTest {
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
        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

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
        val profileRepository = TestProfileRepository()
        val viewModel = OnboardingViewModel(repository, profileRepository)
        val effects = mutableListOf<OnboardingEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(OnboardingAction.Init)
        advanceUntilIdle()

        assertEquals(listOf(OnboardingEffect.Completed), effects)
        assertTrue(profileRepository.currentProfile.value.hasCompletedOnboarding)
        assertEquals(1, repository.clearedCheckpointCount)
        collectJob.cancel()
    }

    @Test
    fun failedAnswerSaveKeepsDraftAndRetryPersistsIt() = runTest {
        val repository = TestOnboardingRepository(
            checkpoint(
                position = position(
                    OnboardingSectionIds.Profile,
                    OnboardingStepIds.ProfileName,
                ),
            ),
        )
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
        val viewModel = OnboardingViewModel(repository, TestProfileRepository())

        viewModel.handleAction(OnboardingAction.Init)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.hasPersistenceError)
        assertNull(viewModel.uiState.value.position)

        repository.failReads = false
        viewModel.handleAction(OnboardingAction.Retry)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasPersistenceError)
        assertEquals(OnboardingStepIds.IntroWelcome, viewModel.uiState.value.position?.stepId)
    }

    @Test
    fun backAfterFailedInitialLoadStillEmitsExit() = runTest {
        val repository = TestOnboardingRepository().apply {
            failReads = true
        }
        val viewModel = OnboardingViewModel(repository, TestProfileRepository())
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

    @Test
    fun finalAnsweredStepFinalizesProfileAndClearsCheckpoint() = runTest {
        val reviewPosition = position(
            OnboardingSectionIds.Review,
            OnboardingStepIds.ReviewSummary,
        )
        val repository = TestOnboardingRepository(
            checkpoint(
                position = reviewPosition,
                draft = OnboardingDraft()
                    .withAnswer(
                        OnboardingStepIds.ProfileName,
                        OnboardingAnswer.Text("Santosh Kumar"),
                    )
                    .withAnswer(
                        OnboardingStepIds.ProfileDateOfBirth,
                        OnboardingAnswer.Text("1990-01-01"),
                    )
                    .withAnswer(
                        OnboardingStepIds.ProfileGender,
                        OnboardingAnswer.Text("male"),
                    )
                    .withAnswer(
                        OnboardingStepIds.BodyGoalHeight,
                        OnboardingAnswer.Decimal(176.0),
                    )
                    .withAnswer(
                        OnboardingStepIds.BodyGoalCurrentWeight,
                        OnboardingAnswer.Decimal(74.5),
                    )
                    .withAnswer(
                        OnboardingStepIds.BodyGoalTargetWeight,
                        OnboardingAnswer.Decimal(70.0),
                    )
                    .withAnswer(
                        reviewPosition.stepId,
                        OnboardingAnswer.Toggle(true),
                    ),
            ),
        )
        val profileRepository = TestProfileRepository()
        val viewModel = initializedViewModel(repository, profileRepository)
        val effects = mutableListOf<OnboardingEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertTrue(repository.savedCheckpoints.last().progress.isCompleted)
        assertEquals(1, repository.clearedCheckpointCount)
        assertEquals(OnboardingCompletionStage.Ready, viewModel.uiState.value.completionStage)
        assertTrue(effects.isEmpty())
        assertEquals("Santosh Kumar", profileRepository.currentProfile.value.displayName)
        assertEquals("1990-01-01", profileRepository.currentProfile.value.dob)
        assertEquals("male", profileRepository.currentProfile.value.gender)
        assertEquals(176, profileRepository.currentProfile.value.height)
        assertEquals(74.5, profileRepository.currentProfile.value.weight, 0.0)
        assertEquals(74.5, profileRepository.currentProfile.value.currentJourney.initialWeight, 0.0)
        assertEquals(70.0, profileRepository.currentProfile.value.currentJourney.targetWeight, 0.0)
        assertTrue(profileRepository.currentProfile.value.hasCompletedOnboarding)
        collectJob.cancel()
    }

    @Test
    fun failedProfileFinalizationCanRetryFromCompletedCheckpoint() = runTest {
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
        val profileRepository = TestProfileRepository().apply {
            failUpdates = true
        }
        val viewModel = initializedViewModel(repository, profileRepository)

        viewModel.handleAction(OnboardingAction.ContinueClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasPersistenceError)
        assertFalse(profileRepository.currentProfile.value.hasCompletedOnboarding)

        profileRepository.failUpdates = false
        viewModel.handleAction(OnboardingAction.Retry)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasPersistenceError)
        assertEquals(OnboardingCompletionStage.Ready, viewModel.uiState.value.completionStage)
        assertTrue(profileRepository.currentProfile.value.hasCompletedOnboarding)
        assertEquals(1, repository.clearedCheckpointCount)
    }

    private fun TestScope.initializedViewModel(
        repository: TestOnboardingRepository,
        profileRepository: TestProfileRepository = TestProfileRepository(),
    ): OnboardingViewModel {
        val viewModel = OnboardingViewModel(repository, profileRepository)
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
    var clearedCheckpointCount: Int = 0
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
        clearedCheckpointCount += 1
    }
}

private class TestProfileRepository(
    initialProfile: UserProfile = UserProfile(
        id = "local-guest",
        displayName = "",
        dob = "",
        gender = "",
        planLabel = "",
        weight = 0.0,
        height = 0,
        bmi = 0.0,
        bmr = 0,
    ),
) : ProfileRepository {
    val currentProfile = MutableStateFlow(initialProfile)
    var failUpdates: Boolean = false

    override fun getCurrentProfile(): Flow<UserProfile> = currentProfile

    override fun getProfile(userId: String): Flow<UserProfile> = currentProfile

    override suspend fun updateProfile(profile: UserProfile) {
        if (failUpdates) {
            error("Expected test profile update failure")
        }
        currentProfile.value = profile
    }

    override suspend fun updateAvatar(jpegBytes: ByteArray): String = ""

    override suspend fun removeAvatar() = Unit
}
