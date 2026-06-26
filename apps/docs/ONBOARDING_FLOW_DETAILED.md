# TnyX Android: Onboarding Runtime Flow Detailed Reference

Document Status: Source-aligned Android onboarding reference
Last Verified: 2026-06-19
Owner: Android Engineering
Truth Boundary: This document describes checked-in Android source under `apps/android/mobile/app/src/main/java/com/tnyx/mobile`. Runtime source remains final truth.

This document explains what happens from app launch to onboarding completion, and which Kotlin files participate at each stage. Folder location matters less than runtime responsibility because onboarding is a large dynamic flow.

---

## 1. App Open: Launcher To Compose Root

### `AndroidManifest.xml`

Role: Declares the first Android entry point.

- Registers `SplashActivity` with `android.intent.action.MAIN`.
- Registers `SplashActivity` with `android.intent.category.LAUNCHER`.
- Registers `MainActivity` separately; `MainActivity` is opened after splash.

### `SplashActivity.kt`

Role: Native launch/splash handoff.

- Runs the initial splash/branding experience.
- Starts `MainActivity` after splash delay/animation.
- Finishes splash so the app moves into the main Compose runtime.

### `MainActivity.kt`

Role: Root Android activity for app UI.

- Extends `FragmentActivity`.
- Initializes `GoogleAuthManager`.
- Initializes `TruecallerAuthManager`.
- Wraps app content in `TnyXTheme`.
- Hosts `RootNavHost`.
- Passes Google/Truecaller callbacks into navigation.
- Also implements Razorpay callbacks, but that is outside onboarding.

---

## 2. Root Navigation: Splash Graph Decides Where User Goes

### `RootNavHost.kt`

Role: Root graph orchestrator.

Root graph starts at `Graph.SPLASH`, specifically `Route.SPLASH_LOADING`. `SplashLoadingScreen` then sends the user to one of these graphs:

- `Graph.AUTH`
- `Graph.ONBOARDING`
- `Graph.MAIN`

Decision order:

1. Developer override via `AppConfig.STARTUP_MODE`.
2. Firebase user check.
3. Local onboarding resume route check.
4. Backend/user onboarding status check for signed-in users.

### `AppConfig.kt`

Role: Developer/runtime switches.

Current startup modes:

- `AppStartupMode.NORMAL`: normal auth/onboarding/main routing.
- `AppStartupMode.DIRECT_HOME`: bypasses startup checks and opens `Graph.MAIN`.
- `AppStartupMode.DIRECT_ONBOARDING`: opens onboarding at `Section.DATA` with `entryPath = SKIP`.

These modes are development shortcuts, not production onboarding truth.

### When Firebase User Is Missing

If `firebaseAuth.currentUser == null`:

- `RemoteConfigManager.getConfig()` provides onboarding config/version.
- `ResumeManager.loadRoute(config.version)` checks local onboarding route.
- If resume route exists:
  - `entryPath = SKIP` and `signupCompleted = false` sends user to `Route.AUTH_UI`.
  - Otherwise user resumes at `Route.onboardingMain(...)`.
- If no resume route exists, user goes to `Graph.AUTH`.

### When Firebase User Exists

If Firebase user exists:

- `AuthViewModel.registerFcmToken(...)` registers FCM/device payload using `DeviceHelper`.
- `AuthViewModel.checkUserStatus(...)` checks profile/target/source completion.
- Fully complete user goes to `Graph.MAIN`.
- Incomplete user:
  - resumes saved onboarding route when available.
  - otherwise starts at `Section.DATA`, `Section.TARGETS`, or `Section.SOURCE` based on incomplete backend status.
  - enters onboarding through `Route.onboardingMain(...)` with sign-in/mobile context.

---

## 3. Auth Graph And Onboarding Entry

### `AuthNavGraph.kt`

Role: Auth screens and login/signup routing before onboarding.

- Handles email, Google, Truecaller, and phone auth routes.
- Builds `deviceId` and `deviceFingerprint` using `DeviceHelper`.
- Sends device identity into auth calls.
- After auth success, routes user into onboarding/main depending on status.

### `RootRoute.kt`

Role: Route and graph constants.

Important onboarding route:

- `Route.ONBOARDING_MAIN_WITH_START`
- `Route.onboardingMain(...)`

This route carries onboarding context as arguments:

- `startSection`
- `entryPath`
- `signupCompleted`
- `authProvider`
- `mobilePresent`
- `mobileVerified`
- `namePrefilled`
- `skipIntro`

These arguments decide whether onboarding starts from intro, data, mobile verification, targets, source, or a resumed step.

---

## 4. Onboarding Graph

### `OnboardingNavGraph.kt`

Role: Owns onboarding navigation inside `Graph.ONBOARDING`.

Routes:

- `Route.ONBOARDING_MAIN_WITH_START`: main onboarding form flow.
- `Route.SETTING_UP_PROCESSING`: finalizing/progress screen.
- `Route.CONGRATS`: completion success screen.

