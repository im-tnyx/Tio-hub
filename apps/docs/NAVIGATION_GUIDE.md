# TNYX Production-Grade Navigation Architecture Guide

**Last updated: 2026-06-27**

यह guide TNYX app के 100+ screens scale को संभालने के लिए frozen navigation architecture define करती है। हम **Type-Safe Navigation (Kotlin Serialization)**, **Nested Graphs**, **Feature-Owned Navigation**, और **Chrome Policy** का उपयोग करते हैं।

Canonical ownership source: [PROFILE_SETTINGS_GUIDE.md](PROFILE_SETTINGS_GUIDE.md)

---

## 1. Directory Structure (Modular)

बड़े app के लिए navigation को decoupled रखना अनिवार्य है। Public route contracts shared routing layer में रहेंगे, feature internals feature module में रहेंगे, और `:app` केवल graph wiring करेगा।

```text
core/src/main/java/com/tnyx/routing/routes/
├── RootRoutes.kt           # Splash, Auth, Onboarding, Main, Modal graph routes
├── AuthRoutes.kt           # Auth public routes
├── MainRoutes.kt           # Main tab graph routes
├── NutritionRoutes.kt      # Nutrition public routes
├── WorkoutRoutes.kt        # Workout public routes
├── ProgressRoutes.kt       # Progress public routes
├── ProfileRoutes.kt        # Avatar-launched profile routes
├── SettingsRoutes.kt       # Gear-launched settings routes
└── ChromePolicy.kt         # MainChrome, NoBottomBar, FullScreen, BottomSheet, Dialog

app/src/main/java/com/tnyx/routing/
├── AppNavHost.kt           # Root graph orchestration
├── MainScreen.kt           # Main shell and tab graph host
└── graphs/                 # Root/Main/Modal graph wiring

features/<feature>/navigation/
└── <Feature>NavGraph.kt    # Feature-owned internal navigation
```

Rule: Feature internal routes/widgets/ViewModels दूसरे feature से import नहीं होंगे। Cross-feature navigation public route contracts से होगी।

---

## 2. Core Principles

### A. Type-Safety (No String Routes)

हम हमेशा `@Serializable` route models use करते हैं। इससे compile-time safety रहती है और deep links/args predictable रहते हैं।

```kotlin
@Serializable sealed interface NutritionRoute {
    @Serializable data object Home : NutritionRoute
    @Serializable data object Targets : NutritionRoute
    @Serializable data class MealDetails(val mealId: String) : NutritionRoute
}
```

### B. Route Args Policy

Routes में केवल **stable IDs** pass करें, जैसे `mealId: String`, `workoutId: String`, `photoId: String`। पूरी data object route args में pass नहीं होगी। ViewModel ID से repository/use case के जरिए data load करेगा।

### C. NavController Ownership

Screens को कभी भी `NavController` नहीं मिलेगा। Screens हमेशा lambdas लेंगी, जैसे `onBack`, `onMealClick`, `onSaveClick`। Navigation wiring `Route` या `NavGraph` layer में रहेगी।

### D. Public Route Contracts

Cross-feature navigation केवल public routes से होगी:

```kotlin
navigate(NutritionRoute.Targets)
navigate(WorkoutRoute.Settings)
navigate(ProgressRoute.Journey)
navigate(ProfileRoute.Home)
navigate(SettingsRoute.Home)
```

Profile और Settings launcher हो सकते हैं, लेकिन दूसरे features की business logic own नहीं करेंगे।

---

## 3. Navigation Hierarchy

### Layer 1: Root Graph (`AppNavHost.kt`)

Root Graph app-level flow decide करेगा:

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

Root Graph feature internals नहीं जानेगा। यह only graph-level transitions own करेगा।

### Layer 2: Auth Graph

Auth Graph sign in, sign up, OTP, forgot/reset password, और auth session entry own करेगा। Auth subscription entitlement, profile domain data, या onboarding target ownership own नहीं करेगा।

### Layer 3: Onboarding Graph

Onboarding initial data collection और resume flow own करेगा। Onboarding collected data को owning repositories में save करेगा:

- Personal Information -> Profile repository.
- Nutrition Targets -> Nutrition repository.
- Workout preferences -> Workout repository.
- Health/Recovery targets -> future owning repositories.

### Layer 4: Main Graph (`MainScreen.kt`)

Main Graph persistent app chrome और bottom tabs host करेगा:

```text
MainGraph
├── HomeGraph
├── WorkoutGraph
├── NutritionGraph
├── CoachGraph
└── ProgressGraph
```

Main Graph business rules own नहीं करेगा। Tab selection और selected destination derivation `NavBackStack` से होगी।

### Layer 5: Profile Graph

Profile Graph avatar से launch होगा। Profile **Fitness Hub + Account Launcher** है, business domain नहीं। Profile own करेगा:

- User identity summary.
- Current plan summary.
- Personal Information UI.
- Launcher cards/shortcuts.

Profile Journey, Progress Photos, Rewards, Resources, Nutrition Targets, Workout Settings, Health Connections, या Subscription business logic own नहीं करेगा।

### Layer 6: Settings Graph

