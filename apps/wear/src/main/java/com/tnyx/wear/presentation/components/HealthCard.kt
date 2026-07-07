package com.tnyx.wear.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import com.tnyx.wear.theme.CardBackground
import com.tnyx.wear.theme.TextGray
import com.tnyx.wear.theme.TextWhite
import com.tnyx.wear.theme.WearTypography

@Composable
fun HealthCard(
    icon: Painter,
    title: String,
    valueText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = icon,
            contentDescription = title,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            // Label is on top (e.g. "Steps") - small & gray
            Text(
                text = title,
                style = WearTypography.body1,
                color = TextGray
            )
            // Value is on bottom (e.g. "6,000 steps") - large & bold white
            Text(
                text = valueText,
                style = WearTypography.title1,
                color = TextWhite
            )
        }
    }
}
