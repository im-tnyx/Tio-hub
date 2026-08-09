package com.tnyx.data.workout

import androidx.room.withTransaction
import com.tnyx.data.workout.local.WorkoutDao
import com.tnyx.data.workout.local.WorkoutDatabase
import com.tnyx.features.workout.domain.repository.CustomExerciseMediaUpdate
import com.tnyx.features.workout.domain.repository.ExerciseCatalogRepository
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import com.tnyx.shared.workout.domain.catalog.ExerciseCatalogParser
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseMediaAsset
import com.tnyx.shared.workout.domain.model.ExerciseMediaReleaseStatus
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import com.tnyx.shared.workout.domain.model.ExerciseTrackingType
import com.tnyx.shared.workout.domain.model.WORKOUT_CONTRACT_VERSION
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.createOrContinueUpload
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import io.ktor.http.ContentType
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes

private const val EXERCISE_MEDIA_BUCKET = "tio-exercise-media"
private const val MAX_EXERCISE_IMAGE_BYTES = 10L * 1024 * 1024
private const val MAX_EXERCISE_VIDEO_BYTES = 50L * 1024 * 1024
private const val RESUMABLE_UPLOAD_THRESHOLD_BYTES = 6L * 1024 * 1024

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class SupabaseExerciseCatalogRepository @Inject constructor(
    private val database: WorkoutDatabase,
    private val dao: WorkoutDao,
    private val codec: WorkoutPersistenceCodec,
    private val sessionProvider: AuthSessionProvider,
    private val supabaseClient: SupabaseClient,
) : ExerciseCatalogRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bundledCatalog = ExerciseCatalogParser.loadFromResources()
    private val ownerUserFlow = sessionProvider.observeSession()
        .map { session -> session?.userId?.takeIf(::isUuid) }
        .distinctUntilChanged()

    private val customCatalog = ownerUserFlow.flatMapLatest { ownerUserId ->
        if (ownerUserId == null) {
            flowOf(emptyList())
        } else {
            dao.observeCustomExerciseDefinitions(ownerUserId)
                .map { entities -> entities.map { codec.decodeExerciseDefinition(it.definitionJson) } }
        }
    }

    init {
        repositoryScope.launch {
            ownerUserFlow.collectLatest { ownerUserId ->
                if (ownerUserId != null) {
                    runCatching { syncRemoteCustomExercises(ownerUserId) }
                }
            }
        }
    }

    override fun getExercises(): Flow<List<ExerciseDefinition>> {
        return customCatalog.map(::mergeCatalogs)
    }

    override fun searchExercises(query: String, muscleGroupFilter: String): Flow<List<ExerciseDefinition>> {
        return getExercises().map { catalog ->
            catalog.filter { exercise ->
                val matchesQuery = query.isBlank() ||
                    exercise.name.contains(query, ignoreCase = true) ||
                    exercise.bodyPart?.contains(query, ignoreCase = true) == true ||
                    exercise.primaryMuscleGroups.any { it.contains(query, ignoreCase = true) } ||
                    exercise.secondaryMuscleGroups.any { it.contains(query, ignoreCase = true) } ||
                    exercise.equipment.any { it.contains(query, ignoreCase = true) } ||
                    exercise.aliases.any { it.contains(query, ignoreCase = true) }

                val matchesMuscle = muscleGroupFilter.equals("ALL", ignoreCase = true) ||
                    exercise.bodyPart.equals(muscleGroupFilter, ignoreCase = true) ||
                    exercise.primaryMuscleGroups.any { it.equals(muscleGroupFilter, ignoreCase = true) }

                matchesQuery && matchesMuscle
            }
        }
    }

    override suspend fun getExerciseById(exerciseId: String): ExerciseDefinition? {
        val cleanId = exerciseId.trim()

        bundledCatalog.firstOrNull { exercise -> exercise.id.equals(cleanId, ignoreCase = true) }?.let {
            return it
        }

        val ownerUserId = currentUserId()
        if (ownerUserId != null) {
            runCatching {
                dao.getCustomExerciseDefinition(ownerUserId, cleanId)?.let { entity ->
                    return codec.decodeExerciseDefinition(entity.definitionJson).copy(isCustom = true)
                }

                val remoteRow = supabaseClient.from("custom_exercises").select {
                    filter {
                        eq("user_id", ownerUserId)
                        eq("id", cleanId)
                    }
                }.decodeSingleOrNull<CustomExerciseRowDto>()

                if (remoteRow != null) {
                    val exercise = remoteRow.toDomain()
                    dao.upsertCustomExerciseDefinitions(
                        listOf(
                            exercise.toCustomCacheEntity(
                                ownerUserId = ownerUserId,
                                codec = codec,
                                syncedAtMs = System.currentTimeMillis(),
                            )
                        )
                    )
                    return exercise
                }
            }
        }

        return null
    }

    override suspend fun saveCustomExercise(
        exercise: ExerciseDefinition,
        mediaUpdate: CustomExerciseMediaUpdate,
    ) {
        val ownerUserId = requireUserId()
        val previousObjectPaths = exercise.mediaAssets.flatMap { asset ->
            listOfNotNull(asset.imageRef, asset.videoRef, asset.thumbnailRef)
        }.mapNotNull(String::toExerciseMediaObjectPath)
        var uploadedObjectPath: String? = null

        val customExercise = when (mediaUpdate) {
            CustomExerciseMediaUpdate.Unchanged -> exercise
            CustomExerciseMediaUpdate.Remove -> exercise.copy(mediaAssets = emptyList())
            is CustomExerciseMediaUpdate.Replace -> {
                val mediaType = requireNotNull(SUPPORTED_EXERCISE_MEDIA[mediaUpdate.mimeType.lowercase()]) {
                    "Selected exercise media type is not supported."
                }
                val mediaFile = File(mediaUpdate.localFilePath)
                require(mediaFile.isFile && mediaFile.length() > 0L) {
                    "Selected exercise media file is unavailable."
                }
                val maxBytes = if (mediaType.isVideo) {
                    MAX_EXERCISE_VIDEO_BYTES
                } else {
                    MAX_EXERCISE_IMAGE_BYTES
                }
                require(mediaFile.length() <= maxBytes) {
                    if (mediaType.isVideo) {
                        "Exercise video is too large. Maximum size is 50 MB."
                    } else {
                        "Exercise image is too large. Maximum size is 10 MB."
                    }
                }

                val objectPath = exerciseMediaObjectPath(
                    ownerUserId = ownerUserId,
                    exerciseId = exercise.id,
                    mediaFile = mediaFile,
                    extension = mediaType.extension,
                )
                val bucket = supabaseClient.storage.from(EXERCISE_MEDIA_BUCKET)
                bucket.uploadExerciseMedia(
                    objectPath = objectPath,
                    mediaFile = mediaFile,
                    contentType = ContentType.parse(mediaUpdate.mimeType),
                )
                uploadedObjectPath = objectPath
                val publicUrl = bucket.publicUrl(objectPath)

                exercise.copy(
                    mediaAssets = listOf(
                        ExerciseMediaAsset(
                            id = "${exercise.id}_custom_media",
                            variant = ExerciseMediaVariant.NEUTRAL,
                            imageRef = publicUrl.takeUnless { mediaType.isVideo },
                            videoRef = publicUrl.takeIf { mediaType.isVideo },
                            provenanceId = "user-generated",
                            releaseStatus = ExerciseMediaReleaseStatus.APPROVED,
                        )
                    )
                )
            }
        }.copy(isCustom = true)
        val row = customExercise.toCustomExerciseRow(ownerUserId)

        try {
            supabaseClient.from("custom_exercises").upsert(row) {
                onConflict = "id"
            }
        } catch (error: Exception) {
            uploadedObjectPath?.let { path ->
                runCatching { supabaseClient.storage.from(EXERCISE_MEDIA_BUCKET).delete(path) }
            }
            throw error
        }

        dao.upsertCustomExerciseDefinitions(
            listOf(
                customExercise.toCustomCacheEntity(
                    ownerUserId = ownerUserId,
                    codec = codec,
                    syncedAtMs = System.currentTimeMillis(),
                )
            )
        )

        if (mediaUpdate != CustomExerciseMediaUpdate.Unchanged) {
            val activeObjectPath = uploadedObjectPath
            previousObjectPaths
                .filterNot { path -> path == activeObjectPath }
                .forEach { path ->
                    runCatching { supabaseClient.storage.from(EXERCISE_MEDIA_BUCKET).delete(path) }
                }
        }
    }

    override suspend fun deleteCustomExercise(exerciseId: String) {
        val ownerUserId = requireUserId()
        val cachedExercise = dao.getCustomExerciseDefinition(ownerUserId, exerciseId)
            ?.let { entity -> codec.decodeExerciseDefinition(entity.definitionJson) }
        val exercise = cachedExercise ?: supabaseClient.from("custom_exercises").select {
            filter {
                eq("user_id", ownerUserId)
                eq("id", exerciseId)
            }
        }.decodeSingleOrNull<CustomExerciseRowDto>()?.toDomain()
        val mediaObjectPaths = exercise
            ?.mediaAssets
            .orEmpty()
            .flatMap { asset ->
                listOfNotNull(asset.imageRef, asset.videoRef, asset.thumbnailRef)
            }
            .mapNotNull(String::toExerciseMediaObjectPath)

        supabaseClient.from("custom_exercises").delete {
            filter {
                eq("user_id", ownerUserId)
                eq("id", exerciseId)
            }
        }

        dao.deleteCustomExerciseDefinition(ownerUserId, exerciseId)
        mediaObjectPaths.forEach { path ->
            runCatching { supabaseClient.storage.from(EXERCISE_MEDIA_BUCKET).delete(path) }
        }
    }

    private suspend fun syncRemoteCustomExercises(ownerUserId: String) {
        val remoteRows = supabaseClient.from("custom_exercises").select {
            filter { eq("user_id", ownerUserId) }
        }.decodeList<CustomExerciseRowDto>()

        val syncedAtMs = System.currentTimeMillis()
        val entities = remoteRows.map { row ->
            row.toDomain().toCustomCacheEntity(
                ownerUserId = ownerUserId,
                codec = codec,
                syncedAtMs = syncedAtMs,
            )
        }

        database.withTransaction {
            dao.deleteCustomExerciseDefinitionsForOwner(ownerUserId)
            if (entities.isNotEmpty()) {
                dao.upsertCustomExerciseDefinitions(entities)
            }
        }
    }

    private fun mergeCatalogs(customExercises: List<ExerciseDefinition>): List<ExerciseDefinition> {
        return buildMap {
            bundledCatalog.forEach { exercise -> put(exercise.id, exercise) }
            customExercises.forEach { exercise -> put(exercise.id, exercise) }
        }.values.sortedBy { it.name.lowercase() }
    }

    private fun requireUserId(): String {
        return currentUserId() ?: error("A signed-in Supabase user is required to save custom exercises")
    }

    private fun currentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id?.takeIf(::isUuid)
            ?: sessionProvider.currentSession()?.userId?.takeIf(::isUuid)
    }

    private fun isUuid(value: String): Boolean {
        return runCatching { java.util.UUID.fromString(value) }.isSuccess
    }

    private fun exerciseMediaObjectPath(
        ownerUserId: String,
        exerciseId: String,
        mediaFile: File,
        extension: String,
    ): String {
        val sourceFingerprint = UUID.nameUUIDFromBytes(
            "${mediaFile.absolutePath}:${mediaFile.length()}:${mediaFile.lastModified()}".toByteArray()
        )
        return "$ownerUserId/$exerciseId/$sourceFingerprint.$extension"
    }
}

