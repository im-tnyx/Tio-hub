package com.tnyx.features.onboarding.presentation

import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import javax.inject.Inject

enum class OnboardingCheckpointUiStatus {
    Ready,
    Saving,
    PersistenceError,
}

class OnboardingCheckpointUiStateFactory @Inject constructor(
    private val uiStateFactory: OnboardingUiStateFactory,
) {
    constructor() : this(
        uiStateFactory = OnboardingUiStateFactory(),
    )

    operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        flow: OnboardingFlowDefinition,
        status: OnboardingCheckpointUiStatus = OnboardingCheckpointUiStatus.Ready,
    ): OnboardingUiState {
        return when (status) {
            OnboardingCheckpointUiStatus.Ready -> {
                uiStateFactory(
                    checkpoint = checkpoint,
                    flow = flow,
                )
            }

            OnboardingCheckpointUiStatus.Saving -> {
                uiStateFactory(
                    checkpoint = checkpoint,
                    flow = flow,
                    isSaving = true,
                )
            }

            OnboardingCheckpointUiStatus.PersistenceError -> {
                uiStateFactory(
                    checkpoint = checkpoint,
                    flow = flow,
                    hasPersistenceError = true,
                )
            }
        }
    }
}
