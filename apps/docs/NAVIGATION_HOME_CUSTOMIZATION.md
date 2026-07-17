# Bottom Navigation And Home Customization

**Status:** Product and architecture specification  
**Last updated:** 2026-07-17

## 1. Purpose

Tio should provide a simple default experience while allowing users to personalize the two surfaces they use most:

1. the persistent bottom navigation,
2. the Home screen section order and visibility.

Customization must be available only through Settings. The app must not automatically reorder navigation or Home sections without an explicit user action.

## 2. Default Bottom Navigation

The reset/default configuration is:

```text
Home | Nutrition | AI | Workout | Progress
```

Rules:

- Home is mandatory.
- Home remains the first tab.
- The default contains five tabs.
- A user may configure between three and six total tabs.
- Duplicate tabs are not allowed.
- Changes are applied only after a valid configuration is saved.

## 3. Available Bottom Navigation Destinations

The initial customization catalog is:

- Home
- Nutrition
- AI
- Workout
- Progress
- Explore
- Profile

Additional destinations must not be added to this catalog merely because a feature exists. A destination should be eligible only when it has a stable top-level experience and a supported route contract.

### Profile behavior

Profile currently launches as a root-level graph from the avatar. Product implementation must choose one explicit behavior before exposing Profile as a configurable tab:

1. promote Profile to a true MainGraph top-level destination, or
2. treat the tab as a launcher into the existing ProfileGraph.

The first option provides correct persistent selected-tab behavior and is the preferred long-term model. The second option is acceptable only as an interim implementation and must not pretend that Profile is a persistent MainGraph tab.

### Explore behavior

Explore may appear in the customization catalog only after its route and screen are implemented. Until then, Settings may show it as unavailable or omit it entirely.

## 4. Settings Experience

Settings should expose:

```text
Settings
└── App preferences
    ├── Customize bottom navigation
    └── Customize Home
```

### Customize bottom navigation

The screen should provide:

- drag-and-drop reordering,
- add/remove controls,
- a visible maximum of six tabs,
- a visible minimum of three tabs,
- Home shown as fixed and non-removable,
- a preview of the resulting navigation,
- Save,
- Reset to default.

Invalid configurations must not be saved.

### Reset behavior

Reset restores exactly:

```text
Home | Nutrition | AI | Workout | Progress
```

Reset should require confirmation only when the current configuration differs from the default.

## 5. Runtime Navigation Behavior

Bottom navigation must be state-driven rather than defined by a static private list inside the UI widget.

Recommended flow:

```text
BottomNavPreferencesRepository
        ↓
BottomNavPreferencesViewModel / app-level state holder
        ↓
MainScreen
        ↓
TnyxShell
        ↓
MainBottomNav(tabs = configuredTabs)
```

The route graph remains the source of truth for available destinations. User preferences control only which eligible destinations are rendered and their order.

Required behavior:

- Selected state continues to derive from the current navigation back stack.
- Re-selecting a top-level destination should use the existing top-level navigation behavior.
- Removing the currently selected tab from Settings must return the shell to Home after save.
- A malformed or outdated stored configuration must fall back safely to the default.
- If a previously available destination becomes unavailable, it must be removed during preference normalization.
- Navigation customization must not move feature business logic into `TnyxShell`.

## 6. Suggested Preference Model

The persisted model should use stable identifiers rather than localized labels or icon resources.

```kotlin
data class BottomNavPreferences(
    val orderedTabs: List<ShellTabId> = DefaultBottomTabs,
)

val DefaultBottomTabs = listOf(
    ShellTabId.Home,
    ShellTabId.Nutrition,
    ShellTabId.Ai,
    ShellTabId.Workout,
    ShellTabId.Progress,
)
```

Normalization must enforce:

- known identifiers only,
- no duplicates,
- Home at index zero,
- minimum three,
- maximum six,
- fallback to default when validity cannot be restored.

DataStore is the recommended local persistence mechanism. Cloud synchronization may be added later, but local navigation must not wait on network state.

## 7. Home Screen Default Structure

The reset/default Home configuration is:

1. Daily Overview
2. Nutrition
3. Activity
4. Recovery
5. Routine / Today's Plan

### Daily Overview

Daily Overview is the compact summary section. It may combine the most important values from:

- targets,
- calories or nutrition status,
- activity,
- recovery,
- today's plan.

It should summarize domains, not duplicate every full domain card.

## 8. Available Home Sections

Initial eligible sections:

- Daily Overview
- Nutrition
- Activity
- Recovery
- Routine / Today's Plan
- AI Insights
- Weight
- Water
- Streaks
- Health Metrics
- Challenges
- Weekly Summary
- Notes

