package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingCheckpointResolver
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import javax.inject.Inject

sealed interface ResolveOnboardingInitializationResult {
    data object ProfileAlreadyCompleted : ResolveOnboardingInitializationResult

    data class ResumeCompletedCheckpoint(
        val checkpoint: OnboardingCheckpoint,
        val persistCheckpoint: Boolean,
    ) : ResolveOnboardingInitializationResult

    data class Ready(
        val checkpoint: OnboardingCheckpoint,
        val shouldPersistCheckpoint: Boolean,
    ) : ResolveOnboardingInitializationResult
}

class ResolveOnboardingInitializationUseCase @Inject constructor() {
    private val resolver = OnboardingCheckpointResolver()

    operator fun invoke(
        hasCompletedOnboarding: Boolean,
        storedCheckpoint: OnboardingCheckpoint?,
        flow: OnboardingFlowDefinition,
    ): ResolveOnboardingInitializationResult {
        val resolvedCheckpoint = resolver.resolve(storedCheckpoint, flow)

        if (hasCompletedOnboarding) {
            return ResolveOnboardingInitializationResult.ProfileAlreadyCompleted
        }

        if (resolvedCheckpoint.progress.isCompleted) {
            return ResolveOnboardingInitializationResult.ResumeCompletedCheckpoint(
                checkpoint = resolvedCheckpoint,
                persistCheckpoint = storedCheckpoint != resolvedCheckpoint,
            )
        }

        return ResolveOnboardingInitializationResult.Ready(
            checkpoint = resolvedCheckpoint,
            shouldPersistCheckpoint = storedCheckpoint != resolvedCheckpoint,
        )
    }
}
