# Lyfta Library Exercise Screen Reference

## Scope

This document covers the Lyfta `Library` exercise flow, including favorites, folders, and the bottom sheets that support that exercise-library flow, based on the decompiled reference at `design/references/lyfta/lyfta_1.578`.

Included:

- `Library` tab exercise entry points
- create-exercise entry points tied to the exercise library flow
- the exercise picker/search surface opened from `Library`
- the reusable filter bottom sheet used by that exercise surface
- favorite-specific save and banner behavior
- folder browsing, folder filtering, folder picker, and folder creation/management bottom sheets

Excluded:

- programs-only and routines-only library flows
- workout logger exercise rows
- exercise detail screens outside the search/listing flow

## Provenance

- Reference root: `design/references/lyfta/lyfta_1.578`
- Entry from `Library`: `sources/com/lyfta/app/feature/library_tab/LibraryTabFragment.java:768`
- Library exercise cards/rows: `sources/com/lyfta/app/feature/library_tab/LibraryTabScreenKt.java:452`, `:471`, `:620`, `:639`, `:3167`
- Exercise screen layout: `resources/res/layout/fragment_search_exercises.xml`
- Exercise screen controller: `sources/com/lyfta/fragments/exercise/SearchForExercisesFragment.java`
- Exercise state/behavior: `sources/com/lyfta/fragments/exercise/vm/ExerciseVM.java`
- Create-exercise handoff from library and browser: `sources/com/lyfta/app/feature/library_tab/LibraryTabFragment.java`, `sources/com/lyfta/fragments/exercise/vm/ExerciseVM.java`
- Filter sheet host: `sources/com/lyfta/fragments/bottomsheets/FiltersBottomSheet.java`
- Filter sheet UI helpers: `sources/com/lyfta/fragments/bottomsheets/FiltersBottomSheetKt.java`
- Folder picker sheet: `sources/com/lyfta/fragments/bottomsheets/ExerciseFolderPickerSheet.java`
- Create folder sheet: `sources/com/lyfta/fragments/bottomsheets/CreateFolderSheet.java`
- Manage folders sheet: `sources/com/lyfta/fragments/bottomsheets/ManageFoldersSheet.java`
- Per-exercise action sheet hook: `sources/com/lyfta/fragments/bottomsheets/ExerciseBottomSheet.java`

## 1. Library Tab -> Exercises Entry

Lyfta `Library` tab has a dedicated `Exercises` filter alongside the other library categories. The label is backed by `R.string.library_filter_exercises` (`resources/res/values/strings.xml:1329`) and mapped in `LibraryTabScreenKt` (`:3167`).

Within the `Exercises` view, the first-class entry cards are:

- `Favorite exercises` (`strings.xml:1326-1327`)
- `Custom exercises` (`strings.xml:1322-1323`)
- exercise folders rendered from `ExerciseFolderItem`

Evidence:

- Grid cards: `LibraryTabScreenKt.java:452-471`
- List rows: `LibraryTabScreenKt.java:620-639`
- Tap-to-open exercise surface: `LibraryTabFragment.java:768`

At open time, `LibraryTabFragment` routes into `SearchForExercisesFragment.Companion.newInstance(...)` and passes four important exercise-context flags:

- `openSearch`
- `openFavorites`
- `openCustomExercises`
- `folderExerciseIds`

Evidence for the library entry mapping:

- generic/search-first open: `LibraryTabFragment.java:655-657`
- favorite exercises open: `LibraryTabFragment.java:577-579`
- custom exercises open: `LibraryTabFragment.java:583-585`
- create exercise open: `LibraryTabFragment.java:673-700`
- folder open: `LibraryTabFragment.java:589-591`, `:763-768`

This means the same exercise surface is reused for:

- plain exercise browsing
- favorite-only library browsing
- custom-only library browsing
- folder-scoped browsing

## 1.2 Create Exercise From Library

`Create exercise` is a first-class part of the library exercise surface, not a separate unrelated tool.

Entry points:

- grid create card
- list create row
- `LibraryCreateMenuSheet.RESULT_EXERCISE`

