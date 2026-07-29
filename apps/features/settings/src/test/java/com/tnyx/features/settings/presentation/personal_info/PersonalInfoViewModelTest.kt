package com.tnyx.features.settings.presentation.personal_info

import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalInfoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun profileIdentityLoadsFromRepositories() = runTest {
        val repository = TestProfileRepository(profile())
        val viewModel = PersonalInfoViewModel(repository, TestSessionProvider())

        advanceUntilIdle()

        assertEquals("Santosh", viewModel.uiState.value.fullName)
        assertEquals("santosh", viewModel.uiState.value.username)
        assertEquals("santosh@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun saveNormalizesAndUpdatesNameAndUsername() = runTest {
        val repository = TestProfileRepository(profile())
        val viewModel = PersonalInfoViewModel(repository, TestSessionProvider())
        advanceUntilIdle()

        viewModel.onAction(PersonalInfoAction.OnFullNameChange("  Santosh Kumar  "))
        viewModel.onAction(PersonalInfoAction.OnUsernameChange("@Santosh_Kumar"))
        viewModel.onAction(PersonalInfoAction.OnSaveClicked)
        advanceUntilIdle()

        assertEquals("Santosh Kumar", repository.updatedProfile?.displayName)
        assertEquals("santosh_kumar", repository.updatedProfile?.username)
        assertFalse(viewModel.uiState.value.hasChanges)
    }

    @Test
    fun invalidUsernameDoesNotUpdateProfile() = runTest {
        val repository = TestProfileRepository(profile())
        val viewModel = PersonalInfoViewModel(repository, TestSessionProvider())
        advanceUntilIdle()

        viewModel.onAction(PersonalInfoAction.OnUsernameChange("ab"))
        viewModel.onAction(PersonalInfoAction.OnSaveClicked)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.usernameError)
        assertEquals(null, repository.updatedProfile)
    }

    private fun profile(): UserProfile {
        return UserProfile(
            id = "user-a",
            displayName = "Santosh",
            username = "santosh",
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

private class TestSessionProvider : AuthSessionProvider {
    private val session = AuthSession(
        userId = "user-a",
        email = "santosh@example.com",
        displayName = "Santosh",
        isDemo = false,
    )

    override fun observeSession(): Flow<AuthSession?> = flowOf(session)

    override fun currentSession(): AuthSession = session
}

private class TestProfileRepository(
    initialProfile: UserProfile,
) : ProfileRepository {
    private val profile = MutableStateFlow(initialProfile)
    var updatedProfile: UserProfile? = null

    override fun getCurrentProfile(): Flow<UserProfile> = profile

    override fun getProfile(userId: String): Flow<UserProfile> = profile

    override suspend fun updateProfile(profile: UserProfile) {
        updatedProfile = profile
        this.profile.value = profile
    }

    override suspend fun updateAvatar(jpegBytes: ByteArray): String = ""

    override suspend fun removeAvatar() = Unit
}
