# Tio-hub

Tio-hub is the Android and Wear OS engineering home for **TNYX / Tio**, an AI-powered health, fitness, workout, nutrition, progress, and coaching companion.

This repository is built with a simple architectural promise: product features can grow fast, but boundaries stay clean. UI remains dumb, business rules stay in ViewModel/domain layers, navigation stays type-safe, and backend/persistence details are introduced through repository abstractions instead of leaking into screens.

Documentation baseline: root docs plus `apps/docs/` are the source of truth for future implementation work.

---

## Project Overview

Tio is designed as a premium health and wellness app that helps users manage:

- personalized onboarding
- nutrition diary, macro tracking, meal editing, and target setup
- workout planning and workout history
- progress, measurements, profile, and settings
- Wear OS companion workflows
- future AI Coach, recovery, Health Connect, subscription, and backend-backed sync

Tone of the project: practical, clean, aur contributor-friendly. Agar aap naye contributor ho, welcome. Pehle docs padho, module boundary samjho, phir chhote focused PR se start karo.

---

## Current Repository Shape

```text
Tio-hub/
|-- README.md
|-- CONTRIBUTING.md
|-- CODE_OF_CONDUCT.md
|-- LICENSE
|-- .env            # Environment configuration (not committed)
`-- apps/
    |-- app/        # Android phone app entry point, routing glue, DI composition
    |-- core/       # Design system, TnyxTheme, reusable UI, app shell, global routes
    |-- features/   # Feature modules: auth, onboarding, nutrition, workout, profile, settings, progress
    |-- shared/     # Pure Kotlin domain models and repository contracts for phone/watch reuse
    |-- wear/       # Wear OS companion app
    |-- docs/       # Architecture, navigation, onboarding, Supabase, testing, Wear OS plans
    |-- build.gradle.kts
    |-- settings.gradle.kts
    `-- gradle/
```

The Gradle root for the Android project is currently `apps/`, not the repository root.

---

## Architecture Overview

Tio-hub follows **multi-module Clean Architecture** with an MVI-style presentation contract.

### Core Principles

- **Clean Architecture**: domain contracts stay separate from UI and platform wiring.
- **MVI**: `Route -> ViewModel -> Contract -> Screen` is the preferred flow.
- **Type-safe Navigation**: route models use `@Serializable`; avoid stringly-typed routes.
- **Feature ownership**: each feature owns its own navigation, presentation, and business behavior.
- **Thin shell**: app shell shows chrome and tab structure; it does not own feature logic.
- **Token-first UI**: screens and components consume `TnyxTheme`, not random hardcoded values.
- **Supabase temporary abstraction**: hardcoded data may exist as scaffolding only until a repository/Supabase-backed slice replaces it.

### Module Responsibilities

| Module | Responsibility |
|---|---|
| `apps/app` | Phone app entry, `MainActivity`, app-level navigation host, DI and platform wiring |
| `apps/core` | Tnyx design system, reusable Compose components, shell, global route definitions, legal UI |
| `apps/features/*` | Feature-owned Route/Screen/ViewModel/Contract/navigation code |
| `apps/shared` | Pure Kotlin models, repository interfaces, and use cases shared by phone and watch |
| `apps/wear` | Wear OS app entry, Wear routes, watch-specific UI and future sync UX |

Important rule: `core` can be used by features, but `core` must not import feature modules. `shared` must stay pure Kotlin and should not depend on Android UI APIs.

---

## MVI Presentation Pattern

Most feature screens should follow this shape:

```text
features/<feature>/src/main/java/com/tnyx/features/<feature>/
|-- navigation/
|   `-- <Feature>NavGraph.kt
`-- presentation/<workflow>/
    |-- <Workflow>Route.kt
    |-- <Workflow>Screen.kt
    |-- <Workflow>ViewModel.kt
    |-- <Workflow>Contract.kt
    `-- widgets/
```

Role clarity:

| Role | Responsibility |
|---|---|
| `Route` | Collect state, collect one-off effects, wire navigation, provide ViewModel |
| `Screen` | Render immutable `UiState`, emit `Action`; no repository/API/database/NavController access |
| `ViewModel` | Handle `Action`, update `UiState`, emit `Effect`, call domain/repository layers |
| `Contract` | Keep `UiState`, `Action`, and `Effect` for that screen/workflow together |
| `widgets/` | Small composables with explicit callbacks; no business logic |

Hard rule: `Screen` dumb UI rahega. Business decision ViewModel/domain layer mein rahegi.

---

## Navigation Policy

Tio-hub uses **Compose Navigation 2.8.x type-safe navigation**.

Expected route style:

```kotlin
@Serializable
sealed interface NutritionRoute {
    @Serializable
    data object Diary : NutritionRoute

