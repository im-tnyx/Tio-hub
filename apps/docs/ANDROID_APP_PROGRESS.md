# TNYX Android App Implementation Progress

यह file Android mobile app की current progress track करने के लिए है। इसका purpose यह है कि कौन सा काम complete है, कौन सा skeleton है, कौन सा placeholder है, और next implementation slice क्या होना चाहिए, यह साफ रहे।

Truth boundary:

- Runtime source actual behavior की final truth है।
- Product/ownership decisions के लिए `PROFILE_SETTINGS_GUIDE.md` canonical है।
- Navigation decisions के लिए `NAVIGATION_GUIDE.md` canonical है।
- Hardcoded data removal और Supabase slices के लिए `SUPABASE_INCREMENTAL_SETUP_PLAN.md` canonical है।

---

## 🟢 Phase 1: Core Android Foundation (COMPLETED)

- [x] **Gradle Android App Setup:** `:app` module Android entry point के रूप में configured है।
- [x] **Core Module:** `:core` module design system, reusable UI components, legal shell, और app shell को own करता है।
- [x] **Shared Module:** `:shared` pure Kotlin domain contracts के लिए available है।
- [x] **Wear Module:** `:wear` companion foundation repo में मौजूद है।
- [x] **Type-Safe Navigation Foundation:** `@Serializable` route contracts और Compose Navigation pattern use हो रहा है।
- [x] **MVI Pattern:** Route → Screen → ViewModel → Contract pattern docs और Nutrition implementation में established है।
- [x] **TnyxTheme:** Design tokens, semantic colors, typography, spacing, shapes, motion, और component tokens मौजूद हैं।
- [x] **App Shell:** `TnyxShell`, `MainBottomNav`, aur `MainTopBar` foundation maujood hai.

---

## 🟢 Phase 2: Architecture Freeze Docs (COMPLETED)

- [x] **Profile/Settings Ownership Freeze:** `PROFILE_SETTINGS_GUIDE.md` canonical architecture reference बन चुका है।
- [x] **Navigation Freeze:** `NAVIGATION_GUIDE.md` 100+ screens graph architecture, `ProfileGraph`, `SettingsGraph`, `ModalGraph`, और Chrome Policy define करता है।
- [x] **Architecture Guide Alignment:** `ARCHITECTURE.md` ownership freeze, shell rules, feature folder patterns, और chrome policy से aligned है।
- [x] **Supabase Incremental Plan:** `SUPABASE_INCREMENTAL_SETUP_PLAN.md` hardcoded data removal, seed data, RLS, repository/API contracts, और future TypeScript/Turborepo boundary define करता है।
- [x] **README Alignment:** Root README और app README current documentation links और ownership notes से aligned हैं।

---

## 🟢 Phase 3: Feature Module Skeletons (COMPLETED)

- [x] **Existing Feature Modules:** `:features:auth`, `:features:onboarding`, `:features:workout`, और `:features:nutrition` मौजूद हैं।
- [x] **Profile Module Skeleton:** `:features:profile` create हो चुका है।
- [x] **Settings Module Skeleton:** `:features:settings` create हो चुका है।
- [x] **Progress Module Skeleton:** `:features:progress` create हो चुका है।
- [x] **Home Module:** `:features:home` main dashboard UI own करता है और `:app` से wired है।
- [x] **Public Route Contracts:** `ProfileRoute`, `SettingsRoute`, और `ProgressRoute` add हो चुके हैं।
- [x] **Progress Tab Wiring:** Bottom nav का `Progress` tab अब `MainRoute.ProgressGraph` और `ProgressNavGraph` से wired है।
- [x] **Gradle Wiring:** `settings.gradle.kts` और `:app` dependencies में `home`, `profile`, `settings`, और `progress` modules include हैं।

Current Gradle feature modules:

```text
features/
├── auth/
├── onboarding/
├── home/
├── workout/
├── nutrition/
├── profile/
├── settings/
└── progress/
```

Important: `profile`, `settings`, और `progress` अभी skeleton boundaries हैं। इनका purpose ownership lock करना है, full production UI complete करना नहीं।

---

## 🟡 Phase 4: Current Runtime Feature Status (IN PROGRESS)

### Home

