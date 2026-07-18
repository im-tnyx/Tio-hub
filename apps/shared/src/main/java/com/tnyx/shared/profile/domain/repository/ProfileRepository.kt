package com.tnyx.shared.profile.domain.repository

import com.tnyx.shared.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getCurrentProfile(): Flow<UserProfile>
    fun getProfile(userId: String): Flow<UserProfile>
    suspend fun updateProfile(profile: UserProfile)
    suspend fun updateAvatar(jpegBytes: ByteArray): String
    suspend fun removeAvatar()
}
