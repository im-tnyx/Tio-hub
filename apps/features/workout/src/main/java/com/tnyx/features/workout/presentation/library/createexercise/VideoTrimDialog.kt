package com.tnyx.features.workout.presentation.library.createexercise

import android.graphics.Bitmap
import android.net.Uri
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@OptIn(UnstableApi::class)
@Composable
internal fun VideoTrimDialog(
    visible: Boolean,
    videoUri: Uri?,
    maxOutputBytes: Long,
    onDismissRequest: () -> Unit,
    onTrimSuccess: (File) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible || videoUri == null) return

    val context = LocalContext.current
    var durationMs by remember(videoUri) { mutableLongStateOf(0L) }
    var sourceAspectRatio by remember(videoUri) { mutableStateOf(1f) }
    var selectedRange by remember(videoUri) { mutableStateOf(0f..0f) }
    var draftRange by remember(videoUri) { mutableStateOf(0f..0f) }
    var selectedCrop by remember(videoUri) { mutableStateOf(VideoCropPreset.CUSTOM) }
    var draftCrop by remember(videoUri) { mutableStateOf(VideoCropPreset.CUSTOM) }
    var selectedCropBounds by remember(videoUri) { mutableStateOf(NormalizedVideoCrop.Full) }
    var timelineFrames by remember(videoUri) { mutableStateOf<List<Bitmap>>(emptyList()) }
    var currentPositionMs by remember(videoUri) { mutableLongStateOf(0L) }
    var mode by remember(videoUri) { mutableStateOf(VideoEditorMode.EDITOR) }
    var requiresTrimConfirmation by remember(videoUri) { mutableStateOf(false) }
    var isPlaying by remember(videoUri) { mutableStateOf(false) }
    var isExporting by remember(videoUri) { mutableStateOf(false) }
    var errorMessage by remember(videoUri) { mutableStateOf<String?>(null) }
    val cropEditorState = rememberVideoCropEditorState()
    val previewTransformState = rememberVideoPreviewTransformState()

    val player = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }
    val previewRange = if (mode == VideoEditorMode.TRIM) draftRange else selectedRange

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(videoUri) {
        val metadata = withContext(Dispatchers.IO) {
            loadVideoTrimMetadata(context, videoUri)
        }
        if (metadata.durationMs <= 0L) {
            onError("Selected video duration could not be read.")
            onDismissRequest()
            return@LaunchedEffect
        }
        durationMs = metadata.durationMs
        sourceAspectRatio = metadata.sourceAspectRatio
        selectedRange = 0f..min(metadata.durationMs, MAX_VIDEO_TRIM_DURATION_MS).toFloat()
        draftRange = selectedRange
        timelineFrames = metadata.frames
        requiresTrimConfirmation = requiresVideoTrimConfirmation(metadata.durationMs)
        mode = if (requiresTrimConfirmation) {
            VideoEditorMode.TRIM
        } else {
            VideoEditorMode.EDITOR
        }
    }

    LaunchedEffect(player, previewRange, isPlaying) {
        while (isActive) {
            val rangeEndMs = previewRange.endInclusive.toLong()
            currentPositionMs = player.currentPosition.coerceAtLeast(0L)
            if (isPlaying && rangeEndMs > 0L && currentPositionMs >= rangeEndMs) {
                player.pause()
                player.seekTo(previewRange.start.toLong())
                currentPositionMs = previewRange.start.toLong()
            }
            delay(100L)
        }
    }

    Dialog(
        onDismissRequest = { if (!isExporting) onDismissRequest() },
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            decorFitsSystemWindows = true,
        ),
    ) {
        val dialogWindow = (LocalView.current.parent as DialogWindowProvider).window
        SideEffect {
            dialogWindow.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
            )
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(TnyxTheme.colors.background)
                .statusBarsPadding(),
        ) {
            if (mode == VideoEditorMode.CROP) {
                VideoCropViewport(
                    player = player,
                    sourceAspectRatio = sourceAspectRatio,
                    state = cropEditorState,
                    onManualCrop = { draftCrop = VideoCropPreset.CUSTOM },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            } else {
                VideoPreviewStage(
                    player = player,
                    isPlaying = isPlaying,
                    enabled = durationMs > 0L && !isExporting,
                    sourceAspectRatio = sourceAspectRatio,
                    transformState = previewTransformState,
                    showPlaybackButton = mode != VideoEditorMode.TRIM,
                    onTogglePlayback = { toggleVideoPlayback(player, previewRange, isPlaying) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }

            when (mode) {
                VideoEditorMode.EDITOR -> VideoEditorToolbar(
                    isExporting = isExporting,
                    errorMessage = errorMessage,
                    onTrimClick = {
                        player.pause()
                        draftRange = selectedRange
                        mode = VideoEditorMode.TRIM
                    },
                    onCropClick = {
                        player.pause()
                        draftCrop = selectedCrop
                        cropEditorState.showSelection(selectedCropBounds)
                        mode = VideoEditorMode.CROP
                    },
                    onCancel = onDismissRequest,
                    onApply = {
                        if (requiresTrimConfirmation) {
                            mode = VideoEditorMode.TRIM
                            return@VideoEditorToolbar
                        }
                        player.pause()
                        errorMessage = null
                        isExporting = true
                    },
                    canApply = durationMs > 0L && !requiresTrimConfirmation,
                )

                VideoEditorMode.TRIM -> VideoTrimToolbar(
                    selectedRange = draftRange,
                    durationMs = durationMs,
                    currentPositionMs = currentPositionMs,
                    frames = timelineFrames,
                    isPlaying = isPlaying,
                    enabled = !isExporting,
                    onTogglePlayback = { toggleVideoPlayback(player, draftRange, isPlaying) },
                    onRangeChange = { range ->
                        draftRange = range
                        errorMessage = null
                        player.pause()
                        player.seekTo(range.start.toLong())
                        currentPositionMs = range.start.toLong()
                    },
                    onSeek = { positionMs ->
                        player.seekTo(positionMs)
                        currentPositionMs = positionMs
                    },
                    onCancel = {
                        player.pause()
                        draftRange = selectedRange
                        if (requiresTrimConfirmation) {
                            onDismissRequest()
                        } else {
                            mode = VideoEditorMode.EDITOR
                        }
                    },
                    onApply = {
                        player.pause()
                        selectedRange = draftRange
                        requiresTrimConfirmation = false
                        mode = VideoEditorMode.EDITOR
                    },
                )

                VideoEditorMode.CROP -> VideoCropToolbar(
                    selectedPreset = draftCrop,
                    sourceAspectRatio = sourceAspectRatio,
                    enabled = !isExporting,
                    onPresetSelected = { preset ->
                        draftCrop = preset
                        cropEditorState.showSelection(
                            centeredVideoCrop(
                                sourceAspectRatio = sourceAspectRatio,
                                targetAspectRatio = preset.aspectRatio,
                            )
                        )
                    },
                    onCancel = {
                        draftCrop = selectedCrop
                        mode = VideoEditorMode.EDITOR
                    },
                    onApply = {
                        selectedCrop = draftCrop
                        selectedCropBounds = cropEditorState.currentSelection()
                        mode = VideoEditorMode.EDITOR
                    },
                )
            }
        }
    }

    LaunchedEffect(isExporting) {
        if (!isExporting) return@LaunchedEffect
        if (
            requiresTrimConfirmation ||
            selectedRange.endInclusive - selectedRange.start > MAX_VIDEO_TRIM_DURATION_MS
        ) {
            isExporting = false
            errorMessage = "Select and confirm a clip of 30 seconds or less."
            mode = VideoEditorMode.TRIM
            return@LaunchedEffect
        }
        val result = runCatching {
            exportEditedVideo(
                context = context,
                sourceUri = videoUri,
                startPositionMs = selectedRange.start.toLong(),
                endPositionMs = selectedRange.endInclusive.toLong(),
                crop = selectedCropBounds,
            )
        }
        isExporting = false
        result.onSuccess { outputFile ->
            if (outputFile.length() <= 0L) {
                outputFile.delete()
                errorMessage = "Edited video could not be created. Try another clip."
            } else if (outputFile.length() > maxOutputBytes) {
                outputFile.delete()
                errorMessage = "Edited video is still too large. Select a shorter clip (maximum 50 MB)."
            } else {
                onTrimSuccess(outputFile)
            }
        }.onFailure { error ->
            errorMessage = error.message ?: "Video could not be edited. Try another clip."
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPreviewStage(
    player: ExoPlayer,
    isPlaying: Boolean,
    enabled: Boolean,
    sourceAspectRatio: Float,
    transformState: VideoPreviewTransformState,
    showPlaybackButton: Boolean,
    onTogglePlayback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TnyxTheme.colors

    BoxWithConstraints(
        modifier = modifier
            .background(colors.surfaceRaised)
            .clipToBounds()
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTransformGestures { centroid, pan, zoom, _ ->
                    transformState.transform(
                        centroid = centroid,
                        pan = pan,
                        zoom = zoom,
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val stageAspectRatio = maxWidth.value / maxHeight.value
        val previewWidth = if (sourceAspectRatio >= stageAspectRatio) maxWidth else maxHeight * sourceAspectRatio
        val previewHeight = if (sourceAspectRatio >= stageAspectRatio) maxWidth / sourceAspectRatio else maxHeight
        SideEffect {
            transformState.updateLayout(
                stageSize = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat()),
                contentSize = Size(
                    width = constraints.maxWidth.toFloat().let { stageWidth ->
                        if (sourceAspectRatio >= stageAspectRatio) {
                            stageWidth
                        } else {
                            constraints.maxHeight * sourceAspectRatio
                        }
                    },
                    height = constraints.maxHeight.toFloat().let { stageHeight ->
                        if (sourceAspectRatio >= stageAspectRatio) {
                            constraints.maxWidth / sourceAspectRatio
                        } else {
                            stageHeight
                        }
                    },
                ),
            )
        }

        Box(
            modifier = Modifier
                .size(width = previewWidth, height = previewHeight)
                .clip(RectangleShape)
                .graphicsLayer {
                    scaleX = transformState.scale
                    scaleY = transformState.scale
                    translationX = transformState.offset.x
                    translationY = transformState.offset.y
                },
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        useController = false
                        this.player = player
                    }
                },
                update = { view ->
                    view.player = player
                    view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                },
                onRelease = { view -> view.player = null },
                modifier = Modifier.fillMaxSize(),
            )

            if (showPlaybackButton) {
                IconButton(
                    onClick = onTogglePlayback,
                    enabled = enabled,
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = if (isPlaying) "Pause video" else "Play video",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(TnyxDimens.IconL),
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoEditorToolbar(
    isExporting: Boolean,
    errorMessage: String?,
    onTrimClick: () -> Unit,
    onCropClick: () -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit,
    canApply: Boolean,
) {
    VideoToolPanel {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = TnyxDimens.SpaceM),
            horizontalArrangement = Arrangement.Center,
        ) {
            VideoEditorTool(
                label = "Trim",
                icon = { tint ->
                    Icon(Icons.Outlined.ContentCut, "Trim video", tint = tint)
                },
                onClick = onTrimClick,
                enabled = !isExporting,
            )
            Spacer(modifier = Modifier.width(TnyxDimens.SpaceXXL))
            VideoEditorTool(
                label = "Crop",
                icon = { tint ->
                    Icon(Icons.Outlined.Crop, "Crop video", tint = tint)
                },
                onClick = onCropClick,
                enabled = !isExporting,
            )
        }
        errorMessage?.let { VideoEditorError(it) }
        VideoEditorActionFooter(
            title = if (isExporting) "EXPORTING" else "EDITOR",
            onCancel = onCancel,
            onApply = onApply,
            canApply = canApply && !isExporting,
            isProcessing = isExporting,
            applyContentDescription = "Apply video edits",
        )
    }
}

@Composable
private fun VideoEditorTool(
    label: String,
    icon: @Composable (androidx.compose.ui.graphics.Color) -> Unit,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val tint = if (enabled) TnyxTheme.colors.textPrimary else TnyxTheme.colors.textMuted
    Column(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = TnyxDimens.SpaceM, vertical = TnyxDimens.SpaceS),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(TnyxDimens.IconL), contentAlignment = Alignment.Center) {
            icon(tint)
        }
        Spacer(modifier = Modifier.height(TnyxDimens.SpaceXS))
        Text(label, style = TnyxTheme.typography.labelMedium, color = tint)
    }
}

@Composable
private fun VideoTrimToolbar(
    selectedRange: ClosedFloatingPointRange<Float>,
    durationMs: Long,
    currentPositionMs: Long,
    frames: List<Bitmap>,
    isPlaying: Boolean,
    enabled: Boolean,
    onTogglePlayback: () -> Unit,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onSeek: (Long) -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    VideoToolPanel {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = TnyxDimens.SpaceM,
                    top = TnyxDimens.SpaceM,
                    end = TnyxDimens.SpaceM,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                formatVideoTime(currentPositionMs.coerceIn(0L, durationMs)),
                style = TnyxTheme.typography.labelMedium,
                color = TnyxTheme.colors.textSecondary,
            )
            IconButton(onClick = onTogglePlayback, enabled = enabled && durationMs > 0L) {
                Icon(
                    imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (isPlaying) "Pause selected clip" else "Play selected clip",
                    tint = TnyxTheme.colors.textPrimary,
                )
            }
            Text(
                formatVideoTime(selectedRange.endInclusive.toLong()),
                style = TnyxTheme.typography.labelMedium,
                color = TnyxTheme.colors.textSecondary,
            )
        }

        VideoTimelineSelector(
            frames = frames,
            durationMs = durationMs,
            selectedRange = selectedRange,
            currentPositionMs = currentPositionMs,
            enabled = enabled,
            onRangeChange = onRangeChange,
            onSeek = onSeek,
            modifier = Modifier
                .fillMaxWidth()
                .height(TnyxDimens.SpaceHuge + TnyxDimens.SpaceM),
        )

        VideoEditorActionFooter(
            title = "TRIM",
            onCancel = onCancel,
            onApply = onApply,
            canApply = enabled && durationMs > 0L,
            isProcessing = false,
            applyContentDescription = "Apply video trim",
        )
    }
}

@Composable
private fun VideoCropToolbar(
    selectedPreset: VideoCropPreset,
    sourceAspectRatio: Float,
    enabled: Boolean,
    onPresetSelected: (VideoCropPreset) -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    VideoToolPanel {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = TnyxDimens.SpaceM, vertical = TnyxDimens.SpaceM),
            horizontalArrangement = Arrangement.spacedBy(TnyxDimens.SpaceM),
            verticalAlignment = Alignment.Bottom,
        ) {
            VideoCropPreset.entries.forEach { preset ->
                CropPresetOption(
                    preset = preset,
                    sourceAspectRatio = sourceAspectRatio,
                    selected = preset == selectedPreset,
                    enabled = enabled,
                    onClick = { onPresetSelected(preset) },
                )
            }
        }
        VideoEditorActionFooter(
            title = "CROP",
            onCancel = onCancel,
            onApply = onApply,
            canApply = enabled,
            isProcessing = false,
            applyContentDescription = "Apply video crop",
        )
    }
}

