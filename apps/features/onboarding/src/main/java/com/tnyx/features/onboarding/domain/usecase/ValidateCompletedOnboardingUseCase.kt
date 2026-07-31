package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
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
        return requiredStepIds(flow, draft.answers).all { stepId ->
            validateOnboardingAnswer(stepId, draft.answerFor(stepId))
        }
    }

    fun missingRequiredStepIds(
        draft: OnboardingDraft,
        flow: OnboardingFlowDefinition,
    ): List<OnboardingStepId> {
        return requiredStepIds(flow, draft.answers).filterNot { stepId ->
            validateOnboardingAnswer(stepId, draft.answerFor(stepId))
        }
    }

    private fun requiredStepIds(
        flow: OnboardingFlowDefinition,
        draftAnswers: Map<OnboardingStepId, OnboardingAnswer>,
    ): List<OnboardingStepId> {
        val wantsToSkipWorkout = (draftAnswers[OnboardingStepIds.WorkoutIntroChoice] as? OnboardingAnswer.Toggle)
            ?.value == false
        val gymOnlyAccess = (draftAnswers[OnboardingStepIds.WorkoutGymAccess] as? OnboardingAnswer.Text)
            ?.value == "gym"

        return flow.sections
            .filterNot { section ->
                wantsToSkipWorkout && section.id == OnboardingSectionIds.Workout
            }
            .flatMap { section ->
                if (!gymOnlyAccess || section.id != OnboardingSectionIds.Workout) {
                    section.steps
                } else {
                    section.steps.filterNot { step -> step.id == OnboardingStepIds.WorkoutEquipment }
                }
            }
            .filter { step -> step.isRequired }
            .map { step -> step.id }
    }
}
