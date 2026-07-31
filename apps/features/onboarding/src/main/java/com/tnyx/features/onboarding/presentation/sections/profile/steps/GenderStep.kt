package com.tnyx.features.onboarding.presentation.sections.profile.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading
import com.tnyx.features.onboarding.presentation.common.OnboardingValidationMessage

@Composable
internal fun GenderStep(
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
        OnboardingStepHeading(
            title = "How do you identify?",
            description = "This helps Tio personalize guidance while keeping your profile respectful.",
        )
        GenderOption.entries.forEach { option ->
            val isSelected = selectedId == option.id
            TnyxCard(
                modifier = Modifier.fillMaxWidth(),
                variant = TnyxCardVariant.Outlined,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
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
            OnboardingValidationMessage("Select one option to continue")
        }
    }
}

private enum class GenderOption(
    val id: String,
    val label: String,
) {
    Male(id = "male", label = "Male"),
    Female(id = "female", label = "Female"),
    PreferNotToSay(id = "prefer_not_to_say", label = "Prefer not to say"),
}
