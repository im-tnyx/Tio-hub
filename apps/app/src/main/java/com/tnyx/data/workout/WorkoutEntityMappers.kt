package com.tnyx.data.workout

import com.tnyx.data.workout.local.WorkoutEngineStateEntity
import com.tnyx.data.workout.local.WorkoutExerciseDefinitionEntity
import com.tnyx.data.workout.local.WorkoutMutationOutboxEntity
import com.tnyx.data.workout.local.WorkoutOutboxDeliveryStatus
import com.tnyx.data.workout.local.WorkoutRoutineEntity
import com.tnyx.data.workout.local.WorkoutSessionHistoryEntity
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.WorkoutEngineState
import com.tnyx.shared.workout.domain.model.WorkoutMutation
import com.tnyx.shared.workout.domain.model.WorkoutRoutine
import com.tnyx.shared.workout.domain.model.WorkoutSession
import com.tnyx.shared.workout.domain.model.WorkoutSessionStatus

internal fun WorkoutEngineState.toEntity(
    codec: WorkoutPersistenceCodec,
    updatedAtMs: Long
): WorkoutEngineStateEntity = WorkoutEngineStateEntity(
    sessionId = session?.id,
    sessionStatus = session?.status?.name,
    sessionRevision = session?.revision,
    contractVersion = schemaVersion,
    stateJson = codec.encodeEngineState(this),
    updatedAtMs = updatedAtMs
)

internal fun WorkoutMutation.toOutboxEntity(
    codec: WorkoutPersistenceCodec,
    resultingState: WorkoutEngineState
): WorkoutMutationOutboxEntity = WorkoutMutationOutboxEntity(
    mutationId = mutationId,
    sessionId = sessionId,
    origin = origin.name,
    originDeviceId = originDeviceId,
    originSequence = originSequence,
    occurredAtMs = occurredAtMs,
    contractVersion = schemaVersion,
    resultingRevision = resultingState.session?.revision ?: 0L,
    mutationJson = codec.encodeMutation(this),
    deliveryStatus = WorkoutOutboxDeliveryStatus.PENDING.name,
    createdAtMs = occurredAtMs
)

internal fun WorkoutSession.toHistoryEntity(
    codec: WorkoutPersistenceCodec,
    updatedAtMs: Long
): WorkoutSessionHistoryEntity {
    require(status == WorkoutSessionStatus.COMPLETED) {
        "Only completed Workout sessions can enter history"
    }
    val completedAtMs = requireNotNull(endedAtMs) {
        "Completed Workout sessions require an end time"
    }
    return WorkoutSessionHistoryEntity(
        sessionId = id,
        status = status.name,
        startedAtMs = startedAtMs,
        endedAtMs = completedAtMs,
        revision = revision,
        contractVersion = schemaVersion,
        sessionJson = codec.encodeSession(this),
        updatedAtMs = updatedAtMs
    )
}

internal fun ExerciseDefinition.toEntity(
    codec: WorkoutPersistenceCodec
): WorkoutExerciseDefinitionEntity = WorkoutExerciseDefinitionEntity(
    exerciseId = id,
    name = name,
    contractVersion = schemaVersion,
    definitionJson = codec.encodeExerciseDefinition(this)
)

internal fun WorkoutRoutine.toEntity(
    codec: WorkoutPersistenceCodec
): WorkoutRoutineEntity = WorkoutRoutineEntity(
    routineId = id,
    name = name,
    contractVersion = schemaVersion,
    lastPerformedAtMs = lastPerformedAtMs,
    routineJson = codec.encodeRoutine(this)
)
