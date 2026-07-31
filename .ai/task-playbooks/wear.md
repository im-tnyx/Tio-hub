# Wear OS Task Playbook

Use this guide when adding or modifying Wear OS companion features in Tio-hub.

## Pre-Work

1. Confirm the Phone-side feature exists first — Wear only extends Phone features.
2. Check `apps/docs/WEAR_OS_PLAN.md` for the current Wear scope and roadmap.
3. Check `apps/docs/WEAR_OS_PROGRESS.md` for current implementation status.
4. Identify the Phone/Wear shared contract needed (if any).

## Module Ownership

```
apps/shared/   ← Phone+Wear shared domain contracts (pure Kotlin only)
apps/wear/     ← Wear-specific runtime, screens, ViewModels
apps/app/      ← Phone-side implementations and DI
```

## Shared Contract Rules

Move a type to `apps/shared/` only when:
- Both Phone AND Wear need the same type at runtime.
- The type is pure Kotlin (no Android/Compose imports).

Do not put Wear-only types in `apps/shared/`.

## Wear Screen Pattern

Same pattern as Phone:

```
apps/wear/src/main/java/com/tnyx/wear/
  <screen>/
    <Screen>Route.kt
    <Screen>Screen.kt       ← Wear Compose (Horologist or bare Compose for Wear)
    <Screen>ViewModel.kt
    <Screen>UiState.kt
    <Screen>Action.kt
```

## Validation

```bash
cd apps
.\gradlew.bat :wear:compileDebugKotlin
.\gradlew.bat :wear:testDebugUnitTest --no-configuration-cache
```

## Canonical References

- [Wear OS Plan](../../../apps/docs/WEAR_OS_PLAN.md)
- [Wear OS Progress](../../../apps/docs/WEAR_OS_PROGRESS.md)
- `apps/wear/`
- `apps/shared/`
