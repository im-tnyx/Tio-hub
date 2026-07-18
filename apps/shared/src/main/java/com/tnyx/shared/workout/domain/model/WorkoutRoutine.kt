package com.tnyx.shared.workout.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RepRange(
    val min: Int = 8,
    val max: Int = 12
)
@Serializable
data class WorkoutRoutine(
    val id: String,
    val name: String,
    val schemaVersion: Int = WORKOUT_CONTRACT_VERSION,
    val description: String = "",
    val exercises: List<RoutineExercise> = emptyList(),
    val lastPerformedAtMs: Long? = null
)

@Serializable
data class RoutineExercise(
    val id: String,
    val exerciseId: String,
    val exerciseNameSnapshot: String,
    val order: Int,
    val plannedSets: Int,
    val trackingTypeSnapshot: ExerciseTrackingType = ExerciseTrackingType.WEIGHT_REPS,
    val targetReps: RepRange = RepRange(),
    val restSeconds: Int = 90,
    val setType: SetType = SetType.NORMAL,
    val supersetWithEntryId: String? = null
)
