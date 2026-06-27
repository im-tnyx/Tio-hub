# Tnyx Profile & Settings Architecture Guide

यह दस्तावेज़ Tnyx ऐप में **Profile (Fitness Hub + Account Launcher)**, **Settings (App Config)**, और उनसे जुड़े feature ownership rules को परिभाषित करता है। यह guide 100+ screens, Wear OS, future iOS, AI Coach, Health integrations, Recovery, Subscription, और Offline Mode को ध्यान में रखकर बनाया गया canonical architecture reference है।

**Last Updated:** 2026-06-27  
**Status:** Canonical Architecture Reference

---

## 1. Conceptual Split: Fitness Hub vs. App Config

Tnyx में Profile और Settings दो अलग उद्देश्यों के लिए उपयोग होंगे:

| Feature | Concept | Responsibility |
|---|---|---|
| **Profile** | **Fitness Hub + Account Launcher** | User identity summary, current plan summary, shortcuts, and cross-feature launch cards. |
| **Settings** | **App Config** | Technical preferences, account controls, legal surfaces, and app behavior. |

### Profile Is Not A Business Domain

Profile किसी दूसरे feature की business logic own नहीं करेगा। Profile केवल user-facing launcher surface है।

Profile की responsibilities:
- User identity summary.
- Current plan summary.
- Journey card, launcher only.
- Progress Photos card, launcher only.
- Quick Actions.
- Rewards shortcut.
- Resources shortcut.
- Settings shortcut.

Profile कभी भी Nutrition, Workout, Progress, Health, Recovery, Billing, Rewards, या Resources की business logic own नहीं करेगा।

### Settings Is Not A Domain Dump

Settings केवल app-level preferences और account-level controls का entry point है। जो setting किसी specific feature/domain से जुड़ी है, वह उसी feature में रहेगी।

Examples:
- Nutrition Targets Settings में नहीं, Nutrition में रहेंगे।
- Workout Settings Settings में नहीं, Workout में रहेंगे।
- Health Connections Settings में नहीं, Health में रहेंगे।
- Subscription UI Settings से खुल सकता है, लेकिन business logic Billing / Entitlement own करेगा।

---

## 2. Feature Folder Tree

### A. Profile Module (`:features:profile`)

प्रोफ़ाइल मॉड्यूल user identity summary और launcher cards का मालिक है। यह Progress, Nutrition, Workout, Health, Billing, Rewards, और Resources तक type-safe route contracts के जरिए navigate करेगा।

```text
features/profile/
├── navigation/
│   ├── ProfileNavGraph.kt      # Avatar entry and profile-owned destinations
│   └── ProfileRoutes.kt        # Profile public/internal routes
└── presentation/
    ├── home/                   # Fitness Hub + Account Launcher
    ├── personal_info/          # Profile-owned identity data UI
    └── shared/widgets/         # Profile-only reusable widgets
```

Profile में ये folders नहीं होंगे:
- `journey/`
- `progress_photos/`
- `rewards/`
- `resources/`

इनका ownership अलग feature modules में रहेगा।

### B. Settings Module (`:features:settings`)

Settings module app behavior, account controls, और technical preferences को handle करेगा। Settings किसी दूसरे domain की calculation, repository, या business rule own नहीं करेगा।

```text
features/settings/
├── navigation/
│   ├── SettingsNavGraph.kt
│   └── SettingsRoutes.kt
└── presentation/
    ├── home/                   # Gear icon entry
    ├── app_preferences/        # Theme, Language, App behavior
    ├── notifications/          # Notification preferences
    ├── units/                  # Metric/Imperial preferences
    ├── account/                # Account controls, sign out, delete account entry
    ├── export_data/            # Export request UI
    └── about/                  # Terms, Privacy, Version info
```

### C. Progress Module (`:features:progress`)

Progress user transformation और long-term progress data का owner है।

```text
features/progress/
├── navigation/
│   ├── ProgressNavGraph.kt
│   └── ProgressRoutes.kt
└── presentation/
    ├── home/                   # Progress tab landing screen
    ├── journey/                # Journey timeline, milestones
    ├── progress_photos/        # Photo capture, gallery, comparison
    ├── measurements/           # Body measurements
    ├── weight/                 # Weight logs and trends
    ├── achievements/           # Progress achievements
    ├── analytics/              # Progress analytics
    └── shared/widgets/
```

### D. Future Feature Modules

ये modules अभी full implementation के लिए जरूरी नहीं हैं, लेकिन ownership अभी से freeze रहेगी:

```text
features/health/                # Health Connections, Health Connect, Garmin, Fitbit, Apple Health
features/recovery/              # Sleep, HRV, Readiness, Recovery Score
features/billing/               # Subscription UI, plan management, purchase restore
features/rewards/               # Rewards, badges, streak rewards, gamification
features/learn/                 # Resources, education, articles, guides
features/coach/                 # AI Coach chat and coaching experiences
```

