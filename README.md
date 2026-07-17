# Tio Hub

Tio Hub is the Android and Wear OS engineering home for **TNYX / Tio** — an AI-powered health, fitness, workout, nutrition, progress, and coaching companion.

This repository is built with a simple architectural promise: **Product features can grow fast, but boundaries stay clean.** UI remains dumb, business rules stay in ViewModel/domain layers, navigation stays type-safe, and backend/persistence details are introduced through repository abstractions instead of leaking into screens.

> [!NOTE]
> **Documentation Baseline:** Root documentation plus `apps/docs/` serve as the absolute source of truth for all future implementation work.

---

## 🎯 Project Overview

Tio is designed as a premium health and wellness application that helps users manage:

*   **📱 Personalized Onboarding:** Splash & welcome pathways to capture targets.
*   **🥗 Nutrition Diary:** Macro tracking, calorie count, water tracking, and meal editing.
*   **🏋️ Workout Management:** Training plans, routines, and historical logs.
*   **📈 Progress Tracker:** Weights, measurements, profile avatar launch, and configurations.
*   **⌚ Wear OS Companion:** Fast watch workflows and telemetry integration.
*   **🤖 AI Coach Integration:** Recovery, sleep insights, HRV sync, and Health Connect.

> [!IMPORTANT]
> **Tone of the project:** Practical, clean, aur contributor-friendly. *Agar aap naye contributor ho, welcome. Pehle docs padho, module boundary samjho, phir chhote focused PR se start karo.*

---

## 🏗️ Current Repository Shape

```text
Tio-hub/
├── README.md
├── CONTRIBUTING.md
├── CODE_OF_CONDUCT.md
├── LICENSE
├── .env            # Environment configuration (not committed)
├── .ai/            # Short AI/contributor orientation layer
└── apps/
    ├── app/        # Android phone app entry point, routing glue, DI composition
    ├── core/       # Design system, TnyxTheme, reusable UI, app shell, global routes
    ├── features/   # Feature modules: auth, onboarding, nutrition, workout, profile, settings, progress
    ├── shared/     # Pure Kotlin domain models and repository contracts for phone/watch reuse
    ├── wear/       # Wear OS companion app
    ├── docs/       # Architecture, navigation, onboarding, Supabase, testing, Wear OS plans
    ├── build.gradle.kts
    ├── settings.gradle.kts
    └── gradle/
```

> [!WARNING]
> The Gradle root for the Android project is currently `apps/`, not the repository root.

---

## 🧩 Architecture Overview

Tio-hub follows a **multi-module Clean Architecture** with an MVI-style presentation contract.

### 📐 Core Principles

*   **Clean Architecture:** Domain contracts stay separate from UI and platform wiring.
*   **MVI:** `Route -> ViewModel -> Contract -> Screen` is the preferred flow.
*   **Type-safe Navigation:** Route models use `@Serializable`; avoid stringly-typed routes.
*   **Feature Ownership:** Each feature owns its own navigation, presentation, and business behavior.
*   **Thin Shell:** App shell shows chrome and tab structure; it does not own feature logic.
*   **Token-first UI:** Screens and components consume `TnyxTheme`, not random hardcoded values.
*   **Supabase Temporary Abstraction:** Hardcoded data may exist as scaffolding only until a repository/Supabase-backed slice replaces it.

### 🗂️ Module Responsibilities

| Module | Responsibility / Scope |
| :--- | :--- |
| `apps/app` | Phone app entry, `MainActivity`, app-level navigation host, DI and platform wiring. |
| `apps/core` | Tnyx design system, reusable Compose components, shell, global route definitions, legal UI. |
| `apps/features/*` | Feature-owned Route/Screen/ViewModel/Contract/navigation code. |
| `apps/shared` | Pure Kotlin models, repository interfaces, and use cases shared by phone and watch. |
| `apps/wear` | Wear OS app entry, Wear routes, watch-specific UI and future sync UX. |

