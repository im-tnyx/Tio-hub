package com.tnyx.features.workout.presentation.library.createexercise

import kotlin.test.Test
import kotlin.test.assertEquals

class VideoCropPresetTest {

    @Test
    fun `custom preset keeps current crop aspect ratio`() {
        assertEquals(9f / 16f, VideoCropPreset.CUSTOM.resolveAspectRatio(9f / 16f))
    }

    @Test
    fun `square preset resolves to square output`() {
        assertEquals(1f, VideoCropPreset.SQUARE.resolveAspectRatio(16f / 9f))
    }

    @Test
    fun `landscape preset resolves to sixteen by nine output`() {
        assertEquals(16f / 9f, VideoCropPreset.LANDSCAPE.resolveAspectRatio(9f / 16f))
    }

    @Test
    fun `square crop is centered within landscape source`() {
        assertEquals(
            NormalizedVideoCrop(left = 0.21875f, top = 0f, right = 0.78125f, bottom = 1f),
            centeredVideoCrop(sourceAspectRatio = 16f / 9f, targetAspectRatio = 1f),
        )
    }
}
