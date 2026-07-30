package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.engine.OnboardingStateMachine
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import javax.inject.Inject

sealed interface ResolveBackNavigationResult {
    data object Exit : ResolveBackNavigationResult

    data class Previous(
        val checkpoint: OnboardingCheckpoint,
    ) : ResolveBackNavigationResult
}

class ResolveBackNavigationUseCase @Inject constructor() {
    operator fun invoke(
        checkpoint: OnboardingCheckpoint?,
        flow: OnboardingFlowDefinition,
    ): ResolveBackNavigationResult {
        val currentCheckpoint = checkpoint ?: return ResolveBackNavigationResult.Exit
        val previousPosition = OnboardingStateMachine(flow).previousPosition(currentCheckpoint)
            ?: return ResolveBackNavigationResult.Exit

        return ResolveBackNavigationResult.Previous(
            checkpoint = currentCheckpoint.copy(
                progress = currentCheckpoint.progress.copy(
                    position = previousPosition,
                    isCompleted = false,
                ),
            ),
        )
    }
}
