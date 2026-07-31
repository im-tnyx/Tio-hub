package com.tnyx.features.onboarding.presentation.container

internal data class OnboardingContainerState(
    val showButton: Boolean,
    val buttonText: String,
    val isButtonEnabled: Boolean,
    val showBackButton: Boolean,
    val showProgressBar: Boolean,
)
