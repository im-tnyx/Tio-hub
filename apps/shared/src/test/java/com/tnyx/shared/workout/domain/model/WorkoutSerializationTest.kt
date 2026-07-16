package com.tnyx.shared.workout.domain.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkoutSerializationTest {
    private val json = Json {
        classDiscriminator = "mutationType"
        encodeDefaults = true
    }

    @Test
    fun mutationEnvelopeRoundTripsWithStablePayloadType() {
        val mutation = WorkoutMutation(
            mutationId = "mutation-1",
            sessionId = "session-1",
            origin = WorkoutMutationOrigin.WEAR,
            originDeviceId = "wear-device",
            originSequence = 7,
            occurredAtMs = 10_000L,
            payload = SessionStarted(
                startedAtMs = 10_000L,
                routineId = "routine-1",
                routineName = "Push Day",
                initialExercises = listOf(
                    WorkoutExercise(
                        id = "entry-1",
                        exerciseId = "exercise-1",
                        exerciseNameSnapshot = "Bench Press",
                        order = 0
                    )
                )
            )
        )

        val encoded = json.encodeToString(mutation)
        val decoded = json.decodeFromString<WorkoutMutation>(encoded)

        assertEquals(mutation, decoded)
        assertTrue(encoded.contains("\"mutationType\":\"session_started\""))
        assertTrue(encoded.contains("\"schemaVersion\":$WORKOUT_CONTRACT_VERSION"))
    }

    @Test
    fun exerciseDefinitionRoundTripsWithGenderMediaVariants() {
        val exercise = ExerciseDefinition(
            id = "exercise-1",
            name = "Bench Press",
            mediaAssets = listOf(
                ExerciseMediaAsset(
                    id = "exercise-1-female-v1",
                    variant = ExerciseMediaVariant.FEMALE,
                    imageRef = "media/exercise-1/female/v1/image",
                    videoRef = "media/exercise-1/female/v1/video",
                    mediaVersion = 1,
                    provenanceId = "provenance-1",
                    releaseStatus = ExerciseMediaReleaseStatus.APPROVED
                )
            )
        )

        val encoded = json.encodeToString(exercise)
        val decoded = json.decodeFromString<ExerciseDefinition>(encoded)

        assertEquals(exercise, decoded)
        assertEquals(ExerciseMediaVariant.FEMALE, decoded.mediaAssets.single().variant)
        assertEquals("exercise-1", decoded.id)
    }
}
