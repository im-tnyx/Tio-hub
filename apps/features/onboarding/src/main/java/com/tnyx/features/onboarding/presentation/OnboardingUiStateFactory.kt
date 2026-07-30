package com.tnyx.features.onboarding.presentation

import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingSectionDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.domain.usecase.ValidateOnboardingAnswerUseCase
import javax.inject.Inject

class OnboardingUiStateFactory @Inject constructor(
    private val validateOnboardingAnswer: ValidateOnboardingAnswerUseCase,
) {
    constructor() : this(
        validateOnboardingAnswer = ValidateOnboardingAnswerUseCase(),
    )

    operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        flow: OnboardingFlowDefinition,
        isSaving: Boolean = false,
        hasPersistenceError: Boolean = false,
    ): OnboardingUiState {
        val position = checkpoint.progress.position
        val effectiveSections = flow
            .effectiveSections(checkpoint.draft.answers)
            .takeIf { sections -> sections.containsPosition(position) }
            ?: flow.sections
        val sectionIndex = effectiveSections.indexOfFirst { section -> section.id == position.sectionId }
        val section = effectiveSections[sectionIndex]
        val step = section.steps.first { definition -> definition.id == position.stepId }
        val stepIndex = effectiveSections
            .take(sectionIndex)
            .sumOf { definition -> definition.steps.size } +
            section.steps.indexOf(step)
        val totalSteps = effectiveSections.sumOf { definition -> definition.steps.size }
        val currentAnswer = checkpoint.draft.answerFor(position.stepId)
        val hasRequiredAnswer = !step.isRequired ||
            validateOnboardingAnswer(position.stepId, currentAnswer)

        return OnboardingUiState(
            isLoading = false,
            isSaving = isSaving,
            position = position,
            currentAnswer = currentAnswer,
            draftAnswers = checkpoint.draft.answers,
            completedFraction = (stepIndex + 1).toFloat() / totalSteps.toFloat(),
            sectionNumber = sectionIndex + 1,
            sectionCount = effectiveSections.size,
            stepNumber = stepIndex + 1,
            totalSteps = totalSteps,
            canContinue = !checkpoint.progress.isCompleted && hasRequiredAnswer,
            canSkipSection = !checkpoint.progress.isCompleted && section.isSkippable,
            isLastStep = flow.next(position) == null,
            validationError = null,
            hasPersistenceError = hasPersistenceError,
        )
    }

    private fun OnboardingFlowDefinition.effectiveSections(
        draftAnswers: Map<OnboardingStepId, OnboardingAnswer>,
    ): List<OnboardingSectionDefinition> {
        val wantsToSkipWorkout = (draftAnswers[OnboardingStepIds.WorkoutIntroChoice] as? OnboardingAnswer.Toggle)
            ?.value == false
        val gymOnlyAccess = (draftAnswers[OnboardingStepIds.WorkoutGymAccess] as? OnboardingAnswer.Text)
            ?.value == "gym"

        val visibleSections = if (wantsToSkipWorkout) {
            sections.filterNot { section -> section.id == OnboardingSectionIds.Workout }
        } else {
            sections
        }

        if (!gymOnlyAccess) return visibleSections

        return visibleSections.map { section ->
            if (section.id != OnboardingSectionIds.Workout) {
                section
            } else {
                section.copy(
                    steps = section.steps.filterNot { step -> step.id == OnboardingStepIds.WorkoutEquipment },
                )
            }
        }
    }

    private fun List<OnboardingSectionDefinition>.containsPosition(
        position: OnboardingPosition,
    ): Boolean {
        return any { section ->
            section.id == position.sectionId &&
                section.steps.any { step -> step.id == position.stepId }
        }
    }
}
