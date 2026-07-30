package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import kotlin.math.roundToInt
import javax.inject.Inject

class SeedOnboardingRecommendationsUseCase @Inject constructor() {
    operator fun invoke(checkpoint: OnboardingCheckpoint): OnboardingCheckpoint {
        val stepId = checkpoint.progress.position.stepId
        if (checkpoint.draft.answerFor(stepId) != null) return checkpoint

        val recommendation = when (stepId) {
            OnboardingStepIds.TargetsStepsTarget -> checkpoint.recommendedStepsTarget()
            OnboardingStepIds.TargetsSleepTarget -> checkpoint.recommendedSleepTarget()
            OnboardingStepIds.TargetsWaterTarget -> checkpoint.recommendedWaterTarget()
            OnboardingStepIds.TargetsRecommendationSummary -> OnboardingAnswer.Toggle(true)
            else -> null
        } ?: return checkpoint

        return checkpoint.copy(
            draft = checkpoint.draft.withAnswer(stepId, recommendation),
        )
    }

    private fun OnboardingCheckpoint.recommendedStepsTarget(): OnboardingAnswer.Decimal {
        val activityLevel = (draft.answerFor(OnboardingStepIds.BodyGoalActivityLevel) as? OnboardingAnswer.Text)?.value
        val primaryGoal = (draft.answerFor(OnboardingStepIds.BodyGoalPrimaryGoal) as? OnboardingAnswer.Text)?.value

        val baseTarget = when (activityLevel) {
            "sedentary" -> 6000
            "light" -> 8000
            "active" -> 10000
            "very_active" -> 12000
            "dynamic" -> 13000
            else -> 8000
        }
        val adjustedTarget = when (primaryGoal) {
            "lose_weight" -> baseTarget + 1000
            "manage_stress" -> baseTarget - 1000
            else -> baseTarget
        }.coerceIn(4000, 16000)

        return OnboardingAnswer.Decimal(adjustedTarget.toDouble())
    }

    private fun OnboardingCheckpoint.recommendedSleepTarget(): OnboardingAnswer.Text {
        val activityLevel = (draft.answerFor(OnboardingStepIds.BodyGoalActivityLevel) as? OnboardingAnswer.Text)?.value
        val primaryGoal = (draft.answerFor(OnboardingStepIds.BodyGoalPrimaryGoal) as? OnboardingAnswer.Text)?.value

        val recommendation = when {
            primaryGoal == "manage_stress" -> "recover_early"
            activityLevel == "very_active" -> "recover_early"
            activityLevel == "dynamic" -> "flexible_late_schedule"
            else -> "balanced_evenings"
        }

        return OnboardingAnswer.Text(recommendation)
    }

    private fun OnboardingCheckpoint.recommendedWaterTarget(): OnboardingAnswer.Decimal {
        val currentWeight = (draft.answerFor(OnboardingStepIds.BodyGoalCurrentWeight) as? OnboardingAnswer.Decimal)?.value
        val activityLevel = (draft.answerFor(OnboardingStepIds.BodyGoalActivityLevel) as? OnboardingAnswer.Text)?.value

        val recommendedMl = if (currentWeight != null) {
            (currentWeight * 35.0).roundToNearest(250)
        } else {
            when (activityLevel) {
                "sedentary" -> 2200
                "light" -> 2500
                "active" -> 3000
                "very_active" -> 3500
                "dynamic" -> 3750
                else -> 2500
            }
        }.coerceIn(1500, 5000)

        return OnboardingAnswer.Decimal(recommendedMl.toDouble())
    }

    private fun Double.roundToNearest(step: Int): Int {
        return (this / step).roundToInt() * step
    }
}
