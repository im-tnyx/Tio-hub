package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

sealed interface PersistOnboardingCheckpointResult {
    data class Success(
        val checkpoint: OnboardingCheckpoint,
    ) : PersistOnboardingCheckpointResult

    data class Failure(
        val checkpoint: OnboardingCheckpoint,
    ) : PersistOnboardingCheckpointResult
}

class PersistOnboardingCheckpointUseCase @Inject constructor() {
    suspend operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        repository: OnboardingRepository,
    ): PersistOnboardingCheckpointResult {
        return try {
            repository.saveCheckpoint(checkpoint)
            PersistOnboardingCheckpointResult.Success(checkpoint)
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            PersistOnboardingCheckpointResult.Failure(checkpoint)
        }
    }
}
