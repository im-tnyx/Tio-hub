package com.tnyx.features.workout.presentation.library.createexercise

import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.tnyx.core.theme.TnyxTheme
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val MAX_VIDEO_CROP_SCALE = 6f
private const val CROP_SETTLE_DURATION_MILLIS = 320

internal enum class VideoCropHandle {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
}

@Stable
internal class VideoCropEditorState {
    var scale by mutableFloatStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set
    var cropRect by mutableStateOf(Rect.Zero)
        private set
    var stageSize by mutableStateOf(Size.Zero)
        private set
    var imageDisplaySize by mutableStateOf(Size.Zero)
        private set
    private var cropAreaFraction = 1f

    private var pendingSelection: NormalizedVideoCrop = NormalizedVideoCrop.Full
    private var needsSelectionLoad = true

    fun showSelection(selection: NormalizedVideoCrop) {
        pendingSelection = selection
        needsSelectionLoad = true
        loadPendingSelectionIfPossible()
    }

    fun updateGeometry(
        newStageSize: Size,
        newImageDisplaySize: Size,
        newCropAreaFraction: Float,
    ) {
        val geometryChanged = stageSize != newStageSize || imageDisplaySize != newImageDisplaySize
        stageSize = newStageSize
        imageDisplaySize = newImageDisplaySize
        cropAreaFraction = newCropAreaFraction.coerceIn(0f, 1f)
        if (geometryChanged) needsSelectionLoad = true
        loadPendingSelectionIfPossible()
    }

    fun transform(pan: Offset, zoom: Float) {
        if (!isReady()) return
        val requestedScale = (scale * zoom).coerceIn(1f, MAX_VIDEO_CROP_SCALE)
        val minimumScale = minimumScaleForCrop()
        scale = max(requestedScale, minimumScale)
        offset = clampOffset(offset + pan, scale)
        pendingSelection = currentSelection()
    }

    fun resize(handle: VideoCropHandle, dragAmount: Offset, minimumCropSize: Float) {
        if (!isReady()) return
        val imageBounds = transformedImageBounds()
        cropRect = when (handle) {
            VideoCropHandle.TOP_LEFT -> Rect(
                left = (cropRect.left + dragAmount.x).coerceIn(
                    imageBounds.left,
                    cropRect.right - minimumCropSize,
                ),
                top = (cropRect.top + dragAmount.y).coerceIn(
                    imageBounds.top,
                    cropRect.bottom - minimumCropSize,
                ),
                right = cropRect.right,
                bottom = cropRect.bottom,
            )

            VideoCropHandle.TOP_RIGHT -> Rect(
                left = cropRect.left,
                top = (cropRect.top + dragAmount.y).coerceIn(
                    imageBounds.top,
                    cropRect.bottom - minimumCropSize,
                ),
                right = (cropRect.right + dragAmount.x).coerceIn(
                    cropRect.left + minimumCropSize,
                    imageBounds.right,
                ),
                bottom = cropRect.bottom,
            )

            VideoCropHandle.BOTTOM_LEFT -> Rect(
                left = (cropRect.left + dragAmount.x).coerceIn(
                    imageBounds.left,
                    cropRect.right - minimumCropSize,
                ),
                top = cropRect.top,
                right = cropRect.right,
                bottom = (cropRect.bottom + dragAmount.y).coerceIn(
                    cropRect.top + minimumCropSize,
                    imageBounds.bottom,
                ),
            )

            VideoCropHandle.BOTTOM_RIGHT -> Rect(
                left = cropRect.left,
                top = cropRect.top,
                right = (cropRect.right + dragAmount.x).coerceIn(
                    cropRect.left + minimumCropSize,
                    imageBounds.right,
                ),
                bottom = (cropRect.bottom + dragAmount.y).coerceIn(
                    cropRect.top + minimumCropSize,
                    imageBounds.bottom,
                ),
            )
        }
        scale = max(scale, minimumScaleForCrop())
        offset = clampOffset(offset, scale)
        pendingSelection = currentSelection()
    }