    @Serializable
    data class MealEditor(val mealId: String?) : NutritionRoute
}
```

Navigation guidelines:

- Prefer serializable route models/classes.
- Do not introduce raw string routes for new flows.
- Feature modules own their internal graph.
- Cross-feature navigation should happen through public route contracts.
- Bottom navigation remains focused on primary app tabs.
- Profile is avatar-launched; Settings is gear-launched.

Canonical navigation details live in [`apps/docs/NAVIGATION_GUIDE.md`](apps/docs/NAVIGATION_GUIDE.md).

---

## Supabase And Data Strategy

Current app slices may still contain temporary sample or hardcoded data. That is acceptable only as UI scaffolding.

The target direction is:

1. Define domain/repository contract.
2. Keep shared phone/watch models in `apps/shared` where reuse is real.
3. Add Supabase/backend-backed implementation in the platform/data layer.
4. Keep screens rendering `UiState`; never let screens depend on table shape.
5. Add RLS, seed data, and validation when a slice becomes persistent.

No service-role key, admin key, private key, or production secret belongs in Android, Wear, web public code, screenshots, or committed docs.

See [`apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md`](apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md).

---

## Getting Started

### Prerequisites

- Android Studio with recent Kotlin/Compose support
- JDK 21
- Android SDK for compile/target SDK used by the project
- Git
- A local `.env` only if a feature explicitly requires runtime environment values

### Clone And Open

```bash
git clone <repo-url>
cd Tio-hub
```

Open the `apps/` directory in Android Studio, then sync Gradle.

### Build From Terminal

From the repository root:

```bash
cd apps
./gradlew :app:assembleDebug
```

On Windows PowerShell:

```powershell
cd apps
.\gradlew.bat :app:assembleDebug
```

### Run Tests

```bash
cd apps
./gradlew test
```

On Windows:

```powershell
cd apps
.\gradlew.bat test
```

For focused checks, prefer module-level commands:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :features:nutrition:testDebugUnitTest
./gradlew :shared:test
```

### Run The App

1. Open `apps/` in Android Studio.
2. Select the `app` configuration for Android phone.
3. Select the `wear` module/configuration for Wear OS work.
4. Build and run on emulator or physical device.

---

## Documentation Index

| Document | Purpose |
|---|---|
| [`apps/README.md`](apps/README.md) | Android app-focused technical overview |
| [`apps/docs/ARCHITECTURE.md`](apps/docs/ARCHITECTURE.md) | Module ownership, design system, shell, feature patterns |
| [`apps/docs/NAVIGATION_GUIDE.md`](apps/docs/NAVIGATION_GUIDE.md) | Type-safe navigation and graph policy |
| [`apps/docs/PROFILE_SETTINGS_GUIDE.md`](apps/docs/PROFILE_SETTINGS_GUIDE.md) | Profile, Settings, Progress, ownership rules |
| [`apps/docs/ONBOARDING_FLOW_DETAILED.md`](apps/docs/ONBOARDING_FLOW_DETAILED.md) | Complete onboarding flow reference |
| [`apps/docs/TNYX_MODULAR_ONBOARDING.md`](apps/docs/TNYX_MODULAR_ONBOARDING.md) | Modular onboarding implementation guide |
| [`apps/docs/NUTRITION.md`](apps/docs/NUTRITION.md) | Nutrition runtime, UI, and persistence roadmap |
| [`apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md`](apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md) | Supabase migration plan and security boundaries |
| [`apps/docs/TESTING_GUIDE.md`](apps/docs/TESTING_GUIDE.md) | Testing strategy and expectations |
| [`apps/docs/ANDROID_APP_PROGRESS.md`](apps/docs/ANDROID_APP_PROGRESS.md) | Android implementation progress |
| [`apps/docs/WEAR_OS_PLAN.md`](apps/docs/WEAR_OS_PLAN.md) | Wear OS planning |
| [`apps/docs/WEAR_OS_PROGRESS.md`](apps/docs/WEAR_OS_PROGRESS.md) | Wear OS progress tracking |

---

## Contributing

Contributions are welcome. Please read [`CONTRIBUTING.md`](CONTRIBUTING.md) before opening a PR.

Quick expectations:

- Work from a focused branch.
- Keep changes small and reviewable.
- Follow Clean Architecture and MVI boundaries.
- Use `TnyxTheme` tokens for UI.
- Keep navigation type-safe.
- Do not commit secrets, generated outputs, APKs/AABs, `.env`, or local caches.
- Update docs when architecture, navigation, persistence, or ownership changes.

---

## Code Of Conduct

This project follows a contributor-friendly Code of Conduct. Be respectful, specific, and constructive. Disagreement is fine; personal attacks are not.

See [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md).

---

## License

Tio-hub is licensed under the MIT License. See [`LICENSE`](LICENSE).

---
**Last Updated:** 2026-06-29
Maintained by TNYX Engineering.
