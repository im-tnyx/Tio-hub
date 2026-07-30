package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RetryOnboardingSessionUseCaseTest {
    private val useCase = RetryOnboardingSessionUseCase()
    private val finalizeProfile = FinalizeOnboardingProfileUseCase()

    @Test
    fun returnsReinitializeWhenCheckpointMissing() = runTest {
        val result = useCase(
            checkpoint = null,
            onboardingRepository = FakeRetryOnboardingRepository(),
            profileRepository = FakeRetryProfileRepository(),
            finalizeOnboardingProfile = finalizeProfile,
        )

        assertEquals(RetryOnboardingSessionResult.Reinitialize, result)
    }

    @Test
    fun persistsIncompleteCheckpointDuringRetry() = runTest {
        val checkpoint = checkpoint(isCompleted = false)
        val repository = FakeRetryOnboardingRepository()

        val result = useCase(
            checkpoint = checkpoint,
            onboardingRepository = repository,
            profileRepository = FakeRetryProfileRepository(),
            finalizeOnboardingProfile = finalizeProfile,
        )

        assertEquals(RetryOnboardingSessionResult.Persisted(checkpoint), result)
        assertEquals(checkpoint, repository.savedCheckpoints.single())
    }

    @Test
    fun returnsPersistFailureWhenSaveFails() = runTest {
        val checkpoint = checkpoint(isCompleted = false)
        val repository = FakeRetryOnboardingRepository().apply {
            failSaves = true
        }

        val result = useCase(
            checkpoint = checkpoint,
            onboardingRepository = repository,
            profileRepository = FakeRetryProfileRepository(),
            finalizeOnboardingProfile = finalizeProfile,
        )

        assertEquals(RetryOnboardingSessionResult.PersistFailed(checkpoint), result)
    }

    @Test
    fun completesCheckpointDuringRetry() = runTest {
        val checkpoint = checkpoint(isCompleted = true)
        val onboardingRepository = FakeRetryOnboardingRepository()
        val profileRepository = FakeRetryProfileRepository()

        val result = useCase(
            checkpoint = checkpoint,
            onboardingRepository = onboardingRepository,
            profileRepository = profileRepository,
            finalizeOnboardingProfile = finalizeProfile,
        )

        assertEquals(RetryOnboardingSessionResult.Completed(checkpoint), result)
        assertEquals(1, onboardingRepository.clearCheckpointCalls)
        assertTrue(profileRepository.currentProfile.value.hasCompletedOnboarding)
    }

    @Test
    fun returnsCompleteFailureWhenProfileUpdateFails() = runTest {
        val checkpoint = checkpoint(isCompleted = true)
        val result = useCase(
            checkpoint = checkpoint,
            onboardingRepository = FakeRetryOnboardingRepository(),
            profileRepository = FakeRetryProfileRepository().apply {
                failUpdates = true
            },
            finalizeOnboardingProfile = finalizeProfile,
        )

        assertEquals(RetryOnboardingSessionResult.CompleteFailed(checkpoint), result)
    }

    private fun checkpoint(isCompleted: Boolean): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = OnboardingDraft().withAnswer(
                OnboardingStepIds.ProfileName,
                OnboardingAnswer.Text("Santosh"),
            ),
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = DefaultOnboardingFlow.definition.firstPosition(),
                isCompleted = isCompleted,
            ),
        )
    }
}

private class FakeRetryOnboardingRepository : OnboardingRepository {
    val savedCheckpoints = mutableListOf<OnboardingCheckpoint>()
    var clearCheckpointCalls: Int = 0
    var failSaves: Boolean = false

    override fun observeCheckpoint(): Flow<OnboardingCheckpoint?> = flowOf(null)

    override suspend fun saveCheckpoint(checkpoint: OnboardingCheckpoint) {
        if (failSaves) error("Expected test save failure")
        savedCheckpoints += checkpoint
    }

    override suspend fun clearCheckpoint() {
        clearCheckpointCalls += 1
    }
}

private class FakeRetryProfileRepository(
    initialProfile: UserProfile = UserProfile(
        id = "local-guest",
        displayName = "",
        dob = "",
        gender = "",
        planLabel = "",
        weight = 0.0,
        height = 0,
        bmi = 0.0,
        bmr = 0,
    ),
) : ProfileRepository {
    val currentProfile = MutableStateFlow(initialProfile)
    var failUpdates: Boolean = false

    override fun getCurrentProfile(): Flow<UserProfile> = currentProfile

    override fun getProfile(userId: String): Flow<UserProfile> = currentProfile

    override suspend fun updateProfile(profile: UserProfile) {
        if (failUpdates) error("Expected test profile update failure")
        currentProfile.value = profile
    }

    override suspend fun updateAvatar(jpegBytes: ByteArray): String = ""

    override suspend fun removeAvatar() = Unit
}
