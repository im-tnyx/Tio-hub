package com.tnyx.data.profile

import com.tnyx.data.profile.local.ProfileDao
import com.tnyx.data.profile.local.ProfileEntity
import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class RoomProfileRepository(
    private val sessionProvider: AuthSessionProvider,
    private val dao: ProfileDao,
    private val codec: ProfilePersistenceCodec,
    private val avatarStore: InternalProfileAvatarStore,
) : ProfileRepository {

    override fun getCurrentProfile(): Flow<UserProfile> {
        return sessionProvider.observeSession()
            .flatMapLatest(::observeProfile)
            .distinctUntilChanged()
    }

    override fun getProfile(userId: String): Flow<UserProfile> {
        return sessionProvider.observeSession()
            .flatMapLatest { session ->
                require(session.localProfileId() == userId) {
                    "Profile is not available for user: $userId"
                }
                observeProfile(session)
            }
            .distinctUntilChanged()
    }

    override suspend fun updateProfile(profile: UserProfile) {
        val activeProfileId = sessionProvider.currentSession().localProfileId()
        require(profile.id == activeProfileId) {
            "Cannot replace the active local profile identity"
        }
        persist(profile)
    }

    override suspend fun updateAvatar(jpegBytes: ByteArray): String {
        require(jpegBytes.isNotEmpty()) { "Avatar image is empty" }

        val session = sessionProvider.currentSession()
        val profile = readProfile(session)
        val avatarUri = avatarStore.write(profile.id, jpegBytes)
        persist(profile.copy(avatarUrl = avatarUri))
        return avatarUri
    }

    override suspend fun removeAvatar() {
        val session = sessionProvider.currentSession()
        val profile = readProfile(session)
        persist(profile.copy(avatarUrl = null))
        avatarStore.delete(profile.id)
    }

    private fun observeProfile(session: AuthSession?): Flow<UserProfile> {
        val profileId = session.localProfileId()
        return dao.observeProfile(profileId)
            .map { entity -> entity?.toDomain() ?: emptyLocalProfile(session) }
    }

    private suspend fun readProfile(session: AuthSession?): UserProfile {
        val profileId = session.localProfileId()
        return dao.getProfile(profileId)?.toDomain() ?: emptyLocalProfile(session)
    }

    private suspend fun persist(profile: UserProfile) {
        dao.upsertProfile(
            ProfileEntity(
                userId = profile.id,
                profileJson = codec.encode(profile),
                updatedAtMs = System.currentTimeMillis(),
            ),
        )
    }

    private fun ProfileEntity.toDomain(): UserProfile {
        return codec.decode(profileJson).also { profile ->
            require(profile.id == userId) { "Persisted Profile identity does not match its row" }
        }
    }
}
