package com.tnyx.features.settings.presentation.home

data class SettingsHomeUiState(
    val title: String = "Settings",
    val subtitle: String = "App Config"
)

sealed interface SettingsHomeAction {
    data object BackClicked : SettingsHomeAction
}
