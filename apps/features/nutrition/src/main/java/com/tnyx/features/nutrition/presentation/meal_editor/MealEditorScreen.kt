package com.tnyx.features.nutrition.presentation.meal_editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.features.nutrition.presentation.meal_editor.widgets.MealItemTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealEditorScreen(
    state: MealEditorUiState,
    onAction: (MealEditorAction) -> Unit
) {
    val isExistingMeal = state.meal.id.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isExistingMeal) "Edit your meal" else "Log new meal",
                        style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TnyxTheme.colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(MealEditorAction.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TnyxTheme.colors.textPrimary,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(MealEditorAction.ShareClicked) }) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = TnyxTheme.colors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TnyxTheme.colors.background,
                    titleContentColor = TnyxTheme.colors.textPrimary,
                )
            )
        },
        bottomBar = {
            MealEditorBottomBar(
                category = state.meal.type,
                isExistingMeal = isExistingMeal,
                isSaving = state.isSaving,
                onCategoryChanged = { onAction(MealEditorAction.CategoryChanged(it)) },
                onDelete = { onAction(MealEditorAction.DeleteMealClicked) },
                onSave = { onAction(MealEditorAction.SaveClicked) }
            )
        },
        containerColor = TnyxTheme.colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Hero Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                ImagePickerPlaceholder()
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.meal.name.ifBlank { "New Meal" },
                            style = TnyxTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onAction(MealEditorAction.EditNameRequested) }) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit Name",
                                modifier = Modifier.size(20.dp),
                                tint = TnyxTheme.colors.textSecondary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.1f)),
                        color = Color.Transparent
                    ) {
                        Text(
                            text = "${state.meal.servingSize} serving",
                            style = TnyxTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = TnyxTheme.colors.textPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (state.meal.description.isNotEmpty()) state.meal.description else "Add a description for this meal...",
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.textSecondary,
                        maxLines = 4
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Items Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Items",
                    style = TnyxTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary
                )
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.15f)),
                    color = Color.Transparent,
                    modifier = Modifier.clickable { onAction(MealEditorAction.AddItemClicked) }
                ) {
                    Text(
                        text = "Add Item +",
                        style = TnyxTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        color = TnyxTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (state.meal.items.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TnyxTheme.colors.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No items added yet. Tap 'Add Item +' to add food items to this meal.",
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.textSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                state.meal.items.forEach { item ->
                    MealItemTile(
                        item = item,
                        onDelete = { onAction(MealEditorAction.ItemDeleted(item.id)) },
                        onTap = { /* Navigate to item editor */ },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImagePickerPlaceholder() {
    Column(
        modifier = Modifier
            .size(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(TnyxTheme.colors.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = TnyxTheme.colors.textPrimary.copy(alpha = 0.3f)
            )
        }
        
        Box(
            modifier = Modifier
                .height(38.dp)
                .fillMaxWidth()
                .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = TnyxTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ADD IMAGE",
                    style = TnyxTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = TnyxTheme.colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun MealEditorBottomBar(
    category: String,
    isExistingMeal: Boolean,
    isSaving: Boolean,
    onCategoryChanged: (String) -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    val categories = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACKS")

    Surface(
        color = TnyxTheme.colors.surface,
        tonalElevation = 8.dp,
        border = BorderStroke(0.5.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Category & Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showDropdown = true }
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = category.ifBlank { "BREAKFAST" },
                            style = TnyxTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = TnyxTheme.colors.textPrimary,
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = "Select category",
                            tint = TnyxTheme.colors.textPrimary,
                        )
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = cat,
                                        style = TnyxTheme.typography.bodyMedium.copy(
                                            fontWeight = if (cat == category) FontWeight.Bold else FontWeight.Normal
                                        ),
                                    )
                                },
                                onClick = {
                                    onCategoryChanged(cat)
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }

                // Date Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Date",
                        modifier = Modifier.size(16.dp),
                        tint = TnyxTheme.colors.textSecondary,
                    )
                    Text(
                        text = "Today",
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.textSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row (Delete/Cancel + Save)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, if (isExistingMeal) TnyxTheme.colors.error.copy(alpha = 0.4f) else TnyxTheme.colors.textPrimary.copy(alpha = 0.15f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isExistingMeal) TnyxTheme.colors.error else TnyxTheme.colors.textPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isExistingMeal) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = if (isExistingMeal) "Delete" else "Cancel",
                            style = TnyxTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                TnyxPrimaryButton(
                    text = if (isSaving) "Saving..." else "Save Meal",
                    onPressed = onSave,
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                )
            }
        }
    }
}
