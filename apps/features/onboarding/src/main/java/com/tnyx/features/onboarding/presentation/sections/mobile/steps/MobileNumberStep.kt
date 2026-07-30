package com.tnyx.features.onboarding.presentation.sections.mobile.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.inputs.TnyxPhoneField
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading

@Composable
internal fun MobileNumberStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialValue = (answer as? OnboardingAnswer.Text)?.value.orEmpty()
    var mobileNumber by rememberSaveable { mutableStateOf(initialValue) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        OnboardingStepHeading(
            title = "What is your mobile number?",
            description = "This keeps your profile ready for future verification, recovery, and account support flows.",
        )
        TnyxPhoneField(
            value = mobileNumber,
            onValueChange = { updatedValue ->
                val sanitizedValue = updatedValue.filter { character ->
                    character.isDigit() || character == '+' || character == ' ' || character == '-'
                }
                mobileNumber = sanitizedValue
                onAnswerChanged(
                    sanitizedValue
                        .takeIf(String::isNotBlank)
                        ?.let { value -> OnboardingAnswer.Text(value.trim()) },
                )
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = "+91 9876543210",
            helperMessage = "Use country code if needed. Verification can be added later.",
            isError = showValidationError,
            errorMessage = "Enter a valid mobile number",
        )
    }
}