For `Route.ONBOARDING_MAIN_WITH_START`:

- Reads route arguments.
- Converts string args into `Section`, `OnboardingEntryPath`, and `AuthProvider`.
- Gets graph-scoped `OnboardingViewModel` and `AuthViewModel`.
- Calls `OnboardingRoute(...)`.

For `Route.SETTING_UP_PROCESSING`:

- Shows `SettingUpScreen` from `ProgressScreen.kt`.
- Runs `viewModel.finalizeOnboarding(...)`.
- Simulates progress up to 85% while backend finalize is running.
- On success, navigates to `Route.CONGRATS`.
- On failure, shows Toast and returns to onboarding.

For `Route.CONGRATS`:

- Shows `OnboardingCongratsScreen`.
- Continue navigates to `Graph.MAIN` and clears `Graph.ONBOARDING`.

---

## 5. OnboardingRoute: Runtime Context Builder

### `OnboardingRoute.kt`

Role: Thin Compose route between navigation and onboarding container.

Responsibilities:

- Watches Firebase auth state through `FirebaseAuth.AuthStateListener`.
- Calculates `isUserLoggedIn`.
- Calculates `needsMobileScreen`.
- Passes all dependencies and route arguments into `OnboardingContainer`.

Important behavior:

- In testing mode, user is treated as logged in.
- Mobile screen is required when signed-in user has no Firebase phone number, unless testing mode overrides it.

---

## 6. OnboardingContainer: Main Flow Controller

### `OnboardingContainer.kt`

Role: Central UI orchestration for onboarding.

This is the most important runtime file for active onboarding flow. It controls:

- Current `FlowState`.
- Current `Section`.
- Current step inside section.
- Button visibility/text/enabled state.
- Step validation.
- Progress bar value.
- Back behavior.
- Draft save at section boundaries.
- Resume persistence.
- Analytics events.
- Auth interruption/resume behavior.
- Rendering current screen through `SectionRenderer`.

Main dependencies:

- `BuildFlowUseCase`
- `RestoreFlowUseCase`
- `NextStepUseCase`
- `PreviousStepUseCase`
- `CompleteOnboardingUseCase`
- `StepValidator`
- `StepConfig`
- `ResumeManager`
- `RemoteConfigManager`
- `AnalyticsTracker`
- `SectionRenderer`

Runtime sequence:

1. Initial route context is copied into `OnboardingViewModel`.
2. `RestoreFlowUseCase` builds starting `FlowState`.
3. `StepConfig.getSteps(...)` calculates total steps for current section.
4. `StepValidator` decides bottom button enabled state.
5. `rememberOnboardingProgress(...)` calculates top progress value.
6. On every section/step change:
   - `ResumeManager.save(...)` persists route.
   - `AnalyticsEvent.ScreenView` is tracked.
   - button visibility is recalculated.
7. `SectionRenderer(...)` renders current section/step screen.
8. Bottom button dispatches `OnboardingAction.NextClicked`.
9. Back button dispatches `OnboardingAction.BackClicked`.

### Next Click Behavior

When user presses next:

- `AnalyticsEvent.NextClicked` is tracked.
- For `WORKOUT_INTRO`, workout choice rebuilds path:
  - yes -> `Section.WORKOUT`
  - no -> `Section.TARGETS`
- For normal sections, `NextStepUseCase` decides next move.
- If current section is complete and user is logged in, `saveDraftForSection(...)` syncs that section.
- If auth is required, route is persisted and user is sent to auth.
- If flow is complete, `CompleteOnboardingUseCase` calls `onFinish`, opening `Route.SETTING_UP_PROCESSING`.

### Back Behavior

When user presses back:

- `AnalyticsEvent.BackClicked` is tracked.
- `PreviousStepUseCase` decides previous section/step.
- If result is exit:
  - skip intro state is reset.
  - resume route is cleared.
  - user exits onboarding graph.

---

## 6A. Get Started, Next, Back, And Skip Button Flow

This section explains user-facing button behavior from the first welcome/onboarding entry.

### WelcomeScreen Buttons

### `WelcomeScreen.kt`

Role: First auth/welcome surface shown from `Graph.AUTH` when no signed-in/resumable user is routed directly to onboarding.

Buttons/actions:

- `Get Started`: starts normal onboarding entry path. User enters onboarding with `entryPath = GET_STARTED`, usually starting from `Section.INTRO`.
- `Sign In`: opens auth UI for existing users.
- `Skip`: skips intro-first onboarding and opens `Route.AUTH_UI` with skip context. After auth, onboarding can resume with `entryPath = SKIP` and usually starts from `Section.DATA`.
- Language selector changes language through `LanguageSelectionSheet` and `viewModel.updateLanguage(...)`.
- Terms/privacy text opens legal document dialogs; it does not move onboarding forward.

### Intro Skip Inside Onboarding

Files involved:

- `SectionRenderer.kt`
- `IntroSection.kt`
- `OnboardingContainer.kt`
- `OnboardingAction.kt`

