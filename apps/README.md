# Tio-app (Tnyx Android)

> एक premium Android health, fitness, nutrition, recovery, workout, coaching, और Wear-ready app — Jetpack Compose, Clean Architecture, और Type-Safe Navigation के साथ built।

**Last updated: 2026-06-27**

---

## 📱 App Overview

**Tnyx** एक AI-powered health & wellness Android app है जो users को nutrition, workout, recovery, health targets, progress, और coaching workflows track करने में help करता है। App एक personalized onboarding flow के साथ शुरू होता है जो user का profile, fitness goals, workout preference, health inputs, और target data collect करता है।

Architecture को 100+ screens, Wear OS, future iOS/KMP readiness, AI Coach, Health integrations, Subscription, और Offline Mode को ध्यान में रखकर रखा गया है।

Canonical ownership source: [docs/PROFILE_SETTINGS_GUIDE.md](docs/PROFILE_SETTINGS_GUIDE.md)

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose + Material 3 |
| Architecture | Multi-module Clean Architecture (MVI pattern) |
| DI | Hilt |
| Navigation | Compose Navigation (Type-Safe, `@Serializable` routes) |
| Async | Kotlin Coroutines + Flow |
| Auth | Firebase Auth *(planned)* |
| Serialization | Kotlin Serialization (`kotlinx.serialization`) |
| Dependency Versions | Gradle Version Catalog (`libs.versions.toml`) |
| Min SDK | API 26 (Android 8.0) |
| Target/Compile SDK | API 35 (Android 15) |
| Java Version | Java 21 |

---

## 🏗️ Architecture Overview

प्रोजेक्ट **Multi-module Clean Architecture** + **MVI (Model-View-Intent)** pattern follow करता है। Runtime source actual behavior की final truth है; product/ownership decisions के लिए canonical docs follow करें।

Current checked-in module structure:

```text
Tnyx/
├── app/                              # Phone app entry, DI composition, RootNavHost
│   └── routing/                      # Navigation wiring
│       ├── AppNavHost.kt             # Root graph orchestration
│       └── graphs/                   # Main graph and feature graph wiring
│
├── wear/                             # Wear OS companion app
│   └── src/main/java/com/tnyx/wear/  # Wear entry and screens
│
├── core/                             # Shared design system, UI components, shell
│   ├── theme/                        # TnyxTheme — tokens and locals
│   │   └── tokens/                   # Foundation, Semantic, Component tokens
│   ├── ui/                           # Reusable UI and App Shell
│   │   ├── components/               # Buttons, Cards, Inputs, Sheets
│   │   └── shell/                    # App Chrome (BottomNav, TopBar, SecondaryNav)
│   └── routing/routes/               # Global @Serializable route definitions
│
├── shared/                           # Pure Kotlin domain contracts (Phone + Watch)
│   └── workout/domain/               # Current shared Workout models/repositories
│       ├── model/
│       └── repository/
│
└── features/                         # Independent feature modules
    ├── onboarding/                   # Splash + Welcome entry flow
    ├── auth/                         # Authentication/session entry
    ├── workout/                      # Workout tracking and navigation
    ├── nutrition/                    # Nutrition diary and macro tracking
    ├── profile/                      # Fitness Hub + Account Launcher skeleton
    ├── settings/                     # App Config skeleton
    └── progress/                     # Progress tab skeleton
```

Canonical target ownership for future work:

```text
features/
├── home/          # Main dashboard, no cross-domain business ownership
├── workout/       # Workout settings, rest timer, plate calculator, graph settings, RPE
├── nutrition/     # Nutrition targets, calories, macros, water goal, glass size
├── coach/         # AI Coach surfaces
├── progress/      # Journey, progress photos, measurements, weight, achievements
├── profile/       # Fitness Hub + Account Launcher, personal information source
├── settings/      # App config, notifications, units, theme, language, account controls
├── health/        # Future: Health Connect, Samsung Health, Garmin, Fitbit, Apple Health
├── recovery/      # Future: Sleep, HRV, readiness, recovery score
├── billing/       # Future: subscription UI and entitlement/business logic
├── rewards/       # Future: rewards, badges, gamification
└── learn/         # Future: resources, education, guides
```

> 📄 विस्तृत architecture के लिए देखें: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

---

## 🎨 Design System — TnyxTheme

App **Type-Safe Navigation** use करता है — string routes नहीं, `@Serializable` route models।

```kotlin
@Serializable sealed interface WorkoutRoute {
    @Serializable data object History  : WorkoutRoute
    @Serializable data object Explore  : WorkoutRoute
    @Serializable data object Routines : WorkoutRoute
}
```

---

## 🧭 Navigation Freeze

TNYX navigation 100+ screens के लिए graph-owned और feature-owned रहेगी।

