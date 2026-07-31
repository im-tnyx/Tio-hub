package com.tnyx.features.onboarding.presentation.sections.profile.steps

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
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.inputs.TnyxTextField
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading

@Composable
internal fun NameStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialName = (answer as? OnboardingAnswer.Text)?.value.orEmpty()
    var name by rememberSaveable { mutableStateOf(initialName) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        OnboardingStepHeading(
            title = "What should we call you?",
            description = "Use the name you want to see across your Tio experience.",
        )
        TnyxTextField(
            value = name,
            onValueChange = { updatedName ->
                if (updatedName.length <= PROFILE_NAME_MAX_LENGTH) {
                    name = updatedName
                    onAnswerChanged(
                        updatedName
                            .takeIf(String::isNotBlank)
                            ?.let { value -> OnboardingAnswer.Text(value) },
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            placeholder = { Text("Your name") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
            ),
            isError = showValidationError,
            errorMessage = "Enter between 2 and 30 characters",
            helperMessage = "You can update this later in Profile",
        )
    }
}

private const val PROFILE_NAME_MAX_LENGTH = 30
