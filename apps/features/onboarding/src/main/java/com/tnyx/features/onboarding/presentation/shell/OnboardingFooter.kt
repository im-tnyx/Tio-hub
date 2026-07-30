package com.tnyx.features.onboarding.presentation.shell

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.presentation.component.OnboardingBottomBar

@Composable
internal fun OnboardingFooter(
    visible: Boolean,
    text: String,
    enabled: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingBottomBar(
        modifier = modifier,
        visible = visible,
        text = text,
        enabled = enabled,
        onClick = onContinue,
    )
}
