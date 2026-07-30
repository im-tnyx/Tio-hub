package com.tnyx.features.onboarding.presentation.shell

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.presentation.component.OnboardingProgress

@Composable
internal fun OnboardingProgressBar(
    completedFraction: Float,
    modifier: Modifier = Modifier,
) {
    OnboardingProgress(
        completedFraction = completedFraction,
        modifier = modifier,
    )
}
