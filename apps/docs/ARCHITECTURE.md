# Tnyx Android Architecture Guide

यह document Android app के current checked-in source के आधार पर architecture, module ownership और coding rules define करता है।

Last audited against:

- `:shared` (pure Kotlin domain models, repository interfaces)
- `:core` (theme, ui/components, ui/shell, legal, global routes)
- `:features:onboarding` (splash, welcome)
- `:features:workout` (workout navigation & logic)
- `:features:nutrition` (nutrition diary & logic)
- `:features:auth` (authentication & login logic)
- `:features:profile` (Fitness Hub + Account Launcher skeleton)
- `:features:settings` (App Config skeleton)
- `:features:progress` (Progress tab skeleton)
- `:wear` (Wear OS watch app module)
- `:app` (Main entry point, DI graph, AppNavHost)

Last updated: 2026-06-27

Canonical ownership source: [PROFILE_SETTINGS_GUIDE.md](PROFILE_SETTINGS_GUIDE.md). Current source may not yet contain every future module, but new implementation must follow that ownership freeze.

---

## 0. Module Dependency Graph

```text
       [ :app ] (Main Entry & Glue)
          |
    --------------------------
    |           |            |
[ :features ] [ :core ]      |
    |           |            |
    |     [ :shared ] <-------
    |___________|
```

---

## 0A. Canonical Information Architecture And Ownership Freeze

TNYX is expected to scale beyond 100 screens. The canonical ownership model is defined in `PROFILE_SETTINGS_GUIDE.md`; this section summarizes the architecture rules that affect all Android modules.

Primary bottom navigation is frozen as:

```text
Home
Workout
Nutrition
Coach
Progress
```

Profile is not a bottom-nav tab. Profile is launched from the avatar and acts as **Fitness Hub + Account Launcher**. It owns user identity summary, current plan summary, personal information UI, and launcher cards only.

Settings is not a bottom-nav tab. Settings is launched from the gear icon and owns app configuration, account controls, notification preferences, units, theme, language, export data, and legal entry points.

Ownership freeze:

| Area | Owner |
|---|---|
| Journey, Progress Photos, Measurements, Weight, Achievements, Progress Analytics | `:features:progress` |
| Nutrition Targets, Calories, Macros, Water Goal, Glass Size | `:features:nutrition` |
| Workout Settings, Rest Timer, Plate Calculator, Graph Settings, RPE, Previous Workout Values | `:features:workout` |
| Health Connections, Health Connect, Samsung Health, Garmin, Fitbit, Apple Health | future `:features:health` |
| Sleep, HRV, Readiness, Recovery Score | future `:features:recovery` |
| Subscription, plan state, entitlement checks, feature access | future `:features:billing` / Entitlement |
| Rewards, badges, gamification | future `:features:rewards` |
| Resources, education, guides | future `:features:learn` |
| Personal Information source of truth | `:features:profile` domain/repository |

Rule: Profile and Settings can launch other features through public route contracts, but they must not own another feature's business logic, repository calls, validation, or persistence.

---
## 0. Shared Module (`:shared`) — ⭐ Start Here

`:shared` एक **pure Kotlin JVM module** है जो Phone App (`:app`) और Watch App (`:wear`) दोनों में समान रहता है। यहां का code किसी भी platform पर चल सकता है।


### Structure

```text
shared/
├── build.gradle.kts          ← kotlin("jvm") plugin — Android dependency नहीं
└── src/main/java/com/tnyx/shared/
    ├── workout/
    │   └── domain/
    │       ├── model/
    │       │   ├── SetType.kt         ← enum: NORMAL, WARMUP, DROP_SET, FAILURE, SUPERSET
    │       │   ├── WorkoutSet.kt      ← logged set data (weight, reps, type)
    │       │   └── WorkoutSession.kt  ← session + routine models
    │       └── repository/
    │           └── WorkoutRepository.kt ← interface (implemented in :app and :wearapp)
    └── nutrition/
        └── domain/                    ← (future) NutritionLog, FoodEntry, NutritionRepository
```

Canonical future domain contract slots are reserved for stable pure Kotlin contracts when the implementation needs phone/watch/future-iOS reuse:

```text
shared/src/main/java/com/tnyx/shared/
├── profile/domain/
├── workout/domain/
├── nutrition/domain/
├── progress/domain/
├── health/domain/
├── recovery/domain/
├── billing/domain/
├── coach/domain/
├── rewards/domain/
└── learn/domain/
```

Do not create these folders just to satisfy a tree. Add them when the first real model, repository interface, or use case needs to be shared across app surfaces.

### Dependency Graph

```
         :shared
        /        \
    :app          :wear
 (Phone)          :watch
```

> `:shared` किसी पर depend नहीं करता — यह सबसे base layer है।

### क्या जाएगा `:shared` में

| हाँ डालो | नहीं डालो |
|---|---|
| Domain Models (`data class`) | Compose UI code |
| Enums (`SetType`, `MealType`) | `androidx.*` imports |
| Repository interfaces | Hilt/DI annotations |
| UseCases (pure functions) | `Context`, `Activity` |
| `@Serializable` annotations | Room `@Entity`, `@Dao` |
| `Flow`, `StateFlow` | Android-only APIs |

