package com.tnyx.features.profile.presentation.avatar_viewer

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.tnyx.core.ui.utils.readAvatarJpeg
import com.tnyx.core.ui.utils.toSquareJpegBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AvatarViewerRoute(
    onNavigateBack: () -> Unit,
    viewModel: AvatarViewerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val jpegBytes = withContext(Dispatchers.IO) {
                context.readAvatarJpeg(uri)
            }
            if (jpegBytes != null) {
                viewModel.uploadAvatar(jpegBytes)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap: Bitmap? ->
        if (bitmap == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val jpegBytes = withContext(Dispatchers.IO) {
                bitmap.toSquareJpegBytes()
            }
            if (jpegBytes != null) {
                viewModel.uploadAvatar(jpegBytes)
            }
        }
    }

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
                    cameraLauncher.launch(null)
                }
                AvatarViewerAction.GalleryClicked -> {
                    galleryLauncher.launch("image/*")
                }
            }
        },
    )
}
