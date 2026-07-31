package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
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

class ContinueOnboardingSessionUseCaseTest {
    private val useCase = ContinueOnboardingSessionUseCase()
    private val finalizeProfile = FinalizeOnboardingProfileUseCase()

    @Test
    fun persistsNextCheckpointWhenAdvanceReturnsNext() = runTest {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.BodyGoal,
            stepId = OnboardingStepIds.BodyGoalHeight,
        )
        val repository = FakeContinueOnboardingRepository()

        val result = useCase(
            advanceResult = AdvanceOnboardingStepResult.Next(checkpoint),
            onboardingRepository = repository,
            profileRepository = FakeContinueProfileRepository(),
            finalizeOnboardingProfile = finalizeProfile,
        )

        assertEquals(ContinueOnboardingSessionResult.Persisted(checkpoint), result)
        assertEquals(checkpoint, repository.savedCheckpoints.single())
    }

    @Test
    fun returnsPersistFailureWhenNextSaveFails() = runTest {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.BodyGoal,
            stepId = OnboardingStepIds.BodyGoalHeight,
        )
        val repository = FakeContinueOnboardingRepository().apply {
            failSaves = true
        }

        val result = useCase(
            advanceResult = AdvanceOnboardingStepResult.Next(checkpoint),
            onboardingRepository = repository,
            profileRepository = FakeContinueProfileRepository(),
            finalizeOnboardingProfile = finalizeProfile,
        )

        assertEquals(ContinueOnboardingSessionResult.PersistFailed(checkpoint), result)
    }

    @Test
    fun completesSessionWhenAdvanceReturnsCompleted() = runTest {
        val checkpoint = completedCheckpoint()
        val onboardingRepository = FakeContinueOnboardingRepository()
        val profileRepository = FakeContinueProfileRepository()

        val result = useCase(
            advanceResult = AdvanceOnboardingStepResult.Completed(checkpoint),
            onboardingRepository = onboardingRepository,
            profileRepository = profileRepository,
            finalizeOnboardingProfile = finalizeProfile,
        )

        assertEquals(ContinueOnboardingSessionResult.Completed(checkpoint), result)
        assertEquals(checkpoint, onboardingRepository.savedCheckpoints.single())
        assertEquals(1, onboardingRepository.clearCheckpointCalls)
        assertTrue(profileRepository.currentProfile.value.hasCompletedOnboarding)
    }

    @Test
    fun returnsCompleteFailureWhenCompletedPathFails() = runTest {
        val checkpoint = completedCheckpoint()

        val result = useCase(
            advanceResult = AdvanceOnboardingStepResult.Completed(checkpoint),
            onboardingRepository = FakeContinueOnboardingRepository(),
            profileRepository = FakeContinueProfileRepository().apply {
                failUpdates = true
            },
            finalizeOnboardingProfile = finalizeProfile,
        )

        assertEquals(ContinueOnboardingSessionResult.CompleteFailed(checkpoint), result)
    }

    private fun checkpoint(
        sectionId: com.tnyx.features.onboarding.domain.model.OnboardingSectionId,
        stepId: com.tnyx.features.onboarding.domain.model.OnboardingStepId,
    ): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = OnboardingDraft(),
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = OnboardingPosition(
                    sectionId = sectionId,
                    stepId = stepId,
                ),
            ),
        )
    }

    private fun completedCheckpoint(): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = completedDraft(),
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = OnboardingPosition(
                    sectionId = OnboardingSectionIds.Review,
                    stepId = OnboardingStepIds.ReviewSummary,
                ),
                completedSectionIds = DefaultOnboardingFlow.definition.sections
                    .map { section -> section.id }
                    .toSet(),
                isCompleted = true,
            ),
        )
    }

    private fun completedDraft(): OnboardingDraft {
        return OnboardingDraft()
            .withAnswer(OnboardingStepIds.ProfileName, OnboardingAnswer.Text("Santosh"))
            .withAnswer(OnboardingStepIds.ProfileGender, OnboardingAnswer.Text("male"))
            .withAnswer(OnboardingStepIds.ProfileDateOfBirth, OnboardingAnswer.Text("1990-01-01"))
            .withAnswer(OnboardingStepIds.BodyGoalPrimaryGoal, OnboardingAnswer.Text("lose_weight"))
            .withAnswer(OnboardingStepIds.BodyGoalHeight, OnboardingAnswer.Decimal(176.0))
            .withAnswer(OnboardingStepIds.BodyGoalCurrentWeight, OnboardingAnswer.Decimal(80.0))
            .withAnswer(OnboardingStepIds.BodyGoalTargetWeight, OnboardingAnswer.Decimal(74.0))
            .withAnswer(OnboardingStepIds.BodyGoalActivityLevel, OnboardingAnswer.Text("active"))
            .withAnswer(OnboardingStepIds.BodyGoalHealthCondition, OnboardingAnswer.Selections(listOf("none")))
            .withAnswer(OnboardingStepIds.MobileNumber, OnboardingAnswer.Text("+91 9876543210"))
            .withAnswer(OnboardingStepIds.WorkoutIntroChoice, OnboardingAnswer.Toggle(false))
            .withAnswer(OnboardingStepIds.TargetsStepsTarget, OnboardingAnswer.Decimal(11000.0))
            .withAnswer(OnboardingStepIds.TargetsSleepTarget, OnboardingAnswer.Text("balanced_evenings"))
            .withAnswer(OnboardingStepIds.TargetsWaterTarget, OnboardingAnswer.Decimal(2750.0))
            .withAnswer(OnboardingStepIds.TargetsRecommendationSummary, OnboardingAnswer.Toggle(true))
            .withAnswer(OnboardingStepIds.TargetsGoalPace, OnboardingAnswer.Text("steady"))
            .withAnswer(OnboardingStepIds.TargetsNutritionSummary, OnboardingAnswer.Text("protein_priority"))
            .withAnswer(OnboardingStepIds.SourceChannel, OnboardingAnswer.Text("friend_referral"))
            .withAnswer(OnboardingStepIds.SourceReason, OnboardingAnswer.Text("complete_reset"))
            .withAnswer(OnboardingStepIds.ReviewSummary, OnboardingAnswer.Toggle(true))
    }
}

private class FakeContinueOnboardingRepository : OnboardingRepository {
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

private class FakeContinueProfileRepository(
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
