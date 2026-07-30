package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import javax.inject.Inject

class UpdateOnboardingAnswerUseCase @Inject constructor() {
    operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        answer: OnboardingAnswer?,
    ): OnboardingCheckpoint {
        val stepId = checkpoint.progress.position.stepId
        val updatedDraft = if (answer == null) {
            checkpoint.draft.withoutAnswer(stepId)
        } else {
            checkpoint.draft.withAnswer(stepId, answer)
        }
        return checkpoint.copy(draft = updatedDraft)
    }
}
