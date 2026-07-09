package com.tnyx.features.settings.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsHomeRoute(
    onNavigateBack: () -> Unit,
    onOpenNutritionTargets: () -> Unit,
    onOpenAppPreferences: () -> Unit,
    onOpenPersonalInfo: () -> Unit,
    viewModel: SettingsHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsHomeScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                SettingsHomeAction.BackClicked -> onNavigateBack()
                SettingsHomeAction.PersonalInfoClicked -> onOpenPersonalInfo()
                SettingsHomeAction.NutritionTargetsClicked -> onOpenNutritionTargets()
                SettingsHomeAction.AppPreferencesClicked -> onOpenAppPreferences()
                SettingsHomeAction.ResetPasswordClicked -> {
                    // TODO: Wire reset password flow when the destination exists.
                }
                else -> {
                    // TODO: Handle other settings actions
                }
            }
        }
    )
}
