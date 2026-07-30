package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
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

class CompleteOnboardingUseCase @Inject constructor() {
    suspend operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        persistCheckpoint: Boolean,
        onboardingRepository: OnboardingRepository,
        profileRepository: ProfileRepository,
        finalizeOnboardingProfile: FinalizeOnboardingProfileUseCase,
    ): CompleteOnboardingResult {
        return try {
            if (persistCheckpoint) {
                onboardingRepository.saveCheckpoint(checkpoint)
            }
            profileRepository.updateProfile(
                finalizeOnboardingProfile(
                    checkpoint.draft,
                    profileRepository.getCurrentProfile().first(),
                ),
            )
            runCatching {
                onboardingRepository.clearCheckpoint()
            }
            CompleteOnboardingResult.Success(checkpoint)
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            CompleteOnboardingResult.Failure(checkpoint)
        }
    }
}
