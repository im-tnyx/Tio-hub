# Bottom Navigation Customization

**Status:** Product and architecture specification  
**Last updated:** 2026-07-17

## 1. Purpose

Tio keeps a predictable default navigation while allowing each user to shape the app around their primary use case:

- nutrition-focused,
- workout-focused,
- mixed coaching,
- or a custom combination.

Customization remains explicit and Settings-owned. The app must not silently reorder, add, or remove tabs based on AI, behavior, onboarding answers, or usage history.

Home is a cross-domain summary. Its detailed card design remains separate, but its summary emphasis may be derived deterministically from the user’s enabled domain tabs.

## 2. Default Configuration

Reset and first-launch defaults remain:

```text
Home | Nutrition | Tio | Workout | Progress
```

Rules:

- Home is mandatory and remains first.
- The default contains five tabs.
- A user may configure between three and six total tabs.
- Duplicate destinations are invalid.
- Changes apply only after Save.
- Reset restores exactly the default five tabs and order.
- New optional destinations are never inserted silently into an existing valid configuration.

## 3. Supported Destination Catalog

The supported ordered catalog is:

1. Home
2. Nutrition
3. Meal Plan
4. Tio
5. Workout
6. Library
7. Progress
8. You

### Destination ownership

| Destination | Product responsibility |
| --- | --- |
| Home | Adaptive summary and next actions only |
| Nutrition | Food logging, calories, macros and nutrition targets |
| Meal Plan | Daily/weekly plans, meal suggestions and future grocery planning |
| Tio | Cross-domain AI coaching and suggestions |
| Workout | Current training, session execution and history entry points |
| Library | Exercises, routines, programs and templates |
| Progress | Trends, measurements, photos and achievements |
| You | Profile, goals, preferences, integrations, subscription and Settings launchers |

`Tio` is the display label for the persisted `ai` destination identifier. `Library` persists as `workout_library` so its ownership remains unambiguous. `You` is the user-facing profile destination.

### Route eligibility

A destination enters the catalog only when it has:

- a stable top-level route,
- persistent-chrome behavior,
- deterministic selected-state behavior,
- and a safe runtime foundation screen.

Meal Plan and Library may begin as foundation surfaces while their detailed product experiences are implemented inside Nutrition and Workout ownership respectively.

### You behavior

You is a true MainGraph top-level destination, not a fake launcher tab. It renders the profile experience inside the persistent shell. The avatar follows this rule:

- when You is enabled, the avatar selects You;
- when You is not enabled, the avatar may continue launching the root ProfileGraph.

### Explore

Explore/Discover remains future work and is not part of the current catalog. It may be added only after a distinct production experience and route are defined.

## 4. Settings Experience

```text
Settings
└── App Settings
    └── Personalization
        └── Bottom navigation
```

The editor provides:

- accessible Move up and Move down actions,
- add and remove controls,
- a visible minimum of three tabs,
- a visible maximum of six tabs,
- Home fixed and non-removable,
- a live preview,
- Save,
- and Reset.

Drag gestures may be added later, but accessible deterministic controls are required regardless.

## 5. Runtime Architecture

```text
BottomNavPreferencesRepository
        ↓
MainScreenViewModel
        ↓
MainScreen
        ↓
TnyxShell
        ↓
MainBottomNav(configuredTabs)
```

The stable catalog and route graph define supported destinations. Preferences control visibility and order only.

Required behavior:

- Selected state derives from the current navigation hierarchy.
- Re-selecting uses top-level save/restore behavior.
- Removing the active tab returns to Home after save.
- Unsupported or malformed identifiers fall back safely.
- Feature business logic does not move into the shell or preferences layer.
- The default does not change when optional destinations are introduced.

## 6. Adaptive Home Mode

Home derives a high-level mode from enabled tabs:

| Enabled domain tabs | Home mode |
| --- | --- |
| Nutrition and/or Meal Plan, without Workout/Library | Nutrition |
| Workout and/or Library, without Nutrition/Meal Plan | Workout |
| At least one nutrition domain and one workout domain | Balanced |
| Neither domain group | Custom |

This mode controls future summary priority, not navigation order.

Examples:

```text
Home | Nutrition | Meal Plan
→ Nutrition-focused Home

Home | Workout | Library
→ Workout-focused Home

Home | Nutrition | Tio | Workout | Progress | You
→ Balanced Home
```

Detailed logging, planning, workout execution and profile management remain in their owning tabs.

## 7. Persistence Model

Persist stable identifiers, never display labels or icon resources.

```kotlin
val DefaultBottomTabs = listOf(
    Home,
    Nutrition,
    Ai,
    Workout,
    Progress,
)

val SupportedCatalog = listOf(
    Home,
    Nutrition,
    MealPlan,
    Ai,
    Workout,
    WorkoutLibrary,
    Progress,
    You,
)
```

Normalization enforces:

- supported identifiers only,
- no duplicates,
- Home at index zero,
- minimum three tabs,
- maximum six tabs,
- and deterministic default fallback.

Local DataStore remains the source of truth. Navigation availability must never depend on network state.

## 8. Accessibility And Layout

- Every tab and control has a readable accessibility label.
- Reordering always has non-gesture controls.
- Three through six tabs remain usable at supported font scales.
- Labels do not become icon-only solely to fit six items.
- Tio is a normal icon-and-label destination, not a circular floating action.
- Touch targets, insets and selected-state contrast follow design-system tokens.

## 9. Analytics And Privacy

Optional analytics may record enabled stable destination identifiers, tab count, save and reset usage. Nutrition, workout, health or profile metric values must never be included in customization events.

## 10. Testing Requirements

### Unit

- exact default configuration,
- stable optional identifiers,
- Home-first enforcement,
- duplicate removal,
- minimum and maximum enforcement,
- unsupported identifier removal,
- reset behavior,
- corrupted preference fallback,
- and Home mode derivation.

### UI and navigation

- add/remove/reorder tabs,
- prevent invalid save,
- render three through six tabs,
- persistence after restart,
- every catalog destination resolves,
- selected state follows the route hierarchy,
- You remains inside the persistent shell,
- avatar behavior follows You availability,
- and removing the active tab returns Home.

## 11. Non-Goals

This specification does not authorize:

- silent AI-driven navigation changes,
- arbitrary deep screens as persistent tabs,
- feature business logic inside the app shell,
- automatic insertion of optional destinations,
- cloud synchronization before local behavior is stable,
- or a full Home card-layout redesign in the navigation implementation.

## 12. Acceptance Criteria

The navigation model is complete when:

- default remains `Home | Nutrition | Tio | Workout | Progress`,
- Settings is the only customization surface,
- Home remains mandatory and first,
- users can save three to six supported destinations,
- Meal Plan, Library and You are optional,
- Reset restores the exact default,
- preferences survive restart,
- routes and selected states remain deterministic,
- Home derives the correct domain mode,
- and feature ownership remains unchanged.
