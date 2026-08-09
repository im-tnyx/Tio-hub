package com.tnyx.features.workout.presentation.library.createexercise

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
    var selectedCropUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                CreateExerciseEvent.SaveSuccess -> onSaveSuccess()
                is CreateExerciseEvent.SaveError -> {
                    // Handled if needed
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onAction(CreateExerciseAction.ImageSourceBottomSheetDismissed)
            selectedCropUri = uri
        }
    }

    CreateExerciseScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                CreateExerciseAction.BackClicked -> onNavigateBack()
                CreateExerciseAction.GalleryClicked -> {
                    viewModel.onAction(action)
                    galleryLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
                else -> viewModel.onAction(action)
            }
        },
        modifier = modifier
    )

    // Automatic Image Cropper & Transform Flow after selecting image
    ImageCropperDialog(
        visible = selectedCropUri != null,
        imageUri = selectedCropUri,
        onDismissRequest = { selectedCropUri = null },
        onCropSuccess = { croppedBytes ->
            selectedCropUri = null
            coroutineScope.launch(Dispatchers.IO) {
                runCatching {
                    val file = File(context.cacheDir, "cropped_exercise_${System.currentTimeMillis()}.jpg")
                    file.outputStream().use { out -> out.write(croppedBytes) }
                    Uri.fromFile(file).toString()
                }.getOrNull()?.let { croppedUriString ->
                    withContext(Dispatchers.Main) {
                        viewModel.onAction(CreateExerciseAction.AssetSelected(croppedUriString))
                    }
                }
            }
        }
    )
}
