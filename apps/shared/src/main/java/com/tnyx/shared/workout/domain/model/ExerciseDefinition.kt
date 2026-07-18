package com.tnyx.shared.workout.domain.model

import kotlinx.serialization.Serializable

/** Determines which durable metrics an exercise records; presentation may vary by platform. */
@Serializable
enum class ExerciseTrackingType {
    WEIGHT_REPS,
    BODYWEIGHT_REPS,
    ASSISTED_BODYWEIGHT_REPS,
    DURATION,
    DISTANCE_DURATION,
    STEPS_DURATION
}

/** Canonical exercise identity. Media variants never create a second exercise identity. */
@Serializable
data class ExerciseDefinition(
    val id: String,
    val name: String,
    val schemaVersion: Int = WORKOUT_CONTRACT_VERSION,
    val aliases: List<String> = emptyList(),
    val primaryMuscleGroups: List<String> = emptyList(),
    val secondaryMuscleGroups: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val mediaAssets: List<ExerciseMediaAsset> = emptyList(),
    val trackingType: ExerciseTrackingType = ExerciseTrackingType.WEIGHT_REPS
)
