package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class SeedOnboardingRecommendationsUseCaseTest {
    private val useCase = SeedOnboardingRecommendationsUseCase()

    @Test
    fun seedsTargetsRecommendationsFromEarlierAnswers() {
        val baseDraft = OnboardingDraft()
            .withAnswer(OnboardingStepIds.BodyGoalPrimaryGoal, OnboardingAnswer.Text("lose_weight"))
            .withAnswer(OnboardingStepIds.BodyGoalActivityLevel, OnboardingAnswer.Text("active"))
            .withAnswer(OnboardingStepIds.BodyGoalCurrentWeight, OnboardingAnswer.Decimal(80.0))

        val stepsCheckpoint = checkpoint(
            stepId = OnboardingStepIds.TargetsStepsTarget,
            draft = baseDraft,
        )
        val waterCheckpoint = checkpoint(
            stepId = OnboardingStepIds.TargetsWaterTarget,
            draft = baseDraft,
        )
        val sleepCheckpoint = checkpoint(
            stepId = OnboardingStepIds.TargetsSleepTarget,
            draft = baseDraft,
        )

        assertEquals(
            OnboardingAnswer.Decimal(11000.0),
            useCase(stepsCheckpoint).draft.answerFor(OnboardingStepIds.TargetsStepsTarget),
        )
        assertEquals(
            OnboardingAnswer.Decimal(2750.0),
            useCase(waterCheckpoint).draft.answerFor(OnboardingStepIds.TargetsWaterTarget),
        )
        assertEquals(
            OnboardingAnswer.Text("balanced_evenings"),
            useCase(sleepCheckpoint).draft.answerFor(OnboardingStepIds.TargetsSleepTarget),
        )
    }

    @Test
    fun keepsExistingManualAnswerUntouched() {
        val checkpoint = checkpoint(
            stepId = OnboardingStepIds.TargetsWaterTarget,
            draft = OnboardingDraft().withAnswer(
                OnboardingStepIds.TargetsWaterTarget,
                OnboardingAnswer.Decimal(3200.0),
            ),
        )

        val result = useCase(checkpoint)

        assertEquals(
            OnboardingAnswer.Decimal(3200.0),
            result.draft.answerFor(OnboardingStepIds.TargetsWaterTarget),
        )
    }

    private fun checkpoint(
        stepId: com.tnyx.features.onboarding.domain.model.OnboardingStepId,
        draft: OnboardingDraft,
    ): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = draft,
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = OnboardingPosition(
                    sectionId = OnboardingSectionIds.Targets,
                    stepId = stepId,
                ),
            ),
        )
    }
}