Behavior:

- `LibraryTabFragment.openCreateExercise()` opens `CreateExerciseFragment`
- the fragment is created with no initial exercise id, empty media lists, and `ComingFromConstants.COMING_FROM_EXPLORE`
- after create completes, the callback refreshes `LibraryTabViewModel`

Evidence:

- grid create card: `LibraryTabScreenKt.java:433`, `strings.xml:1316-1317`
- list create row: `LibraryTabScreenKt.java:601`, `strings.xml:1333`
- direct open action: `LibraryTabFragment.java:673-700`
- post-create refresh callback: `LibraryTabFragment.java:704-709`
- create-menu result mapping: `LibraryTabFragment.java:795-819`

## 1.1 Library Exercise Section Structure

Inside `Library`, the exercise area is first rendered as one `Library` content branch under `LibraryFilter.EXERCISES`, not as a direct full-screen search surface.

Evidence:

- `LibraryTabViewModel.LibraryFilter.EXERCISES`: `LibraryTabViewModel.java:201-205`
- exercise branch selection in `LibraryContent(...)`: `LibraryTabScreenKt.java:222`, `:280`, `:372`, `:535`

The exercise section supports two persisted presentation modes:

- `LayoutMode.LIST`
- `LayoutMode.GRID`

It also supports three persisted sort modes:

- `SortOption.RECENTS`
- `SortOption.ALPHABETICAL`
- `SortOption.OLDEST`

Evidence:

- enums: `LibraryTabViewModel.java:190-217`
- filter persistence: `LibraryTabViewModel.java:1172-1175`
- sort persistence: `LibraryTabViewModel.java:1177-1180`
- layout persistence: `LibraryTabViewModel.java:1215-1222`
- sort labels:
  - `library_sort_recents`: `strings.xml:1338`
  - `library_sort_alphabetical`: `strings.xml:1336`
  - `library_sort_oldest`: `strings.xml:1337`

At the top of both grid and list exercise content, Lyfta renders the same `SortRow(...)` control. This row owns:

- sort selection
- layout toggle between grid and list

Evidence:

- grid `SortRow(...)`: `LibraryTabScreenKt.java:390`
- list `SortRow(...)`: `LibraryTabScreenKt.java:558`
- shared control implementation: `LibraryTabScreenKt.java:2366`

Within the exercise section, the reusable entries are:

- create custom exercise
- favorite exercises
- custom exercises
- exercise folders

Evidence:

- grid create card: `LibraryTabScreenKt.java:433`, `strings.xml:1317`
- list create row: `LibraryTabScreenKt.java:601`, `strings.xml:1333`
- grid favorite/custom cards: `LibraryTabScreenKt.java:452-471`
- list favorite/custom rows: `LibraryTabScreenKt.java:620-639`

So the full `Library -> Exercises` flow is two-tiered:

1. `Library` exercise section with sort/layout and organizational entry points
2. shared `SearchForExercisesFragment` opened from those entry points

## 2. Exercise Screen Structure

The exercise surface is `SearchForExercisesFragment` and is inflated from `fragment_search_exercises.xml`.

### 2.1 Top Bar

The top bar contains:

- back/close affordance: `img_back`
- in-search close affordance: `img_close_search`
- title text: `tv_add_exercise_title`
- search icon: `img_search_icon`
- filter icon: `img_filter`
- active-filter ring: `filter_active_ring`
- active-filter badge: `filter_active_badge`
- plus/create action: `img_dots`

Evidence:

- `fragment_search_exercises.xml:160-189`
- title wiring in `SearchForExercisesFragment.java:338-360`
- search activation in `ExerciseVM.java:1261-1310`

The title changes by entry context:

- default: `Add Exercises`
- from explore/feed: `Exercises`
- replace flow: `Replace with`

Evidence:

- `SearchForExercisesFragment.java:338-360`

### 2.2 Search Mode

The fragment supports an explicit search mode instead of always-visible inline search.

Behavior:

