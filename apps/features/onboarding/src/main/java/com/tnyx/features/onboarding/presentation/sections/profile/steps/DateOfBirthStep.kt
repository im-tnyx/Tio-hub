package com.tnyx.features.onboarding.presentation.sections.profile.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.inputs.TnyxDobPickerDialog
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading
import com.tnyx.features.onboarding.presentation.common.OnboardingValidationMessage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun DateOfBirthStep(
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
        OnboardingStepHeading(
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
            OnboardingValidationMessage("Choose a valid past date")
        }
    }

    if (showDatePicker) {
        TnyxDobPickerDialog(
            title = "DOB Picker",
            supportingText = "This helps personalize your Tio experience",
            initialDate = selectedDate ?: DEFAULT_DATE_OF_BIRTH,
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

private val DEFAULT_DATE_OF_BIRTH: LocalDate = LocalDate.of(2003, 4, 15)
private const val EARLIEST_BIRTH_YEAR = 1950
