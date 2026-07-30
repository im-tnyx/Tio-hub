package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateOnboardingAnswerUseCaseTest {
    private val useCase = ValidateOnboardingAnswerUseCase()

    @Test
    fun introWelcomeIsAlwaysContinuable() {
        assertTrue(useCase(OnboardingStepIds.IntroWelcome, null))
    }

    @Test
    fun validatesProfileNameLength() {
        assertFalse(useCase(OnboardingStepIds.ProfileName, OnboardingAnswer.Text("S")))
        assertTrue(useCase(OnboardingStepIds.ProfileName, OnboardingAnswer.Text("Santosh")))
    }

    @Test
    fun validatesDateOfBirthAgainstPastDateWindow() {
        assertFalse(
            useCase(
                OnboardingStepIds.ProfileDateOfBirth,
                OnboardingAnswer.Text(LocalDate.now().plusDays(1).toString()),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.ProfileDateOfBirth,
                OnboardingAnswer.Text("1990-01-01"),
            ),
        )
    }

    @Test
    fun validatesMobileNumberLength() {
        assertFalse(useCase(OnboardingStepIds.MobileNumber, OnboardingAnswer.Text("12345")))
        assertTrue(useCase(OnboardingStepIds.MobileNumber, OnboardingAnswer.Text("+91 9876543210")))
    }

    @Test
    fun validatesHealthConditionSelections() {
        assertFalse(useCase(OnboardingStepIds.BodyGoalHealthCondition, null))
        assertFalse(
            useCase(
                OnboardingStepIds.BodyGoalHealthCondition,
                OnboardingAnswer.Selections(listOf("unknown")),
            ),
        )
        assertFalse(
            useCase(
                OnboardingStepIds.BodyGoalHealthCondition,
                OnboardingAnswer.Selections(listOf("none", "diabetes")),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.BodyGoalHealthCondition,
                OnboardingAnswer.Selections(listOf("none")),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.BodyGoalHealthCondition,
                OnboardingAnswer.Selections(listOf("diabetes", "injury_recovery")),
            ),
        )
    }

    @Test
    fun validatesWorkoutGymAccessAgainstStableIds() {
        assertFalse(useCase(OnboardingStepIds.WorkoutGymAccess, null))
        assertFalse(
            useCase(
                OnboardingStepIds.WorkoutGymAccess,
                OnboardingAnswer.Text("office"),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.WorkoutGymAccess,
                OnboardingAnswer.Text("both"),
            ),
        )
    }

    @Test
    fun validatesWorkoutFocusAreaSelections() {
        assertFalse(useCase(OnboardingStepIds.WorkoutFocusAreas, null))
        assertFalse(
            useCase(
                OnboardingStepIds.WorkoutFocusAreas,
                OnboardingAnswer.Selections(listOf("neck")),
            ),
        )
        assertFalse(
            useCase(
                OnboardingStepIds.WorkoutFocusAreas,
                OnboardingAnswer.Selections(listOf("full_body", "arms")),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.WorkoutFocusAreas,
                OnboardingAnswer.Selections(listOf("arms", "back")),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.WorkoutFocusAreas,
                OnboardingAnswer.Selections(
                    listOf("full_body", "shoulders", "arms", "back", "chest", "abs", "glutes", "legs", "cardio"),
                ),
            ),
        )
    }

    @Test
    fun validatesWorkoutSplitAgainstStableIds() {
        assertFalse(useCase(OnboardingStepIds.WorkoutSplit, null))
        assertFalse(
            useCase(
                OnboardingStepIds.WorkoutSplit,
                OnboardingAnswer.Text("bro_split"),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.WorkoutSplit,
                OnboardingAnswer.Text("upper_lower"),
            ),
        )
    }

    @Test
    fun validatesWorkoutTrainingDaysAgainstStableIds() {
        assertFalse(
            useCase(
                OnboardingStepIds.WorkoutTrainingDays,
                OnboardingAnswer.Selections(listOf("holiday")),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.WorkoutTrainingDays,
                OnboardingAnswer.Selections(listOf("monday", "friday")),
            ),
        )
    }

    @Test
    fun validatesWorkoutIntroChoiceAsRequiredToggle() {
        assertFalse(useCase(OnboardingStepIds.WorkoutIntroChoice, null))
        assertTrue(
            useCase(
                OnboardingStepIds.WorkoutIntroChoice,
                OnboardingAnswer.Toggle(false),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.WorkoutIntroChoice,
                OnboardingAnswer.Toggle(true),
            ),
        )
    }

    @Test
    fun reviewSummaryRequiresConfirmedToggle() {
        assertFalse(useCase(OnboardingStepIds.ReviewSummary, null))
        assertFalse(
            useCase(
                OnboardingStepIds.ReviewSummary,
                OnboardingAnswer.Toggle(false),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.ReviewSummary,
                OnboardingAnswer.Toggle(true),
            ),
        )
    }

    @Test
    fun validatesSourceChannelAgainstStableIds() {
        assertFalse(
            useCase(
                OnboardingStepIds.SourceChannel,
                OnboardingAnswer.Text("newspaper"),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.SourceChannel,
                OnboardingAnswer.Text("friend_referral"),
            ),
        )
    }

    @Test
    fun validatesSourceReasonAgainstStableIds() {
        assertFalse(
            useCase(
                OnboardingStepIds.SourceReason,
                OnboardingAnswer.Text("just_testing"),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.SourceReason,
                OnboardingAnswer.Text("complete_reset"),
            ),
        )
    }

    @Test
    fun validatesOptionalSourceReferralDetailShape() {
        assertTrue(useCase(OnboardingStepIds.SourceReferralDetail, null))
        assertFalse(
            useCase(
                OnboardingStepIds.SourceReferralDetail,
                OnboardingAnswer.Text("x"),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.SourceReferralDetail,
                OnboardingAnswer.Text("Coach Neha"),
            ),
        )
    }

    @Test
    fun validatesTargetsStepsTargetRange() {
        assertFalse(
            useCase(
                OnboardingStepIds.TargetsStepsTarget,
                OnboardingAnswer.Decimal(1500.0),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.TargetsStepsTarget,
                OnboardingAnswer.Decimal(8000.0),
            ),
        )
    }

    @Test
    fun validatesTargetsWaterTargetRange() {
        assertFalse(
            useCase(
                OnboardingStepIds.TargetsWaterTarget,
                OnboardingAnswer.Decimal(250.0),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.TargetsWaterTarget,
                OnboardingAnswer.Decimal(2500.0),
            ),
        )
    }

    @Test
    fun validatesTargetsRecommendationSummaryAsConfirmedToggle() {
        assertFalse(useCase(OnboardingStepIds.TargetsRecommendationSummary, null))
        assertFalse(
            useCase(
                OnboardingStepIds.TargetsRecommendationSummary,
                OnboardingAnswer.Toggle(false),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.TargetsRecommendationSummary,
                OnboardingAnswer.Toggle(true),
            ),
        )
    }

    @Test
    fun validatesTargetsSleepTargetAgainstStableIds() {
        assertFalse(
            useCase(
                OnboardingStepIds.TargetsSleepTarget,
                OnboardingAnswer.Text("night_owl"),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.TargetsSleepTarget,
                OnboardingAnswer.Text("balanced_evenings"),
            ),
        )
    }

    @Test
    fun validatesTargetsGoalPaceAgainstStableIds() {
        assertFalse(
            useCase(
                OnboardingStepIds.TargetsGoalPace,
                OnboardingAnswer.Text("fast"),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.TargetsGoalPace,
                OnboardingAnswer.Text("steady"),
            ),
        )
    }

    @Test
    fun validatesTargetsNutritionSummaryAgainstStableIds() {
        assertFalse(
            useCase(
                OnboardingStepIds.TargetsNutritionSummary,
                OnboardingAnswer.Text("calorie_crash"),
            ),
        )
        assertTrue(
            useCase(
                OnboardingStepIds.TargetsNutritionSummary,
                OnboardingAnswer.Text("protein_priority"),
            ),
        )
    }
}
