package com.tnyx.features.workout.presentation.library.createexercise

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tnyx.core.ui.components.image.ImageCropperDialog
import com.tnyx.core.ui.components.image.ImageCropOutputFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val MAX_SELECTED_IMAGE_BYTES = 10L * 1024 * 1024
private const val MAX_SELECTED_VIDEO_BYTES = 50L * 1024 * 1024

@Composable
fun CreateExerciseRoute(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateExerciseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingCropMedia by remember { mutableStateOf<ExerciseMediaSelection?>(null) }
    var pendingTrimMedia by remember { mutableStateOf<ExerciseMediaSelection?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                CreateExerciseEvent.SaveSuccess -> onSaveSuccess()
                is CreateExerciseEvent.SaveError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onAction(CreateExerciseAction.ImageSourceBottomSheetDismissed)
            val mediaSelection = resolveExerciseMediaSelection(
                uri = uri,
                mimeType = context.contentResolver.getType(uri),
            )
            when {
                mediaSelection == null -> coroutineScope.launch {
                    snackbarHostState.showSnackbar("Selected media type is not supported.")
                }
                mediaSelection.shouldCrop -> pendingCropMedia = mediaSelection
                mediaSelection.isVideo -> pendingTrimMedia = mediaSelection
                else -> coroutineScope.launch(Dispatchers.IO) {
                    runCatching {
                        copySelectedMediaToCache(context, mediaSelection)
                    }.onSuccess { mediaFile ->
                        withContext(Dispatchers.Main) {
                            viewModel.onAction(
                                CreateExerciseAction.AssetSelected(
                                    uri = Uri.fromFile(mediaFile).toString(),
                                    localFilePath = mediaFile.absolutePath,
                                    mimeType = mediaSelection.mimeType,
                                )
                            )
                        }
                    }.onFailure { error ->
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(error.toMediaSelectionMessage())
                        }
                    }
                }
            }
        }
    }

    CreateExerciseScreen(
        state = uiState,
        snackbarHostState = snackbarHostState,
        onAction = { action ->
            when (action) {
                CreateExerciseAction.BackClicked -> onNavigateBack()
                CreateExerciseAction.GalleryClicked -> {
                    viewModel.onAction(action)
                    galleryLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageAndVideo
                        )
                    )
                }
                else -> viewModel.onAction(action)
            }
        },
        modifier = modifier
    )

    // Static images are cropped; animated GIF and video files remain intact.
    ImageCropperDialog(
        visible = pendingCropMedia != null,
        imageUri = pendingCropMedia?.uri,
        outputFormat = pendingCropMedia?.cropOutputFormat ?: ImageCropOutputFormat.JPEG,
        onDismissRequest = { pendingCropMedia = null },
        onCropSuccess = { croppedBytes ->
            val mediaSelection = pendingCropMedia ?: return@ImageCropperDialog
            pendingCropMedia = null
            coroutineScope.launch(Dispatchers.IO) {
                val croppedUriString = runCatching {
                    require(croppedBytes.size <= MAX_SELECTED_IMAGE_BYTES) {
                        "Exercise image is too large. Maximum size is 10 MB."
                    }
                    val file = File(
                        context.cacheDir,
                        "cropped_exercise_${System.currentTimeMillis()}.${mediaSelection.extension}",
                    )
                    file.outputStream().use { out -> out.write(croppedBytes) }
                    file
                }.getOrElse { error ->
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar(error.toMediaSelectionMessage())
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    viewModel.onAction(
                        CreateExerciseAction.AssetSelected(
                            uri = Uri.fromFile(croppedUriString).toString(),
                            localFilePath = croppedUriString.absolutePath,
                            mimeType = mediaSelection.mimeType,
                        )
                    )
                }
            }
        }
    )

    VideoTrimDialog(
        visible = pendingTrimMedia != null,
        videoUri = pendingTrimMedia?.uri,
        maxOutputBytes = MAX_SELECTED_VIDEO_BYTES,
        onDismissRequest = { pendingTrimMedia = null },
        onTrimSuccess = { trimmedFile ->
            pendingTrimMedia = null
            viewModel.onAction(
                CreateExerciseAction.AssetSelected(
                    uri = Uri.fromFile(trimmedFile).toString(),
                    localFilePath = trimmedFile.absolutePath,
                    mimeType = "video/mp4",
                )
            )
        },
        onError = { message ->
            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
        },
    )
}

