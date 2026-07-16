package com.tnyx.shared.workout.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class WorkoutSessionStatus {
    ACTIVE,
    COMPLETED,
    DISCARDED
}
/** A stable exercise instance inside one workout session. */
@Serializable
data class WorkoutExercise(
    val id: String,
    val exerciseId: String,
    val exerciseNameSnapshot: String,
    val order: Int,
    val restSeconds: Int = 90,
    val sets: List<WorkoutSet> = emptyList(),
    val notes: String = "",
    val isSkipped: Boolean = false
)

@Serializable
data class WorkoutSession(
    val id: String,
    val startedAtMs: Long,
    val schemaVersion: Int = WORKOUT_CONTRACT_VERSION,
    val routineId: String? = null,
    val routineName: String = "",
    val endedAtMs: Long? = null,
    val status: WorkoutSessionStatus = WorkoutSessionStatus.ACTIVE,
    val exercises: List<WorkoutExercise> = emptyList(),
    val notes: String = "",
    val revision: Long = 0L
) {
    val isActive: Boolean
        get() = status == WorkoutSessionStatus.ACTIVE && endedAtMs == null

    val sets: List<WorkoutSet>
        get() = exercises.flatMap { it.sets }

    fun durationMs(currentTimeMs: Long): Long =
        ((endedAtMs ?: currentTimeMs) - startedAtMs).coerceAtLeast(0L)

    val totalVolumeKg: Double
        get() = sets.sumOf { it.volumeKg }

    val completedSets: Int
        get() = sets.count { it.isCompleted }

    val workingSets: Int
        get() = sets.count { it.countsToVolume }
}