| Layer | File/Graph | Role |
|---|---|---|
| Root | `AppNavHost.kt` / `RootGraph` | Splash → Auth → Onboarding → Main/Profile/Settings/Modal routing |
| Auth | `AuthGraph` | Sign in, sign up, OTP, password reset |
| Onboarding | `OnboardingGraph` | Initial data collection and resume flow |
| Main Shell | `MainScreen.kt` / `MainGraph` | Bottom nav shell, tab state from back-stack |
| Feature | `<Feature>NavGraph.kt` | Feature-owned internal screen routing |
| Profile | `ProfileGraph` | Avatar-launched Fitness Hub + Account Launcher |
| Settings | `SettingsGraph` | Gear-launched App Config surfaces |
| Modal | `ModalGraph` | App-wide dialogs, sheets, permission prompts, media flows |

Main Graph contains only primary tabs:

```text
Home
Workout
Nutrition
Coach
Progress
```

Profile is launched from avatar. Settings is launched from gear icon. Cross-feature navigation must use public route contracts.

> 📄 विस्तृत navigation के लिए देखें: [docs/NAVIGATION_GUIDE.md](docs/NAVIGATION_GUIDE.md)

---

## 🚀 Onboarding Flow

App launch होने पर `SplashActivity` → `MainActivity` → `RootNavHost` का sequence चलता है।

### Onboarding Sections

| # | Section | Steps | Description |
|---|---|---|---|
| 1 | `INTRO` | 5 | Welcome/education screens (skippable) |
| 2 | `DATA` | 9 | Name, Gender, Goal, Age, Height, Weight, Activity, Health |
| 3 | `MOBILE` | 1 | Phone verification (dynamic insertion) |
| 4 | `WORKOUT_INTRO` | 1 | Workout plan opt-in (Yes/No) |
| 5 | `WORKOUT` | 8-9 | Gym access, Equipment, Experience, Training days… |
| 6 | `TARGETS` | 6 | Step target, sleep target, water target, goal pace, and nutrition targets collected by onboarding and saved through owning domains |
| 7 | `SOURCE` | 2 | Discovery/referral source |

Ownership note:
- Personal Information -> Profile repository.
- Nutrition Targets / Calories / Macros / Water / Glass Size -> Nutrition repository.
- Workout preferences/settings -> Workout repository.
- Steps/Health connections -> Health ownership when implemented.
- Sleep/HRV/Readiness -> Recovery ownership when implemented.

### Entry Paths

- **GET_STARTED** → Intro-first flow
- **SKIP** → Auth first, then Data section
- **SIGN_IN** → Existing user resume

> 📄 विस्तृत onboarding के लिए देखें: [docs/ONBOARDING_FLOW_DETAILED.md](docs/ONBOARDING_FLOW_DETAILED.md)

---

## 🏠 App Shell — TnyxShell

`TnyxShell` app chrome provide करता है। Shell feature business logic own नहीं करता।

| Widget | Visibility | Description |
|---|---|---|
| `MainTopBar` | MainChrome destinations where needed | Dynamic top bar with user/account entry points |
| `MainBottomNav` | `MainChrome` destinations | 5-tab bottom navigation: Home, Workout, Nutrition, Coach, Progress |
| `WorkoutSecondaryNav` | Workout tab only | Secondary sub-tab bar (History \| Explore \| Routines) |

### Chrome Policy

Every destination declares one chrome policy:

| Policy | Usage |
|---|---|
| `MainChrome` | Main tab screens with bottom nav and normal shell |
| `NoBottomBar` | Detail/edit screens where bottom nav hidden रहे |
| `FullScreen` | Auth, onboarding, camera, media viewer, active workout session |
| `BottomSheet` | Sheet-style transient destination |
| `Dialog` | Confirmation, warning, legal, permission explanation |

### Workout Secondary Nav — Scroll Behavior

```text
Workout tab press    → bar slides UP from bottom (enter, 400ms)
Scroll UP > 200dp   → bar slides DOWN (hide, 320ms)
Scroll stops 1.5s   → bar slides UP (auto-restore, 320ms)
Scroll DOWN         → bar slides UP immediately
Tab switch (exit)   → bar slides DOWN (exit, 280ms)
```

**Architecture Rule:** Shell सिर्फ chrome show/hide करता है। Workout sub-sections (History, Explore, Routines) `WorkoutNavGraph` own करता है। Selected sub-tab `NavBackStack` से derive होता है — कोई अलग source of truth नहीं।

---

## 📋 Presentation Layer Rules (MVI Contract)

| Role | Responsibility |
|---|---|
| `Route` | ViewModel collect करे, effects handle करे, navigation wire करे |
| `Screen` | Dumb UI — केवल `UiState` render करे, `Action` emit करे |
| `ViewModel` | `Action` handle करे, `UiState` mutate करे, `Effect` emit करे |
| `Contract` | `UiState` + `Action` + `Effect` एक file में |
| `widgets/` | Explicit callbacks लें, business logic नहीं |

**Hard Rule:** `Screen` में repository, API, database, NavController या mutable business state नहीं होगा।

---

