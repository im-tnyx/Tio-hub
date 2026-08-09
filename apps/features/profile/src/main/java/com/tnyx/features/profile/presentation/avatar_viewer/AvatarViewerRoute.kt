package com.tnyx.features.profile.presentation.avatar_viewer

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.tnyx.core.ui.components.image.ImageCropperDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun AvatarViewerRoute(
    onNavigateBack: () -> Unit,
    viewModel: AvatarViewerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedCropUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.dismissEditSheet()
            selectedCropUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.dismissEditSheet()
            coroutineScope.launch(Dispatchers.IO) {
                runCatching {
                    val tempFile = File(context.cacheDir, "camera_crop_temp.jpg")
                    FileOutputStream(tempFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    }
                    Uri.fromFile(tempFile)
                }.getOrNull()?.let { uri ->
                    withContext(Dispatchers.Main) {
                        selectedCropUri = uri
                    }
                }
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
                AvatarViewerAction.DownloadClicked -> { /* Handle download */ }
                AvatarViewerAction.CameraClicked -> {
                    cameraLauncher.launch(null)
                }
                AvatarViewerAction.GalleryClicked -> {
                    galleryLauncher.launch("image/*")
                }
            }
        },
    )

    // Crop / Transform Dialog for Profile Photo
    ImageCropperDialog(
        visible = selectedCropUri != null,
        imageUri = selectedCropUri,
        onDismissRequest = { selectedCropUri = null },
        onCropSuccess = { croppedBytes ->
            selectedCropUri = null
            viewModel.uploadAvatar(croppedBytes)
        }
    )
}
