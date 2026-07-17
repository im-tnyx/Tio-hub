# TNYX Production-Grade Navigation Architecture Guide

**Last updated: 2026-07-17**

यह guide TNYX app के 100+ screens scale को संभालने के लिए frozen navigation architecture define करती है। हम **Type-Safe Navigation (Kotlin Serialization)**, **Nested Graphs**, **Feature-Owned Navigation**, और **Chrome Policy** का उपयोग करते हैं।

Canonical ownership source: [PROFILE_SETTINGS_GUIDE.md](PROFILE_SETTINGS_GUIDE.md)

Bottom-navigation customization rules: [BOTTOM_NAVIGATION_CUSTOMIZATION.md](BOTTOM_NAVIGATION_CUSTOMIZATION.md)

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

Main Graph persistent app chrome और supported top-level destinations host करेगा:

```text
MainGraph
├── HomeGraph
├── NutritionGraph
├── AiCoach
├── WorkoutGraph
└── ProgressGraph
```

Default rendered order:

```text
Home | Nutrition | AI | Workout | Progress
```

Main Graph business rules own नहीं करेगा। Tab selection और selected destination derivation `NavBackStack` से होगी। Settings-backed preferences केवल eligible destinations की rendered list और order control कर सकती हैं; route graph supported-destination source of truth रहेगा।

### Layer 5: Profile Graph

Profile Graph avatar से launch होगा। Profile **Fitness Hub + Account Launcher** है, business domain नहीं। Profile own करेगा:

- User identity summary.
- Current plan summary.
- Personal Information UI.
- Launcher cards/shortcuts.

Profile Journey, Progress Photos, Rewards, Resources, Nutrition Targets, Workout Settings, Health Connections, या Subscription business logic own नहीं करेगा।

Profile को configurable persistent tab के रूप में expose करने से पहले उसे true `MainGraph` destination बनाने या root-graph launcher behavior को explicitly approve करने का architecture decision आवश्यक है।

### Layer 6: Settings Graph

Settings Graph gear icon से launch होगा। Settings app config और account controls का owner है। Feature-specific settings owning feature graph में रहेंगी; Settings केवल route launch कर सकता है। Bottom-navigation preferences UI और validation Settings/App Preferences में रहेंगी, जबकि shell केवल saved valid state render करेगा।

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
- Validated ordered tab state rendering.
- Chrome policy application.

Not allowed:

- Nutrition calculations.
- Workout save/update logic.
- Progress analytics.
- Subscription entitlement decision.
- Health connection logic.
- Feature repository calls.
- Destination eligibility decisions based on feature-internal runtime state.

---

## 5. UI Chrome Policy (TopBar & BottomNav)

- **BottomBar:** `TnyxShell` और `MainScreen.kt` में centralized रहेगा।
- **Rendered Tabs:** static private UI list की जगह validated Settings-backed ordered list receive करेगा।
- **Active State:** active tab `navController.currentBackStackEntry?.destination?.hierarchy` से derive होगा। Saved index active-state source नहीं होगा।
- **TopBar:** screen-level needs के हिसाब से owning Route/Screen wire करेगा। Shell generic top bar दे सकता है, feature-specific action नहीं।
- **Workout:** abhi clean placeholder hai. Shell mein Workout-specific secondary nav, scroll hide/show, ya sub-tab state nahi hai.
- **Profile Avatar:** `ProfileGraph` launch करेगा जब तक approved persistent-tab behavior implement नहीं होता।
- **Settings Gear:** `SettingsGraph` launch करेगा।

---

## 6. Configurable Bottom Navigation Policy

Canonical product details [BOTTOM_NAVIGATION_CUSTOMIZATION.md](BOTTOM_NAVIGATION_CUSTOMIZATION.md) में हैं। Architecture constraints:

- Default: `Home | Nutrition | AI | Workout | Progress`.
- Home mandatory है और index zero पर रहेगा।
- Valid total तीन से छह tabs है।
- Duplicate destinations invalid हैं।
- Preferences stable destination IDs store करेंगी, labels या icon resources नहीं।
- DataStore local persistence recommended है।
- Invalid, corrupted, unknown, या unavailable destinations normalize होंगे; unrecoverable state default पर fallback होगी।
- Current selected destination remove होने पर Save के बाद shell Home पर लौटेगा।
- New optional destinations existing valid user configuration में silently insert नहीं होंगे।
- Explore route/screen ready होने के बाद ही eligible होगा।
- Profile explicit graph behavior approve होने के बाद ही eligible होगा।
- Customization केवल Settings से होगी; AI या behavioral automation silently navigation change नहीं करेगा।
- Home screen section customization इस policy का हिस्सा नहीं है। Home app summary surface रहेगा और उसका layout separate future decision होगा।

---

## 7. Scaling Tips

1. **Nested Navigation:** हर feature अपना `NavGraphBuilder.navigation()` graph own करेगा।
2. **Deep Links:** Deep links public route contracts पर map होंगे, internal widget/screen classes पर नहीं।
3. **Stable Args:** IDs pass करें, data objects नहीं।
4. **Launcher vs Owner:** Profile/Settings launch कर सकते हैं; owning feature ही business logic और repository call करेगा।
5. **Chrome Policy:** हर new destination में chrome policy explicitly declare करें।
6. **Route Contracts:** Public route contracts को stable रखें; internal route names refactor हो सकते हैं।
7. **Eligibility:** हर top-level feature automatically configurable tab नहीं बनेगा; stable top-level route semantics required हैं।

---

## 8. Workout Placeholder Pattern

Workout tab currently has one simple placeholder destination inside `WorkoutNavGraph`.

Rules:

- Shell only owns top-level tab selection and bottom navigation.
- No Workout-specific secondary navigation lives in `TnyxShell`.
- No `WorkoutSubTab` state/action is part of shell state.
- No Workout cards, detail flow, or production redesign is introduced yet.
- Future Workout screens should be added inside the Workout feature graph when the product direction is ready.

---

## 9. Ownership-Aligned Navigation Examples

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

TNYX navigation freeze का लक्ष्य यह है कि 100+ screens के बाद भी `AppNavHost` छोटा रहे, `MainShell` feature-specific न बने, और teams independently feature graphs evolve कर सकें। Root graph app state handle करेगा, Main graph persistent chrome और supported top-level destinations handle करेगा, Settings validated user ordering persist करेगा, Profile/Settings launcher graphs रहेंगे जब तक explicitly promoted नहीं होते, और business logic हमेशा owning feature/domain में रहेगी।
