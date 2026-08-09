package com.tnyx.data.workout.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

const val WORKOUT_ENGINE_STATE_SINGLETON_ID: Int = 1

@Entity(tableName = "workout_engine_state")
data class WorkoutEngineStateEntity(
    @PrimaryKey
    val singletonId: Int = WORKOUT_ENGINE_STATE_SINGLETON_ID,
    val sessionId: String?,
    val sessionStatus: String?,
    val sessionRevision: Long?,
    val contractVersion: Int,
    val stateJson: String,
    val updatedAtMs: Long
)

@Entity(
    tableName = "workout_mutation_outbox",
    indices = [
        Index(value = ["sessionId"]),
        Index(
            value = ["origin", "originDeviceId", "originSequence"],
            unique = true
        )
    ]
)
data class WorkoutMutationOutboxEntity(
    @PrimaryKey
    val mutationId: String,
    val sessionId: String,
    val origin: String,
    val originDeviceId: String,
    val originSequence: Long,
    val occurredAtMs: Long,
    val contractVersion: Int,
    val resultingRevision: Long,
    val mutationJson: String,
    val deliveryStatus: String,
    val createdAtMs: Long
)

@Entity(
    tableName = "workout_session_history",
    indices = [Index(value = ["endedAtMs"])]
)
data class WorkoutSessionHistoryEntity(
    @PrimaryKey
    val sessionId: String,
    val status: String,
    val startedAtMs: Long,
    val endedAtMs: Long,
    val revision: Long,
    val contractVersion: Int,
    val sessionJson: String,
    val updatedAtMs: Long
)

@Entity(tableName = "workout_exercise_definition")
data class WorkoutExerciseDefinitionEntity(
    @PrimaryKey
    val exerciseId: String,
    val name: String,
    val contractVersion: Int,
    val definitionJson: String
)

@Entity(
    tableName = "workout_custom_exercise",
    primaryKeys = ["ownerUserId", "exerciseId"],
    indices = [Index(value = ["ownerUserId"])]
)
data class WorkoutCustomExerciseEntity(
    val ownerUserId: String,
    val exerciseId: String,
    val name: String,
    val contractVersion: Int,
    val definitionJson: String,
    val syncedAtMs: Long
)

@Entity(
    tableName = "workout_routine",
    indices = [Index(value = ["lastPerformedAtMs"])]
)
data class WorkoutRoutineEntity(
    @PrimaryKey
    val routineId: String,
    val name: String,
    val contractVersion: Int,
    val lastPerformedAtMs: Long?,
    val routineJson: String
)

enum class WorkoutOutboxDeliveryStatus {
    PENDING
}
