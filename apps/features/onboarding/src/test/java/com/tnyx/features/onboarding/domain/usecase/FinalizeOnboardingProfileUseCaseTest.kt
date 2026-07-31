package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.shared.profile.domain.model.ProfileJourney
import com.tnyx.shared.profile.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinalizeOnboardingProfileUseCaseTest {
    private val useCase = FinalizeOnboardingProfileUseCase()

    @Test
    fun mapsOnboardingDraftIntoCompletedProfile() {
        val profile = useCase(
            draft = OnboardingDraft()
                .withAnswer(
                    OnboardingStepIds.ProfileName,
                    OnboardingAnswer.Text("Santosh Kumar"),
                )
                .withAnswer(
                    OnboardingStepIds.ProfileDateOfBirth,
                    OnboardingAnswer.Text("1990-01-01"),
                )
                .withAnswer(
                    OnboardingStepIds.ProfileGender,
                    OnboardingAnswer.Text("male"),
                )
                .withAnswer(
                    OnboardingStepIds.MobileNumber,
                    OnboardingAnswer.Text("+91 9876543210"),
                )
                .withAnswer(
                    OnboardingStepIds.BodyGoalHeight,
                    OnboardingAnswer.Decimal(176.0),
                )
                .withAnswer(
                    OnboardingStepIds.BodyGoalCurrentWeight,
                    OnboardingAnswer.Decimal(74.5),
                )
                .withAnswer(
                    OnboardingStepIds.BodyGoalTargetWeight,
                    OnboardingAnswer.Decimal(70.0),
                ),
            currentProfile = baseProfile(),
        )

        assertEquals("Santosh Kumar", profile.displayName)
        assertEquals("1990-01-01", profile.dob)
        assertEquals("male", profile.gender)
        assertEquals("+91 9876543210", profile.mobile)
        assertEquals(176, profile.height)
        assertEquals(74.5, profile.weight, 0.0)
        assertEquals(74.5, profile.currentJourney.initialWeight, 0.0)
        assertEquals(70.0, profile.currentJourney.targetWeight, 0.0)
        assertTrue(profile.hasCompletedOnboarding)
    }

    @Test
    fun keepsExistingProfileValuesWhenDraftDoesNotProvideThem() {
        val profile = useCase(
            draft = OnboardingDraft(),
            currentProfile = baseProfile(),
        )

        assertEquals("Existing User", profile.displayName)
        assertEquals("1988-12-01", profile.dob)
        assertEquals("female", profile.gender)
        assertEquals("", profile.mobile)
        assertEquals(165, profile.height)
        assertEquals(68.0, profile.weight, 0.0)
        assertEquals(68.0, profile.currentJourney.initialWeight, 0.0)
        assertEquals(62.0, profile.currentJourney.targetWeight, 0.0)
        assertTrue(profile.hasCompletedOnboarding)
    }

    private fun baseProfile(): UserProfile {
        return UserProfile(
            id = "local-guest",
            displayName = "Existing User",
            dob = "1988-12-01",
            gender = "female",
            planLabel = "",
            weight = 68.0,
            height = 165,
            bmi = 0.0,
            bmr = 0,
            currentJourney = ProfileJourney(
                initialWeight = 70.0,
                targetWeight = 62.0,
            ),
        )
    }
}
