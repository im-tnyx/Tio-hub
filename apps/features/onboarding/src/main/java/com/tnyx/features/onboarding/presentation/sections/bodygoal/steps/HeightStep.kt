package com.tnyx.features.onboarding.presentation.sections.bodygoal.steps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer

@Composable
internal fun HeightStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    BodyGoalDecimalInputStep(
        title = "How tall are you?",
        description = "We use height together with weight to understand your body targets.",
        label = "Height",
        placeholder = "170",
        helperMessage = "Enter height in centimeters for now",
        unit = "cm",
        answer = answer,
        showValidationError = showValidationError,
        validationMessage = "Enter a valid height in cm",
        onAnswerChanged = onAnswerChanged,
        modifier = modifier,
    )
}