## 🗂️ Resource Structure

```text
app/src/main/
├── res/             # Strings, values, common resources
├── res-icons/       # App icons
├── res-images/      # Illustrations & large images
└── res-features/    # Feature-specific graphics
```

---

# Unit tests run करें

```bash
./gradlew test

# Lint check
./gradlew lint
```

---

## 📏 Coding Golden Rules

1. **No hardcoded design values** — Spacing, colors, typography के लिए `TnyxTheme.*` use करें
2. **Core must stay feature-agnostic** — `core` से `features` import नहीं होना चाहिए
3. **Screens are dumb UI** — Business decisions ViewModel/domain layer में रहें
4. **Reusable UI goes to `core/ui`** — Multiple features में reused components को promote करें
5. **Semantic colors first** — Raw palette नहीं, `TnyxTheme.colors.*` use करें
6. **Type-safe navigation** — Serializable route models prefer करें
7. **No secrets in client** — Keys, private keys, keystores repository में नहीं
8. **Generated output commit न करें** — `build/`, `.gradle/`, APK/AAB, `.env` ignore करें
9. **Domain models `:shared` में लिखो** — Phone/Watch reuse वाले models `shared/` में बनाओ। `androidx.*` imports मत डालो `:shared` में
10. **Repository interfaces `:shared` में** — Shared contracts `:shared` में रखें; implementation platform/data layer में होगी
11. **Feature launchers thin रखें** — Profile, Settings, Home, और Coach दूसरे feature की business logic या repositories own नहीं करेंगे
12. **Ownership matrix wins** — ambiguity हो तो [docs/PROFILE_SETTINGS_GUIDE.md](docs/PROFILE_SETTINGS_GUIDE.md) follow करें
13. **Hardcoded data temporary रखें** — Supabase Incremental Setup Plan is the source for replacing sample data with repositories, seed data, RLS, and future TypeScript/Turborepo contracts

---

## 📁 Documentation

| File | Description |
|---|---|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Core theme system, component library, shell, feature folder patterns, ownership freeze summary |
| [docs/NUTRITION.md](docs/NUTRITION.md) | Nutrition Meal Diary runtime, UI components, data boundary, and persistence roadmap |
| [docs/NAVIGATION_GUIDE.md](docs/NAVIGATION_GUIDE.md) | 100+ screen type-safe navigation, nested graphs, route contracts, chrome policy |
| [docs/PROFILE_SETTINGS_GUIDE.md](docs/PROFILE_SETTINGS_GUIDE.md) | Canonical Profile, Settings, Progress, Billing, Health, Recovery ownership reference |
| [docs/ONBOARDING_FLOW_DETAILED.md](docs/ONBOARDING_FLOW_DETAILED.md) | Complete onboarding runtime flow reference |
| [docs/TNYX_MODULAR_ONBOARDING.md](docs/TNYX_MODULAR_ONBOARDING.md) | Modular onboarding implementation guide |
| [docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md](docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md) | Plan for replacing hardcoded data with Supabase-backed slices, RLS, seed data, and future TypeScript/Turborepo boundaries |
| [docs/TESTING_GUIDE.md](docs/TESTING_GUIDE.md) | Testing strategy and guidelines |
| [docs/ANDROID_APP_PROGRESS.md](docs/ANDROID_APP_PROGRESS.md) | Android mobile app implementation progress tracking |
| [docs/WEAR_OS_PLAN.md](docs/WEAR_OS_PLAN.md) | Wear OS feature plan — Workout + Nutrition watch app |
| [docs/WEAR_OS_PROGRESS.md](docs/WEAR_OS_PROGRESS.md) | Live tracking — Implementation status of the Wear OS app |

---

## 🗺️ Future Roadmap

### Kotlin Multiplatform (KMP) — iOS Support

> **⏳ यह production के बाद होगा — अभी जरूरत नहीं।**

App production-ready और feature-complete होने के बाद **KMP + Compose Multiplatform (CMP)** adopt किया जा सकता है ताकि Android + iOS दोनों को एक codebase से serve किया जा सके।

**तब क्या होगा:**
- `:shared` में stable domain models, repository interfaces, और pure use cases रहेंगे
- Hilt → KMP-compatible DI strategy evaluate होगी
- UI layer future CMP path के लिए clean boundaries रखेगी

**अभी इसलिए नहीं:**
- iOS development अभी immediate scope में नहीं है
- Android features पहले stable होने चाहिए
- CMP ecosystem और product scope दोनों को पहले validate करना होगा

**अभी KMP-ready habits follow करें:**
- Domain layer में `androidx.*` imports मत डालो
- Business logic UseCases में रखो (pure Kotlin)
- Repository interfaces define करो (implementations बाद में swap होंगी)
- Phone/Watch/future-iOS reuse वाले contracts `:shared` में रखें

---

> देखें: [Supabase Incremental Setup Plan](docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md)

*Maintained by Android Engineering — Tnyx*