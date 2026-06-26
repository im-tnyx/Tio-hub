package com.tnyx.features.splash.presentation.state

data class SplashUiState(
    val isLoading: Boolean = true
)

sealed class SplashEffect {
    object NavigateToWelcome : SplashEffect()
}
