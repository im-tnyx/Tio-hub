package com.tnyx.features.profile.presentation.home

data class ProfileHomeUiState(
    val displayName: String = "TNYX User",
    val planLabel: String = "Free",
    val subtitle: String = "Fitness Hub + Account Launcher"
)

sealed interface ProfileHomeAction {
    data object JourneyClicked : ProfileHomeAction
    data object ProgressPhotosClicked : ProfileHomeAction
    data object SettingsClicked : ProfileHomeAction
}
