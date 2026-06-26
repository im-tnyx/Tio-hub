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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Share
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit your meal") },
                navigationIcon = {
                    IconButton(onClick = { onAction(MealEditorAction.BackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(MealEditorAction.ShareClicked) }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TnyxTheme.colors.background,
                    titleContentColor = TnyxTheme.colors.textPrimary
                )
            )
        },
        bottomBar = {
            MealEditorBottomBar(
                category = state.meal.type,
                onCategoryChanged = { onAction(MealEditorAction.CategoryChanged(it)) },
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
                            text = state.meal.name,
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
                        text = if (state.meal.description.isNotEmpty()) state.meal.description else "Add a description...",
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.textSecondary,
                        maxLines = 4
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Macro Grid (Simplified for editor)
            // TODO: Add MealEditMacroGrid
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Items Section
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
                    border = BorderStroke(1.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.1f)),
                    color = Color.Transparent,
                    modifier = Modifier.clickable { onAction(MealEditorAction.AddItemClicked) }
                ) {
                    Text(
                        text = "Add Item +",
                        style = TnyxTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = TnyxTheme.colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
    onCategoryChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    val categories = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACKS")

    Surface(
        color = TnyxTheme.colors.background,
        tonalElevation = 8.dp,
        border = BorderStroke(0.5.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Text(
                        text = category,
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.textPrimary,
                        modifier = Modifier
                            .clickable { showDropdown = true }
                            .padding(vertical = 8.dp)
                    )
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    onCategoryChanged(cat)
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Date placeholder
                Text(
                    text = "Feb 5, 20:35",
                    style = TnyxTheme.typography.bodySmall,
                    color = TnyxTheme.colors.textSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { /* Add hint */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.1f))
                ) {
                    Text("Add Hint", color = TnyxTheme.colors.textPrimary)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                TnyxPrimaryButton(
                    text = "Save Meal",
                    onPressed = onSave,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