Settings Graph gear icon से launch होगा। Settings app config और account controls का owner है। Feature-specific settings owning feature graph में रहेंगी; Settings केवल route launch कर सकता है।

### Layer 7: Modal Graph

Modal Graph app-wide modal destinations own करेगा:

- Permission prompts.
- Confirmation dialogs.
- Paywall sheets.
- Legal dialogs.
- Media picker / camera flows.

Feature-specific modals owning feature graph में रह सकते हैं।

---

## 4. Chrome Policy

हर destination अपनी chrome policy declare करेगा। Shell destination name guess करके UI hide/show नहीं करेगा।

Allowed policies:

| Policy | Usage |
|---|---|
| `MainChrome` | Main tab screens with bottom nav and normal shell. |
| `NoBottomBar` | Detail/edit screens where bottom nav hidden रहे। |
| `FullScreen` | Auth, onboarding, camera, media viewer, active workout session. |
| `BottomSheet` | Sheet-style transient destination. |
| `Dialog` | Confirmation, warning, legal, permission explanation. |

### MainShell Rule

`MainShell` feature-specific UI logic contain नहीं करेगा।

Allowed:
- Top bar.
- Bottom nav.
- Shell-level spacing/insets.
- Route-derived selected tab.
- Chrome policy application.
- Workout secondary nav visibility trigger.

Not allowed:
- Nutrition calculations.
- Workout save/update logic.
- Progress analytics.
- Subscription entitlement decision.
- Health connection logic.
- Feature repository calls.

---

## 5. UI Chrome Policy (TopBar & BottomNav & SecondaryNav)

- **BottomBar:** `TnyxShell` और `MainScreen.kt` में centralized रहेगा।
- **Active State:** active tab `navController.currentBackStackEntry?.destination?.hierarchy` से derive होगा।
- **TopBar:** screen-level needs के हिसाब से owning Route/Screen wire करेगा। Shell generic top bar दे सकता है, feature-specific action नहीं।
- **WorkoutSecondaryNav:** केवल Workout tab पर visible होगा। इसका navigation `WorkoutNavGraph` में जाएगा। Screens को `NavController` नहीं मिलेगा।
- **Profile Avatar:** `ProfileGraph` launch करेगा।
- **Settings Gear:** `SettingsGraph` launch करेगा।

---

## 6. Scaling Tips

1. **Nested Navigation:** हर feature अपना `NavGraphBuilder.navigation()` graph own करेगा।
2. **Deep Links:** Deep links public route contracts पर map होंगे, internal widget/screen classes पर नहीं।
3. **Stable Args:** IDs pass करें, data objects नहीं।
4. **Launcher vs Owner:** Profile/Settings launch कर सकते हैं; owning feature ही business logic और repository call करेगा।
5. **Chrome Policy:** हर new destination में chrome policy explicitly declare करें।
6. **Route Contracts:** Public route contracts को stable रखें; internal route names refactor हो सकते हैं।

---

## 7. Workout Sub-Navigation Pattern

Workout tab में secondary nav bar (`History | Explore | Routines`) एक अलग navigation layer provide करता है।

```kotlin
is ShellAction.WorkoutSubTabSelected -> {
    val workoutRoute = when (action.tab) {
        WorkoutSubTab.History  -> WorkoutRoute.History
        WorkoutSubTab.Explore  -> WorkoutRoute.Explore
        WorkoutSubTab.Routines -> WorkoutRoute.Routines
    }
    mainNavController.navigate(workoutRoute) {
        popUpTo<MainRoute.WorkoutGraph> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
```

Rules:

- `popUpTo<MainRoute.WorkoutGraph>` Workout graph root तक pop करता है।
- `saveState = true` + `restoreState = true` tab switch पर scroll/state preserve करता है।
- `launchSingleTop = true` duplicate destinations avoid करता है।
- Shell को `NavController` नहीं देना; `MainScreen` action से navigation wire करेगा।

---

## 8. Ownership-Aligned Navigation Examples

| User Action | Navigate To | Business Logic Owner |
|---|---|---|
| Profile Journey card tap | `ProgressRoute.Journey` | Progress |
| Profile Progress Photos tap | `ProgressRoute.ProgressPhotos` | Progress |
| Profile Nutrition Targets tap | `NutritionRoute.Targets` | Nutrition |
| Profile Workout Settings tap | `WorkoutRoute.Settings` | Workout |
| Profile Health Connections tap | `HealthRoute.Connections` | Health |
| Profile Current Plan tap | `BillingRoute.Subscription` | Billing / Entitlement |
| Settings Subscription row tap | `BillingRoute.Subscription` | Billing / Entitlement |
| Settings Units row tap | `SettingsRoute.Units` | Settings |

---

**CTO Note:**
TNYX navigation freeze का लक्ष्य यह है कि 100+ screens के बाद भी `AppNavHost` छोटा रहे, `MainShell` feature-specific न बने, और teams independently feature graphs evolve कर सकें। Root graph app state handle करेगा, Main graph persistent chrome handle करेगा, Profile/Settings launcher graphs होंगे, और business logic हमेशा owning feature/domain में रहेगी।