---

## 3. Feature Ownership

"Feature Ownership" rule के अनुसार कोई भी feature दूसरे feature की business logic own नहीं करेगा। Cross-feature UI केवल public route contracts या public domain contracts के जरिए integrate होगा।

### Progress Domain (owned by `:features:progress`)

Progress owns:
- Journey.
- Progress Photos.
- Measurements.
- Weight.
- Achievements.
- Progress Analytics.

Profile केवल Progress screens के लिए launcher cards दिखाएगा।

### Nutrition Domain (owned by `:features:nutrition`)

Nutrition owns:
- Nutrition Targets.
- Calories.
- Macros.
- Water Goal.
- Glass Size.

Nutrition does not own:
- Steps.
- Sleep.

Steps Health/Activity side में जाएंगे। Sleep Recovery/Health side में जाएगा।

### Workout Domain (owned by `:features:workout`)

Workout owns:
- Workout Settings.
- Rest Timer.
- Plate Calculator.
- Graph Settings.
- RPE.
- Previous Workout Values.

Workout settings का UI Settings module में duplicate नहीं होगा। Settings या Profile केवल `WorkoutRoute.Settings` पर navigate करेंगे।

### Health Domain (future `:features:health`)

Health owns:
- Health Connections.
- Samsung Health.
- Health Connect.
- Garmin.
- Fitbit.
- Apple Health.

Profile केवल Health Connections shortcut दे सकता है।

### Recovery Domain (future `:features:recovery`)

Recovery owns:
- Sleep.
- HRV.
- Readiness.
- Recovery Score.

Sleep को Nutrition Targets में नहीं रखा जाएगा।

### Billing / Entitlement Domain (future `:features:billing`)

Billing / Entitlement owns subscription business logic:
- Active plan.
- Entitlements.
- Feature access.
- Purchase restore.
- Expiry/grace period.
- Paywall decisions.

UI entry:
- `Settings -> Subscription`

Optional shortcut:
- `Profile -> Current Plan Card`

Subscription state किसी auth/session layer में own नहीं होगा। Auth सिर्फ user identity/session का owner है; feature access Billing / Entitlement से आएगा।

### Personal Information

Personal Information Profile domain का हिस्सा है।

Owner:
- UI Owner: Profile.
- Business Logic Owner: Profile.
- Repository Owner: Profile repository.

Single Source of Truth:
- Onboarding, Profile, और Settings तीनों same repository use करेंगे।
- Onboarding initial data collect कर सकता है, लेकिन final persisted identity data Profile repository से आएगा।
- Settings personal info edit screen खोल सकता है, लेकिन ownership Profile की रहेगी।

---

## 4. Cross-Feature Integration (Linking)

Profile और Settings cross-feature launchers हो सकते हैं, लेकिन business logic owners नहीं।

### Allowed Pattern

```kotlin
navigate(NutritionRoute.Targets)
navigate(WorkoutRoute.Settings)
navigate(ProgressRoute.Journey)
navigate(HealthRoute.Connections)
navigate(BillingRoute.Subscription)
```

### Not Allowed

- Profile directly Nutrition repository call करे।
- Settings Workout settings save करे।
- Nutrition Sleep target calculate करे।
- Auth subscription entitlement own करे।
- Feature internal widgets दूसरे feature में import हों।

Cross-feature navigation हमेशा public route contracts से होगी। Cross-feature data access shared domain contracts, repositories, या use cases से होगा, direct UI dependency से नहीं।

---

## 5. UI Entry Points & Navigation Flow

### Main Information Architecture

Primary bottom navigation freeze:

```text
Home
Workout
Nutrition
Coach
Progress
```

Profile bottom nav tab नहीं होगा। Profile top-right avatar से खुलेगा।

Settings bottom nav tab नहीं होगा। Settings gear icon से खुलेगा।

### Profile Dashboard Structure

Profile screen एक **Fitness Hub + Account Launcher** की तरह काम करेगी:

1. **Header:** User identity summary (Name, avatar, basic identity).
2. **Current Plan:** Current plan summary and optional subscription shortcut.
3. **Progress Launchers:** Journey card and Progress Photos card.
4. **Quick Actions:** Nutrition Targets, Workout Settings, Health Connections.
5. **Shortcuts:** Rewards, Resources, Settings.

Profile cards के click actions:
- **Journey** -> `navigate(ProgressRoute.Journey)`
- **Progress Photos** -> `navigate(ProgressRoute.ProgressPhotos)`
- **Nutrition Targets** -> `navigate(NutritionRoute.Targets)`
- **Workout Settings** -> `navigate(WorkoutRoute.Settings)`
- **Health Connections** -> `navigate(HealthRoute.Connections)`
- **Subscription / Current Plan** -> `navigate(BillingRoute.Subscription)`
- **Rewards** -> `navigate(RewardsRoute.Home)`
- **Resources** -> `navigate(LearnRoute.Home)`
- **Settings** -> `navigate(SettingsRoute.Home)`

