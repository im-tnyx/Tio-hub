package com.tnyx.features.onboarding.presentation.sections.workout.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.inputs.TnyxTextField
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading

@Composable
internal fun WorkoutSpecialEventStep(
    answer: OnboardingAnswer?,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialValue = (answer as? OnboardingAnswer.Text)?.value.orEmpty()
    var eventGoal by rememberSaveable { mutableStateOf(initialValue) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        OnboardingStepHeading(
            title = "Training toward a specific event or deadline?",
            description = "Share any race, trip, photoshoot, wedding, or milestone that should shape future planning.",
        )
        TnyxTextField(
            value = eventGoal,
            onValueChange = { updatedValue ->
                eventGoal = updatedValue
                onAnswerChanged(
                    updatedValue
                        .trim()
                        .takeIf(String::isNotBlank)
                        ?.let { value -> OnboardingAnswer.Text(value) },
                )
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Special event") },
            placeholder = { Text("Example: 10K race in October, wedding in December, vacation beach trip") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default,
            ),
            singleLine = true,
            helperMessage = "Optional. Leave blank if you just want a general routine for now.",
        )
    }
}