A section should appear in the catalog only when its data contract and empty/loading/error behavior are defined.

## 9. Home Customization Rules

Users may:

- reorder sections,
- show or hide optional sections,
- choose which enabled section appears first,
- restore the default configuration.

Recommended constraints:

- At least one section must remain enabled.
- Daily Overview is enabled by default.
- Daily Overview may be hidden only if the product team confirms that Home still has a clear primary summary.
- Section configuration affects presentation only; it does not change feature ownership or data ownership.
- The app may recommend changes, but it must not silently reorder or hide sections.

Reset restores:

```text
Daily Overview
Nutrition
Activity
Recovery
Routine / Today's Plan
```

## 10. Home Runtime Architecture

Recommended flow:

```text
HomeLayoutPreferencesRepository
        ↓
HomeLayoutViewModel
        ↓
HomeScreen
        ↓
Ordered list of feature-owned section renderers
```

The Home feature owns composition and ordering. Each domain continues to own its data and business logic.

Examples:

- Nutrition owns calorie, macro, meal, and water calculations.
- Workout or Activity owns workout and movement data.
- Recovery owns sleep, readiness, HRV, and recovery metrics when implemented.
- Routine owns today's scheduled plan.
- Progress owns weight and measurement progress.

Home receives presentation models through public contracts or use cases. It must not reach into another feature's internal screen or ViewModel.

## 11. Suggested Home Preference Model

```kotlin
data class HomeLayoutPreferences(
    val sections: List<HomeSectionPreference> = DefaultHomeSections,
)

data class HomeSectionPreference(
    val id: HomeSectionId,
    val isVisible: Boolean,
)
```

The stored order is the render order. Normalization must:

- remove unknown IDs,
- remove duplicates,
- append newly introduced default sections according to migration policy,
- guarantee at least one visible section,
- fall back to defaults when corrupted.

## 12. Accessibility And UX Requirements

- Every tab and section control must have a readable accessibility label.
- Reordering must support an accessible alternative to drag-and-drop, such as Move up and Move down actions.
- Six tabs must remain usable at supported font scales.
- Labels must not be replaced by icon-only navigation solely to fit six tabs.
- AI may retain distinct visual treatment, but it remains a standard selectable destination rather than a second floating action button.
- Existing app FAB behavior must remain independent from bottom navigation customization.

## 13. Analytics And Privacy

Optional product analytics may record:

- whether customization was used,
- number of enabled tabs,
- enabled Home section identifiers,
- reset usage.

Do not log private health values as part of customization analytics. Analytics must not be required for preferences to function.

## 14. Testing Requirements

### Unit tests

- default configuration,
- normalization of duplicates and unknown IDs,
- Home fixed at first position,
- minimum and maximum enforcement,
- reset behavior,
- migration from an older preference schema,
- corrupted preference fallback.

### UI tests

- reorder tabs,
- add and remove destinations,
- prevent invalid save,
- render three through six tabs,
- selected destination remains correct after process recreation,
- reorder and hide Home sections,
- reset both customization screens,
- accessibility actions for reordering.

### Navigation tests

- every configurable tab resolves to a valid supported route,
- selected state derives from destination hierarchy,
- removed active tab returns to Home,
- Profile behavior matches the chosen architecture,
- destinations with `NoBottomBar` or `FullScreen` continue to hide the bottom bar.

## 15. Delivery Order

1. Introduce stable tab and Home-section identifiers.
2. Add preference repositories with defaults and normalization.
3. Make `MainBottomNav` receive an ordered tab list.
4. Pass preferences through `MainScreen` and `TnyxShell`.
5. Add Settings customization screens.
6. Add Home section registry and ordered rendering.
7. Add migration, tests, analytics, and accessibility support.
8. Expose Profile and Explore only after their navigation behavior is production-ready.

## 16. Non-Goals

This specification does not authorize:

- automatic AI-driven rearrangement,
- multiple floating action buttons,
- moving domain business logic into the app shell or Home,
- creating placeholder feature modules solely to populate customization choices,
- allowing arbitrary deep screens as persistent bottom tabs,
- syncing preferences to Supabase before local behavior is stable.

## 17. Acceptance Criteria

The feature is complete when:

- the default remains `Home | Nutrition | AI | Workout | Progress`,
- Settings is the only place where users change navigation or Home layout,
- Home is fixed as the first bottom tab,
- users can save a valid configuration containing three to six tabs,
- users can reorder and hide supported Home sections,
- both surfaces have deterministic Reset to default behavior,
- preferences survive app restart,
- invalid stored data safely falls back,
- navigation/chrome policies remain intact,
- feature ownership boundaries remain unchanged.
