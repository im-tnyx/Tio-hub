package com.tnyx.data.profile

import com.tnyx.features.auth.data.repository.FakeAuthRepository
import com.tnyx.features.auth.data.session.InMemoryAuthSessionStore
import com.tnyx.features.auth.domain.model.AuthResult
import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.auth.domain.repository.MutableAuthSessionStore
import com.tnyx.shared.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryProfileRepositoryTest {

    @Test
    fun signedOutProfileContainsNoPreviousUserData() = runTest {
        val repository = InMemoryProfileRepository(TestAuthSessionStore())

        val profile = repository.getCurrentProfile().first()

        assertEquals("local-guest", profile.id)
        assertEquals("", profile.displayName)
        assertEquals("", profile.username)
        assertEquals(0.0, profile.weight, 0.0)
        assertEquals(0, profile.height)
        assertNull(profile.avatarUrl)
    }

    @Test
    fun authenticatedSessionSeedsIdentityWithoutFabricatedMetrics() = runTest {
        val sessions = TestAuthSessionStore()
        val repository = InMemoryProfileRepository(sessions)
        sessions.setSession(session("user-a", "alice@example.com", "Alice"))

        val profile = repository.getCurrentProfile().first()

        assertEquals("user-a", profile.id)
        assertEquals("Alice", profile.displayName)
        assertEquals("alice", profile.username)
        assertEquals(0.0, profile.weight, 0.0)
    }

    @Test
    fun profileUpdatesRemainIsolatedAcrossAccountSwitches() = runTest {
        val sessions = TestAuthSessionStore()
        val repository = InMemoryProfileRepository(sessions)
        sessions.setSession(session("user-a", "alice@example.com", "Alice"))
        repository.updateProfile(
            repository.getCurrentProfile().first().copy(
                displayName = "Alice Updated",
                weight = 62.5,
            ),
        )

        sessions.setSession(session("user-b", "bob@example.com", "Bob"))
        val userB = repository.getCurrentProfile().first()
        assertEquals("user-b", userB.id)
        assertEquals("Bob", userB.displayName)
        assertEquals(0.0, userB.weight, 0.0)

        sessions.setSession(session("user-a", "alice@example.com", "Alice"))
        val restoredUserA = repository.getCurrentProfile().first()
        assertEquals("Alice Updated", restoredUserA.displayName)
        assertEquals(62.5, restoredUserA.weight, 0.0)
    }

    @Test
    fun anotherProfileIdentityCannotReplaceActiveProfile() {
        val sessions = TestAuthSessionStore(session("user-a", "alice@example.com", "Alice"))
        val repository = InMemoryProfileRepository(sessions)

        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                repository.updateProfile(profile(id = "user-b"))
            }
        }
    }

    @Test
    fun avatarCanBeUpdatedAndRemovedForActiveAccount() = runTest {
        val sessions = TestAuthSessionStore(session("user-a", "alice@example.com", "Alice"))
        val repository = InMemoryProfileRepository(sessions)

        val dataUrl = repository.updateAvatar(byteArrayOf(1, 2, 3))
        assertTrue(dataUrl.startsWith("data:image/jpeg;base64,"))
        assertEquals(dataUrl, repository.getCurrentProfile().first().avatarUrl)

        repository.removeAvatar()
        assertNull(repository.getCurrentProfile().first().avatarUrl)
    }

    @Test
    fun signOutSwitchesToGuestWithoutExposingAuthenticatedProfile() = runTest {
        val sessions = TestAuthSessionStore(session("user-a", "alice@example.com", "Alice"))
        val repository = InMemoryProfileRepository(sessions)
        repository.updateProfile(
            repository.getCurrentProfile().first().copy(weight = 62.5),
        )

        sessions.clearSession()
        val guest = repository.getCurrentProfile().first()

        assertEquals("local-guest", guest.id)
        assertEquals("", guest.displayName)
        assertEquals(0.0, guest.weight, 0.0)
    }

    @Test
    fun fakeAuthLifecycleDrivesAndRestoresItsOwnLocalProfile() = runTest {
        val sessions = InMemoryAuthSessionStore()
        val authRepository = FakeAuthRepository(sessions)
        val profileRepository = InMemoryProfileRepository(sessions)
        authRepository.signUp("Santosh Kumar", "santosh@example.com", "securepass")
        val authenticated = authRepository.verifyOtp("santosh@example.com", "123456")
            as AuthResult.Authenticated

        val initialProfile = profileRepository.getCurrentProfile().first()
        assertEquals(authenticated.session.userId, initialProfile.id)
        assertEquals("Santosh Kumar", initialProfile.displayName)
        profileRepository.updateProfile(initialProfile.copy(weight = 70.0))

        authRepository.signOut()
        assertEquals("local-guest", profileRepository.getCurrentProfile().first().id)
        authRepository.signIn("santosh@example.com", "securepass")

        val restoredProfile = profileRepository.getCurrentProfile().first()
        assertEquals(authenticated.session.userId, restoredProfile.id)
        assertEquals("Santosh Kumar", restoredProfile.displayName)
        assertEquals(70.0, restoredProfile.weight, 0.0)
    }

    private fun session(
        id: String,
        email: String,
        displayName: String,
    ): AuthSession {
        return AuthSession(
            userId = id,
            email = email,
            displayName = displayName,
            isDemo = false,
        )
    }

    private fun profile(id: String): UserProfile {
        return UserProfile(
            id = id,
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

private class TestAuthSessionStore(
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
