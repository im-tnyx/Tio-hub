package com.tnyx.features.profile.presentation.home

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
fun ProfileHomeRoute(
    onOpenSettings: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    showBackButton: Boolean,
    onOpenAvatarViewer: () -> Unit = {},
    viewModel: ProfileHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedCropUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.dismissBottomSheet()
            selectedCropUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.dismissBottomSheet()
            coroutineScope.launch(Dispatchers.IO) {
                runCatching {
                    val tempFile = File(context.cacheDir, "camera_profile_crop_temp.jpg")
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

    ProfileHomeScreen(
        uiState = uiState,
        showBackButton = showBackButton,
        onAction = { action ->
            when (action) {
                ProfileHomeAction.EditProfileClicked -> onOpenEditProfile()
                ProfileHomeAction.AvatarClicked -> {
                    if (uiState.avatarUrl.isNullOrBlank()) {
                        viewModel.openBottomSheet()
                    } else {
                        onOpenAvatarViewer()
                    }
                }
                ProfileHomeAction.ChangePhotoClicked -> viewModel.openBottomSheet()
                ProfileHomeAction.DismissBottomSheet -> viewModel.dismissBottomSheet()
                ProfileHomeAction.CameraClicked -> {
                    cameraLauncher.launch(null)
                }
                ProfileHomeAction.GalleryClicked -> {
                    galleryLauncher.launch("image/*")
                }
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

    // Image Cropper & Transform Dialog for Profile Photo selection
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
