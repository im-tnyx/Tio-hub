package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import javax.inject.Inject

sealed interface ContinueOnboardingSessionResult {
    data class Persisted(
        val checkpoint: com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint,
    ) : ContinueOnboardingSessionResult

    data class PersistFailed(
        val checkpoint: com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint,
    ) : ContinueOnboardingSessionResult

    data class Completed(
        val checkpoint: com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint,
    ) : ContinueOnboardingSessionResult

    data class CompleteFailed(
        val checkpoint: com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint,
    ) : ContinueOnboardingSessionResult

}

class ContinueOnboardingSessionUseCase @Inject constructor(
    private val persistOnboardingCheckpoint: PersistOnboardingCheckpointUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
) {
    constructor() : this(
        persistOnboardingCheckpoint = PersistOnboardingCheckpointUseCase(),
        completeOnboardingUseCase = CompleteOnboardingUseCase(),
    )

    suspend operator fun invoke(
        advanceResult: AdvanceOnboardingStepResult,
        onboardingRepository: OnboardingRepository,
        profileRepository: ProfileRepository,
        finalizeOnboardingProfile: FinalizeOnboardingProfileUseCase,
    ): ContinueOnboardingSessionResult {
        return when (advanceResult) {
            is AdvanceOnboardingStepResult.Next -> {
                when (persistOnboardingCheckpoint(advanceResult.checkpoint, onboardingRepository)) {
                    is PersistOnboardingCheckpointResult.Success -> {
                        ContinueOnboardingSessionResult.Persisted(advanceResult.checkpoint)
                    }

                    is PersistOnboardingCheckpointResult.Failure -> {
                        ContinueOnboardingSessionResult.PersistFailed(advanceResult.checkpoint)
                    }
                }
            }

            is AdvanceOnboardingStepResult.Completed -> {
                val completionResult = completeOnboardingUseCase(
                    checkpoint = advanceResult.checkpoint,
                    persistCheckpoint = true,
                    onboardingRepository = onboardingRepository,
                    profileRepository = profileRepository,
                    finalizeOnboardingProfile = finalizeOnboardingProfile,
                )
                when (completionResult) {
                    is CompleteOnboardingResult.Success -> {
                        ContinueOnboardingSessionResult.Completed(advanceResult.checkpoint)
                    }

                    is CompleteOnboardingResult.Failure -> {
                        ContinueOnboardingSessionResult.CompleteFailed(advanceResult.checkpoint)
                    }
                }
            }
        }
    }
}