@Composable
private fun CropPresetOption(
    preset: VideoCropPreset,
    sourceAspectRatio: Float,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = TnyxTheme.colors
    val ratio = preset.resolveAspectRatio(sourceAspectRatio).coerceIn(0.4f, 2.5f)
    val previewHeight = TnyxDimens.IconM
    val previewWidth = (previewHeight.value * ratio)
        .coerceAtMost(TnyxDimens.ButtonHeight.value)
        .dp
    val tint = when {
        !enabled -> colors.textMuted
        selected -> colors.primary
        else -> colors.textSecondary
    }
    Column(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(TnyxDimens.SpaceXS),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(TnyxDimens.ButtonHeight)
                .border(
                    width = if (selected) TnyxDimens.BorderMedium else TnyxDimens.BorderThin,
                    color = tint,
                    shape = TnyxTheme.shapes.Material.extraSmall,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = previewWidth, height = previewHeight)
                    .background(colors.surfaceContainerHighest),
            )
        }
        Spacer(modifier = Modifier.height(TnyxDimens.SpaceXS))
        Text(preset.label, style = TnyxTheme.typography.labelSmall, color = tint)
    }
}

@Composable
private fun VideoToolPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TnyxTheme.colors.surface)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

@Composable
private fun VideoEditorError(message: String) {
    Text(
        text = message,
        style = TnyxTheme.typography.bodySmall,
        color = TnyxTheme.colors.error,
        modifier = Modifier.padding(horizontal = TnyxDimens.SpaceM),
    )
}

