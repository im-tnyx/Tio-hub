# Bottom Navigation Customization

**Status:** Product and architecture specification  
**Last updated:** 2026-07-17

## 1. Purpose

Tio should keep a predictable default bottom navigation while allowing users to choose which eligible top-level destinations are visible and how they are ordered.

Customization is available only through Settings. The app must not automatically reorder, add, or remove tabs based on behavior, AI suggestions, onboarding answers, or feature usage.

Home screen layout customization is intentionally outside this specification. Home is a cross-domain summary surface, while the detailed actions and workflows remain inside their owning tabs.

## 2. Default Configuration

Reset and first-launch defaults are:

```text
Home | Nutrition | AI | Workout | Progress
```

Rules:

- Home is mandatory.
- Home remains the first tab.
- The default contains five tabs.
- A user may configure between three and six total tabs.
- Duplicate destinations are invalid.
- Changes are applied only after a valid configuration is saved.
- Reset restores exactly the five default tabs in the default order.

## 3. Eligible Destinations

The initial destination catalog is:

- Home
- Nutrition
- AI
- Workout
- Progress
- Explore
- Profile

A feature is not automatically eligible because it exists. A destination may enter the catalog only when it has:

- a stable top-level product experience,
- a public and supported route contract,
- correct persistent-chrome behavior,
- deterministic selected-state behavior,
- and production-ready loading, empty, error, and unavailable states.

### Explore

Explore must not be selectable until its route and production screen exist. Settings may omit unavailable destinations or show them disabled with an explanation.

### Profile

Profile currently launches from the avatar as a root-level graph. Before Profile becomes selectable as a persistent tab, product and architecture must choose one explicit model:

1. promote Profile to a true `MainGraph` top-level destination, or
2. keep it as a launcher into the existing root-level `ProfileGraph`.

The first model is preferred because it supports correct persistent selected-tab and back-stack behavior. The launcher model is acceptable only as an interim implementation and must not pretend Profile is a normal persistent tab.

## 4. Settings Experience

Recommended entry:

```text
Settings
└── App preferences
    └── Customize bottom navigation
```

The screen should provide:

- drag-and-drop reordering,
- accessible Move up and Move down actions,
- add and remove controls,
- a visible minimum of three tabs,
- a visible maximum of six tabs,
- Home shown as fixed and non-removable,
- a live preview,
- Save,
- and Reset to default.

Invalid configurations must not be saved.

Reset confirmation is required only when the saved configuration differs from the default.

## 5. Runtime Architecture

The bottom navigation must become state-driven instead of reading from a static private tab list inside the UI widget.

Recommended flow:

```text
BottomNavPreferencesRepository
        ↓
App-level state holder / ViewModel
        ↓
MainScreen
        ↓
TnyxShell
        ↓
MainBottomNav(tabs = configuredTabs)
```

The route graph remains the source of truth for supported destinations. Preferences control only which eligible destinations are rendered and their order.

Required behavior:

- Selected state continues to derive from the current navigation back stack.
- Re-selecting a top-level destination uses the existing top-level navigation behavior.
- Removing the currently selected tab returns the shell to Home after save.
- Unsupported or outdated destinations are removed during preference normalization.
- Malformed stored configuration falls back safely to the default.
- Feature business logic must not move into `TnyxShell`, `MainBottomNav`, or the preferences layer.

## 6. Suggested Preference Model

Persist stable identifiers, not localized labels, display order numbers, or icon resources.

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

- known and currently supported identifiers only,
- no duplicates,
- Home at index zero,
- minimum three tabs,
- maximum six tabs,
- and fallback to defaults when a valid configuration cannot be restored.

DataStore is the recommended local persistence mechanism. Cloud synchronization may be considered later, but navigation availability must never depend on network state.

## 7. UI And Accessibility Requirements

- Every tab and control has a readable accessibility label.
- Reordering supports accessible alternatives to gestures.
- Three through six tabs remain usable at supported font scales and in supported languages.
- Labels must not become icon-only solely to fit six destinations.
- AI may retain distinct visual treatment but remains a normal selectable destination.
- AI must not become a second floating action button; the app already has an independent FAB.
- Touch targets, navigation-bar insets, and selected/unselected contrast continue to follow design-system tokens.

## 8. Persistence And Migration

The stored schema should include a version when migrations become necessary.

On read:

1. decode the stored identifiers,
2. remove unknown or unavailable entries,
3. remove duplicates while preserving first occurrence,
4. force Home to index zero,
5. enforce the three-to-six range,
6. append required defaults only when necessary to restore validity,
7. otherwise fall back to the full default configuration.

New optional destinations must not be silently inserted into an existing valid user configuration.

## 9. Analytics And Privacy

Optional analytics may record:

- whether customization was used,
- the number of enabled tabs,
- enabled destination identifiers,
- and reset usage.

Do not include nutrition, workout, recovery, health, or other private metric values in customization events. Analytics must not be required for preferences to function.

## 10. Testing Requirements

### Unit tests

- default configuration,
- Home fixed at index zero,
- duplicate removal,
- unknown and unavailable destination removal,
- minimum and maximum enforcement,
- reset behavior,
- migration from an older schema,
- and corrupted preference fallback.

### UI tests

- reorder tabs,
- add and remove destinations,
- prevent invalid save,
- render three through six tabs,
- Reset to default,
- accessible reorder actions,
- font-scale and localization checks,
- and persistence after process recreation.

### Navigation tests

- every configurable destination resolves to a supported route,
- selected state derives from destination hierarchy,
- removing the active tab returns to Home,
- Profile behavior matches the chosen architecture,
- and `NoBottomBar` or `FullScreen` destinations still hide the bottom bar.

## 11. Delivery Order

1. Introduce stable tab identifiers and destination availability rules.
2. Add a DataStore-backed preferences repository with normalization.
3. Make `MainBottomNav` receive an ordered destination list.
4. Pass preferences through `MainScreen` and `TnyxShell`.
5. Add the Settings customization screen.
6. Add migration, unit, UI, navigation, and accessibility tests.
7. Expose Explore and Profile only after their route behavior is production-ready.

## 12. Non-Goals

This specification does not authorize:

- Home screen section customization,
- automatic AI-driven rearrangement,
- multiple floating action buttons,
- arbitrary deep screens as persistent tabs,
- feature business logic inside the app shell,
- placeholder modules solely to populate the catalog,
- or Supabase synchronization before local behavior is stable.

## 13. Acceptance Criteria

The feature is complete when:

- the default remains `Home | Nutrition | AI | Workout | Progress`,
- Settings is the only place where users customize tabs,
- Home remains mandatory and first,
- users can save a valid configuration containing three to six destinations,
- Reset restores the exact default,
- preferences survive app restart,
- invalid stored state falls back safely,
- navigation and chrome policies remain intact,
- and feature ownership boundaries remain unchanged.
