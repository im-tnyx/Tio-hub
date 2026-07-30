package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import javax.inject.Inject

class SkipOnboardingSectionUseCase @Inject constructor() {
    operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint? {
        val currentPosition = checkpoint.progress.position
        val currentSection = flow.sections.firstOrNull { section ->
            section.id == currentPosition.sectionId
        } ?: return null
        if (!currentSection.isSkippable) return null

        var nextPosition: OnboardingPosition? = flow.next(currentPosition)
        while (nextPosition?.sectionId == currentPosition.sectionId) {
            nextPosition = flow.next(nextPosition)
        }
        val targetPosition = nextPosition ?: return null

        return checkpoint.copy(
            progress = checkpoint.progress.copy(
                position = targetPosition,
                isCompleted = false,
            ),
        )
    }
}
