package com.tnyx.data.workout

import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseMediaAsset
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

internal const val EXERCISE_MEDIA_BUCKET = "tio-exercise-media"

private const val EXERCISE_MEDIA_REFERENCE_PREFIX =
    "supabase-storage://$EXERCISE_MEDIA_BUCKET/"

private val exerciseMediaUrlMarkers = listOf(
    "/storage/v1/object/public/$EXERCISE_MEDIA_BUCKET/",
    "/storage/v1/object/sign/$EXERCISE_MEDIA_BUCKET/",
)

internal fun String.toExerciseMediaObjectPath(): String? {
    val encodedPath = when {
        startsWith(EXERCISE_MEDIA_REFERENCE_PREFIX) -> removePrefix(EXERCISE_MEDIA_REFERENCE_PREFIX)
        else -> exerciseMediaUrlMarkers.firstNotNullOfOrNull { marker ->
            substringAfter(marker, missingDelimiterValue = "")
                .takeIf(String::isNotBlank)
        }
    }?.substringBefore('?') ?: return null

    val decodedPath = runCatching {
        URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())
    }.getOrNull() ?: return null

    return decodedPath.takeIf(String::isSafeExerciseMediaObjectPath)
}

internal fun String.toOwnedExerciseMediaObjectPath(ownerUserId: String): String? {
    return toExerciseMediaObjectPath()
        ?.takeIf { path -> path.substringBefore('/') == ownerUserId }
}

internal fun String.toExerciseMediaStorageReference(): String {
    return "$EXERCISE_MEDIA_REFERENCE_PREFIX$this"
}

internal fun ExerciseDefinition.withDurableExerciseMediaReferences(): ExerciseDefinition {
    return copy(
        mediaAssets = mediaAssets.map(ExerciseMediaAsset::withDurableExerciseMediaReferences)
    )
}

private fun ExerciseMediaAsset.withDurableExerciseMediaReferences(): ExerciseMediaAsset {
    return copy(
        imageRef = imageRef.toDurableExerciseMediaReference(),
        videoRef = videoRef.toDurableExerciseMediaReference(),
        thumbnailRef = thumbnailRef.toDurableExerciseMediaReference(),
    )
}

private fun String?.toDurableExerciseMediaReference(): String? {
    val reference = this ?: return null
    return reference.toExerciseMediaObjectPath()
        ?.toExerciseMediaStorageReference()
        ?: reference
}

private fun String.isSafeExerciseMediaObjectPath(): Boolean {
    if (isBlank() || startsWith('/') || contains('\\')) return false
    return split('/').all { segment ->
        segment.isNotBlank() && segment != "." && segment != ".."
    }
}
