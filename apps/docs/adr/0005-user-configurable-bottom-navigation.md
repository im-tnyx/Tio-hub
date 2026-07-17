# ADR-0005: User-Configurable Bottom Navigation

**Status:** Proposed  
**Date:** 2026-07-17

## Context

Tio currently exposes a fixed five-item bottom navigation:

```text
Home | Nutrition | AI | Workout | Progress
```

The product direction requires users to personalize eligible top-level destinations from Settings while preserving a simple default experience, type-safe navigation, chrome policy, accessibility, and feature ownership boundaries.

Home is a cross-domain summary surface. Its layout and section composition will be designed separately after the owning tabs and their workflows are mature. This decision therefore covers bottom navigation only.

## Decision

Tio will support explicit user customization of bottom-navigation destination order and visibility through Settings.

### Constraints

- Default: `Home | Nutrition | AI | Workout | Progress`.
- Home is required and remains first.
- Total enabled tabs: minimum three, maximum six.
- Duplicate destinations are invalid.
- Eligible destinations come from a stable catalog backed by supported top-level route contracts.
- Initial catalog may include Home, Nutrition, AI, Workout, Progress, Explore, and Profile.
- Explore and Profile must not be exposed until their runtime navigation behavior is explicitly supported.
- Selected state continues to derive from the navigation back stack.
- Reset restores the exact default configuration.
- Settings is the only surface that changes the saved tab configuration.

### Persistence

Preferences use stable identifiers and a local DataStore-backed repository. Stored configurations are normalized to remove duplicates, unknown identifiers, unavailable destinations, and invalid counts. Corrupted state falls back to defaults.

### Ownership

- The app shell owns rendering and top-level navigation interaction only.
- The preferences layer owns saved ordering, visibility, validation, and migration only.
- Feature domains continue to own their calculations, repositories, use cases, and screens.
- No feature ViewModel or internal screen is imported into the shell or another feature.

### Home boundary

Home screen section customization is not part of this ADR. Home remains a summary of important information from owning domains; detailed actions and management stay inside Nutrition, AI, Workout, Progress, and future eligible tabs.

### Automation boundary

The app may explain or suggest customization options, but it will not silently reorder, add, or hide destinations. AI-driven automatic rearrangement is outside this decision.

## Consequences

### Positive

- The default experience remains predictable.
- Users can prioritize their most-used top-level areas.
- Bottom navigation becomes state-driven and testable.
- Feature ownership remains compatible with the modular architecture.
- Reset provides a deterministic recovery path.

### Costs

- Preference schema migration and normalization are required.
- Settings needs accessible reorder controls in addition to drag-and-drop.
- Every configurable destination needs a stable identifier and availability rules.
- Profile needs an explicit decision between a true `MainGraph` tab and an interim root-graph launcher.
- Six-tab layouts require font-scale and localization validation.

## Rejected Alternatives

### Keep all navigation fixed

Rejected because it forces one information hierarchy on every user and does not support the requested personalization.

### Add AI as another floating action button

Rejected because the app already has a FAB and multiple floating primary actions create unclear hierarchy. AI remains a destination.

### Allow arbitrary screens as tabs

Rejected because deep destinations may not support persistent chrome, stable back-stack behavior, or top-level route semantics.

### Automatically personalize using behavior or AI

Rejected for the initial implementation because silent navigation changes reduce predictability and user control.

### Include Home layout customization in the same decision

Rejected for now because Home summarizes the entire app and depends on stable domain tabs, contracts, and product priorities. Home layout will be specified separately later.

### Store labels and icons directly

Rejected because localized labels and resources are unstable persistence identifiers.

## Implementation Guidance

Detailed product rules, suggested models, testing requirements, and delivery order are documented in:

- [`BOTTOM_NAVIGATION_CUSTOMIZATION.md`](../BOTTOM_NAVIGATION_CUSTOMIZATION.md)
- [`NAVIGATION_GUIDE.md`](../NAVIGATION_GUIDE.md)
- [`PROFILE_SETTINGS_GUIDE.md`](../PROFILE_SETTINGS_GUIDE.md)

## Validation Required Before Acceptance

- Product confirmation that Home remains fixed first.
- Product confirmation of the three-to-six tab range.
- Architecture decision for Profile when selected as a tab.
- Route readiness criteria for Explore.
- UX validation at supported font scales and languages.
- Unit, UI, navigation, migration, and accessibility test plans.
