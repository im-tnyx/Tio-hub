package com.tnyx.features.profile.presentation.avatar_viewer

data class AvatarViewerUiState(
    val avatarUrl: String? = null,
    val displayName: String = "",
    val isBottomSheetVisible: Boolean = false,
)

sealed interface AvatarViewerAction {
    data object BackClicked : AvatarViewerAction
    data object EditClicked : AvatarViewerAction
    data object DismissBottomSheet : AvatarViewerAction
    data object DeleteClicked : AvatarViewerAction
    data object DownloadClicked : AvatarViewerAction
    data object CameraClicked : AvatarViewerAction
    data object GalleryClicked : AvatarViewerAction
}