- `img_search_icon` turns search mode on
- `img_close_search` exits search mode and clears the query
- `img_clear_search` clears the current query
- search is debounced by `300ms`

Evidence:

- layout IDs: `fragment_search_exercises.xml:91-143`
- `SearchForExercisesFragment.java:362-366`
- `ExerciseVM.java:1286-1310`
- debounce constant: `ExerciseVM.java:742`

If `openSearch=true` is passed from the caller, the screen opens directly in search mode.

Evidence:

- argument parsing: `SearchForExercisesFragment.java:91-109`
- activation on init: `SearchForExercisesFragment.java:362-366`

### 2.3 Filter Strip Above Results

Above the results list, Lyfta shows a horizontal filter strip:

- a dedicated quick favorite toggle icon: `img_favorite_filter`
- a horizontally scrolling chip row: `chip_group_muscles`

Evidence:

- `fragment_search_exercises.xml:224-258`
- selected muscle chips are mirrored into the strip in `SearchForExercisesFragment.java:225-241`

Important detail:

- the favorite icon is a quick inline filter
- broader filtering still comes from the bottom sheet
- selected body-part choices are visually echoed in the chip strip by `setChecked()` and alpha changes

### 2.4 Results Zone

The main content area contains:

- `SwipeRefreshLayout`: `swipe_refresh`
- exercise list: `rv_exercises`
- bottom CTA: `btn_add`
- empty-state container: `linear_no_exercise_container`
- transient favorite banner: `favorite_banner`

Evidence:

- `fragment_search_exercises.xml:305-390`
- listener setup: `ExerciseVM.java:2587-2652`

The add button is selection-aware and updates with selected exercise count.

Evidence:

- `ExerciseVM.java:2394`, `:3248`

Exercise rows also support a per-item action path:

- one click type navigates to exercise details
- another click type opens `ExerciseBottomSheet`

Evidence:

- row click routing: `ExerciseVM.java:2899-2912`
- bottom-sheet launch: `ExerciseVM.java:1793-1799`

The top-right `img_dots` action is also mode-sensitive:

- visible in normal browsing
- hidden while search mode is active

Evidence:

- listener wiring: `ExerciseVM.java:2623-2625`
- visible in browse mode: `ExerciseVM.java:3302-3305`
- hidden in search mode: `ExerciseVM.java:3316-3318`

`img_dots` is also a create-exercise trigger on this screen.

Evidence:

- `img_dots` listener -> `navigateToCreateExercise()`: `ExerciseVM.java:2623-2625`, `a.java:28-30`

## 3. State Passed Into the Exercise Screen

`SearchForExercisesFragment` preconfigures the `ExerciseVM` from navigation args:

- `setFilter` -> preselect body part
- `setEquipmentFilter` -> preselect equipment
- `openFavorites` -> enable favorite filter
- `openCustomExercises` -> enable custom filter
- `folderExerciseIds` -> restrict results to a folder-scoped exercise set

Evidence:

- `SearchForExercisesFragment.java:80-109`
- `SearchForExercisesFragment.java:281-336`

After argument parsing, `ExerciseVM.setSelectedChips()` materializes those incoming values into actual checked muscle/equipment items. If a gym filter already exists, Lyfta first applies that gym preset; if an explicit equipment arg is present, it clears the gym selection and then applies the direct equipment tile.

Evidence:

- `ExerciseVM.java:3044-3078`

This is why the exercise screen behaves like a reusable `library exercise browser`, not just a dumb search page.

## 3.1 Create Exercise From The Browser Screen

The exercise browser can launch `CreateExerciseFragment` contextually from inside the search/list flow.

Entry points confirmed from the VM:

- top-right `img_dots`
- empty-state `btnNoExercisesCreate`

Behavior:

- both entry points call `navigateToCreateExercise()`
- the VM hides the keyboard first
- current search query is passed into `CreateExerciseFragment`
- current muscle and equipment groups are copied and passed along
- the new screen receives a default exercise model via `ExerciseKt.createDefaultExercise(...)`
- when creation succeeds, the callback calls `addExerciseAndScrollToPosition(exercise)`