Runtime behavior:

1. `SectionRenderer` renders `IntroSection` for `Section.INTRO`.
2. `IntroSection` renders `IntroScreen1` to `IntroScreen5` based on current step.
3. If intro skip is triggered, `SectionRenderer` dispatches `OnboardingAction.IntroSkipped`.
4. `OnboardingContainer` handles `IntroSkipped` by:
   - setting `entryPath = SKIP`.
   - resetting signup gate.
   - clearing auth route context.
   - building a new path from `Section.DATA` step 1.
   - persisting this skipped route through `ResumeManager`.
   - warming up server through `authViewModel.warmupServer`.
   - sending user to `Route.AUTH_UI` through `onRequireAuth`.

So skip does not simply jump to the next intro screen. It creates a skip onboarding path and sends unauthenticated user to auth before continuing.

### Next Button Flow

Files involved:

- `OnboardingBottomBar.kt`
- `OnboardingContainer.kt`
- `NextStepUseCase.kt`
- `OnboardingStateMachine.kt`
- `StepValidator.kt`
- `StepConfig.kt`

Runtime behavior:

1. `OnboardingBottomBar` displays the current button text from `StepValidator.buttonText(...)`.
2. Button enabled state comes from `StepValidator.isStepValid(...)` and draft saving state.
3. On click, `OnboardingContainer` dispatches `OnboardingAction.NextClicked`.
4. `AnalyticsEvent.NextClicked` is tracked.
5. If current section is `WORKOUT_INTRO`:
   - `workoutDecision == "yes"` rebuilds path to `Section.WORKOUT` step 1.
   - otherwise path is rebuilt to `Section.TARGETS` step 1.
6. For normal sections, `NextStepUseCase` runs:
   - if current step is less than total steps, it moves to `step + 1` in same section.
   - if current step is last step, it asks `OnboardingStateMachine` for next section and moves to that section step 1.
   - if current section is `DATA` and user is not logged in, it returns `RequireAuth` and pauses onboarding at current data step.
   - if there is no next section, it returns `Complete`.
7. On section completion, `OnboardingContainer` calls `saveDraftForSection(...)` for signed-in users.
8. On `Complete`, `CompleteOnboardingUseCase` triggers finalization route.

### Back Button Flow

Files involved:

- `OnboardingTopBar.kt`
- `OnboardingContainer.kt`
- `PreviousStepUseCase.kt`
- `OnboardingStateMachine.kt`
- `StepConfig.kt`

Runtime behavior:

1. Top back button dispatches `OnboardingAction.BackClicked`.
2. Android system back is also captured by `BackHandler` and dispatches the same action.
3. `AnalyticsEvent.BackClicked` is tracked.
4. `PreviousStepUseCase` runs:
   - if current step is greater than 1, it moves to `step - 1` in same section.
   - if current step is 1, it asks `OnboardingStateMachine` for previous section.
   - if previous section exists, it moves to that section's last step using `StepConfig.getSteps(...)`.
   - if no previous section exists, it returns `Exit`.
5. On `Exit`, `OnboardingContainer`:
   - resets skip intro state.
   - clears saved resume route through `ResumeManager.clear()`.
   - calls `onExit`, which navigates out of onboarding.

### Button Visibility Rules

File: `StepValidator.kt`

- `INTRO` step 1 hides button until typing/animation signals completion.
- `INTRO` later steps show normal next button after animation.
- `DATA`, `WORKOUT_INTRO`, and `WORKOUT` usually rely on screen-level typing/selection completion to show button.
- `MOBILE`, `TARGETS`, and `SOURCE` generally show bottom button directly.
- Final source step uses `FINISH` text instead of `CONTINUE`.

---

## 7. SectionRenderer: Section To Screen Mapping

### `SectionRenderer.kt`

Role: Converts current `Section + step` into actual Compose UI.

Mapping:

- `Section.INTRO` -> `IntroSection`
- `Section.DATA` -> `DataSection`
- `Section.MOBILE` -> `MobileScreen`
- `Section.WORKOUT_INTRO` -> `WorkoutIntroSection`
- `Section.WORKOUT` -> `WorkoutSection`
- `Section.TARGETS` -> `TargetsSection`
- `Section.SOURCE` -> `SourceSection`

This file is important because onboarding is not one screen. It is a dynamic renderer that picks the right section component based on runtime flow state.

---

## 8. Sections And Screens

### `Section.kt`

Role: Defines high-level onboarding sections.

Current sections:

- `INTRO`
- `DATA`
- `MOBILE`
- `WORKOUT_INTRO`
- `WORKOUT`
- `TARGETS`
- `SOURCE`

### `StepConfig.kt`

Role: Defines step count per section.

Current counts:

- `INTRO`: 5
- `DATA`: 9
- `MOBILE`: 1
- `WORKOUT_INTRO`: 1
- `WORKOUT`: 9 when `gymAccess == "home"`, otherwise 8
- `TARGETS`: 6
- `SOURCE`: 2