@Composable
private fun VideoEditorActionFooter(
    title: String,
    onCancel: () -> Unit,
    onApply: () -> Unit,
    canApply: Boolean,
    isProcessing: Boolean,
    applyContentDescription: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TnyxDimens.SpaceM, vertical = TnyxDimens.SpaceS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCancel, enabled = !isProcessing) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = "Cancel",
                tint = TnyxTheme.colors.textPrimary,
            )
        }
        Text(
            title,
            style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TnyxTheme.colors.textPrimary,
        )
        if (isProcessing) {
            Box(
                modifier = Modifier.size(TnyxDimens.ScreenHeaderActionSize),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = TnyxTheme.colors.primary,
                    modifier = Modifier.size(TnyxDimens.IconM),
                )
            }
        } else {
            IconButton(onClick = onApply, enabled = canApply) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = applyContentDescription,
                    tint = if (canApply) TnyxTheme.colors.primary else TnyxTheme.colors.textMuted,
                )
            }
        }
    }
}

private fun toggleVideoPlayback(
    player: ExoPlayer,
    range: ClosedFloatingPointRange<Float>,
    isPlaying: Boolean,
) {
    if (isPlaying) {
        player.pause()
        return
    }
    val rangeStartMs = range.start.toLong()
    val rangeEndMs = range.endInclusive.toLong()
    if (player.currentPosition !in rangeStartMs until rangeEndMs) {
        player.seekTo(rangeStartMs)
    }
    player.play()
}

private fun formatVideoTime(positionMs: Long): String {
    val totalSeconds = positionMs.coerceAtLeast(0L) / 1_000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
