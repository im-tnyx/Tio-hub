package com.tnyx.features.profile.presentation.avatar_viewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AvatarViewerRoute(
    onNavigateBack: () -> Unit,
    viewModel: AvatarViewerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    AvatarViewerScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                AvatarViewerAction.BackClicked -> onNavigateBack()
                AvatarViewerAction.EditClicked -> viewModel.openEditSheet()
                AvatarViewerAction.DismissEditSheet -> viewModel.dismissEditSheet()
                AvatarViewerAction.DeleteClicked -> viewModel.openDeleteSheet()
                AvatarViewerAction.DismissDeleteSheet -> viewModel.dismissDeleteSheet()
                AvatarViewerAction.ConfirmDeleteClicked -> viewModel.confirmDeletePhoto(onSuccess = onNavigateBack)
                AvatarViewerAction.DownloadClicked -> { /* TODO: handle download photo */ }
                AvatarViewerAction.CameraClicked -> {
                    viewModel.dismissEditSheet()
                    /* TODO: handle camera picker */
                }
                AvatarViewerAction.GalleryClicked -> {
                    viewModel.dismissEditSheet()
                    /* TODO: handle gallery picker */
                }
            }
        },
    )
}
