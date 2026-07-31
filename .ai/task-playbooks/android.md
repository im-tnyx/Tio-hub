# Android Task Playbook

Use this guide when starting a new Android feature slice in Tio-hub.

## Pre-Work Checklist

Before writing any code:

1. Confirm feature ownership in `apps/docs/PROFILE_SETTINGS_GUIDE.md`.
2. Check `apps/docs/ANDROID_APP_PROGRESS.md` for current implementation status.
3. Check `apps/docs/ARCHITECTURE.md` for relevant patterns.
4. Run the mandatory UI core audit from `.ai/core/ui-rules.md`.
5. Identify the Supabase table needed (if any) from `apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md`.

## Standard Slice Shape

Every new feature screen follows this pattern:

```
apps/features/<feature>/
  src/main/java/com/tnyx/<feature>/
    ui/
      <Screen>Route.kt        ← navigation entry point
      <Screen>Screen.kt       ← dumb Compose UI only
      <Screen>ViewModel.kt    ← state + action handler
      <Screen>UiState.kt      ← sealed/data class state
      <Screen>Action.kt       ← sealed class actions
    domain/
      <Feature>Repository.kt  ← interface contract
    data/
      Supabase<Feature>Repository.kt  ← Supabase implementation
```

## Build And Validate

```bash
cd apps

# Compile check (fast)
.\gradlew.bat :app:compileDebugKotlin

# Feature unit tests
.\gradlew.bat :<feature>:testDebugUnitTest --no-configuration-cache

# Full compile + feature test (pre-commit)
.\gradlew.bat :features:<feature>:testDebugUnitTest :app:compileDebugKotlin --no-configuration-cache
```

## DI Registration

New repositories register in `apps/app/src/main/java/com/tnyx/app/di/`.

Check existing `*Module.kt` files before creating a new one.

## Supabase Table Flow

See `.ai/core/supabase-rules.md` for full rules.

Short flow:
1. Build UI slice with empty/local state first.
2. Identify real data shape from the feature.
3. Create minimum migration under `supabase/migrations/`.
4. Add RLS policy.
5. Connect repository implementation.
6. Test end to end.

## Wear OS

If the feature has a Wear companion:
- Shared contracts go in `apps/shared/`.
- Wear runtime goes in `apps/wear/`.
- Check `apps/docs/WEAR_OS_PLAN.md` before adding shared types.

## Done Criteria

Reference `apps/docs/DEFINITION_OF_DONE.md` before marking any slice complete.
