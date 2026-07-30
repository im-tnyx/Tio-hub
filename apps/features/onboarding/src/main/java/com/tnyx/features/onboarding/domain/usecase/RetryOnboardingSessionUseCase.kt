package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import javax.inject.Inject

sealed interface RetryOnboardingSessionResult {
    data object Reinitialize : RetryOnboardingSessionResult

    data class Persisted(
        val checkpoint: OnboardingCheckpoint,
    ) : RetryOnboardingSessionResult

    data class PersistFailed(
        val checkpoint: OnboardingCheckpoint,
    ) : RetryOnboardingSessionResult

    data class Completed(
        val checkpoint: OnboardingCheckpoint,
    ) : RetryOnboardingSessionResult

    data class CompleteFailed(
        val checkpoint: OnboardingCheckpoint,
    ) : RetryOnboardingSessionResult
}

class RetryOnboardingSessionUseCase @Inject constructor(
    private val resolveOnboardingRetry: ResolveOnboardingRetryUseCase,
    private val persistOnboardingCheckpoint: PersistOnboardingCheckpointUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
) {
    constructor() : this(
        resolveOnboardingRetry = ResolveOnboardingRetryUseCase(),
        persistOnboardingCheckpoint = PersistOnboardingCheckpointUseCase(),
        completeOnboardingUseCase = CompleteOnboardingUseCase(),
    )

    suspend operator fun invoke(
        checkpoint: OnboardingCheckpoint?,
        onboardingRepository: OnboardingRepository,
        profileRepository: ProfileRepository,
        finalizeOnboardingProfile: FinalizeOnboardingProfileUseCase,
    ): RetryOnboardingSessionResult {
        return when (val result = resolveOnboardingRetry(checkpoint)) {
            ResolveOnboardingRetryResult.Reinitialize -> RetryOnboardingSessionResult.Reinitialize
            is ResolveOnboardingRetryResult.Persist -> {
                when (persistOnboardingCheckpoint(result.checkpoint, onboardingRepository)) {
                    is PersistOnboardingCheckpointResult.Success -> {
                        RetryOnboardingSessionResult.Persisted(result.checkpoint)
                    }

                    is PersistOnboardingCheckpointResult.Failure -> {
                        RetryOnboardingSessionResult.PersistFailed(result.checkpoint)
                    }
                }
            }

            is ResolveOnboardingRetryResult.Complete -> {
                when (
                    completeOnboardingUseCase(
                        checkpoint = result.checkpoint,
                        persistCheckpoint = true,
                        onboardingRepository = onboardingRepository,
                        profileRepository = profileRepository,
                        finalizeOnboardingProfile = finalizeOnboardingProfile,
                    )
                ) {
                    is CompleteOnboardingResult.Success -> {
                        RetryOnboardingSessionResult.Completed(result.checkpoint)
                    }

                    is CompleteOnboardingResult.Failure -> {
                        RetryOnboardingSessionResult.CompleteFailed(result.checkpoint)
                    }
                }
            }
        }
    }
}
