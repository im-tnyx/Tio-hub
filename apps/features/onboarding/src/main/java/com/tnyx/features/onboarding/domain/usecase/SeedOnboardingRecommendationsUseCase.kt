package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import javax.inject.Inject

class SeedOnboardingRecommendationsUseCase @Inject constructor(
    private val autoCalculateOnboardingTargets: AutoCalculateOnboardingTargetsUseCase,
) {
    constructor() : this(
        autoCalculateOnboardingTargets = AutoCalculateOnboardingTargetsUseCase(),
    )

    operator fun invoke(checkpoint: OnboardingCheckpoint): OnboardingCheckpoint {
        val stepId = checkpoint.progress.position.stepId
        if (checkpoint.draft.answerFor(stepId) != null) return checkpoint

        val targetSnapshot = autoCalculateOnboardingTargets(checkpoint.draft)
        val recommendation = when (stepId) {
            OnboardingStepIds.TargetsStepsTarget -> OnboardingAnswer.Decimal(targetSnapshot.stepsTarget.toDouble())
            OnboardingStepIds.TargetsSleepTarget -> OnboardingAnswer.Text(targetSnapshot.sleepTargetId)
            OnboardingStepIds.TargetsWaterTarget -> OnboardingAnswer.Decimal(targetSnapshot.waterTargetMl.toDouble())
            OnboardingStepIds.TargetsRecommendationSummary -> OnboardingAnswer.Toggle(true)
            else -> null
        } ?: return checkpoint

        return checkpoint.copy(
            draft = checkpoint.draft.withAnswer(stepId, recommendation),
        )
    }
}
