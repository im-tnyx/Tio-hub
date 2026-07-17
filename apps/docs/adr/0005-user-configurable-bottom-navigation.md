# ADR-0005: User-Configurable Bottom Navigation

**Status:** Accepted  
**Date:** 2026-07-17

## Context

Tio serves different user intents: some users primarily log nutrition, some primarily train, and others use a mixed coaching experience. A single fixed information hierarchy cannot represent all of these workflows well.

The predictable first-launch and reset default remains:

```text
Home | Nutrition | Tio | Workout | Progress
```

Home is a cross-domain summary. Detailed actions remain in the owning tabs, but Home may adapt its summary emphasis to the explicitly enabled domains.

## Decision

Tio supports explicit user customization of bottom-navigation order and visibility through Settings.

### Constraints

- Home is required and remains first.
- Total enabled tabs: minimum three, maximum six.
- Duplicate destinations are invalid.
- Reset restores the exact five-tab default.
- Settings is the only surface that changes the saved configuration.
- The app never silently reorders, adds, or removes tabs.
- Selected state derives from the navigation hierarchy.

### Supported catalog

The ordered catalog is:

```text
Home
Nutrition
Meal Plan
Tio
Workout
Library
Progress
You
```

Stable identifiers remain independent of display labels:

- `ai` displays as Tio,
- `workout_library` displays as Library,
- `you` displays as You.

Meal Plan and Library are optional top-level foundations owned by Nutrition and Workout respectively. They do not change the default configuration.

### You architecture

You is promoted to a true MainGraph top-level destination that renders the profile experience inside the persistent shell.

- When You is enabled, the avatar selects You.
- When You is disabled, the avatar may continue opening the root ProfileGraph.

This resolves the previous Profile architecture question without pretending a root-graph launcher is a persistent tab.

### Adaptive Home boundary

Home derives a high-level mode from the enabled domain tabs:

- Nutrition: Nutrition and/or Meal Plan only,
- Workout: Workout and/or Library only,
- Balanced: at least one nutrition and one workout domain,
- Custom: neither domain group.

This mode may prioritize future Home summaries. It does not authorize automatic navigation changes or move feature business logic into the shell.

### Persistence

Preferences use stable identifiers in a local DataStore-backed repository. Normalization removes duplicates, unknown destinations and invalid counts, forces Home first, caps the list at six and falls back safely to the default.

New optional destinations are never inserted into an existing valid saved configuration.

### Ownership

- The shell owns rendering and top-level navigation interaction.
- The preferences layer owns visibility, order, validation and migration.
- Nutrition owns Meal Plan workflows.
- Workout owns Library workflows.
- Profile/Settings ownership is presented under You.
- Feature calculations, repositories and detailed screens remain outside the shell.

## Consequences

### Positive

- Tio can behave like a nutrition app, workout app or mixed coaching app without separate products.
- The default remains simple and stable.
- Users explicitly control their primary workflows.
- You provides a clearer user-centered label than Profile.
- Home has a deterministic basis for future adaptive summaries.

### Costs

- Every catalog destination requires a supported route and selected-state mapping.
- Six-tab layouts require font-scale and localization validation.
- Meal Plan and Library need feature-owned product implementation beyond their initial foundation screens.
- Root ProfileGraph and MainGraph You must coexist safely during migration.

## Rejected Alternatives

### Fixed navigation for every user

Rejected because it forces the same hierarchy on nutrition-only, workout-only and mixed users.

### Automatic AI personalization

Rejected because silent navigation changes reduce predictability and user control.

### Profile as a fake launcher tab

Rejected because a root-level launcher cannot provide correct persistent selected-state behavior.

### Suggestions as a standalone tab

Rejected for now. Nutrition suggestions belong in Meal Plan, workout suggestions belong in Workout/Library, and cross-domain coaching belongs in Tio.

### Full Home customization in this ADR

Rejected. This decision establishes deterministic Home mode only; detailed Home card composition remains separate product work.

## Implementation Reference

Detailed rules and tests are documented in:

- [`BOTTOM_NAVIGATION_CUSTOMIZATION.md`](../BOTTOM_NAVIGATION_CUSTOMIZATION.md)
- [`NAVIGATION_GUIDE.md`](../NAVIGATION_GUIDE.md)
- [`PROFILE_SETTINGS_GUIDE.md`](../PROFILE_SETTINGS_GUIDE.md)

## Validation Still Required

- local compile and unit-test execution,
- runtime persistence after process restart,
- selected-state checks for all optional routes,
- six-tab font-scale and localization checks,
- and accessibility review of reorder and toggle controls.
