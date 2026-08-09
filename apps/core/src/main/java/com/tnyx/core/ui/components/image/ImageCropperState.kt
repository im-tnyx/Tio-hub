package com.tnyx.core.ui.components.image

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Stable
internal class ImageCropperState {
    val scale = Animatable(1f)
    val offsetX = Animatable(0f)
    val offsetY = Animatable(0f)
    val cropLeft = Animatable(InitialCropLeft)
    val cropRight = Animatable(InitialCropRight)
    val cropTop = Animatable(InitialCropTop)
    val cropBottom = Animatable(InitialCropBottom)

    var defaultLeft by mutableFloatStateOf(InitialCropLeft)
        private set
    var defaultRight by mutableFloatStateOf(InitialCropRight)
        private set
    var defaultTop by mutableFloatStateOf(InitialCropTop)
        private set
    var defaultBottom by mutableFloatStateOf(InitialCropBottom)
        private set

    var activeEffectiveScale by mutableFloatStateOf(1f)
    var previewStageSize by mutableStateOf(Size.Zero)
    var previewImageDisplaySize by mutableStateOf(Size.Zero)

    suspend fun updateDefaultCrop(bounds: NormalizedCropBounds) {
        defaultLeft = bounds.left
        defaultRight = bounds.right
        defaultTop = bounds.top
        defaultBottom = bounds.bottom

        cropLeft.snapTo(defaultLeft)
        cropRight.snapTo(defaultRight)
        cropTop.snapTo(defaultTop)
        cropBottom.snapTo(defaultBottom)
    }

    suspend fun reset() = coroutineScope {
        val animationSpec = tween<Float>(
            durationMillis = ResetAnimationDurationMillis,
            easing = FastOutSlowInEasing,
        )
        launch { scale.animateTo(1f, animationSpec) }
        launch { offsetX.animateTo(0f, animationSpec) }
        launch { offsetY.animateTo(0f, animationSpec) }
        launch { cropLeft.animateTo(defaultLeft, animationSpec) }
        launch { cropRight.animateTo(defaultRight, animationSpec) }
        launch { cropTop.animateTo(defaultTop, animationSpec) }
        launch { cropBottom.animateTo(defaultBottom, animationSpec) }
    }

    private companion object {
        const val InitialCropLeft = 0.08f
        const val InitialCropRight = 0.92f
        const val InitialCropTop = 0.18f
        const val InitialCropBottom = 0.82f
        const val ResetAnimationDurationMillis = 300
    }
}

@Composable
internal fun rememberImageCropperState(): ImageCropperState = remember { ImageCropperState() }
