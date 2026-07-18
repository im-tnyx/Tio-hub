package com.tnyx.shared.workout.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SetType {
    NORMAL,
    WARMUP,
    DROP_SET,
    FAILURE,
    SUPERSET
}

/** One set nested under a stable session exercise entry. */
@Serializable
data class WorkoutSet(
    val id: String,
    val exerciseEntryId: String,
    val setNumber: Int,
    val schemaVersion: Int = WORKOUT_CONTRACT_VERSION,
    val type: SetType = SetType.NORMAL,
    val weightKg: Double? = null,
    val reps: Int? = null,
    val steps: Int? = null,
    val durationSeconds: Int? = null,
    val distanceMeters: Double? = null,
    val isCompleted: Boolean = false,
    val rpe: Int? = null,
    val notes: String = "",
    val completedAtMs: Long? = null
) {
    val countsToVolume: Boolean
        get() = isCompleted && type != SetType.WARMUP

    val volumeKg: Double
        get() = if (countsToVolume) (weightKg ?: 0.0) * (reps ?: 0) else 0.0

    val hasRecordedMetric: Boolean
        get() = (reps ?: 0) > 0 ||
            (steps ?: 0) > 0 ||
            (durationSeconds ?: 0) > 0 ||
            (distanceMeters ?: 0.0) > 0.0
}
