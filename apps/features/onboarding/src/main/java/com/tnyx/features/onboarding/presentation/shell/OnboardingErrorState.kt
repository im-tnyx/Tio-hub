package com.tnyx.features.onboarding.presentation.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton

@Composable
internal fun OnboardingErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = TnyxTheme.insets.screenHorizontal,
                vertical = TnyxTheme.insets.screenVertical,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = TnyxTheme.typography.bodyLarge,
            color = TnyxTheme.colors.error,
        )
        TnyxPrimaryButton(
            text = "Retry",
            onPressed = onRetry,
            modifier = Modifier.padding(top = TnyxTheme.dimens.SpaceM),
        )
    }
}
