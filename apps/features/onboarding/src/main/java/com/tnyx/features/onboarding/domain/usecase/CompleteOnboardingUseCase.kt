package com.tnyx.features.onboarding.domain.usecase

import android.util.Log
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.repository.OnboardingCompletionSyncRepository
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import com.tnyx.features.onboarding.domain.resume.ResumeManager
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

sealed interface CompleteOnboardingResult {
    data class Success(
        val checkpoint: OnboardingCheckpoint,
    ) : CompleteOnboardingResult

    data class Failure(
        val checkpoint: OnboardingCheckpoint,
    ) : CompleteOnboardingResult
}

class CompleteOnboardingUseCase @Inject constructor(
    private val resumeManager: ResumeManager,
    private val onboardingCompletionSyncRepository: OnboardingCompletionSyncRepository,
) {
    companion object {
        private const val LogTag = "OnboardingCompletion"
    }

    constructor() : this(
        resumeManager = CompleteNoOpResumeManager,
        onboardingCompletionSyncRepository = NoOpOnboardingCompletionSyncRepository,
    )

    suspend operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        persistCheckpoint: Boolean,
        onboardingRepository: OnboardingRepository,
        profileRepository: ProfileRepository,
        finalizeOnboardingProfile: FinalizeOnboardingProfileUseCase,
    ): CompleteOnboardingResult {
        return try {
            if (!checkpoint.progress.isCompleted) {
                logError(
                    "Completion rejected because the final checkpoint is not marked completed: " +
                        "section=${checkpoint.progress.position.sectionId.value}, " +
                        "step=${checkpoint.progress.position.stepId.value}",
                )
                return CompleteOnboardingResult.Failure(checkpoint)
            }
            if (persistCheckpoint) {
                runCatching { onboardingRepository.saveCheckpoint(checkpoint) }
                    .onFailure { error ->
                        logWarning("Unable to save recovery checkpoint; continuing remote sync", error)
                    }
            }
            logInfo("Updating profile for completed onboarding")
            profileRepository.updateProfile(
                finalizeOnboardingProfile(
                    checkpoint.draft,
                    profileRepository.getCurrentProfile().first(),
                ),
            )
            logInfo("Syncing nutrition and workout onboarding data")
            onboardingCompletionSyncRepository.syncCompletedOnboarding(checkpoint.draft)
            runCatching {
                onboardingRepository.clearCheckpoint()
            }.onFailure { error ->
                logWarning("Unable to clear local onboarding checkpoint after sync", error)
            }
            runCatching { resumeManager.clearCheckpoint() }.onFailure { error ->
                logWarning("Unable to clear resume checkpoint after sync", error)
            }
            logInfo("Completed onboarding sync successfully")
            CompleteOnboardingResult.Success(checkpoint)
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            logError("Completed onboarding sync failed", exception)
            CompleteOnboardingResult.Failure(checkpoint)
        }
    }

    private fun logInfo(message: String) {
        runCatching { Log.i(LogTag, message) }
    }

    private fun logWarning(message: String, error: Throwable) {
        runCatching { Log.w(LogTag, message, error) }
    }

    private fun logError(message: String, error: Throwable? = null) {
        runCatching {
            if (error == null) {
                Log.e(LogTag, message)
            } else {
                Log.e(LogTag, message, error)
            }
        }
    }
}

private object CompleteNoOpResumeManager : ResumeManager {
    override suspend fun restoreCheckpoint(): OnboardingCheckpoint? = null

    override suspend fun saveCheckpoint(checkpoint: OnboardingCheckpoint) = Unit

    override suspend fun clearCheckpoint() = Unit
}

private object NoOpOnboardingCompletionSyncRepository : OnboardingCompletionSyncRepository {
    override suspend fun syncCompletedOnboarding(
        draft: com.tnyx.features.onboarding.domain.model.OnboardingDraft,
    ) = Unit
}
