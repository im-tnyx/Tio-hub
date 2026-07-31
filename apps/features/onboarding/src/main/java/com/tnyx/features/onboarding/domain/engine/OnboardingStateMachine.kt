package com.tnyx.features.onboarding.domain.engine

import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.flow.firstPosition
import com.tnyx.features.onboarding.domain.flow.next
import com.tnyx.features.onboarding.domain.flow.position
import com.tnyx.features.onboarding.domain.flow.previous
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.usecase.BuildFlowUseCase

class OnboardingStateMachine(
    private val flow: OnboardingFlowDefinition,
    private val buildFlowUseCase: BuildFlowUseCase = BuildFlowUseCase(),
) {
    fun nextPosition(checkpoint: OnboardingCheckpoint): OnboardingPosition? {
        val effectiveSections = buildFlowUseCase(flow, checkpoint)
        val currentPosition = checkpoint.progress.position

        if (currentPosition.stepId == OnboardingStepIds.WorkoutFocusAreas && hasGymOnlyAccess(checkpoint)) {
            return effectiveSections.position(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutTrainingDays,
            )
        }

        if (currentPosition.stepId != OnboardingStepIds.WorkoutIntroChoice) {
            return effectiveSections.next(currentPosition)
        }

        val wantsWorkoutPlan = (checkpoint.draft.answerFor(OnboardingStepIds.WorkoutIntroChoice) as? OnboardingAnswer.Toggle)
            ?.value ?: return effectiveSections.next(currentPosition)

        return if (wantsWorkoutPlan) {
            effectiveSections.next(currentPosition)
        } else {
            effectiveSections.firstPosition(OnboardingSectionIds.Targets)
        }
    }

    fun previousPosition(checkpoint: OnboardingCheckpoint): OnboardingPosition? {
        val effectiveSections = buildFlowUseCase(flow, checkpoint)
        val currentPosition = checkpoint.progress.position

        return when {
            currentPosition.sectionId == OnboardingSectionIds.Targets &&
                wantsToSkipWorkout(checkpoint) -> {
                effectiveSections.firstPosition(OnboardingSectionIds.WorkoutIntro)
            }

            currentPosition.stepId == OnboardingStepIds.WorkoutTrainingDays &&
                hasGymOnlyAccess(checkpoint) -> {
                effectiveSections.position(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutFocusAreas,
                )
            }

            else -> effectiveSections.previous(currentPosition)
        }
    }

    fun nextSectionEntryPosition(checkpoint: OnboardingCheckpoint): OnboardingPosition? {
        val effectiveSections = buildFlowUseCase(flow, checkpoint)
        val currentSectionId = checkpoint.progress.position.sectionId
        val currentSectionIndex = effectiveSections.indexOfFirst { section -> section.id == currentSectionId }
        if (currentSectionIndex < 0) return null

        return effectiveSections
            .getOrNull(currentSectionIndex + 1)
            ?.steps
            ?.firstOrNull()
            ?.let { step ->
                OnboardingPosition(
                    sectionId = effectiveSections[currentSectionIndex + 1].id,
                    stepId = step.id,
                )
            }
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
