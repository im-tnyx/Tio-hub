package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import com.tnyx.features.onboarding.domain.resume.ResumeManager
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

class PersistOnboardingCheckpointUseCase @Inject constructor(
    private val resumeManager: ResumeManager,
) {
    constructor() : this(resumeManager = NoOpResumeManager)

    suspend operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        repository: OnboardingRepository,
    ): PersistOnboardingCheckpointResult {
        return try {
            repository.saveCheckpoint(checkpoint)
            runCatching { resumeManager.saveCheckpoint(checkpoint) }
            PersistOnboardingCheckpointResult.Success(checkpoint)
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            PersistOnboardingCheckpointResult.Failure(checkpoint)
        }
    }
}

private object NoOpResumeManager : ResumeManager {
    override suspend fun restoreCheckpoint(): OnboardingCheckpoint? = null

    override suspend fun saveCheckpoint(checkpoint: OnboardingCheckpoint) = Unit

    override suspend fun clearCheckpoint() = Unit
}