private data class ExerciseMediaSelection(
    val uri: Uri,
    val mimeType: String,
    val extension: String,
    val isVideo: Boolean,
    val shouldCrop: Boolean,
    val cropOutputFormat: ImageCropOutputFormat = ImageCropOutputFormat.JPEG,
) {
    val maxBytes: Long
        get() = if (isVideo) MAX_SELECTED_VIDEO_BYTES else MAX_SELECTED_IMAGE_BYTES
}

private fun resolveExerciseMediaSelection(
    uri: Uri,
    mimeType: String?,
): ExerciseMediaSelection? {
    val normalizedMimeType = mimeType?.lowercase()
        ?: uri.lastPathSegment?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            ?.toMimeType()
        ?: return null
    return when (normalizedMimeType) {
        "image/jpeg", "image/jpg" -> ExerciseMediaSelection(
            uri = uri,
            mimeType = "image/jpeg",
            extension = "jpg",
            isVideo = false,
            shouldCrop = true,
        )
        "image/png" -> ExerciseMediaSelection(
            uri = uri,
            mimeType = "image/png",
            extension = "png",
            isVideo = false,
            shouldCrop = true,
            cropOutputFormat = ImageCropOutputFormat.PNG,
        )
        "image/gif" -> ExerciseMediaSelection(
            uri = uri,
            mimeType = "image/gif",
            extension = "gif",
            isVideo = false,
            shouldCrop = false,
        )
        "video/mp4" -> ExerciseMediaSelection(uri, "video/mp4", "mp4", true, false)
        "video/webm" -> ExerciseMediaSelection(uri, "video/webm", "webm", true, false)
        "video/quicktime" -> ExerciseMediaSelection(uri, "video/quicktime", "mov", true, false)
        "video/x-m4v" -> ExerciseMediaSelection(uri, "video/x-m4v", "m4v", true, false)
        "video/3gpp" -> ExerciseMediaSelection(uri, "video/3gpp", "3gp", true, false)
        else -> null
    }
}

private fun String.toMimeType(): String? = when (this) {
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "gif" -> "image/gif"
    "mp4" -> "video/mp4"
    "webm" -> "video/webm"
    "mov" -> "video/quicktime"
    "m4v" -> "video/x-m4v"
    "3gp", "3gpp" -> "video/3gpp"
    else -> null
}

private fun copySelectedMediaToCache(
    context: Context,
    selection: ExerciseMediaSelection,
): File {
    val mediaFile = File(
        context.cacheDir,
        "exercise_media_${System.currentTimeMillis()}.${selection.extension}",
    )
    return runCatching {
        val declaredSize = runCatching {
            context.contentResolver.openAssetFileDescriptor(selection.uri, "r")?.use { descriptor ->
                descriptor.length.takeIf { length -> length >= 0L }
            }
        }.getOrNull()
        require(declaredSize == null || declaredSize <= selection.maxBytes) {
            selection.sizeLimitMessage
        }

        context.contentResolver.openInputStream(selection.uri)?.use { input ->
            mediaFile.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var totalBytes = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    totalBytes += count
                    require(totalBytes <= selection.maxBytes) {
                        selection.sizeLimitMessage
                    }
                    output.write(buffer, 0, count)
                }
            }
        } ?: error("Selected exercise media could not be opened.")
        require(mediaFile.length() > 0L) { "Selected exercise media is empty." }
        mediaFile
    }.getOrElse { error ->
        mediaFile.delete()
        throw error
    }
}

private val ExerciseMediaSelection.sizeLimitMessage: String
    get() = if (isVideo) {
        "Exercise video is too large. Maximum size is 50 MB."
    } else {
        "Exercise image is too large. Maximum size is 10 MB."
    }

private fun Throwable.toMediaSelectionMessage(): String {
    return message?.takeIf(String::isNotBlank)
        ?: "Exercise media could not be prepared. Try again."
}
