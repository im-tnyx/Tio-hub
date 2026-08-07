package com.tnyx.core.ui.components.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding

/**
 * Tnyx reusable modal bottom sheet.
 * Feature screens own visibility/state; this component owns visual styling only.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TnyxModalBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    showDivider: Boolean = true,
    skipPartiallyExpanded: Boolean = true,
    contentBottomPadding: Dp? = null,
    /** Override horizontal padding for edge-to-edge content (e.g. full-width lists). Defaults to tokens value. */
    contentHorizontalPadding: Dp? = null,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val tokens = TnyxTheme.components.sheet

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.statusBarsPadding(),
        sheetState = sheetState,
        shape = tokens.shape,
        containerColor = tokens.containerColor,
        contentColor = tokens.contentColor,
        scrimColor = tokens.scrimColor,
        tonalElevation = TnyxTheme.elevation.None,
        dragHandle = dragHandle,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = contentHorizontalPadding ?: tokens.horizontalPadding)
                .padding(bottom = contentBottomPadding ?: tokens.bottomPadding)
        ) {
            if (!title.isNullOrBlank()) {
                // When content is edge-to-edge (contentHorizontalPadding=0), title still needs
                // standard horizontal padding so it is never flush against the screen edge.
                val titleHorizontalPadding = if (contentHorizontalPadding != null) tokens.horizontalPadding else 0.dp
                Text(
                    text = title,
                    style = tokens.titleStyle ?: TnyxTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.contentColor,
                    modifier = Modifier
                        .padding(horizontal = titleHorizontalPadding)
                        .padding(bottom = TnyxTheme.dimens.SpaceS)
                )
                if (showDivider) {
                    HorizontalDivider(
                        color = tokens.dividerColor.copy(alpha = 0.12f),
                        thickness = 1.dp,
                    )
                }
            }

            content()
        }
    }
}
