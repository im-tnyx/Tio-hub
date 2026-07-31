package com.tnyx.features.profile.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileHomeRoute(
    onOpenSettings: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    showBackButton: Boolean,
    onOpenAvatarViewer: () -> Unit = {},
    viewModel: ProfileHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileHomeScreen(
        uiState = uiState,
        showBackButton = showBackButton,
        onAction = { action ->
            when (action) {
                ProfileHomeAction.EditProfileClicked -> onOpenEditProfile()
                ProfileHomeAction.AvatarClicked -> onOpenAvatarViewer()
                ProfileHomeAction.AddProgressPhotosClicked -> { /* TODO: Navigate to add photos */ }
                ProfileHomeAction.SettingsClicked -> onOpenSettings()
                ProfileHomeAction.BackClicked -> onNavigateBack()
                ProfileHomeAction.JourneyHistoryClicked -> { /* TODO: Navigate to history */ }
                ProfileHomeAction.ProgressPhotosClicked -> { /* TODO: Navigate to progress */ }
                ProfileHomeAction.SupportClicked -> { /* TODO: Navigate to support */ }
                ProfileHomeAction.ViewAllProgressClicked -> { /* TODO: Navigate to progress */ }
                ProfileHomeAction.HealthConnectionsClicked -> { /* TODO: Navigate to health connections */ }
                ProfileHomeAction.RefreshProfile -> viewModel.loadUserProfile()
            }
        }
    )
}