private suspend fun BucketApi.uploadExerciseMedia(
    objectPath: String,
    mediaFile: File,
    contentType: ContentType,
) {
    if (mediaFile.length() <= RESUMABLE_UPLOAD_THRESHOLD_BYTES) {
        upload(objectPath, mediaFile) {
            this.contentType = contentType
        }
        return
    }

    val upload = resumable.createOrContinueUpload(objectPath, mediaFile) {
        this.contentType = contentType
    }
    try {
        upload.startOrResumeUploading()
        withTimeout(10.minutes) {
            upload.stateFlow.first { state -> state.isDone }
        }
    } catch (error: Exception) {
        upload.pause()
        throw error
    }
}

private data class ExerciseMediaType(
    val extension: String,
    val isVideo: Boolean,
)

private val SUPPORTED_EXERCISE_MEDIA = mapOf(
    "image/jpeg" to ExerciseMediaType(extension = "jpg", isVideo = false),
    "image/png" to ExerciseMediaType(extension = "png", isVideo = false),
    "image/gif" to ExerciseMediaType(extension = "gif", isVideo = false),
    "video/mp4" to ExerciseMediaType(extension = "mp4", isVideo = true),
    "video/webm" to ExerciseMediaType(extension = "webm", isVideo = true),
    "video/quicktime" to ExerciseMediaType(extension = "mov", isVideo = true),
    "video/x-m4v" to ExerciseMediaType(extension = "m4v", isVideo = true),
    "video/3gpp" to ExerciseMediaType(extension = "3gp", isVideo = true),
)

