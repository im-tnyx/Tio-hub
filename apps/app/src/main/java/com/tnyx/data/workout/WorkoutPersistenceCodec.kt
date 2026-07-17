package com.tnyx.data.workout

import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.WorkoutEngineState
import com.tnyx.shared.workout.domain.model.WorkoutMutation
import com.tnyx.shared.workout.domain.model.WorkoutRoutine
import com.tnyx.shared.workout.domain.model.WorkoutSession
import kotlinx.serialization.json.Json

class WorkoutPersistenceCodec {
    private val json = Json {
        classDiscriminator = "type"
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = false
    }

    fun encodeEngineState(value: WorkoutEngineState): String =
        json.encodeToString(WorkoutEngineState.serializer(), value)

    fun decodeEngineState(value: String): WorkoutEngineState =
        json.decodeFromString(WorkoutEngineState.serializer(), value)

    fun encodeMutation(value: WorkoutMutation): String =
        json.encodeToString(WorkoutMutation.serializer(), value)

    fun decodeMutation(value: String): WorkoutMutation =
        json.decodeFromString(WorkoutMutation.serializer(), value)

    fun encodeSession(value: WorkoutSession): String =
        json.encodeToString(WorkoutSession.serializer(), value)

    fun decodeSession(value: String): WorkoutSession =
        json.decodeFromString(WorkoutSession.serializer(), value)

    fun encodeExerciseDefinition(value: ExerciseDefinition): String =
        json.encodeToString(ExerciseDefinition.serializer(), value)

    fun decodeExerciseDefinition(value: String): ExerciseDefinition =
        json.decodeFromString(ExerciseDefinition.serializer(), value)

    fun encodeRoutine(value: WorkoutRoutine): String =
        json.encodeToString(WorkoutRoutine.serializer(), value)

    fun decodeRoutine(value: String): WorkoutRoutine =
        json.decodeFromString(WorkoutRoutine.serializer(), value)
}
