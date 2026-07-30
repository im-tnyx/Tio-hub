package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
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
        val nextPosition = resolveNextPosition(checkpoint, flow, currentPosition)

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

    private fun resolveNextPosition(
        checkpoint: OnboardingCheckpoint,
        flow: OnboardingFlowDefinition,
        currentPosition: OnboardingPosition,
    ): OnboardingPosition? {
        if (currentPosition.stepId == OnboardingStepIds.WorkoutFocusAreas && hasGymOnlyAccess(checkpoint)) {
            return flow.sections
                .firstOrNull { section -> section.id == OnboardingSectionIds.Workout }
                ?.steps
                ?.firstOrNull { step -> step.id == OnboardingStepIds.WorkoutTrainingDays }
                ?.let { step -> OnboardingPosition(OnboardingSectionIds.Workout, step.id) }
        }

        if (currentPosition.stepId != OnboardingStepIds.WorkoutIntroChoice) {
            return flow.next(currentPosition)
        }

        val wantsWorkoutPlan = (checkpoint.draft.answerFor(OnboardingStepIds.WorkoutIntroChoice) as? OnboardingAnswer.Toggle)
            ?.value
            ?: return flow.next(currentPosition)

        return if (wantsWorkoutPlan) {
            flow.next(currentPosition)
        } else {
            flow.sections
                .firstOrNull { section -> section.id == OnboardingSectionIds.Targets }
                ?.steps
                ?.firstOrNull()
                ?.let { step -> OnboardingPosition(OnboardingSectionIds.Targets, step.id) }
        }
    }

    private fun hasGymOnlyAccess(checkpoint: OnboardingCheckpoint): Boolean {
        return (checkpoint.draft.answerFor(OnboardingStepIds.WorkoutGymAccess) as? OnboardingAnswer.Text)
            ?.value == "gym"
    }
}