### Coding Rules for `:shared`

1. **`androidx.*` import कभी नहीं** — कोई भी Android-specific import `:shared` को break कर देगा
2. **`@Immutable` नहीं** — यह Compose annotation है, pure `data class` काफी है
3. **Pure Kotlin only** — `kotlinx.coroutines`, `kotlinx.serialization` allowed हैं
4. **Repository interface यहां, implementation `:app` में** — Hilt `@Provides` `:app` module में होगा
5. **Phone + Watch दोनों सोचकर design करो** — अगर Watch पर जाना है तो model simple rakhna होगा

### `:app` में इसे kaise use karen

```kotlin
// app/build.gradle.kts (and wear/build.gradle.kts)
dependencies {
    implementation(project(":shared"))
}

// Phone app का ViewModel — shared model use karta hai
import com.tnyx.shared.workout.domain.model.WorkoutSet
import com.tnyx.shared.workout.domain.model.SetType

class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository  // ← :shared interface
) : ViewModel()
```

---

## 1. Core Module Structure (`:core`)

`core` app का shared design-system और reusable UI layer है। इसे किसी feature पर depend नहीं करना चाहिए। Features `core` को use कर सकते हैं, लेकिन `core` को `features/*` से import नहीं करना चाहिए।

Current structure:

```text
core/src/main/java/com/tnyx/core
├── theme
│   ├── TnyxTheme.kt
│   ├── TnyxThemeProvider.kt
│   ├── TnyxThemeConfig.kt
│   ├── TnyxThemeMode.kt
│   ├── locals
│   └── tokens
│       ├── components
│       ├── domain
│       ├── effects
│       ├── foundation
│       ├── semantic
│       └── typography
└── ui
    ├── components
    │   ├── buttons
    │   ├── calendar
    │   ├── cards
    │   ├── inputs
    │   ├── layouts
    │   ├── navigation
    │   └── sheets
    └── shell                    ← App Shell layer
        └── presentation
            ├── shell            # TnyxShell.kt
            ├── widgets          # MainBottomNav, MainTopBar, WorkoutSecondaryNav
            ├── state            # ShellUiState, ShellTab, WorkoutSubTab
            ├── action           # ShellAction
            └── view_model       # ShellViewModel
```

---

## 2. Theme System (`core/theme`)

Theme system app-wide design tokens, semantic colors, typography, spacing, motion, shapes और component-level specs provide करता है। Compose screens और reusable components को raw values hardcode करने के बजाय `TnyxTheme` accessor use करना चाहिए।

### A. Theme Entry Points

#### `TnyxTheme.kt`

`TnyxTheme` design system का public accessor है। Current exposed accessors:

- `TnyxTheme.colors`
- `TnyxTheme.dimens`
- `TnyxTheme.insets`
- `TnyxTheme.elevation`
- `TnyxTheme.typography`
- `TnyxTheme.textStyles`
- `TnyxTheme.motion`
- `TnyxTheme.shapes`
- `TnyxTheme.gradients`
- `TnyxTheme.shadows`
- `TnyxTheme.components`

Rule: App UI को design tokens consume करने के लिए direct token files import करने के बजाय normally `TnyxTheme.*` use करना चाहिए। Exception only तब हो जब token definition layer के अंदर काम हो रहा हो।

#### `TnyxThemeProvider.kt`

`TnyxThemeProvider` app theme का root provider है। `MainActivity` में `setContent` के अंदर पूरी app को wrap करता है।

Responsibilities:

- `TnyxThemeConfig.mode` के आधार पर palette choose करना।
- `Light`, `Dark`, `Oled`, और `System` theme modes support करना।
- सभी `LocalTnyx*` CompositionLocals provide करना।
- Component tokens को selected palette के हिसाब से derive करना।
- Compose `MaterialTheme` को full Tnyx-backed `ColorScheme`, shapes और typography से bridge करना।
- `highContrast`, `reducedMotion`, और supported devices पर `useDynamicColor` behavior apply करना।

Current behavior:

- `TnyxThemeMode.Light` -> `TnyxLightColors`
- `TnyxThemeMode.Dark` -> `TnyxDarkColors`
- `TnyxThemeMode.Oled` -> `TnyxOledColors`
- `TnyxThemeMode.System` -> system dark mode के हिसाब से dark/light palette
- `TnyxThemeMode.Oled` में dynamic color disabled रहता है ताकि pure-black behavior stable रहे।

#### `TnyxThemeConfig.kt`

Theme behavior control करने वाली config class:

- `mode: TnyxThemeMode = TnyxThemeMode.System`
- `useDynamicColor: Boolean = false`
- `highContrast: Boolean = false`
- `reducedMotion: Boolean = false`

Important: Current provider में `mode`, `useDynamicColor`, `highContrast`, और `reducedMotion` सभी behavior drive करते हैं। `useDynamicColor` Android 12+ पर Material dynamic scheme apply करता है; `highContrast` text/surface contrast strengthen करता है; `reducedMotion` animation durations को zero करता है।

#### `TnyxThemeMode.kt`

Supported modes:

