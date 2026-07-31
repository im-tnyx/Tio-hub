package com.tnyx.features.onboarding.presentation.sections.intro.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading

@Composable
internal fun IntroWelcomeStep(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        OnboardingStepHeading(
            title = "Welcome to Tio",
            description = "We will set up your profile, goals, workout preferences, and daily targets in a lightweight flow you can upgrade later.",
        )
        IntroValueCard(
            title = "Personal baseline",
            description = "Add your basic profile and body goal details first so the next recommendations stay grounded.",
        )
        IntroValueCard(
            title = "Workout, if you want it",
            description = "You can choose whether to continue into workout planning or skip straight to targets for now.",
        )
        IntroValueCard(
            title = "Fast first setup",
            description = "This is the simple version of onboarding. The section structure is ready for richer intro screens later.",
        )
        Text(
            text = "Tap Continue to start.",
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun IntroValueCard(
    title: String,
    description: String,
) {
    TnyxCard(
        modifier = Modifier.fillMaxWidth(),
        variant = TnyxCardVariant.Outlined,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXXS),
        ) {
            Text(
                text = title,
                style = TnyxTheme.typography.titleMedium,
                color = TnyxTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textSecondary,
            )
        }
    }
}
