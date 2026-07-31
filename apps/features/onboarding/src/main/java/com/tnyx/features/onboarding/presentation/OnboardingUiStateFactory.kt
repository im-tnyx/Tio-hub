package com.tnyx.features.onboarding.presentation

import com.tnyx.features.onboarding.domain.flow.containsPosition
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.usecase.BuildFlowUseCase
import com.tnyx.features.onboarding.domain.usecase.ValidateOnboardingAnswerUseCase
import com.tnyx.features.onboarding.domain.validator.StepValidator
import javax.inject.Inject

class OnboardingUiStateFactory @Inject constructor(
    private val buildFlowUseCase: BuildFlowUseCase,
    private val stepValidator: StepValidator,
) {
    constructor() : this(
        buildFlowUseCase = BuildFlowUseCase(),
        stepValidator = StepValidator(ValidateOnboardingAnswerUseCase()),
    )

    operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        flow: OnboardingFlowDefinition,
        progressSourceCheckpoint: OnboardingCheckpoint = checkpoint,
        isSaving: Boolean = false,
        hasPersistenceError: Boolean = false,
    ): OnboardingUiState {
        val position = checkpoint.progress.position
        val effectiveSections = buildFlowUseCase(flow, checkpoint)
            .takeIf { sections -> sections.containsPosition(position) }
            ?: flow.sections
        val progressPosition = progressSourceCheckpoint.progress.position
        val effectiveProgressSections = buildFlowUseCase(flow, progressSourceCheckpoint)
            .takeIf { sections -> sections.containsPosition(progressPosition) }
            ?: flow.sections
        val sectionIndex = effectiveSections.indexOfFirst { section -> section.id == position.sectionId }
        val section = effectiveSections[sectionIndex]
        val step = section.steps.first { definition -> definition.id == position.stepId }
        val stepIndex = effectiveSections
            .take(sectionIndex)
            .sumOf { definition -> definition.steps.size } +
            section.steps.indexOf(step)
        val totalSteps = effectiveSections.sumOf { definition -> definition.steps.size }
        val progressSectionIndex = effectiveProgressSections.indexOfFirst { section ->
            section.id == progressPosition.sectionId
        }
        val progressSection = effectiveProgressSections[progressSectionIndex]
        val progressStep = progressSection.steps.first { definition ->
            definition.id == progressPosition.stepId
        }
        val progressStepIndex = effectiveProgressSections
            .take(progressSectionIndex)
            .sumOf { definition -> definition.steps.size } +
            progressSection.steps.indexOf(progressStep)
        val progressTotalSteps = effectiveProgressSections.sumOf { definition -> definition.steps.size }
        val currentAnswer = checkpoint.draft.answerFor(position.stepId)
        val hasRequiredAnswer = stepValidator.isStepValid(step, currentAnswer)

        return OnboardingUiState(
            isLoading = false,
            isSaving = isSaving,
            position = position,
            currentAnswer = currentAnswer,
            draftAnswers = checkpoint.draft.answers,
            completedFraction = (progressStepIndex + 1).toFloat() / progressTotalSteps.toFloat(),
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
}