Evidence:

- create navigation method: `ExerciseVM.java:1336-1359`
- post-create callback: `ExerciseVM.java:1361-1364`
- `img_dots` click mapping: `ExerciseVM.java:2623-2625`, `a.java:28-30`
- empty-state button text/visibility: `ExerciseVM.java:2115-2118`, `strings.xml:589`
- empty-state button click mapping: `ExerciseVM.java:2643-2645`, `a.java:43-45`

Important detail:

- Lyfta treats create-exercise as context-preserving authoring
- the user can start from a failed search or filtered browse state and create an exercise without losing that context

## 4. Filter Indicator Rules

The filter icon exposes two active-state signals:

- badge with numeric count
- ring highlight behind the icon

Evidence:

- UI IDs: `fragment_search_exercises.xml:151-189`
- update logic: `ExerciseVM.java:3339-3360`

The badge count is not raw chip count. It is category-based:

- `+1` if any muscle/body-part filter is active
- `+1` if gym or equipment filter is active
- `+1` if folder filter is active
- `+1` if favorites filter is active
- `+1` if performed filter is active
- `+1` if custom filter is active

Evidence:

- `activeSheetFilterCount()`: `ExerciseVM.java:759-777`

This is a useful product detail: one badge unit represents one filter family, not one selected value.

## 5. Filter Bottom Sheet

There is not just one bottom sheet in this flow. Lyfta uses a small bottom-sheet ecosystem around exercise-library organization:

- `FiltersBottomSheet` for search/filter refinement
- `ExerciseFolderPickerSheet` for adding/removing one exercise to/from folders
- `CreateFolderSheet` for new folder creation
- `ManageFoldersSheet` for folder maintenance
- `ExerciseBottomSheet` for per-exercise actions

Evidence:

- `ExerciseVM.java:1793-1799`, `:1910-1915`, `:1935-1940`
- `FiltersBottomSheet.java:39-62`
- `ExerciseFolderPickerSheet.java:32-55`
- `CreateFolderSheet.java:27-58`
- `ManageFoldersSheet.java:25-37`

The exercise screen opens a reusable `FiltersBottomSheet`.

Evidence:

- launcher: `ExerciseVM.java:1910-1923`
- sheet class: `FiltersBottomSheet.java:39-62`

The sheet shares the parent `ExerciseVM`, so filter edits operate on the same exercise state used by the screen.

Evidence:

- parent-fragment-scoped `ViewModelProvider`: `FiltersBottomSheet.java:297-302`

### 5.1 Sheet Initialization

The sheet initializes from current VM state:

- selected muscle -> `selectedMuscleId`
- selected equipment -> `selectedEquipmentTileId`
- if a gym filter is already active, direct equipment tile preselection is suppressed

Evidence:

- `FiltersBottomSheet.java:326-328`

### 5.2 Apply Behavior

When the user applies filters:

- selected gym, if present, overrides plain equipment tile selection
- body part is posted into `get_bodyPart()`
- equipment is posted into `get_category()`
- callback returns `true` after a short delay, which triggers a fresh fetch on the exercise screen

Evidence:

- apply mapping: `FiltersBottomSheet.java:166-186`
- delayed callback: `FiltersBottomSheet.java:188-193`
- fetch on apply: `ExerciseVM.java:1919-1923`

This is an important implementation clue: gym is treated as a higher-order equipment preset.

Important detail:

- the sheet applies its current state on dismiss as well, not only on an explicit primary action

Evidence:

- `FiltersBottomSheet.onDestroyView()`: `FiltersBottomSheet.java:479-483`

### 5.3 Sheet Content Groups

`FiltersSheetContent(...)` is a Compose surface that receives:

- body-part data
- equipment data
- dismiss callbacks
- create/manage gym callbacks
- create/manage folder callbacks

Evidence:

- composition entry: `FiltersBottomSheet.java:451`
- function signature: `FiltersBottomSheetKt.java:447`

The decompiled lambdas show the filter families explicitly:

- favorites toggle: `FiltersBottomSheetKt.java:466-471`
- performed toggle: `FiltersBottomSheetKt.java:476-481`
- custom toggle: `FiltersBottomSheetKt.java:486-491`
- gyms list: `FiltersBottomSheetKt.java:496`
- folders list: `FiltersBottomSheetKt.java:501`
- selected folder id: `FiltersBottomSheetKt.java:461`
- selected equipment id: `FiltersBottomSheetKt.java:510`
- selected muscle id: `FiltersBottomSheetKt.java:456`

The host sheet also exposes create/manage flows:

- create gym: `FiltersBottomSheet.java:225-232`
- manage gyms: `FiltersBottomSheet.java:252-259`
- create folder: `FiltersBottomSheet.java:211-218`
- manage folders: `FiltersBottomSheet.java:239-246`

### 5.4 Create Folder Logic

Lyfta has two related folder-creation paths around this exercise flow.

#### A. Create folder from the filters bottom sheet

Inside the filters sheet, the folders area exposes both `Create folder` and `Manage Folders` actions.

Behavior:

- `openCreateFolder(...)` opens `CreateFolderSheet`
- when a folder is created, `setOnCreated(...)` triggers `reloadFolders(...)`
- `reloadFolders(...)` calls `ExerciseVM.refreshExerciseFolders(...)` in the view lifecycle scope
- this refresh keeps the sheet's folder list current without leaving the exercise screen

Evidence:

- create action: `FiltersBottomSheet.java:211-214`
- create callback -> refresh: `FiltersBottomSheet.java:218-220`
- manage callback -> refresh: `FiltersBottomSheet.java:239-247`
- reload coroutine: `FiltersBottomSheet.java:285-289`
- folder refresh entry: `ExerciseVM.java:2927-2931`

#### B. Create folder while saving a favorite exercise

The `Add to Folder` CTA from the favorite banner does not create a folder directly. It waits briefly, then opens `ExerciseFolderPickerSheet` for the specific exercise.

Behavior:

- favorite banner CTA callback waits `300ms`
- then `ExerciseVM.showFolderPicker(exercise)` opens `ExerciseFolderPickerSheet`
- the picker is created with both `exerciseId` and `exerciseName`
- if the user chooses `Create Folder`, the picker opens `CreateFolderSheet` with that same `exerciseId`
- `CreateFolderSheet` creates the folder and, when `exerciseId > 0`, immediately adds that exercise into the new folder
- success feedback uses `saved_to_folder`
- after creation, the picker reloads folders and propagates its `onFoldersChanged` callback

Evidence:

- banner CTA handoff: `ExerciseVM$showFavoriteBanner$1$1.java:30-46`
- picker launch: `ExerciseVM.java:1929-1940`
- picker args: `ExerciseFolderPickerSheet.java:48-55`
- picker create-folder entry: `ExerciseFolderPickerSheet.java:372-376`
- picker created callback -> reload: `ExerciseFolderPickerSheet.java:387-390`
- create-sheet optional exercise arg: `CreateFolderSheet.java:53-58`
- create folder call: `CreateFolderSheet.java:106-112`
- auto-add exercise to newly created folder: `CreateFolderSheet.java:141-152`
- success message: `CreateFolderSheet.java:126-139`, `strings.xml:2117`

Important detail:

- Lyfta treats folder creation as both an organization primitive and an immediate post-favorite action
- the same `CreateFolderSheet` can be used in two modes:
  - folder-only creation from filter management
  - create-and-save-exercise from the favorite/folder picker flow

#### C. Folder picker behavior for an existing exercise

`ExerciseFolderPickerSheet` is the dedicated bottom sheet for assigning one exercise to folders.

Behavior:

- it loads current folders through `ExerciseFolderManager.getFolders(...)`
- if the exercise is already in a folder, tapping that folder removes it
- if the exercise is not in the folder, tapping adds it
- remove uses `removed_from_folder` toast-style feedback and reloads the picker list
- add uses `saved_to_folder` feedback and dismisses the picker
- if no folders exist yet, the picker shows `No folders yet`

Evidence:

