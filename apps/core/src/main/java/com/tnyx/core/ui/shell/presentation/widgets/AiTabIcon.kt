package com.tnyx.core.ui.shell.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme

@Composable
fun AiTabIcon(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val aiColor = TnyxTheme.colors.ai
    // Selection effects removed, keeping a static premium look
    val shadowAlpha = 0.2f
    val elevation = 2.dp
    
    Box(
        modifier = modifier
            .size(45.dp)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                ambientColor = aiColor.copy(alpha = shadowAlpha),
                spotColor = aiColor.copy(alpha = shadowAlpha)
            )
            .background(
                color = TnyxTheme.colors.surface,
                shape = CircleShape
            )
            .border(
                width = 1.6.dp,
                color = aiColor.copy(alpha = 0.6f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = "AI",
            tint =  aiColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
