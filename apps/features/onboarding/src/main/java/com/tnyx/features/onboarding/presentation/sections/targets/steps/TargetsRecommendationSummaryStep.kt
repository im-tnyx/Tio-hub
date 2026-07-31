package com.tnyx.features.onboarding.presentation.sections.targets.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading

@Composable
internal fun TargetsRecommendationSummaryStep(
    draftAnswers: Map<OnboardingStepId, OnboardingAnswer>,
    modifier: Modifier = Modifier,
) {
    val stepsTarget = (draftAnswers[OnboardingStepIds.TargetsStepsTarget] as? OnboardingAnswer.Decimal)
        ?.value
        ?.toInt()
        ?: 0
    val sleepTarget = when ((draftAnswers[OnboardingStepIds.TargetsSleepTarget] as? OnboardingAnswer.Text)?.value) {
        "recover_early" -> "Early recovery"
        "balanced_evenings" -> "Balanced evenings"
        "flexible_late_schedule" -> "Flexible late schedule"
        else -> "Balanced evenings"
    }
    val waterTarget = (draftAnswers[OnboardingStepIds.TargetsWaterTarget] as? OnboardingAnswer.Decimal)
        ?.value
        ?.toInt()
        ?: 0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        OnboardingStepHeading(
            title = "Here is the baseline Tio prepared for you",
            description = "These defaults came from your activity, body goal, and weight inputs so the next planning steps start from something practical.",
        )
        RecommendationCard(
            label = "Daily steps",
            value = "$stepsTarget steps",
            description = draftAnswers.stepsReason(),
        )
        RecommendationCard(
            label = "Sleep target",
            value = sleepTarget,
            description = draftAnswers.sleepReason(),
        )
        RecommendationCard(
            label = "Water target",
            value = "$waterTarget ml",
            description = draftAnswers.waterReason(),
        )
        Text(
            text = "You can keep moving with these recommendations now and fine-tune them later from your plan or settings.",
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun RecommendationCard(
    label: String,
    value: String,
    description: String,
) {
    TnyxCard(
        variant = TnyxCardVariant.Outlined,
        onClick = null,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
        ) {
            Text(
                text = label,
                style = TnyxTheme.typography.labelLarge,
                color = TnyxTheme.colors.textSecondary,
            )
            Text(
                text = value,
                style = TnyxTheme.typography.titleLarge,
                color = TnyxTheme.colors.textPrimary,
            )
            Text(
                text = description,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textSecondary,
            )
        }
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.stepsReason(): String {
    val activityLevel = (this[OnboardingStepIds.BodyGoalActivityLevel] as? OnboardingAnswer.Text)?.value
    val primaryGoal = (this[OnboardingStepIds.BodyGoalPrimaryGoal] as? OnboardingAnswer.Text)?.value
    return when {
        primaryGoal == "lose_weight" -> "Raised a bit to support extra daily movement for your fat-loss goal."
        primaryGoal == "manage_stress" -> "Kept a little gentler so consistency feels easier to maintain."
        activityLevel == "very_active" || activityLevel == "dynamic" ->
            "Matched to your higher activity baseline so the target does not feel too low."
        else -> "Matched to your current activity baseline for a realistic starting rhythm."
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.sleepReason(): String {
    val activityLevel = (this[OnboardingStepIds.BodyGoalActivityLevel] as? OnboardingAnswer.Text)?.value
    val primaryGoal = (this[OnboardingStepIds.BodyGoalPrimaryGoal] as? OnboardingAnswer.Text)?.value
    return when {
        primaryGoal == "manage_stress" -> "Biased toward earlier recovery because stress management usually improves with a steadier sleep rhythm."
        activityLevel == "very_active" -> "Biased toward stronger recovery because your current activity suggests more fatigue load."
        activityLevel == "dynamic" -> "Kept flexible because your schedule may shift more than a strict early bedtime allows."
        else -> "Balanced around a sustainable everyday routine instead of an extreme sleep schedule."
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.waterReason(): String {
    val currentWeight = (this[OnboardingStepIds.BodyGoalCurrentWeight] as? OnboardingAnswer.Decimal)?.value
    val activityLevel = (this[OnboardingStepIds.BodyGoalActivityLevel] as? OnboardingAnswer.Text)?.value
    return if (currentWeight != null) {
        "Estimated from your current weight so hydration starts close to your likely daily baseline."
    } else {
        when (activityLevel) {
            "very_active", "dynamic" -> "Raised for a more active day-to-day load."
            else -> "Set from your activity level to keep hydration practical, not excessive."
        }
    }
}
