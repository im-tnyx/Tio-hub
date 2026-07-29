# Health Placeholder

Reserved ownership placeholder for future `:features:health`.

Planned ownership:

- Android Health Connect integration.
- Samsung Health, Garmin, and Fitbit connection surfaces.
- Future Apple Health provider contract where platform/backend support exists.
- Steps and daily activity summaries.
- Health permissions, consent, connection state, and sync status.
- Health repository contracts and provider-specific adapters.

Planned structure when runtime implementation begins:

```text
features/health/
├── navigation/
├── presentation/
│   ├── home/
│   ├── connections/
│   ├── permissions/
│   └── sync_status/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
└── data/
    ├── health_connect/
    └── providers/
```

Sleep, HRV, readiness, and recovery score belong to `:features:recovery`.
Nutrition and Workout calculations remain inside their owning features.

This folder is not wired in `settings.gradle.kts` or `app/build.gradle.kts` yet.
Add the feature module structure here when runtime implementation begins.
