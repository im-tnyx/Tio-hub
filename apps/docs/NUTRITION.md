# TNYX Android: Nutrition Feature Reference

Document Status: Source-aligned nutrition reference
Last Verified: 2026-06-27
Owner: Android Engineering
Truth Boundary: This document describes checked-in Android source under `apps/features/nutrition` and shared reusable UI under `apps/core`. Runtime source remains final truth.

This document explains the current nutrition feature shape, especially the Meal Diary screen, the reusable calendar/header components it depends on, and the boundary between current UI work and future Supabase-backed persistence.

---

## 1. Current Scope

The current pushed nutrition work is primarily UI/design-system work, not persistence work.

Implemented in current source:

- Meal Diary screen layout refinement.
- Sticky weekly calendar at top of the screen.
- Compact fading `Meal Diary` header.
- Reusable `TnyxScreenHeader` component.
- Token-backed `TnyxWeeklyCalendar` component.
- Swipeable weekly calendar pages.
- Nutrient grid visual polish.
- Nutrition card progress bars.
- Filled/outlined navigation icon resource pairs.
- New nutrition icon assets for protein/fiber.

Not implemented by this change:

- Supabase-backed nutrition repository.
- Real meal read/write persistence.
- Auth-owned user nutrition data.
- RLS-tested nutrition tables.
- Meal add/edit/delete API writes.
- Food search/catalog backend integration.

Important boundary: current `MealDiaryViewModel` still creates sample meals in memory. UI behavior improved, but nutrition data source is not production truth yet.

---

## 2. Source Files

### Feature files

```text
apps/features/nutrition/src/main/java/com/tnyx/features/nutrition/
|-- domain/models/MealModels.kt
`-- presentation/meal_diary/
    |-- MealDiaryRoute.kt
    |-- MealDiaryScreen.kt
    |-- MealDiaryViewModel.kt
    |-- MealDiaryContract.kt
    `-- widgets/
        |-- NutritionEmptyStateCard.kt
        |-- NutritionMealCard.kt
        |-- NutritionNutrientCard.kt
        |-- NutritionNutrientGrid.kt
        `-- NutritionVitaminSection.kt
```

### Reusable core UI files used by nutrition

```text
apps/core/src/main/java/com/tnyx/core/ui/components/
|-- calendar/TnyxWeeklyCalendar.kt
|-- cards/TnyxCard.kt
`-- layouts/TnyxScreenHeader.kt
```

### Design token files involved

```text
apps/core/src/main/java/com/tnyx/core/theme/tokens/components/
|-- CalendarTokens.kt
|-- CardTokens.kt
|-- NavigationTokens.kt
`-- TnyxComponentTokens.kt
```

### Resource files involved

```text
apps/core/src/main/res/drawable/
|-- ic_fiber_outlined.png
|-- ic_protein_outlined.png
|-- ic_nav_home_filled.xml
|-- ic_nav_home_outlined.xml
|-- ic_nav_nutrition_filled.xml
|-- ic_nav_nutrition_outlined.xml
|-- ic_nav_progress_filled.xml
|-- ic_nav_progress_outlined.xml
|-- ic_nav_workout_filled.xml
`-- ic_nav_workout_outlined.xml
```

---

## 3. Runtime Flow

### `MealDiaryRoute.kt`

Role: Route boundary for the meal diary screen.

Current responsibilities:

- Gets `MealDiaryViewModel` through Hilt.
- Collects `uiState`.
- Collects one-off effects.
- Calls `MealDiaryScreen(state, onAction)`.
- Keeps navigation/effect handling outside the dumb screen.

Expected architecture:

```text
MealDiaryRoute -> MealDiaryViewModel -> MealDiaryUiState -> MealDiaryScreen
MealDiaryScreen -> MealDiaryAction -> MealDiaryViewModel
MealDiaryViewModel -> MealDiaryEffect -> MealDiaryRoute
```

Route must remain the owner of navigation. `MealDiaryScreen` must not receive `NavController`.

---

## 4. State, Actions, Effects

### `MealDiaryUiState`

Current fields:

