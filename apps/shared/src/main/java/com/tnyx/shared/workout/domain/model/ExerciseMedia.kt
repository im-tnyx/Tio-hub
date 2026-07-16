package com.tnyx.shared.workout.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ExerciseMediaPreference {
    AUTO,
    MALE,
    FEMALE,
    NEUTRAL
}

@Serializable
enum class ExerciseMediaVariant {
    MALE,
    FEMALE,
    NEUTRAL
}

@Serializable
enum class ExerciseMediaReleaseStatus {
    BLOCKED,
    APPROVED
}

@Serializable
data class ExerciseMediaAsset(
    val id: String,
    val variant: ExerciseMediaVariant,
    val imageRef: String? = null,
    val videoRef: String? = null,
    val thumbnailRef: String? = null,
    val mediaVersion: Int = 1,
    val provenanceId: String,
    val releaseStatus: ExerciseMediaReleaseStatus = ExerciseMediaReleaseStatus.BLOCKED
) {
    val hasMedia: Boolean
        get() = !imageRef.isNullOrBlank() || !videoRef.isNullOrBlank() || !thumbnailRef.isNullOrBlank()
}

@Serializable
enum class ExerciseMediaResolutionReason {
    EXACT,
    NEUTRAL_FALLBACK,
    PLACEHOLDER
}

@Serializable
data class ResolvedExerciseMedia(
    val exerciseId: String,
    val requestedVariant: ExerciseMediaVariant,
    val resolvedVariant: ExerciseMediaVariant? = null,
    val asset: ExerciseMediaAsset? = null,
    val reason: ExerciseMediaResolutionReason
)