- [x] Home module exists and is Gradle-wired into `:app`.
- [x] `MainRoute.Home` renders the feature-owned `HomeRoute` and `HomeScreen`.
- [x] Home icon and image resource roots are separated under `res-icons` and `res-images`.
- [ ] Home currently renders an adaptive foundation summary; repository-backed dashboard sections are not implemented.

### Auth

- [x] Auth module exists.
- [x] Auth graph exists.
- [x] Login destination has minimum real UI.
- [x] Signup destination has minimum real UI.
- [x] OTP destination has minimum real UI.
- [x] Auth success flow is reachable from Login demo/sign-in and OTP verify actions.
- [x] AuthRepository contract exists.
- [x] FakeAuthRepository backs the minimum auth flow for app testing.
- [x] Unit tests cover FakeAuthRepository and Auth ViewModels (Login, Signup, OTP).
- [x] Fake Auth publishes a shared in-memory `AuthSession` with normalized,
  stable per-email user IDs.
- [x] Signup display name survives OTP verification and seeds the local Profile.
- [x] Settings Logout clears the active session and returns to `AuthGraph`
  after removing the authenticated main back stack.
- [x] Android owns `DataStoreAuthSessionStore`; Fake Auth session identity
  survives process restart and no password is persisted.
- [x] Splash resolves the persisted session before routing: signed-in sessions
  open `MainGraph`, signed-out state opens Welcome.
- [ ] Backend auth/token/identity source of truth is finalized.

### Onboarding

- [x] Onboarding docs and flow reference exist.
- [x] Splash and welcome foundation exist.
- [x] Versioned section/step domain contracts define the simple
  Profile -> Body Goal -> Workout -> Review flow with stable string IDs.
- [x] Flow tests cover ordering, cross-section navigation, serialization,
  insertion-safe positions, and invalid definitions.
- [x] Typed draft and versioned progress are persisted atomically through the
  feature-owned `OnboardingRepository` contract and app-owned Preferences
  DataStore implementation.
- [x] Resume validation preserves compatible checkpoints and resets stale flow
  versions, unknown positions, and unknown IDs.
- [x] Typed `RootRoute.Onboarding`, feature-owned navigation, generic container,
  ViewModel, state, actions, and effects are wired.
- [x] ViewModel serializes checkpoint operations and handles required answers,
  next/back/skip, local completion, and persistence retry.
- [x] Profile name, gender, and date-of-birth forms render in the container and
  persist stable typed answers.
- [x] Body Goal primary goal, height, current weight, target weight, and
  activity level forms render in the container and persist stable typed
  answers.
- [x] Workout experience, location, optional equipment, training days, and
  duration forms render in the container and persist stable typed answers.
- [x] Welcome `Get Started` opens onboarding; Welcome `Skip` still opens Main.
- [ ] Review is not implemented, so onboarding becomes incomplete after the
  Workout section.
- [ ] Full modular onboarding runtime is not yet product-complete.
- [ ] Authenticated account handoff, backend sync, and finalization are not yet
  repository-backed.

### Nutrition

- [x] Nutrition module exists.
- [x] Nutrition graph exists.
- [x] Meal Diary, Meal Editor, and Meal Item Editor screens exist.
- [x] Bottom nav visibility is hidden for meal edit/item edit flows.
- [ ] Nutrition data is still not fully local/backend repository-backed.
- [ ] Hardcoded sample meal data is still not fully removed.
- [ ] Nutrition targets table/repository slice is not implemented yet.

### Workout

