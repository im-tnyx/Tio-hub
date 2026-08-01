package com.tnyx.features.onboarding.domain.repository

import com.tnyx.features.onboarding.domain.model.OnboardingDraft

/** Persists completed answers to the repositories that own their data. */
interface OnboardingCompletionSyncRepository {
    suspend fun syncCompletedOnboarding(draft: OnboardingDraft)
}
