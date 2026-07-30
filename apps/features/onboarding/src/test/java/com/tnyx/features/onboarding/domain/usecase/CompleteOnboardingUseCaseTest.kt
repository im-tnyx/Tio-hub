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

class CompleteOnboardingUseCaseTest {
    private val useCase = CompleteOnboardingUseCase()
    private val finalizeProfile = FinalizeOnboardingProfileUseCase()

    @Test
    fun persistsCompletedCheckpointUpdatesProfileAndClearsLocalCheckpoint() = runTest {
        val onboardingRepository = FakeCompleteOnboardingRepository()
        val profileRepository = FakeCompleteProfileRepository()
        val checkpoint = completedCheckpoint()

        val result = useCase(
            checkpoint = checkpoint,
            persistCheckpoint = true,
            onboardingRepository = onboardingRepository,
            profileRepository = profileRepository,
            finalizeOnboardingProfile = finalizeProfile,
        )

        assertEquals(CompleteOnboardingResult.Success(checkpoint), result)
        assertEquals(checkpoint, onboardingRepository.savedCheckpoints.single())
        assertEquals(1, onboardingRepository.clearCheckpointCalls)
        assertTrue(profileRepository.currentProfile.value.hasCompletedOnboarding)
        assertEquals("Santosh", profileRepository.currentProfile.value.displayName)
    }

    @Test
    fun returnsFailureWhenProfileUpdateFails() = runTest {
        val onboardingRepository = FakeCompleteOnboardingRepository()
        val profileRepository = FakeCompleteProfileRepository().apply {
            failUpdates = true
        }
        val checkpoint = completedCheckpoint()

        val result = useCase(
            checkpoint = checkpoint,
            persistCheckpoint = true,
            onboardingRepository = onboardingRepository,
            profileRepository = profileRepository,
            finalizeOnboardingProfile = finalizeProfile,
        )

        assertEquals(CompleteOnboardingResult.Failure(checkpoint), result)
        assertEquals(0, onboardingRepository.clearCheckpointCalls)
        assertTrue(!profileRepository.currentProfile.value.hasCompletedOnboarding)
    }

    private fun completedCheckpoint(): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = OnboardingDraft().withAnswer(
                OnboardingStepIds.ProfileName,
                OnboardingAnswer.Text("Santosh"),
            ),
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = DefaultOnboardingFlow.definition.firstPosition(),
                isCompleted = true,
            ),
        )
    }
}

private class FakeCompleteOnboardingRepository : OnboardingRepository {
    val savedCheckpoints = mutableListOf<OnboardingCheckpoint>()
    var clearCheckpointCalls: Int = 0

    override fun observeCheckpoint(): Flow<OnboardingCheckpoint?> = flowOf(null)

    override suspend fun saveCheckpoint(checkpoint: OnboardingCheckpoint) {
        savedCheckpoints += checkpoint
    }

    override suspend fun clearCheckpoint() {
        clearCheckpointCalls += 1
    }
}

private class FakeCompleteProfileRepository(
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
