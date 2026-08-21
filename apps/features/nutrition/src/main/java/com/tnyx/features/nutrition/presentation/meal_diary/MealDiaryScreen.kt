package com.tnyx.features.nutrition.presentation.meal_diary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PieChartOutline
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.components.TnyxHeaderSize
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.ui.components.calendar.TnyxWeeklyCalendar
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.presentation.meal_diary.widgets.*
import kotlin.math.roundToInt

@Composable
fun MealDiaryScreen(
    state: MealDiaryUiState,
    onAction: (MealDiaryAction) -> Unit
) {
    val scrollState = rememberLazyListState()
    val density = LocalDensity.current

    // Dimensions for manual sticky logic — driven by design tokens
    val headerHeight = TnyxDimens.ScreenHeaderHeightCompact
    val calendarHeight = 56.dp // Matching CalendarTokens height
    val headerHeightPx = with(density) { headerHeight.toPx() }

    // Calculate translation and alpha for the header
    val headerState by remember {
        derivedStateOf {
            val scrollOffset = if (scrollState.firstVisibleItemIndex == 0) {
                scrollState.firstVisibleItemScrollOffset.toFloat()
            } else {
                headerHeightPx
            }
            
            val translationY = (-scrollOffset).coerceIn(-headerHeightPx, 0f)
            val alpha = (1f - (scrollOffset / headerHeightPx)).coerceIn(0f, 1f)
            
            translationY to alpha
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // --- 1. SCROLLABLE CONTENT ---
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 150.dp)
        ) {
            // Placeholder for Header + Calendar (Zero gap)
            item {
                Spacer(modifier = Modifier.height(headerHeight + calendarHeight))
            }

            state.errorMessage?.let { message ->
                item {
                    com.tnyx.core.ui.components.cards.TnyxCard(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        variant = com.tnyx.core.ui.components.cards.TnyxCardVariant.Surface,
                    ) {
                        Text(
                            text = message,
                            style = TnyxTheme.typography.bodyMedium,
                            color = TnyxTheme.colors.error,
                        )
                    }
                }
            }

            // ... rest of the content (Nutrients, Vitamins, Meals)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    NutritionNutrientGrid(
                        state = state,
                        onOverviewRequested = { onAction(MealDiaryAction.OverviewRequested(it)) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    NutritionVitaminSection(
                        state = state,
                        onOverviewRequested = { onAction(MealDiaryAction.OverviewRequested(it)) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.isHistoryEmpty) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        NutritionEmptyStateCard()
                    }
                }
            } else {
                val groupedMeals = state.meals.groupBy { it.type }
                groupedMeals.forEach { (type, meals) ->
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            MealGroupHeader(type = type)
                        }
                    }
                    items(meals) { meal ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            NutritionMealCard(
                                meal = meal,
                                onTap = { onAction(MealDiaryAction.MealClicked(meal)) },
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. FIXED HEADER & CALENDAR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, headerState.first.roundToInt()) }
                .background(TnyxTheme.colors.background)
        ) {
            // Reusable Screen Header
            TnyxScreenHeader(
                title = "Meal Diary",
                alpha = headerState.second,
                size = TnyxHeaderSize.Compact,
                uppercaseTitle = false,
                actions = {
                    IconButton(onClick = { onAction(MealDiaryAction.OverviewRequested("all")) }) {
                        Icon(
                            imageVector = Icons.Rounded.PieChartOutline,
                            contentDescription = "Nutrition Analytics",
                            tint = TnyxTheme.colors.textPrimary
                        )
                    }
                    Box {
                        IconButton(onClick = { onAction(MealDiaryAction.OptionsMenuToggled) }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "More options",
                                tint = TnyxTheme.colors.textPrimary
                            )
                        }

                        if (state.isOptionsMenuExpanded) {
                            androidx.compose.ui.window.Popup(
                                alignment = Alignment.TopEnd,
                                offset = IntOffset(x = -12, y = 44),
                                onDismissRequest = { onAction(MealDiaryAction.OptionsMenuDismissed) }
                            ) {
                                com.tnyx.core.ui.components.cards.TnyxCard(
                                    variant = com.tnyx.core.ui.components.cards.TnyxCardVariant.Surface,
                                    padding = 0.dp,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .width(220.dp)
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .clickable { onAction(MealDiaryAction.NutritionSettingsClicked) }
                                                .padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Tune,
                                                contentDescription = null,
                                                tint = TnyxTheme.colors.textPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Nutrition Target",
                                                style = TnyxTheme.typography.bodyMedium,
                                                color = TnyxTheme.colors.textPrimary
                                            )
                                        }

                                        HorizontalDivider(
                                            color = TnyxTheme.colors.textSecondary.copy(alpha = 0.15f),
                                            thickness = 0.5.dp
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .clickable { onAction(MealDiaryAction.AppSettingsClicked) }
                                                .padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Settings,
                                                contentDescription = null,
                                                tint = TnyxTheme.colors.textPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "App Settings",
                                                style = TnyxTheme.typography.bodyMedium,
                                                color = TnyxTheme.colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
            // Calendar (Stays visible as the Column offset stops at -headerHeightPx)
            TnyxWeeklyCalendar(
                selectedDate = state.selectedDate,
                onDateSelected = { onAction(MealDiaryAction.DateSelected(it)) }
            )
        }

        // --- 3. EXPANDABLE MEAL FAB ---
        ExpandableMealFab(
            isExpanded = state.isFabExpanded,
            onToggle = { onAction(MealDiaryAction.FabToggled) },
            onCollapse = { onAction(MealDiaryAction.FabCollapsed) },
            onSearchClicked = { onAction(MealDiaryAction.AddMealClicked) },
            onMicClicked = { onAction(MealDiaryAction.AddMealVoiceClicked) },
            onCameraClicked = { onAction(MealDiaryAction.AddMealCameraClicked) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 72.dp)
                .navigationBarsPadding(),
        )
    }
}

@Composable
private fun MealGroupHeader(type: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = type.uppercase(),
            style = TnyxTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = TnyxTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = TnyxTheme.colors.textPrimary.copy(alpha = 0.1f)
        )
    }
}
