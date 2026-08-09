package com.tnyx.core.ui.components.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.tnyx.core.theme.TnyxTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class CropAspectRatio(val ratio: Float, val label: String) {
    SQUARE(1f, "1 : 1"),
}

enum class ImageCropOutputFormat {
    JPEG,
    PNG,
}

@Composable
fun ImageCropperDialog(
    visible: Boolean,
    imageUri: Uri?,
    onDismissRequest: () -> Unit,
    onCropSuccess: (ByteArray) -> Unit,
    outputFormat: ImageCropOutputFormat = ImageCropOutputFormat.JPEG,
    modifier: Modifier = Modifier,
) {
    if (!visible || imageUri == null) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cropperState = rememberImageCropperState()
    var loadedBitmap by remember(imageUri) { mutableStateOf<Bitmap?>(null) }
    var fineAngle by remember { mutableFloatStateOf(0f) }
    var discreteRotation by remember { mutableFloatStateOf(0f) }
    var isFlippedHorizontal by remember { mutableStateOf(false) }
    var selectedAspectRatio by remember { mutableStateOf(CropAspectRatio.SQUARE) }
    var isProcessing by remember { mutableStateOf(false) }
    var showDiscardConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(imageUri) {
        loadedBitmap = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(imageUri)?.use(BitmapFactory::decodeStream)
            }.getOrNull()
        }
    }

    val totalRotation = (fineAngle + discreteRotation) % 360f
    val dialogBackgroundColor = TnyxTheme.colors.background

    Dialog(
        onDismissRequest = { showDiscardConfirmation = true },
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            decorFitsSystemWindows = true,
        ),
    ) {
        val dialogWindow = (LocalView.current.parent as DialogWindowProvider).window
        SideEffect {
            dialogWindow.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
            )
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(dialogBackgroundColor),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ImageCropperCanvas(
                    bitmap = loadedBitmap,
                    totalRotation = totalRotation,
                    isFlippedHorizontal = isFlippedHorizontal,
                    state = cropperState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                ImageCropperToolbar(
                    fineAngle = fineAngle,
                    isFlippedHorizontal = isFlippedHorizontal,
                    selectedAspectRatio = selectedAspectRatio,
                    isProcessing = isProcessing,
                    canApply = loadedBitmap != null,
                    onFineAngleChange = { fineAngle = it },
                    onFlip = { isFlippedHorizontal = !isFlippedHorizontal },
                    onRotateQuarterTurn = {
                        discreteRotation = (discreteRotation + 90f) % 360f
                    },
                    onReset = {
                        coroutineScope.launch { cropperState.reset() }
                        fineAngle = 0f
                        discreteRotation = 0f
                        isFlippedHorizontal = false
                        selectedAspectRatio = CropAspectRatio.SQUARE
                    },
                    onAspectRatioSelected = { selectedAspectRatio = it },
                    onCancel = { showDiscardConfirmation = true },
                    onApply = {
                        loadedBitmap?.let { bitmap ->
                            isProcessing = true
                            coroutineScope.launch(Dispatchers.IO) {
                                val croppedBytes = processAndCropBitmap(
                                    source = bitmap,
                                    stageSize = cropperState.previewStageSize,
                                    imageDisplaySize = cropperState.previewImageDisplaySize,
                                    totalRotation = totalRotation,
                                    isFlipped = isFlippedHorizontal,
                                    scale = cropperState.activeEffectiveScale,
                                    offset = Offset(cropperState.offsetX.value, cropperState.offsetY.value),
                                    cropLeft = cropperState.cropLeft.value,
                                    cropTop = cropperState.cropTop.value,
                                    cropRight = cropperState.cropRight.value,
                                    cropBottom = cropperState.cropBottom.value,
                                    outputFormat = outputFormat,
                                )
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    if (croppedBytes != null) {
                                        onCropSuccess(croppedBytes)
                                    } else {
                                        onDismissRequest()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (showDiscardConfirmation) {
                ImageCropperDiscardDialog(
                    onKeepEditing = { showDiscardConfirmation = false },
                    onDiscard = {
                        showDiscardConfirmation = false
                        onDismissRequest()
                    },
                )
            }
        }
    }
}
