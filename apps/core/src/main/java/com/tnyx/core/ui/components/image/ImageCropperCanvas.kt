package com.tnyx.core.ui.components.image

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import com.tnyx.core.theme.TnyxTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
internal fun ImageCropperCanvas(
    bitmap: Bitmap?,
    totalRotation: Float,
    isFlippedHorizontal: Boolean,
    state: ImageCropperState,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val colors = TnyxTheme.colors
    val tokens = TnyxTheme.components.imageCropper
    val overlayColor = colors.background.copy(alpha = tokens.overlayAlpha)
    val frameColor = colors.textPrimary
    val gridColor = frameColor.copy(alpha = tokens.gridAlpha)

    BoxWithConstraints(
        modifier = modifier.padding(top = tokens.canvasTopPadding),
        contentAlignment = Alignment.Center,
    ) {
        val stageWidth = constraints.maxWidth.toFloat()
        val stageHeight = constraints.maxHeight.toFloat()
        val squareSide = min(stageWidth, stageHeight) * tokens.cropAreaFraction
        val defaultCropBounds = calculateCenteredSquareCropBounds(
            stageSize = Size(stageWidth, stageHeight),
            cropAreaFraction = tokens.cropAreaFraction,
        )

        LaunchedEffect(defaultCropBounds) {
            defaultCropBounds?.let { state.updateDefaultCrop(it) }
        }

        val currentBitmap = bitmap ?: return@BoxWithConstraints
        val bitmapWidth = currentBitmap.width.toFloat()
        val bitmapHeight = currentBitmap.height.toFloat()
        val (imageDisplayWidth, imageDisplayHeight) = if (bitmapWidth <= bitmapHeight) {
            squareSide to squareSide * (bitmapHeight / bitmapWidth)
        } else {
            squareSide * (bitmapWidth / bitmapHeight) to squareSide
        }

        val rotationRadians = Math.toRadians(totalRotation.toDouble())
        val rotationFitScale = squareSide * (
            abs(cos(rotationRadians)).toFloat() + abs(sin(rotationRadians)).toFloat()
            ) / min(imageDisplayWidth, imageDisplayHeight).coerceAtLeast(1f)
        val cropRect = cropRectFromNormalized(
            stageWidth = stageWidth,
            stageHeight = stageHeight,
            left = state.cropLeft.value,
            top = state.cropTop.value,
            right = state.cropRight.value,
            bottom = state.cropBottom.value,
        )
        val currentTransform = resolveCropperTransform(
            stageSize = Size(stageWidth, stageHeight),
            cropRect = cropRect,
            imageDisplaySize = Size(imageDisplayWidth, imageDisplayHeight),
            requestedScale = max(state.scale.value, rotationFitScale),
            proposedOffset = Offset(state.offsetX.value, state.offsetY.value),
            rotationDegrees = totalRotation,
        )

        LaunchedEffect(
            stageWidth,
            stageHeight,
            imageDisplayWidth,
            imageDisplayHeight,
            currentTransform.effectiveScale,
        ) {
            state.previewStageSize = Size(stageWidth, stageHeight)
            state.previewImageDisplaySize = Size(imageDisplayWidth, imageDisplayHeight)
            state.activeEffectiveScale = currentTransform.effectiveScale
        }

        LaunchedEffect(
            totalRotation,
            cropRect,
            imageDisplayWidth,
            imageDisplayHeight,
            state.scale.value,
        ) {
            val resolved = resolveCropperTransform(
                stageSize = Size(stageWidth, stageHeight),
                cropRect = cropRect,
                imageDisplaySize = Size(imageDisplayWidth, imageDisplayHeight),
                requestedScale = max(state.scale.value, rotationFitScale),
                proposedOffset = Offset(state.offsetX.value, state.offsetY.value),
                rotationDegrees = totalRotation,
            )
            state.activeEffectiveScale = resolved.effectiveScale
            state.offsetX.snapTo(resolved.clampedOffset.x)
            state.offsetY.snapTo(resolved.clampedOffset.y)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(stageWidth, stageHeight, imageDisplayWidth, imageDisplayHeight, totalRotation) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        coroutineScope.launch {
                            val requestedScale = (state.scale.value * zoom).coerceIn(1f, 6f)
                            val resolved = resolveCropperTransform(
                                stageSize = Size(stageWidth, stageHeight),
                                cropRect = cropRect,
                                imageDisplaySize = Size(imageDisplayWidth, imageDisplayHeight),
                                requestedScale = max(requestedScale, rotationFitScale),
                                proposedOffset = Offset(
                                    x = state.offsetX.value + pan.x,
                                    y = state.offsetY.value + pan.y,
                                ),
                                rotationDegrees = totalRotation,
                            )
                            state.scale.snapTo(requestedScale)
                            state.activeEffectiveScale = resolved.effectiveScale
                            state.offsetX.snapTo(resolved.clampedOffset.x)
                            state.offsetY.snapTo(resolved.clampedOffset.y)
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = "Crop image",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .requiredSize(
                        width = with(density) { imageDisplayWidth.toDp() },
                        height = with(density) { imageDisplayHeight.toDp() },
                    )
                    .graphicsLayer {
                        scaleX = currentTransform.effectiveScale * if (isFlippedHorizontal) -1f else 1f
                        scaleY = currentTransform.effectiveScale
                        translationX = state.offsetX.value
                        translationY = state.offsetY.value
                        rotationZ = totalRotation
                    },
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val left = state.cropLeft.value * size.width
                val right = state.cropRight.value * size.width
                val top = state.cropTop.value * size.height
                val bottom = state.cropBottom.value * size.height
                val cropPath = Path().apply { addRect(Rect(left, top, right, bottom)) }

                clipPath(cropPath, clipOp = ClipOp.Difference) {
                    drawRect(overlayColor)
                }
                drawRect(
                    color = frameColor,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = tokens.frameStrokeWidth.toPx()),
                )

                val gridWidth = (right - left) / 3f
                val gridHeight = (bottom - top) / 3f
                val gridStroke = tokens.gridStrokeWidth.toPx()
                drawLine(gridColor, Offset(left + gridWidth, top), Offset(left + gridWidth, bottom), gridStroke)
                drawLine(gridColor, Offset(left + gridWidth * 2f, top), Offset(left + gridWidth * 2f, bottom), gridStroke)
                drawLine(gridColor, Offset(left, top + gridHeight), Offset(right, top + gridHeight), gridStroke)
                drawLine(gridColor, Offset(left, top + gridHeight * 2f), Offset(right, top + gridHeight * 2f), gridStroke)

                val handleLength = tokens.handleLength.toPx()
                val handleStroke = tokens.handleStrokeWidth.toPx()
                drawLine(frameColor, Offset(left, top), Offset(left + handleLength, top), handleStroke)
                drawLine(frameColor, Offset(left, top), Offset(left, top + handleLength), handleStroke)
                drawLine(frameColor, Offset(right, top), Offset(right - handleLength, top), handleStroke)
                drawLine(frameColor, Offset(right, top), Offset(right, top + handleLength), handleStroke)
                drawLine(frameColor, Offset(left, bottom), Offset(left + handleLength, bottom), handleStroke)
                drawLine(frameColor, Offset(left, bottom), Offset(left, bottom - handleLength), handleStroke)
                drawLine(frameColor, Offset(right, bottom), Offset(right - handleLength, bottom), handleStroke)
                drawLine(frameColor, Offset(right, bottom), Offset(right, bottom - handleLength), handleStroke)
            }

            ImageCropperHandles(
                state = state,
                stageSize = Size(stageWidth, stageHeight),
                imageDisplaySize = Size(imageDisplayWidth, imageDisplayHeight),
                totalRotation = totalRotation,
                rotationFitScale = rotationFitScale,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
