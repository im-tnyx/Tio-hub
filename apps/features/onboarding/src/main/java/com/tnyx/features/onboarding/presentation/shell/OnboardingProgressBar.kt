package com.tnyx.features.onboarding.presentation.shell

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme

@Composable
internal fun OnboardingProgressBar(
    completedFraction: Float,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = completedFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 650, easing = LinearOutSlowInEasing),
        label = "onboarding_progress",
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier.fillMaxWidth(),
        color = TnyxTheme.colors.primary,
        trackColor = TnyxTheme.colors.surfaceVariant,
    )
}