### Intro Section

Files:

- `IntroSection.kt`
- `IntroScreen1.kt`
- `IntroScreen2.kt`
- `IntroScreen3.kt`
- `IntroScreen4.kt`
- `IntroScreen5.kt`

Role:

- Shows first-time intro/welcome education screens.
- Can be skipped through `OnboardingAction.IntroSkipped`.
- Skip path sends user to auth before continuing data collection.

### Data Section

Files:

- `DataSection.kt`
- `NameScreen.kt`
- `GenderScreen.kt`
- `GoalScreen.kt`
- `AgeScreen.kt`
- `HeightScreen.kt`
- `WeightScreen.kt`
- `TargetScreen.kt`
- `ActivityScreen.kt`
- `HealthConditionScreen.kt`

Step mapping:

1. Name
2. Gender
3. Goal
4. DOB/Age
5. Height
6. Current weight
7. Target weight
8. Activity level
9. Health conditions

Important: `MobileScreen` is not inside `DataSection`. Mobile verification is separate `Section.MOBILE`, inserted dynamically by `FlowEngine`.

### Mobile Section

Files:

- `MobileScreen.kt`
- `MobileVerifyField.kt`

Role:

- Collects/verifies mobile when required.
- Appears only when flow rules decide phone verification is still needed.
- Placement is dynamic:
  - after data for `GET_STARTED` / `SIGN_IN`
  - before/around data for `SKIP`, depending on route context

### Workout Intro Section

Files:

- `WorkoutIntroSection.kt`
- `WorkoutIntroScreen.kt`

Role:

- Asks whether user wants workout plan/coaching.
- Yes includes workout section.
- No skips workout and moves to targets.

### Workout Section

Files:

- `WorkoutSection.kt`
- `GymAccessScreen.kt`
- `EquipmentScreen.kt`
- `ExperienceLevelScreen.kt`
- `FocusAreasScreen.kt`
- `TrainingDaysScreen.kt`
- `WorkoutDurationScreen.kt`
- `WorkoutSplitScreen.kt`
- `HealthConcernsScreen.kt`
- `SpecialEventScreen.kt`

Dynamic step mapping:

1. Gym access
2. Equipment, only when `gymAccess == "home"`
3. Experience level
4. Focus areas
5. Training days
6. Workout duration
7. Workout split
8. Workout health concerns
9. Special event

If gym access is not home, equipment step is skipped, so workout has 8 steps.

### Targets Section

Files:

- `TargetsSection.kt`
- `BridgeScreen.kt`
- `StepTargetScreen.kt`
- `SleepTargetScreen.kt`
- `WaterTargetScreen.kt`
- `GoalPaceScreen.kt`
- `NutritionScreen.kt`

Step mapping:

1. Bridge/transition screen
2. Step target
3. Sleep target and schedule
4. Water target
5. Goal pace
6. Nutrition target summary

### Source Section

Files:

- `SourceSection.kt`
- `SourceScreen.kt`

Role:

- Captures discovery channel/referral/source data.
- Source section has 2 configured steps, but currently `SourceSection` renders `SourceScreen` for step 1 and no UI for other steps.
- `StepValidator` requires source selection on step 1, including text when source is `other`.

---

## 9. Dynamic Flow Engine

### `FlowEngine.kt`

Role: Builds personalized section list.

Inputs:

- `entryPath`
- `signupCompleted`
- `workoutEnabled`
- `authProvider`
- `mobilePresent`
- `mobileVerified`
- `namePrefilled`
- `needsMobileScreen`
- current section/step

Rules:

- Starts from remote config sections.
- Removes `Section.MOBILE`, then inserts it only if needed.
- Removes intro for `SKIP`, `SIGN_IN`, or config with `showIntro = false`.
- Removes workout intro/workout for `SKIP`, `SIGN_IN`, disabled workout, or config with `enableWorkout = false`.
- Normalizes mobile state for `PHONE` and `TRUECALLER`.
- Treats Google as name-prefilled.

### `BuildFlowUseCase.kt`

Role: Public use case wrapper around `FlowEngine.buildPath(...)`.

Used when path needs to be created or rebuilt, especially after workout yes/no decision.

### `RestoreFlowUseCase.kt`

Role: Creates initial `FlowState` from route arguments.

Used by `OnboardingContainer` during startup.

### `NextStepUseCase.kt`

Role: Moves forward.

Possible results:

- `Move`: go to another section/step.
- `RequireAuth`: pause onboarding and go to auth.
- `Complete`: onboarding form flow is done.

### `PreviousStepUseCase.kt`

Role: Moves backward.

Possible results:

- `Move`: go to previous section/step.
- `Exit`: leave onboarding.

---

## 10. State, Validation, Persistence, And API

### `OnboardingViewModel.kt`

Role: Holds onboarding data and performs onboarding operations.

Key responsibilities:

