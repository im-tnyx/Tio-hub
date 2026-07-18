package com.tnyx.features.settings.presentation.personal_info

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.ByteArrayOutputStream
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val AVATAR_MAX_EDGE_PX = 1024
private const val AVATAR_JPEG_QUALITY = 88

@Composable
fun PersonalInfoRoute(
    onNavigateBack: () -> Unit,
    viewModel: PersonalInfoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showPhotoMenu by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        coroutineScope.launch {
            val jpegBytes = withContext(Dispatchers.IO) {
                context.readAvatarJpeg(uri)
            }
            if (jpegBytes == null) {
                viewModel.onAction(PersonalInfoAction.OnDismissAvatarError)
            } else {
                viewModel.onAction(PersonalInfoAction.OnAvatarBytesReady(jpegBytes))
            }
        }
    }

    PersonalInfoScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                PersonalInfoAction.OnBackClicked -> onNavigateBack()
                PersonalInfoAction.OnChangePhotoClicked -> showPhotoMenu = true
                else -> viewModel.onAction(action)
            }
        },
    )

    if (showPhotoMenu) {
        AlertDialog(
            onDismissRequest = { showPhotoMenu = false },
            title = { Text("Profile photo") },
            text = {
                Text(
                    if (uiState.avatarUrl.isNullOrBlank()) {
                        "Choose a photo from your device. It will be cropped to a square before upload."
                    } else {
                        "Choose a new photo or remove the current profile photo."
                    },
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.isAvatarUploading,
                    onClick = {
                        showPhotoMenu = false
                        galleryLauncher.launch("image/*")
                    },
                ) {
                    Text("Choose photo")
                }
            },
            dismissButton = {
                if (!uiState.avatarUrl.isNullOrBlank()) {
                    TextButton(
                        enabled = !uiState.isAvatarUploading,
                        onClick = {
                            showPhotoMenu = false
                            viewModel.onAction(PersonalInfoAction.OnRemovePhotoClicked)
                        },
                    ) {
                        Text("Remove")
                    }
                } else {
                    TextButton(onClick = { showPhotoMenu = false }) {
                        Text("Cancel")
                    }
                }
            },
        )
    }

    uiState.avatarError?.let { error ->
        AlertDialog(
            onDismissRequest = {
                viewModel.onAction(PersonalInfoAction.OnDismissAvatarError)
            },
            title = { Text("Profile photo") },
            text = { Text(error) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onAction(PersonalInfoAction.OnDismissAvatarError)
                    },
                ) {
                    Text("OK")
                }
            },
        )
    }
}

private fun Context.readAvatarJpeg(uri: Uri): ByteArray? {
    val source = contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) ?: return null

    return try {
        val edge = min(source.width, source.height)
        val left = (source.width - edge) / 2
        val top = (source.height - edge) / 2
        val square = Bitmap.createBitmap(source, left, top, edge, edge)
        val outputSize = min(edge, AVATAR_MAX_EDGE_PX)
        val scaled = if (square.width == outputSize) {
            square
        } else {
            Bitmap.createScaledBitmap(square, outputSize, outputSize, true)
        }

        ByteArrayOutputStream().use { output ->
            if (!scaled.compress(Bitmap.CompressFormat.JPEG, AVATAR_JPEG_QUALITY, output)) {
                return null
            }
            output.toByteArray()
        }.also {
            if (scaled !== square) scaled.recycle()
            if (square !== source) square.recycle()
        }
    } finally {
        source.recycle()
    }
}