- `Light`
- `Dark`
- `Oled`
- `System`

---

## 3. Theme Tokens (`core/theme/tokens`)

Actual token hierarchy `core/theme/tokens/...` के नीचे है। Documentation या imports में `core/theme/foundation` जैसा path use नहीं करना चाहिए।

### A. Foundation Tokens (`tokens/foundation`)

Foundation tokens raw primitives और layout scales define करते हैं।

Current files:

- `TnyxPalette.kt`: raw color palette.
- `TnyxDimens.kt`: spacing, icon sizes, radius, border और layout dimensions.
- `TnyxShapes.kt`: app-level shape definitions और Material shape bridge.
- `TnyxInsets.kt`: screen/system layout insets जैसे horizontal padding और bottom nav padding.
- `TnyxElevation.kt`: elevation scale.
- `TnyxMotion.kt`: animation/motion durations और timing scale.

Rule: UI code में `8.dp`, `16.dp`, arbitrary radius, arbitrary icon size जैसे raw values avoid करें। Reusable UI में `TnyxTheme.dimens`, `TnyxTheme.insets`, `TnyxTheme.shapes`, `TnyxTheme.motion` prefer करें।

### B. Semantic Color Tokens (`tokens/semantic`)

Semantic tokens raw palette को UI meaning देते हैं।

Current files:

- `TnyxColors.kt`: semantic color data class.
- `TnyxLightColors.kt`: light palette mapping.
- `TnyxDarkColors.kt`: dark palette mapping.
- `TnyxOledColors.kt`: OLED-specific dark palette override.

Rule: Screens/components को `TnyxPalette` directly use नहीं करना चाहिए। Normal UI code में `TnyxTheme.colors.primary`, `background`, `surface`, `textPrimary`, `textSecondary`, `textMuted`, `error` जैसे semantic colors use करें।

### C. Typography Tokens (`tokens/typography`)

Typography app की text system define करती है।

Current files:

- `TnyxTypography.kt`: Material `Typography` mapping और font family setup.
- `TnyxTextStyles.kt`: app-specific text styles.

Rule: UI में text size/weight hardcode करने के बजाय `TnyxTheme.typography.*` और approved text styles use करें।

### D. Domain Tokens (`tokens/domain`)

Domain tokens health/fitness/nutrition जैसी app-specific meanings को represent करते हैं।

Current files:

- `NutritionColors.kt`: protein, carbs, fat जैसे nutrition-specific colors.

Rule: Domain colors only उस feature/domain में use करें जहां semantic meaning clear हो। General UI states के लिए `TnyxTheme.colors` use करें।

### E. Component Tokens (`tokens/components`)

Component tokens reusable components की design specs define करते हैं।

Current files:

- `ButtonTokens.kt`: button height, padding, colors aur text style defaults.
- `InputTokens.kt`: text field colors, shape, padding aur indicator specs.
- `CardTokens.kt`: card container, content, border aur elevation specs.
- `SheetTokens.kt`: bottom sheet container, scrim, shape, padding aur divider specs.
- `NavigationTokens.kt`: bottom navigation aur workout secondary navigation dimensions/animation specs.
- `CalendarTokens.kt`: weekly calendar height, day cell width, indicator size, icon size, divider alpha, label alpha aur future-date alpha specs.
- `TnyxComponentTokens.kt`: consolidated component token container for button, input, card, sheet, navigation aur calendar tokens.

Rule: `core/ui/components/*` components को अपने token specs `TnyxTheme.components.*` से लेना चाहिए। Feature screens को component internals duplicate नहीं करने चाहिए।

### F. Effects Tokens (`tokens/effects`)

Visual effects premium UI surface के लिए हैं।

Current files:

- `TnyxGradients.kt`: app gradients.
- `TnyxShadows.kt`: shadow data class, shadow presets और `Modifier.tnyxShadow`.
- `TnyxBlur.kt`: blur constants.

Rule: Shadows/gradients reusable UI में centralized रहें। Random alpha/elevation/shadow blocks feature screens में duplicate न करें।

---

## 4. Composition Locals (`core/theme/locals`)

`locals` folder theme values को Compose tree में supply करने वाली `CompositionLocal` keys रखता है।

Current locals:

- `LocalTnyxColors`
- `LocalTnyxDimens`
- `LocalTnyxInsets`
- `LocalTnyxElevation`
- `LocalTnyxTypography`
- `LocalTnyxMotion`
- `LocalTnyxShapes`
- `LocalTnyxGradients`
- `LocalTnyxShadows`
- `LocalTnyxComponentTokens`

Rule: App code को locals directly read करने की जरूरत normally नहीं होनी चाहिए। Public access `TnyxTheme.*` से हो।

---

## 5. Core UI Components (`core/ui/components`)

`core/ui/components` app के reusable Compose building blocks हैं। इन components में business logic, repository calls, network calls, database calls या feature-specific mutation logic नहीं होना चाहिए।

### A. Buttons (`components/buttons`)

Current file:

- `TnyxButton.kt`

Current public components:

- `TnyxPrimaryButton`
- `TnyxSecondaryButton`

Expected behavior:

