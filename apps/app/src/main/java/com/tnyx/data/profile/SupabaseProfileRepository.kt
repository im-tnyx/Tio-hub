package com.tnyx.data.profile

import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    val display_name: String,
    val dob: String,
    val gender: String,
    val plan_label: String,
    val weight: Double,
    val height: Int,
    val bmi: Double,
    val bmr: Int
)

class SupabaseProfileRepository(
    private val supabaseClient: SupabaseClient
) : ProfileRepository {

    override fun getProfile(userId: String): Flow<UserProfile> = flow {
        val dto = supabaseClient.from("profiles").select {
            filter {
                eq("id", userId)
            }
        }.decodeSingle<ProfileDto>()
        
        emit(
            UserProfile(
                id = dto.id,
                displayName = dto.display_name,
                dob = dto.dob,
                gender = dto.gender,
                planLabel = dto.plan_label,
                weight = dto.weight,
                height = dto.height,
                bmi = dto.bmi,
                bmr = dto.bmr
            )
        )
    }

    override suspend fun updateProfile(profile: UserProfile) {
        supabaseClient.from("profiles").update(
            ProfileDto(
                id = profile.id,
                display_name = profile.displayName,
                dob = profile.dob,
                gender = profile.gender,
                plan_label = profile.planLabel,
                weight = profile.weight,
                height = profile.height,
                bmi = profile.bmi,
                bmr = profile.bmr
            )
        ) {
            filter {
                eq("id", profile.id)
            }
        }
    }
}
