package com.tnyx.features.workout.presentation.library.createexercise

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals

class VideoPreviewTransformStateTest {

    @Test
    fun `preview cannot pan outside stage at default scale`() {
        val result = clampVideoPreviewOffset(
            proposed = Offset(100f, 100f),
            scale = 1f,
            stageSize = Size(400f, 800f),
            contentSize = Size(400f, 225f),
        )

        assertEquals(Offset.Zero, result)
    }

    @Test
    fun `zoomed preview pans only where scaled video covers stage`() {
        val result = clampVideoPreviewOffset(
            proposed = Offset(500f, 500f),
            scale = 2f,
            stageSize = Size(400f, 800f),
            contentSize = Size(400f, 225f),
        )

        assertEquals(Offset(200f, 0f), result)
    }
}
