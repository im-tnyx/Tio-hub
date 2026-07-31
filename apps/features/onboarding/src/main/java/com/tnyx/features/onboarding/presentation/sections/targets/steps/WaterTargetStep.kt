package com.tnyx.features.onboarding.presentation.sections.targets.steps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.sections.bodygoal.steps.BodyGoalDecimalInputStep

@Composable
internal fun WaterTargetStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    BodyGoalDecimalInputStep(
        title = "How much water should Tio remind you about?",
        description = "A simple hydration target gives Progress and Nutrition a cleaner daily baseline later.",
        label = "Water target",
        placeholder = "2500",
        helperMessage = "Use your ideal daily intake in milliliters",
        unit = "ml",
        answer = answer,
        showValidationError = showValidationError,
        validationMessage = "Enter a whole-number target between 500 and 6000 ml",
        onAnswerChanged = onAnswerChanged,
        modifier = modifier,
    )
}
