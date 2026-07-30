package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveOnboardingInitializationUseCaseTest {
    private val useCase = ResolveOnboardingInitializationUseCase()

    @Test
    fun returnsProfileAlreadyCompletedWhenProfileIsDone() {
        val result = useCase(
            hasCompletedOnboarding = true,
            storedCheckpoint = null,
            flow = DefaultOnboardingFlow.definition,
        )

        assertEquals(ResolveOnboardingInitializationResult.ProfileAlreadyCompleted, result)
    }

    @Test
    fun returnsResumeCompletedCheckpointWhenStoredCheckpointIsCompleted() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Review,
            stepId = OnboardingStepIds.ReviewSummary,
            isCompleted = true,
        )

        val result = useCase(
            hasCompletedOnboarding = false,
            storedCheckpoint = checkpoint,
            flow = DefaultOnboardingFlow.definition,
        )

        result as ResolveOnboardingInitializationResult.ResumeCompletedCheckpoint
        assertEquals(checkpoint, result.checkpoint)
        assertTrue(!result.persistCheckpoint)
    }

    @Test
    fun resolvesIncompatibleCheckpointToFreshReadyState() {
        val incompatibleCheckpoint = OnboardingCheckpoint(
            draft = OnboardingDraft(),
            progress = OnboardingProgress(
                flowVersion = 99,
                position = OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutDuration,
                ),
            ),
        )

        val result = useCase(
            hasCompletedOnboarding = false,
            storedCheckpoint = incompatibleCheckpoint,
            flow = DefaultOnboardingFlow.definition,
        )

        result as ResolveOnboardingInitializationResult.Ready
        assertEquals(DefaultOnboardingFlow.definition.firstPosition(), result.checkpoint.progress.position)
        assertTrue(result.shouldPersistCheckpoint)
    }

    private fun checkpoint(
        sectionId: com.tnyx.features.onboarding.domain.model.OnboardingSectionId,
        stepId: com.tnyx.features.onboarding.domain.model.OnboardingStepId,
        isCompleted: Boolean,
    ): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = OnboardingDraft(),
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = OnboardingPosition(
                    sectionId = sectionId,
                    stepId = stepId,
                ),
                isCompleted = isCompleted,
            ),
        )
    }
}
