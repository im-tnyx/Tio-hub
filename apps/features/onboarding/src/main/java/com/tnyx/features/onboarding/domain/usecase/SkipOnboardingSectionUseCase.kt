package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.engine.OnboardingStateMachine
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
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

        val targetPosition = OnboardingStateMachine(flow).nextSectionEntryPosition(checkpoint)
            ?: return null

        return checkpoint.copy(
            progress = checkpoint.progress.copy(
                position = targetPosition,
                isCompleted = false,
            ),
        )
    }
}
