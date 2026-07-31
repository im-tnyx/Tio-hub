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
                AvatarViewerAction.EditClicked -> viewModel.openBottomSheet()
                AvatarViewerAction.DismissBottomSheet -> viewModel.dismissBottomSheet()
                AvatarViewerAction.DeleteClicked -> { /* TODO: handle delete photo */ }
                AvatarViewerAction.DownloadClicked -> { /* TODO: handle download photo */ }
                AvatarViewerAction.CameraClicked -> {
                    viewModel.dismissBottomSheet()
                    /* TODO: handle camera picker */
                }
                AvatarViewerAction.GalleryClicked -> {
                    viewModel.dismissBottomSheet()
                    /* TODO: handle gallery picker */
                }
            }
        },
    )
}
