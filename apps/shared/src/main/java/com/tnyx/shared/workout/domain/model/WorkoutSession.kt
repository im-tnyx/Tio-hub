package com.tnyx.shared.workout.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Workout Session — active workout का complete data।
 * Phone App + Watch App दोनों इसे sync करते हैं।
 */
@Serializable
data class WorkoutSession(
    val id: String,
    val routineId: String?,
    val routineName: String,
    val startTimeMs: Long,
    val endTimeMs: Long? = null,
    val sets: List<WorkoutSet> = emptyList(),
    val notes: String = ""
) {
    val isActive: Boolean get() = endTimeMs == null
    val durationMs: Long get() = (endTimeMs ?: System.currentTimeMillis()) - startTimeMs

    /** Total volume across all non-warmup sets */
    val totalVolumeKg: Double get() = sets.sumOf { it.volume }

    /** Completed sets count */
    val completedSets: Int get() = sets.count { it.isCompleted }

    /** Total sets count (excluding warm-ups) */
    val workingSets: Int get() = sets.count { it.countsToVolume }
}

/**
 * Routine — pre-built workout plan।
 * Phone से Watch पर sync होता है।
 */
@Serializable
data class WorkoutRoutine(
    val id: String,
    val name: String,
    val description: String = "",
    val exercises: List<RoutineExercise> = emptyList(),
    val lastPerformedMs: Long? = null
)

@Serializable
data class RoutineExercise(
    val id: String,
    val exerciseId: String,
    val exerciseName: String,
    val muscleGroup: String,
    val plannedSets: Int,
    @Serializable(with = IntRangeSerializer::class)
    val targetReps: IntRange = 8..12,
    val restSeconds: Int = 90,
    val setType: SetType = SetType.NORMAL,
    val supersetWithId: String? = null  // null = not a superset
)

/**
 * Custom Serializer for IntRange.
 * "8..12" format में serialize करता है।
 */
object IntRangeSerializer : KSerializer<IntRange> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntRange", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: IntRange) {
        encoder.encodeString("${value.first}..${value.last}")
    }

    override fun deserialize(decoder: Decoder): IntRange {
        val string = decoder.decodeString()
        val parts = string.split("..")
        return if (parts.size == 2) {
            parts[0].toInt()..parts[1].toInt()
        } else {
            val single = string.toInt()
            single..single
        }
    }
}