- Button visuals `TnyxTheme.components.button` से driven हों।
- Button content slots जैसे `leading` और `trailing` composable रहें।
- Business action external callback से pass हो।

### B. Cards (`components/cards`)

Current file:

- `TnyxCard.kt`

Current public APIs:

- `TnyxCard`
- `TnyxCardVariant`

Expected behavior:

- Card tokens `TnyxTheme.components.card` से use हों।
- Surface/glass/elevated variants reusable रहें।
- Feature-specific content caller provide करे।

### C. Inputs (`components/inputs`)

Current file:

- `TnyxTextField.kt`

Current public component:

- `TnyxTextField`

Expected behavior:

- Input visuals `TnyxTheme.components.input` और semantic error colors से driven हों।
- Validation result caller से आए; component खुद business validation owner नहीं है।

### D. Calendar (`components/calendar`)

Current file:

- `TnyxWeeklyCalendar.kt`

Current public component:

- `TnyxWeeklyCalendar`

Expected behavior:

- Weekly date selection UI provide karta hai aur selected date caller-owned rahta hai.
- `selectedDate`, `onDateSelected`, `allowFutureDates`, `today`, aur `locale` caller-controlled hain.
- Calendar ab `TnyxTheme.components.calendar` se visual dimensions consume karta hai. Height, side section width, day width, indicator size, icon size aur alpha values tokenized hain.
- Week rows horizontally swipeable hain through `HorizontalPager`; implementation pseudo-infinite page range use karta hai taaki user past/future weeks browse kar sake.
- Left side month label visible week ke basis par render hota hai. Same side par calendar icon tap karne se `today` select hota hai.
- Current week detect hota hai; current week dekhte samay side affordance muted rahta hai, current week se door hone par accent color se "jump to today" affordance stronger dikhta hai.
- Sunday dates semantic error color family se highlighted hain taaki week scan karte samay rest/off-day context visually clear rahe.
- Future date disabling UI-level behavior hai; disabled future dates alpha-reduced hain aur click disabled hai. Domain validation fir bhi feature/ViewModel layer mein enforce hona chahiye.
- `CalendarDayItem` private implementation detail hai; feature code ko direct day cell compose nahi karna chahiye.

### E. Layouts (`components/layouts`)

Current files:

- `TnyxDynamicHeader.kt`
- `TnyxScreenHeader.kt`

Current public components:

- `TnyxDynamicHeader`
- `TnyxScreenHeader`

Expected behavior:

- Gradient header/background overlay provide karta hai.
- Color/height caller override kar sakta hai.
- Feature-specific screen state is component mein nahi aana chahiye.
- `TnyxScreenHeader` compact screen title row provide karta hai. It supports uppercase title rendering, optional action icon, fixed height override aur alpha fade for scroll-synchronized headers.
- `TnyxScreenHeader` action behavior callback-owned hai. Component keval visual shell hai; notification/search/edit jaise actions feature Route/ViewModel layer se wire hone chahiye.
- `TnyxScreenHeader` currently direct spacing values use karta hai; future cleanup mein inhein layout/header tokens mein move kiya ja sakta hai agar multiple screens adopt karein.

### F. Navigation (`components/navigation`)

Current file:

- `TnyxTabSwitcher.kt`

Current public APIs:

- `TnyxTabItem<T>`
- `TnyxTabSwitcher<T>`

Expected behavior:

- Generic tab switching UI provide करता है।
- Selected value और selection callback caller-owned रहें।
- Navigation decision या route mutation feature Route/ViewModel layer में रहे।

### G. Sheets (`components/sheets`)

Current file:

- `TnyxModalBottomSheet.kt`

Current public component:

- `TnyxModalBottomSheet`

Expected behavior:

- Feature screens visibility/state own करें; sheet component केवल visual shell provide करे।
- Bottom sheets language selector, filters, editors, pickers और action menus जैसे flows के लिए reusable रहें।
- Sheet content slots dumb UI रहें; ViewModel/repository/API logic sheet component में नहीं आना चाहिए।

### H. Legal Module (`core/legal`)

Flutter legal card design से inspired यह module app-wide legal documents (Terms, Privacy Policy) के लिए floating card/dialog shell provide करता है। अभी priority card shell visual है; content/WebView integration intentionally disabled रहेगा जब तक card design final न हो।

Current files:

- `presentation/route/LegalRoute.kt`
- `presentation/screen/LegalScreen.kt`
- `presentation/view_model/LegalViewModel.kt`
- `presentation/state/LegalState.kt`
- `presentation/action/LegalAction.kt`
- `presentation/widgets/LegalCloseButton.kt`
- `presentation/widgets/LegalPlaceholderContent.kt`
- `presentation/widgets/LegalInfoBox.kt`

Expected behavior:

- Legal module app-wide dialog/card shell hai. Yeh Terms/Privacy jaise legal surfaces ko route/screen/view-model/state/action/widgets boundaries mein rakhta hai.
- Legal content WebView ya final legal document rendering abhi source truth nahi hai; placeholder/card shell behavior hi current boundary hai.
- Legal widgets visual-only rahein. Navigation, dismissal aur content source decisions route/view-model layer mein rahein.

---

