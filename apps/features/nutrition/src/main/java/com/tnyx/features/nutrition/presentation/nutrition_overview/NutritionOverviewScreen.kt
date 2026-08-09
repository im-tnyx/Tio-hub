package com.tnyx.features.nutrition.presentation.nutrition_overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.components.TnyxHeaderSize
import com.tnyx.core.theme.tokens.foundation.TnyxPalette
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader

@Composable
fun NutritionOverviewScreen(
    state: NutritionOverviewUiState,
    onAction: (NutritionOverviewAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        "all" to "Overview",
        "calories" to "Calories",
        "protein" to "Protein",
        "carbs" to "Carbs",
        "fats" to "Fats",
        "fiber" to "Fiber",
        "water" to "Water",
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TnyxTheme.colors.background)
                    .statusBarsPadding()
            ) {
                TnyxScreenHeader(
                    title = "Nutrition Overview",
                    size = TnyxHeaderSize.Standard,
                    uppercaseTitle = false,
                    navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onNavigationClick = { onAction(NutritionOverviewAction.BackClicked) }
                )
            }
        },
        containerColor = TnyxTheme.colors.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. FILTER TABS ---
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(tabs) { (key, label) ->
                        val isSelected = state.targetNutrient == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) TnyxTheme.colors.accent else TnyxTheme.colors.surface
                                )
                                .clickable { onAction(NutritionOverviewAction.TabSelected(key)) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = TnyxTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TnyxTheme.colors.onPrimary else TnyxTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }

            // --- 2. MACRONUTRIENT SUMMARY CARDS ---
            item {
                TnyxCard(
                    variant = TnyxCardVariant.Surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Daily Macro Breakdown",
                            style = TnyxTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TnyxTheme.colors.textPrimary
                        )

                        NutrientProgressBar(
                            label = "Calories",
                            consumed = "${state.caloriesConsumed} / ${state.caloriesGoal} kcal",
                            progress = (state.caloriesConsumed.toFloat() / state.caloriesGoal.coerceAtLeast(1)).coerceIn(0f, 1f),
                            color = TnyxPalette.Pumpkin
                        )

                        NutrientProgressBar(
                            label = "Protein",
                            consumed = "${state.proteinConsumed} / ${state.proteinGoal} g",
                            progress = (state.proteinConsumed.toFloat() / state.proteinGoal.coerceAtLeast(1.0).toFloat()).coerceIn(0f, 1f),
                            color = TnyxTheme.colors.accent
                        )

                        NutrientProgressBar(
                            label = "Carbs",
                            consumed = "${state.carbsConsumed} / ${state.carbsGoal} g",
                            progress = (state.carbsConsumed.toFloat() / state.carbsGoal.coerceAtLeast(1.0).toFloat()).coerceIn(0f, 1f),
                            color = TnyxPalette.Amber
                        )

                        NutrientProgressBar(
                            label = "Fats",
                            consumed = "${state.fatsConsumed} / ${state.fatsGoal} g",
                            progress = (state.fatsConsumed.toFloat() / state.fatsGoal.coerceAtLeast(1.0).toFloat()).coerceIn(0f, 1f),
                            color = TnyxPalette.Rose
                        )
                    }
                }
            }

            // --- 3. MICRONUTRIENT & WATER CARDS ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TnyxCard(
                        variant = TnyxCardVariant.Surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Fiber",
                                style = TnyxTheme.typography.labelMedium,
                                color = TnyxTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.fiberConsumed}g",
                                style = TnyxTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TnyxTheme.colors.textPrimary
                            )
                            Text(
                                text = "Goal: ${state.fiberGoal}g",
                                style = TnyxTheme.typography.bodySmall,
                                color = TnyxTheme.colors.textSecondary
                            )
                        }
                    }

                    TnyxCard(
                        variant = TnyxCardVariant.Surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Water",
                                style = TnyxTheme.typography.labelMedium,
                                color = TnyxTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.waterConsumed}L",
                                style = TnyxTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TnyxPalette.SkyBlue
                            )
                            Text(
                                text = "Goal: ${state.waterGoal}L",
                                style = TnyxTheme.typography.bodySmall,
                                color = TnyxTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientProgressBar(
    label: String,
    consumed: String,
    progress: Float,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = TnyxTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TnyxTheme.colors.textPrimary
            )
            Text(
                text = consumed,
                style = TnyxTheme.typography.bodySmall,
                color = TnyxTheme.colors.textSecondary
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(TnyxTheme.colors.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}
