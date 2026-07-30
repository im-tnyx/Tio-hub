package com.tnyx.features.onboarding.domain.resume

import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint

interface ResumeManager {
    suspend fun restoreCheckpoint(): OnboardingCheckpoint?

    suspend fun saveCheckpoint(checkpoint: OnboardingCheckpoint)

    suspend fun clearCheckpoint()
}