## 5A. App Shell (`core/ui/shell`)

`core/ui/shell` app chrome ka shared runtime layer hai. Yeh bottom navigation, top bar, workout secondary navigation aur shell-level state/actions ko own karta hai. Feature screens shell ko mutate nahi karte; woh route/action callbacks ke through navigation request karte hain.

### A. `MainBottomNav.kt`

Current behavior:

- `MainBottomNav` canonical 5 primary tabs render karta hai: `Home`, `Workout`, `Nutrition`, `Coach`, aur `Progress`.
- Non-Coach tabs private `BottomNavTab` model se driven hain jismein `selectedIconRes` aur `unselectedIconRes` dono resource ids rahte hain.
- Selected/unselected icons `Crossfade` se swap hote hain taaki active state jumpy na lage.
- Regular tab color `animateColorAsState` se transition karta hai: selected state `textPrimary`, unselected state `textSecondary`.
- Regular tab icon container `animateFloatAsState` spring scale use karta hai; selected tab halka scale-up hota hai.
- Label selected state mein medium weight use karta hai, unselected state normal weight rakhta hai.
- `Coach` tab special circular visual path use कर सकता है और normal selected/unselected drawable pair पर depend नहीं करेगा अगर center-action design रखा जाए।
- Bottom nav dimensions `TnyxTheme.components.navigation` se aate hain, including nav height, icon size, ripple size, AI icon size aur divider alpha.
- Bottom nav only tab-selection callback emit karta hai. Actual graph navigation shell owner (`MainScreen`) mein hona chahiye, component mein nahi.

Resource contract:

- Har regular tab ke liye filled aur outlined drawable pair chahiye. Current non-Coach pairs:
  - `ic_nav_home_filled` / `ic_nav_home_outlined`
  - `ic_nav_nutrition_filled` / `ic_nav_nutrition_outlined`
  - `ic_nav_workout_filled` / `ic_nav_workout_outlined`
  - `ic_nav_progress_filled` / `ic_nav_progress_outlined`
- Filled/outlined icon names stable rakhein taaki shell refactor ke bina resource replacement possible rahe.
- Icon assets common nav resources hain, isliye inhein feature-specific folder mein na rakhein.

### B. `WorkoutSecondaryNav.kt`

- Keval Workout tab active hone par visible rahta hai.
- Tab switch (Workout tab select): `AnimatedVisibility` se slide-up enter (400ms), slide-down exit (280ms).
- Scroll behavior `TnyxShell` mein `NestedScrollConnection` se control hota hai.
- Secondary nav selection `WorkoutNavGraph` mein navigation drive karta hai; shell state nahi badalta.
- `selectedWorkoutTab` `NavBackStack` se derive hota hai (`MainScreen` mein).

### C. Scroll Behavior Contract

```text
User scrolls UP > 200dp  ->  WorkoutSecondaryNav slides DOWN (hide, 320ms)
Scroll stops (1.5s)      ->  WorkoutSecondaryNav slides UP  (restore, 320ms)
User scrolls DOWN        ->  WorkoutSecondaryNav slides UP  (immediate)
Workout tab deselected   ->  WorkoutSecondaryNav slides DOWN (exit, 280ms)
```

Implementation: `animateFloatAsState` + `graphicsLayer { translationY }` for scroll, `AnimatedVisibility(slideInVertically/slideOutVertically)` for tab switch.

Implementation rules (scroll state management):
- `isWorkoutTab` को `remember { derivedStateOf { ... } }` से wrap करें — plain `val` नहीं। `derivedStateOf` scroll thread पर safe snapshot read देता है और unnecessary recompositions avoid करता है।
- Auto-restore job को `mutableStateOf<Job?>` में नहीं रखें — `Job` non-stable type है। `var restoreJob: Job? by remember { mutableStateOf(null) }` delegate pattern use करें।
- `NestedScrollConnection` को `remember` से बनाएं, `remember(coroutineScope)` से नहीं — `coroutineScope` stable है और closure में capture करना sufficient है। Key पर रखने से mid-gesture callback loss का risk रहता है।

### D. ShellState.kt

Current enums:

- `ShellTab`: `Home`, `Workout`, `Nutrition`, `Coach`, `Progress`
- `WorkoutSubTab`: `History`, `Explore`, `Routines`
- `ShellPlanTier`: `Free`, `Plus`, `Premium`

`ShellUiState` fields:

- `selectedTab: ShellTab`
- `isBottomNavVisible: Boolean`
- `appBarOpacity: Float`
- `planTier: ShellPlanTier`
- `selectedWorkoutTab: WorkoutSubTab`

### E. ShellAction.kt

Current actions:

- `TabSelected(tab: ShellTab)`: bottom nav tab selection।
- `ScrollChanged(offset: Float)`: scroll offset change।
- `PremiumClicked`: premium upgrade trigger।
- `StreakClicked`: streak details trigger।
- `ProfileClicked`: avatar से `ProfileGraph` launch trigger। Profile launcher है, business domain नहीं।
- `WorkoutSubTabSelected(tab: WorkoutSubTab)`: workout secondary nav tab selection → `MainScreen` में `NavController.navigate()` call होता है।