- [x] Workout module exists.
- [x] Workout graph exists.
- [x] Workout graph now exposes typed Home and History destinations.
- [x] Planned product/UX target and staged delivery order are documented in `WORKOUT_PRODUCT_BLUEPRINT.md`.
- [x] Shared Workout contract v2 defines versioned exercise, media, routine, session, set, timer, and mutation models.
- [x] `ExerciseMediaResolver` implements approved exact -> neutral -> placeholder resolution without male/female cross-fallback.
- [x] `WorkoutReducer` implements deterministic start, exercise, set, timer, finish, and discard transitions with rejection reasons.
- [x] Phone Room database v1 persists the engine-state snapshot, completed-session history, catalog/routine JSON records, and mutation outbox in `apps/app`.
- [x] `RoomWorkoutRepository` applies reducer output and snapshot/outbox writes inside one Room transaction with mutation-ID and origin-sequence guards.
- [x] File-backed Robolectric recovery tests prove active-session/set recovery, persisted next-sequence recovery, idempotency, ordering, terminal history behavior, and rollback (7 tests, 0 failures).
- [x] Full shared/Phone/Wear validation gate passed on 2026-07-17 for `:shared:test :app:testDebugUnitTest :app:compileDebugKotlin :wear:compileDebugKotlin`.
- [x] Stage 3 Phone UI follows `Route + Screen + ViewModel + UiState + Action -> Coordinator -> WorkoutRepository`.
- [x] The thin offline flow starts a blank workout, adds one first-party starter exercise, completes one reps-based set, finishes once, and renders Room-backed history.
- [x] Coordinator and ViewModel tests cover ordered mutation sequencing across sessions, invalid input, restored UI state, and repeated-finish idempotency (7 tests, 0 failures).
- [x] Workout and its reused core Button, Input, Card, and Header consumers resolve visual state through `TnyxTheme` component/semantic tokens without feature-local hardcoded colors, dimensions, alpha values, or typography overrides.
- [x] Workout source now has additive exercise tracking types/snapshots and a feature-owned reusable exercise editor with keyed multiple-exercise, set, and metric UI state; Active mode is wired while Routine-edit and Read-only consumers remain future work.
- [x] Active Workout now uses a dense full-width set table with tracking-type columns, add-set, latest completed-session `Previous` mapping/copy, and an RPE 5-10 selector for strength/reps exercises.
- [ ] Corrected reusable-editor feature tests/compile and the full Phone/Wear gate remain pending because the local rerun was blocked by the Codex Gradle-cache usage limit.
- [ ] Exercise catalog/media, routine builder, notes, live rest timer, advanced set types, reordering, and device UX smoke are not implemented yet.

Boundary note: shared contracts, Phone persistence/recovery, and the first repository-backed Phone UI slice are implemented. Approved media catalog integration, routine building, advanced session UX, Settings UI, remote outbox delivery, and real Wear sync remain unimplemented. The blueprint allows a Tio-owned visual UI while preserving the approved Lyfta-derived core UX behavior.

### Profile

- [x] Profile module exists.
- [x] Profile public routes exist.
- [x] Profile home uses a card-less identity layout with reusable large avatar,
  `@username`, membership summary, and plain Weight/Height/BMR metrics.
- [x] Profile top bar exposes Edit and Settings; standalone Profile shows Back
  while the persistent You tab does not.
- [x] Edit and avatar actions open the real Settings Personal Information route.
- [x] Avatar entry selects You when enabled and launches standalone `ProfileGraph` as fallback.
- [x] Shared Profile model and Supabase repository DTO expose additive username data.
- [x] Active Profile Hilt binding uses a local persistent
  `RoomProfileRepository`; authenticated profiles are keyed by active fake
  user ID and signed-out state resolves to a clean guest profile.
- [x] Local Profile changes remain isolated across account switches and persist
  through app process/database recreation.
- [x] Personal Information reads active name, username, email, and avatar;
  Save updates normalized name/username and avatar actions update the active
  local Room profile.
- [x] Direct `SupabaseProfileRepository` is not the active Android Profile
  runtime source.
- [x] `bootstrap_user_profiles` और `deny_direct_auth_identity_access` migrations
  connected Tio-hub Supabase project पर applied और verified हैं।
- [ ] Personal Information email and mobile writes remain deferred; remote
  Profile/avatar synchronization is not implemented.
- [ ] Remaining Profile launchers are intentionally absent until their owning feature slices exist.

### Settings

- [x] Settings module exists.
- [x] Settings public routes exist.
- [x] Settings home skeleton exists.
- [x] Settings graph is launched from the You/Profile gear entry.
- [ ] App preferences, notifications, units, account, export, and about screens are placeholders/folders only.
- [ ] Subscription UI entry is not wired to Billing / Entitlement yet.

### Progress

- [x] Progress module exists.
- [x] Progress public routes exist.
- [x] Progress graph is wired as bottom-nav `Progress` tab.
- [x] Progress home skeleton exists.
- [ ] Journey screen is placeholder only.
- [ ] Progress Photos screen is placeholder only.
- [ ] Measurements, Weight, Achievements, and Analytics are placeholder/folder boundaries only.
- [ ] Progress repository/source of truth is not implemented yet.

---

## ⚪ Phase 5: Next Recommended Implementation Slices (PLANNED)

Recommended next order:

