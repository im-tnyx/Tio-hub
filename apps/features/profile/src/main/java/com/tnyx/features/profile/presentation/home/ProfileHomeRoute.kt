package com.tnyx.features.profile.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileHomeRoute(
    onOpenSettings: () -> Unit,
    onOpenProgress: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProfileHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileHomeScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ProfileHomeAction.JourneyHistoryClicked -> { /* TODO: Navigate to history */ }
                ProfileHomeAction.ProgressPhotosClicked -> onOpenProgress()
                ProfileHomeAction.AddProgressPhotosClicked -> { /* TODO: Navigate to add photos */ }
                ProfileHomeAction.SettingsClicked -> onOpenSettings()
                ProfileHomeAction.BackClicked -> onNavigateBack()
                ProfileHomeAction.SupportClicked -> { /* TODO: Navigate to support */ }
                else -> {
                    // TODO: Handle other launcher actions like Nutrition, Workout, etc.
                }
            }
        }
    )
}
