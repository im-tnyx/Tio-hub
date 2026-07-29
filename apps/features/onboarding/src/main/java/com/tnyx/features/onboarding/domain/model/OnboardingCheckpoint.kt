package com.tnyx.features.onboarding.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OnboardingProgress(
    val flowVersion: Int,
    val position: OnboardingPosition,
    val completedSectionIds: Set<OnboardingSectionId> = emptySet(),
    val isCompleted: Boolean = false,
) {
    init {
        require(flowVersion > 0) { "Onboarding progress flow version must be positive" }
    }
}

@Serializable
data class OnboardingCheckpoint(
    val draft: OnboardingDraft = OnboardingDraft(),
    val progress: OnboardingProgress,
)
