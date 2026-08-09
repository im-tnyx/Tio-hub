package com.tnyx.shared.workout.domain.catalog

import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseMediaAsset
import com.tnyx.shared.workout.domain.model.ExerciseMediaReleaseStatus
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import com.tnyx.shared.workout.domain.model.ExerciseTrackingType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ExerciseMediaVariantDto(
    @SerialName("videoUrl") val videoUrl: String? = null,
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("imageUrl") val imageUrl: String? = null
)

@Serializable
data class ExerciseMediaGroupDto(
    val type: String? = null,
    val defaultGender: String? = null,
    val male: ExerciseMediaVariantDto? = null,
    val female: ExerciseMediaVariantDto? = null
)

/**
 * Raw DTO matching `apps/shared/exerciseData.json`.
 * Supports both legacy flat media keys and future structured nested `media` objects.
 */
@Serializable
data class ExerciseCatalogDto(
    val id: String,
    val title: String,
    val priority: Int = 0,
    @SerialName("muscle_group")
    val muscleGroup: String = "",
    @SerialName("other_muscles")
    val otherMuscles: List<String> = emptyList(),
    @SerialName("exercise_type")
    val exerciseType: String = "",
    @SerialName("equipment_category")
    val equipmentCategory: String = "",
    @SerialName("is_custom")
    val isCustom: Boolean = false,
    @SerialName("is_archived")
    val isArchived: Boolean = false,
    val url: String? = null,
    @SerialName("url_female")
    val urlFemale: String? = null,
    @SerialName("media_type")
    val mediaType: String? = null,
    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerialName("thumbnail_url_female")
    val thumbnailUrlFemale: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("image_url_female")
    val imageUrlFemale: String? = null,
    val media: ExerciseMediaGroupDto? = null,
    @SerialName("manual_tag")
    val manualTag: String? = null,
    @SerialName("volume_doubling_supported")
    val volumeDoublingSupported: Boolean = false,
    @SerialName("hundred_percent_bodyweight_exercise")
    val hundredPercentBodyweightExercise: Boolean = false,
    @SerialName("comparable_exercise")
    val comparableExercise: Boolean = false,
    @SerialName("leaderboard_exercise")
    val leaderboardExercise: Boolean = false,
    @SerialName("strength_level_exercise")
    val strengthLevelExercise: Boolean = false,
    val instructions: String? = null,
    val goal: List<String> = emptyList(),
    val level: List<String> = emptyList(),
    val category: String? = null,
    @SerialName("granular_equipments")
    val granularEquipments: List<String> = emptyList()
) {
    fun toDomain(): ExerciseDefinition {
        val mediaList = mutableListOf<ExerciseMediaAsset>()

        // Male media asset resolution with nested & flat fallback chain
        val maleVideo = media?.male?.videoUrl ?: url
        val maleThumb = media?.male?.thumbnailUrl ?: thumbnailUrl
        val maleImage = media?.male?.imageUrl ?: imageUrl ?: maleThumb ?: maleVideo

        if (!maleImage.isNullOrBlank() || !maleThumb.isNullOrBlank() || !maleVideo.isNullOrBlank()) {
            mediaList.add(
                ExerciseMediaAsset(
                    id = "${id}_male",
                    variant = ExerciseMediaVariant.MALE,
                    imageRef = maleImage,
                    videoRef = maleVideo,
                    thumbnailRef = maleThumb ?: maleImage,
                    mediaVersion = 1,
                    provenanceId = "exercise_data_json",
                    releaseStatus = ExerciseMediaReleaseStatus.APPROVED
                )
            )
        }

        // Female media asset resolution with nested & flat fallback chain
        val femaleVideo = media?.female?.videoUrl ?: urlFemale
        val femaleThumb = media?.female?.thumbnailUrl ?: thumbnailUrlFemale
        val femaleImage = media?.female?.imageUrl ?: imageUrlFemale ?: femaleThumb ?: femaleVideo

        if (!femaleImage.isNullOrBlank() || !femaleThumb.isNullOrBlank() || !femaleVideo.isNullOrBlank()) {
            mediaList.add(
                ExerciseMediaAsset(
                    id = "${id}_female",
                    variant = ExerciseMediaVariant.FEMALE,
                    imageRef = femaleImage,
                    videoRef = femaleVideo,
                    thumbnailRef = femaleThumb ?: femaleImage,
                    mediaVersion = 1,
                    provenanceId = "exercise_data_json",
                    releaseStatus = ExerciseMediaReleaseStatus.APPROVED
                )
            )
        }

        val trackingEnum = when (exerciseType.lowercase()) {
            "weight_reps" -> ExerciseTrackingType.WEIGHT_REPS
            "reps_only", "bodyweight_reps" -> ExerciseTrackingType.BODYWEIGHT_REPS
            "assisted_bodyweight_reps" -> ExerciseTrackingType.ASSISTED_BODYWEIGHT_REPS
            "duration" -> ExerciseTrackingType.DURATION
            "distance_duration" -> ExerciseTrackingType.DISTANCE_DURATION
            "steps_duration" -> ExerciseTrackingType.STEPS_DURATION
            else -> ExerciseTrackingType.WEIGHT_REPS
        }

        val instructionList = instructions
            ?.lines()
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        return ExerciseDefinition(
            id = id,
            name = title,
            bodyPart = muscleGroup.takeIf { it.isNotBlank() },
            primaryMuscleGroups = if (muscleGroup.isNotBlank()) listOf(muscleGroup) else emptyList(),
            secondaryMuscleGroups = otherMuscles,
            equipment = if (equipmentCategory.isNotBlank()) listOf(equipmentCategory) else emptyList(),
            instructions = instructionList,
            mediaAssets = mediaList,
            trackingType = trackingEnum
        )
    }
}

object ExerciseCatalogParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parseJson(jsonString: String): List<ExerciseDefinition> {
        val dtos: List<ExerciseCatalogDto> = json.decodeFromString(jsonString)
        return dtos.filter { !it.isArchived }.map { it.toDomain() }
    }

    fun loadFromResources(): List<ExerciseDefinition> {
        val stream = ExerciseCatalogParser::class.java.getResourceAsStream("/exerciseData.json")
            ?: return emptyList()
        val text = stream.bufferedReader().use { it.readText() }
        return parseJson(text)
    }
}
