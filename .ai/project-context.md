# Project Context

TNYX is an AI health, fitness, nutrition, recovery, coaching, workout, wearable, and future multi-platform product.

The current checked-in app focus is native Android and Wear OS under `apps/`.

## Repository Identity

- This context applies to `G:\projects\Tio-hub`.
- `G:\projects\Tnyx-hub` is a separate repository and is not runtime, backend, database, documentation, branch, or task truth for this checkout.
- Flutter or other-repository work may be used only when the active task explicitly requests comparison or migration analysis.

## Current Platform Scope

- `apps/app`: Android application shell and root navigation.
- `apps/core`: shared Android runtime, routes, theme, and foundational utilities.
- `apps/shared`: reusable Android/Kotlin contracts and shared domain-safe types.
- `apps/features/auth`: authentication flow.
- `apps/features/onboarding`: onboarding flow.
- `apps/features/workout`: workout domain and screens.
- `apps/features/nutrition`: nutrition domain and screens.
- `apps/features/profile`: Fitness Hub + Account Launcher.
- `apps/features/settings`: app/account settings entry points.
- `apps/features/progress`: progress-owned journey, photos, measurements, weight, achievements, and analytics.
- `apps/wear`: Wear OS companion foundation.

## Current Status

The Android app is in architecture-foundation and vertical-slice buildout.

For authoritative implementation status, see:

- [Android App Progress](../apps/docs/ANDROID_APP_PROGRESS.md)

Do not infer production readiness from skeleton modules.

## Future Platform Scope

Do not build future platform work until explicitly requested.
For the long-term product vision, see:

- [Architecture](../apps/docs/ARCHITECTURE.md)
- [Supabase Incremental Setup Plan](../apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md)
