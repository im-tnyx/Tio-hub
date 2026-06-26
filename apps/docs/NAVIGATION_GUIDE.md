# TNYX Production-Grade Navigation Architecture Guide

**Last updated: 2026-06-26**

यह गाइड TNYX ऐप के स्केल (60-80 screens) को संभालने के लिए बनाए गए आधुनिक और स्केलेबल नेविगेशन स्ट्रक्चर को समझाती है।
 हम **Type-Safe Navigation (Kotlin Serialization)**, **Nested Graphs**, और **Feature-Owned Architecture** का उपयोग कर रहे हैं।

## 1. Directory Structure (Modular)

बड़े ऐप के लिए नेविगेशन को डिकपल्ड (decoupled) रखना अनिवार्य है। रूट्स को `:core` में रखने से सर्कुलर डिपेंडेंसी से बचा जा सकता है।

```text
core/src/main/java/com/tnyx/routing/routes/
├── RootRoutes.kt           # Global @Serializable route definitions
├── AuthRoutes.kt
├── MainRoutes.kt
└── NutritionRoutes.kt

app/src/main/java/com/tnyx/routing/
├── AppNavHost.kt           # रूट नेविगेशन (Splash -> Welcome -> Main)
├── MainScreen.kt           # BottomNav Shell Layout
└── graphs/                 # Feature Graphs wiring (Auth, Main, Nutrition)

features/X/navigation/
└── XNavGraph.kt            # Feature-specific internal navigation
```

---

## 2. Core Principles (मुख्य सिद्धांत)

### A. Type-Safety (No String Routes)
हम हमेशा `@Serializable` ऑब्जेक्ट्स का उपयोग करते हैं। यह कंपाइल-टाइम सुरक्षा सुनिश्चित करता है।
```kotlin
@Serializable sealed interface NutritionRoute {
    @Serializable data object Home : NutritionRoute
    @Serializable data class MealDetails(val mealId: String) : NutritionRoute
}
```

### B. Route Args Policy
रूट्स में केवल **Stable IDs** (जैसे `mealId: String`) पास करें। पूरी डेटा ऑब्जेक्ट (जैसे `meal: Meal`) पास न करें। ViewModel में ID के ज़रिए डेटा लोड करें।

### C. NavController Ownership
Screens को कभी भी `NavController` नहीं मिलना चाहिए। Screens को हमेशा **Lambdas** (`onBack`, `onMealClick`) दें। नेविगेशन की वायरिंग हमेशा `Graphs` में होनी चाहिए।

---

## 3. Navigation Hierarchy (नेविगेशन लेयर्स)

### Layer 1: Root NavHost (`AppNavHost.kt`)
ऐप का सबसे बाहरी नेविगेटर जो बड़े फ्लो तय करता है।
- **Root Graph:** Splash, Welcome, Auth, Onboarding, और Full-Screen Views (Camera, Video Player)।

### Layer 2: Main Shell (`MainScreen.kt`)
उन स्क्रीन्स के लिए जो **BottomBar** शेयर करती हैं।
- इसमें अपना एक `NavHost` होगा।
- **Tab Switching Logic:** टैब स्विच करते समय `saveState = true` और `restoreState = true` का उपयोग करें ताकि यूजर की प्रोग्रेस बनी रहे।
- **WorkoutSubTab Derivation:** `selectedWorkoutTab` अलग state नहीं, `NavBackStack` से derive होता है — single source of truth।

```kotlin
// MainScreen.kt में WorkoutSubTab derive करने का pattern:
val selectedWorkoutTab = when {
    currentDestination?.hierarchy?.any { it.hasRoute<WorkoutScreen.Explore>() } == true -> WorkoutSubTab.Explore
    currentDestination?.hierarchy?.any { it.hasRoute<WorkoutScreen.Routines>() } == true -> WorkoutSubTab.Routines
    else -> WorkoutSubTab.History
}
```

---

## 4. UI Chrome Policy (TopBar & BottomNav & SecondaryNav)

- **BottomBar:** `TnyxShell` विऺ `MainScreen.kt` में centralized रहेगा।
- **Active State:** टैब एक्टिव दिखाने के लिए `navController.currentBackStackEntry?.destination?.hierarchy` का उपयोग करें (ताकि चाइल्ड स्क्रीन्स पर भी पैरेंट टैब एक्टिव दिखे)।
- **TopBar:** हर **Screen-level** पर अलग `Scaffold` और `TopBar` रखें।
    - **फायदा:** `NutritionHome` में 'Search' चाहिए, जबकि `MealDetails` में 'Back + Edit'। स्क्रीन-लेवल पर इसे कंट्रोल करना क्लीन होता है।
- **WorkoutSecondaryNav:** सिर्फ Workout tab पर `TnyxShell` द्वारा show/hide होता है। इसका navigation सीधे `WorkoutNavGraph` में जाता है। Screens को NavController नहीं मिलता — `MainScreen` wiring करता है।

---

## 5. Scaling Tips (स्केलेबिलिटी के लिए सुझाव)

1. **Nested Navigation:** हर फीचर को `NavGraphBuilder.navigation()` का उपयोग करके अपना ग्राफ खुद मैनेज करना चाहिए।
2. **Deep Linking:** टाइप-सेफ रूट्स के साथ डीप लिंकिंग सेटअप करना आसान है। इन्हें `deeplink/` पैकेज में रखें।
3. **Internal vs Public:** `TnyxTheme` की तरह नेविगेशन के लिए भी `NavigationActions` का एक सेंट्रलाइज्ड हेल्पर रखें।

---

## 6. Workout Sub-Navigation Pattern

Workout tab में secondary nav bar (History | Explore | Routines) एक अलग नेविगेशन layer provide करता है।

```kotlin
// WorkoutSubTabSelected action का MainScreen में handling:
is ShellAction.WorkoutSubTabSelected -> {
    val workoutRoute = when (action.tab) {
        WorkoutSubTab.History  -> WorkoutScreen.History
        WorkoutSubTab.Explore  -> WorkoutScreen.Explore
        WorkoutSubTab.Routines -> WorkoutScreen.Routines
    }
    mainNavController.navigate(workoutRoute) {
        popUpTo<MainRoute.WorkoutGraph> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
```

Rules:

- `popUpTo<MainRoute.WorkoutGraph>`: WorkoutGraph root तक pop करता है ताकि back stack साफ रहे।
- `saveState = true` + `restoreState = true`: tab switch पर scroll position और state बना रहता है।
- `launchSingleTop = true`: duplicate destinations avoid होते हैं।
- Shell को NavController नहीं देना — `MainScreen` विपरीत action से navigate करता है।

---

**CTO Note:**
TNYX navigation को feature-owned graphs, type-safe routes, और clear shell boundaries पर रखा गया है। Root graph app state handle करता है, Main graph persistent app chrome handle करता है, और feature graphs अपनी screens own करते हैं। WorkoutNavGraph जैसे feature graphs secondary nav की destinations स्वयं own करते हैं — shell सिर्फ उन्हें trigger करता है। इससे 60-80 screens के बाद भी `AppNavHost` छोटा रहता है, route contracts compile-time safe रहते हैं, और teams Nutrition, Workout, Coaching, Profile जैसे modules पर independently काम कर सकती हैं।