Rule: Shell actions business logic नहीं रखते। Navigation decisions `MainScreen` में होते हैं।

---

---

## 5B. Chrome Policy

Every destination must declare one chrome policy. Shell must apply policy from route metadata, not from hardcoded feature guesses.

Allowed policies:

| Policy | Usage |
|---|---|
| `MainChrome` | Main tab screens with bottom nav and normal shell. |
| `NoBottomBar` | Detail/edit screens inside app where bottom nav hidden रहे। |
| `FullScreen` | Auth, onboarding, camera, media viewer, active workout session. |
| `BottomSheet` | Sheet-style transient destination. |
| `Dialog` | Confirmation, warning, legal, permission explanation. |

MainShell allowed responsibilities:
- Top bar.
- Bottom nav.
- Shell-level spacing/insets.
- Route-derived selected tab.
- Chrome policy application.

MainShell must not contain:
- Nutrition calculations.
- Workout save/update logic.
- Progress analytics.
- Subscription entitlement decisions.
- Health connection logic.
- Feature repository calls.

---

## 6. Feature Module Pattern (`:features:*`)

हर feature अब एक अलग Gradle module है और उसे Clean Architecture-style presentation structure follow करना चाहिए। Folder depth feature की complexity के हिसाब से decide होगी।

For scale: 100+ screens होने पर preferred unit `screen/workflow folder` है, global `screen`, `state`, `action`, `view_model` folders नहीं। हर screen/workflow अपनी `Route`, `Screen`, `ViewModel`, `Contract` और private `widgets` साथ रखेगा।

### A. Small Feature Pattern

`splash`, `welcome`, simple onboarding step जैसे छोटे features के लिए flat presentation structure ठीक है:

```text
features/<feature_name>/presentation
├── route
├── screen
├── view_model
├── state
├── action
└── widgets
```

### B. Large Feature / Multi-Screen Pattern

`nutrition`, `workout`, `recovery`, `coach` जैसे बड़े features में tab, sub-flow, editor, detail screen और section-specific widgets होंगे। ऐसे features को vertical workflow folders में split करें:

```text
features/<feature_name>
├── navigation
│   ├── <Feature>NavGraph.kt
│   └── <Feature>Destination.kt
└── presentation
    ├── home
    │   ├── <Feature>HomeRoute.kt
    │   ├── <Feature>HomeScreen.kt
    │   ├── <Feature>HomeViewModel.kt
    │   ├── <Feature>HomeContract.kt
    │   └── widgets
    ├── <workflow_or_screen_group>
    │   ├── <Workflow>Route.kt
    │   ├── <Workflow>Screen.kt
    │   ├── <Workflow>ViewModel.kt
    │   ├── <Workflow>Contract.kt
    │   └── widgets
    └── shared
        └── widgets
```

`shared/widgets` optional है। इसे only feature-level reused widgets के लिए use करें। किसी section के private widgets को `shared/widgets` में move न करें।

Example for `nutrition`:

```text
features/nutrition
├── navigation
│   ├── NutritionNavGraph.kt
│   └── NutritionDestination.kt
└── presentation
    ├── home
    │   ├── NutritionHomeRoute.kt
    │   ├── NutritionHomeScreen.kt
    │   ├── NutritionHomeViewModel.kt
    │   ├── NutritionHomeContract.kt
    │   └── widgets
    ├── diet_plan
    │   ├── DietPlanRoute.kt
    │   ├── DietPlanScreen.kt
    │   ├── DietPlanViewModel.kt
    │   ├── DietPlanContract.kt
    │   └── widgets
    ├── meal_diary
    │   ├── MealDiaryRoute.kt
    │   ├── MealDiaryScreen.kt
    │   ├── MealDiaryViewModel.kt
    │   ├── MealDiaryContract.kt
    │   └── widgets
    ├── recipe
    │   ├── list
    │   │   └── widgets
    │   ├── detail
    │   │   └── widgets
    │   ├── editor
    │   │   └── widgets
    │   └── widgets
    ├── meal_editor
    │   ├── MealEditorRoute.kt
    │   ├── MealEditorScreen.kt
    │   ├── MealEditorViewModel.kt
    │   ├── MealEditorContract.kt
    │   └── widgets
    └── shared
        └── widgets
```

Nutrition rule: `home/widgets`, `diet_plan/widgets`, `meal_diary/widgets`, `recipe/widgets`, और `meal_editor/widgets` अपने section के private widgets रखेंगे। `presentation/shared/widgets` में only वही widgets आएंगे जो nutrition के multiple sections में reused हों, जैसे macro summary row, nutrition score chip, calorie ring या common date selector wrapper.

Example for `workout`:

```text
features/workout
├── navigation
│   ├── WorkoutNavGraph.kt
│   └── WorkoutDestination.kt
└── presentation
    ├── home
    │   └── widgets
    ├── workout_plan
    │   └── widgets
    ├── exercise_library
    │   ├── list
    │   │   └── widgets
    │   ├── detail
    │   │   └── widgets
    │   └── widgets
    ├── session
    │   └── widgets
    ├── workout_editor
    │   └── widgets
    ├── history
    │   └── widgets
    └── shared
        └── widgets
```

