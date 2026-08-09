package com.tnyx.core.ui.components.image

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

internal data class CropperTransformResolution(
    val minimumScale: Float,
    val effectiveScale: Float,
    val clampedOffset: Offset,
)

internal data class NormalizedCropBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

internal fun calculateCenteredSquareCropBounds(
    stageSize: Size,
    cropAreaFraction: Float,
): NormalizedCropBounds? {
    if (stageSize.width <= 0f || stageSize.height <= 0f) return null

    val squareSide = min(stageSize.width, stageSize.height) * cropAreaFraction.coerceIn(0f, 1f)
    val normalizedWidth = squareSide / stageSize.width
    val normalizedHeight = squareSide / stageSize.height
    val left = (1f - normalizedWidth) / 2f
    val top = (1f - normalizedHeight) / 2f
    return NormalizedCropBounds(
        left = left,
        top = top,
        right = 1f - left,
        bottom = 1f - top,
    )
}

internal fun cropRectFromNormalized(
    stageWidth: Float,
    stageHeight: Float,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
): Rect = Rect(
    left = left * stageWidth,
    top = top * stageHeight,
    right = right * stageWidth,
    bottom = bottom * stageHeight,
)

internal fun resolveCropperTransform(
    stageSize: Size,
    cropRect: Rect,
    imageDisplaySize: Size,
    requestedScale: Float,
    proposedOffset: Offset,
    rotationDegrees: Float,
): CropperTransformResolution {
    if (stageSize.width <= 0f || stageSize.height <= 0f || imageDisplaySize.width <= 0f || imageDisplaySize.height <= 0f) {
        return CropperTransformResolution(
            minimumScale = 1f,
            effectiveScale = requestedScale.coerceAtLeast(1f),
            clampedOffset = Offset.Zero,
        )
    }

    val radians = Math.toRadians(rotationDegrees.toDouble())
    val cosTheta = cos(radians).toFloat()
    val sinTheta = sin(radians).toFloat()
    val stageCenter = Offset(stageSize.width / 2f, stageSize.height / 2f)
    val cropCorners = listOf(
        Offset(cropRect.left, cropRect.top),
        Offset(cropRect.right, cropRect.top),
        Offset(cropRect.left, cropRect.bottom),
        Offset(cropRect.right, cropRect.bottom),
    ).map { point ->
        Offset(point.x - stageCenter.x, point.y - stageCenter.y)
    }

    val axisXValues = cropCorners.map { point -> cosTheta * point.x + sinTheta * point.y }
    val axisYValues = cropCorners.map { point -> -sinTheta * point.x + cosTheta * point.y }

    val requiredHalfWidth = ((axisXValues.maxOrNull() ?: 0f) - (axisXValues.minOrNull() ?: 0f)) / 2f
    val requiredHalfHeight = ((axisYValues.maxOrNull() ?: 0f) - (axisYValues.minOrNull() ?: 0f)) / 2f
    val minimumScale = max(
        1f,
        max(
            (requiredHalfWidth * 2f) / imageDisplaySize.width.coerceAtLeast(1f),
            (requiredHalfHeight * 2f) / imageDisplaySize.height.coerceAtLeast(1f),
        ),
    )
    val effectiveScale = max(requestedScale, minimumScale)
    val halfImageWidth = imageDisplaySize.width * effectiveScale / 2f
    val halfImageHeight = imageDisplaySize.height * effectiveScale / 2f

    val uMinRaw = (axisXValues.maxOrNull() ?: 0f) - halfImageWidth
    val uMaxRaw = (axisXValues.minOrNull() ?: 0f) + halfImageWidth
    val vMinRaw = (axisYValues.maxOrNull() ?: 0f) - halfImageHeight
    val vMaxRaw = (axisYValues.minOrNull() ?: 0f) + halfImageHeight

    val uMid = (uMinRaw + uMaxRaw) / 2f
    val vMid = (vMinRaw + vMaxRaw) / 2f
    val uMin = min(uMinRaw, uMaxRaw)
    val uMax = max(uMinRaw, uMaxRaw)
    val vMin = min(vMinRaw, vMaxRaw)
    val vMax = max(vMinRaw, vMaxRaw)

    val proposedU = cosTheta * proposedOffset.x + sinTheta * proposedOffset.y
    val proposedV = -sinTheta * proposedOffset.x + cosTheta * proposedOffset.y
    val clampedU = if (uMin == uMax) uMid else proposedU.coerceIn(uMin, uMax)
    val clampedV = if (vMin == vMax) vMid else proposedV.coerceIn(vMin, vMax)

    return CropperTransformResolution(
        minimumScale = minimumScale,
        effectiveScale = effectiveScale,
        clampedOffset = Offset(
            x = cosTheta * clampedU - sinTheta * clampedV,
            y = sinTheta * clampedU + cosTheta * clampedV,
        ),
    )
}

internal fun isCropRectCoveredByImage(
    stageSize: Size,
    cropRect: Rect,
    imageDisplaySize: Size,
    scale: Float,
    offset: Offset,
    rotationDegrees: Float,
): Boolean {
    if (stageSize.width <= 0f || stageSize.height <= 0f || imageDisplaySize.width <= 0f || imageDisplaySize.height <= 0f) {
        return false
    }

    val radians = Math.toRadians(rotationDegrees.toDouble())
    val cosTheta = cos(radians).toFloat()
    val sinTheta = sin(radians).toFloat()
    val stageCenter = Offset(stageSize.width / 2f, stageSize.height / 2f)
    val halfImageWidth = imageDisplaySize.width * scale / 2f
    val halfImageHeight = imageDisplaySize.height * scale / 2f

    return listOf(
        Offset(cropRect.left, cropRect.top),
        Offset(cropRect.right, cropRect.top),
        Offset(cropRect.left, cropRect.bottom),
        Offset(cropRect.right, cropRect.bottom),
    ).all { point ->
        val dx = point.x - stageCenter.x - offset.x
        val dy = point.y - stageCenter.y - offset.y
        val localX = cosTheta * dx + sinTheta * dy
        val localY = -sinTheta * dx + cosTheta * dy
        abs(localX) <= halfImageWidth + 0.5f && abs(localY) <= halfImageHeight + 0.5f
    }
}
