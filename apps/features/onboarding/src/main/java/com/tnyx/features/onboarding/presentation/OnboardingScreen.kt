package com.tnyx.features.onboarding.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.presentation.container.OnboardingContainer

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingContainer(
        state = state,
        onAction = onAction,
        modifier = modifier,
    )
}
