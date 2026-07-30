package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.profile.domain.model.ProfileJourney
import com.tnyx.shared.profile.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrepareOnboardingCheckpointUseCaseTest {
    private val useCase = PrepareOnboardingCheckpointUseCase()

    @Test
    fun seedsProfileAnswersAndAlignsFreshSignedInCheckpoint() {
        val result = useCase(
            checkpoint = OnboardingCheckpoint(
                progress = OnboardingProgress(
                    flowVersion = DefaultOnboardingFlow.VERSION,
                    position = OnboardingPosition(
                        sectionId = OnboardingSectionIds.Intro,
                        stepId = OnboardingStepIds.IntroWelcome,
                    ),
                ),
            ),
            currentProfile = UserProfile(
                id = "user-1",
                displayName = "Santosh",
                dob = "1990-01-01",
                gender = "male",
                planLabel = "",
                weight = 82.0,
                height = 177,
                bmi = 0.0,
                bmr = 0,
                mobile = "+91 9876543210",
                currentJourney = ProfileJourney(targetWeight = 75.0),
            ),
            authSession = AuthSession(
                userId = "user-1",
                email = "santosh@example.com",
                displayName = "Santosh",
                isDemo = false,
            ),
        )

        assertEquals(OnboardingStepIds.ProfileName, result.progress.position.stepId)
        assertEquals(
            OnboardingAnswer.Text("Santosh"),
            result.draft.answerFor(OnboardingStepIds.ProfileName),
        )
        assertEquals(
            OnboardingAnswer.Text("1990-01-01"),
            result.draft.answerFor(OnboardingStepIds.ProfileDateOfBirth),
        )
        assertEquals(
            OnboardingAnswer.Text("+91 9876543210"),
            result.draft.answerFor(OnboardingStepIds.MobileNumber),
        )
        assertEquals(
            OnboardingAnswer.Decimal(177.0),
            result.draft.answerFor(OnboardingStepIds.BodyGoalHeight),
        )
        assertEquals(
            OnboardingAnswer.Decimal(82.0),
            result.draft.answerFor(OnboardingStepIds.BodyGoalCurrentWeight),
        )
        assertEquals(
            OnboardingAnswer.Decimal(75.0),
            result.draft.answerFor(OnboardingStepIds.BodyGoalTargetWeight),
        )
        assertTrue(result.routeContext.signupCompleted)
        assertTrue(result.routeContext.mobilePresent)
        assertTrue(result.routeContext.namePrefilled)
    }
}
