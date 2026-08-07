package com.tnyx.wear.presentation.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.tnyx.wear.R
import com.tnyx.wear.theme.BackgroundBlack
import com.tnyx.wear.theme.CardBackground
import com.tnyx.wear.theme.ColorWater
import com.tnyx.wear.theme.GreenConfirm
import com.tnyx.wear.theme.TextGray
import com.tnyx.wear.theme.TextWhite
import kotlinx.coroutines.delay

data class FoodItem(
    val name: String,
    val calories: Float,
    val carbs: Float,
    val fat: Float,
    val protein: Float
)

val defaultFoodList = listOf(
    FoodItem("Oatmeal", 150f, 27f, 3f, 6f),
    FoodItem("Apple", 95f, 25f, 0f, 0f),
    FoodItem("Banana", 105f, 27f, 0f, 1f),
    FoodItem("Boiled Egg", 70f, 1f, 5f, 6f),
    FoodItem("Chicken Breast", 165f, 0f, 3.6f, 31f),
    FoodItem("White Rice", 200f, 45f, 0.5f, 4f)
)

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun LogFoodScreen(
    onConfirm: (FoodItem) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    mealType: String = "Breakfast",
    foods: List<FoodItem> = defaultFoodList
) {
    val listState = rememberScalingLazyListState()
    var loggingIndex by remember { mutableStateOf(-1) }
    var isAdded by remember { mutableStateOf(false) }

    if (loggingIndex != -1) {
        LaunchedEffect(loggingIndex) {
            delay(600)
            isAdded = true
            delay(800)
            onConfirm(foods[loggingIndex])
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                top = 36.dp,
                start = 10.dp,
                end = 10.dp,
                bottom = 36.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            autoCentering = AutoCenteringParams(itemIndex = 0)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "Log Food",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "for $mealType",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }

            items(foods.size) { index ->
                val food = foods[index]
                val isLoggingThis = index == loggingIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(
                            CardBackground,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable(enabled = loggingIndex == -1) {
                            loggingIndex = index
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = food.name,
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${food.calories.toInt()} kcal",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Status Icon
                        if (isLoggingThis) {
                            if (isAdded) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_checkmark),
                                    contentDescription = "Added",
                                    modifier = Modifier.size(20.dp),
                                    tint = GreenConfirm
                                )
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    indicatorColor = ColorWater
                                )
                            }
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_plus),
                                contentDescription = "Add",
                                modifier = Modifier.size(20.dp),
                                tint = ColorWater
                            )
                        }
                    }
                }
            }
        }
    }
}