### Settings Gear Icon

Settings gear icon Profile screen के Top-Right में रहेगा और other account surfaces पर भी reusable entry हो सकता है।

- **Entry:** `navigate(SettingsRoute.Home)`
- **Content:** Technical preferences, notification preferences, units, language, theme, export data, legal, account controls.

Settings को feature-specific business settings duplicate नहीं करनी चाहिए। Settings list में ऐसे rows हो सकते हैं जो owning feature के route पर navigate करें।

---

## 6. Ownership Matrix

| Surface | UI Owner | Navigation Owner | Business Logic Owner | Repository Owner |
|---|---|---|---|---|
| Journey | Progress | Progress; Profile can launch | Progress | Progress |
| Progress Photos | Progress | Progress; Profile can launch | Progress | Progress / Media |
| Workout Settings | Workout | Workout; Profile/Settings can launch | Workout | Workout |
| Nutrition Targets | Nutrition | Nutrition; Profile/Settings can launch | Nutrition | Nutrition |
| Health Connections | Health | Health; Profile/Settings can launch | Health | Health |
| Subscription | Billing UI or Settings entry | Billing; Settings/Profile can launch | Billing / Entitlement | Billing |
| Resources | Learn | Learn; Profile/Settings can launch | Learn / Content | Learn / Content |
| Rewards | Rewards | Rewards; Profile can launch | Rewards | Rewards |
| Personal Information | Profile | Profile; Settings/Onboarding can launch | Profile | Profile |

Rule: If a row says "can launch", that feature only owns the entry point. It does not own data mutation, validation, business calculation, or persistence.

---

## 7. Navigation Freeze

Tnyx navigation 100+ screens के लिए graph-owned और feature-owned रहेगी।

```text
RootGraph
├── SplashGraph
├── AuthGraph
├── OnboardingGraph
├── MainGraph
│   ├── HomeGraph
│   ├── WorkoutGraph
│   ├── NutritionGraph
│   ├── CoachGraph
│   └── ProgressGraph
├── ProfileGraph
├── SettingsGraph
└── ModalGraph
```

### Root Graph

Root Graph app startup और graph-level routing own करेगा:
- Splash.
- Auth.
- Onboarding.
- Main.
- Full-screen required flows.

Root Graph feature internals नहीं जानेगा।

### Auth Graph

Auth Graph sign in, sign up, OTP, password reset, और auth session entry own करेगा। Auth subscription entitlement या profile domain data own नहीं करेगा।

### Onboarding Graph

Onboarding Graph initial data collection और resume flow own करेगा। Onboarding final persisted personal information Profile repository में save करेगा। Onboarding Nutrition/Workout/Health/Recovery targets collect कर सकता है, लेकिन ownership target-specific repositories की होगी।

### Main Graph

Main Graph shell tabs contain करेगा:
- Home.
- Workout.
- Nutrition.
- Coach.
- Progress.

Main Graph का काम shell और tab navigation है। यह feature business rules own नहीं करेगा।

### Profile Graph

Profile Graph avatar से launch होगा। यह profile home, personal information, और account launcher surfaces own करेगा।

### Settings Graph

Settings Graph gear icon से launch होगा। यह app config surfaces own करेगा।

### Modal Graph

Modal Graph app-wide modal surfaces के लिए होगा:
- Permission prompts.
- Paywall sheets.
- Confirmation dialogs.
- Media picker/camera flows.
- Legal dialogs.

Feature-specific modals owning feature graph में रह सकते हैं। App-wide modal contracts ModalGraph में रहेंगे।

---

## 8. Chrome Policy

हर destination अपनी chrome policy declare करेगा।

Allowed policies:
- `MainChrome`
- `NoBottomBar`
- `FullScreen`
- `BottomSheet`
- `Dialog`

### Policy Meaning

| Policy | Usage |
|---|---|
| `MainChrome` | Main tab screens with bottom nav and normal shell. |
| `NoBottomBar` | Detail/edit screens inside app where bottom nav hidden रहे। |
| `FullScreen` | Auth, onboarding, camera, media viewer, workout active session. |
| `BottomSheet` | Sheet-style transient destination. |
| `Dialog` | Confirmation, legal, warning, permission explanation. |

### Shell Rule

`MainShell` कभी feature-specific UI logic contain नहीं करेगा।

Allowed in MainShell:
- Top bar.
- Bottom nav.
- Shell-level spacing/insets.
- Route-derived selected tab.
- Chrome policy application.

