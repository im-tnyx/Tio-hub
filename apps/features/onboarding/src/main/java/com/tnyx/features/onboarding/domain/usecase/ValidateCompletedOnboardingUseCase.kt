package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.effectiveSections
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import javax.inject.Inject

class ValidateCompletedOnboardingUseCase @Inject constructor(
    private val validateOnboardingAnswer: ValidateOnboardingAnswerUseCase,
) {
    constructor() : this(
        validateOnboardingAnswer = ValidateOnboardingAnswerUseCase(),
    )

    operator fun invoke(
        draft: OnboardingDraft,
        flow: OnboardingFlowDefinition,
    ): Boolean {
        return invoke(defaultCheckpointFor(draft, flow), flow)
    }

    operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        flow: OnboardingFlowDefinition,
    ): Boolean {
        return requiredStepIds(flow, checkpoint).all { stepId ->
            validateOnboardingAnswer(stepId, checkpoint.draft.answerFor(stepId))
        }
    }

    fun missingRequiredStepIds(
        draft: OnboardingDraft,
        flow: OnboardingFlowDefinition,
    ): List<OnboardingStepId> {
        return missingRequiredStepIds(defaultCheckpointFor(draft, flow), flow)
    }

    fun missingRequiredStepIds(
        checkpoint: OnboardingCheckpoint,
        flow: OnboardingFlowDefinition,
    ): List<OnboardingStepId> {
        return requiredStepIds(flow, checkpoint).filterNot { stepId ->
            validateOnboardingAnswer(stepId, checkpoint.draft.answerFor(stepId))
        }
    }

    private fun requiredStepIds(
        flow: OnboardingFlowDefinition,
        checkpoint: OnboardingCheckpoint,
    ): List<OnboardingStepId> {
        return flow.effectiveSections(checkpoint)
            .flatMap { section -> section.steps }
            .filter { step -> step.isRequired }
            .map { step -> step.id }
    }

    private fun defaultCheckpointFor(
        draft: OnboardingDraft,
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        val firstSection = flow.sections.first()
        val firstStep = firstSection.steps.first()
        return OnboardingCheckpoint(
            draft = draft,
            progress = OnboardingProgress(
                flowVersion = flow.version,
                position = OnboardingPosition(
                    sectionId = firstSection.id,
                    stepId = firstStep.id,
                ),
            ),
        )
    }
}
