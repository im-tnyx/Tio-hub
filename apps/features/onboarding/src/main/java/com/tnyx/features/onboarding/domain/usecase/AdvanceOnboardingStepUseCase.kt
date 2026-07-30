package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.engine.OnboardingStateMachine
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import javax.inject.Inject

sealed interface AdvanceOnboardingStepResult {
    data class Next(
        val checkpoint: OnboardingCheckpoint,
    ) : AdvanceOnboardingStepResult

    data class Completed(
        val checkpoint: OnboardingCheckpoint,
    ) : AdvanceOnboardingStepResult
}

class AdvanceOnboardingStepUseCase @Inject constructor() {
    operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        flow: OnboardingFlowDefinition,
    ): AdvanceOnboardingStepResult {
        val currentPosition = checkpoint.progress.position
        val nextPosition = OnboardingStateMachine(flow).nextPosition(checkpoint)

        if (nextPosition == null) {
            return AdvanceOnboardingStepResult.Completed(
                checkpoint = checkpoint.copy(
                    progress = checkpoint.progress.copy(
                        completedSectionIds = checkpoint.progress.completedSectionIds +
                            currentPosition.sectionId,
                        isCompleted = true,
                    ),
                ),
            )
        }

        val completedSections = if (nextPosition.sectionId != currentPosition.sectionId) {
            checkpoint.progress.completedSectionIds + currentPosition.sectionId
        } else {
            checkpoint.progress.completedSectionIds
        }

        return AdvanceOnboardingStepResult.Next(
            checkpoint = checkpoint.copy(
                progress = checkpoint.progress.copy(
                    position = nextPosition,
                    completedSectionIds = completedSections,
                ),
            ),
        )
    }
}
