# Task: Nutrition Text Log And Live Search

## Status
- State: In Progress
- Primary Owner: Android Nutrition
- Created: 2026-08-09

## Objective
Complete authenticated manual meal logging and the first live food-search
slice: create or edit a meal, add manual or searched food items, persist the
aggregate to Supabase, and reload it in Meal Diary.

## Scope
- Keep voice, Edamam, USDA, and multi-provider orchestration out of this slice.
- Limit barcode scope to a bundled ML Kit analyzer on the existing CameraX
  preview with handoff into Search Meals; no separate scanner screen, barcode
  provider, or duplicate editor flow.
- Extend the slice with a dedicated crop-free meal camera client and a
  FatSecret Image Recognition adapter. Deployment and external meal-photo
  transmission were explicitly approved on 2026-08-10; provider access,
  licensing, and storable-data constraints remain verification gates.
- Route debounced search queries from the dedicated Search Meals screen through
  an authenticated Supabase Edge Function backed by Open Food Facts
  Search-a-licious.
- Treat Meal Item Editor as draft editing; persist only from Meal Editor.
- Load existing meals and items before editing.
- Keep failed saves on-screen with actionable validation or error feedback.
- Reuse existing Tnyx theme tokens and input/button components.
- Add focused repository and ViewModel coverage.

## Safety Boundary
- Live Supabase hardening was separately approved on 2026-08-09.
- Owner-scoped RLS and least-privilege table grants remain the database boundary.
- No provider credentials or secrets enter the Android client.

## Validation
- `:features:nutrition:testDebugUnitTest`
- `:features:nutrition:compileDebugKotlin`
- `:app:compileDebugKotlin`
- `git diff --check`

## Next Checkpoint
- Connect an Android device and smoke-test camera permission, live preview,
  flash, gallery, capture, Retry/Done, Meal Editor photo handoff, and a known
  packaged-food barcode that exists in the current search provider.
- Verify FatSecret Image Recognition server secrets, provider access/licensing,
  and provider-reference persistence before treating photo analysis as live.

## Latest Checkpoint (2026-08-09)
- Text-only meal and item editors now use validated draft state.
- `MealEditor` owns aggregate Supabase persistence and existing meal loading.
- Successful mutations refresh Meal Diary immediately; uncontrolled polling
  and silent diary read failures are removed.
- `:features:nutrition:testDebugUnitTest` passed with 11 tests.
- Combined `:features:nutrition:testDebugUnitTest` and
  `:app:compileDebugKotlin` validation passed after the live migration.
- `git diff --check` and provider-secret key-name scan passed.
- Live `harden_nutrition_meal_logs` and
  `index_nutrition_meal_log_owner_fk` migrations were applied and listed.
- Live rollback-only verification passed authenticated CRUD, cross-user
  isolation, `updated_at`, and cascade delete with no persisted test rows.
- `anon` has zero privileges on both meal tables; `authenticated` has only
  `SELECT`, `INSERT`, `UPDATE`, and `DELETE`.
- Provider credentials remain outside the Android client and repository.
- `nutrition-food-search` version 6 is live with `verify_jwt=true` and
  permanent-user enforcement; anon-role invocation returns `401` and
  Search-a-licious returned live product hits.
- Search results use database-compatible UUIDs and navigate into Meal Editor as
  draft items.
- Search Meals now uses a dedicated button-free result flow: typing is debounced,
  category and add controls are removed, and tapping a result opens Meal Editor.
- Search Meals is standalone without shell bottom navigation, and each result
  source renders as one grouped card with tappable rows.
- Group cards show a short overview preview; tapping a group header opens its
  standalone full-result view without refetching the query.
- Food database groups currently show 5 preview rows and request up to 15 live
  results for the full group view.
- Meal Editor renders aggregate calories and macro summaries, keeps the existing
  save bar, and routes Add item back through Search Meals without losing draft
  state. Tapping an existing meal item opens MealItemEditor for detailed
  micronutrition editing.
- Connected-device smoke verified Search result -> Meal Editor -> Add item ->
  Search -> second result -> same two-item draft, plus item-row navigation into
  the detailed Edit item screen.
- Meal Editor now uses a compact identity area with a themed meal-photo tile,
  a plain meal name edited only from its pencil-triggered bottom sheet, and
  separate clickable editors for serving count and per-serving amount/unit.
  The first selected food seeds serving size from its unit (for example,
  `100 g`), and connected-device smoke verified the layout on SM-S921B.
- Meal Editor now offers direct Camera and Gallery selection with an in-place
  preview and no crop flow. JPEG, PNG, and WebP inputs are capped at 10 MB;
  selected bytes remain draft-only until `Save Meal`.
- The app-owned repository now uploads owner-scoped meal photos, stores durable
  private Storage references, resolves short-lived signed URLs for rendering,
  and cleans replaced, removed, or deleted objects. The checked-in
  `tio-nutrition-media` migration remains unapplied to live Supabase, so live
  photo upload is not yet testable.
- Meal-level `servingSize` and `servingsDescription` are draft state only until
  an explicitly approved schema migration adds their persistence contract.
