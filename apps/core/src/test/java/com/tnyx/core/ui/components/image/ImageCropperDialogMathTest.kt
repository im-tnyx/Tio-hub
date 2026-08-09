package com.tnyx.core.ui.components.image

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageCropperDialogMathTest {

    @Test
    fun `portrait stage centers square using available width`() {
        val bounds = calculateCenteredSquareCropBounds(
            stageSize = Size(1000f, 1600f),
            cropAreaFraction = 0.8f,
        )!!

        assertEquals(0.1f, bounds.left, 0.001f)
        assertEquals(0.25f, bounds.top, 0.001f)
        assertEquals(0.9f, bounds.right, 0.001f)
        assertEquals(0.75f, bounds.bottom, 0.001f)
    }

    @Test
    fun `landscape stage centers square using available height`() {
        val bounds = calculateCenteredSquareCropBounds(
            stageSize = Size(1600f, 1000f),
            cropAreaFraction = 0.8f,
        )!!

        assertEquals(0.25f, bounds.left, 0.001f)
        assertEquals(0.1f, bounds.top, 0.001f)
        assertEquals(0.75f, bounds.right, 0.001f)
        assertEquals(0.9f, bounds.bottom, 0.001f)
    }

    @Test
    fun `crop fraction is clamped to stage bounds`() {
        val bounds = calculateCenteredSquareCropBounds(
            stageSize = Size(1000f, 1600f),
            cropAreaFraction = 2f,
        )!!

        assertEquals(0f, bounds.left, 0.001f)
        assertEquals(1f, bounds.right, 0.001f)
        assertTrue(bounds.top >= 0f)
        assertTrue(bounds.bottom <= 1f)
    }

    @Test
    fun `invalid stage cannot produce crop bounds`() {
        assertNull(
            calculateCenteredSquareCropBounds(
                stageSize = Size.Zero,
                cropAreaFraction = 0.8f,
            )
        )
    }

    @Test
    fun `rotation fit scale grows to cover square crop at 45 degrees`() {
        val resolution = resolveCropperTransform(
            stageSize = Size(1000f, 1000f),
            cropRect = Rect(200f, 200f, 800f, 800f),
            imageDisplaySize = Size(600f, 600f),
            requestedScale = 1f,
            proposedOffset = Offset.Zero,
            rotationDegrees = 45f,
        )

        assertEquals(1.414f, resolution.minimumScale, 0.02f)
        assertTrue(
            isCropRectCoveredByImage(
                stageSize = Size(1000f, 1000f),
                cropRect = Rect(200f, 200f, 800f, 800f),
                imageDisplaySize = Size(600f, 600f),
                scale = resolution.effectiveScale,
                offset = resolution.clampedOffset,
                rotationDegrees = 45f,
            )
        )
    }

    @Test
    fun `pan is clamped so rotated image still covers crop rect`() {
        val resolution = resolveCropperTransform(
            stageSize = Size(1000f, 1200f),
            cropRect = Rect(180f, 300f, 820f, 940f),
            imageDisplaySize = Size(640f, 900f),
            requestedScale = 1.7f,
            proposedOffset = Offset(420f, -360f),
            rotationDegrees = 32f,
        )

        assertTrue(
            isCropRectCoveredByImage(
                stageSize = Size(1000f, 1200f),
                cropRect = Rect(180f, 300f, 820f, 940f),
                imageDisplaySize = Size(640f, 900f),
                scale = resolution.effectiveScale,
                offset = resolution.clampedOffset,
                rotationDegrees = 32f,
            )
        )
    }
}
