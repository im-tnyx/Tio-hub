package com.tnyx.core.ui.components.image

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.tnyx.core.theme.TnyxTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private enum class CropHandle {
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight,
}

@Composable
internal fun ImageCropperHandles(
    state: ImageCropperState,
    stageSize: Size,
    imageDisplaySize: Size,
    totalRotation: Float,
    rotationFitScale: Float,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val tokens = TnyxTheme.components.imageCropper
    val touchRadius = with(density) { tokens.touchRadius.toPx() }
    val minimumCropSize = with(density) { tokens.minimumCropSize.toPx() }

    Box(modifier = modifier.fillMaxSize()) {
        CropHandle.entries.forEach { handle ->
            val center = handle.center(
                left = state.cropLeft.value * stageSize.width,
                top = state.cropTop.value * stageSize.height,
                right = state.cropRight.value * stageSize.width,
                bottom = state.cropBottom.value * stageSize.height,
            )

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (center.x - touchRadius).roundToInt(),
                            y = (center.y - touchRadius).roundToInt(),
                        )
                    }
                    .size(tokens.touchRadius * 2f)
                    .pointerInput(handle, stageSize, imageDisplaySize, totalRotation) {
                        detectDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    settleCropHandle(
                                        state = state,
                                        stageSize = stageSize,
                                        imageDisplaySize = imageDisplaySize,
                                        totalRotation = totalRotation,
                                        rotationFitScale = rotationFitScale,
                                    )
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    resizeCropFromHandle(
                                        handle = handle,
                                        dragAmount = dragAmount,
                                        state = state,
                                        stageSize = stageSize,
                                        minimumCropSize = minimumCropSize,
                                    )
                                }
                            },
                        )
                    },
            )
        }
    }
}

private fun CropHandle.center(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
): Offset = when (this) {
    CropHandle.TopLeft -> Offset(left, top)
    CropHandle.TopRight -> Offset(right, top)
    CropHandle.BottomLeft -> Offset(left, bottom)
    CropHandle.BottomRight -> Offset(right, bottom)
}

private suspend fun resizeCropFromHandle(
    handle: CropHandle,
    dragAmount: Offset,
    state: ImageCropperState,
    stageSize: Size,
    minimumCropSize: Float,
) {
    val currentWidth = (state.cropRight.value - state.cropLeft.value) * stageSize.width
    val currentHeight = (state.cropBottom.value - state.cropTop.value) * stageSize.height
    val currentSize = min(currentWidth, currentHeight)
    val sizeDelta = when (handle) {
        CropHandle.TopLeft -> -((dragAmount.x + dragAmount.y) / 2f)
        CropHandle.TopRight -> (dragAmount.x - dragAmount.y) / 2f
        CropHandle.BottomLeft -> (-dragAmount.x + dragAmount.y) / 2f
        CropHandle.BottomRight -> (dragAmount.x + dragAmount.y) / 2f
    }
    val newSize = (currentSize + sizeDelta).coerceIn(
        minimumCropSize,
        min(stageSize.width, stageSize.height) * MaximumCropAreaFraction,
    )
    val normalizedWidth = newSize / stageSize.width
    val normalizedHeight = newSize / stageSize.height
    val minimumWidth = minimumCropSize / stageSize.width
    val minimumHeight = minimumCropSize / stageSize.height

    when (handle) {
        CropHandle.TopLeft -> {
            state.cropLeft.snapTo(
                (state.cropRight.value - normalizedWidth).coerceIn(
                    CropEdgeInsetFraction,
                    state.cropRight.value - minimumWidth,
                ),
            )
            state.cropTop.snapTo(
                (state.cropBottom.value - normalizedHeight).coerceIn(
                    CropEdgeInsetFraction,
                    state.cropBottom.value - minimumHeight,
                ),
            )
        }

        CropHandle.TopRight -> {
            state.cropRight.snapTo(
                (state.cropLeft.value + normalizedWidth).coerceIn(
                    state.cropLeft.value + minimumWidth,
                    1f - CropEdgeInsetFraction,
                ),
            )
            state.cropTop.snapTo(
                (state.cropBottom.value - normalizedHeight).coerceIn(
                    CropEdgeInsetFraction,
                    state.cropBottom.value - minimumHeight,
                ),
            )
        }

        CropHandle.BottomLeft -> {
            state.cropLeft.snapTo(
                (state.cropRight.value - normalizedWidth).coerceIn(
                    CropEdgeInsetFraction,
                    state.cropRight.value - minimumWidth,
                ),
            )
            state.cropBottom.snapTo(
                (state.cropTop.value + normalizedHeight).coerceIn(
                    state.cropTop.value + minimumHeight,
                    1f - CropEdgeInsetFraction,
                ),
            )
        }

        CropHandle.BottomRight -> {
            state.cropRight.snapTo(
                (state.cropLeft.value + normalizedWidth).coerceIn(
                    state.cropLeft.value + minimumWidth,
                    1f - CropEdgeInsetFraction,
                ),
            )
            state.cropBottom.snapTo(
                (state.cropTop.value + normalizedHeight).coerceIn(
                    state.cropTop.value + minimumHeight,
                    1f - CropEdgeInsetFraction,
                ),
            )
        }
    }
}

