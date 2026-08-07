package com.tnyx.wear.presentation.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import com.tnyx.wear.presentation.components.CircularConfirmButton
import com.tnyx.wear.theme.BackgroundBlack
import com.tnyx.wear.theme.ColorWater
import com.tnyx.wear.theme.TextGray
import com.tnyx.wear.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun WaterSelectionScreen(
    onConfirm: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cupOptions = remember { (1..20).map { "$it" } }
    val listState = rememberScalingLazyListState()
    var selectedCups by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }

    val centerIndex = listState.centerItemIndex
    LaunchedEffect(centerIndex) {
        if (centerIndex in cupOptions.indices) {
            selectedCups = cupOptions[centerIndex].toInt()
        }
    }

    if (isLoading) {
        LaunchedEffect(Unit) {
            delay(1000) // Simulated water log save & sync
            onConfirm(selectedCups)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header
            Text(
                text = "Cups",
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )

            // 2. Scrollable Cup Picker
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ScalingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    autoCentering = AutoCenteringParams(itemIndex = 0)
                ) {
                    items(cupOptions.size) { index ->
                        val item = cupOptions[index]
                        val isSelected = index == centerIndex
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (item == "1") "1 Cup" else "$item Cups",
                                fontSize = if (isSelected) 24.sp else 18.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ColorWater else TextGray
                            )
                        }
                    }
                }
            }

            // 3. Confirm Button
            CircularConfirmButton(
                onClick = { isLoading = true },
                enabled = !isLoading,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Circular progress overlay while saving
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlack.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    indicatorColor = ColorWater,
                    trackColor = TextGray.copy(alpha = 0.3f),
                    strokeWidth = 4.dp
                )
            }
        }
    }
}