- Owns `OnboardingData`.
- Updates profile/workout/target/source fields.
- Restores draft from backend when Firebase user exists.
- Falls back to local storage when remote draft is unavailable.
- Saves completed section draft through `saveDraftForSection(...)`.
- Migrates local draft after auth when required.
- Finalizes onboarding through `finalizeOnboarding(...)`.
- Normalizes API payload fields before sending backend data.
- Tracks `AnalyticsEvent.OnboardingCompleted` after finalize success.

### `OnboardingData.kt`

Role: Main in-memory data model for user inputs.

Includes profile, mobile, workout, target, source, and completion-related fields used across screens and API mapping.

### `StepValidator.kt`

Role: Controls validation and button behavior.

Examples:

- Name must be at least 3 characters.
- Gender is required.
- Primary goal is required.
- Target weight must be greater than 0.
- Activity level is required.
- Workout fields are required depending on dynamic workout step.
- Goal pace can block continue when invalid.
- Source is required, and `other` requires text.

### `ResumeManager.kt`

Role: Persists local route progress.

Important: It uses `SharedPreferences`, not `DataStore`.

Stored fields include:

- `entry_path`
- `signup_completed`
- `workout_enabled`
- `auth_provider`
- `mobile_present`
- `mobile_verified`
- `name_prefilled`
- `section`
- `step`
- `version`
- `auth_required`

Saved `version` is matched against `RemoteConfigManager.getConfig().version`. If version changes, old resume route is ignored.

### `OnboardingStorage.kt` And Local Data Source

Role: Local draft fallback.

Used when remote draft is unavailable or before authenticated backend sync is possible.

### `OnboardingApi.kt`

Role: Retrofit API contract for onboarding backend calls.

Current endpoints:

- `GET api/v1/onboarding` -> `getOnboardingDraft`
- `PATCH api/v1/onboarding/{section}` -> generic section draft
- `PATCH api/v1/onboarding/profile` -> profile draft
- `PATCH api/v1/onboarding/workout` -> workout draft
- `PATCH api/v1/onboarding/target` -> target draft
- `PATCH api/v1/onboarding/source` -> source draft
- `POST api/v1/source/apply-referral` -> referral/source apply
- `POST api/v1/onboarding/finalize` -> finalize onboarding
- `PATCH api/v1/targets` -> update targets

### `OnboardingRemoteDataSource.kt`

Role: Authenticated remote onboarding calls.

- Gets Firebase token.
- Calls `OnboardingApi`.
- Parses backend errors.
- Saves/restores/finalizes remote onboarding state.

### `OnboardingRepositoryImpl.kt`

Role: Coordinates local and remote onboarding data.

- Restores local fallback where needed.
- Pulls remote draft.
- Saves local and remote draft.
- Finalizes onboarding and clears local state where appropriate.

### `OnboardingMapper.kt`

Role: Converts between domain model and API request/response models.

This is where `OnboardingData` becomes backend-compatible profile/workout/target/source payloads.

---

## 10A. Auth Success Resume Flow

This section explains how auth returns the user back into onboarding.

### `AuthNavGraph.kt`

Role: Auth graph decides where a successful login/signup should land.

Important helper functions:

- `navigateAfterStatus(...)`
- `applyAuthRouteContext(...)`
- `resolveRouteProvider(...)`
- `shouldShowMobileForProvider(...)`
- `providerHasVerifiedMobile(...)`

### Welcome -> Get Started

When user taps `Get Started` on `WelcomeScreen`:

1. `onboardingViewModel.updateEntryPath(OnboardingEntryPath.GET_STARTED)` runs.
2. signup gate is reset through `resetSignupGate()`.
3. Navigation opens `Route.onboardingMain(...)` with:
   - `startSection = Section.INTRO`
   - `entryPath = GET_STARTED`
   - `signupCompleted = false`
4. User enters normal intro-first onboarding.

### Welcome -> Skip

When user taps `Skip` on `WelcomeScreen`:

1. If `AppConfig.SKIP_TO_HOME_DIRECTLY` is true, app goes to `Graph.MAIN`.
2. Otherwise `entryPath` becomes `SKIP`.
3. User is sent to `Route.AUTH_UI`.
4. After successful auth, onboarding resumes with skip context.

### Auth UI Success

When `Route.AUTH_UI` auth succeeds:

1. `AuthViewModel.checkUserStatus(...)` checks backend/user completion status.
2. `ResumeManager.loadRoute(config.version)` tries to restore saved onboarding route.
3. Provider is resolved from saved route or fresh auth success provider.
4. `applyAuthRouteContext(...)` updates onboarding context:
   - `authProvider`
   - `mobilePresent`
   - `mobileVerified`
   - `namePrefilled`
5. `navigateAfterStatus(...)` decides final destination:
   - fully complete -> `Graph.MAIN`
   - profile incomplete -> `Route.onboardingMain(...)` starting at `Section.DATA` or `Section.MOBILE`
   - target incomplete -> `Section.TARGETS`
   - source incomplete -> `Section.SOURCE`

