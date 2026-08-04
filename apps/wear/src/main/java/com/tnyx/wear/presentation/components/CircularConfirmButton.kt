package com.tnyx.wear.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import com.tnyx.wear.R
import com.tnyx.wear.theme.GreenConfirm

@Composable
fun CircularConfirmButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.primaryButtonColors(
            backgroundColor = GreenConfirm,
            contentColor = Color.White
        ),
        modifier = modifier.size(46.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_checkmark),
            contentDescription = "Confirm",
            modifier = Modifier.size(22.dp),
            tint = Color.White
        )
    }
}