- folder loading: `ExerciseFolderPickerSheet.java:80-100`
- remove flow: `ExerciseFolderPickerSheet.java:170-200`
- add flow: `ExerciseFolderPickerSheet.java:201-230`
- empty picker state: `ExerciseFolderPickerSheet.java:248-255`, `strings.xml:1691`
- feedback strings:
  - `removed_from_folder`: `strings.xml:2026`
  - `saved_to_folder`: `strings.xml:2117`

#### D. Manage folders behavior

`ManageFoldersSheet` is the maintenance surface behind the filter-sheet `Manage Folders` action.

Behavior confirmed from the decompiled host:

- it can open `CreateFolderSheet` from inside management
- its create callback propagates change through `onChanged`
- it also has a `deleteFolder(...)` coroutine path

Evidence:

- manage-sheet host: `ManageFoldersSheet.java:25-37`
- create-from-manage: `ManageFoldersSheet.java:123-126`
- create callback hook: `ManageFoldersSheet.java:130-132`
- delete coroutine presence: `ManageFoldersSheet.java:44-68`

#### E. Folder filter state after reload

When folder data is refreshed, Lyfta tries to preserve folder-context filtering:

- if `selectedFolderFilterIds` is empty but `folderExerciseIds` already exists, the VM matches the current folder by its exercise-id set
- if a match is found, it restores that folder's id into `selectedFolderFilterIds`
- selecting a folder filter behaves as single-select: Lyfta clears previous folder ids, optionally adds the tapped one, then recomputes `folderExerciseIds`

Evidence:

- folder-id restoration on refresh: `ExerciseVM.java:1475-1492`
- recompute from selected folders: `ExerciseVM.java:1403-1432`
- single-select toggle behavior: `ExerciseVM.java:3258-3263`

### 5.5 Apply Button Feedback

The bottom sheet does not show a static `Apply` label only. It calculates result feedback:

- `No exercises found`
- `Show N exercises`
- `Show 1,000+ exercises` when capped

Evidence:

- `FiltersBottomSheetKt.java:165`
- `strings.xml:1687`
- `strings.xml:2302`

The count is computed by `ExerciseVM.computeFilterResultCount(...)`, which delegates to `countMatchingExercises(...)`.

Evidence:

- `ExerciseVM.java:2449-2454`

### 5.6 Reset And Clear Behavior

The sheet has a clear/reset path in Compose state, and the backing screen VM also has a full filter reset routine.

Compose-side reset clears:

- selected muscle
- selected equipment
- selected folder
- favorites toggle
- performed toggle
- custom toggle

Evidence:

- reset callback family in `FiltersBottomSheetKt.java` via `FiltersSheetContent$lambda$48...$lambda$45(...)`
- exposed state lambdas:
  - selected folder: `FiltersBottomSheetKt.java:461`
  - favorites: `FiltersBottomSheetKt.java:466-471`
  - performed: `FiltersBottomSheetKt.java:476-481`
  - custom: `FiltersBottomSheetKt.java:486-491`

VM-side full clear does the broader data reset:

- turns off favorites/custom/performed
- clears folder filter
- clears selected gym
- clears body-part and equipment selections
- resets paging
- fetches exercises again

Evidence:

- `ExerciseVM.clearAllActiveFilters()`: `ExerciseVM.java:2406-2423`
- indicator refresh before fetch: `ExerciseVM.java:2456-2463`

## 6. Empty-State Logic

The exercise screen has differentiated empty states, not one generic fallback.

Branches covered in `toggleNoExerciseLayout(...)`:

- empty folder
- folder + search no match
- favorites empty
- favorites + active filters no match
- custom empty
- custom + active filters no match
- performed empty
- search + filters no match
- filters only no match
- search only no match
- global no-results

Evidence:

- `ExerciseVM.java:2030-2121`
- key strings:
  - `empty_folder_empty`: `strings.xml:744`
  - `empty_folder_no_match_search`: `strings.xml:745`
  - `empty_clear_filters_hint`: `strings.xml:741`
  - `empty_no_match_filters`: `strings.xml:748`
  - `empty_no_match_search_filters`: `strings.xml:749`