1. **Home dashboard vertical slice**
   - Replace the adaptive foundation copy with the first real summary section.
   - Consume only public feature contracts; keep domain repositories in their owners.
   - Keep Home as composition and launcher UI, not a cross-domain business owner.

2. **Nutrition repository vertical slice**
   - Add `NutritionRepository` contract.
   - Move Meal Diary read path from hardcoded ViewModel data to repository.
   - Add local/dev fake repository first if Supabase is not ready.
   - Then add Supabase tables/seed/RLS when implementation begins.

3. **Auth real session source slice**
   - Replace `FakeAuthRepository` with the approved backend auth implementation.
   - Keep ViewModels behind the stable `AuthRepository` contract.

4. **Progress real screens**
   - Implement Journey screen first.
   - Then Progress Photos.
   - Keep Weight/Measurements/Achievements as separate ownership folders.

5. **Supabase local/dev slice**
   - Start with Nutrition if immediate goal is removing hardcoded meal data.
   - Create only the tables needed for the slice.
   - Add seed data and RLS validation per `SUPABASE_INCREMENTAL_SETUP_PLAN.md`.

---

## ⚪ Phase 6: Future Modules (OWNERSHIP PLACEHOLDERS)

These modules are ownership-frozen and have placeholder folders only. Their
Gradle modules should not be created until the first real screen or repository is needed:

- [ ] `:features:health`
- [ ] `:features:recovery`
- [ ] `:features:billing`
- [ ] `:features:rewards`
- [ ] `:features:referrals`
- [ ] `:features:learn`
- [ ] `:features:coach`

Rule: Future module folders may exist as checked-in ownership placeholders, but Gradle modules should be added only when runtime code needs them.

---

## 📂 Current File Structure (As of 2026-07-29)

- `apps/app`: Android app entry, routing, DI composition.
- `apps/core`: theme, UI components, shell, route contracts.
- `apps/shared`: pure Kotlin shared domain contracts.
- `apps/features/auth`: Auth graph skeleton.
- `apps/features/onboarding`: Splash/welcome/onboarding foundation.
- `apps/features/workout`: Typed Workout graph, Stage 3 offline coordinator, Phone UI, and focused tests.
- `apps/features/nutrition`: Nutrition diary/editor screens.
- `apps/features/profile`: Profile launcher skeleton.
- `apps/features/settings`: Settings config skeleton.
- `apps/features/progress`: Progress graph and skeleton screens.
- `apps/features/home`: Main dashboard UI owner, Gradle-wired.
- `apps/features/coach`: Ownership placeholder only, not Gradle-wired.
- `apps/features/health`: Ownership placeholder only, not Gradle-wired.
- `apps/features/recovery`: Ownership placeholder only, not Gradle-wired.
- `apps/features/billing`: Ownership placeholder only, not Gradle-wired.
- `apps/features/rewards`: Ownership placeholder only, not Gradle-wired.
- `apps/features/referrals`: Referral/invite ownership placeholder with an icon resource directory, not Gradle-wired.
- `apps/features/learn`: Ownership placeholder only, not Gradle-wired.
- `apps/docs/ANDROID_APP_PROGRESS.md`: This tracking file.

---

## ✅ Latest Validation

### 2026-07-30: Workout onboarding section

- [x] `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:testDebugUnitTest :app:compileDebugKotlin --no-configuration-cache`
- [x] Result: BUILD SUCCESSFUL; onboarding 44 tests, app 25 tests, 0 failures,
  0 errors.
- [x] Scope: Workout experience/location/equipment/training-days/duration
  forms, shared onboarding choice cards, ViewModel validation, and Workout
  boundary progression into Review.
- [x] Review, account handoff, backend/Supabase sync, analytics, and business
  finalization remain incomplete or unchanged.

### 2026-07-30: Body Goal onboarding section

- [x] `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:testDebugUnitTest :app:compileDebugKotlin --no-configuration-cache`
- [x] Result: BUILD SUCCESSFUL; onboarding 38 tests, app 25 tests, 0 failures,
  0 errors.
- [x] Scope: Body Goal goal/activity selections, numeric body-stat steps,
  scroll-safe onboarding shell, ViewModel validation, and Body Goal boundary
  progression into Workout.
- [x] Workout, Review, account handoff, backend/Supabase sync, analytics, and
  business finalization remain incomplete or unchanged.