> [!IMPORTANT]
> **Dependency Rule:** `core` can be used by features, but `core` must not import feature modules. `shared` must stay pure Kotlin and should not depend on Android UI APIs.

---

## 📋 MVI Presentation Pattern

Most feature screens should follow this shape:

```text
features/<feature>/src/main/java/com/tnyx/features/<feature>/
├── navigation/
│   └── <Feature>NavGraph.kt
└── presentation/<workflow>/
    ├── <Workflow>Route.kt
    ├── <Workflow>Screen.kt
    ├── <Workflow>ViewModel.kt
    ├── <Workflow>Contract.kt
    └── widgets/
```

### Role Clarity

| Role | Responsibility |
| :--- | :--- |
| **`Route`** | Collect state, collect one-off effects, wire navigation, provide ViewModel. |
| **`Screen`** | Render immutable `UiState`, emit `Action`; no repository/API/database/NavController access. |
| **`ViewModel`** | Handle `Action`, update `UiState`, emit `Effect`, call domain/repository layers. |
| **`Contract`** | Keep `UiState`, `Action`, and `Effect` for that screen/workflow together. |
| **`widgets/`** | Small composables with explicit callbacks; no business logic. |

> [!IMPORTANT]
> **Hard Rule:** *Screen dumb UI rahega. Business decision ViewModel/domain layer mein rahegi.*

---

## 🧭 Navigation Policy

Tio-hub uses **Compose Navigation 2.8.x type-safe navigation**.

### Expected Route Style

```kotlin
@Serializable
sealed interface NutritionRoute {
    @Serializable
    data object Diary : NutritionRoute

    @Serializable
    data class MealEditor(val mealId: String?) : NutritionRoute
}
```

### Navigation Guidelines

*   Prefer serializable route models/classes.
*   Do not introduce raw string routes for new flows.
*   Feature modules own their internal graph.
*   Cross-feature navigation should happen through public route contracts.
*   Bottom navigation remains focused on primary app tabs.
*   Profile is avatar-launched; Settings is gear-launched.

*Canonical navigation details live in [`apps/docs/NAVIGATION_GUIDE.md`](apps/docs/NAVIGATION_GUIDE.md).*

---

## ⚡ Supabase and Future Backend Strategy

Current app slices may still contain temporary sample or hardcoded data. That is acceptable only as UI scaffolding.

The current implementation strategy is **Supabase-backed app completion first, custom backend later**. Do not create a backend runtime before the core Android/Wear product flows and repository contracts are stable.

### The target direction is:

1.  Define domain/repository contract.
2.  Keep shared phone/watch models in `apps/shared` where reuse is real.
3.  Add Supabase-backed implementation in the platform/data layer during the current app phase.
4.  Keep screens rendering `UiState`; never let screens depend on table shape.
5.  Add RLS, seed data, and validation when a slice becomes persistent.
6.  Start custom backend planning later through an ADR-backed runtime decision.

> [!CAUTION]
> No service-role key, admin key, private key, or production secret belongs in Android, Wear, web public code, screenshots, or committed docs.

*See [`apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md`](apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md), [`apps/docs/BACKEND_TRANSITION_PLAN.md`](apps/docs/BACKEND_TRANSITION_PLAN.md), and [`apps/docs/adr/0005-backend-transition-after-supabase-app-completion.md`](apps/docs/adr/0005-backend-transition-after-supabase-app-completion.md).*

---

## 🚀 Getting Started

### Prerequisites

*   Android Studio with recent Kotlin/Compose support (Jellyfish or newer recommended).
*   **JDK 21** configured for Gradle builds.
*   Android SDK for compile/target SDK used by the project (API 35).
*   Git command line.
*   A local `.env` only if a feature explicitly requires runtime environment values.

### Clone and Open

```bash
git clone https://github.com/im-tnyx/Tio-hub.git
cd Tio-hub
```
