package com.tnyx.data.profile

import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import java.util.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Non-persistent Profile source for the current fake-auth development phase.
 *
 * Production user data must come from a future backend repository implementation.
 */
class InMemoryProfileRepository(
    private val sessionProvider: AuthSessionProvider,
    initialProfiles: List<UserProfile> = emptyList(),
) : ProfileRepository {

    private val profiles = MutableStateFlow(initialProfiles.associateBy(UserProfile::id))

    override fun getCurrentProfile(): Flow<UserProfile> {
        return combine(sessionProvider.observeSession(), profiles) { session, profilesById ->
            profilesById[session.localProfileId()] ?: emptyLocalProfile(session)
        }
    }

    override fun getProfile(userId: String): Flow<UserProfile> {
        return combine(sessionProvider.observeSession(), profiles) { session, profilesById ->
            require(session.localProfileId() == userId) {
                "Profile is not available for user: $userId"
            }
            profilesById[userId] ?: emptyLocalProfile(session)
        }
    }

    override suspend fun updateProfile(profile: UserProfile) {
        val activeProfileId = sessionProvider.currentSession().localProfileId()
        require(profile.id == activeProfileId) {
            "Cannot replace the active local profile identity"
        }
        profiles.value = profiles.value + (profile.id to profile)
    }

    override suspend fun updateAvatar(jpegBytes: ByteArray): String {
        require(jpegBytes.isNotEmpty()) { "Avatar image is empty" }

        val dataUrl = "data:image/jpeg;base64," +
            Base64.getEncoder().encodeToString(jpegBytes)
        updateActiveProfile { profile -> profile.copy(avatarUrl = dataUrl) }
        return dataUrl
    }

    override suspend fun removeAvatar() {
        updateActiveProfile { profile -> profile.copy(avatarUrl = null) }
    }

    private fun updateActiveProfile(transform: (UserProfile) -> UserProfile) {
        val session = sessionProvider.currentSession()
        val profileId = session.localProfileId()
        val currentProfile = profiles.value[profileId] ?: emptyLocalProfile(session)
        profiles.value = profiles.value + (profileId to transform(currentProfile))
    }

}
