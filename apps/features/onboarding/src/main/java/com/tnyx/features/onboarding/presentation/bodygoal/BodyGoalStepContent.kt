package com.tnyx.features.onboarding.presentation.bodygoal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.inputs.TnyxTextField
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading
import com.tnyx.features.onboarding.presentation.common.OnboardingValidationMessage
import java.math.BigDecimal

@Composable
internal fun BodyGoalStepContent(
    stepId: OnboardingStepId,
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (stepId) {
        OnboardingStepIds.BodyGoalPrimaryGoal -> BodyGoalPrimaryGoalStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.BodyGoalHeight -> BodyGoalHeightStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.BodyGoalCurrentWeight -> BodyGoalCurrentWeightStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.BodyGoalTargetWeight -> BodyGoalTargetWeightStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.BodyGoalActivityLevel -> BodyGoalActivityLevelStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        else -> Unit
    }
}

@Composable
private fun BodyGoalPrimaryGoalStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedId = (answer as? OnboardingAnswer.Text)?.value

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        OnboardingStepHeading(
            title = "What is your main goal right now?",
            description = "Choose one primary direction so Tio can shape your targets around it.",
        )
        PrimaryGoalOption.entries.forEach { option ->
            SelectionCard(
                title = option.label,
                description = option.description,
                selected = selectedId == option.id,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Select one goal to continue")
        }
    }
}

@Composable
private fun BodyGoalHeightStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    DecimalInputStep(
        title = "How tall are you?",
        description = "We use height together with weight to understand your body targets.",
        label = "Height",
        placeholder = "170",
        helperMessage = "Enter height in centimeters for now",
        unit = "cm",
        answer = answer,
        showValidationError = showValidationError,
        validationMessage = "Enter a valid height in cm",
        onAnswerChanged = onAnswerChanged,
        modifier = modifier,
    )
}

@Composable
private fun BodyGoalCurrentWeightStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    DecimalInputStep(
        title = "What is your current weight?",
        description = "Your starting weight helps Tio calculate practical targets and progress.",
        label = "Current weight",
        placeholder = "72.5",
        helperMessage = "You can refine this later from Profile or Progress",
        unit = "kg",
        answer = answer,
        showValidationError = showValidationError,
        validationMessage = "Enter a valid current weight in kg",
        onAnswerChanged = onAnswerChanged,
        modifier = modifier,
    )
}

@Composable
private fun BodyGoalTargetWeightStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    DecimalInputStep(
        title = "What target weight are you aiming for?",
        description = "This gives Tio an initial destination. You can fine-tune it later.",
        label = "Target weight",
        placeholder = "68",
        helperMessage = "Keep it realistic. You can adjust this later.",
        unit = "kg",
        answer = answer,
        showValidationError = showValidationError,
        validationMessage = "Enter a valid target weight in kg",
        onAnswerChanged = onAnswerChanged,
        modifier = modifier,
    )
}

@Composable
private fun BodyGoalActivityLevelStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedId = (answer as? OnboardingAnswer.Text)?.value

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        OnboardingStepHeading(
            title = "How active are you most days?",
            description = "Your day-to-day movement helps Tio estimate calories, recovery, and effort.",
        )
        ActivityLevelOption.entries.forEach { option ->
            SelectionCard(
                title = option.label,
                description = option.description,
                badge = option.badge,
                selected = selectedId == option.id,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Select one activity level to continue")
        }
    }
}

@Composable
private fun DecimalInputStep(
    title: String,
    description: String,
    label: String,
    placeholder: String,
    helperMessage: String,
    unit: String,
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    validationMessage: String,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialValue = (answer as? OnboardingAnswer.Decimal)?.value?.toInputText().orEmpty()
    var textValue by rememberSaveable(initialValue) { androidx.compose.runtime.mutableStateOf(initialValue) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        OnboardingStepHeading(
            title = title,
            description = description,
        )
        TnyxTextField(
            value = textValue,
            onValueChange = { updatedValue ->
                if (updatedValue.length <= DECIMAL_INPUT_MAX_LENGTH && DECIMAL_INPUT_PATTERN.matches(updatedValue)) {
                    textValue = updatedValue
                    onAnswerChanged(updatedValue.toDecimalAnswer())
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = {
                Text(
                    text = unit,
                    style = TnyxTheme.typography.labelLarge,
                    color = TnyxTheme.colors.textSecondary,
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = showValidationError,
            errorMessage = validationMessage,
            helperMessage = helperMessage,
        )
    }
}

@Composable
private fun SelectionCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    TnyxCard(
        modifier = modifier.fillMaxWidth(),
        variant = TnyxCardVariant.Outlined,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = TnyxTheme.colors.primary,
                    unselectedColor = TnyxTheme.colors.textMuted,
                ),
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXXS),
            ) {
                Text(
                    text = title,
                    style = TnyxTheme.typography.titleMedium,
                    color = TnyxTheme.colors.textPrimary,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
                if (!badge.isNullOrBlank()) {
                    Text(
                        text = badge,
                        style = TnyxTheme.typography.labelMedium,
                        color = TnyxTheme.colors.primary,
                    )
                }
                Text(
                    text = description,
                    style = TnyxTheme.typography.bodyMedium,
                    color = TnyxTheme.colors.textSecondary,
                )
            }
        }
    }
}

private fun String.toDecimalAnswer(): OnboardingAnswer.Decimal? {
    return takeIf(String::isNotBlank)
        ?.toDoubleOrNull()
        ?.takeIf(Double::isFinite)
        ?.let(OnboardingAnswer::Decimal)
}

private fun Double.toInputText(): String {
    return BigDecimal.valueOf(this)
        .stripTrailingZeros()
        .toPlainString()
}

private enum class PrimaryGoalOption(
    val id: String,
    val label: String,
    val description: String,
) {
    BuildMuscle(
        id = "build_muscle",
        label = "Build muscle",
        description = "Prioritize muscle gain and a stronger body composition.",
    ),
    LoseWeight(
        id = "lose_weight",
        label = "Lose weight",
        description = "Focus on fat loss with sustainable training and nutrition guidance.",
    ),
    KeepFit(
        id = "keep_fit",
        label = "Keep fit",
        description = "Maintain health, energy, and a balanced routine.",
    ),
    BoostStrength(
        id = "boost_strength",
        label = "Boost strength",
        description = "Improve performance, strength, and capability in the gym.",
    ),
    ManageStress(
        id = "manage_stress",
        label = "Manage stress",
        description = "Support recovery, mood, and consistency with lower-pressure guidance.",
    ),
}

private enum class ActivityLevelOption(
    val id: String,
    val label: String,
    val badge: String,
    val description: String,
) {
    Sedentary(
        id = "sedentary",
        label = "Sedentary",
        badge = "Mostly seated",
        description = "Desk-heavy routine with little walking or planned movement.",
    ),
    Light(
        id = "light",
        label = "Light",
        badge = "Light movement",
        description = "Some walking or chores, but not much structured activity yet.",
    ),
    Active(
        id = "active",
        label = "Active",
        badge = "Regular movement",
        description = "Frequent walking or workouts a few times each week.",
    ),
    VeryActive(
        id = "very_active",
        label = "Very active",
        badge = "High routine",
        description = "Hard training, physical work, or consistently high daily movement.",
    ),
    Dynamic(
        id = "dynamic",
        label = "Dynamic",
        badge = "Athletic load",
        description = "Very high output days with intense training or demanding activity.",
    ),
}

private val DECIMAL_INPUT_PATTERN = Regex("^\\d*(\\.\\d{0,1})?$")
private const val DECIMAL_INPUT_MAX_LENGTH = 6
