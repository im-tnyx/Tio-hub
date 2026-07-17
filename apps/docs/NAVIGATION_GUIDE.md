# TNYX Production-Grade Navigation Architecture Guide

**Last updated: 2026-07-17**

यह guide TNYX app के large-scale navigation architecture को define करती है। App **type-safe routes**, **nested graphs**, **feature-owned navigation**, **persistent shell chrome**, और **Settings-backed configurable top-level destinations** use करता है।

Canonical references:

- [PROFILE_SETTINGS_GUIDE.md](PROFILE_SETTINGS_GUIDE.md)
- [BOTTOM_NAVIGATION_CUSTOMIZATION.md](BOTTOM_NAVIGATION_CUSTOMIZATION.md)
- [ADR-0005](adr/0005-user-configurable-bottom-navigation.md)

---

## 1. Directory Structure

```text
core/src/main/java/com/tnyx/routing/routes/
├── RootRoutes.kt
├── MainRoutes.kt
├── NutritionRoutes.kt
├── WorkoutRoutes.kt
├── ProgressRoutes.kt
├── ProfileRoutes.kt
├── SettingsRoutes.kt
└── ChromePolicy.kt

app/src/main/java/com/tnyx/routing/
├── AppNavHost.kt
├── MainScreen.kt
├── NavigationActions.kt
└── graphs/MainGraph.kt

features/<feature>/navigation/
└── <Feature>NavGraph.kt
```

Public route contracts shared routing layer में रहते हैं। Feature internals owning feature module में रहते हैं। `:app` graph composition और cross-graph callbacks wire करता है।

---

## 2. Core Principles

### Type-safe routes

सभी public destinations `@Serializable` route objects/classes use करेंगे। String routes allowed नहीं हैं।

### Stable arguments

Routes केवल stable IDs pass करेंगी। Full data objects route arguments में pass नहीं होंगे।

### NavController ownership

Screens को `NavController` नहीं मिलेगा। Screens callbacks लेंगी; Route/NavGraph layer navigation perform करेगी।

### Feature ownership

Cross-feature navigation public routes से होगी। एक feature दूसरे feature के internal ViewModel, widget या repository को import नहीं करेगा।

Exception: `:app` composition layer reusable public Route entry points compose कर सकता है, जैसे MainGraph का `You` destination profile feature का public `ProfileHomeRoute` render करता है। Business logic फिर भी Profile feature में रहता है।

---

## 3. Graph Hierarchy

### Root Graph

```text
RootGraph
├── SplashGraph
├── AuthGraph
├── OnboardingGraph
├── MainGraph
├── ProfileGraph
├── SettingsGraph
└── ModalGraph
```

`ProfileGraph` migration/fallback launcher के रूप में root पर रह सकता है। `SettingsGraph` root-owned configuration flow है।

### Main Graph

MainGraph persistent shell और supported top-level destinations host करता है:

```text
MainGraph
├── Home
├── NutritionGraph
├── MealPlan
├── Tio
├── WorkoutGraph
├── WorkoutLibrary
├── ProgressGraph
└── You
```

Default rendered order:

```text
Home | Nutrition | Tio | Workout | Progress
```

Supported configurable catalog:

```text
Home | Nutrition | Meal Plan | Tio | Workout | Library | Progress | You
```

Preferences route graph को modify नहीं करतीं। वे केवल supported destinations की visibility और order control करती हैं।

### ProfileGraph and You

Profile business UI दो entry models support कर सकती है:

1. `You` — true MainGraph top-level destination with persistent bottom navigation.
2. `ProfileGraph` — root-level fallback launched from avatar when You enabled नहीं है या legacy flow needs root navigation.

Rules:

- You selected-state `MainRoute.You` से derive होगा।
- You enabled होने पर avatar You select करेगा।
- You disabled होने पर avatar root `ProfileGraph` launch कर सकता है।
- Profile calculations/business ownership Profile feature में ही रहेगी।

### SettingsGraph

Settings app preferences, account controls और bottom-navigation editor own करता है। Feature-specific settings owning feature graph में रहेंगी; Settings केवल public route launch करेगा।

---

## 4. Configurable Navigation Policy

Canonical product rules [BOTTOM_NAVIGATION_CUSTOMIZATION.md](BOTTOM_NAVIGATION_CUSTOMIZATION.md) में हैं। Architecture constraints:

- Home mandatory और index zero पर है।
- Valid total तीन से छह tabs है।
- Duplicate destinations invalid हैं।
- Stable IDs persist होंगे, labels/icons नहीं।
- DataStore local source of truth है।
- New optional destinations existing valid configuration में silently insert नहीं होंगे।
- Current selected tab remove होने पर shell Home पर लौटेगा।
- Customization केवल Settings से होगी।
- AI/behavior automation navigation silently change नहीं करेगा।

