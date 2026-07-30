package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import javax.inject.Inject

sealed interface ResolveOnboardingRetryResult {
    data object Reinitialize : ResolveOnboardingRetryResult

    data class Persist(
        val checkpoint: OnboardingCheckpoint,
    ) : ResolveOnboardingRetryResult

    data class Complete(
        val checkpoint: OnboardingCheckpoint,
    ) : ResolveOnboardingRetryResult
}

class ResolveOnboardingRetryUseCase @Inject constructor() {
    operator fun invoke(
        checkpoint: OnboardingCheckpoint?,
    ): ResolveOnboardingRetryResult {
        val currentCheckpoint = checkpoint ?: return ResolveOnboardingRetryResult.Reinitialize
        return if (currentCheckpoint.progress.isCompleted) {
            ResolveOnboardingRetryResult.Complete(currentCheckpoint)
        } else {
            ResolveOnboardingRetryResult.Persist(currentCheckpoint)
        }
    }
}
