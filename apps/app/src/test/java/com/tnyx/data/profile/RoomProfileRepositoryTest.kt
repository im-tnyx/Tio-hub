package com.tnyx.data.profile

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.tnyx.data.profile.local.ProfileDatabase
import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.auth.domain.repository.MutableAuthSessionStore
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomProfileRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val databaseName = "profile-test-${UUID.randomUUID()}.db"
    private val avatarRoot = File(context.cacheDir, "profile-avatar-${UUID.randomUUID()}")
    private var database: ProfileDatabase? = null

    @After
    fun tearDown() {
        database?.close()
        context.deleteDatabase(databaseName)
        avatarRoot.deleteRecursively()
    }

    @Test
    fun profileSurvivesRepositoryAndDatabaseRecreation() = runTest {
        val sessions = TestProfileSessionStore(authSession("user-a", "Alice"))
        val firstRepository = repository(sessions)
        val initial = firstRepository.getCurrentProfile().first()

        firstRepository.updateProfile(
            initial.copy(
                displayName = "Alice Updated",
                username = "alice_fit",
                weight = 62.5,
            ),
        )
        closeDatabase()

        val recreatedRepository = repository(sessions)
        val restored = recreatedRepository.getCurrentProfile().first()

        assertEquals("Alice Updated", restored.displayName)
        assertEquals("alice_fit", restored.username)
        assertEquals(62.5, restored.weight, 0.0)
    }

    @Test
    fun avatarPersistsAsInternalFileAndCanBeRemoved() = runTest {
        val sessions = TestProfileSessionStore(authSession("user-a", "Alice"))
        val repository = repository(sessions)
        val jpegBytes = byteArrayOf(1, 2, 3, 4)

        val avatarUri = repository.updateAvatar(jpegBytes)
        val avatarFile = InternalProfileAvatarStore(avatarRoot).avatarFile("user-a")

        assertEquals(Uri.fromFile(avatarFile).toString(), avatarUri)
        assertArrayEquals(jpegBytes, avatarFile.readBytes())
        assertEquals(avatarUri, repository.getCurrentProfile().first().avatarUrl)

        repository.removeAvatar()

        assertNull(repository.getCurrentProfile().first().avatarUrl)
        assertEquals(false, avatarFile.exists())
    }

    private fun repository(
        sessions: TestProfileSessionStore,
    ): RoomProfileRepository {
        val database = Room.databaseBuilder(
            context,
            ProfileDatabase::class.java,
            databaseName,
        ).build()
        this.database = database
        return RoomProfileRepository(
            sessionProvider = sessions,
            dao = database.profileDao(),
            codec = ProfilePersistenceCodec(),
            avatarStore = InternalProfileAvatarStore(avatarRoot),
        )
    }

    private fun closeDatabase() {
        database?.close()
        database = null
    }

    private fun authSession(
        userId: String,
        displayName: String,
    ): AuthSession {
        return AuthSession(
            userId = userId,
            email = "$userId@example.com",
            displayName = displayName,
            isDemo = false,
        )
    }
}

private class TestProfileSessionStore(
    initialSession: AuthSession? = null,
) : MutableAuthSessionStore {
    private val session = MutableStateFlow(initialSession)

    override fun observeSession(): Flow<AuthSession?> = session.asStateFlow()

    override fun currentSession(): AuthSession? = session.value

    override suspend fun setSession(session: AuthSession) {
        this.session.value = session
    }

    override suspend fun clearSession() {
        session.value = null
    }
}
