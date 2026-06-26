# Tio-app (Tnyx Android)

> एक premium Android fitness & nutrition app — Jetpack Compose, Clean Architecture, और Type-Safe Navigation के साथ built।

**Last updated: 2026-06-26**

---

## 📱 App Overview

**Tnyx** एक AI-powered health & wellness Android app है जो users को उनकी nutrition, workout, recovery और health targets track करने में help करता है। App एक personalized onboarding flow के साथ शुरू होता है जो user का profile, fitness goals, workout preference और health data collect करता है।

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

प्रोजेक्ट **Multi-module Clean Architecture** + **MVI (Model-View-Intent)** पैटर्न फॉलो करता है।

```text
Tnyx/
├── app/                              # Phone App (Main entry & Glue)
│   └── routing/                      # Navigation wiring
│       ├── AppNavHost.kt             # Root NavHost (Splash -> Main)
│       └── graphs/                   # Feature NavGraphs integration
│
├── wear/                             # Watch App (Wear OS module)
│   └── src/main/java/com/tnyx/wear/  # Wear Entry & Screens
│
├── core/                             # Shared Design System & UI Layer
│   ├── theme/                        # TnyxTheme — tokens & locals
│   │   └── tokens/                   # Foundation, Semantic, Component tokens
│   ├── ui/                           # Reusable UI & App Shell
│   │   ├── components/               # Buttons, Cards, Inputs, Sheets
│   │   └── shell/                    # App Chrome (BottomNav, TopBar, SecondaryNav)
│   └── routing/routes/               # ⭐ Global @Serializable Route definitions
│
├── shared/                           # ⭐ Pure Kotlin Domain (Phone + Watch)
│   └── workout/domain/               # Workout Models & Repo Interfaces
│       ├── model/                    # WorkoutSet, Session, Routine
│       └── repository/               # Repository interfaces
│
└── features/                         # Independent Feature Modules
    ├── onboarding/                   # Splash + Welcome entry flow
    ├── workout/                      # Workout tracking & navigation
    ├── auth/                         # Authentication & Profile management
    └── nutrition/                    # Nutrition diary, macro tracking
        ├── navigation/               # Feature-owned NavGraph
        └── presentation/             # MVI Screens (Route -> Screen -> ViewModel)
```

> 📄 विस्तृत architecture के लिए देखें: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

---

## 🎨 Design System — TnyxTheme

App **Type-Safe Navigation** use करता है — string routes नहीं, `@Serializable` objects।

```kotlin
@Serializable sealed interface WorkoutScreen {
    @Serializable data object History  : WorkoutScreen
    @Serializable data object Explore  : WorkoutScreen
    @Serializable data object Routines : WorkoutScreen
}
```

### Navigation Layers

| Layer | File | Role |
|---|---|---|
| Root | `AppNavHost.kt` | Splash → Auth → Onboarding → Main routing |
| Main Shell | `MainScreen.kt` | BottomNav shell, sub-tab derivation from back-stack |
| Workout | `WorkoutNavGraph.kt` | History / Explore / Routines destinations |
| Feature | `<Feature>NavGraph.kt` | Feature-owned screen routing |

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
| 5 | `WORKOUT` | 8–9 | Gym access, Equipment, Experience, Training days… |
| 6 | `TARGETS` | 6 | Steps, Sleep, Water, Goal pace, Nutrition targets |
| 7 | `SOURCE` | 2 | Discovery/referral source |

### Entry Paths

- **GET_STARTED** → Intro-first flow
- **SKIP** → Auth first, then Data section
- **SIGN_IN** → Existing user resume

> 📄 विस्तृत onboarding के लिए देखें: [docs/ONBOARDING_FLOW_DETAILED.md](docs/ONBOARDING_FLOW_DETAILED.md)

---

## 🏠 App Shell — TnyxShell

`TnyxShell` सभी screens को wrap करता है और app chrome provide करता है।

| Widget | Visibility | Description |
|---|---|---|
| `MainTopBar` | Home tab only | Dynamic top bar with user info |
| `MainBottomNav` | Always | 5-tab bottom navigation |
| `WorkoutSecondaryNav` | Workout tab only | Secondary sub-tab bar (History \| Explore \| Routines) |

### Workout Secondary Nav — Scroll Behavior

```
Workout tab press    → bar slides UP from bottom (enter, 400ms)
Scroll UP > 200dp   → bar slides DOWN (hide, 320ms)
Scroll stops 1.5s   → bar slides UP (auto-restore, 320ms)
Scroll DOWN         → bar slides UP immediately
Tab switch (exit)   → bar slides DOWN (exit, 280ms)
```

**Architecture Rule:** Shell सिर्फ bar show/hide करता है। Workout sub-sections (History, Explore, Routines) `WorkoutNavGraph` own करता है। Selected sub-tab `NavBackStack` से derive होता है — कोई अलग state नहीं।

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

```
app/src/main/
├── res/             # Strings, values, common resources
├── res-icons/       # App icons
├── res-images/      # Illustrations & large images
└── res-features/    # Feature-specific graphics
```

---

# Unit tests run करें
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
9. **Domain models `:shared` में लिखो** — Workout/Nutrition के models `shared/` में बनाओ ताकि Watch App भी use कर सके। `androidx.*` imports मत डालो `:shared` में
10. **Repository interfaces `:shared` में** — Implementation `:app` में होगी, interface `:shared` में — Watch App same interface use करेगा

---

## 📁 Documentation

| File | Description |
|---|---|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Core theme system, component library, feature folder patterns |
| [docs/NAVIGATION_GUIDE.md](docs/NAVIGATION_GUIDE.md) | Type-safe navigation, nested graphs, UI chrome policy |
| [docs/ONBOARDING_FLOW_DETAILED.md](docs/ONBOARDING_FLOW_DETAILED.md) | Complete onboarding runtime flow reference |
| [docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md](docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md) | Step-by-step Supabase tables, RLS, seed, and app integration plan |
| [docs/WEAR_OS_PLAN.md](docs/WEAR_OS_PLAN.md) | Wear OS feature plan — Workout + Nutrition watch app (post-production) |
| [docs/WEAR_OS_PROGRESS.md](docs/WEAR_OS_PROGRESS.md) | **Live Tracking** — Implementation status of the Wear OS app |

---

## 🗺️ Future Roadmap

### Kotlin Multiplatform (KMP) — iOS Support

> **⏳ यह production के बाद होगा — अभी जरूरत नहीं।**

App production-ready और feature-complete होने के बाद **KMP + Compose Multiplatform (CMP)** adopt किया जाएगा ताकि Android + iOS दोनों को एक codebase से serve किया जा सके।

**तब क्या होगा:**
- `:shared` module बनेगा — domain models, ViewModels, business logic share होगी
- Hilt → **Koin** (KMP-compatible DI)
- UI layer → **Compose Multiplatform** (अभी का Compose code iOS पर भी चलेगा)

**अभी इसलिए नहीं:**
- iOS development अभी scope में नहीं है
- Android features पहले complete और stable होने चाहिए
- CMP ecosystem अभी mature हो रहा है

**अभी KMP-ready habits follow करें:**
- Domain layer में `androidx.*` imports मत डालो
- Business logic UseCases में रखो (pure Kotlin)
- Repository interfaces define करो (implementations बाद में swap होंगी)

---

*Maintained by Android Engineering — Tnyx*
