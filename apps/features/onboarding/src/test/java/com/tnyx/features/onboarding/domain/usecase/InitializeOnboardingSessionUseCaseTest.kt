package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
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

class InitializeOnboardingSessionUseCaseTest {
    private val useCase = InitializeOnboardingSessionUseCase()

    @Test
    fun returnsProfileAlreadyCompletedWhenCurrentProfileIsDone() = runTest {
        val onboardingRepository = FakeInitializeOnboardingRepository()
        val profileRepository = FakeInitializeProfileRepository(
            initialProfile = FakeInitializeProfileRepository.defaultProfile(
                hasCompletedOnboarding = true,
            ),
        )

        val result = useCase(
            flow = DefaultOnboardingFlow.definition,
            onboardingRepository = onboardingRepository,
            profileRepository = profileRepository,
        )

        assertEquals(InitializeOnboardingSessionResult.ProfileAlreadyCompleted, result)
    }

    @Test
    fun persistsResolvedCheckpointBeforeReturningReadyState() = runTest {
        val incompatibleCheckpoint = OnboardingCheckpoint(
            draft = OnboardingDraft(),
            progress = OnboardingProgress(
                flowVersion = 999,
                position = OnboardingPosition(
                    sectionId = OnboardingSectionIds.Source,
                    stepId = OnboardingStepIds.SourceReason,
                ),
            ),
        )
        val onboardingRepository = FakeInitializeOnboardingRepository(
            initialCheckpoint = incompatibleCheckpoint,
        )
        val profileRepository = FakeInitializeProfileRepository()

        val result = useCase(
            flow = DefaultOnboardingFlow.definition,
            onboardingRepository = onboardingRepository,
            profileRepository = profileRepository,
        )

        result as InitializeOnboardingSessionResult.Ready
        assertEquals(DefaultOnboardingFlow.definition.firstPosition(), result.checkpoint.progress.position)
        assertEquals(result.checkpoint, onboardingRepository.savedCheckpoints.single())
    }

    @Test
    fun returnsResumeCompletedCheckpointWithoutPersistingAgain() = runTest {
        val completedCheckpoint = OnboardingCheckpoint(
            draft = OnboardingDraft(),
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = OnboardingPosition(
                    sectionId = OnboardingSectionIds.Review,
                    stepId = OnboardingStepIds.ReviewSummary,
                ),
                isCompleted = true,
            ),
        )
        val onboardingRepository = FakeInitializeOnboardingRepository(
            initialCheckpoint = completedCheckpoint,
        )
        val profileRepository = FakeInitializeProfileRepository()

        val result = useCase(
            flow = DefaultOnboardingFlow.definition,
            onboardingRepository = onboardingRepository,
            profileRepository = profileRepository,
        )

        result as InitializeOnboardingSessionResult.ResumeCompletedCheckpoint
        assertEquals(completedCheckpoint, result.checkpoint)
        assertTrue(!result.persistCheckpoint)
        assertTrue(onboardingRepository.savedCheckpoints.isEmpty())
    }
}

private class FakeInitializeOnboardingRepository(
    initialCheckpoint: OnboardingCheckpoint? = null,
) : OnboardingRepository {
    var checkpoint: OnboardingCheckpoint? = initialCheckpoint
    val savedCheckpoints = mutableListOf<OnboardingCheckpoint>()

    override fun observeCheckpoint(): Flow<OnboardingCheckpoint?> = flowOf(checkpoint)

    override suspend fun saveCheckpoint(checkpoint: OnboardingCheckpoint) {
        this.checkpoint = checkpoint
        savedCheckpoints += checkpoint
    }

    override suspend fun clearCheckpoint() = Unit
}

private class FakeInitializeProfileRepository(
    initialProfile: UserProfile = defaultProfile(),
) : ProfileRepository {
    val currentProfile = MutableStateFlow(initialProfile)

    override fun getCurrentProfile(): Flow<UserProfile> = currentProfile

    override fun getProfile(userId: String): Flow<UserProfile> = currentProfile

    override suspend fun updateProfile(profile: UserProfile) {
        currentProfile.value = profile
    }

    override suspend fun updateAvatar(jpegBytes: ByteArray): String = ""

    override suspend fun removeAvatar() = Unit

    companion object {
        fun defaultProfile(hasCompletedOnboarding: Boolean = false): UserProfile {
            return UserProfile(
                id = "local-guest",
                displayName = "",
                dob = "",
                gender = "",
                planLabel = "",
                weight = 0.0,
                height = 0,
                bmi = 0.0,
                bmr = 0,
                hasCompletedOnboarding = hasCompletedOnboarding,
            )
        }
    }
}
