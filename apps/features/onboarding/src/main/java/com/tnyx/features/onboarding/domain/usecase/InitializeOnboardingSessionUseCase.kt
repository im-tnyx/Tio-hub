package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

sealed interface InitializeOnboardingSessionResult {
    data object ProfileAlreadyCompleted : InitializeOnboardingSessionResult

    data class ResumeCompletedCheckpoint(
        val checkpoint: OnboardingCheckpoint,
        val persistCheckpoint: Boolean,
    ) : InitializeOnboardingSessionResult

    data class Ready(
        val checkpoint: OnboardingCheckpoint,
    ) : InitializeOnboardingSessionResult
}

class InitializeOnboardingSessionUseCase @Inject constructor(
    private val resolveOnboardingInitialization: ResolveOnboardingInitializationUseCase,
    private val persistOnboardingCheckpoint: PersistOnboardingCheckpointUseCase,
) {
    constructor() : this(
        resolveOnboardingInitialization = ResolveOnboardingInitializationUseCase(),
        persistOnboardingCheckpoint = PersistOnboardingCheckpointUseCase(),
    )

    suspend operator fun invoke(
        flow: OnboardingFlowDefinition,
        onboardingRepository: OnboardingRepository,
        profileRepository: ProfileRepository,
    ): InitializeOnboardingSessionResult {
        val currentProfile = profileRepository.getCurrentProfile().first()
        val storedCheckpoint = onboardingRepository.observeCheckpoint().first()

        return when (
            val result = resolveOnboardingInitialization(
                hasCompletedOnboarding = currentProfile.hasCompletedOnboarding,
                storedCheckpoint = storedCheckpoint,
                flow = flow,
            )
        ) {
            ResolveOnboardingInitializationResult.ProfileAlreadyCompleted -> {
                InitializeOnboardingSessionResult.ProfileAlreadyCompleted
            }

            is ResolveOnboardingInitializationResult.Ready -> {
                if (result.shouldPersistCheckpoint) {
                    when (persistOnboardingCheckpoint(result.checkpoint, onboardingRepository)) {
                        is PersistOnboardingCheckpointResult.Failure -> {
                            error("Failed to save checkpoint")
                        }

                        is PersistOnboardingCheckpointResult.Success -> Unit
                    }
                }
                InitializeOnboardingSessionResult.Ready(result.checkpoint)
            }

            is ResolveOnboardingInitializationResult.ResumeCompletedCheckpoint -> {
                InitializeOnboardingSessionResult.ResumeCompletedCheckpoint(
                    checkpoint = result.checkpoint,
                    persistCheckpoint = result.persistCheckpoint,
                )
            }
        }
    }
}
