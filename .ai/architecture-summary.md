# Architecture Summary

TNYX Android uses a Gradle multi-module architecture with feature-owned modules and a Settings-configurable MainGraph shell.

The target shape is Clean Architecture with feature-level vertical slices.

## Core Rules

- App shell owns top-level navigation and chrome only.
- Feature modules own their routes, screens, view models, repositories, and feature logic.
- Screens are dumb UI.
- Business logic belongs in `ViewModel`, use cases, repositories, or domain services.
- Cross-feature navigation goes through public route contracts.
- Shared domain-safe models belong in shared modules only when reused across features or Wear OS.
- Do not move feature business logic into `app`, `core`, or shell code.

## Android UI Pattern

Use this pattern for feature screens:

- `Route`
- `Screen`
- `ViewModel`
- `UiState`
- `Action`

Compose screens must not perform network calls, repository writes, persistence, or mutation logic.

## Main Navigation

Exact default:

```text
Home | Nutrition | Tio | Workout | Progress
```

Supported optional catalog:

```text
Home | Nutrition | Meal Plan | Tio | Workout | Library | Progress | You
```

Rules:

- Home is fixed first.
- Users select three through six tabs in Settings.
- Stable IDs persist; labels and icons do not.
- Meal Plan belongs to Nutrition.
- Library belongs to Workout.
- You is the MainGraph profile destination.
- When You is enabled, avatar selects You; otherwise avatar may open root ProfileGraph.
- Home derives Nutrition, Workout, Balanced, or Custom summary mode from enabled domain tabs.
- The app never silently changes saved navigation.

Settings remains root-launched from the gear/You experience.

## Future Modules

Create modules only when runtime code needs them.

Do not create empty future modules for:

- Health
- Recovery
- Billing
- Entitlement
- Community
- Challenges
- Learn / Resources
- Rewards
- Analytics

Document the owner first, then add the module when the feature slice starts.
