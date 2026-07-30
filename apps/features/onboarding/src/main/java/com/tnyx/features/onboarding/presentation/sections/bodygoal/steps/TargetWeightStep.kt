package com.tnyx.features.onboarding.presentation.sections.bodygoal.steps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer

@Composable
internal fun TargetWeightStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    BodyGoalDecimalInputStep(
        title = "What target weight are you aiming for?",
        description = "This gives Tio an initial destination. You can fine-tune it later.",
        label = "Target weight",
        placeholder = "68",
        helperMessage = "Keep it realistic. You can adjust this later.",
        unit = "kg",
        answer = answer,
        showValidationError = showValidationError,
        validationMessage = "Enter a valid target weight in kg",
        onAnswerChanged = onAnswerChanged,
        modifier = modifier,
    )
}
