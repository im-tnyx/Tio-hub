package com.tnyx.features.onboarding.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.theme.TnyxTheme

@Composable
internal fun OnboardingStepHeading(
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
internal fun OnboardingValidationMessage(message: String) {
    Text(
        text = message,
        style = TnyxTheme.typography.bodyMedium,
        color = TnyxTheme.colors.error,
    )
}
