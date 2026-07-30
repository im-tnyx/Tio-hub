package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import javax.inject.Inject

sealed interface ResolveBackNavigationResult {
    data object Exit : ResolveBackNavigationResult

    data class Previous(
        val checkpoint: OnboardingCheckpoint,
    ) : ResolveBackNavigationResult
}

class ResolveBackNavigationUseCase @Inject constructor() {
    operator fun invoke(
        checkpoint: OnboardingCheckpoint?,
        flow: OnboardingFlowDefinition,
    ): ResolveBackNavigationResult {
        val currentCheckpoint = checkpoint ?: return ResolveBackNavigationResult.Exit
        val previousPosition = resolvePreviousPosition(currentCheckpoint, flow)
            ?: return ResolveBackNavigationResult.Exit

        return ResolveBackNavigationResult.Previous(
            checkpoint = currentCheckpoint.copy(
                progress = currentCheckpoint.progress.copy(
                    position = previousPosition,
                    isCompleted = false,
                ),
            ),
        )
    }

    private fun resolvePreviousPosition(
        checkpoint: OnboardingCheckpoint,
        flow: OnboardingFlowDefinition,
    ) = when {
        checkpoint.progress.position.sectionId == OnboardingSectionIds.Targets &&
            wantsToSkipWorkout(checkpoint) -> {
            flow.sections
                .firstOrNull { section -> section.id == OnboardingSectionIds.WorkoutIntro }
                ?.steps
                ?.firstOrNull()
                ?.let { step -> com.tnyx.features.onboarding.domain.model.OnboardingPosition(OnboardingSectionIds.WorkoutIntro, step.id) }
        }

        checkpoint.progress.position.stepId == OnboardingStepIds.WorkoutTrainingDays &&
            hasGymOnlyAccess(checkpoint) -> {
            flow.sections
                .firstOrNull { section -> section.id == OnboardingSectionIds.Workout }
                ?.steps
                ?.firstOrNull { step -> step.id == OnboardingStepIds.WorkoutFocusAreas }
                ?.let { step -> com.tnyx.features.onboarding.domain.model.OnboardingPosition(OnboardingSectionIds.Workout, step.id) }
        }

        else -> flow.previous(checkpoint.progress.position)
    }

    private fun wantsToSkipWorkout(checkpoint: OnboardingCheckpoint): Boolean {
        return (checkpoint.draft.answerFor(OnboardingStepIds.WorkoutIntroChoice) as? OnboardingAnswer.Toggle)
            ?.value == false
    }

    private fun hasGymOnlyAccess(checkpoint: OnboardingCheckpoint): Boolean {
        return (checkpoint.draft.answerFor(OnboardingStepIds.WorkoutGymAccess) as? OnboardingAnswer.Text)
            ?.value == "gym"
    }
}
