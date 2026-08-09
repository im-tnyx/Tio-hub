package com.tnyx.features.workout.presentation.library.createexercise

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
internal fun VideoTimelineSelector(
    frames: List<Bitmap>,
    durationMs: Long,
    selectedRange: ClosedFloatingPointRange<Float>,
    currentPositionMs: Long,
    enabled: Boolean,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TnyxTheme.colors
    val cropperTokens = TnyxTheme.components.imageCropper
    val currentOnRangeChange by rememberUpdatedState(onRangeChange)
    val currentOnSeek by rememberUpdatedState(onSeek)
    val initialViewport = focusedVideoTimelineViewport(
        selectedRange = selectedRange,
        sourceDurationMs = durationMs.toFloat(),
        handleInsetFraction = VIDEO_TIMELINE_HANDLE_INSET_FRACTION,
    )
    val viewportStart = remember(durationMs) { Animatable(initialViewport.startMs) }
    val viewportEnd = remember(durationMs) { Animatable(initialViewport.endMs) }
    val animationScope = rememberCoroutineScope()
    var focusAnimation by remember(durationMs) { mutableStateOf<Job?>(null) }
    var dragViewport by remember(durationMs) { mutableStateOf<VideoTimelineViewport?>(null) }
    val animatedViewport = VideoTimelineViewport(viewportStart.value, viewportEnd.value)
    val visibleViewport = dragViewport ?: animatedViewport
    val currentViewport by rememberUpdatedState(visibleViewport)

    fun beginInteraction() {
        focusAnimation?.cancel()
        val frozenViewport = currentViewport
        dragViewport = frozenViewport
        animationScope.launch {
            viewportStart.stop()
            viewportEnd.stop()
            viewportStart.snapTo(frozenViewport.startMs)
            viewportEnd.snapTo(frozenViewport.endMs)
        }
    }

    fun updateInteraction(
        range: ClosedFloatingPointRange<Float>,
        viewport: VideoTimelineViewport,
    ) {
        dragViewport = viewport
        currentOnRangeChange(range)
    }

    fun finishInteraction(
        finalRange: ClosedFloatingPointRange<Float>,
        finalViewport: VideoTimelineViewport,
    ) {
        val targetViewport = focusedVideoTimelineViewport(
            selectedRange = finalRange,
            sourceDurationMs = durationMs.toFloat(),
            handleInsetFraction = VIDEO_TIMELINE_HANDLE_INSET_FRACTION,
        )
        focusAnimation?.cancel()
        focusAnimation = animationScope.launch {
            viewportStart.snapTo(finalViewport.startMs)
            viewportEnd.snapTo(finalViewport.endMs)
            dragViewport = null
            delay(TIMELINE_FOCUS_DELAY_MS)
            coroutineScope {
                launch {
                    viewportStart.animateTo(
                        targetValue = targetViewport.startMs,
                        animationSpec = timelineFocusAnimationSpec(),
                    )
                }
                launch {
                    viewportEnd.animateTo(
                        targetValue = targetViewport.endMs,
                        animationSpec = timelineFocusAnimationSpec(),
                    )
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier.background(colors.surfaceContainerHigh),
    ) {
        VideoTimelineFrames(
            frames = frames,
            sourceDurationMs = durationMs,
            viewport = visibleViewport,
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
        )

        VideoTimelineSelectionOverlay(
            selectedRange = selectedRange,
            viewport = visibleViewport,
            currentPositionMs = currentPositionMs,
            overlayAlpha = cropperTokens.overlayAlpha,
            modifier = Modifier.fillMaxSize(),
        )

        VideoTimelineGestureLayer(
            selectedRange = selectedRange,
            viewport = visibleViewport,
            durationMs = durationMs,
            enabled = enabled,
            handleWidth = TnyxDimens.SpaceL,
            handleTouchOffset = TnyxDimens.SpaceS,
            onInteractionStart = ::beginInteraction,
            onInteractionUpdate = ::updateInteraction,
            onInteractionFinished = ::finishInteraction,
            onSeek = currentOnSeek,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun VideoTimelineSelectionOverlay(
    selectedRange: ClosedFloatingPointRange<Float>,
    viewport: VideoTimelineViewport,
    currentPositionMs: Long,
    overlayAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val colors = TnyxTheme.colors
    Canvas(modifier = modifier) {
        if (viewport.durationMs <= 0f) return@Canvas
        val startX = videoTimelineFraction(selectedRange.start, viewport) * size.width
        val endX = videoTimelineFraction(selectedRange.endInclusive, viewport) * size.width
        val visibleStartX = startX.coerceIn(0f, size.width)
        val visibleEndX = endX.coerceIn(0f, size.width)
        val playheadX = videoTimelineFraction(currentPositionMs.toFloat(), viewport) * size.width
        val maskColor = colors.background.copy(alpha = overlayAlpha)
        val handleWidth = TnyxDimens.SpaceL.toPx()
        val gripOffset = TnyxDimens.SpaceXXS.toPx()
        val gripInset = TnyxDimens.SpaceM.toPx()

        drawRect(
            color = maskColor,
            topLeft = Offset.Zero,
            size = Size(visibleStartX, size.height),
        )
        drawRect(
            color = maskColor,
            topLeft = Offset(visibleEndX, 0f),
            size = Size((size.width - visibleEndX).coerceAtLeast(0f), size.height),
        )
        drawRect(
            color = colors.primary,
            topLeft = Offset(visibleStartX, 0f),
            size = Size(
                (visibleEndX - visibleStartX).coerceAtLeast(0f),
                size.height,
            ),
            style = Stroke(width = TnyxDimens.BorderThick.toPx()),
        )

        val handleLeftPositions = listOf(
            visibleStartX,
            visibleEndX - handleWidth,
        )
        handleLeftPositions.forEach { handleLeft ->
            drawRect(
                color = colors.primary,
                topLeft = Offset(handleLeft, 0f),
                size = Size(handleWidth, size.height),
            )
            val handleCenter = handleLeft + handleWidth / 2f
            drawLine(
                color = colors.onPrimary,
                start = Offset(handleCenter - gripOffset, gripInset),
                end = Offset(handleCenter - gripOffset, size.height - gripInset),
                strokeWidth = TnyxDimens.BorderMedium.toPx(),
            )
            drawLine(
                color = colors.onPrimary,
                start = Offset(handleCenter + gripOffset, gripInset),
                end = Offset(handleCenter + gripOffset, size.height - gripInset),
                strokeWidth = TnyxDimens.BorderMedium.toPx(),
            )
        }

        if (currentPositionMs.toFloat() in selectedRange) {
            drawLine(
                color = colors.textPrimary,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, size.height),
                strokeWidth = TnyxDimens.BorderMedium.toPx(),
            )
        }
    }
}

@Composable
private fun VideoTimelineGestureLayer(
    selectedRange: ClosedFloatingPointRange<Float>,
    viewport: VideoTimelineViewport,
    durationMs: Long,
    enabled: Boolean,
    handleWidth: Dp,
    handleTouchOffset: Dp,
    onInteractionStart: () -> Unit,
    onInteractionUpdate: (
        ClosedFloatingPointRange<Float>,
        VideoTimelineViewport,
    ) -> Unit,
    onInteractionFinished: (
        ClosedFloatingPointRange<Float>,
        VideoTimelineViewport,
    ) -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentRange by rememberUpdatedState(selectedRange)
    val currentViewport by rememberUpdatedState(viewport)
    val currentOnInteractionStart by rememberUpdatedState(onInteractionStart)
    val currentOnInteractionUpdate by rememberUpdatedState(onInteractionUpdate)
    val currentOnInteractionFinished by rememberUpdatedState(onInteractionFinished)
    val currentOnSeek by rememberUpdatedState(onSeek)

    Box(
        modifier = modifier.pointerInput(enabled, durationMs, handleWidth, handleTouchOffset) {
            if (!enabled || durationMs <= 0L) return@pointerInput
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val widthPx = size.width.toFloat().coerceAtLeast(1f)
                val initialRange = currentRange
                val initialViewport = currentViewport
                val startHandleX = videoTimelineFraction(
                    initialRange.start,
                    initialViewport,
                ) * widthPx
                val endHandleX = videoTimelineFraction(
                    initialRange.endInclusive,
                    initialViewport,
                ) * widthPx
                val target = resolveTimelineGestureTarget(
                    touchX = down.position.x,
                    startHandleX = startHandleX,
                    endHandleX = endHandleX,
                    handleWidthPx = handleWidth.toPx(),
                    touchOffsetPx = handleTouchOffset.toPx(),
                )
                var accumulatedDragPx = 0f
                var interactionStarted = false
                var latestRange = initialRange
                var latestViewport = initialViewport

                drag(down.id) { change ->
                    accumulatedDragPx += change.positionChange().x
                    if (!interactionStarted && abs(accumulatedDragPx) >= viewConfiguration.touchSlop) {
                        interactionStarted = true
                        currentOnInteractionStart()
                    }
                    if (interactionStarted) {
                        val result = when (target) {
                            VideoTimelineGestureTarget.FILMSTRIP -> scrollVideoTimeline(
                                initialRange = initialRange,
                                initialViewport = initialViewport,
                                accumulatedDragPx = accumulatedDragPx,
                                timelineWidthPx = widthPx,
                                sourceDurationMs = durationMs.toFloat(),
                            )

                            VideoTimelineGestureTarget.START_HANDLE -> resizeVideoTrimRange(
                                handle = VideoTrimHandle.START,
                                initialRange = initialRange,
                                initialViewport = initialViewport,
                                accumulatedDragPx = accumulatedDragPx,
                                timelineWidthPx = widthPx,
                                sourceDurationMs = durationMs.toFloat(),
                            )

                            VideoTimelineGestureTarget.END_HANDLE -> resizeVideoTrimRange(
                                handle = VideoTrimHandle.END,
                                initialRange = initialRange,
                                initialViewport = initialViewport,
                                accumulatedDragPx = accumulatedDragPx,
                                timelineWidthPx = widthPx,
                                sourceDurationMs = durationMs.toFloat(),
                            )
                        }
                        latestRange = result.range
                        latestViewport = result.viewport
                        currentOnInteractionUpdate(latestRange, latestViewport)
                    }
                    change.consume()
                }

                if (interactionStarted) {
                    currentOnInteractionFinished(latestRange, latestViewport)
                } else {
                    val requestedTime = initialViewport.startMs +
                        initialViewport.durationMs * (down.position.x / widthPx)
                    currentOnSeek(
                        requestedTime.coerceIn(
                            initialRange.start,
                            initialRange.endInclusive,
                        ).toLong(),
                    )
                }
            }
        },
    )
}

private fun resolveTimelineGestureTarget(
    touchX: Float,
    startHandleX: Float,
    endHandleX: Float,
    handleWidthPx: Float,
    touchOffsetPx: Float,
): VideoTimelineGestureTarget {
    val touchesStartHandle = touchX in
        (startHandleX - touchOffsetPx)..(startHandleX + handleWidthPx + touchOffsetPx)
    val touchesEndHandle = touchX in
        (endHandleX - handleWidthPx - touchOffsetPx)..(endHandleX + touchOffsetPx)
    val startCenter = startHandleX + handleWidthPx / 2f
    val endCenter = endHandleX - handleWidthPx / 2f
    return when {
        touchesStartHandle && (!touchesEndHandle || abs(touchX - startCenter) <= abs(touchX - endCenter)) ->
            VideoTimelineGestureTarget.START_HANDLE

        touchesEndHandle -> VideoTimelineGestureTarget.END_HANDLE
        else -> VideoTimelineGestureTarget.FILMSTRIP
    }
}

private fun scrollVideoTimeline(
    initialRange: ClosedFloatingPointRange<Float>,
    initialViewport: VideoTimelineViewport,
    accumulatedDragPx: Float,
    timelineWidthPx: Float,
    sourceDurationMs: Float,
): VideoTimelineInteractionResult {
    val requestedDeltaMs = -accumulatedDragPx /
        timelineWidthPx.coerceAtLeast(1f) * initialViewport.durationMs
    val shiftedRange = shiftVideoTrimRange(
        selectedRange = initialRange,
        deltaMs = requestedDeltaMs,
        sourceDurationMs = sourceDurationMs,
    )
    val appliedDeltaMs = shiftedRange.start - initialRange.start
    return VideoTimelineInteractionResult(
        range = shiftedRange,
        viewport = shiftVideoTimelineViewport(initialViewport, appliedDeltaMs),
    )
}

private fun resizeVideoTrimRange(
    handle: VideoTrimHandle,
    initialRange: ClosedFloatingPointRange<Float>,
    initialViewport: VideoTimelineViewport,
    accumulatedDragPx: Float,
    timelineWidthPx: Float,
    sourceDurationMs: Float,
): VideoTimelineInteractionResult {
    val initialHandleX = when (handle) {
        VideoTrimHandle.START -> videoTimelineFraction(initialRange.start, initialViewport)
        VideoTrimHandle.END -> videoTimelineFraction(initialRange.endInclusive, initialViewport)
    } * timelineWidthPx
    val isOutwardDrag = when (handle) {
        VideoTrimHandle.START -> accumulatedDragPx < 0f
        VideoTrimHandle.END -> accumulatedDragPx > 0f
    }
    if (!isOutwardDrag) {
        val deltaMs = accumulatedDragPx /
            timelineWidthPx.coerceAtLeast(1f) * initialViewport.durationMs
        val minimumDuration = MIN_VIDEO_TRIM_DURATION_MS.toFloat()
        val range = when (handle) {
            VideoTrimHandle.START -> {
                val maximumStart = initialRange.endInclusive - minimumDuration
                (initialRange.start + deltaMs)
                    .coerceIn(0f, maximumStart)..initialRange.endInclusive
            }

            VideoTrimHandle.END -> {
                val minimumEnd = initialRange.start + minimumDuration
                initialRange.start..(initialRange.endInclusive + deltaMs)
                    .coerceIn(minimumEnd, sourceDurationMs)
            }
        }
        return VideoTimelineInteractionResult(range, initialViewport)
    }

    val initialDuration = initialRange.endInclusive - initialRange.start
    val availableDuration = min(
        MAX_VIDEO_TRIM_DURATION_MS.toFloat() - initialDuration,
        when (handle) {
            VideoTrimHandle.START -> initialRange.start
            VideoTrimHandle.END -> sourceDurationMs - initialRange.endInclusive
        },
    ).coerceAtLeast(0f)
    if (availableDuration == 0f) {
        return VideoTimelineInteractionResult(initialRange, initialViewport)
    }

    val outwardTravelPx = when (handle) {
        VideoTrimHandle.START -> initialHandleX
        VideoTrimHandle.END -> timelineWidthPx - initialHandleX
    }.coerceAtLeast(1f)
    val outwardFraction = (abs(accumulatedDragPx) / outwardTravelPx).coerceIn(0f, 1f)
    val expansionMs = availableDuration * outwardFraction
    val range = when (handle) {
        VideoTrimHandle.START -> (initialRange.start - expansionMs)..initialRange.endInclusive
        VideoTrimHandle.END -> initialRange.start..(initialRange.endInclusive + expansionMs)
    }
    val draggedHandleFraction = when (handle) {
        VideoTrimHandle.START ->
            ((initialHandleX - outwardTravelPx * outwardFraction) / timelineWidthPx)
                .coerceIn(0f, 1f)

        VideoTrimHandle.END ->
            ((initialHandleX + outwardTravelPx * outwardFraction) / timelineWidthPx)
                .coerceIn(0f, 1f)
    }
    val fixedHandleFraction = when (handle) {
        VideoTrimHandle.START ->
            videoTimelineFraction(initialRange.endInclusive, initialViewport)

        VideoTrimHandle.END -> videoTimelineFraction(initialRange.start, initialViewport)
    }
    val viewport = when (handle) {
        VideoTrimHandle.START -> videoTimelineViewportForSelection(
            selectedRange = range,
            selectionStartFraction = draggedHandleFraction,
            selectionEndFraction = fixedHandleFraction,
        )

        VideoTrimHandle.END -> videoTimelineViewportForSelection(
            selectedRange = range,
            selectionStartFraction = fixedHandleFraction,
            selectionEndFraction = draggedHandleFraction,
        )
    }
    return VideoTimelineInteractionResult(range, viewport)
}

@Composable
private fun VideoTimelineFrames(
    frames: List<Bitmap>,
    sourceDurationMs: Long,
    viewport: VideoTimelineViewport,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        if (frames.isEmpty() || sourceDurationMs <= 0L || viewport.durationMs <= 0f) {
            return@BoxWithConstraints
        }
        val frameDurationMs = sourceDurationMs.toFloat() / frames.size
        frames.forEachIndexed { index, frame ->
            val frameStartMs = index * frameDurationMs
            val frameEndMs = (index + 1) * frameDurationMs
            val visibleStartMs = max(frameStartMs, viewport.startMs)
            val visibleEndMs = min(frameEndMs, viewport.endMs)
            if (visibleEndMs > visibleStartMs) {
                val startFraction = (visibleStartMs - viewport.startMs) / viewport.durationMs
                val widthFraction = (visibleEndMs - visibleStartMs) / viewport.durationMs
                Image(
                    bitmap = frame.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .offset(x = maxWidth * startFraction)
                        .width(maxWidth * widthFraction)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

private fun videoTimelineFraction(
    timeMs: Float,
    viewport: VideoTimelineViewport,
): Float {
    if (viewport.durationMs <= 0f) return 0f
    return (timeMs - viewport.startMs) / viewport.durationMs
}

private fun timelineFocusAnimationSpec() = tween<Float>(
    durationMillis = TIMELINE_FOCUS_DURATION_MS,
    easing = FastOutSlowInEasing,
)

private data class VideoTimelineInteractionResult(
    val range: ClosedFloatingPointRange<Float>,
    val viewport: VideoTimelineViewport,
)

private enum class VideoTimelineGestureTarget {
    FILMSTRIP,
    START_HANDLE,
    END_HANDLE,
}

private enum class VideoTrimHandle {
    START,
    END,
}

private const val TIMELINE_FOCUS_DELAY_MS = 100L
private const val TIMELINE_FOCUS_DURATION_MS = 500
