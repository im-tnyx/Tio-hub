package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.shared.profile.domain.model.UserProfile
import javax.inject.Inject

class SeedOnboardingProfileDraftUseCase @Inject constructor() {
    operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        currentProfile: UserProfile?,
    ): OnboardingCheckpoint {
        currentProfile ?: return checkpoint

        var draft = checkpoint.draft
        profileName(currentProfile)?.let { value ->
            if (draft.answerFor(OnboardingStepIds.ProfileName) == null) {
                draft = draft.withAnswer(OnboardingStepIds.ProfileName, OnboardingAnswer.Text(value))
            }
        }
        stableGender(currentProfile.gender)?.let { value ->
            if (draft.answerFor(OnboardingStepIds.ProfileGender) == null) {
                draft = draft.withAnswer(OnboardingStepIds.ProfileGender, OnboardingAnswer.Text(value))
            }
        }
        currentProfile.dob.trim().takeIf(String::isNotBlank)?.let { value ->
            if (draft.answerFor(OnboardingStepIds.ProfileDateOfBirth) == null) {
                draft = draft.withAnswer(OnboardingStepIds.ProfileDateOfBirth, OnboardingAnswer.Text(value))
            }
        }
        currentProfile.mobile.trim().takeIf(String::isNotBlank)?.let { value ->
            if (draft.answerFor(OnboardingStepIds.MobileNumber) == null) {
                draft = draft.withAnswer(OnboardingStepIds.MobileNumber, OnboardingAnswer.Text(value))
            }
        }
        currentProfile.height.takeIf { it > 0 }?.toDouble()?.let { value ->
            if (draft.answerFor(OnboardingStepIds.BodyGoalHeight) == null) {
                draft = draft.withAnswer(OnboardingStepIds.BodyGoalHeight, OnboardingAnswer.Decimal(value))
            }
        }
        currentProfile.weight.takeIf { it > 0.0 }?.let { value ->
            if (draft.answerFor(OnboardingStepIds.BodyGoalCurrentWeight) == null) {
                draft = draft.withAnswer(
                    OnboardingStepIds.BodyGoalCurrentWeight,
                    OnboardingAnswer.Decimal(value),
                )
            }
        }
        currentProfile.currentJourney.targetWeight.takeIf { it > 0.0 }?.let { value ->
            if (draft.answerFor(OnboardingStepIds.BodyGoalTargetWeight) == null) {
                draft = draft.withAnswer(
                    OnboardingStepIds.BodyGoalTargetWeight,
                    OnboardingAnswer.Decimal(value),
                )
            }
        }

        return checkpoint.copy(draft = draft)
    }

    private fun profileName(currentProfile: UserProfile): String? {
        return currentProfile.displayName.trim().takeIf(String::isNotBlank)
            ?: currentProfile.username.trim().removePrefix("@").takeIf(String::isNotBlank)
    }

    private fun stableGender(value: String): String? {
        return when (value.trim().lowercase()) {
            "male" -> "male"
            "female" -> "female"
            "prefer_not_to_say" -> "prefer_not_to_say"
            else -> null
        }
    }
}
