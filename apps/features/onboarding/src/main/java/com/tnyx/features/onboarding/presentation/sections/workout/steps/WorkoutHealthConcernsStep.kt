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
internal fun WorkoutHealthConcernsStep(
    answer: OnboardingAnswer?,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialValue = (answer as? OnboardingAnswer.Text)?.value.orEmpty()
    var concerns by rememberSaveable { mutableStateOf(initialValue) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        OnboardingStepHeading(
            title = "Any workout-specific concerns we should note?",
            description = "Share injuries, movement limits, or anything else that should shape future training guidance.",
        )
        TnyxTextField(
            value = concerns,
            onValueChange = { updatedValue ->
                concerns = updatedValue
                onAnswerChanged(
                    updatedValue
                        .trim()
                        .takeIf(String::isNotBlank)
                        ?.let { value -> OnboardingAnswer.Text(value) },
                )
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Workout concerns") },
            placeholder = { Text("Example: knee pain during squats, shoulder mobility, recent lower-back strain") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default,
            ),
            singleLine = false,
            minLines = 4,
            maxLines = 8,
            helperMessage = "Optional. You can leave this blank and refine it later.",
        )
    }
}
