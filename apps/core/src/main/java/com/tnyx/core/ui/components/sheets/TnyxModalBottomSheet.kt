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
import com.tnyx.core.theme.TnyxTheme

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
        modifier = modifier,
        sheetState = sheetState,
        shape = tokens.shape,
        containerColor = tokens.containerColor,
        contentColor = tokens.contentColor,
        scrimColor = tokens.scrimColor,
        tonalElevation = TnyxTheme.elevation.None,
        dragHandle = dragHandle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = tokens.horizontalPadding)
                .padding(bottom = tokens.bottomPadding)
        ) {
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    style = tokens.titleStyle ?: TnyxTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.contentColor,
                    modifier = Modifier.padding(bottom = TnyxTheme.dimens.SpaceM)
                )
                if (showDivider) {
                    HorizontalDivider(
                        color = tokens.dividerColor,
                        thickness = TnyxTheme.dimens.BorderThin,
                        modifier = Modifier.padding(bottom = TnyxTheme.dimens.SpaceM)
                    )
                }
            }

            content()
        }
    }
}
