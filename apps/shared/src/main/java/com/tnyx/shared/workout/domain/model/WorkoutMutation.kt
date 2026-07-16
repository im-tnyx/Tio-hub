package com.tnyx.shared.workout.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class WorkoutMutationOrigin {
    PHONE,
    WEAR
}

/** Durable mutation envelope. Idempotency is enforced by the persistence owner. */
@Serializable
data class WorkoutMutation(
    val mutationId: String,
    val sessionId: String,
    val origin: WorkoutMutationOrigin,
    val originDeviceId: String,
    val originSequence: Long,
    val occurredAtMs: Long,
    val payload: WorkoutMutationPayload,
    val schemaVersion: Int = WORKOUT_CONTRACT_VERSION
)

@Serializable
sealed interface WorkoutMutationPayload

@Serializable
@SerialName("session_started")
data class SessionStarted(
    val startedAtMs: Long,
    val routineId: String? = null,
    val routineName: String = "",
    val initialExercises: List<WorkoutExercise> = emptyList()
) : WorkoutMutationPayload

@Serializable
@SerialName("exercise_added")
data class ExerciseAdded(
    val exercise: WorkoutExercise,
    val atIndex: Int? = null
) : WorkoutMutationPayload

@Serializable
@SerialName("exercise_removed")
data class ExerciseRemoved(
    val exerciseEntryId: String
) : WorkoutMutationPayload

@Serializable
@SerialName("exercise_reordered")
data class ExerciseReordered(
    val exerciseEntryId: String,
    val toIndex: Int
) : WorkoutMutationPayload

@Serializable
@SerialName("exercise_skip_changed")
data class ExerciseSkipChanged(
    val exerciseEntryId: String,
    val isSkipped: Boolean
) : WorkoutMutationPayload

@Serializable
@SerialName("set_upserted")
data class SetUpserted(
    val exerciseEntryId: String,
    val set: WorkoutSet
) : WorkoutMutationPayload

@Serializable
@SerialName("set_removed")
data class SetRemoved(
    val exerciseEntryId: String,
    val setId: String
) : WorkoutMutationPayload

@Serializable
@SerialName("session_notes_updated")
data class SessionNotesUpdated(
    val notes: String
) : WorkoutMutationPayload

@Serializable
@SerialName("rest_timer_started")
data class RestTimerStarted(
    val exerciseEntryId: String,
    val setId: String? = null,
    val durationSeconds: Int
) : WorkoutMutationPayload

@Serializable
@SerialName("rest_timer_adjusted")
data class RestTimerAdjusted(
    val deltaSeconds: Int
) : WorkoutMutationPayload

@Serializable
@SerialName("rest_timer_stopped")
data object RestTimerStopped : WorkoutMutationPayload

@Serializable
@SerialName("session_finished")
data class SessionFinished(
    val endedAtMs: Long
) : WorkoutMutationPayload

@Serializable
@SerialName("session_discarded")
data class SessionDiscarded(
    val endedAtMs: Long
) : WorkoutMutationPayload
