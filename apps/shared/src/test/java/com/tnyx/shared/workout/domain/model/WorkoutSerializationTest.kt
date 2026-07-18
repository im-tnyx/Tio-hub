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
            trackingType = ExerciseTrackingType.WEIGHT_REPS,
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
        assertEquals(ExerciseTrackingType.WEIGHT_REPS, decoded.trackingType)
        assertEquals("exercise-1", decoded.id)
    }

    @Test
    fun legacyExerciseDefinitionDefaultsToWeightAndRepsTracking() {
        val decoded = json.decodeFromString<ExerciseDefinition>(
            """{"id":"exercise-legacy","name":"Legacy exercise"}"""
        )

        assertEquals(ExerciseTrackingType.WEIGHT_REPS, decoded.trackingType)
    }

    @Test
    fun sessionExerciseRoundTripsTrackingSnapshot() {
        val exercise = WorkoutExercise(
            id = "entry-1",
            exerciseId = "exercise-1",
            exerciseNameSnapshot = "Bodyweight Squat",
            order = 0,
            trackingTypeSnapshot = ExerciseTrackingType.BODYWEIGHT_REPS
        )

        assertEquals(exercise, json.decodeFromString<WorkoutExercise>(json.encodeToString(exercise)))
    }

    @Test
    fun stepBasedSetRoundTripsAsADurableMetric() {
        val set = WorkoutSet(
            id = "set-steps",
            exerciseEntryId = "entry-steps",
            setNumber = 1,
            steps = 500,
            durationSeconds = 300,
            isCompleted = true,
            completedAtMs = 2_000L
        )

        val decoded = json.decodeFromString<WorkoutSet>(json.encodeToString(set))

        assertEquals(set, decoded)
        assertTrue(decoded.hasRecordedMetric)
    }
}
