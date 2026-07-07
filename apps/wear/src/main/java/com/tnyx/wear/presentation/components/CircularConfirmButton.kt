package com.tnyx.wear.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import com.tnyx.wear.theme.GreenConfirm

@Composable
fun CircularConfirmButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.primaryButtonColors(
            backgroundColor = GreenConfirm,
            contentColor = Color.White
        ),
        modifier = modifier.size(46.dp)
    ) {
        // Fallback to text checkmark if vector icons aren't imported
        Text(
            text = "✓",
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        )
    }
}