private fun String.toExerciseMediaObjectPath(): String? {
    val marker = "/storage/v1/object/public/$EXERCISE_MEDIA_BUCKET/"
    val encodedPath = substringAfter(marker, missingDelimiterValue = "")
        .substringBefore('?')
        .takeIf(String::isNotBlank)
        ?: return null
    return URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())
}

@Serializable
private data class CustomExerciseRowDto(
    val id: String,
    val user_id: String,
    val name: String,
    val schema_version: Int = WORKOUT_CONTRACT_VERSION,
    val body_part: String? = null,
    val aliases: List<String> = emptyList(),
    val primary_muscle_groups: List<String> = emptyList(),
    val secondary_muscle_groups: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val media_assets: List<ExerciseMediaAsset> = emptyList(),
    val tracking_type: String = ExerciseTrackingType.WEIGHT_REPS.name,
) {
    fun toDomain(): ExerciseDefinition {
        return ExerciseDefinition(
            id = id,
            name = name,
            schemaVersion = schema_version,
            bodyPart = body_part,
            aliases = aliases,
            primaryMuscleGroups = primary_muscle_groups,
            secondaryMuscleGroups = secondary_muscle_groups,
            equipment = equipment,
            instructions = instructions,
            mediaAssets = media_assets,
            trackingType = tracking_type.toTrackingType(),
            isCustom = true,
        )
    }
}

private fun ExerciseDefinition.toCustomExerciseRow(userId: String): CustomExerciseRowDto {
    return CustomExerciseRowDto(
        id = id,
        user_id = userId,
        name = name,
        schema_version = schemaVersion,
        body_part = bodyPart,
        aliases = aliases,
        primary_muscle_groups = primaryMuscleGroups,
        secondary_muscle_groups = secondaryMuscleGroups,
        equipment = equipment,
        instructions = instructions,
        media_assets = mediaAssets,
        tracking_type = trackingType.name,
    )
}

private fun String.toTrackingType(): ExerciseTrackingType {
    return runCatching { ExerciseTrackingType.valueOf(this) }
        .getOrDefault(ExerciseTrackingType.WEIGHT_REPS)
}