Stable display mapping:

| Stable ID | Display label |
| --- | --- |
| `home` | Home |
| `nutrition` | Nutrition |
| `meal_plan` | Meal Plan |
| `ai` | Tio |
| `workout` | Workout |
| `workout_library` | Library |
| `progress` | Progress |
| `you` | You |

---

## 5. Adaptive Home Boundary

Home detailed workflows own नहीं करता। Home enabled domains से high-level summary mode derive कर सकता है:

- Nutrition mode: Nutrition/Meal Plan enabled, Workout/Library disabled.
- Workout mode: Workout/Library enabled, Nutrition/Meal Plan disabled.
- Balanced mode: दोनों domain groups enabled.
- Custom mode: दोनों domain groups absent.

यह mode future card priority को guide करेगा। यह navigation order, saved preferences या feature calculations automatically change नहीं करेगा।

---

## 6. Selected-State Rules

Bottom navigation selected state हमेशा current destination hierarchy से derive होगा:

```kotlin
currentDestination.hierarchy.any { it.hasRoute(MainRoute.WorkoutGraph::class) }
```

Saved tab index selected-state source नहीं है। हर catalog destination का explicit route mapping होना चाहिए:

```text
Home            -> MainRoute.Home
Nutrition       -> MainRoute.NutritionGraph
Meal Plan       -> MainRoute.MealPlan
Tio             -> MainRoute.AiCoach
Workout         -> MainRoute.WorkoutGraph
Library         -> MainRoute.WorkoutLibrary
Progress        -> MainRoute.ProgressGraph
You             -> MainRoute.You
```

Top-level navigation `launchSingleTop`, state save और restore behavior use करेगी।

---

## 7. Chrome Policy

Allowed policies:

| Policy | Usage |
| --- | --- |
| `MainChrome` | Top-level MainGraph surfaces |
| `NoBottomBar` | Detail/edit flows |
| `FullScreen` | Auth, onboarding, camera, active workout |
| `BottomSheet` | Transient sheet |
| `Dialog` | Confirmation/legal/prompt |

Shell owns:

- top-level chrome,
- bottom-nav rendering,
- route-derived selected state,
- validated ordered tabs,
- and insets.

Shell does not own:

- nutrition calculations,
- meal-plan generation,
- workout execution logic,
- exercise-library repositories,
- progress analytics,
- profile/account repositories,
- subscription entitlement,
- or health integrations.

---

## 8. Feature Destination Boundaries

### Nutrition

NutritionGraph owns food logging, diary, editor, targets और nutrition detail flows.

### Meal Plan

Meal Plan top-level entry Nutrition ownership में है। Daily/weekly plans, meal suggestions और future grocery planning इसी domain में evolve होंगे।

### Workout

WorkoutGraph active/current training और session workflows own करता है।

### Library

Library Workout ownership में है। Exercises, saved routines, programs और templates इसी surface में evolve होंगे।

### Tio

Tio cross-domain coaching और suggestions own करता है। Nutrition-specific suggestions Meal Plan में, workout-specific suggestions Workout/Library में रह सकती हैं।

### You

You profile, goals, preferences, integrations, subscription और Settings launchers present करता है। Owning domains की business logic You में move नहीं होगी।

---

## 9. Scaling Rules

1. हर feature अपना internal graph own करेगा।
2. Deep links public route contracts पर map होंगे।
3. Stable IDs pass होंगे, full objects नहीं।
4. हर new top-level destination explicit selected-state mapping देगा।
5. हर destination chrome policy declare करेगा।
6. Catalog eligibility deliberate होगी; enum entry automatically user-facing tab नहीं बनेगी।
7. Optional destinations defaults में automatically add नहीं होंगे।
8. Root launcher और persistent-tab behavior स्पष्ट रूप से अलग रहेंगे।

---

## 10. Validation Checklist

- Every catalog item resolves to a route.
- Three through six tabs render correctly.
- Home remains first.
- Save persists after process restart.
- Removing active tab returns Home.
- You selected state remains inside MainGraph.
- Avatar follows You availability.
- Meal Plan and Library selected states are deterministic.
- Home mode derives correctly.
- No feature business logic enters the shell.

---

**CTO Note:** MainGraph persistent chrome और supported top-level routes own करता है; Settings user ordering own करता है; feature domains detailed workflows own करते हैं। यह separation nutrition-only, workout-only और mixed users को एक ही app architecture में support करता है।