- `selectedDate: LocalDate`
- `weekDays: List<LocalDate>`
- `hasDietPlan: Boolean`
- `caloriesConsumed: Int`
- `caloriesGoal: Int`
- `proteinConsumed: Double`
- `proteinGoal: Double`
- `fiberConsumed: Double`
- `fiberGoal: Double`
- `carbsConsumed: Double`
- `carbsGoal: Double`
- `sugarConsumed: Double`
- `sugarGoal: Double`
- `fatsConsumed: Double`
- `fatsGoal: Double`
- `waterConsumed: Double`
- `waterGoal: Double`
- `vitaminsProgress: Double`
- `mineralsProgress: Double`
- `meals: List<NutritionMeal>`
- `isLoading: Boolean`

Derived field:

- `isHistoryEmpty`: true when `meals` is empty.

Current source note:

- `MealDiaryUiState` imports `androidx.compose.runtime.Immutable`.
- This is acceptable for feature presentation state.
- Domain models intended for phone/watch reuse should move to `apps/shared` later and avoid Compose imports.

### `MealDiaryAction`

Current actions:

- `DateSelected(date: LocalDate)`: user selects a date from the weekly calendar.
- `MealClicked(meal: NutritionMeal)`: user taps a meal card.
- `OverviewRequested(target: String)`: user taps a nutrient/vitamin target overview.
- `AddMealClicked`: user requests meal creation.

### `MealDiaryEffect`

Current effects:

- `NavigateToMealDetail(mealId: String)`
- `NavigateToAddMeal`
- `ShowOverview(target: String)`

Effects are one-off route-level events. They should not be stored as persistent UI state.

---

## 5. Meal Diary Screen Layout

### `MealDiaryScreen.kt`

Role: Dumb UI surface for meal diary state.

Current high-level structure:

```text
Box(fillMaxSize)
|-- LazyColumn(scrollable content)
|   |-- top spacer for fixed header + calendar
|   |-- NutritionNutrientGrid
|   |-- NutritionVitaminSection
|   |-- NutritionEmptyStateCard, when no meals exist
|   `-- grouped MealGroupHeader + NutritionMealCard list
`-- fixed Column(header + calendar)
    |-- TnyxScreenHeader
    `-- TnyxWeeklyCalendar
