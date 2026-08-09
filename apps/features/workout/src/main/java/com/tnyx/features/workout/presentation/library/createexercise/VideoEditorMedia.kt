package com.tnyx.features.workout.presentation.library.createexercise

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Crop
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

private const val TIMELINE_FRAME_COUNT = 9
private const val TIMELINE_FRAME_WIDTH = 160
private const val TIMELINE_FRAME_HEIGHT = 90
private const val RIGHT_ANGLE_DEGREES = 90
private const val THREE_QUARTER_ANGLE_DEGREES = 270

internal fun loadVideoTrimMetadata(context: Context, uri: Uri): VideoTrimMetadata {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLongOrNull()
            ?: 0L
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            ?.toFloatOrNull()
            ?: 1f
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            ?.toFloatOrNull()
            ?: 1f
        val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            ?.toIntOrNull()
            ?: 0
        val sourceAspectRatio = if (
            rotation == RIGHT_ANGLE_DEGREES || rotation == THREE_QUARTER_ANGLE_DEGREES
        ) {
            height / width
        } else {
            width / height
        }.coerceAtLeast(Float.MIN_VALUE)
        val frames = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && durationMs > 0L) {
            List(TIMELINE_FRAME_COUNT) { index ->
                val frameTimeUs = (durationMs * 1_000L * index) / (TIMELINE_FRAME_COUNT - 1)
                retriever.getScaledFrameAtTime(
                    frameTimeUs,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    TIMELINE_FRAME_WIDTH,
                    TIMELINE_FRAME_HEIGHT,
                )
            }.filterNotNull()
        } else {
            emptyList()
        }
        VideoTrimMetadata(
            durationMs = durationMs,
            sourceAspectRatio = sourceAspectRatio,
            frames = frames,
        )
    } finally {
        retriever.release()
    }
}

@OptIn(UnstableApi::class)
internal suspend fun exportEditedVideo(
    context: Context,
    sourceUri: Uri,
    startPositionMs: Long,
    endPositionMs: Long,
    crop: NormalizedVideoCrop,
): File = withContext(Dispatchers.Main) {
    val outputFile = File(context.cacheDir, "edited_exercise_${System.currentTimeMillis()}.mp4")
    runCatching { outputFile.delete() }

    suspendCancellableCoroutine { continuation ->
        val mediaItem = MediaItem.Builder()
            .setUri(sourceUri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(startPositionMs)
                    .setEndPositionMs(endPositionMs)
                    .build()
            )
            .build()
        val editedMediaItem = EditedMediaItem.Builder(mediaItem).apply {
            if (crop != NormalizedVideoCrop.Full) {
                setEffects(
                    Effects(
                        emptyList(),
                        listOf(
                            Crop(
                                crop.left * 2f - 1f,
                                crop.right * 2f - 1f,
                                1f - crop.bottom * 2f,
                                1f - crop.top * 2f,
                            )
                        ),
                    )
                )
            }
        }.build()
        lateinit var transformer: Transformer
        transformer = Transformer.Builder(context)
            .addListener(
                object : Transformer.Listener {
                    override fun onCompleted(
                        composition: androidx.media3.transformer.Composition,
                        exportResult: ExportResult,
                    ) {
                        if (continuation.isActive) continuation.resume(outputFile)
                    }

                    override fun onError(
                        composition: androidx.media3.transformer.Composition,
                        exportResult: ExportResult,
                        exportException: ExportException,
                    ) {
                        outputFile.delete()
                        if (continuation.isActive) {
                            continuation.resumeWith(Result.failure(exportException))
                        }
                    }
                }
            )
            .build()
        continuation.invokeOnCancellation {
            transformer.cancel()
            outputFile.delete()
        }
        transformer.start(editedMediaItem, outputFile.absolutePath)
    }
}
