package com.tnyx.core.legal.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tnyx.core.legal.presentation.action.LegalAction
import com.tnyx.core.legal.presentation.state.LegalUiState
import com.tnyx.core.legal.presentation.widgets.LegalCloseButton
import com.tnyx.core.legal.presentation.widgets.LegalPlaceholderContent
import com.tnyx.core.theme.TnyxTheme

private const val LegalScrimAlpha = 0.08f
private const val LegalBorderAlpha = 0.22f

@Composable
fun LegalScreen(
    state: LegalUiState,
    onAction: (LegalAction) -> Unit,
    onLoadingFinished: () -> Unit
) {
    val colors = TnyxTheme.colors
    val dimens = TnyxTheme.dimens
    val backdropInteractionSource = remember { MutableInteractionSource() }
    val cardInteractionSource = remember { MutableInteractionSource() }

    Dialog(
        onDismissRequest = { onAction(LegalAction.BackdropTapped) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = LegalScrimAlpha))
                .clickable(
                    interactionSource = backdropInteractionSource,
                    indication = null,
                    onClick = { onAction(LegalAction.BackdropTapped) }
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(dimens.SpaceS)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.90f)
                        .align(Alignment.TopCenter)
                        .clickable(
                            interactionSource = cardInteractionSource,
                            indication = null,
                            onClick = {}
                        ),
                    shape = TnyxTheme.shapes.Material.extraLarge,
                    color = colors.surface,
                    contentColor = colors.textPrimary,
                    border = BorderStroke(
                        width = dimens.BorderThin,
                        color = colors.textPrimary.copy(alpha = LegalBorderAlpha)
                    ),
                    tonalElevation = TnyxTheme.elevation.None,
                    shadowElevation = TnyxTheme.elevation.Level4
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LegalPlaceholderContent(
                            state = state,
                            modifier = Modifier.fillMaxSize()
                        )

                        LegalCloseButton(
                            onTap = { onAction(LegalAction.CloseTapped) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(dimens.SpaceS)
                        )
                    }
                }
            }
        }
    }
}
