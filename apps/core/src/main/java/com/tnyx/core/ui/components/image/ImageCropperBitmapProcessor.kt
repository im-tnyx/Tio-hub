package com.tnyx.core.ui.components.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal fun processAndCropBitmap(
    source: Bitmap,
    stageSize: Size,
    imageDisplaySize: Size,
    totalRotation: Float,
    isFlipped: Boolean,
    scale: Float,
    offset: Offset,
    cropLeft: Float,
    cropTop: Float,
    cropRight: Float,
    cropBottom: Float,
): ByteArray? {
    return runCatching {
        if (stageSize.width <= 0f || stageSize.height <= 0f || imageDisplaySize.width <= 0f || imageDisplaySize.height <= 0f) {
            return@runCatching null
        }

        val renderScale = min(
            2f,
            max(
                1f,
                min(
                    source.width / imageDisplaySize.width.coerceAtLeast(1f),
                    source.height / imageDisplaySize.height.coerceAtLeast(1f),
                ),
            ),
        )
        val renderWidth = (stageSize.width * renderScale).roundToInt().coerceAtLeast(1)
        val renderHeight = (stageSize.height * renderScale).roundToInt().coerceAtLeast(1)
        val renderedBitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(renderedBitmap)
        val matrix = Matrix()
        matrix.postTranslate(-source.width / 2f, -source.height / 2f)
        if (isFlipped) {
            matrix.postScale(-1f, 1f)
        }
        matrix.postScale(
            imageDisplaySize.width * scale * renderScale / source.width.coerceAtLeast(1),
            imageDisplaySize.height * scale * renderScale / source.height.coerceAtLeast(1),
        )
        if (totalRotation != 0f) {
            matrix.postRotate(totalRotation)
        }
        matrix.postTranslate(
            (stageSize.width / 2f + offset.x) * renderScale,
            (stageSize.height / 2f + offset.y) * renderScale,
        )
        canvas.drawBitmap(source, matrix, null)

        val normalizedWidth = (cropRight - cropLeft).coerceIn(0.05f, 1f)
        val normalizedHeight = (cropBottom - cropTop).coerceIn(0.05f, 1f)
        val targetWidth = (renderWidth * normalizedWidth).roundToInt().coerceIn(1, renderWidth)
        val targetHeight = (renderHeight * normalizedHeight).roundToInt().coerceIn(1, renderHeight)
        val startX = (renderWidth * cropLeft).roundToInt().coerceIn(0, max(0, renderWidth - targetWidth))
        val startY = (renderHeight * cropTop).roundToInt().coerceIn(0, max(0, renderHeight - targetHeight))
        val croppedBitmap = Bitmap.createBitmap(
            renderedBitmap,
            startX,
            startY,
            min(targetWidth, renderWidth - startX),
            min(targetHeight, renderHeight - startY),
        )

        ByteArrayOutputStream().use { output ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            output.toByteArray()
        }
    }.getOrNull()
}
