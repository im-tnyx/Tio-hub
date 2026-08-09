package com.tnyx.features.workout.presentation.library.createexercise

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VideoTrimRangeTest {

    @Test
    fun `video at thirty seconds does not require trim confirmation`() {
        assertEquals(false, requiresVideoTrimConfirmation(30_000L))
    }

    @Test
    fun `video longer than thirty seconds requires trim confirmation`() {
        assertEquals(true, requiresVideoTrimConfirmation(30_001L))
    }

    @Test
    fun `end handle is clamped to thirty seconds while start stays fixed`() {
        val result = constrainVideoTrimRange(
            previousRange = 0f..30_000f,
            proposedRange = 0f..60_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(0f..30_000f, result)
    }

    @Test
    fun `start handle is clamped to thirty seconds while end stays fixed`() {
        val result = constrainVideoTrimRange(
            previousRange = 30_000f..60_000f,
            proposedRange = 0f..60_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(30_000f..60_000f, result)
    }

    @Test
    fun `range shorter than minimum keeps previous selection`() {
        val previous = 5_000f..10_000f

        val result = constrainVideoTrimRange(
            previousRange = previous,
            proposedRange = 9_500f..10_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(previous, result)
    }

    @Test
    fun `three second range is accepted`() {
        val proposed = 7_000f..10_000f

        val result = constrainVideoTrimRange(
            previousRange = 5_000f..10_000f,
            proposedRange = proposed,
            sourceDurationMs = 90_000f,
        )

        assertEquals(proposed, result)
    }

    @Test
    fun `range below three seconds keeps previous selection`() {
        val previous = 5_000f..10_000f

        val result = constrainVideoTrimRange(
            previousRange = previous,
            proposedRange = 7_001f..10_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(previous, result)
    }

    @Test
    fun `valid range remains unchanged`() {
        val proposed = 12_000f..28_000f

        val result = constrainVideoTrimRange(
            previousRange = 0f..30_000f,
            proposedRange = proposed,
            sourceDurationMs = 90_000f,
        )

        assertEquals(proposed, result)
    }

    @Test
    fun `start handle can enlarge a shortened clip up to thirty seconds`() {
        val result = constrainVideoTrimRange(
            previousRange = 20_000f..35_000f,
            proposedRange = 0f..35_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(5_000f..35_000f, result)
    }

    @Test
    fun `end handle can enlarge a shortened clip up to thirty seconds`() {
        val result = constrainVideoTrimRange(
            previousRange = 20_000f..35_000f,
            proposedRange = 20_000f..60_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(20_000f..50_000f, result)
    }

    @Test
    fun `trim range never extends outside source video`() {
        val result = constrainVideoTrimRange(
            previousRange = 5_000f..20_000f,
            proposedRange = -5_000f..20_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(0f..20_000f, result)
    }

    @Test
    fun `selected clip can move without changing duration`() {
        val result = shiftVideoTrimRange(
            selectedRange = 5_000f..20_000f,
            deltaMs = 12_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(17_000f..32_000f, result)
    }

    @Test
    fun `moving selected clip clamps at source start`() {
        val result = shiftVideoTrimRange(
            selectedRange = 5_000f..20_000f,
            deltaMs = -10_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(0f..15_000f, result)
    }

    @Test
    fun `moving selected clip clamps at source end`() {
        val result = shiftVideoTrimRange(
            selectedRange = 5_000f..20_000f,
            deltaMs = 100_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(75_000f..90_000f, result)
    }

    @Test
    fun `timeline focuses the selected range inside a short video`() {
        val viewport = focusedVideoTimelineViewport(
            selectedRange = 2_000f..12_000f,
            sourceDurationMs = 20_000f,
        )

        assertEquals(VideoTimelineViewport(2_000f, 12_000f), viewport)
    }

    @Test
    fun `maximum clip focuses timeline on selected range`() {
        val viewport = focusedVideoTimelineViewport(
            selectedRange = 30_000f..60_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(VideoTimelineViewport(30_000f, 60_000f), viewport)
    }

    @Test
    fun `shortened clip fills the focused timeline viewport`() {
        val viewport = focusedVideoTimelineViewport(
            selectedRange = 30_000f..45_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(VideoTimelineViewport(30_000f, 45_000f), viewport)
    }

    @Test
    fun `timeline focuses selected clip near video end`() {
        val viewport = focusedVideoTimelineViewport(
            selectedRange = 80_000f..90_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(VideoTimelineViewport(80_000f, 90_000f), viewport)
    }

    @Test
    fun `three second clip fills the focused timeline viewport`() {
        val viewport = focusedVideoTimelineViewport(
            selectedRange = 12_000f..15_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(VideoTimelineViewport(12_000f, 15_000f), viewport)
    }

    @Test
    fun `timeline reveals source while selection is enlarged outward`() {
        val viewport = expandVideoTimelineViewportToIncludeSelection(
            viewport = VideoTimelineViewport(30_000f, 45_000f),
            selectedRange = 20_000f..50_000f,
            sourceDurationMs = 90_000f,
        )

        assertEquals(VideoTimelineViewport(20_000f, 50_000f), viewport)
    }

    @Test
    fun `focused timeline places both handles at their normal inset`() {
        val selectedRange = 30_000f..45_000f
        val viewport = focusedVideoTimelineViewport(
            selectedRange = selectedRange,
            sourceDurationMs = 90_000f,
            handleInsetFraction = VIDEO_TIMELINE_HANDLE_INSET_FRACTION,
        )

        assertFloatEquals(
            VIDEO_TIMELINE_HANDLE_INSET_FRACTION,
            timelineFraction(selectedRange.start, viewport),
        )
        assertFloatEquals(
            1f - VIDEO_TIMELINE_HANDLE_INSET_FRACTION,
            timelineFraction(selectedRange.endInclusive, viewport),
        )
    }

    @Test
    fun `active end handle can reach outer edge while start handle stays fixed`() {
        val selectedRange = 20_000f..50_000f
        val viewport = videoTimelineViewportForSelection(
            selectedRange = selectedRange,
            selectionStartFraction = VIDEO_TIMELINE_HANDLE_INSET_FRACTION,
            selectionEndFraction = 1f,
        )

        assertFloatEquals(
            VIDEO_TIMELINE_HANDLE_INSET_FRACTION,
            timelineFraction(selectedRange.start, viewport),
        )
        assertFloatEquals(1f, timelineFraction(selectedRange.endInclusive, viewport))
    }

    @Test
    fun `scrolling timeline shifts viewport without changing zoom`() {
        val viewport = VideoTimelineViewport(10_000f, 30_000f)
        val shifted = shiftVideoTimelineViewport(viewport, 7_500f)

        assertEquals(VideoTimelineViewport(17_500f, 37_500f), shifted)
        assertEquals(viewport.durationMs, shifted.durationMs)
    }

    private fun timelineFraction(
        timeMs: Float,
        viewport: VideoTimelineViewport,
    ): Float = (timeMs - viewport.startMs) / viewport.durationMs

    private fun assertFloatEquals(expected: Float, actual: Float) {
        assertTrue(
            abs(expected - actual) < 0.0001f,
            "Expected $expected but was $actual",
        )
    }

}
