package com.tnyx.features.onboarding.presentation.sections.targets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.presentation.sections.targets.steps.GoalPaceStep
import com.tnyx.features.onboarding.presentation.sections.targets.steps.NutritionSummaryStep
import com.tnyx.features.onboarding.presentation.sections.targets.steps.TargetsRecommendationSummaryStep
import com.tnyx.features.onboarding.presentation.sections.targets.steps.SleepTargetStep
import com.tnyx.features.onboarding.presentation.sections.targets.steps.StepsTargetStep
import com.tnyx.features.onboarding.presentation.sections.targets.steps.WaterTargetStep

@Composable
internal fun TargetsSectionContent(
    stepId: OnboardingStepId,
    answer: OnboardingAnswer?,
    draftAnswers: Map<OnboardingStepId, OnboardingAnswer>,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (stepId) {
        OnboardingStepIds.TargetsStepsTarget -> {
            StepsTargetStep(
                answer = answer,
                showValidationError = showValidationError,
                onAnswerChanged = onAnswerChanged,
                modifier = modifier,
            )
        }

        OnboardingStepIds.TargetsSleepTarget -> {
            SleepTargetStep(
                answer = answer,
                showValidationError = showValidationError,
                onAnswerChanged = onAnswerChanged,
                modifier = modifier,
            )
        }

        OnboardingStepIds.TargetsWaterTarget -> {
            WaterTargetStep(
                answer = answer,
                showValidationError = showValidationError,
                onAnswerChanged = onAnswerChanged,
                modifier = modifier,
            )
        }

        OnboardingStepIds.TargetsRecommendationSummary -> {
            TargetsRecommendationSummaryStep(
                draftAnswers = draftAnswers,
                modifier = modifier,
            )
        }

        OnboardingStepIds.TargetsGoalPace -> {
            GoalPaceStep(
                answer = answer,
                showValidationError = showValidationError,
                onAnswerChanged = onAnswerChanged,
                modifier = modifier,
            )
        }

        OnboardingStepIds.TargetsNutritionSummary -> {
            NutritionSummaryStep(
                answer = answer,
                showValidationError = showValidationError,
                onAnswerChanged = onAnswerChanged,
                modifier = modifier,
            )
        }
    }
}
