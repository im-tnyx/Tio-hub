package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveOnboardingRetryUseCaseTest {
    private val useCase = ResolveOnboardingRetryUseCase()

    @Test
    fun returnsReinitializeWhenCheckpointMissing() {
        val result = useCase(null)

        assertEquals(ResolveOnboardingRetryResult.Reinitialize, result)
    }

    @Test
    fun returnsPersistWhenCheckpointIsIncomplete() {
        val checkpoint = checkpoint(isCompleted = false)

        val result = useCase(checkpoint)

        assertEquals(ResolveOnboardingRetryResult.Persist(checkpoint), result)
    }

    @Test
    fun returnsCompleteWhenCheckpointIsCompleted() {
        val checkpoint = checkpoint(isCompleted = true)

        val result = useCase(checkpoint)

        assertEquals(ResolveOnboardingRetryResult.Complete(checkpoint), result)
    }

    private fun checkpoint(isCompleted: Boolean): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = OnboardingDraft(),
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = DefaultOnboardingFlow.definition.firstPosition(),
                isCompleted = isCompleted,
            ),
        )
    }
}