### Phone Auth Success

When phone auth succeeds:

- `signupCompleted` is marked true.
- `authProvider = PHONE`.
- `mobilePresent = true`.
- `mobileVerified = true`.
- User is routed by `navigateAfterStatus(...)`.

### Email Login Success

When existing user logs in through email:

- `entryPath` becomes `SIGN_IN`.
- Auth context is applied from provider/profile/Firebase state.
- User is routed to main, data, targets, or source depending on backend completion state.

### Email Signup Success

When email signup completes:

- Saved resume route is preferred when available.
- `signupCompleted` is marked true.
- Auth context is applied.
- User is routed back into onboarding using saved route or current entry path.

### Google / Truecaller Context

- `GOOGLE` can prefill name from Firebase display name.
- `TRUECALLER` and `PHONE` are treated as verified mobile providers.
- `GOOGLE` and `EMAIL` generally still require mobile verification unless mobile is already present/verified.

---

## 10B. Draft Save, Restore, And Migration Lifecycle

Onboarding has two different persistence layers. They solve different problems.

### Route Progress: `ResumeManager.kt`

Purpose: remembers where user was in onboarding.

Storage: `SharedPreferences` named `onboarding`.

Stores route metadata:

- entry path
- signup completion
- workout enabled state
- auth provider
- mobile state
- section
- step
- config version
- auth required flag

Used by:

- `RootNavHost` during app startup.
- `AuthNavGraph` after auth success.
- `OnboardingContainer` on every section/step change.

This is not the full form draft. It only restores route position and routing context.

### Form Draft: `OnboardingStorage.kt`

Purpose: stores actual `OnboardingData` locally.

Storage: Android `DataStore` named `onboarding_prefs`.

Stored data:

- serialized `OnboardingData` JSON under `onboarding_data`.

Used when:

- backend draft is unavailable.
- user is not authenticated yet.
- local inputs need to survive app restart before remote sync.

### Remote Draft: `OnboardingApi.kt`

Remote draft endpoints:

- `GET api/v1/onboarding`
- `PATCH api/v1/onboarding/profile`
- `PATCH api/v1/onboarding/workout`
- `PATCH api/v1/onboarding/target`
- `PATCH api/v1/onboarding/source`
- generic `PATCH api/v1/onboarding/{section}` also exists.

### Save Timing

Inside `OnboardingContainer`:

1. Every section/step change persists route through `ResumeManager.save(...)`.
2. When a section is completed and user is logged in, `saveCompletedSection(...)` calls `viewModel.saveDraftForSection(...)`.
3. `saveDraftForSection(...)` calls `SaveDraftUseCase`.
4. `SaveDraftUseCase` calls `OnboardingRepository.saveDraft(section, data)`.
5. Repository/data source maps and sends section-specific draft to backend.

Important behavior:

- `Section.INTRO` is not remotely saved as onboarding draft.
- Mobile/profile data maps into profile-like draft payloads.
- Workout draft is skipped when workout decision says no/skip/later/false.

### Restore Timing

Inside `OnboardingViewModel` restore flow:

1. If Firebase user exists, `RestoreDraftUseCase` tries remote draft first.
2. If remote draft is missing/unavailable, local `OnboardingStorage.load()` can provide fallback.
3. Restored data becomes current `OnboardingData`.
4. `RestoreFlowUseCase` and saved route decide where UI resumes.

### Migration After Auth

When onboarding pauses for auth and user returns logged in:

1. `OnboardingContainer` detects `flowState.isAuthRequired && isUserLoggedIn`.
2. It marks signup completed.
3. It runs `viewModel.migrateLocalDraft(...)`.
4. `MigrateLocalDraftUseCase` saves current data as `Section.DATA` through `SaveDraftUseCase`.
5. After migration success, `NextStepUseCase` continues the flow.

This is the bridge from unauthenticated local onboarding input to authenticated backend draft.

---

## 10C. Language And Translation Behavior

### `LanguageManager.kt`

Role: Runtime language lookup and fallback.

- Holds `currentLanguage`.
- Reads labels using `LanguageManager.getText(key)`.
- Falls back when a key is missing for current language.
- Uses system locale only for initial/default language detection.

### `TranslationKeys.kt`, `EnStrings.kt`, `HiStrings.kt`

Role: Define translation keys and language-specific labels.

Onboarding screens use these keys for titles, descriptions, buttons, helper text, sheet labels, and validation/info copy.

### `WelcomeScreen.kt`

Language behavior:

- Top-left language chip opens `LanguageSelectionSheet`.
- Selected language calls `viewModel.updateLanguage(selectedLanguageEnum.name.lowercase())`.
- Welcome labels come from `LanguageManager.getText(...)`.

### `StepValidator.kt`

Button text is translated through `LanguageManager`:

- `LETS_DO_IT`
- `NEXT`
- `CONTINUE`
- `FINISH`

So button labels change with current language.

### Screen-level Recomposition

