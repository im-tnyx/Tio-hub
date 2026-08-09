package com.tnyx.features.workout.presentation.library.createexercise

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.max

private const val MIN_VIDEO_PREVIEW_SCALE = 1f
private const val MAX_VIDEO_PREVIEW_SCALE = 4f

@Stable
internal class VideoPreviewTransformState {
    var scale by mutableFloatStateOf(MIN_VIDEO_PREVIEW_SCALE)
        private set

    var offset by mutableStateOf(Offset.Zero)
        private set

    private var stageSize = Size.Zero
    private var contentSize = Size.Zero

    fun updateLayout(stageSize: Size, contentSize: Size) {
        this.stageSize = stageSize
        this.contentSize = contentSize
        offset = clampVideoPreviewOffset(offset, scale, stageSize, contentSize)
    }

    fun transform(centroid: Offset, pan: Offset, zoom: Float) {
        if (stageSize == Size.Zero || contentSize == Size.Zero) return

        val previousScale = scale
        val nextScale = (previousScale * zoom).coerceIn(
            MIN_VIDEO_PREVIEW_SCALE,
            MAX_VIDEO_PREVIEW_SCALE,
        )
        val scaleChange = nextScale / previousScale
        val stageCenter = Offset(stageSize.width / 2f, stageSize.height / 2f)
        val centroidFromCenter = centroid - stageCenter
        val zoomedOffset = Offset(
            x = centroidFromCenter.x * (1f - scaleChange) + offset.x * scaleChange,
            y = centroidFromCenter.y * (1f - scaleChange) + offset.y * scaleChange,
        )

        scale = nextScale
        offset = clampVideoPreviewOffset(
            proposed = zoomedOffset + pan,
            scale = nextScale,
            stageSize = stageSize,
            contentSize = contentSize,
        )
    }
}

@Composable
internal fun rememberVideoPreviewTransformState(): VideoPreviewTransformState =
    remember { VideoPreviewTransformState() }

internal fun clampVideoPreviewOffset(
    proposed: Offset,
    scale: Float,
    stageSize: Size,
    contentSize: Size,
): Offset {
    val maxHorizontalOffset = max(0f, (contentSize.width * scale - stageSize.width) / 2f)
    val maxVerticalOffset = max(0f, (contentSize.height * scale - stageSize.height) / 2f)
    return Offset(
        x = proposed.x.coerceIn(-maxHorizontalOffset, maxHorizontalOffset),
        y = proposed.y.coerceIn(-maxVerticalOffset, maxVerticalOffset),
    )
}
