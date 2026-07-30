package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.profile.domain.model.UserProfile
import javax.inject.Inject

class PrepareOnboardingCheckpointUseCase @Inject constructor(
    private val seedOnboardingProfileDraft: SeedOnboardingProfileDraftUseCase,
    private val seedOnboardingRecommendations: SeedOnboardingRecommendationsUseCase,
    private val syncOnboardingRouteContext: SyncOnboardingRouteContextUseCase,
    private val alignOnboardingCheckpoint: AlignOnboardingCheckpointUseCase,
) {
    constructor() : this(
        seedOnboardingProfileDraft = SeedOnboardingProfileDraftUseCase(),
        seedOnboardingRecommendations = SeedOnboardingRecommendationsUseCase(),
        syncOnboardingRouteContext = SyncOnboardingRouteContextUseCase(),
        alignOnboardingCheckpoint = AlignOnboardingCheckpointUseCase(),
    )

    operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        currentProfile: UserProfile?,
        authSession: AuthSession?,
    ): OnboardingCheckpoint {
        val preparedCheckpoint = syncOnboardingRouteContext(
            checkpoint = seedOnboardingRecommendations(
                seedOnboardingProfileDraft(
                    checkpoint = checkpoint,
                    currentProfile = currentProfile,
                ),
            ),
            currentProfile = currentProfile,
            authSession = authSession,
        )
        return alignOnboardingCheckpoint(
            checkpoint = preparedCheckpoint,
            flow = DefaultOnboardingFlow.definition,
        )
    }
}
