package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.shared.profile.domain.model.UserProfile
import javax.inject.Inject

class FinalizeOnboardingProfileUseCase @Inject constructor() {
    operator fun invoke(
        draft: OnboardingDraft,
        currentProfile: UserProfile,
    ): UserProfile {
        val displayName = draft.textValue(OnboardingStepIds.ProfileName)
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: currentProfile.displayName
        val dateOfBirth = draft.textValue(OnboardingStepIds.ProfileDateOfBirth)
            ?.takeIf(String::isNotBlank)
            ?: currentProfile.dob
        val gender = draft.textValue(OnboardingStepIds.ProfileGender)
            ?.takeIf(String::isNotBlank)
            ?: currentProfile.gender
        val mobile = draft.textValue(OnboardingStepIds.MobileNumber)
            ?.takeIf(String::isNotBlank)
            ?: currentProfile.mobile
        val heightCm = draft.decimalValue(OnboardingStepIds.BodyGoalHeight)
            ?.toInt()
            ?: currentProfile.height
        val currentWeight = draft.decimalValue(OnboardingStepIds.BodyGoalCurrentWeight)
            ?: currentProfile.weight
        val targetWeight = draft.decimalValue(OnboardingStepIds.BodyGoalTargetWeight)
            ?: currentProfile.currentJourney.targetWeight

        return currentProfile.copy(
            displayName = displayName,
            dob = dateOfBirth,
            gender = gender,
            mobile = mobile,
            height = heightCm,
            weight = currentWeight,
            currentJourney = currentProfile.currentJourney.copy(
                initialWeight = currentWeight.takeIf { it > 0.0 }
                    ?: currentProfile.currentJourney.initialWeight,
                targetWeight = targetWeight.takeIf { it > 0.0 }
                    ?: currentProfile.currentJourney.targetWeight,
            ),
            hasCompletedOnboarding = true,
        )
    }
}

private fun OnboardingDraft.textValue(stepId: OnboardingStepId): String? {
    return (answerFor(stepId) as? OnboardingAnswer.Text)?.value
}

private fun OnboardingDraft.decimalValue(stepId: OnboardingStepId): Double? {
    return (answerFor(stepId) as? OnboardingAnswer.Decimal)?.value
}
