package com.tnyx.features.onboarding.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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

@Composable
internal fun OnboardingChoiceCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    selectionMode: OnboardingSelectionMode = OnboardingSelectionMode.Single,
) {
    TnyxCard(
        modifier = modifier.fillMaxWidth(),
        variant = TnyxCardVariant.Outlined,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (selectionMode) {
                OnboardingSelectionMode.Single -> {
                    RadioButton(
                        selected = selected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = TnyxTheme.colors.primary,
                            unselectedColor = TnyxTheme.colors.textMuted,
                        ),
                    )
                }

                OnboardingSelectionMode.Multiple -> {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(
                            checkedColor = TnyxTheme.colors.primary,
                            uncheckedColor = TnyxTheme.colors.textMuted,
                            checkmarkColor = TnyxTheme.colors.onPrimary,
                        ),
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXXS),
            ) {
                Text(
                    text = title,
                    style = TnyxTheme.typography.titleMedium,
                    color = TnyxTheme.colors.textPrimary,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
                if (!badge.isNullOrBlank()) {
                    Text(
                        text = badge,
                        style = TnyxTheme.typography.labelMedium,
                        color = TnyxTheme.colors.primary,
                    )
                }
                Text(
                    text = description,
                    style = TnyxTheme.typography.bodyMedium,
                    color = TnyxTheme.colors.textSecondary,
                )
            }
        }
    }
}

internal enum class OnboardingSelectionMode {
    Single,
    Multiple,
}
