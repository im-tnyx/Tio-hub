package com.tnyx.features.onboarding.presentation.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton

@Composable
internal fun OnboardingFooter(
    isSaving: Boolean,
    isLastStep: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        TnyxPrimaryButton(
            text = when {
                isSaving -> "Saving..."
                isLastStep -> "Finish"
                else -> "Continue"
            },
            onPressed = onContinue,
            enabled = !isSaving,
            expand = true,
        )
    }
}
