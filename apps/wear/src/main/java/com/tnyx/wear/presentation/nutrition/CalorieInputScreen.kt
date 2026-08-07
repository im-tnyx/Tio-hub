package com.tnyx.wear.presentation.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.tnyx.wear.presentation.components.CircularConfirmButton
import com.tnyx.wear.theme.BackgroundBlack
import com.tnyx.wear.theme.ColorWater
import com.tnyx.wear.theme.TextGray
import com.tnyx.wear.theme.TextWhite
import kotlinx.coroutines.delay

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun CalorieInputScreen(
    onConfirm: (Float) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    mealType: String = "Breakfast"
) {
    val calorieOptions = remember { (50..1500 step 50).map { "$it" } }
    val listState = rememberScalingLazyListState()
    var selectedCalorie by remember { mutableStateOf(calorieOptions[0]) }
    var isLoading by remember { mutableStateOf(false) }

    val centerIndex = listState.centerItemIndex
    LaunchedEffect(centerIndex) {
        if (centerIndex in calorieOptions.indices) {
            selectedCalorie = calorieOptions[centerIndex]
        }
    }

    if (isLoading) {
        LaunchedEffect(Unit) {
            delay(1000)
            val value = selectedCalorie.toFloatOrNull() ?: 0f
            onConfirm(value)
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
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Calories",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "for $mealType",
                    color = TextGray,
                    fontSize = 11.sp
                )
            }

            // 2. Scrollable Picker
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    autoCentering = AutoCenteringParams(itemIndex = 0)
                ) {
                    items(calorieOptions.size) { index ->
                        val item = calorieOptions[index]
                        val isSelected = index == centerIndex
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$item kcal",
                                fontSize = if (isSelected) 24.sp else 17.sp,
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
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // Circular progress loading animation overlay
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
