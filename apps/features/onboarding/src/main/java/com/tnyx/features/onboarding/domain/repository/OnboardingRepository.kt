package com.tnyx.features.onboarding.domain.repository

import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun observeCheckpoint(): Flow<OnboardingCheckpoint?>

    suspend fun saveCheckpoint(checkpoint: OnboardingCheckpoint)

    suspend fun clearCheckpoint()
}