private suspend fun settleCropHandle(
    state: ImageCropperState,
    stageSize: Size,
    imageDisplaySize: Size,
    totalRotation: Float,
    rotationFitScale: Float,
) = coroutineScope {
    val currentWidth = (state.cropRight.value - state.cropLeft.value).coerceAtLeast(MinimumCropFraction)
    val currentHeight = (state.cropBottom.value - state.cropTop.value).coerceAtLeast(MinimumCropFraction)
    val defaultWidth = (state.defaultRight - state.defaultLeft).coerceAtLeast(MinimumCropFraction)
    val defaultHeight = (state.defaultBottom - state.defaultTop).coerceAtLeast(MinimumCropFraction)
    val zoomFactor = min(defaultWidth / currentWidth, defaultHeight / currentHeight)
    val selectedCenterX = (state.cropLeft.value + state.cropRight.value) / 2f
    val selectedCenterY = (state.cropTop.value + state.cropBottom.value) / 2f
    val stageCenterX = (state.defaultLeft + state.defaultRight) / 2f
    val stageCenterY = (state.defaultTop + state.defaultBottom) / 2f
    val targetScale = (state.scale.value * zoomFactor).coerceIn(1f, MaximumScale)
    val resolved = resolveCropperTransform(
        stageSize = stageSize,
        cropRect = cropRectFromNormalized(
            stageWidth = stageSize.width,
            stageHeight = stageSize.height,
            left = state.defaultLeft,
            top = state.defaultTop,
            right = state.defaultRight,
            bottom = state.defaultBottom,
        ),
        imageDisplaySize = imageDisplaySize,
        requestedScale = max(targetScale, rotationFitScale),
        proposedOffset = Offset(
            x = state.offsetX.value +
                (stageCenterX - selectedCenterX) * stageSize.width * targetScale,
            y = state.offsetY.value +
                (stageCenterY - selectedCenterY) * stageSize.height * targetScale,
        ),
        rotationDegrees = totalRotation,
    )
    val animationSpec = tween<Float>(
        durationMillis = HandleSettleAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )

    launch { state.scale.animateTo(targetScale, animationSpec) }
    launch { state.offsetX.animateTo(resolved.clampedOffset.x, animationSpec) }
    launch { state.offsetY.animateTo(resolved.clampedOffset.y, animationSpec) }
    launch { state.cropLeft.animateTo(state.defaultLeft, animationSpec) }
    launch { state.cropRight.animateTo(state.defaultRight, animationSpec) }
    launch { state.cropTop.animateTo(state.defaultTop, animationSpec) }
    launch { state.cropBottom.animateTo(state.defaultBottom, animationSpec) }
}

private const val MaximumScale = 6f
private const val MaximumCropAreaFraction = 0.85f
private const val CropEdgeInsetFraction = 0.02f
private const val MinimumCropFraction = 0.05f
private const val HandleSettleAnimationDurationMillis = 320