- Meal Editor's `Today` control keeps the date-time picker visible while wheel
  selection callbacks update the draft; only explicit dismissal closes it.
- Nutrition tests and `:app:compileDebugKotlin` pass with `functions-kt` wired.
- Meal Item Editor now uses the Tio design system for a reference-informed
  ingredient form: plain editable name, quantity/unit selection, editable macro
  card, collapsible supported micronutrients, reset/delete actions, and a fixed
  inset-safe Save action.
- Ingredient edits remain draft-only until `Save Meal`; the aggregate save
  persists all supported item nutrition fields and signals Meal Diary to reload
  the Supabase-backed snapshot so calories, protein, fiber, carbs, sugar, and
  fats update in the nutrition grid.
- Live `Tio-hub` Supabase schema was read-only verified with RLS enabled on
  `meal_logs` and `meal_log_items`; the item table includes `calories`,
  `protein`, `carbs`, `fats`, `fiber`, `sugar`, `trans_fat`, and
  `saturated_fat`.
- Vitamin fields remain intentionally absent because the domain model and live
  schema do not yet define a persistence contract for them.
- Combined `:features:nutrition:testDebugUnitTest` and
  `:app:compileDebugKotlin` passed after adding full nutrition-grid aggregation
  assertions and meal aggregate nutrient preservation coverage.
- `:features:nutrition:testDebugUnitTest :app:testDebugUnitTest
  :app:compileDebugKotlin` passed after adding the crop-free meal photo draft,
  storage-reference, size-limit, and persistence path.
- Meal Diary camera now navigates to a dedicated CameraX screen rather than
  Search Meals. The screen supports permission, live preview, flash, gallery,
  shutter, captured preview, Retry/Done, crop-free temp-photo handling, and
  parsed draft handoff into Meal Editor.
- `nutrition-meal-photo-analyze` active version 10 is deployed with
  `verify_jwt=true`. Provider credentials are configured as remote-only secrets
  and remain outside the Android client and repository. The function now tries
  OAuth2 first and falls back to signed OAuth1 requests for consumer credentials.
- Connected-device `Done` requests now pass provider authentication. The latest
  live invocation returned `422` with FatSecret's `No food was detected` result
  for a laptop-screen photo, replacing the previous configuration failure.
- Analysis preprocessing now follows FatSecret's recommended square input by
  producing an orientation-corrected, centered 512x512 JPEG while leaving the
  original Meal Editor photo unchanged.
- Checked-in FatSecret recognition source now uses v2 first and retries the
  supported v1 model only when v2 returns provider code `211`; other provider
  failures are not masked. Supabase connector usage limits blocked deployment,
  so remote photo function version 10 does not include this fallback yet.
- Live text search now prioritizes India-tagged Open Food Facts results and
  fills sparse regional matches from a deduplicated global fallback. User input
  is escaped before it enters the provider's Lucene query.
- `nutrition-food-search` version 13 is live with `verify_jwt=true`; connected-device
  `paneer` search returned `200`, 15 results, and an India-relevant Amul item first.
- The Android adapter now maps only typed provider errors into UI messages and
  replaces unexpected transport exceptions with a generic message. Regression
  coverage prevents raw authorization headers or session details from being
  rendered in the camera error state.
- `:features:nutrition:testDebugUnitTest :app:testDebugUnitTest
  :app:compileDebugKotlin` and `git diff --check` pass for the camera slice.
- The updated debug APK installed successfully on the connected `SM-S921B`.
  Provider-success handoff still needs a real-meal positive recognition test.
  Local `deno` remains unavailable, so Edge Function type-check remains pending.
- Camera controls now use a balanced Gallery / shutter / Barcode row. Barcode
  mode runs bundled ML Kit analysis on the same CameraX preview, supports EAN-13,
  EAN-8, UPC-A, and UPC-E, and forwards a detected code into Search Meals.
  Connected-device smoke detected `5000171010025` and opened prefilled Search
  Meals without launching a separate scanner screen; that test code returned no
  provider match, so positive barcode-to-food resolution remains pending.
- `FoodSearchRepository` now separates exact barcode lookup from text search.
  Checked-in `nutrition-food-search` validates 8-14 digit codes and calls Open
  Food Facts product-by-code, then FatSecret Barcode v2 with `region=IN`, then
  optionally calls Edamam's UPC parser with server-side credentials. An exact
  response opens Meal Editor directly and no match falls back to Search Meals.
  Android requires `lookup: "barcode"` before trusting a response as exact,
  preventing remote v13 text results from being misclassified. Remote
  deployment, provider access/licensing/attribution verification, and positive
  indexed-product smoke are still pending.
- Publication validation passed on 2026-08-10 with
  `:features:nutrition:testDebugUnitTest`, `:app:testDebugUnitTest`,
  `:app:compileDebugKotlin`, and `:app:assembleDebug`.
- GitHub publication is split into reviewable commits: database migrations
  `284490c`, Edge Function source `4f7d27c`, and Android runtime/tests
  `c6bdd76`; all three are pushed to `codex/nutrition-text-log`.
