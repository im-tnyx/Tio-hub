package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingAuthState
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.profile.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncOnboardingRouteContextUseCaseTest {
    private val useCase = SyncOnboardingRouteContextUseCase()

    @Test
    fun derivesRouteContextFromSessionProfileAndDraft() {
        val checkpoint = OnboardingCheckpoint(
            draft = OnboardingDraft()
                .withAnswer(OnboardingStepIds.WorkoutIntroChoice, OnboardingAnswer.Toggle(false)),
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = OnboardingPosition(
                    sectionId = OnboardingSectionIds.Mobile,
                    stepId = OnboardingStepIds.MobileNumber,
                ),
            ),
        )

        val result = useCase(
            checkpoint = checkpoint,
            currentProfile = UserProfile(
                id = "user-1",
                displayName = "",
                dob = "",
                gender = "",
                planLabel = "",
                weight = 0.0,
                height = 0,
                bmi = 0.0,
                bmr = 0,
                mobile = "+91 9876543210",
            ),
            authSession = AuthSession(
                userId = "user-1",
                email = "santosh@example.com",
                displayName = "Santosh",
                isDemo = false,
            ),
        )

        assertEquals(OnboardingAuthState.SignedIn, result.routeContext.authState)
        assertTrue(result.routeContext.signupCompleted)
        assertTrue(result.routeContext.namePrefilled)
        assertTrue(result.routeContext.mobilePresent)
        assertEquals(false, result.routeContext.workoutPlanEnabled)
    }

    @Test
    fun keepsGuestSessionOnTheGetStartedFlow() {
        val checkpoint = OnboardingCheckpoint(
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = OnboardingPosition(
                    sectionId = OnboardingSectionIds.Intro,
                    stepId = OnboardingStepIds.IntroWelcome,
                ),
            ),
            routeContext = com.tnyx.features.onboarding.domain.model.OnboardingRouteContext(
                authRequired = true,
            ),
        )

        val result = useCase(
            checkpoint = checkpoint,
            currentProfile = null,
            authSession = AuthSession(
                userId = "guest-1",
                email = "",
                displayName = null,
                isDemo = true,
            ),
        )

        assertEquals(OnboardingAuthState.SignedOut, result.routeContext.authState)
        assertEquals(false, result.routeContext.signupCompleted)
        assertTrue(result.routeContext.authRequired)
    }
}
