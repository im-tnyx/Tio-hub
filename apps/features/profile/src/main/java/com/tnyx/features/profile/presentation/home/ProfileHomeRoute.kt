package com.tnyx.features.profile.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileHomeRoute(
    onOpenSettings: () -> Unit,
    onOpenProgress: () -> Unit,
    viewModel: ProfileHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileHomeScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ProfileHomeAction.JourneyClicked,
                ProfileHomeAction.ProgressPhotosClicked -> onOpenProgress()
                ProfileHomeAction.SettingsClicked -> onOpenSettings()
            }
        }
    )
}