Workout rule: section-specific widgets अपने workflow folder में रहेंगे। `presentation/shared/widgets` केवल truly reused workout UI रखेगा, जैसे exercise thumbnail, set summary row, rest timer chip या workout intensity badge.

### C. Profile / Settings / Progress Ownership Pattern

Profile is a launcher surface, not a business domain. Its folder stays intentionally small:

```text
features/profile
├── navigation
│   ├── ProfileNavGraph.kt
│   └── ProfileRoutes.kt
└── presentation
    ├── home
    ├── personal_info
    └── shared
        └── widgets
```

Settings owns app configuration only:

```text
features/settings
├── navigation
│   ├── SettingsNavGraph.kt
│   └── SettingsRoutes.kt
└── presentation
    ├── home
    ├── app_preferences
    ├── notifications
    ├── units
    ├── account
    ├── export_data
    └── about
```

Progress owns long-term user progress:

```text
features/progress
├── navigation
│   ├── ProgressNavGraph.kt
│   └── ProgressRoutes.kt
└── presentation
    ├── home
    ├── journey
    ├── progress_photos
    ├── measurements
    ├── weight
    ├── achievements
    ├── analytics
    └── shared
        └── widgets
```

Rules:
- Profile can launch `ProgressRoute.Journey`, but Progress owns Journey data and repository.
- Profile can launch `ProgressRoute.ProgressPhotos`, but Progress owns photo workflow. Shared media primitives can live in `core/media` later.
- Settings can launch `NutritionRoute.Targets` or `WorkoutRoute.Settings`, but Nutrition/Workout own save logic.
- Subscription business logic belongs to Billing / Entitlement, with Settings as UI entry and Profile current-plan card as optional shortcut.
### C. Nested Screen Groups

अगर किसी workflow में multiple screens हैं, तो उस workflow के अंदर screen-level folders बना सकते हैं। Example:

```text
features/nutrition/presentation/recipe
├── list
│   ├── RecipeListRoute.kt
│   ├── RecipeListScreen.kt
│   ├── RecipeListViewModel.kt
│   ├── RecipeListContract.kt
│   └── widgets
├── detail
│   ├── RecipeDetailRoute.kt
│   ├── RecipeDetailScreen.kt
│   ├── RecipeDetailViewModel.kt
│   ├── RecipeDetailContract.kt
│   └── widgets
└── editor
    ├── RecipeEditorRoute.kt
    ├── RecipeEditorScreen.kt
    ├── RecipeEditorViewModel.kt
    ├── RecipeEditorContract.kt
    └── widgets
```

### D. Required Presentation Roles

- `Route`: navigation boundary, ViewModel collection, effect handling.
- `Screen`: dumb UI. केवल `UiState` render करे और `Action` callbacks emit करे.
- `ViewModel`: state management, action handling, orchestration.
- `Contract`: same screen/workflow के `UiState`, `Action`, और `Effect` को एक जगह रखता है।
- `UiState`: screen render करने वाला immutable state.
- `Action`: user/system events.
- `Effect`: one-off events जैसे navigation, snackbar, permission prompt.
- `widgets`: small composables. यह feature-level, workflow-level या screen-level हो सकता है।

For large features, prefer:

```text
<ScreenName>Contract.kt
├── <ScreenName>UiState
├── <ScreenName>Action
└── <ScreenName>Effect
```

Separate `state/` और `action/` folders small existing features में acceptable हैं, लेकिन large features में ये folders जल्दी overcrowded हो जाते हैं।

### E. Dumb UI Contract

यह contract हर screen पर लागू होगा:

- `Route` ही `hiltViewModel()`, state collection और effect collection करेगा।
- `Route` state collect करने के लिए Android screen पर `collectAsStateWithLifecycle()` prefer करेगा।
- `Route` one-off effects जैसे navigation, snackbar, permission prompt और dialog routing handle करेगा।
- `Screen` सिर्फ `state: <Screen>UiState` और `onAction: (<Screen>Action) -> Unit` लेगा।
- `Screen` repository, API, database, DataStore, ViewModel या navigation controller access नहीं करेगा।
- `Screen` में `remember` only visual/transient UI state के लिए allowed है, जैसे animation visibility, pager position, local expand/collapse. Business state हमेशा `UiState` में रहेगा।
- `Screen` user events को `Action` में map कर सकता है, लेकिन business decision नहीं लेगा।
- `widgets` by default explicit callbacks लेंगे, जैसे `onSkipClick`, `onDateSelected`, `onMealClick`.
- `widgets` को normally `<Screen>Action` import नहीं करना चाहिए। Action mapping screen boundary पर रहे।
- Feature-private widget तभी direct `Action` use कर सकता है जब वह सिर्फ उसी screen में locked हो और reuse की कोई संभावना न हो; default फिर भी explicit callbacks है।
- `ViewModel` ही `Action` handle करेगा, `UiState` mutate करेगा और `Effect` emit करेगा।
- `UiState` immutable data holder रहेगा; इसमें callback, `Context`, `NavController`, repository या mutable collection नहीं होगा।

### F. Widget Placement Rules

