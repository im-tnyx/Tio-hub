package com.tnyx.shared.workout.domain.model

import kotlinx.serialization.Serializable

/**
 * Set का type — Phone App और Watch App दोनों में same enum use होगा।
 *
 * Volume calculation rules:
 * - WARMUP → total volume में count नहीं होगा
 * - DROP_SET → rest timer skip होगा automatically
 * - SUPERSET → अगले exercise के साथ back-to-back चलेगा
 */
@Serializable
enum class SetType {
    NORMAL,     // Standard working set
    WARMUP,     // Warm-up (volume excluded)
    DROP_SET,   // Drop set (no rest, reduced weight)
    FAILURE,    // Taken to failure
    SUPERSET    // Paired with next exercise
}

/**
 * एक logged set का data model।
 * Phone App → Watch App → Phone App sync में यही travel करता है।
 */
@Serializable
data class WorkoutSet(
    val id: String,
    val exerciseId: String,
    val setNumber: Int,
    val type: SetType = SetType.NORMAL,
    val weightKg: Double,
    val reps: Int,
    val isCompleted: Boolean = false,
    val rpe: Int? = null,          // Rate of Perceived Exertion (1–10)
    val notes: String = "",
    val timestampMs: Long = 0L
) {
    /** Volume में count होगा — Warm-up sets exclude */
    val countsToVolume: Boolean get() = type != SetType.WARMUP

    /** Total volume = weight × reps (Warm-up excluded) */
    val volume: Double get() = if (countsToVolume) weightKg * reps else 0.0
}
