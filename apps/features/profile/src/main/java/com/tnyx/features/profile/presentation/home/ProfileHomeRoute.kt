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

    ProfileHomeScreenNew(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ProfileHomeAction.BackClicked -> onNavigateBack()
                ProfileHomeAction.SupportClicked -> { /* TODO: Navigate to support */ }
                ProfileHomeAction.SettingsClicked -> onOpenSettings()
                ProfileHomeAction.JourneyHistoryClicked -> { /* TODO: Navigate to history */ }
                ProfileHomeAction.AddProgressPhotosClicked -> { /* TODO: Navigate to add photos */ }
                else -> {
                    // TODO: Handle other actions (show toast "Coming soon")
                }
            }
        }
    )
}