    suspend fun settleCropToDefault() {
        if (!isReady()) return
        val selection = currentSelection()
        val target = layoutForSelection(selection)
        val startScale = scale
        val startOffset = offset
        val startCrop = cropRect
        Animatable(0f).animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = CROP_SETTLE_DURATION_MILLIS,
                easing = FastOutSlowInEasing,
            ),
        ) {
            scale = lerp(startScale, target.scale, value)
            offset = Offset(
                x = lerp(startOffset.x, target.offset.x, value),
                y = lerp(startOffset.y, target.offset.y, value),
            )
            cropRect = Rect(
                left = lerp(startCrop.left, target.cropRect.left, value),
                top = lerp(startCrop.top, target.cropRect.top, value),
                right = lerp(startCrop.right, target.cropRect.right, value),
                bottom = lerp(startCrop.bottom, target.cropRect.bottom, value),
            )
        }
        pendingSelection = selection
    }

    fun currentSelection(): NormalizedVideoCrop {
        if (!isReady()) return pendingSelection
        val imageBounds = transformedImageBounds()
        return NormalizedVideoCrop(
            left = ((cropRect.left - imageBounds.left) / imageBounds.width).coerceIn(0f, 1f),
            top = ((cropRect.top - imageBounds.top) / imageBounds.height).coerceIn(0f, 1f),
            right = ((cropRect.right - imageBounds.left) / imageBounds.width).coerceIn(0f, 1f),
            bottom = ((cropRect.bottom - imageBounds.top) / imageBounds.height).coerceIn(0f, 1f),
        )
    }

    private fun loadPendingSelectionIfPossible() {
        if (!needsSelectionLoad || !isReady()) return
        val target = layoutForSelection(pendingSelection)
        cropRect = target.cropRect
        scale = target.scale
        offset = target.offset
        needsSelectionLoad = false
    }

    private fun layoutForSelection(selection: NormalizedVideoCrop): VideoCropLayout {
        val selectionWidth = (selection.right - selection.left).coerceAtLeast(Float.MIN_VALUE)
        val selectionHeight = (selection.bottom - selection.top).coerceAtLeast(Float.MIN_VALUE)
        val selectionAspect = imageDisplaySize.width * selectionWidth /
            (imageDisplaySize.height * selectionHeight).coerceAtLeast(Float.MIN_VALUE)
        val availableWidth = stageSize.width * cropAreaFraction
        val availableHeight = stageSize.height * cropAreaFraction
        val availableAspect = availableWidth / availableHeight.coerceAtLeast(Float.MIN_VALUE)
        val desiredWidth = if (selectionAspect >= availableAspect) {
            availableWidth
        } else {
            availableHeight * selectionAspect
        }
        val desiredHeight = if (selectionAspect >= availableAspect) {
            availableWidth / selectionAspect
        } else {
            availableHeight
        }
        val desiredScale = max(
            desiredWidth / (imageDisplaySize.width * selectionWidth),
            desiredHeight / (imageDisplaySize.height * selectionHeight),
        )
        val targetScale = desiredScale.coerceIn(1f, MAX_VIDEO_CROP_SCALE)
        val targetWidth = imageDisplaySize.width * selectionWidth * targetScale
        val targetHeight = imageDisplaySize.height * selectionHeight * targetScale
        val targetCrop = Rect(
            left = (stageSize.width - targetWidth) / 2f,
            top = (stageSize.height - targetHeight) / 2f,
            right = (stageSize.width + targetWidth) / 2f,
            bottom = (stageSize.height + targetHeight) / 2f,
        )
        val scaledImageWidth = imageDisplaySize.width * targetScale
        val scaledImageHeight = imageDisplaySize.height * targetScale
        val imageLeft = targetCrop.left - selection.left * scaledImageWidth
        val imageTop = targetCrop.top - selection.top * scaledImageHeight
        return VideoCropLayout(
            cropRect = targetCrop,
            scale = targetScale,
            offset = Offset(
                x = imageLeft + scaledImageWidth / 2f - stageSize.width / 2f,
                y = imageTop + scaledImageHeight / 2f - stageSize.height / 2f,
            ),
        )
    }

    private fun minimumScaleForCrop(): Float = max(
        1f,
        max(
            cropRect.width / imageDisplaySize.width.coerceAtLeast(1f),
            cropRect.height / imageDisplaySize.height.coerceAtLeast(1f),
        ),
    )

    private fun clampOffset(proposed: Offset, effectiveScale: Float): Offset {
        val halfWidth = imageDisplaySize.width * effectiveScale / 2f
        val halfHeight = imageDisplaySize.height * effectiveScale / 2f
        val centerX = stageSize.width / 2f
        val centerY = stageSize.height / 2f
        return Offset(
            x = proposed.x.coerceIn(
                cropRect.right - centerX - halfWidth,
                cropRect.left - centerX + halfWidth,
            ),
            y = proposed.y.coerceIn(
                cropRect.bottom - centerY - halfHeight,
                cropRect.top - centerY + halfHeight,
            ),
        )
    }

    private fun transformedImageBounds(): Rect {
        val centerX = stageSize.width / 2f + offset.x
        val centerY = stageSize.height / 2f + offset.y
        val halfWidth = imageDisplaySize.width * scale / 2f
        val halfHeight = imageDisplaySize.height * scale / 2f
        return Rect(
            left = centerX - halfWidth,
            top = centerY - halfHeight,
            right = centerX + halfWidth,
            bottom = centerY + halfHeight,
        )
    }

    private fun isReady(): Boolean =
        stageSize.width > 0f && stageSize.height > 0f &&
            imageDisplaySize.width > 0f && imageDisplaySize.height > 0f
}

