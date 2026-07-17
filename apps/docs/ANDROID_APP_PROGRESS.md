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
- [x] **Public Route Contracts:** `ProfileRoute`, `SettingsRoute`, और `ProgressRoute` add हो चुके हैं।
- [x] **Progress Tab Wiring:** Bottom nav का `Progress` tab अब `MainRoute.ProgressGraph` और `ProgressNavGraph` से wired है।
- [x] **Gradle Wiring:** `settings.gradle.kts` और `:app` dependencies में `profile`, `settings`, और `progress` modules include हैं।

Current skeleton modules:

```text
features/
├── auth/
├── onboarding/
├── workout/
├── nutrition/
├── profile/
├── settings/
└── progress/
```

Important: `profile`, `settings`, और `progress` अभी skeleton boundaries हैं। इनका purpose ownership lock करना है, full production UI complete करना नहीं।

---

## 🟡 Phase 4: Current Runtime Feature Status (IN PROGRESS)

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
- [ ] Supabase/Firebase auth source of truth is finalized.

### Onboarding

- [x] Onboarding docs and flow reference exist.
- [x] Splash and welcome foundation exist.
- [ ] Full modular onboarding runtime is not yet wired into the new app architecture.
- [ ] Onboarding persistence is not yet repository-backed.
- [ ] Resume manager is not yet Supabase-backed.

### Nutrition

- [x] Nutrition module exists.
- [x] Nutrition graph exists.
- [x] Meal Diary, Meal Editor, and Meal Item Editor screens exist.
- [x] Bottom nav visibility is hidden for meal edit/item edit flows.
- [ ] Nutrition data is still not fully repository/Supabase-backed.
- [ ] Hardcoded sample meal data is still not fully removed.
- [ ] Nutrition targets table/repository slice is not implemented yet.

### Workout

- [x] Workout module exists.
- [x] Workout graph exists.
- [x] Workout tab is intentionally kept as a simple placeholder.
- [x] Planned product/UX target and staged delivery order are documented in `WORKOUT_PRODUCT_BLUEPRINT.md`.
- [x] Shared Workout contract v2 defines versioned exercise, media, routine, session, set, timer, and mutation models.
- [x] `ExerciseMediaResolver` implements approved exact -> neutral -> placeholder resolution without male/female cross-fallback.
- [x] `WorkoutReducer` implements deterministic start, exercise, set, timer, finish, and discard transitions with rejection reasons.
- [x] Phone Room database v1 persists the engine-state snapshot, completed-session history, catalog/routine JSON records, and mutation outbox in `apps/app`.
- [x] `RoomWorkoutRepository` applies reducer output and snapshot/outbox writes inside one Room transaction with mutation-ID and origin-sequence guards.
- [x] File-backed Robolectric recovery tests prove active-session/set recovery, idempotency, ordering, terminal history behavior, and rollback (6 tests, 0 failures).
- [x] Full shared/Phone/Wear validation gate passed on 2026-07-17 for `:shared:test :app:testDebugUnitTest :app:compileDebugKotlin :wear:compileDebugKotlin`.
- [ ] Workout redesign and real production screens are not implemented yet.
- [ ] Workout feature UI is not yet wired to the repository-backed runtime.

Boundary note: shared contracts and Phone persistence/recovery are implemented, but Phone UI consumption, approved media catalog integration, Settings UI, remote outbox delivery, and real Wear sync remain unimplemented. The blueprint allows a Tio-owned visual UI while preserving the approved Lyfta-derived core UX behavior.

### Profile

- [x] Profile module exists.
- [x] Profile public routes exist.
- [x] Profile home skeleton exists.
- [ ] Profile graph is not yet launched from avatar in root navigation.
- [ ] Personal Information repository source of truth is not implemented yet.
- [ ] Profile launcher cards are not production UI yet.

### Settings

- [x] Settings module exists.
- [x] Settings public routes exist.
- [x] Settings home skeleton exists.
- [ ] Settings graph is not yet launched from gear icon in root navigation.
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

1. **Profile/Settings navigation wiring**
   - Launch `ProfileGraph` from avatar.
   - Launch `SettingsGraph` from gear icon.
   - Keep Profile as launcher only.

2. **Nutrition repository vertical slice**
   - Add `NutritionRepository` contract.
   - Move Meal Diary read path from hardcoded ViewModel data to repository.
   - Add local/dev fake repository first if Supabase is not ready.
   - Then add Supabase tables/seed/RLS when implementation begins.

3. **Auth real session source slice**
   - Replace `FakeAuthRepository` with Supabase/Firebase or backend-backed implementation.
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

## ⚪ Phase 6: Future Modules (PLANNED, DO NOT CREATE YET)

These modules are ownership-frozen in docs but should not be created until first real screen/repository is needed:

- [ ] `:features:health`
- [ ] `:features:recovery`
- [ ] `:features:billing`
- [ ] `:features:rewards`
- [ ] `:features:learn`
- [ ] `:features:coach`

Rule: Future module folders should be created only when runtime code needs them.

---

## 📂 Current File Structure (As of 2026-06-27)

- `apps/app`: Android app entry, routing, DI composition.
- `apps/core`: theme, UI components, shell, route contracts.
- `apps/shared`: pure Kotlin shared domain contracts.
- `apps/features/auth`: Auth graph skeleton.
- `apps/features/onboarding`: Splash/welcome/onboarding foundation.
- `apps/features/workout`: Simple Workout placeholder graph.
- `apps/features/nutrition`: Nutrition diary/editor screens.
- `apps/features/profile`: Profile launcher skeleton.
- `apps/features/settings`: Settings config skeleton.
- `apps/features/progress`: Progress graph and skeleton screens.
- `apps/docs/ANDROID_APP_PROGRESS.md`: This tracking file.

---

## ✅ Latest Validation

### 2026-07-16: Workout shared contract v2

- [x] `./gradlew.bat :shared:test :app:compileDebugKotlin :wear:compileDebugKotlin`
- [x] Result: BUILD SUCCESSFUL
- [x] Shared test result: 15 tests, 0 failures, 0 errors, 0 skipped
- [x] Scope: contract serialization, KMP boundary, gender-media resolution, reducer transitions, Phone compile compatibility, and Wear compile compatibility

### 2026-07-17: Workout Stage 2 validation gate

- [x] `./gradlew.bat :shared:test :app:testDebugUnitTest :app:compileDebugKotlin :wear:compileDebugKotlin`
- [x] Result: BUILD SUCCESSFUL
- [x] Scope: Phone Room persistence v1, `RoomWorkoutRepository`, shared reducer compatibility, recovery tests, Phone compile compatibility, and Wear compile compatibility

### Previous validation

- [x] `./gradlew.bat :app:compileDebugKotlin`
- [x] Result: BUILD SUCCESSFUL
- [x] Scope: AuthRepository boundary, FakeAuthRepository, Auth minimum screens, and AuthGraph wiring compile with app.

Known warning:

- Onboarding uses deprecated `ClickableText`; existing warning, not introduced by this progress tracker.

---

**Last Updated:** 2026-07-17
**Current Focus:** Workout Stage 2 exit criteria now pass on the current Android branch and the work is review-ready. The next implementation gate is Workout Stage 3: the first thin offline vertical slice (`Start blank workout -> add one exercise -> complete one set -> finish -> open history -> restart app and verify recovery`).