Two CTA rules matter:

- `Create exercise` appears when there is a search query but no active exercise filter
- `Clear Filters` appears when any exercise filter is active

Evidence:

- `ExerciseVM.java:2062-2118`

## 7. Favorite-Specific UX

Lyfta gives favorites special treatment on this screen:

- inline quick favorite filter icon in the top strip
- per-exercise favorite action
- success banner with `Add to Folder` secondary action
- removal banner when an exercise is unfavorited

Evidence:

- quick toggle listener: `ExerciseVM.java:1247-1254`
- quick-filter icon state update: `ExerciseVM.java:2176-2179`
- favorite banner behavior: `ExerciseVM.java:1887-1905`
- `action_add_to_folder_banner`: `strings.xml:115`
- `added_to_favorites`: `strings.xml:237`
- `removed_from_favorites`: `strings.xml:2025`
- favorite banner container: `fragment_search_exercises.xml:352-390`

Banner behavior is asymmetric:

- add to favorites -> filled favorite icon, `Added to Favorites`, `Add to Folder` CTA, `4000ms` duration
- remove from favorites -> remove-favorite icon, `Removed from Favorites`, no secondary CTA, `2500ms` duration

Evidence:

- `ExerciseVM.java:1893-1899`

This means favorites are both:

- a search filter
- an organization workflow entry point

Favorites also bridge directly into the folder workflow:

- favorite save can lead into `ExerciseFolderPickerSheet`
- `Add to Folder` is the fastest path from favorite action to folder organization

Evidence:

- banner CTA handoff: `ExerciseVM$showFavoriteBanner$1$1.java:30-46`
- folder picker launch: `ExerciseVM.java:1929-1940`

## 8. Implementation Notes For Tio

If we adapt this pattern in Tio, the reference implies these reusable primitives are worth separating:

1. `Library exercises entry surface`
2. `Exercise browser/search surface`
3. `Filter indicator + active-count policy`
4. `Filter bottom sheet with result preview`
5. `Favorite banner with folder/save follow-up`
6. `Context-preserving create-exercise flow`

The strongest Lyfta ideas here are not the visuals themselves, but the interaction model:

- one reusable exercise browser for multiple entry contexts
- fast inline filters plus deeper bottom-sheet filters
- explicit result-count feedback before applying filters
- create-exercise entry from both browse and empty states
- context-aware empty states instead of a single blank screen

## 9. Evidence Summary

Core files:

- `design/references/lyfta/lyfta_1.578/sources/com/lyfta/app/feature/library_tab/LibraryTabFragment.java`
- `design/references/lyfta/lyfta_1.578/sources/com/lyfta/app/feature/library_tab/LibraryTabScreenKt.java`
- `design/references/lyfta/lyfta_1.578/sources/com/lyfta/fragments/exercise/SearchForExercisesFragment.java`
- `design/references/lyfta/lyfta_1.578/sources/com/lyfta/fragments/exercise/vm/ExerciseVM.java`
- `design/references/lyfta/lyfta_1.578/sources/com/lyfta/fragments/exercise/vm/a.java`
- `design/references/lyfta/lyfta_1.578/sources/com/lyfta/fragments/bottomsheets/FiltersBottomSheet.java`
- `design/references/lyfta/lyfta_1.578/sources/com/lyfta/fragments/bottomsheets/FiltersBottomSheetKt.java`
- `design/references/lyfta/lyfta_1.578/sources/com/lyfta/fragments/bottomsheets/ExerciseFolderPickerSheet.java`
- `design/references/lyfta/lyfta_1.578/sources/com/lyfta/fragments/bottomsheets/CreateFolderSheet.java`
- `design/references/lyfta/lyfta_1.578/sources/com/lyfta/fragments/bottomsheets/ManageFoldersSheet.java`
- `design/references/lyfta/lyfta_1.578/resources/res/layout/fragment_search_exercises.xml`
- `design/references/lyfta/lyfta_1.578/resources/res/values/strings.xml`