private data class VideoCropLayout(
    val cropRect: Rect,
    val scale: Float,
    val offset: Offset,
)

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

@Composable
internal fun rememberVideoCropEditorState(): VideoCropEditorState = remember { VideoCropEditorState() }

@OptIn(UnstableApi::class)
@Composable
internal fun VideoCropViewport(
    player: ExoPlayer,
    sourceAspectRatio: Float,
    state: VideoCropEditorState,
    onManualCrop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val colors = TnyxTheme.colors
    val tokens = TnyxTheme.components.imageCropper

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val stageWidth = constraints.maxWidth.toFloat()
        val stageHeight = constraints.maxHeight.toFloat()
        val availableWidth = stageWidth * tokens.cropAreaFraction
        val availableHeight = stageHeight * tokens.cropAreaFraction
        val availableAspect = availableWidth / availableHeight
        val imageDisplayWidth = if (sourceAspectRatio >= availableAspect) {
            availableWidth
        } else {
            availableHeight * sourceAspectRatio
        }
        val imageDisplayHeight = if (sourceAspectRatio >= availableAspect) {
            availableWidth / sourceAspectRatio
        } else {
            availableHeight
        }

        LaunchedEffect(stageWidth, stageHeight, imageDisplayWidth, imageDisplayHeight) {
            state.updateGeometry(
                newStageSize = Size(stageWidth, stageHeight),
                newImageDisplaySize = Size(imageDisplayWidth, imageDisplayHeight),
                newCropAreaFraction = tokens.cropAreaFraction,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(stageWidth, stageHeight, imageDisplayWidth, imageDisplayHeight) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        state.transform(pan = pan, zoom = zoom)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                        this.player = player
                    }
                },
                update = { view -> view.player = player },
                onRelease = { view -> view.player = null },
                modifier = Modifier
                    .requiredSize(
                        width = with(density) { imageDisplayWidth.toDp() },
                        height = with(density) { imageDisplayHeight.toDp() },
                    )
                    .graphicsLayer {
                        scaleX = state.scale
                        scaleY = state.scale
                        translationX = state.offset.x
                        translationY = state.offset.y
                    },
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val crop = state.cropRect
                if (crop == Rect.Zero) return@Canvas
                val cropPath = Path().apply { addRect(crop) }
                clipPath(cropPath, clipOp = ClipOp.Difference) {
                    drawRect(colors.background.copy(alpha = tokens.overlayAlpha))
                }
                drawRect(
                    color = colors.textPrimary,
                    topLeft = crop.topLeft,
                    size = crop.size,
                    style = Stroke(width = tokens.frameStrokeWidth.toPx()),
                )
                val gridWidth = crop.width / 3f
                val gridHeight = crop.height / 3f
                val gridColor = colors.textPrimary.copy(alpha = tokens.gridAlpha)
                drawLine(gridColor, Offset(crop.left + gridWidth, crop.top), Offset(crop.left + gridWidth, crop.bottom), tokens.gridStrokeWidth.toPx())
                drawLine(gridColor, Offset(crop.left + gridWidth * 2f, crop.top), Offset(crop.left + gridWidth * 2f, crop.bottom), tokens.gridStrokeWidth.toPx())
                drawLine(gridColor, Offset(crop.left, crop.top + gridHeight), Offset(crop.right, crop.top + gridHeight), tokens.gridStrokeWidth.toPx())
                drawLine(gridColor, Offset(crop.left, crop.top + gridHeight * 2f), Offset(crop.right, crop.top + gridHeight * 2f), tokens.gridStrokeWidth.toPx())

                val handleLength = tokens.handleLength.toPx()
                val handleStroke = tokens.handleStrokeWidth.toPx()
                val frameColor = colors.textPrimary
                drawLine(frameColor, crop.topLeft, Offset(crop.left + handleLength, crop.top), handleStroke)
                drawLine(frameColor, crop.topLeft, Offset(crop.left, crop.top + handleLength), handleStroke)
                drawLine(frameColor, crop.topRight, Offset(crop.right - handleLength, crop.top), handleStroke)
                drawLine(frameColor, crop.topRight, Offset(crop.right, crop.top + handleLength), handleStroke)
                drawLine(frameColor, crop.bottomLeft, Offset(crop.left + handleLength, crop.bottom), handleStroke)
                drawLine(frameColor, crop.bottomLeft, Offset(crop.left, crop.bottom - handleLength), handleStroke)
                drawLine(frameColor, crop.bottomRight, Offset(crop.right - handleLength, crop.bottom), handleStroke)
                drawLine(frameColor, crop.bottomRight, Offset(crop.right, crop.bottom - handleLength), handleStroke)
            }

            VideoCropHandles(
                state = state,
                onManualCrop = onManualCrop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun VideoCropHandles(
    state: VideoCropEditorState,
    onManualCrop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val tokens = TnyxTheme.components.imageCropper
    val touchRadius = with(density) { tokens.touchRadius.toPx() }
    val minimumCropSize = with(density) { tokens.minimumCropSize.toPx() }

    Box(modifier = modifier) {
        VideoCropHandle.entries.forEach { handle ->
            val center = when (handle) {
                VideoCropHandle.TOP_LEFT -> state.cropRect.topLeft
                VideoCropHandle.TOP_RIGHT -> state.cropRect.topRight
                VideoCropHandle.BOTTOM_LEFT -> state.cropRect.bottomLeft
                VideoCropHandle.BOTTOM_RIGHT -> state.cropRect.bottomRight
            }
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (center.x - touchRadius).roundToInt(),
                            y = (center.y - touchRadius).roundToInt(),
                        )
                    }
                    .size(tokens.touchRadius * 2f)
                    .pointerInput(handle, state.stageSize, state.imageDisplaySize) {
                        detectDragGestures(
                            onDragEnd = {
                                coroutineScope.launch { state.settleCropToDefault() }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onManualCrop()
                                state.resize(handle, dragAmount, minimumCropSize)
                            },
                        )
                    },
            )
        }
    }
}
