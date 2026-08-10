package com.tnyx.features.nutrition.presentation.meal_editor

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.tnyx.features.nutrition.domain.models.MealItem
import java.time.LocalDate
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MealEditorRoute(
    onNavigateBack: () -> Unit,
    onMealChanged: () -> Unit,
    onNavigateToSearch: (LocalDate) -> Unit,
    onNavigateToItemEditor: (MealItem?) -> Unit,
    itemResult: String? = null,
    removedItemId: String? = null,
    onItemResultConsumed: () -> Unit = {},
    onItemRemovalConsumed: () -> Unit = {},
    initialPhotoPath: String? = null,
    initialPhotoMimeType: String? = null,
    viewModel: MealEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    fun dispatchSelectedPhoto(uri: Uri, fallbackMimeType: String? = null, cleanup: File? = null) {
        coroutineScope.launch {
            runCatching { readMealPhoto(context, uri, fallbackMimeType) }
                .onSuccess { photo ->
                    viewModel.handleAction(
                        MealEditorAction.PhotoSelected(
                            bytes = photo.bytes,
                            mimeType = photo.mimeType,
                        )
                    )
                }
                .onFailure { error ->
                    viewModel.handleAction(
                        MealEditorAction.PhotoSelectionFailed(
                            error.message ?: "Meal photo could not be opened.",
                        )
                    )
                }
            cleanup?.delete()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { dispatchSelectedPhoto(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { captured ->
        val cameraFile = pendingCameraFile
        pendingCameraFile = null
        if (captured && cameraFile != null) {
            dispatchSelectedPhoto(
                uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    cameraFile,
                ),
                fallbackMimeType = "image/jpeg",
                cleanup = cameraFile,
            )
        } else {
            cameraFile?.delete()
        }
    }

    LaunchedEffect(itemResult) {
        itemResult?.let {
            viewModel.acceptItemResult(it)
            onItemResultConsumed()
        }
    }

    LaunchedEffect(removedItemId) {
        removedItemId?.let {
            viewModel.acceptItemRemoval(it)
            onItemRemovalConsumed()
        }
    }

    LaunchedEffect(initialPhotoPath) {
        initialPhotoPath?.let { path ->
            val file = File(path)
            runCatching {
                withContext(Dispatchers.IO) {
                    require(file.length() in 1..MAX_MEAL_PHOTO_BYTES.toLong()) {
                        "Meal photo is too large. Maximum size is 10 MB."
                    }
                    file.readBytes()
                }
            }.onSuccess { bytes ->
                viewModel.handleAction(
                    MealEditorAction.PhotoSelected(
                        bytes = bytes,
                        mimeType = initialPhotoMimeType ?: "image/jpeg",
                    )
                )
            }.onFailure { error ->
                viewModel.handleAction(
                    MealEditorAction.PhotoSelectionFailed(
                        error.message ?: "Meal photo could not be opened.",
                    )
                )
            }
            file.delete()
        }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                MealEditorEffect.NavigateBack -> onNavigateBack()
                MealEditorEffect.MealSaved,
                MealEditorEffect.MealDeleted -> onMealChanged()
                is MealEditorEffect.NavigateToSearch -> onNavigateToSearch(effect.date)
                is MealEditorEffect.NavigateToItemEditor -> onNavigateToItemEditor(effect.item)
                MealEditorEffect.ShowShareOptions -> { /* Show share options */ }
                MealEditorEffect.LaunchGallery -> galleryLauncher.launch("image/*")
                MealEditorEffect.LaunchCamera -> {
                    runCatching {
                        createMealCameraFile(context).also { cameraFile ->
                            pendingCameraFile = cameraFile
                        }
                    }.onSuccess { cameraFile ->
                        cameraLauncher.launch(
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                cameraFile,
                            )
                        )
                    }.onFailure { error ->
                        viewModel.handleAction(
                            MealEditorAction.PhotoSelectionFailed(
                                error.message ?: "Camera could not be opened.",
                            )
                        )
                    }
                }
            }
        }
    }

    MealEditorScreen(
        state = uiState,
        onAction = viewModel::handleAction,
        snackbarHostState = snackbarHostState,
    )
}

private data class SelectedMealPhoto(
    val bytes: ByteArray,
    val mimeType: String,
)

private fun createMealCameraFile(context: Context): File {
    val directory = File(context.cacheDir, "meal-camera").apply { mkdirs() }
    return File.createTempFile("meal-", ".jpg", directory)
}

private suspend fun readMealPhoto(
    context: Context,
    uri: Uri,
    fallbackMimeType: String?,
): SelectedMealPhoto = withContext(Dispatchers.IO) {
    val mimeType = context.contentResolver.getType(uri)
        ?.lowercase()
        ?: fallbackMimeType
        ?: error("Selected file type is unavailable.")
    val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var totalBytes = 0
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            totalBytes += count
            require(totalBytes <= MAX_MEAL_PHOTO_BYTES) {
                "Meal photo is too large. Maximum size is 10 MB."
            }
            output.write(buffer, 0, count)
        }
        output.toByteArray()
    } ?: error("Meal photo could not be opened.")
    SelectedMealPhoto(bytes = bytes, mimeType = mimeType)
}

private const val MAX_MEAL_PHOTO_BYTES = 10 * 1024 * 1024
