package com.tnyx.data.profile

import com.tnyx.shared.profile.domain.model.MembershipTier
import com.tnyx.shared.profile.domain.model.ProfileJourney
import com.tnyx.shared.profile.domain.model.ProfileWorkoutChart
import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.coroutines.coroutineContext

private const val AVATAR_BUCKET = "tio-profile"
private const val AVATAR_FILE_NAME = "avatar.jpg"
private const val PROFILE_REFRESH_INTERVAL_MS = 10_000L

@Serializable
data class ProfileRowDto(
    val id: String,
    val username: String? = null,
    val display_name: String? = null,
    val mobile: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val is_onboarded: Boolean = false,
    val plan_label: String? = null,
    val avatar_url: String? = null,
    val status_label: String? = null,
    val current_streak: Int? = null,
)

@Serializable
data class NutritionProfileRowDto(
    val weight: Double? = null,
    val height: Int? = null,
    val current_weight_kg: Double? = null,
    val height_cm: Double? = null,
    val target_weight_kg: Double? = null,
    val body_fat: Double? = null,
    val body_fat_percentage: Double? = null,
)

class SupabaseProfileRepository(
    private val supabaseClient: SupabaseClient,
    private val sessionProvider: AuthSessionProvider,
) : ProfileRepository {
    private val localProfileState = MutableStateFlow<LocalProfileSnapshot?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getCurrentProfile(): Flow<UserProfile> {
        return sessionProvider.observeSession()
            .distinctUntilChangedBy { it?.userId }
            .flatMapLatest { session ->
                val userId = session?.userId
                if (userId.isNullOrBlank()) {
                    localProfileState.value = null
                    flowOf(emptyProfile())
                } else {
                    clearLocalProfileIfOwnerChanged(userId)
                    combine(
                        localProfileState,
                        observeRemoteProfile(userId),
                    ) { local, remote ->
                        mergeProfiles(local?.takeIf { it.ownerUserId == userId }?.profile, remote)
                    }
                }
            }
            .distinctUntilChanged()
    }

    override fun getProfile(userId: String): Flow<UserProfile> {
        clearLocalProfileIfOwnerChanged(userId)
        return combine(
            localProfileState,
            observeRemoteProfile(userId),
        ) { local, remote ->
            mergeProfiles(local?.takeIf { it.ownerUserId == userId }?.profile, remote)
        }.distinctUntilChanged()
    }

    override suspend fun updateProfile(profile: UserProfile) {
        val targetUserId = requireNotNull(resolveValidUserId(profile.id)) {
            "A signed-in Supabase user is required to update a profile"
        }

        val profilePayload = buildJsonObject {
            put("id", targetUserId)
            profile.username.trim().removePrefix("@").lowercase()
                .takeIf(String::isNotBlank)
                ?.let { put("username", it) }
            profile.displayName.trim().takeIf(String::isNotBlank)
                ?.let { put("display_name", it) }
            profile.mobile.takeIf(String::isNotBlank)?.let { put("mobile", it) }
            profile.dob.trim().takeIf(String::isNotBlank)?.let { put("dob", it) }
            profile.gender.trim().takeIf(String::isNotBlank)?.let { put("gender", it) }
            profile.planLabel.trim().takeIf(String::isNotBlank)?.let { put("plan_label", it) }
            put("is_onboarded", profile.hasCompletedOnboarding)
        }
        supabaseClient.from("profiles").upsert(profilePayload) {
            onConflict = "id"
        }

        val nutritionPayload = buildJsonObject {
            put("user_id", targetUserId)
            profile.weight.takeIf { it > 0.0 }?.let { put("current_weight_kg", it) }
            profile.height.takeIf { it > 0 }?.let { put("height_cm", it) }
            profile.bodyFat.takeIf { it > 0.0 }?.let { put("body_fat_percentage", it) }
            profile.currentJourney.targetWeight.takeIf { it > 0.0 }
                ?.let { put("target_weight_kg", it) }
        }
        supabaseClient.from("user_nutrition_profiles").upsert(nutritionPayload) {
            onConflict = "user_id"
        }

        localProfileState.value = LocalProfileSnapshot(
            ownerUserId = targetUserId,
            profile = profile.copy(id = targetUserId),
        )
    }

    override suspend fun updateAvatar(jpegBytes: ByteArray): String {
        require(jpegBytes.isNotEmpty()) { "Avatar image is empty" }

        val currentUserId = currentUserId()
        val objectPath = avatarObjectPath(currentUserId)
        val bucket = supabaseClient.storage.from(AVATAR_BUCKET)

        bucket.upload(objectPath, jpegBytes) {
            upsert = true
            contentType = ContentType.Image.JPEG
        }

        val publicUrl = bucket.publicUrl(objectPath)
        val cacheBustedUrl = "$publicUrl?v=${System.currentTimeMillis()}"

        supabaseClient.from("profiles").upsert(
            buildJsonObject {
                put("id", currentUserId)
                put("avatar_url", cacheBustedUrl)
            },
        ) {
            onConflict = "id"
        }
        return cacheBustedUrl
    }

    override suspend fun removeAvatar() {
        val currentUserId = currentUserId()
        val objectPath = avatarObjectPath(currentUserId)
        val bucket = supabaseClient.storage.from(AVATAR_BUCKET)

        runCatching { bucket.delete(objectPath) }

        supabaseClient.from("profiles").upsert(
            buildJsonObject {
                put("id", currentUserId)
                put("avatar_url", JsonNull)
            },
        ) {
            onConflict = "id"
        }
    }

    private suspend fun resolveValidUserId(profileId: String?): String? {
        val remoteUserId = supabaseClient.auth.currentUserOrNull()?.id
            ?: sessionProvider.currentSession()?.userId
        if (remoteUserId != null && isUuid(remoteUserId)) {
            return remoteUserId
        }
        if (!profileId.isNullOrBlank() && profileId != "anonymous" && isUuid(profileId)) {
            return profileId
        }
        return runCatching {
            supabaseClient.auth.signInAnonymously()
            supabaseClient.auth.currentUserOrNull()?.id
        }.getOrNull()?.takeIf(::isUuid)
    }

    private fun isUuid(value: String): Boolean {
        return runCatching { java.util.UUID.fromString(value) }.isSuccess
    }

    private fun currentUserId(): String {
        return requireNotNull(
            supabaseClient.auth.currentUserOrNull()?.id
                ?: sessionProvider.currentSession()?.userId,
        ) {
            "A signed-in user is required for profile access"
        }
    }

    private fun avatarObjectPath(profileId: String): String {
        return "$profileId/$AVATAR_FILE_NAME"
    }

    private fun observeRemoteProfile(userId: String): Flow<UserProfile> = flow {
        while (coroutineContext.isActive) {
            emit(fetchProfile(userId))
            delay(PROFILE_REFRESH_INTERVAL_MS)
        }
    }

    private suspend fun fetchProfile(userId: String): UserProfile {
        val profile = runCatching {
            supabaseClient.from("profiles").select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingleOrNull<ProfileRowDto>()
        }.getOrNull() ?: ProfileRowDto(id = userId)

        val nutrition = runCatching {
            supabaseClient.from("user_nutrition_profiles").select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<NutritionProfileRowDto>().firstOrNull()
        }.getOrNull()

        return profile.toDomain(nutrition)
    }

    private fun ProfileRowDto.toDomain(nutrition: NutritionProfileRowDto?): UserProfile {
        val currentWeight = nutrition?.current_weight_kg ?: nutrition?.weight ?: 0.0
        val heightCm = nutrition?.height_cm ?: nutrition?.height?.toDouble() ?: 0.0
        val targetWeight = nutrition?.target_weight_kg ?: 0.0
        val bmi = if (currentWeight > 0.0 && heightCm > 0.0) {
            ((currentWeight / Math.pow(heightCm / 100.0, 2.0)) * 100.0).toInt() / 100.0
        } else {
            0.0
        }

        return UserProfile(
            id = id,
            displayName = display_name.orEmpty(),
            dob = dob.orEmpty(),
            gender = gender.orEmpty(),
            planLabel = plan_label.orEmpty(),
            weight = currentWeight,
            height = heightCm.toInt(),
            bmi = bmi,
            bmr = 0,
            statusLabel = status_label.orEmpty(),
            streak = current_streak ?: 0,
            bodyFat = nutrition?.body_fat_percentage ?: nutrition?.body_fat ?: 0.0,
            currentJourney = ProfileJourney(
                name = "",
                initialWeight = currentWeight.takeIf { it > 0.0 } ?: 0.0,
                targetWeight = targetWeight,
                progress = 0f,
            ),
            progressPhotos = emptyList(),
            lastPhotoUpdateWeight = "",
            lastPhotoUpdateDate = "",
            workoutChart = ProfileWorkoutChart(),
            avatarUrl = avatar_url?.takeIf(String::isNotBlank),
            membershipTier = MembershipTier.fromPlanLabel(plan_label),
            username = username.orEmpty(),
            mobile = mobile.orEmpty(),
            hasCompletedOnboarding = is_onboarded,
        )
    }

    private fun mergeProfiles(local: UserProfile?, remote: UserProfile): UserProfile {
        if (local == null) return remote
        return remote.copy(
            displayName = remote.displayName.ifBlank { local.displayName },
            dob = remote.dob.ifBlank { local.dob },
            gender = remote.gender.ifBlank { local.gender },
            mobile = remote.mobile.ifBlank { local.mobile },
            planLabel = remote.planLabel.ifBlank { local.planLabel },
            height = if (remote.height > 0) remote.height else local.height,
            weight = if (remote.weight > 0.0) remote.weight else local.weight,
            hasCompletedOnboarding = remote.hasCompletedOnboarding || local.hasCompletedOnboarding,
            currentJourney = remote.currentJourney.copy(
                initialWeight = if (remote.currentJourney.initialWeight > 0.0) remote.currentJourney.initialWeight else local.currentJourney.initialWeight,
                targetWeight = if (remote.currentJourney.targetWeight > 0.0) remote.currentJourney.targetWeight else local.currentJourney.targetWeight,
            ),
        )
    }

    private fun clearLocalProfileIfOwnerChanged(userId: String) {
        if (localProfileState.value?.ownerUserId != userId) {
            localProfileState.value = null
        }
    }

    private fun emptyProfile(): UserProfile {
        return UserProfile(
            id = "anonymous",
            displayName = "",
            dob = "",
            gender = "",
            planLabel = "",
            weight = 0.0,
            height = 0,
            bmi = 0.0,
            bmr = 0,
        )
    }
}

private data class LocalProfileSnapshot(
    val ownerUserId: String,
    val profile: UserProfile,
)