Many onboarding screens read `LanguageManager.currentLanguage` inside `remember(...)` for option lists. This means options and labels are rebuilt when current language changes.

Examples:

- `GoalScreen`
- `TrainingDaysScreen`
- `WorkoutDurationScreen`
- `WorkoutSplitScreen`

### Current Boundary

Language selection updates onboarding state and runtime labels, but this document does not claim full app-wide locale persistence unless source confirms it through storage/bootstrap code.

---

## 10D. Remote Config, Payload Mapping, And Known Gaps

### `RemoteConfigManager.kt`

Role: Provides onboarding flow configuration.

Current checked-in config:

- `sections = INTRO, DATA, WORKOUT_INTRO, WORKOUT, TARGETS, SOURCE`
- `version = 1`
- `enableWorkout = true`
- `showIntro = true`

This config is used by `FlowEngine` and `ResumeManager` route version matching.

Important behavior:

- If config version changes, old saved resume route is ignored.
- If `showIntro = false`, intro and workout intro can be removed from flow.
- If `enableWorkout = false`, workout intro/workout are removed.

### `OnboardingMapper.kt`

Role: Converts `OnboardingData` to backend-compatible payloads and merges backend drafts back into domain data.

Payload builders:

- `buildProfileData(...)`
- `buildWorkoutData(...)`
- `buildTargetData(...)`
- `buildSourceData(...)`
- `buildDraftPayload(section, state)`

Normalizer usage:

- `GenderNormalizer`
- `GoalNormalizer`
- `ActivityNormalizer`
- `WorkoutNormalizer`
- `TimeNormalizer`
- `WaterTargetConverter`
- `SleepCalculator`

Section API mapping:

- `Section.DATA` -> `profile`
- `Section.MOBILE` -> no direct API section, but draft payload is profile-shaped
- `Section.WORKOUT_INTRO` -> no direct API section
- `Section.WORKOUT` -> `workout`
- `Section.TARGETS` -> `target`
- `Section.SOURCE` -> `source`
- `Section.INTRO` -> no API section

### Known Implementation Gaps / Watch Points

These are source-observed notes, not product blockers by themselves:

- `SourceSection` has 2 configured steps through `StepConfig`, but currently renders `SourceScreen` only for step 1.
- `Section.MOBILE` uses profile-like draft payload data, but does not map to a dedicated remote API section in `OnboardingMapper.apiSection(...)`.
- `RemoteConfigManager` is currently static checked-in config, not a live remote fetch implementation.
- `AnalyticsLogger` sends Firebase Analytics events; it does not print debug Logcat events by itself.
- `ResumeManager` route progress and `OnboardingStorage` form draft are separate storage systems; do not treat one as replacing the other.

---

## 10E. Planned Add-on: Gender-based Visual Personalization

This is a planned onboarding personalization layer. It is not fully implemented in the current checked-in workout image code yet.

### Goal

When user selects gender in `GenderScreen`, later onboarding screens should feel personalized. For example:

- If user selects `male`, workout/focus-area images can use male-oriented visuals.
- If user selects `female`, workout/focus-area images can use female-oriented visuals.
- Neutral/fallback images should be used when gender is missing, unknown, or user chooses a non-binary/unsupported value in future.

### Current Source Truth

Current implemented behavior:

- `GenderScreen.kt` allows `male` and `female` selection.
- `DataSection.kt` sends gender selection to `viewModel.updateGender(...)`.
- `OnboardingViewModel.kt` stores it in `OnboardingData.gender`.
- `StepValidator.kt` requires gender on `Section.DATA` step 2.
- `OnboardingMapper.kt` sends normalized gender to backend profile draft/finalize payload.
- `TargetsSection.kt` passes gender into `StepTargetScreen` for target calculation/display logic.

Current workout visual behavior:

- `FocusAreasScreen.kt` uses static drawable assets such as `photo_fullbody`, `photo_shoulders`, `photo_arms`, `photo_back`, `photo_chest`, `photo_abs`, `photo_gluts`, `photo_legs`, and `photo_cardio`.
- `EquipmentScreen.kt` uses static equipment icons such as `ic_dumble`, `ic_bench`, `ic_mat`, `ic_barbell`, `ic_bands`, and `ic_kettle`.
- These workout visuals are not currently selected by gender in source.

### Proposed Implementation Shape

Recommended files/responsibilities:

- `OnboardingData.gender`: source of selected gender.
- `WorkoutSection.kt`: pass `viewModel.state.gender` into visual screens that need personalization.
- `FocusAreasScreen.kt`: select image set based on gender.
- `EquipmentScreen.kt`: usually can stay neutral unless product wants gender-specific equipment illustrations.
- `res/drawable`: add gender-specific image assets with clear naming.

Suggested asset naming:

