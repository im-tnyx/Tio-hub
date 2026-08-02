package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingAuthState
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingRouteContext
import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.profile.domain.model.UserProfile
import javax.inject.Inject

class SyncOnboardingRouteContextUseCase @Inject constructor() {
    operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        currentProfile: UserProfile?,
        authSession: AuthSession?,
    ): OnboardingCheckpoint {
        val existingContext = checkpoint.routeContext
        val hasPermanentSession = authSession != null && !authSession.isDemo
        val routeContext = existingContext.copy(
            authState = if (hasPermanentSession) {
                OnboardingAuthState.SignedIn
            } else {
                OnboardingAuthState.SignedOut
            },
            signupCompleted = existingContext.signupCompleted || hasPermanentSession,
            workoutPlanEnabled = workoutPlanEnabled(checkpoint, existingContext),
            mobilePresent = existingContext.mobilePresent ||
                currentProfile?.mobile.orEmpty().isNotBlank(),
            namePrefilled = existingContext.namePrefilled ||
                currentProfile?.displayName.orEmpty().isNotBlank() ||
                currentProfile?.username.orEmpty().isNotBlank() ||
                authSession?.displayName.orEmpty().isNotBlank(),
            authRequired = existingContext.authRequired && !hasPermanentSession,
        )

        return checkpoint.copy(routeContext = routeContext)
    }

    private fun workoutPlanEnabled(
        checkpoint: OnboardingCheckpoint,
        existingContext: OnboardingRouteContext,
    ): Boolean? {
        return (checkpoint.draft.answerFor(OnboardingStepIds.WorkoutIntroChoice) as? OnboardingAnswer.Toggle)
            ?.value
            ?: existingContext.workoutPlanEnabled
    }
}