- Screen-specific widgets उसी screen/workflow के `widgets` folder में रखें।
- Workflow-specific widgets उसी workflow के `widgets` folder में रखें।
- Same feature के multiple workflows/screens में reused widgets only तब `presentation/shared/widgets` में रखें जब reuse real हो।
- `presentation/shared/widgets` को dumping ground न बनाएं; default choice section-level `widgets` होना चाहिए।
- Multiple features में reused widgets `core/ui/components` में promote करें।
- `widgets` folder में ViewModel, repository, API call, database call या mutation logic नहीं होना चाहिए।
- Generic `edit` folder न बनाएं। Clear workflow names use करें: `meal_editor`, `recipe_editor`, `workout_editor`.

Hard rule: Composable screens में mutable business state, repository calls, network calls, database calls या mutation logic नहीं होगा।

---

## 7. Resource Management

Resource ownership Gradle source sets से अलग किया गया है:

- `res-icons/`: icons.
- `res-images/`: large images और illustrations.
- `res-features/`: feature-specific graphics.
- `res/`: strings, values और common resources.

Rule: Feature-specific assets को common `res/` में dump न करें। Shared assets ही common resource layer में रखें।

---

## 8. Coding Golden Rules

1. **No hardcoded design values**: Spacing, radius, colors, typography, motion और shadows के लिए `TnyxTheme.*` use करें।
2. **Core must stay feature-agnostic**: `core` से `features` import नहीं होना चाहिए।
3. **Screens are dumb UI**: Screens state render करें और actions emit करें; business decisions ViewModel/domain layer में रहें।
4. **Reusable UI goes to `core/ui`**: अगर component multiple features में useful है, उसे feature widget से promote करके `core/ui/components` में रखें।
5. **Component tokens first**: Buttons/cards/inputs जैसे reusable components अपने visual rules `TnyxTheme.components` से लें।
6. **Semantic colors first**: App UI में raw palette नहीं, semantic colors use करें।
7. **Type-safe navigation**: Routes के लिए serializable route models/classes prefer करें।
8. **Backticks for Kotlin keywords**: Package names में Kotlin keywords जैसे `in` use करने पर `` `in` `` syntax रखें।
9. **No secrets in clients**: Mobile app में service keys, admin keys, private keys या backend-only secrets expose न करें।
10. **Generated/cache output commit न करें**: `build`, `.gradle`, APK/AAB, `.env`, keystores, private keys और generated cache files repository में नहीं जाने चाहिए।
11. **Feature launchers must stay thin**: Profile, Settings, Home, और Coach दूसरे feature की business logic या repositories own नहीं करेंगे।
12. **Ownership matrix wins**: अगर ambiguity हो, `PROFILE_SETTINGS_GUIDE.md` का ownership matrix follow करें।

---

## 9. Current Documentation Risk

यह file अब current `core/theme`, `core/ui`, और `core/ui/shell` source tree को cover करती है। Future में जब नया token/component/shell widget add हो:

- पहले implementation सही ownership folder में add करें।
- फिर इस architecture guide में folder और public API update करें।
- अगर `TnyxThemeConfig` के reserved flags का behavior implement हो, तो `TnyxThemeProvider` behavior section update करें।
- अगर Profile, Settings, Progress, Billing, Health, Recovery, Rewards, या Learn ownership बदले, तो `PROFILE_SETTINGS_GUIDE.md` first update करें।
- अगर नया Shell widget add हो (जैसे notification badge, mini-player), तो Section 5A update करें।
- अगर Workout feature में नए sub-sections आएं, `WorkoutNavGraph` और Section 5A का scroll contract update करें।

---

## 10. Workout Feature Structure (`:features:workout`)

Workout feature अपना navigation खुद own करता है। Secondary nav bar (shell) सिर्फ navigation trigger करती है; actual screens workout feature में होते हैं।

```text
features/workout
└── navigation
    └── WorkoutNavGraph.kt      # History / Explore / Routines destinations
```

### WorkoutScreen Routes

```kotlin
@Serializable sealed interface WorkoutScreen {
    @Serializable data object History  : WorkoutScreen  // default start
    @Serializable data object Explore  : WorkoutScreen
    @Serializable data object Routines : WorkoutScreen
}
```

Rule: WorkoutNavGraph `MainRoute.WorkoutGraph` को `startDestination = WorkoutScreen.History` के साथ wrap करता है। Secondary nav tab press → `navController.navigate(WorkoutScreen.X)` with `popUpTo<MainRoute.WorkoutGraph> { saveState = true }` + `restoreState = true`।

Future: जब History, Explore, Routines की presentation layer बनेगी:

```text
features/workout
├── navigation
│   └── WorkoutNavGraph.kt
└── presentation
    ├── history
    │   ├── WorkoutHistoryRoute.kt
    │   ├── WorkoutHistoryScreen.kt
    │   ├── WorkoutHistoryViewModel.kt
    │   ├── WorkoutHistoryContract.kt
    │   └── widgets
    ├── explore
    │   └── widgets
    ├── routines
    │   └── widgets
    └── shared
        └── widgets
```

*Maintained as Android architecture source guide for Tnyx.*

**Last updated: 2026-06-27**



