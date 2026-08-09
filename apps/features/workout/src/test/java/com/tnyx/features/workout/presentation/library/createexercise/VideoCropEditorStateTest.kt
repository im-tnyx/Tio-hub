package com.tnyx.features.workout.presentation.library.createexercise

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VideoCropEditorStateTest {

    @Test
    fun `full selection maps to complete source frame`() {
        val state = VideoCropEditorState()
        state.showSelection(NormalizedVideoCrop.Full)
        state.updateGeometry(
            newStageSize = Size(1_000f, 1_000f),
            newImageDisplaySize = Size(800f, 600f),
            newCropAreaFraction = 0.82f,
        )

        assertEquals(NormalizedVideoCrop.Full, state.currentSelection())
    }

    @Test
    fun `zoom and pan remain inside source frame`() {
        val state = VideoCropEditorState()
        state.showSelection(centeredVideoCrop(16f / 9f, 1f))
        state.updateGeometry(
            newStageSize = Size(1_000f, 1_000f),
            newImageDisplaySize = Size(800f, 450f),
            newCropAreaFraction = 0.82f,
        )

        state.transform(pan = Offset(5_000f, 5_000f), zoom = 2f)
        val selection = state.currentSelection()

        assertTrue(selection.left in 0f..1f)
        assertTrue(selection.top in 0f..1f)
        assertTrue(selection.right in 0f..1f)
        assertTrue(selection.bottom in 0f..1f)
    }

    @Test
    fun `corner drag creates a smaller custom crop`() {
        val state = VideoCropEditorState()
        state.showSelection(NormalizedVideoCrop.Full)
        state.updateGeometry(
            newStageSize = Size(1_000f, 1_000f),
            newImageDisplaySize = Size(800f, 600f),
            newCropAreaFraction = 0.82f,
        )

        state.resize(
            handle = VideoCropHandle.BOTTOM_RIGHT,
            dragAmount = Offset(-200f, -150f),
            minimumCropSize = 60f,
        )
        val selection = state.currentSelection()

        assertTrue(selection.right < 1f)
        assertTrue(selection.bottom < 1f)
    }
}
