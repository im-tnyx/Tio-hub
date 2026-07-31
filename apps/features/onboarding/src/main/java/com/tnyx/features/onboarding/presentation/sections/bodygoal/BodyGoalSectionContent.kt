package com.tnyx.features.onboarding.presentation.sections.bodygoal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.presentation.sections.bodygoal.steps.ActivityLevelStep
import com.tnyx.features.onboarding.presentation.sections.bodygoal.steps.CurrentWeightStep
import com.tnyx.features.onboarding.presentation.sections.bodygoal.steps.HealthConditionStep
import com.tnyx.features.onboarding.presentation.sections.bodygoal.steps.HeightStep
import com.tnyx.features.onboarding.presentation.sections.bodygoal.steps.PrimaryGoalStep
import com.tnyx.features.onboarding.presentation.sections.bodygoal.steps.TargetWeightStep

@Composable
internal fun BodyGoalSectionContent(
    stepId: OnboardingStepId,
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (stepId) {
        OnboardingStepIds.BodyGoalPrimaryGoal -> PrimaryGoalStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.BodyGoalHeight -> HeightStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.BodyGoalCurrentWeight -> CurrentWeightStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.BodyGoalTargetWeight -> TargetWeightStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.BodyGoalActivityLevel -> ActivityLevelStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.BodyGoalHealthCondition -> HealthConditionStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        else -> Unit
    }
}