### 2026-07-30: Profile Onboarding section and Welcome entry

- [x] `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:testDebugUnitTest :app:compileDebugKotlin --no-configuration-cache`
- [x] Result: BUILD SUCCESSFUL; 57 tests, 0 failures, 0 errors.
- [x] Scope: Profile name/gender/date-of-birth forms, step-specific validation,
  bounded reusable date picker, and Welcome Get Started/Skip routing.
- [x] Body Goal, Workout, Review, account handoff, backend/Supabase sync,
  analytics, and business finalization remain incomplete or unchanged.

### 2026-07-29: Onboarding presentation container

- [x] `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:testDebugUnitTest :app:compileDebugKotlin --no-configuration-cache`
- [x] Result: BUILD SUCCESSFUL; 51 tests, 0 failures, 0 errors.
- [x] Scope: typed root destination, generic Tnyx-token container,
  ViewModel/state/actions/effects, serialized persistence operations,
  validation, next/back/skip, local completion, and retry behavior.
- [x] Welcome entry, section-specific forms, authenticated account handoff,
  backend/Supabase sync, analytics, and business finalization remain unchanged.

### 2026-07-29: Local Onboarding checkpoint persistence

- [x] `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:testDebugUnitTest :app:compileDebugKotlin --no-configuration-cache`
- [x] Result: BUILD SUCCESSFUL; 39 tests, 0 failures, 0 errors.
- [x] Scope: typed draft answers, versioned progress, atomic checkpoint
  persistence, Hilt binding, compatible resume, and stale-checkpoint reset.
- [x] No onboarding UI/navigation, authenticated account handoff, backend or
  Supabase sync, analytics, or completion finalization was added.

### 2026-07-29: Stable Onboarding flow contracts

- [x] `./gradlew.bat :features:onboarding:testDebugUnitTest
  :features:onboarding:compileDebugKotlin --no-configuration-cache`
- [x] Result: BUILD SUCCESSFUL.
- [x] Scope: stable section/step IDs, versioned default flow, deterministic
  next/previous positions, serialization, and insertion-safe progress identity.
- [x] No onboarding UI, draft persistence, backend synchronization, remote
  config, analytics, or finalization was added.

### 2026-07-29: Persistent Fake Auth session and Splash gate

- [x] First focused run compiled onboarding/Auth/app and passed
  onboarding/Auth tests.
- [x] App Kotlin and Hilt compilation succeeded with the app-owned
  `DataStoreAuthSessionStore` binding.
- [x] Root cause of the two app test failures was test-only:
  `ApplicationProvider` lacked `RobolectricTestRunner`.
- [x] `DataStoreAuthSessionStoreTest` now declares the required Robolectric
  runner.
- [x] Post-fix `:app:testDebugUnitTest` rerun passes with the Robolectric
  runner.
- [x] No password, backend runtime, Supabase apply/push, or remote data
  synchronization was added.

### 2026-07-29: Room-backed local Profile persistence

