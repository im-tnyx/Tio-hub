package com.tnyx.features.onboarding.presentation.sections.bodygoal.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.inputs.TnyxTextField
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading
import java.math.BigDecimal

@Composable
internal fun BodyGoalDecimalInputStep(
    title: String,
    description: String,
    label: String,
    placeholder: String,
    helperMessage: String,
    unit: String,
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    validationMessage: String,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialValue = (answer as? OnboardingAnswer.Decimal)?.value?.toInputText().orEmpty()
    var textValue by rememberSaveable(initialValue) { mutableStateOf(initialValue) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        OnboardingStepHeading(
            title = title,
            description = description,
        )
        TnyxTextField(
            value = textValue,
            onValueChange = { updatedValue ->
                if (updatedValue.length <= DECIMAL_INPUT_MAX_LENGTH && DECIMAL_INPUT_PATTERN.matches(updatedValue)) {
                    textValue = updatedValue
                    onAnswerChanged(updatedValue.toDecimalAnswer())
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = {
                Text(
                    text = unit,
                    style = TnyxTheme.typography.labelLarge,
                    color = TnyxTheme.colors.textSecondary,
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = showValidationError,
            errorMessage = validationMessage,
            helperMessage = helperMessage,
        )
    }
}

private fun String.toDecimalAnswer(): OnboardingAnswer.Decimal? {
    return takeIf(String::isNotBlank)
        ?.toDoubleOrNull()
        ?.takeIf(Double::isFinite)
        ?.let(OnboardingAnswer::Decimal)
}

private fun Double.toInputText(): String {
    return BigDecimal.valueOf(this)
        .stripTrailingZeros()
        .toPlainString()
}

private val DECIMAL_INPUT_PATTERN = Regex("^\\d*(\\.\\d{0,1})?$")
private const val DECIMAL_INPUT_MAX_LENGTH = 6
