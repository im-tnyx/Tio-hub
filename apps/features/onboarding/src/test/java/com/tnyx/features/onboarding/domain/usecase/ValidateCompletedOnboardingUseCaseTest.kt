package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateCompletedOnboardingUseCaseTest {
    private val useCase = ValidateCompletedOnboardingUseCase()

    @Test
    fun acceptsCompleteDraftWhenWorkoutWasSkipped() {
        val draft = OnboardingDraft()
            .withAnswer(OnboardingStepIds.IntroWelcome, OnboardingAnswer.Toggle(true))
            .withAnswer(OnboardingStepIds.IntroExperienceMode, OnboardingAnswer.Text("balanced"))
            .withAnswer(OnboardingStepIds.ProfileName, OnboardingAnswer.Text("Santosh"))
            .withAnswer(OnboardingStepIds.ProfileGender, OnboardingAnswer.Text("male"))
            .withAnswer(OnboardingStepIds.ProfileDateOfBirth, OnboardingAnswer.Text("1990-01-01"))
            .withAnswer(OnboardingStepIds.BodyGoalPrimaryGoal, OnboardingAnswer.Text("lose_weight"))
            .withAnswer(OnboardingStepIds.BodyGoalHeight, OnboardingAnswer.Decimal(176.0))
            .withAnswer(OnboardingStepIds.BodyGoalCurrentWeight, OnboardingAnswer.Decimal(80.0))
            .withAnswer(OnboardingStepIds.BodyGoalTargetWeight, OnboardingAnswer.Decimal(74.0))
            .withAnswer(OnboardingStepIds.BodyGoalActivityLevel, OnboardingAnswer.Text("active"))
            .withAnswer(OnboardingStepIds.BodyGoalHealthCondition, OnboardingAnswer.Selections(listOf("none")))
            .withAnswer(OnboardingStepIds.MobileNumber, OnboardingAnswer.Text("+91 9876543210"))
            .withAnswer(OnboardingStepIds.WorkoutIntroChoice, OnboardingAnswer.Toggle(false))
            .withAnswer(OnboardingStepIds.TargetsStepsTarget, OnboardingAnswer.Decimal(11000.0))
            .withAnswer(OnboardingStepIds.TargetsSleepTarget, OnboardingAnswer.Text("balanced_evenings"))
            .withAnswer(OnboardingStepIds.TargetsWaterTarget, OnboardingAnswer.Decimal(2750.0))
            .withAnswer(OnboardingStepIds.TargetsRecommendationSummary, OnboardingAnswer.Toggle(true))
            .withAnswer(OnboardingStepIds.TargetsGoalPace, OnboardingAnswer.Text("steady"))
            .withAnswer(OnboardingStepIds.TargetsNutritionSummary, OnboardingAnswer.Text("protein_priority"))
            .withAnswer(OnboardingStepIds.SourceChannel, OnboardingAnswer.Text("friend_referral"))
            .withAnswer(OnboardingStepIds.SourceReason, OnboardingAnswer.Text("complete_reset"))
            .withAnswer(OnboardingStepIds.ReviewSummary, OnboardingAnswer.Toggle(true))

        assertTrue(useCase(draft, DefaultOnboardingFlow.definition))
    }

    @Test
    fun rejectsDraftWhenRequiredTargetConfirmationIsMissing() {
        val draft = OnboardingDraft()
            .withAnswer(OnboardingStepIds.WorkoutIntroChoice, OnboardingAnswer.Toggle(false))
            .withAnswer(OnboardingStepIds.ReviewSummary, OnboardingAnswer.Toggle(true))

        assertFalse(useCase(draft, DefaultOnboardingFlow.definition))
        assertTrue(
            useCase.missingRequiredStepIds(draft, DefaultOnboardingFlow.definition)
                .contains(OnboardingStepIds.TargetsRecommendationSummary),
        )
    }
}