- [x] `./gradlew.bat :shared:test :features:auth:testDebugUnitTest
  :features:onboarding:testDebugUnitTest :features:profile:testDebugUnitTest
  :features:settings:testDebugUnitTest :app:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- [x] Result: BUILD SUCCESSFUL.
- [x] Active Profile binding persists per-user Profile JSON in Room and avatar
  JPEGs in app-internal storage.
- [x] Focused Robolectric coverage verifies database recreation and local avatar
  update/removal behavior.
- [x] No backend runtime or remote Supabase Profile/avatar synchronization was
  added.

### 2026-07-29: Fake Auth and per-user local Profile integration

- [x] `./gradlew.bat :shared:test :features:auth:testDebugUnitTest :features:profile:testDebugUnitTest :features:settings:compileDebugKotlin :app:testDebugUnitTest :app:compileDebugKotlin --no-configuration-cache`
- [x] Result: BUILD SUCCESSFUL
- [x] Shared result: 21 tests, 0 failures, 0 errors, 0 skipped.
- [x] Auth result: 44 tests, 0 failures, 0 errors, 0 skipped.
- [x] Profile result: 3 tests, 0 failures, 0 errors, 0 skipped.
- [x] App result: 18 tests, 0 failures, 0 errors, 0 skipped.
- [x] Follow-up `:features:settings:testDebugUnitTest` result: 9 tests,
  0 failures, 0 errors, 0 skipped.
- [x] Scope: shared session contract, stable Fake Auth identity, per-user
  in-memory Profile isolation, Settings logout, and local name/username/avatar
  editing.
- [x] No backend runtime, live Supabase change, remote account, or app-restart
  persistence was added.

### 2026-07-29: Backend-mediated Profile runtime boundary

- [x] `./gradlew.bat :app:testDebugUnitTest :features:profile:testDebugUnitTest :app:compileDebugKotlin --no-configuration-cache`
- [x] Result: BUILD SUCCESSFUL
- [x] App result: 15 tests, 0 failures, 0 errors, 0 skipped.
- [x] Profile result: 3 tests, 0 failures, 0 errors, 0 skipped.
- [x] Scope: active `InMemoryProfileRepository` binding, empty local defaults,
  update/identity/avatar behavior tests, and backend-mediated architecture docs.
- [x] No backend runtime, Supabase apply/push, or remote data synchronization
  changed in this slice.

### 2026-07-29: Card-less Profile and username contract

- [x] `./gradlew.bat :features:profile:testDebugUnitTest :app:compileDebugKotlin :shared:test --no-configuration-cache`
- [x] Result: BUILD SUCCESSFUL
- [x] Profile result: 3 tests, 0 failures, 0 errors, 0 skipped.
- [x] Scope: reusable Profile header, card-less identity UI, Edit/Settings routing,
  username fallback behavior, additive username model/DTO mapping, and shared compatibility.
- [x] Live Supabase verification: `profiles`, `user_nutrition_profiles`,
  `user_workout_profiles`, locked `auth_identities`, security-invoker
  `profile_overview`, और `tio-profile` bucket मौजूद हैं।
- [x] RLS/grants verification: Profile/Nutrition/Workout owner policies मौजूद हैं;
  `auth_identities` direct authenticated access explicitly denies; Security Advisor
  returned zero findings.
- [x] Data safety verification: no demo user or profile row seeded.
- [x] Durable live-schema inventory and future table backlog are tracked in
  `SUPABASE_SCHEMA_STATUS.md`.
- [x] Backend-mediated client data access is accepted in ADR-0006; live schema
  existence is no longer described as active Android synchronization.

### 2026-07-16: Workout shared contract v2

- [x] `./gradlew.bat :shared:test :app:compileDebugKotlin :wear:compileDebugKotlin`
- [x] Result: BUILD SUCCESSFUL
- [x] Shared test result: 15 tests, 0 failures, 0 errors, 0 skipped
- [x] Scope: contract serialization, KMP boundary, gender-media resolution, reducer transitions, Phone compile compatibility, and Wear compile compatibility

### 2026-07-17: Workout Stage 2 validation gate

- [x] `./gradlew.bat :shared:test :app:testDebugUnitTest :app:compileDebugKotlin :wear:compileDebugKotlin`
- [x] Result: BUILD SUCCESSFUL
- [x] Scope: Phone Room persistence v1, `RoomWorkoutRepository`, shared reducer compatibility, recovery tests, Phone compile compatibility, and Wear compile compatibility

### 2026-07-18: Workout Stage 3 local validation gate

- [x] `./gradlew.bat :shared:test :features:workout:testDebugUnitTest :app:testDebugUnitTest :app:compileDebugKotlin :wear:compileDebugKotlin --no-configuration-cache`
- [x] Result: BUILD SUCCESSFUL
- [x] Workout feature result: 7 tests, 0 failures, 0 errors, 0 skipped
- [x] Scope: typed Workout navigation, repository-backed coordinator, thin offline Phone UI, restored UI state, finish idempotency, theme/component-token compliance, shared compatibility, Phone tests/compile, and Wear compile
- [ ] Device/emulator force-stop and relaunch UX smoke remains a review action

### Previous validation

- [x] `./gradlew.bat :app:compileDebugKotlin`
- [x] Result: BUILD SUCCESSFUL
- [x] Scope: AuthRepository boundary, FakeAuthRepository, Auth minimum screens, and AuthGraph wiring compile with app.

Known warning:

- Onboarding uses deprecated `ClickableText`; existing warning, not introduced by this progress tracker.

---

**Last Updated:** 2026-07-30
**Current Focus:** Complete the Review step so the onboarding shell can reach a real local completion state before any backend/account handoff work.
