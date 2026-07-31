package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingCheckpointResolver
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.resume.ResumeManager

data class RestoreFlowResult(
    val checkpoint: OnboardingCheckpoint,
    val restoredFromSnapshot: Boolean,
    val shouldPersistCheckpoint: Boolean,
)

class RestoreFlowUseCase(
    private val resumeManager: ResumeManager,
) {
    private val resolver = OnboardingCheckpointResolver()

    suspend operator fun invoke(
        flow: OnboardingFlowDefinition,
        storedCheckpoint: OnboardingCheckpoint?,
    ): RestoreFlowResult {
        val resumedCheckpoint = resumeManager.restoreCheckpoint()
        val sourceCheckpoint = resumedCheckpoint ?: storedCheckpoint
        val resolvedCheckpoint = resolver.resolve(sourceCheckpoint, flow)

        return RestoreFlowResult(
            checkpoint = resolvedCheckpoint,
            restoredFromSnapshot = resumedCheckpoint != null,
            shouldPersistCheckpoint = sourceCheckpoint != resolvedCheckpoint ||
                (resumedCheckpoint != null && resumedCheckpoint != storedCheckpoint),
        )
    }
}
