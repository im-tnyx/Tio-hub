package com.tnyx.features.onboarding.presentation.sections.workoutintro.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.common.OnboardingChoiceCard
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading
import com.tnyx.features.onboarding.presentation.common.OnboardingValidationMessage

@Composable
internal fun WorkoutIntroChoiceStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selection = (answer as? OnboardingAnswer.Toggle)?.value

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        OnboardingStepHeading(
            title = "Do you want a workout plan too?",
            description = "We can tailor a training setup around your schedule, space, and experience level.",
        )
        OnboardingChoiceCard(
            title = "Yes, build my workout plan",
            description = "Include coaching for workout experience, training days, equipment, and duration.",
            selected = selection == true,
            onClick = { onAnswerChanged(OnboardingAnswer.Toggle(true)) },
            badge = "Recommended",
        )
        OnboardingChoiceCard(
            title = "Not right now",
            description = "Skip workout setup for now and continue with your daily targets and source details.",
            selected = selection == false,
            onClick = { onAnswerChanged(OnboardingAnswer.Toggle(false)) },
        )
        if (showValidationError) {
            OnboardingValidationMessage("Choose whether you want workout coaching right now")
        }
    }
}
