package com.tnyx.features.onboarding.presentation.sections.intro.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading

data class ExperienceModeOption(
    val id: String,
    val title: String,
    val description: String,
    val tabPreview: String,
)

private val EXPERIENCE_OPTIONS = listOf(
    ExperienceModeOption(
        id = "balanced",
        title = "Balanced Experience",
        description = "Complete fitness, workout tracking, nutrition logging, and progress analytics.",
        tabPreview = "Home | Nutrition | Tio (AI) | Workout | Progress",
    ),
    ExperienceModeOption(
        id = "workout",
        title = "Workout Focus",
        description = "Designed for strength training & gym tracking with quick workout access.",
        tabPreview = "Home | Workout | Workout Library | Progress",
    ),
    ExperienceModeOption(
        id = "nutrition",
        title = "Nutrition Focus",
        description = "Diet-first layout focused on macro targets, meal planning, and body metrics.",
        tabPreview = "Home | Nutrition | Meal Plan | Progress",
    ),
)

@Composable
internal fun IntroExperienceModeStep(
    answer: OnboardingAnswer?,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedId = (answer as? OnboardingAnswer.Text)?.value

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        OnboardingStepHeading(
            title = "Choose your experience mode",
            description = "Select how you want your app layout and bottom navigation organized. You can change this anytime later in Settings.",
        )

        EXPERIENCE_OPTIONS.forEach { option ->
            val isSelected = option.id == selectedId

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) TnyxTheme.colors.primary.copy(alpha = 0.08f) else TnyxTheme.colors.surfaceVariant,
                border = BorderStroke(
                    1.5.dp,
                    if (isSelected) TnyxTheme.colors.primary else TnyxTheme.colors.textSecondary.copy(alpha = 0.15f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onAnswerChanged(OnboardingAnswer.Text(option.id))
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = TnyxTheme.colors.primary,
                            unselectedColor = TnyxTheme.colors.textSecondary.copy(alpha = 0.5f)
                        )
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text(
                            text = option.title,
                            style = TnyxTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TnyxTheme.colors.textPrimary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = option.description,
                            style = TnyxTheme.typography.bodyMedium,
                            color = TnyxTheme.colors.textSecondary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TnyxTheme.colors.background.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = option.tabPreview,
                                style = TnyxTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) TnyxTheme.colors.primary else TnyxTheme.colors.textSecondary,
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
