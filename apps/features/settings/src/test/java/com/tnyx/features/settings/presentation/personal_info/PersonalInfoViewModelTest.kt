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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

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
        assertEquals("+91", viewModel.uiState.value.selectedCountry?.code)
        assertEquals("9876543210", viewModel.uiState.value.phoneNumber)
        assertEquals("Male", viewModel.uiState.value.gender)
        assertEquals("171", viewModel.uiState.value.heightCm)
        assertTrue(viewModel.uiState.value.dobMillis > 0L)
    }

    @Test
    fun saveNormalizesAndUpdatesProfileFields() = runTest {
        val repository = TestProfileRepository(profile())
        val viewModel = PersonalInfoViewModel(repository, TestSessionProvider())
        advanceUntilIdle()

        viewModel.onAction(PersonalInfoAction.OnFullNameChange("  Santosh Kumar  "))
        viewModel.onAction(PersonalInfoAction.OnUsernameChange("@Santosh_Kumar"))
        viewModel.onAction(PersonalInfoAction.OnCountrySelected(com.tnyx.core.ui.components.inputs.Country("United States", "+1", "🇺🇸")))
        viewModel.onAction(PersonalInfoAction.OnMobileChange("5551234567"))
        viewModel.onAction(PersonalInfoAction.OnGenderChange("Other"))
        viewModel.onAction(
            PersonalInfoAction.OnDobChange(
                LocalDate.of(1998, 8, 19)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli(),
            ),
        )
        viewModel.onAction(PersonalInfoAction.OnHeightCmChange("180"))
        viewModel.onAction(PersonalInfoAction.OnSaveClicked)
        advanceUntilIdle()

        assertEquals("Santosh Kumar", repository.updatedProfile?.displayName)
        assertEquals("santosh_kumar", repository.updatedProfile?.username)
        assertEquals("+15551234567", repository.updatedProfile?.mobile)
        assertEquals("Other", repository.updatedProfile?.gender)
        assertEquals("1998-08-19", repository.updatedProfile?.dob)
        assertEquals(180, repository.updatedProfile?.height)
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

    @Test
    fun blankUsernameDoesNotBlockPersonalInformationSave() = runTest {
        val repository = TestProfileRepository(profile().copy(username = ""))
        val viewModel = PersonalInfoViewModel(repository, TestSessionProvider())
        advanceUntilIdle()

        viewModel.onAction(PersonalInfoAction.OnFullNameChange("Santosh Kumar"))
        viewModel.onAction(PersonalInfoAction.OnGenderChange("male"))
        viewModel.onAction(PersonalInfoAction.OnSaveClicked)
        advanceUntilIdle()

        assertEquals("Santosh Kumar", repository.updatedProfile?.displayName)
        assertEquals("", repository.updatedProfile?.username)
        assertEquals("male", repository.updatedProfile?.gender)
    }

    @Test
    fun invalidHeightDoesNotUpdateProfile() = runTest {
        val repository = TestProfileRepository(profile())
        val viewModel = PersonalInfoViewModel(repository, TestSessionProvider())
        advanceUntilIdle()

        viewModel.onAction(PersonalInfoAction.OnHeightCmChange("abc"))
        viewModel.onAction(PersonalInfoAction.OnSaveClicked)
        advanceUntilIdle()

        assertEquals("Height is invalid. Check the value and try again.", viewModel.uiState.value.saveError)
        assertEquals(null, repository.updatedProfile)
    }

    private fun profile(): UserProfile {
        return UserProfile(
            id = "user-a",
            displayName = "Santosh",
            username = "santosh",
            dob = "1995-06-05",
            gender = "Male",
            planLabel = "",
            weight = 0.0,
            height = 171,
            bmi = 0.0,
            bmr = 0,
            mobile = "+919876543210",
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
