package com.tnyx.features.workout.presentation.library.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.inputs.TnyxTextField
import com.tnyx.core.ui.components.inputs.TnyxTextFieldVariant

@Composable
fun SearchExercisesScreen(
    state: SearchExercisesUiState,
    onAction: (SearchExercisesAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background),
    ) {
        SearchExercisesTopBar(
            state = state,
            onAction = onAction,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TnyxTheme.colors.background),
        )
    }
}

@Composable
private fun SearchExercisesTopBar(
    state: SearchExercisesUiState,
    onAction: (SearchExercisesAction) -> Unit,
) {
    Surface(color = TnyxTheme.colors.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = TnyxTheme.dimens.SpaceS, vertical = TnyxTheme.dimens.SpaceS),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.isSearchActive) {
                IconButton(
                    onClick = { onAction(SearchExercisesAction.SearchModeDismissed) },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close search",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }

                Spacer(modifier = Modifier.size(TnyxTheme.dimens.SpaceS))

                TnyxTextField(
                    value = state.searchQuery,
                    onValueChange = { onAction(SearchExercisesAction.QueryChanged(it)) },
                    modifier = Modifier.weight(1f),
                    variant = TnyxTextFieldVariant.Compact,
                    placeholder = { Text(text = "Search exercises") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { }
                    ),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.size(TnyxTheme.dimens.SpaceS))

                IconButton(onClick = { onAction(SearchExercisesAction.FilterIconClicked) }) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Filter exercises",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }
            } else {
                IconButton(
                    onClick = { onAction(SearchExercisesAction.BackClicked) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }

                Spacer(modifier = Modifier.size(TnyxTheme.dimens.SpaceS))

                Text(
                    text = "Exercises",
                    style = TnyxTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = TnyxTheme.colors.textPrimary,
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXS)) {
                    IconButton(onClick = { onAction(SearchExercisesAction.SearchIconClicked) }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search exercises",
                            tint = TnyxTheme.colors.textPrimary,
                        )
                    }

                    IconButton(onClick = { onAction(SearchExercisesAction.FilterIconClicked) }) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Filter exercises",
                            tint = TnyxTheme.colors.textPrimary,
                        )
                    }

                    IconButton(onClick = { onAction(SearchExercisesAction.CreateIconClicked) }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Create exercise",
                            tint = TnyxTheme.colors.textPrimary,
                        )
                    }
                }
            }
        }
    }
}