```

Screen-level behavior:

- Uses `rememberLazyListState()` to observe scroll.
- Applies `statusBarsPadding()` and `navigationBarsPadding()` at root.
- Uses `TnyxTheme.colors.background` as page background.
- Uses manual top spacer inside `LazyColumn` so fixed header/calendar do not overlap the first content item.
- Keeps content padding bottom at `150.dp` to avoid bottom navigation/content collision.

Current fixed header dimensions:

- Header height: `32.dp`.
- Calendar height: `56.dp`.
- Calendar height is intentionally aligned with `CalendarTokens.height`.

### Sticky header behavior

The screen computes `headerState` from `scrollState`:

```text
scrollOffset = first item scroll offset while first item is visible
translationY = -scrollOffset clamped to -headerHeight..0
alpha = 1 - scrollOffset/headerHeight clamped to 0..1
```

Runtime result:

- At top: header and calendar both visible.
- On scroll: header fades and slides upward.
- Calendar remains visible after header collapses.
- Content scrolls underneath the fixed top column only after the reserved spacer.

Architecture boundary:

- This sticky behavior is UI-only.
- It does not mutate meal data.
- It does not call repositories or APIs.
- It should remain screen-local visual state unless multiple screens adopt the same pattern, in which case a reusable layout component can be extracted.

---

## 6. Reusable Screen Header

### `TnyxScreenHeader.kt`

Role: Compact reusable top header for screens that need a lightweight title/action row.

Current API:

```kotlin
@Composable
fun TnyxScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    height: Dp = 44.dp,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null
)
```

Current behavior:

- Renders uppercase title.
- Uses `TnyxTheme.typography.headlineSmall`.
- Uses `TnyxTheme.colors.textPrimary`.
- Uses `graphicsLayer { alpha = ... }` for fade behavior.
- Supports optional action icon and callback.
- Adds a spacer when no icon exists to keep layout balanced.

Current nutrition usage:

```text
TnyxScreenHeader(
    title = "Meal Diary",
    alpha = headerState.second,
    height = headerHeight,
    actionIcon = Icons.Rounded.NotificationsNone,
    onActionClick = { /* placeholder */ }
)
```

Known boundary:

- Notification click is placeholder-only right now.
- The component must not implement notification logic itself.
- If notification behavior becomes real, it should emit an action/effect through the owning feature route/view-model.

---

## 7. Weekly Calendar

### `TnyxWeeklyCalendar.kt`

Role: Reusable weekly date selector.

Current API:

```kotlin
@Composable
fun TnyxWeeklyCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    allowFutureDates: Boolean = false,
    today: LocalDate = LocalDate.now(),
    locale: Locale = Locale.getDefault()
)
```

Current behavior:

- Shows a horizontal weekly date selector.
- Uses `HorizontalPager` for week swiping.
- Uses a pseudo-infinite pager with `INITIAL_PAGE = 10000`.
- Uses current week Monday as the base date.
- Calculates visible week from `pagerState.currentPage`.
- Syncs pager page when `selectedDate` changes externally.
- Shows month label for visible week.
- Shows a calendar icon that selects `today`.
- Highlights Sundays with error color family.
- Shows today indicator dot.
- Disables future dates when `allowFutureDates = false`.
- Uses alpha reduction for disabled future dates.

Token usage:

- `TnyxTheme.components.calendar.height`
- `TnyxTheme.components.calendar.sideSectionWidth`
- `TnyxTheme.components.calendar.dayWidth`
- `TnyxTheme.components.calendar.indicatorSize`
- `TnyxTheme.components.calendar.iconSize`
- `TnyxTheme.components.calendar.dividerAlpha`
- `TnyxTheme.components.calendar.contentAlpha`
- `TnyxTheme.components.calendar.labelAlpha`
- `TnyxTheme.components.calendar.futureAlpha`

Caller ownership:

- Caller owns selected date.
- Caller receives date selection callback.
- Caller owns domain validity of selected date.
- Component only disables future dates visually and interaction-wise when configured.

Nutrition usage:

- `MealDiaryScreen` passes `state.selectedDate`.
- `MealDiaryScreen` emits `MealDiaryAction.DateSelected(date)` when user selects a date.
- `MealDiaryViewModel` updates `selectedDate` in state.
- Current ViewModel does not yet fetch persisted meals for that date.

---

## 8. Nutrient Grid And Cards

### `NutritionNutrientGrid.kt`

Role: Nutrient summary grid for Meal Diary.

Current behavior:

- Renders two rows of nutrient cards.
- Row 1 currently focuses on calories, protein, fiber.
- Row 2 contains carbs, sugar, fats, water.
- Uses `TnyxTheme.dimens.SpaceS` for spacing instead of raw `8.dp`.
- Emits `onOverviewRequested(target)` when a nutrient card is tapped.

Current icon behavior:

- Calories uses vector resource `ic_nav_nutrition_outlined`.
- Protein uses PNG painter resource `ic_protein_outlined`.
- Fiber uses PNG painter resource `ic_fiber_outlined`.
- Other nutrients still use Material outlined icons.

Architecture boundary:

- Grid has no business logic.
- Grid receives all values from `MealDiaryUiState`.
- Grid only maps tap callbacks to nutrient target ids.
- Future macro calculations should live in ViewModel/domain layer, not inside the grid.

### `NutritionNutrientCard.kt`

Role: Reusable visual card for one nutrient metric.

Current behavior:

- Uses `TnyxCard` with `TnyxCardVariant.Normal`.
- Accepts icon as `Any` to support both `ImageVector` and `Painter`.
- Displays value, goal, unit, progress bar and label.
- Progress is clamped to `0f..1f`.
- Progress bar uses horizontal gradient when nutrient color is provided.
- Track color is faint version of nutrient/on-surface color.
- Uses `TnyxTheme.dimens` for icon size, spacing and radius.

Current boundary:

- `icon: Any` is pragmatic for mixed vector/PNG resources, but less type-safe than a sealed visual model.
- If card usage grows, consider replacing `Any` with a small sealed type such as `NutritionMetricIcon.Vector` and `NutritionMetricIcon.Painter`.
- Progress defaults to `0f`; until ViewModel passes real progress, bars can appear empty even when values are non-zero.

---

## 9. Vitamins, Meals, And Empty State

### `NutritionVitaminSection.kt`

Role: Vitamin/mineral overview section.

Current behavior:

- Uses callbacks to request overview.
- Does not fetch data itself.
- Should continue receiving all values through state.

### `NutritionMealCard.kt`

Role: Meal summary item.

Current behavior:

- Receives `NutritionMeal`.
- Emits tap callback.
- Does not navigate directly.
- Does not mutate meal content.

### `MealGroupHeader`

Role: Local private composable inside `MealDiaryScreen.kt`.

Current behavior:

- Groups meals by `meal.type`.
- Displays uppercase meal type.
- Adds divider line after group label.

Current grouping boundary:

- `MealDiaryScreen` currently groups meals using `state.meals.groupBy { it.type }`.
- This is presentation grouping. If ordering, localization, meal type normalization or backend mapping becomes complex, grouping should move to ViewModel/domain mapper.

### `NutritionEmptyStateCard.kt`

Role: Empty meal history visual state.

Current behavior:

- Shown when `state.isHistoryEmpty` is true.
- Does not trigger add meal by itself unless wired by caller.

---

## 10. ViewModel And Data Reality

### `MealDiaryViewModel.kt`

Current behavior:

- Holds `MutableStateFlow<MealDiaryUiState>`.
- Initializes seven `weekDays` around `LocalDate.now()`.
- Initializes sample meals in memory:
  - `Avocado Toast`
  - `Grilled Chicken Salad`
- Initializes sample macro totals:
  - calories consumed: `460`
  - protein consumed: `42.0`
  - carbs consumed: `35.0`
  - fats consumed: `18.0`
- Handles date selection by updating `selectedDate` only.
- Emits navigation/overview effects for meal click, add meal, and overview request.

Current data boundary:

- Meal data is not loaded from Supabase.
- Meal data is not loaded from backend API.
- Meal data is not persisted locally.
- Date selection does not reload date-specific meals yet.
- Add meal effect exists, but add meal persistence is not wired here.

Why this matters:

- UI is now closer to production visual quality.
- Data source is still demo/sample.
- Do not mark nutrition as persistent or backend-backed until repository/API/Supabase integration lands.

---

## 11. Navigation And Effects Boundary

Current effect path:

```text
NutritionNutrientCard tap
-> NutritionNutrientGrid onOverviewRequested
-> MealDiaryScreen emits MealDiaryAction.OverviewRequested
-> MealDiaryViewModel emits MealDiaryEffect.ShowOverview
-> MealDiaryRoute handles effect
```

Meal click path:

```text
NutritionMealCard tap
-> MealDiaryScreen emits MealDiaryAction.MealClicked(meal)
-> MealDiaryViewModel emits MealDiaryEffect.NavigateToMealDetail(meal.id)
-> MealDiaryRoute handles navigation
```

Add meal path:

```text
UI emits MealDiaryAction.AddMealClicked
-> MealDiaryViewModel emits MealDiaryEffect.NavigateToAddMeal
-> MealDiaryRoute handles navigation
```

Rules:

- `MealDiaryScreen` must not hold `NavController`.
- `NutritionMealCard` must not navigate directly.
- `NutritionNutrientGrid` must not show dialogs directly.
- Effects are route-owned.

---

## 12. Design System Boundary

Nutrition UI should prefer the design system:

- Use `TnyxTheme.colors` for semantic colors.
- Use `TnyxTheme.dimens` for spacing/radius/icon sizes.
- Use `TnyxTheme.typography` for text styles.
- Use `TnyxTheme.components.card` through `TnyxCard`.
- Use `TnyxTheme.components.calendar` through `TnyxWeeklyCalendar`.

Current design-system improvements:

- `CalendarTokens` added for calendar dimensions/alpha specs.
- `TnyxComponentTokens` now exposes `calendar`.
- `TnyxThemeProvider` provides `CalendarTokens()`.
- `LocalTnyxComponentTokens` provides default calendar tokens.
- `NavigationTokens` bottom nav dimensions were refined.
- Bottom navigation now supports selected/unselected icon resource pairs.

Known polish debt:

- `TnyxScreenHeader` currently contains direct `16.dp`, `24.dp`, and default `44.dp`. If it becomes a commonly reused component, header/layout tokens should be introduced.
- `MealDiaryScreen` sticky header dimensions are local constants. That is acceptable while only one screen owns this behavior.
- Some nutrition UI comments still describe implementation notes; future cleanup can reduce comments once behavior stabilizes.

---

## 13. Asset And Resource Ownership

Current assets added for nutrition/shell:

- `apps/assets/icons/broccoli-carrot.png`
- `apps/core/src/main/res/drawable/ic_fiber_outlined.png`
- `apps/core/src/main/res/drawable/ic_protein_outlined.png`
- bottom nav filled/outlined icons in `apps/core/src/main/res/drawable`
- `apps/core/src/main/res/font/material_icons_regular.otf`

Ownership rules:

- Bottom nav icons are core shell assets.
- Reusable nutrient metric icons may live in core only if multiple features use them.
- Feature-specific nutrition illustrations should not be dumped into common `res/` without reuse reason.
- If a nutrition asset is only used by one nutrition screen, prefer feature-specific ownership when the module/source-set supports it.

---

## 14. Supabase And Persistence Boundary

The canonical incremental backend plan lives in:

```text
apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md
```

Nutrition persistence should follow that plan.

Minimum future slice:

- Define nutrition read behavior first.
- Add tables only when required.
- Add RLS and ownership rules.
- Add local/dev seed data.
- Add repository contracts.
- Replace `MealDiaryViewModel` sample meals with repository data.
- Keep screens dumb.
- Validate signed-in/signed-out access.

Likely future tables from the Supabase plan:

- `nutrition_targets`
- `meal_logs`
- `meal_log_items`
- `food_items`, if demo/catalog lookup is needed

Important security rule:

- Android client must never receive service role keys.
- User-owned nutrition data must be protected by RLS or backend authorization.
- Demo data must not be treated as production user health data.

---

## 15. Known Gaps / Watch Points

Source-observed notes:

- `MealDiaryViewModel` still owns hardcoded sample meals.
- `DateSelected` updates state but does not reload meals for that date.
- `AddMealClicked` emits an effect, but actual add meal screen/persistence is not validated here.
- `NutritionNutrientCard` accepts `icon: Any`, which is flexible but weakly typed.
- `progress` defaults to `0f`; real consumed/goal progress should be calculated by ViewModel/domain mapper.
- `MealGroupHeader` is private in `MealDiaryScreen.kt`; if multiple screens need the same grouping UI, move it to `meal_diary/widgets` or nutrition shared widgets.
- Current nutrition domain models are in `features/nutrition/domain/models`; cross-phone/watch reuse should move stable domain contracts into `apps/shared` later.
- Current UI polish does not mean nutrition feature is production data-complete.

---

## 16. Testing And Validation

Recommended checks for docs/UI-only nutrition work:

```bash
./gradlew.bat :app:compileDebugKotlin
```

Recommended future tests:

- `MealDiaryViewModelTest`: date selection updates selected date.
- `MealDiaryViewModelTest`: meal click emits `NavigateToMealDetail` with stable id.
- `MealDiaryViewModelTest`: overview request emits `ShowOverview`.
- `MealDiaryViewModelTest`: add meal emits `NavigateToAddMeal`.
- UI test for `TnyxWeeklyCalendar`: future date disabled when `allowFutureDates = false`.
- UI test for `TnyxWeeklyCalendar`: selecting visible date emits callback.
- UI test for `MealDiaryScreen`: empty state appears when `meals` is empty.

Manual visual checks:

- Header fades when list scrolls down.
- Calendar remains visible after header collapses.
- Swiping calendar changes visible week.
- Today shortcut returns to current week/date.
- Sunday styling is visible but not confused with selected state.
- Bottom navigation selected icons crossfade correctly.

---

## 17. Done Definition For Production Nutrition Diary

The current UI work is not enough to call the nutrition diary production-complete.

A production nutrition diary needs:

- Real authenticated user data source.
- Repository abstraction.
- Date-specific meal loading.
- Add/edit/delete meal flows.
- Food item lookup or manual entry model.
- Daily target calculation or persisted targets.
- Offline/error/loading states.
- RLS/backend authorization validation.
- Unit tests for ViewModel state transitions.
- Manual signed-in/signed-out validation.

UI done definition:

- Header/calendar behavior is stable.
- Nutrient cards display real calculated progress.
- Empty/loading/error/content states all render.
- Screen remains dumb UI.
- No raw design values are introduced unless explicitly local and justified.

Data done definition:

- ViewModel no longer constructs permanent sample meals as source of truth.
- Repository returns meal logs for selected date.
- User cannot read or mutate another user's meal rows.
- Add/edit/delete writes are validated.
- Demo data is local/dev only.

---

## 18. Documentation Update Rule

Update this document when any of these change:

- Meal diary runtime behavior changes.
- Calendar behavior/API changes.
- `MealDiaryUiState`, actions, or effects change.
- Nutrition data source changes from sample data to repository/API/Supabase.
- Nutrition folder structure changes.
- New reusable nutrition widgets are promoted to `core` or `presentation/shared/widgets`.
- Security/persistence boundary changes.

Do not update this document for pure visual micro-tuning unless it changes reusable component contracts or user-visible workflow behavior.