package com.tnyx.data.profile

import com.tnyx.shared.profile.domain.model.MembershipTier
import com.tnyx.shared.profile.domain.model.ProfileJourney
import com.tnyx.shared.profile.domain.model.ProfileWorkoutChart
import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

private const val AVATAR_BUCKET = "profile-avatars"
private const val AVATAR_FILE_NAME = "avatar.jpg"

@Serializable
data class ProfileDto(
    val id: String,
    val display_name: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val plan_label: String? = null,
    val avatar_url: String? = null,
    val weight: Double? = null,
    val height: Int? = null,
    val bmi: Double? = null,
    val bmr: Int? = null,
    val status_label: String? = null,
    val streak: Int? = null,
    val body_fat: Double? = null,
    val journey_name: String? = null,
    val journey_initial_weight: Double? = null,
    val journey_target_weight: Double? = null,
    val journey_progress: Float? = null,
    val progress_photos: List<String>? = null,
    val last_photo_update_weight: String? = null,
    val last_photo_update_date: String? = null,
    val chart_duration_minutes: List<Float>? = null,
    val chart_volume_kg: List<Float>? = null,
    val chart_reps: List<Float>? = null,
)

class SupabaseProfileRepository(
    private val supabaseClient: SupabaseClient,
) : ProfileRepository {

    override fun getCurrentProfile(): Flow<UserProfile> = flow {
        emit(getCurrentProfileDto().toDomain())
    }

    override fun getProfile(userId: String): Flow<UserProfile> = flow {
        val dto = supabaseClient.from("profiles").select {
            filter {
                eq("id", userId)
            }
        }.decodeSingle<ProfileDto>()

        emit(dto.toDomain())
    }

    override suspend fun updateProfile(profile: UserProfile) {
        supabaseClient.from("profiles").update(
            mapOf(
                "display_name" to profile.displayName,
                "dob" to profile.dob,
                "gender" to profile.gender,
                "plan_label" to profile.planLabel,
                "weight" to profile.weight,
                "height" to profile.height,
                "bmi" to profile.bmi,
                "bmr" to profile.bmr,
            ),
        ) {
            filter {
                eq("id", profile.id)
            }
        }
    }

    override suspend fun updateAvatar(jpegBytes: ByteArray): String {
        require(jpegBytes.isNotEmpty()) { "Avatar image is empty" }

        val profile = getCurrentProfileDto()
        val objectPath = avatarObjectPath(profile.id)
        val bucket = supabaseClient.storage.from(AVATAR_BUCKET)

        bucket.upload(objectPath, jpegBytes) {
            upsert = true
            contentType = ContentType.Image.JPEG
        }

        val publicUrl = bucket.publicUrl(objectPath)
        val cacheBustedUrl = "$publicUrl?v=${System.currentTimeMillis()}"

        supabaseClient.from("profiles").update(
            mapOf("avatar_url" to cacheBustedUrl),
        ) {
            filter {
                eq("id", profile.id)
            }
        }

        return cacheBustedUrl
    }

    override suspend fun removeAvatar() {
        val profile = getCurrentProfileDto()
        val objectPath = avatarObjectPath(profile.id)
        val bucket = supabaseClient.storage.from(AVATAR_BUCKET)

        runCatching {
            bucket.delete(objectPath)
        }

        supabaseClient.from("profiles").update(
            mapOf<String, Any?>("avatar_url" to null),
        ) {
            filter {
                eq("id", profile.id)
            }
        }
    }

    private suspend fun getCurrentProfileDto(): ProfileDto {
        return supabaseClient.from("profiles").select {
            limit(count = 1)
        }.decodeSingle()
    }

    private fun avatarObjectPath(profileId: String): String {
        return "$profileId/$AVATAR_FILE_NAME"
    }

    private fun ProfileDto.toDomain(): UserProfile {
        return UserProfile(
            id = id,
            displayName = display_name.orEmpty(),
            dob = dob.orEmpty(),
            gender = gender.orEmpty(),
            planLabel = plan_label.orEmpty(),
            weight = weight ?: 0.0,
            height = height ?: 0,
            bmi = bmi ?: 0.0,
            bmr = bmr ?: 0,
            statusLabel = status_label.orEmpty(),
            streak = streak ?: 0,
            bodyFat = body_fat ?: 0.0,
            currentJourney = ProfileJourney(
                name = journey_name.orEmpty(),
                initialWeight = journey_initial_weight ?: 0.0,
                targetWeight = journey_target_weight ?: 0.0,
                progress = (journey_progress ?: 0f).coerceIn(0f, 1f),
            ),
            progressPhotos = progress_photos.orEmpty(),
            lastPhotoUpdateWeight = last_photo_update_weight.orEmpty(),
            lastPhotoUpdateDate = last_photo_update_date.orEmpty(),
            workoutChart = ProfileWorkoutChart(
                durationMinutes = chart_duration_minutes.orEmpty(),
                volumeKg = chart_volume_kg.orEmpty(),
                reps = chart_reps.orEmpty(),
            ),
            avatarUrl = avatar_url?.takeIf(String::isNotBlank),
            membershipTier = MembershipTier.fromPlanLabel(plan_label),
        )
    }
}