Not allowed in MainShell:
- Nutrition calculations.
- Workout-specific business state.
- Progress analytics.
- Subscription paywall decision.
- Health connection logic.
- Feature-specific save/update actions.

Feature screens `UiState` render करेंगी और `Action` emit करेंगी। Navigation wiring Route/NavGraph layer में रहेगी।

---

## 9. Coding Rules for Profile & Settings

1. **No Data Duplication:** `Personal Information`, `Onboarding Data`, और `Settings Personal Info` का source of truth एक ही Profile repository होगा।
2. **Dumb UI:** Profile और Settings की सभी screens `UiState` render करेंगी और `Action` emit करेंगी।
3. **Module Independence:** `:features:profile` और `:features:settings` किसी दूसरे feature के internal widgets, ViewModels, repositories, या use cases import नहीं करेंगे।
4. **Public Route Contracts:** Cross-feature navigation केवल public route contracts से होगी।
5. **No Domain Leakage:** Settings को domain-specific settings duplicate नहीं करनी चाहिए; वह owning feature route पर navigate करेगी।
6. **Subscription Boundary:** Subscription business logic Billing / Entitlement में रहेगा। Settings केवल UI entry दे सकता है।
7. **Future Module Ready:** Health, Recovery, Billing, Rewards, Learn, Coach modules अभी optional implementation हो सकते हैं, लेकिन ownership अभी से fixed है।
8. **KMP/Wear Readiness:** Shared domain models और repository interfaces Android-specific imports से free रहेंगे।

---


### Current Skeleton Status

Current checked-in Android modules now include:

- `:features:profile`
- `:features:settings`
- `:features:progress`

These modules are skeleton boundaries only. They intentionally do not complete the final Profile, Settings, or Progress UI. Their purpose is to lock ownership and prevent future screens from landing in the wrong module.
## 10. Future Scalability

यह architecture 5-year roadmap के लिए freeze की जा रही है।

Expected future areas:
- Recovery.
- Sleep.
- HRV.
- Health Connect.
- Samsung Health.
- Garmin.
- Fitbit.
- Apple Health.
- Wear OS.
- AI Coach.
- Community.
- Challenges.
- Billing.
- Analytics.
- Offline Mode.

Scale rules:
- New feature अपनी navigation, presentation, domain ownership, और repository boundary own करेगा।
- Cross-feature launchers data mutation नहीं करेंगे।
- Shared domain contracts stable होने पर `:shared` में जाएंगे।
- Feature internals दूसरे feature से import नहीं होंगे।
- App shell feature-specific नहीं बनेगा।
- Repository ownership domain के साथ align रहेगा।

---

## 11. Final Folder Tree

यह final target tree है। आज सारे modules बनाना जरूरी नहीं है, लेकिन ownership और placement इसी structure के अनुसार freeze रहेंगे।

```text
apps/
├── app/                         # Android entry, DI composition, RootNavHost
├── wear/                        # Wear OS companion app
├── core/                        # Design system, UI components, shell, app foundations
│   ├── theme/
│   ├── ui/
│   │   ├── components/
│   │   └── shell/
│   ├── navigation/              # Public routes, chrome policy, route contracts
│   ├── session/
│   ├── permissions/
│   ├── analytics/
│   ├── notifications/
│   ├── remoteconfig/
│   └── logging/
├── shared/                      # Pure Kotlin domain contracts for phone/watch/future iOS
│   └── src/main/java/com/tnyx/shared/
│       ├── profile/domain/
│       ├── workout/domain/
│       ├── nutrition/domain/
│       ├── progress/domain/
│       ├── health/domain/
│       ├── recovery/domain/
│       ├── billing/domain/
│       ├── coach/domain/
│       ├── rewards/domain/
│       └── learn/domain/
└── features/
    ├── auth/
    ├── onboarding/
    ├── home/
    ├── workout/
    ├── nutrition/
    ├── coach/
    ├── progress/
    ├── profile/
    ├── settings/
    ├── health/                  # Future module
    ├── recovery/                # Future module
    ├── billing/                 # Future module
    ├── rewards/                 # Future module
    └── learn/                   # Future module
```

### Implementation Order

अभी implementation step-by-step हो सकती है:

1. Profile और Settings doc ownership align करें।
2. Public route contracts define करें।
3. Profile launcher skeleton बनाएं।
4. Settings graph skeleton बनाएं।
5. Progress module create करें जब Journey/Photos/Measurements real work शुरू हो।
6. Health/Recovery/Billing modules तब create करें जब first real screen या repository की जरूरत हो।

Architecture freeze का मतलब यह नहीं कि सभी modules तुरंत बनेंगे। इसका मतलब है कि जब भी feature बनेगा, वह इसी ownership boundary में बनेगा।

---

*Maintainer: TNYX Lead Architect*
