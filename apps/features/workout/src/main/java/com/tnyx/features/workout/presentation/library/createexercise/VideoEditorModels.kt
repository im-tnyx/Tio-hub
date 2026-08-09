package com.tnyx.features.workout.presentation.library.createexercise

import kotlin.math.abs
import kotlin.math.min

internal const val MIN_VIDEO_TRIM_DURATION_MS = 3_000L
internal const val MAX_VIDEO_TRIM_DURATION_MS = 30_000L
internal const val VIDEO_TIMELINE_HANDLE_INSET_FRACTION = 0.08f

internal enum class VideoEditorMode {
    EDITOR,
    TRIM,
    CROP,
}

internal fun requiresVideoTrimConfirmation(durationMs: Long): Boolean =
    durationMs > MAX_VIDEO_TRIM_DURATION_MS

internal enum class VideoCropPreset(
    val label: String,
    val aspectRatio: Float?,
) {
    CUSTOM("Custom", null),
    SQUARE("Square", 1f),
    LANDSCAPE("16:9", 16f / 9f),
    CLASSIC("4:3", 4f / 3f),
    PORTRAIT("9:16", 9f / 16f),
}

internal data class VideoTrimMetadata(
    val durationMs: Long,
    val sourceAspectRatio: Float,
    val frames: List<android.graphics.Bitmap>,
)

internal data class VideoTimelineViewport(
    val startMs: Float,
    val endMs: Float,
) {
    val durationMs: Float
        get() = (endMs - startMs).coerceAtLeast(0f)
}

internal data class NormalizedVideoCrop(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    companion object {
        val Full = NormalizedVideoCrop(0f, 0f, 1f, 1f)
    }
}

internal fun VideoCropPreset.resolveAspectRatio(
    sourceAspectRatio: Float,
    currentCrop: NormalizedVideoCrop = NormalizedVideoCrop.Full,
): Float = aspectRatio ?: (
    sourceAspectRatio * (currentCrop.right - currentCrop.left) /
        (currentCrop.bottom - currentCrop.top).coerceAtLeast(Float.MIN_VALUE)
    )

internal fun centeredVideoCrop(
    sourceAspectRatio: Float,
    targetAspectRatio: Float?,
): NormalizedVideoCrop {
    if (targetAspectRatio == null || sourceAspectRatio <= 0f || targetAspectRatio <= 0f) {
        return NormalizedVideoCrop.Full
    }
    return if (targetAspectRatio < sourceAspectRatio) {
        val visibleWidth = (targetAspectRatio / sourceAspectRatio).coerceIn(0f, 1f)
        val horizontalInset = (1f - visibleWidth) / 2f
        NormalizedVideoCrop(horizontalInset, 0f, 1f - horizontalInset, 1f)
    } else {
        val visibleHeight = (sourceAspectRatio / targetAspectRatio).coerceIn(0f, 1f)
        val verticalInset = (1f - visibleHeight) / 2f
        NormalizedVideoCrop(0f, verticalInset, 1f, 1f - verticalInset)
    }
}

internal fun constrainVideoTrimRange(
    previousRange: ClosedFloatingPointRange<Float>,
    proposedRange: ClosedFloatingPointRange<Float>,
    sourceDurationMs: Float,
): ClosedFloatingPointRange<Float> {
    val safeSourceDuration = sourceDurationMs.coerceAtLeast(0f)
    val proposedStart = proposedRange.start.coerceIn(0f, safeSourceDuration)
    val proposedEnd = proposedRange.endInclusive.coerceIn(0f, safeSourceDuration)
    if (proposedEnd < proposedStart) return previousRange

    val boundedRange = proposedStart..proposedEnd
    val proposedDuration = proposedEnd - proposedStart
    if (proposedDuration < MIN_VIDEO_TRIM_DURATION_MS.toFloat()) return previousRange
    if (proposedDuration <= MAX_VIDEO_TRIM_DURATION_MS.toFloat()) return boundedRange

    val startMovedMore = abs(proposedRange.start - previousRange.start) >
        abs(proposedRange.endInclusive - previousRange.endInclusive)
    return if (startMovedMore) {
        (proposedEnd - MAX_VIDEO_TRIM_DURATION_MS).coerceAtLeast(0f)..proposedEnd
    } else {
        proposedStart..min(
            proposedStart + MAX_VIDEO_TRIM_DURATION_MS,
            safeSourceDuration,
        )
    }
}

internal fun expandVideoTimelineViewportToIncludeSelection(
    viewport: VideoTimelineViewport,
    selectedRange: ClosedFloatingPointRange<Float>,
    sourceDurationMs: Float,
): VideoTimelineViewport {
    val safeSourceDuration = sourceDurationMs.coerceAtLeast(0f)
    return VideoTimelineViewport(
        startMs = min(viewport.startMs, selectedRange.start).coerceIn(0f, safeSourceDuration),
        endMs = maxOf(viewport.endMs, selectedRange.endInclusive)
            .coerceIn(0f, safeSourceDuration),
    )
}

internal fun shiftVideoTrimRange(
    selectedRange: ClosedFloatingPointRange<Float>,
    deltaMs: Float,
    sourceDurationMs: Float,
): ClosedFloatingPointRange<Float> {
    val safeSourceDuration = sourceDurationMs.coerceAtLeast(0f)
    val clipDuration = (selectedRange.endInclusive - selectedRange.start)
        .coerceIn(0f, safeSourceDuration)
    val shiftedStart = (selectedRange.start + deltaMs)
        .coerceIn(0f, safeSourceDuration - clipDuration)
    return shiftedStart..(shiftedStart + clipDuration)
}

internal fun focusedVideoTimelineViewport(
    selectedRange: ClosedFloatingPointRange<Float>,
    sourceDurationMs: Float,
    handleInsetFraction: Float = 0f,
): VideoTimelineViewport {
    val safeDuration = sourceDurationMs.coerceAtLeast(0f)
    if (safeDuration == 0f) return VideoTimelineViewport(0f, 0f)

    val selectionStart = selectedRange.start.coerceIn(0f, safeDuration)
    val selectionEnd = selectedRange.endInclusive.coerceIn(selectionStart, safeDuration)
    val safeInset = handleInsetFraction.coerceIn(0f, 0.49f)
    return videoTimelineViewportForSelection(
        selectedRange = selectionStart..selectionEnd,
        selectionStartFraction = safeInset,
        selectionEndFraction = 1f - safeInset,
    )
}

internal fun videoTimelineViewportForSelection(
    selectedRange: ClosedFloatingPointRange<Float>,
    selectionStartFraction: Float,
    selectionEndFraction: Float,
): VideoTimelineViewport {
    val safeStartFraction = selectionStartFraction.coerceIn(0f, 1f)
    val safeEndFraction = selectionEndFraction.coerceIn(
        safeStartFraction + Float.MIN_VALUE,
        1f,
    )
    val selectionDuration = (selectedRange.endInclusive - selectedRange.start)
        .coerceAtLeast(1f)
    val viewportDuration = selectionDuration / (safeEndFraction - safeStartFraction)
    val viewportStart = selectedRange.start - viewportDuration * safeStartFraction
    return VideoTimelineViewport(
        startMs = viewportStart,
        endMs = viewportStart + viewportDuration,
    )
}

internal fun shiftVideoTimelineViewport(
    viewport: VideoTimelineViewport,
    deltaMs: Float,
): VideoTimelineViewport = VideoTimelineViewport(
    startMs = viewport.startMs + deltaMs,
    endMs = viewport.endMs + deltaMs,
)
