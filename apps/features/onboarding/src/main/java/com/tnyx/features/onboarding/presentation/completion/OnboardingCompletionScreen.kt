package com.tnyx.features.onboarding.presentation.completion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.features.onboarding.presentation.OnboardingCompletionStage
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading

@Composable
internal fun OnboardingCompletionScreen(
    stage: OnboardingCompletionStage,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (stage) {
        OnboardingCompletionStage.SettingUp -> "Setting up your plan"
        OnboardingCompletionStage.Ready -> "Your Tio setup is ready"
    }
    val description = when (stage) {
        OnboardingCompletionStage.SettingUp -> {
            "We are locking in your starter profile, targets, and preferences so your first app session opens cleanly."
        }

        OnboardingCompletionStage.Ready -> {
            "Your starting details are saved. You can refine them later from the app, but your first plan is now ready to explore."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(
                horizontal = TnyxTheme.insets.screenHorizontal,
                vertical = TnyxTheme.insets.screenVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXL))

        OnboardingStepHeading(
            title = title,
            description = description,
        )

        TnyxCard(
            variant = TnyxCardVariant.Outlined,
            onClick = null,
        ) {
            if (stage == OnboardingCompletionStage.SettingUp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = TnyxTheme.dimens.SpaceL),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = TnyxTheme.colors.primary)
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
                ) {
                    Text(
                        text = "What is ready now",
                        style = TnyxTheme.typography.titleLarge,
                        color = TnyxTheme.colors.textPrimary,
                    )
                    CompletionLine("Profile basics are saved for your local account.")
                    CompletionLine("Workout, target, and source preferences are attached to your starter setup.")
                    CompletionLine("You can revisit and refine these choices later from the app.")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (stage == OnboardingCompletionStage.Ready) {
            TnyxPrimaryButton(
                text = "Start app",
                onPressed = onContinue,
                expand = true,
            )
        }
    }
}

@Composable
private fun CompletionLine(
    text: String,
) {
    Text(
        text = text,
        style = TnyxTheme.typography.bodyLarge,
        color = TnyxTheme.colors.textSecondary,
    )
}
