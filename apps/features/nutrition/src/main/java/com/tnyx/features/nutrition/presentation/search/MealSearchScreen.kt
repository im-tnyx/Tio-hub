package com.tnyx.features.nutrition.presentation.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.nutrition.domain.models.MealItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealSearchScreen(
    state: MealSearchUiState,
    onAction: (MealSearchAction) -> Unit,
) {
    val categories = listOf("ALL", "BREAKFAST", "LUNCH", "DINNER", "SNACKS")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { onAction(MealSearchAction.QueryChanged(it)) },
                        placeholder = { Text("Search food or meal...", style = TnyxTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = TnyxTheme.colors.textSecondary
                            )
                        },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { onAction(MealSearchAction.QueryChanged("")) }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        contentDescription = "Clear",
                                        tint = TnyxTheme.colors.textSecondary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TnyxTheme.colors.textPrimary.copy(alpha = 0.3f),
                            unfocusedBorderColor = TnyxTheme.colors.textPrimary.copy(alpha = 0.1f),
                            focusedContainerColor = TnyxTheme.colors.surfaceVariant,
                            unfocusedContainerColor = TnyxTheme.colors.surfaceVariant,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(MealSearchAction.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TnyxTheme.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TnyxTheme.colors.background,
                )
            )
        },
        containerColor = TnyxTheme.colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = state.selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onAction(MealSearchAction.CategorySelected(category)) },
                        label = {
                            Text(
                                text = category,
                                style = TnyxTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TnyxTheme.colors.textPrimary,
                            selectedLabelColor = TnyxTheme.colors.background,
                            containerColor = TnyxTheme.colors.surfaceVariant,
                            labelColor = TnyxTheme.colors.textPrimary,
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Results List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.searchResults, key = { it.id }) { food ->
                    FoodSearchResultTile(
                        food = food,
                        onAdd = { onAction(MealSearchAction.FoodItemSelected(food)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodSearchResultTile(
    food: MealItem,
    onAdd: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = TnyxTheme.colors.surface,
        border = BorderStroke(0.5.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAdd)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TnyxTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${food.calories} kcal",
                        style = TnyxTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TnyxTheme.colors.textPrimary,
                    )
                    Text(
                        text = "P: ${food.protein}g  C: ${food.carbs}g  F: ${food.fats}g",
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.textSecondary,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Per ${food.quantity.toInt()} ${food.unit}",
                    style = TnyxTheme.typography.labelSmall,
                    color = TnyxTheme.colors.textMuted,
                )
            }

            Surface(
                shape = CircleShape,
                color = TnyxTheme.colors.surfaceVariant,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add food",
                        tint = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
