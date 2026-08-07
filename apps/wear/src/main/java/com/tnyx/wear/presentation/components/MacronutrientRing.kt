package com.tnyx.wear.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import com.tnyx.wear.theme.CardBackground
import com.tnyx.wear.theme.TextGray
import com.tnyx.wear.theme.TextWhite

@Composable
fun MacronutrientRing(
    label: String,
    value: Int,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(46.dp)
        ) {
            // Background track
            CircularProgressIndicator(
                progress = 1.0f,
                modifier = Modifier.fillMaxSize(),
                indicatorColor = CardBackground,
                strokeWidth = 3.dp
            )
            // Progress indicator
            CircularProgressIndicator(
                progress = progress.coerceIn(0.0f, 1.0f),
                modifier = Modifier.fillMaxSize(),
                indicatorColor = color,
                strokeWidth = 3.dp
            )
            // Center Value
            Text(
                text = value.toString(),
                color = TextWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            color = TextGray,
            fontSize = 9.sp,
            modifier = Modifier.padding(top = 2.dp),
            textAlign = TextAlign.Center
        )
    }
}