- `photo_male_fullbody`
- `photo_female_fullbody`
- `photo_male_shoulders`
- `photo_female_shoulders`
- `photo_male_arms`
- `photo_female_arms`
- `photo_male_back`
- `photo_female_back`
- `photo_male_chest`
- `photo_female_chest`
- `photo_male_abs`
- `photo_female_abs`
- `photo_male_glutes`
- `photo_female_glutes`
- `photo_male_legs`
- `photo_female_legs`
- `photo_male_cardio`
- `photo_female_cardio`

### Suggested Runtime Rule

Pseudo-flow:

1. User selects gender in `GenderScreen`.
2. `OnboardingViewModel.updateGender(...)` stores it.
3. User reaches workout/focus area screens.
4. Screen receives current gender.
5. Screen resolves drawable:
   - `male` -> male asset
   - `female` -> female asset
   - anything else -> neutral existing asset
6. UI renders personalized visual.

Example resolver shape:

```kotlin
fun focusAreaImageForGender(areaId: String, gender: String): Int {
    return when (gender.lowercase()) {
        "male" -> maleFocusAreaImage(areaId)
        "female" -> femaleFocusAreaImage(areaId)
        else -> neutralFocusAreaImage(areaId)
    }
}
```

### Product Boundary

This should be treated as UI personalization only.

- It should not change validation.
- It should not change workout logic by itself.
- It should not alter backend payload shape unless product explicitly wants gender-specific asset metadata.
- Existing neutral images should remain fallback to avoid broken UI when an asset is missing.

### Why This Matters

Gender-selected visual personalization can make onboarding feel more personal without changing the core flow. The selected gender already exists in state, so this add-on can be implemented as a contained UI asset-selection layer around workout/focus-area imagery.

---

## 11. Finalization Flow

Final journey:

1. User completes final required section.
2. `NextStepUseCase` returns `Complete`.
3. `CompleteOnboardingUseCase` triggers `onFinish`.
4. `OnboardingNavGraph` navigates to `Route.SETTING_UP_PROCESSING`.
5. `SettingUpScreen` is shown from `ProgressScreen.kt`.
6. `OnboardingViewModel.finalizeOnboarding(...)` runs.
7. `FinalizeOnboardingUseCase` calls repository finalize.
8. Repository/data source calls `POST api/v1/onboarding/finalize`.
9. On success:
   - progress becomes 100%.
   - app navigates to `Route.CONGRATS`.
   - `OnboardingCongratsScreen` is shown.
10. User taps continue.
11. App navigates to `Graph.MAIN`.

On failure:

- Toast shows error.
- App returns to `Route.onboardingMain(...)` with current route context.

---

## 12. UI Components Used By Onboarding

### `OnboardingTopBar.kt`

Role: Back button plus progress display.

- Lives in onboarding presentation components.
- Uses `DynamicProgressBar`.
- Hidden/visible behavior is controlled by `OnboardingUiState`.

### `OnboardingBottomBar.kt`

Role: Sticky bottom continue/next/finish button.

- Text comes from `StepValidator.buttonText(...)`.
- Enabled state comes from `StepValidator.isStepValid(...)`.
- Visibility comes from `StepValidator.shouldShowButton(...)` and section-specific typing behavior.

### `OnboardingProgress.kt`

Role: Calculates onboarding progress for current flow, section, and step.

### `DynamicProgressBar.kt`

Role: Reusable progress bar that accepts a float progress value.

---

## 13. Analytics

### `AnalyticsEvent.kt`

Defines onboarding analytics events:

- `ScreenView`
- `NextClicked`
- `BackClicked`
- `OnboardingCompleted`
- `UserAction`

### `AnalyticsTracker.kt`

Maps structured events to event names:

- `screen_view`
- `next_clicked`
- `back_clicked`
- `onboarding_completed`
- `user_action`

### `AnalyticsLogger.kt`

Sends events to Firebase Analytics using `firebase.logEvent(...)`.

Important: This logger does not print Logcat debug logs by itself.

---

## 14. Device Identity

### `DeviceHelper.kt`

Role: Device/auth payload helper.

Provides:

- `getDeviceId`
- `getDeviceFingerprint`
- platform/version/build helpers
- FCM token helper

Used clearly in auth and root user-status flow:

- `RootNavHost` uses it for FCM registration.
- `AuthNavGraph` and `AuthViewModel` use it for login/signup/device trust payloads.

---

## 15. Architecture Boundary

The current onboarding implementation mostly follows:

`Route -> Container -> SectionRenderer -> Section -> Screen -> ViewModel -> UseCase/Repository`

Important boundaries:

- `RootNavHost` decides graph-level routing.
- `OnboardingNavGraph` owns onboarding graph routes.
- `OnboardingRoute` prepares auth/runtime context.
- `OnboardingContainer` owns active onboarding UI flow state.
- `SectionRenderer` maps runtime section/step to actual UI.
- Section files map steps to screens.
- Screens should remain UI-focused and call ViewModel update methods.
- ViewModel owns state mutations, draft/finalize operations, and backend-facing normalization.
- Use cases own flow decisions and completion behavior.
- Repository/data source/API own persistence and network calls.



