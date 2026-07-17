# ADR-0005: User-Configurable Navigation And Home Layout

**Status:** Proposed  
**Date:** 2026-07-17

## Context

Tio currently exposes a fixed five-item bottom navigation:

```text
Home | Nutrition | AI | Workout | Progress
```

The product direction requires users to personalize the bottom navigation and Home screen from Settings. The app must preserve a simple default experience, feature ownership boundaries, type-safe navigation, chrome policy, accessibility, and safe persistence.

The same personalization mechanism must not turn the app shell into a business-logic owner or allow arbitrary deep destinations to become top-level tabs.

## Decision

Tio will support explicit user customization of:

1. bottom-navigation destination order and visibility,
2. Home section order and visibility.

Customization will be available only through Settings.

### Bottom navigation constraints

- Default: `Home | Nutrition | AI | Workout | Progress`.
- Home is required and remains first.
- Total enabled tabs: minimum three, maximum six.
- Duplicate destinations are invalid.
- Eligible destinations are defined by a stable catalog backed by supported route contracts.
- Initial catalog may include Home, Nutrition, AI, Workout, Progress, Explore, and Profile.
- Explore and Profile must not be exposed until their runtime navigation behavior is explicitly supported.
- Selected state continues to derive from the navigation back stack.

### Home layout constraints

Default sections:

1. Daily Overview
2. Nutrition
3. Activity
4. Recovery
5. Routine / Today's Plan

Users may reorder supported sections and hide optional sections. At least one section must remain visible.

### Persistence

Preferences will use stable identifiers and local DataStore-backed repositories. Stored configurations will be normalized to remove duplicates, unknown identifiers, unsupported destinations, and invalid counts. Corrupted state falls back to defaults.

### Ownership

- The app shell owns rendering and top-level navigation interaction only.
- Home owns section composition and ordering only.
- Feature domains continue to own their calculations, repositories, use cases, and presentation models.
- No feature ViewModel or internal screen will be imported into the shell or another feature.

### Automation boundary

The app may suggest customization changes, but it will not silently reorder or hide tabs or Home sections. AI-driven automatic rearrangement is outside this decision.

## Consequences

### Positive

- The default experience remains predictable.
- Different user priorities can be supported without creating multiple Home variants.
- Navigation and Home layout become state-driven and testable.
- Feature ownership remains compatible with the existing modular architecture.
- Reset behavior provides a deterministic recovery path.

### Costs

- Preference schema migration and normalization are required.
- Settings needs accessible reorder controls in addition to drag-and-drop.
- Every configurable destination and section needs stable identifiers and availability rules.
- Profile requires an explicit decision between a true MainGraph tab and an interim root-graph launcher.
- Six-tab layouts require font-scale and localization validation.

## Rejected Alternatives

### Keep all navigation fixed

Rejected because it does not support the requested personalization and forces one information hierarchy on every user.

### Add AI as another floating action button

Rejected because the app already has a FAB and multiple floating primary actions would create unclear hierarchy. AI remains a destination.

### Allow arbitrary screens as tabs

Rejected because deep destinations may not support persistent chrome, stable back-stack behavior, or top-level route semantics.

### Automatically personalize using behavior or AI

Rejected for the initial implementation because silent layout changes reduce predictability and user control.

### Store labels and icons directly

Rejected because localized labels and resources are unstable persistence identifiers.

## Implementation Guidance

The detailed product rules, suggested models, testing requirements, and delivery order are documented in:

- [`NAVIGATION_HOME_CUSTOMIZATION.md`](../NAVIGATION_HOME_CUSTOMIZATION.md)
- [`NAVIGATION_GUIDE.md`](../NAVIGATION_GUIDE.md)
- [`PROFILE_SETTINGS_GUIDE.md`](../PROFILE_SETTINGS_GUIDE.md)

## Validation Required Before Acceptance

- Product confirmation that Home remains fixed first.
- Product confirmation of the three-to-six tab range.
- Architecture decision for Profile when selected as a tab.
- Route readiness criteria for Explore.
- UX validation at supported font scales and languages.
- Unit, UI, navigation, migration, and accessibility test plans.
