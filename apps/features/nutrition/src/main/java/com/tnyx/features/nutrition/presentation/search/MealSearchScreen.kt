package com.tnyx.features.nutrition.presentation.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.inputs.TnyxTextField
import com.tnyx.features.nutrition.domain.models.MealItem

private const val GROUP_PREVIEW_LIMIT = 5

@Composable
fun MealSearchScreen(
    state: MealSearchUiState,
    onAction: (MealSearchAction) -> Unit,
) {
    BackHandler(enabled = state.selectedGroup != null) {
        onAction(MealSearchAction.GroupClosed)
    }

    if (state.selectedGroup == MealSearchGroup.FoodDatabase) {
        MealSearchGroupScreen(
            foods = state.searchResults,
            onBack = { onAction(MealSearchAction.GroupClosed) },
            onFoodSelected = { onAction(MealSearchAction.FoodItemSelected(it)) },
        )
        return
    }

    MealSearchOverview(
        state = state,
        onAction = onAction,
    )
}

@Composable
private fun MealSearchOverview(
    state: MealSearchUiState,
    onAction: (MealSearchAction) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(containerColor = TnyxTheme.colors.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = TnyxTheme.dimens.SpaceM),
        ) {
            SearchBackButton(onClick = { onAction(MealSearchAction.BackClicked) })

            Text(
                text = "Search Meals",
                style = TnyxTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TnyxTheme.colors.textPrimary,
                modifier = Modifier.padding(
                    top = TnyxTheme.dimens.SpaceS,
                    bottom = TnyxTheme.dimens.SpaceL,
                ),
            )

            TnyxTextField(
                value = state.query,
                onValueChange = { onAction(MealSearchAction.QueryChanged(it)) },
                placeholder = {
                    Text(
                        text = "Search food or meal...",
                        style = TnyxTheme.typography.bodyLarge,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                    )
                },
                trailingIcon = if (state.query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onAction(MealSearchAction.QueryChanged("")) }) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = "Clear search",
                            )
                        }
                    }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        onAction(MealSearchAction.SearchSubmitted)
                    },
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))

            when {
                state.isSearching -> SearchLoading()
                state.errorMessage != null -> SearchStatus(state.errorMessage)
                state.hasSearched && state.searchResults.isEmpty() -> {
                    SearchStatus("No matching foods found.")
                }
                state.searchResults.isNotEmpty() -> SearchResultsOverview(
                    foods = state.searchResults,
                    onGroupSelected = {
                        onAction(MealSearchAction.GroupSelected(MealSearchGroup.FoodDatabase))
                    },
                    onFoodSelected = { onAction(MealSearchAction.FoodItemSelected(it)) },
                )
                else -> SearchStatus("Type at least 2 characters to search foods.")
            }
        }
    }
}

@Composable
private fun MealSearchGroupScreen(
    foods: List<MealItem>,
    onBack: () -> Unit,
    onFoodSelected: (MealItem) -> Unit,
) {
    Scaffold(containerColor = TnyxTheme.colors.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = TnyxTheme.dimens.SpaceM),
        ) {
            SearchBackButton(onClick = onBack)
            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
            LazyColumn(
                contentPadding = PaddingValues(bottom = TnyxTheme.dimens.SpaceXL),
            ) {
                item {
                    SearchResultGroupCard(
                        title = "Food database",
                        foods = foods,
                        onFoodSelected = onFoodSelected,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TnyxTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun SearchLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = TnyxTheme.colors.primary,
            modifier = Modifier.size(TnyxTheme.dimens.IconL),
        )
    }
}

@Composable
private fun SearchStatus(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(TnyxTheme.dimens.SpaceL),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun SearchResultsOverview(
    foods: List<MealItem>,
    onGroupSelected: () -> Unit,
    onFoodSelected: (MealItem) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = TnyxTheme.dimens.SpaceXL),
    ) {
        item {
            SearchResultGroupCard(
                title = "Food database",
                foods = foods,
                previewLimit = GROUP_PREVIEW_LIMIT,
                onHeaderClick = onGroupSelected,
                onFoodSelected = onFoodSelected,
            )
        }
    }
}

@Composable
private fun SearchResultGroupCard(
    title: String,
    foods: List<MealItem>,
    onFoodSelected: (MealItem) -> Unit,
    previewLimit: Int? = null,
    onHeaderClick: (() -> Unit)? = null,
) {
    val visibleFoods = previewLimit?.let(foods::take) ?: foods
    TnyxCard(
        variant = TnyxCardVariant.Outlined,
        padding = TnyxTheme.dimens.SpaceNone,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (onHeaderClick != null) {
                            Modifier.clickable(onClick = onHeaderClick)
                        } else {
                            Modifier
                        },
                    )
                    .padding(TnyxTheme.dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = TnyxTheme.colors.textSecondary,
                    modifier = Modifier.size(TnyxTheme.dimens.IconM),
                )
                Text(
                    text = title,
                    style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TnyxTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = foods.size.toString(),
                    style = TnyxTheme.typography.labelLarge,
                    color = TnyxTheme.colors.textSecondary,
                )
            }
            HorizontalDivider(color = TnyxTheme.components.card.outlinedBorderColor)
            visibleFoods.forEachIndexed { index, food ->
                FoodSearchResultRow(
                    food = food,
                    onClick = { onFoodSelected(food) },
                )
                if (index < visibleFoods.lastIndex) {
                    HorizontalDivider(
                        color = TnyxTheme.components.card.outlinedBorderColor,
                        modifier = Modifier.padding(start = TnyxTheme.dimens.SpaceHuge),
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodSearchResultRow(
    food: MealItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(TnyxTheme.dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        Icon(
            imageVector = Icons.Outlined.Restaurant,
            contentDescription = null,
            tint = TnyxTheme.colors.textSecondary,
            modifier = Modifier.size(TnyxTheme.dimens.IconM),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = food.name,
                style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TnyxTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXXS))
            Text(
                text = "${food.calories} kcal | Protein ${food.protein} g | ${food.unit}",
                style = TnyxTheme.typography.bodySmall,
                color = TnyxTheme.colors.textSecondary,
            )
        }
    }
}
