package com.tnyx.features.onboarding.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.inputs.TnyxDatePickerDialog
import com.tnyx.core.ui.components.inputs.TnyxTextField
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun ProfileStepContent(
    stepId: OnboardingStepId,
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (stepId) {
        OnboardingStepIds.ProfileName -> ProfileNameStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.ProfileGender -> ProfileGenderStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.ProfileDateOfBirth -> ProfileDateOfBirthStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        else -> Unit
    }
}

@Composable
private fun ProfileNameStep(
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
        StepHeading(
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

@Composable
private fun ProfileGenderStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedId = (answer as? OnboardingAnswer.Text)?.value

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        StepHeading(
            title = "How do you identify?",
            description = "This helps Tio personalize guidance while keeping your profile respectful.",
        )
        GenderOption.entries.forEach { option ->
            val isSelected = selectedId == option.id
            TnyxCard(
                modifier = Modifier.fillMaxWidth(),
                variant = TnyxCardVariant.Outlined,
                onClick = {
                    onAnswerChanged(OnboardingAnswer.Text(option.id))
                },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = TnyxTheme.colors.primary,
                            unselectedColor = TnyxTheme.colors.textMuted,
                        ),
                    )
                    Text(
                        text = option.label,
                        style = TnyxTheme.typography.titleMedium,
                        color = TnyxTheme.colors.textPrimary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
        if (showValidationError) {
            ValidationMessage("Select one option to continue")
        }
    }
}

@Composable
private fun ProfileDateOfBirthStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedDate = (answer as? OnboardingAnswer.Text)
        ?.value
        ?.let { value -> runCatching { LocalDate.parse(value) }.getOrNull() }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val dateFormatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        StepHeading(
            title = "When were you born?",
            description = "Your date of birth helps tailor age-aware fitness and nutrition guidance.",
        )
        TnyxCard(
            modifier = Modifier.fillMaxWidth(),
            variant = TnyxCardVariant.Outlined,
            onClick = { showDatePicker = true },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = TnyxTheme.colors.primary,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXXS),
                ) {
                    Text(
                        text = "Date of birth",
                        style = TnyxTheme.typography.labelMedium,
                        color = TnyxTheme.colors.textSecondary,
                    )
                    Text(
                        text = selectedDate?.format(dateFormatter) ?: "Choose a date",
                        style = TnyxTheme.typography.titleMedium,
                        color = TnyxTheme.colors.textPrimary,
                    )
                }
            }
        }
        Text(
            text = "You can update this later from Personal Information.",
            style = TnyxTheme.typography.bodySmall,
            color = TnyxTheme.colors.textMuted,
        )
        if (showValidationError) {
            ValidationMessage("Choose a valid past date")
        }
    }

    if (showDatePicker) {
        TnyxDatePickerDialog(
            title = "Select date of birth",
            supportingText = "This helps personalize your Tio experience",
            initialDate = selectedDate ?: LocalDate.now().minusYears(DEFAULT_AGE_YEARS),
            minimumYear = EARLIEST_BIRTH_YEAR,
            maximumDate = LocalDate.now().minusDays(1),
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                showDatePicker = false
                onAnswerChanged(OnboardingAnswer.Text(date.toString()))
            },
        )
    }
}

@Composable
private fun StepHeading(
    title: String,
    description: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
    ) {
        Text(
            text = title,
            style = TnyxTheme.typography.headlineMedium,
            color = TnyxTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = description,
            style = TnyxTheme.typography.bodyLarge,
            color = TnyxTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun ValidationMessage(message: String) {
    Text(
        text = message,
        style = TnyxTheme.typography.bodyMedium,
        color = TnyxTheme.colors.error,
    )
}

private enum class GenderOption(
    val id: String,
    val label: String,
) {
    Male(id = "male", label = "Male"),
    Female(id = "female", label = "Female"),
    PreferNotToSay(id = "prefer_not_to_say", label = "Prefer not to say"),
}

private const val PROFILE_NAME_MAX_LENGTH = 30
private const val DEFAULT_AGE_YEARS = 25L
private const val EARLIEST_BIRTH_YEAR = 1900
