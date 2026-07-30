package com.tnyx.features.onboarding.presentation.sections

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.presentation.OnboardingAction
import com.tnyx.features.onboarding.presentation.OnboardingValidationError
import com.tnyx.features.onboarding.presentation.sections.bodygoal.BodyGoalSectionContent
import com.tnyx.features.onboarding.presentation.sections.intro.IntroSectionContent
import com.tnyx.features.onboarding.presentation.sections.mobile.MobileSectionContent
import com.tnyx.features.onboarding.presentation.sections.profile.ProfileSectionContent
import com.tnyx.features.onboarding.presentation.sections.review.ReviewSectionContent
import com.tnyx.features.onboarding.presentation.sections.source.SourceSectionContent
import com.tnyx.features.onboarding.presentation.sections.targets.TargetsSectionContent
import com.tnyx.features.onboarding.presentation.sections.workout.WorkoutSectionContent
import com.tnyx.features.onboarding.presentation.sections.workoutintro.WorkoutIntroSectionContent

@Composable
internal fun OnboardingSectionContent(
    position: OnboardingPosition,
    currentAnswer: OnboardingAnswer?,
    draftAnswers: Map<OnboardingStepId, OnboardingAnswer>,
    validationError: OnboardingValidationError?,
    onAction: (OnboardingAction) -> Unit,
) {
    key(position.stepId.value) {
        when (position.sectionId) {
            OnboardingSectionIds.Intro -> {
                IntroSectionContent(
                    stepId = position.stepId,
                )
            }

            OnboardingSectionIds.Profile -> {
                ProfileSectionContent(
                    stepId = position.stepId,
                    answer = currentAnswer,
                    showValidationError = validationError == OnboardingValidationError.RequiredAnswerInvalid,
                    onAnswerChanged = { answer ->
                        onAction(OnboardingAction.AnswerChanged(answer))
                    },
                )
            }

            OnboardingSectionIds.BodyGoal -> {
                BodyGoalSectionContent(
                    stepId = position.stepId,
                    answer = currentAnswer,
                    showValidationError = validationError == OnboardingValidationError.RequiredAnswerInvalid,
                    onAnswerChanged = { answer ->
                        onAction(OnboardingAction.AnswerChanged(answer))
                    },
                )
            }

            OnboardingSectionIds.Mobile -> {
                MobileSectionContent(
                    stepId = position.stepId,
                    answer = currentAnswer,
                    showValidationError = validationError == OnboardingValidationError.RequiredAnswerInvalid,
                    onAnswerChanged = { answer ->
                        onAction(OnboardingAction.AnswerChanged(answer))
                    },
                )
            }

            OnboardingSectionIds.Workout -> {
                WorkoutSectionContent(
                    stepId = position.stepId,
                    answer = currentAnswer,
                    showValidationError = validationError == OnboardingValidationError.RequiredAnswerInvalid,
                    onAnswerChanged = { answer ->
                        onAction(OnboardingAction.AnswerChanged(answer))
                    },
                )
            }

            OnboardingSectionIds.WorkoutIntro -> {
                WorkoutIntroSectionContent(
                    stepId = position.stepId,
                    answer = currentAnswer,
                    showValidationError = validationError == OnboardingValidationError.RequiredAnswerInvalid,
                    onAnswerChanged = { answer ->
                        onAction(OnboardingAction.AnswerChanged(answer))
                    },
                )
            }

            OnboardingSectionIds.Targets -> {
                TargetsSectionContent(
                    stepId = position.stepId,
                    answer = currentAnswer,
                    draftAnswers = draftAnswers,
                    showValidationError = validationError == OnboardingValidationError.RequiredAnswerInvalid,
                    onAnswerChanged = { answer ->
                        onAction(OnboardingAction.AnswerChanged(answer))
                    },
                )
            }

            OnboardingSectionIds.Source -> {
                SourceSectionContent(
                    stepId = position.stepId,
                    answer = currentAnswer,
                    draftAnswers = draftAnswers,
                    showValidationError = validationError == OnboardingValidationError.RequiredAnswerInvalid,
                    onAnswerChanged = { answer ->
                        onAction(OnboardingAction.AnswerChanged(answer))
                    },
                )
            }

            OnboardingSectionIds.Review -> {
                ReviewSectionContent(
                    answer = currentAnswer,
                    draftAnswers = draftAnswers,
                    showValidationError = validationError == OnboardingValidationError.RequiredAnswerInvalid,
                    onAnswerChanged = { answer ->
                        onAction(OnboardingAction.AnswerChanged(answer))
                    },
                )
            }

            else -> {
                Text(
                    text = "Unsupported onboarding section: ${position.sectionId.value}",
                    style = TnyxTheme.typography.bodyLarge,
                    color = TnyxTheme.colors.error,
                )
            }
        }
    }
}
