package com.tnyx.features.onboarding.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton

@Composable
internal fun OnboardingBottomBar(
    visible: Boolean,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(
                horizontal = TnyxTheme.insets.screenHorizontal,
                vertical = TnyxTheme.insets.screenVertical,
            ),
        contentAlignment = Alignment.Center,
    ) {
        TnyxPrimaryButton(
            text = text,
            onPressed = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            expand = true,
        )
    }
}
