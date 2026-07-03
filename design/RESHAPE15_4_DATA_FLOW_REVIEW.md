# Reshape 15.4 Data Storage, Asset, and API Flow Review

Last reviewed: 2026-06-30
Scope: `G:\Tnyx-hub\archive\reshape15.4`
Status: Archive/reference analysis, not TNYX production architecture.

## 1. Executive Summary

`reshape15.4` ek Flutter Android archive/decompiled copy hai:

- App identity: `com.nomatictech.nutribook`, label `Reshape`, version `15.4.0`.
- Flutter assets bundled hain: exercise catalog, exercise embeddings, prompts, translations, nutrition source data, workout warmup/cooldown routines, images, audio, and one promo video.
- Exercise catalog local JSON mein hai: `assets/exercise.json` has 1,795 exercises.
- Exercise media ka large part remote CDN se load hota hai: Azure CDN host `reshape-storage-fqfnabahatc0dkhg.z02.azurefd.net`; some entries also have YouTube links.
- Local images bundled hain: `assets/images` under Flutter assets has 425 files; widget layouts and launcher/native resources also bundled hain.
- App has 51 Flutter plugins registered, including `sqflite_android`, `shared_preferences_android`, `flutter_secure_storage`, `home_widget`, `health`, `flutter_downloader`, `firebase_*`, `purchases_flutter`, `mixpanel_flutter`, `clevertap_plugin`, `customer_io`, `appsflyer_sdk`, `workmanager_android`.
- Exact private backend REST endpoint count safely recover nahi hua because Flutter Dart source/AOT native library is not present in this archive. Manifest/resources/native Java se integrations clear hain, but app backend URL list exhaustive nahi hai.
- Local storage exact schema visible nahi hai, but plugin evidence shows app supports SQLite, SharedPreferences/DataStore, secure storage, app-private files/cache, widget state, downloader cache, and OS health stores.

Practical answer: Reshape ka model "all data backend se live load" nahi hai. Kaafi catalog/prompts/assets app ke andar bundled hain; media/CDN, analytics, push, billing, AI/backend calls, health integrations network/provider side par hain; user data likely local persistence + backend sync model se chalta hai, but exact Dart schema archive mein missing hai.

## 2. Files Inspected

Primary files:

- `G:\Tnyx-hub\archive\reshape15.4\app\build.gradle`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\AndroidManifest.xml`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\res\values\strings.xml`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\res\xml\network_security_config.xml`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\java\io\flutter\plugins\GeneratedPluginRegistrant.java`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\java\com\nomatictech\nutribook\MainActivity.java`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\java\com\nomatictech\nutribook\CameraWidgetProvider.java`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\java\com\nomatictech\nutribook\WaterWidgetProvider.java`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\java\com\nomatictech\nutribook\LogWaterAction.java`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\java\cn\r0.java`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\java\cn\b1.java`

Flutter asset files:

- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\assets\flutter_assets\AssetManifest.bin`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\assets\flutter_assets\assets\exercise.json`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\assets\flutter_assets\assets\exercises-embedding.json`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\assets\flutter_assets\assets\prompts.json`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\assets\flutter_assets\assets\fio_feature_guide.md`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\assets\flutter_assets\assets\cardio_activities.json`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\assets\flutter_assets\assets\warmup-routine.json`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\assets\flutter_assets\assets\cooldown-routine.json`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\assets\flutter_assets\assets\data\top_sources.json`
- `G:\Tnyx-hub\archive\reshape15.4\app\src\main\assets\flutter_assets\assets\translations\*.json`

Important limitation:

- No `libapp.so`, `libflutter.so`, `.apk`, or `.aab` was found under `G:\Tnyx-hub\archive\reshape15.4`.
- That means compiled Dart logic and exact private API route strings are not fully available in this archive.

## 3. App Identity and Platform

Evidence from `app\build.gradle`:

- `namespace`: `com.nomatictech.nutribook`
- `applicationId`: `com.nomatictech.nutribook`
- `compileSdkVersion`: `35`
- `minSdkVersion`: `29`
- `targetSdkVersion`: `35`
- `versionCode`: `1340`
- `versionName`: `15.4.0`
- `buildConfig = false`

Evidence from `AndroidManifest.xml`:

- App label: `Reshape`
- Main activity: `com.nomatictech.nutribook.MainActivity`
- Flutter embedding: version `2`
- Custom schemes/app links:
  - `reshape://app`
  - `homeWidgetCamera://home`
  - `homeWidgetCamera://opencameradialog`
  - `homeWidgetCamera://openmicdialog`
  - `homeWidgetCamera://openTextDialog`
  - `homeWidgetCamera://openwaterdialog`
  - HTTPS app link host: `reshape-cf8c3.web.app`

## 4. Bundled Data and Assets

### 4.1 Exercise Catalog

`assets/exercise.json`:

- Root type: JSON array.
- Count: 1,795 exercises.
- Sample fields:
  - `id`
  - `exercise`
  - `primary`
  - `secondary`
  - `landscape_video_url_male`
  - `portrait_video_url_male`
  - `landscape_video_url_female`
  - `portrait_video_url_female`
  - `equipment`
  - `type`
  - `canonical_id`
  - `deprecated`
  - `equipment_raw`
  - `characteristics`
  - `details`

Observed media counts:

- `landscape_video_url_male`: 1,561 exercises.
- `portrait_video_url_male`: 1,560 exercises.
- `landscape_video_url_female`: 538 exercises.
- `portrait_video_url_female`: 537 exercises.
- `embed_link`: 421 exercises.

Remote host domains found in exercise/prompt/guide assets:

- `reshape-storage-fqfnabahatc0dkhg.z02.azurefd.net`
- `youtu.be`
- `youtube.com`
- `www.youtube.com`

Interpretation:

- Exercise list is bundled locally.
- Exercise video media is mostly remote Azure CDN URL based.
- Some video/embed flows use YouTube links.
- Exercise thumbnails may be generated/cached from video or use local UI artwork; unlike Lyfta, this archive does not show a simple `image_name` field per exercise.

### 4.2 Embeddings

`assets/exercises-embedding.json`:

- File size: about 35.7 MB.
- Purpose inferred from name: local semantic/search embedding data for exercises.
- This is useful reference for TNYX exercise search, but do not copy blindly. TNYX should generate/own its own exercise embeddings and version them server-side.

### 4.3 Cardio and Routine Data

`assets/cardio_activities.json`:

- Version: `1.0`
- Activities: 16.
- Examples: `running`, `walking`, `cycling`, `swimming`, `rowing`, `elliptical`, `hiit`, `jump_rope`.
- Fields include `id`, `name`, `category`, `met`, `supports_distance`, `supports_pace`, aliases, and default descriptions.

`assets/warmup-routine.json`:

- Has `text` groups: `upper_body`, `lower_body`, `full_body`, `best_10_overall`.
- Items map slots to exercise IDs and prescriptions.

`assets/cooldown-routine.json`:

- Has groups: `upper_body`, `lower_body`, `full_body`, `best_10_overall`.

### 4.4 Nutrition Source Data

`assets/data/top_sources.json`:

- `datasetVersion`: `3`
- Nutrient keys: 26.
- Examples: `vitamin_d`, `calcium`, `iron`, `potassium`, `sodium`, `vitamin_c`, `vitamin_a`, `vitamin_b12`, `magnesium`, `zinc`, `protein`, `fiber`, `carbs`, `selenium`.
- Items include localized names, serving labels, amounts, units, and diet type.

Interpretation:

- Some nutrition recommendation/reference data is bundled locally.
- It is not a full food database replacement.
- TNYX should keep canonical nutrition data in backend/database, with a versioned local cache for offline UX.

### 4.5 AI Prompts and Product Guide

`assets/prompts.json` top-level keys include:

- `nutrition_prompt`
- `nutrition_vision_prompt`
- `home_insights_prompt`
- `single_workout_prompt`
- `recipe_generation_prompt`
- `diet_plan_prompt`
- `smart_notification_prompt`
- `fio_chat_prompt`
- `fio_chat_prompt_v2`
- `workout_plan_prompt_v2`
- `workout_plan_prompt_v3`
- `top_sources_personalize_prompt`

`assets/fio_feature_guide.md`:

- Last updated inside file: `2026-04-13`
- Describes Reshape/Fio feature behavior, access model, coaching logic, and product positioning.

Important production note:

- Prompts in client assets are visible to anyone who decompiles the app.
- TNYX should not ship sensitive system prompts, provider keys, or action schemas in Flutter assets.
- Use backend-owned prompt templates and server-side AI orchestration for anything private or safety-critical.

### 4.6 Images, Translations, Audio, Video

Bundled Flutter asset folders:

- `assets/images`: 425 image files.
- `assets/videos`: one local video, `try_reshape.mp4`.
- `assets/audio`: sound effects and voice previews.
- `assets/translations`: `ar`, `de`, `en`, `es`, `fr`, `ja`, `ko`, `pt`, plus exercise/equipment translation JSONs.

Translation files include:

- `exercise_names.json`
- `exercise_translations.json`
- `exercise_category_names.json`
- `equipment_names.json`

Interpretation:

- UI copy, exercise names, equipment labels, onboarding artwork, workout card artwork, nutrition/cardio illustrations, and widget resources are largely bundled locally.
- Large exercise videos are remote.

## 5. Backend/API and External Connections

### 5.1 Confirmed Native/Manifest Integrations

| Surface | Purpose | Evidence |
|---|---|---|
| Firebase Core/Analytics/Messaging | push, analytics, app init | `AndroidManifest.xml`, `strings.xml`, `GeneratedPluginRegistrant.java` |
| Firebase project | project/bucket/app link signals | `strings.xml`, app link host `reshape-cf8c3.web.app` |
| RevenueCat / Google Billing | subscriptions/in-app purchase | manifest billing activities, `purchases_flutter` plugin |
| Health Connect | steps, sleep, exercise, nutrition, weight, hydration permissions | manifest permissions and `health` plugin |
| Samsung Health | read steps/exercise/sleep/nutrition/water through custom channel | `MainActivity.java` imports and `samsung_health` MethodChannel |
| Customer.io | push/customer messaging | manifest service + `customer_io` plugin |
| CleverTap | push/in-app/analytics | manifest services/providers + `clevertap_plugin` |
| Mixpanel | product analytics | `mixpanel_flutter` plugin |
| AppsFlyer | attribution/referrer | manifest backup rules + `appsflyer_sdk` |
| Facebook SDK | login/share/events | manifest strings/resources and `MainActivity` event code |
| TikTok Business SDK | event tracking | custom MethodChannel `com.nomatictech/tiktok_events` |
| Google Sign-In | authentication | `google_sign_in_android` plugin |
| InAppWebView / URL launcher | web views and external links | plugin registry |
| Flutter Downloader / Video Player | media download/playback | plugin registry and downloader file paths |
| WebRTC / Record / Camera | voice/video/audio/camera workflows | plugin registry and permissions |
| WorkManager / background service | background jobs/health foreground service | plugin registry and manifest service |

### 5.2 Custom Native MethodChannels

`MainActivity.java` registers 7 custom MethodChannels:

- `com.nomatictech/events`
- `app_launcher`
- `exact_alarm_settings`
- `keyboard_diagnostics`
- `audio_routing`
- `com.nomatictech/tiktok_events`
- `samsung_health`

Confirmed Samsung Health methods:

- `getSteps`
- `getStepEntries`
- `getExerciseEntries`
- `getSleepEntries`
- `requestPermissions`
- `checkPermissions`

The Samsung Health bridge reads:

- Steps aggregate.
- Hourly step entries.
- Exercise sessions.
- Sleep sessions/stages.
- Permission state for `activity`, `exercise`, `nutrition`, `sleep`, `steps`, `water`.

### 5.3 Private App Backend Count

Exact backend API count is not safely recoverable from this archive.

Reason:

- Flutter Dart source is not present.
- AOT native library (`libapp.so`) is not present.
- No `.apk`/`.aab` was present for deeper extraction.
- Native Java/resources show integrations and some external URLs, not the full Dart network layer.

What is safe to say:

- Reshape is not a single-API app.
- It connects to multiple provider/backend surfaces: Firebase, billing/RevenueCat, health providers, analytics/attribution, messaging/push, CDN/video, app links, social/event SDKs, and likely private AI/app backend from Flutter Dart.
- The private AI/nutrition/workout backend must not be inferred as absent just because exact REST routes are not visible in this extracted folder.

### 5.4 Network Security Risk

`res\xml\network_security_config.xml` has:

```xml
<base-config cleartextTrafficPermitted="true"/>
```

Production implication for TNYX:

- Do not copy this unless there is a hard requirement.
- TNYX production mobile should require HTTPS by default.
- If local/dev HTTP is needed, isolate it in debug-only network security config.

## 6. Local Storage and Data Format

### 6.1 Confirmed Persistence Capabilities

`GeneratedPluginRegistrant.java` registers:

- `sqflite_android`
- `shared_preferences_android`
- `flutter_secure_storage`
- `path_provider_android`
- `home_widget`
- `flutter_downloader`
- `workmanager_android`
- `health`
- `firebase_*`
- `purchases_flutter`

This means the app has Android-side capability for:

- SQLite databases through `sqflite_android`.
- Key-value preferences through `shared_preferences_android` / Android DataStore implementation.
- Encrypted/secure key-value storage through `flutter_secure_storage`.
- App-private file/cache directories through `path_provider_android`.
- Home widget state through `home_widget`.
- Downloaded media/files through `flutter_downloader`.
- Background work through WorkManager/background service.
- Health data read/write through Health Connect/native providers.

Important limitation:

- Exact table names, columns, and repository save/edit methods are in Dart code, which is not present.
- So we can identify storage mechanisms, but not the full local DB schema.

### 6.2 Likely Data Placement

Based on plugins and manifest, practical placement is:

| Data type | Likely storage | Notes |
|---|---|---|
| Auth/session tokens | `flutter_secure_storage` | Do not store secrets in plain preferences. |
| User settings/feature flags | `shared_preferences_android` / DataStore | Small key-value state. |
| Meals/workouts/water logs | SQLite through `sqflite_android` or backend cache | Exact schema not visible. |
| Active workout/temporary edits | SQLite/preference/app state | Exact Dart flow not visible. |
| Widget displayed values | `home_widget` state | Native Glance reads `HomeWidgetGlanceState`. |
| Downloaded videos/files | `flutter_downloader` app files/cache | Retention depends on app cleanup/cache policy. |
| Health samples | Health Connect / Samsung Health provider store | App reads/writes with user permission. |
| AI chat/action state | likely app DB/backend | Prompts/tool schemas bundled, actual calls hidden in Dart/backend. |
| Exercise catalog/search | bundled JSON/embedding + possible cache | `exercise.json` and `exercises-embedding.json` are local. |

### 6.3 Home Widget Flow

Native widget evidence:

- `CameraWidgetProvider` uses `MealWidgetGlanceAppWidget`.
- `WaterWidgetProvider` uses `WaterWidgetGlanceAppWidget`.
- Both read `es.antonborri.home_widget.HomeWidgetGlanceState`.
- `LogWaterAction` fires `homeWidgetCamera://addwater`.

Interpretation:

- Widget UI is rendered locally from widget state.
- Button/action opens app/deep link for logging.
- Widget is not directly rendering from backend response.
- Flutter side is expected to write widget values into local widget state and request widget update.

### 6.4 Local Retention

No fixed TTL/expiry was found in inspected native files.

Expected retention:

- SQLite/preferences/secure storage remain until uninstall, clear app data, logout/reset cleanup, migration cleanup, or explicit delete.
- Downloaded media/cache can be removed by app cleanup or OS cache pressure if stored as cache.
- Health Connect/Samsung Health data remains in provider store according to provider/user permissions, not only app sandbox.
- Bundled assets remain part of APK install and are replaced only on app update.

## 7. Data Save/Edit Flow

Exact meal/workout edit flow cannot be fully traced because Dart source is missing.

What is confirmed:

- App has local persistence stack for offline/durable state.
- App has health write permissions: nutrition, hydration, weight, steps, sleep.
- App has local widget update stack.
- App has background work stack.
- App has AI prompt/action design bundled, including tool gateway patterns for meal, water, workout, steps, sleep, weight, routines, and workout plan editing.

Inferred high-level flow:

1. User logs meal/workout/water/health data in Flutter UI.
2. Flutter state/repository layer writes local store and/or calls private backend.
3. Widget state may be updated through `home_widget`.
4. Backend/provider sync or AI processing happens through hidden Dart network layer and provider SDKs.
5. Health Connect/Samsung Health sync uses native/provider APIs with runtime permissions.

This inference is strong at architecture level, but exact routes/tables need Dart source or APK/AOT binary.

## 8. Direct Answers

### Images kahan se load hoti hain?

Mixed:

- Local bundled images: `assets/flutter_assets/assets/images` and native `res/drawable*`.
- Exercise video media: mostly remote Azure CDN URLs from `assets/exercise.json`.
- Some exercise/embed content: YouTube links.
- Widget icons/layout assets: local native resources.
- Downloader/video cache: runtime local files after download/playback.

So answer: Reshape mein images/videos sirf app se ya sirf website/backend se nahi hain. UI/illustrations local bundled hain; exercise videos CDN se aate hain; runtime cache local ho sakta hai.

### Data kaise save hota hai?

Confirmed mechanisms:

- SQLite via `sqflite_android`.
- Preferences/DataStore via `shared_preferences_android`.
- Secure storage via `flutter_secure_storage`.
- App files/cache via `path_provider_android`.
- Widget state via `home_widget`.
- Health provider stores via Health Connect/Samsung Health.

Exact app DB schema archive mein visible nahi hai.

### Data kaise edit hota hai?

Exact Dart repository/edit methods visible nahi hain.

Likely production pattern:

- User edit Flutter layer mein hota hai.
- Local DB/preferences update hoti hai.
- Backend sync/API call hota hai.
- Widget/health provider update optional path se hota hai.

TNYX ko isko normalized local DB + sync queue ke saath implement karna chahiye, hidden client prompt/actions par depend nahi karna chahiye.

### Backend se kitni API connected hai?

Safe answer:

- Exact private REST endpoint count is archive se nahi niklega.
- Confirmed 7 custom native channels.
- Confirmed 51 Flutter plugins.
- Confirmed multiple external/backend/provider surfaces: Firebase, RevenueCat/Billing, Customer.io, CleverTap, Mixpanel, AppsFlyer, Facebook, TikTok, Google Sign-In, Health Connect, Samsung Health, Azure CDN, YouTube, app links.

### Local data kab tak save rehta hai?

- Fixed time-based TTL nahi mila.
- App-private durable data uninstall/clear data/logout/delete/migration tak rahega.
- Cache/downloaded media OS/app cleanup se clear ho sakta hai.
- Health provider data app se bahar provider store mein rehta hai.

### Kis format mein save hota hai?

- Structured local data likely SQLite.
- Small settings/preferences key-value.
- Tokens secure key-value.
- Catalog/prompts JSON assets.
- Widget state HomeWidget/Glance state.
- Download/cache files for media.
- Provider health data Health Connect/Samsung Health format mein.

## 9. TNYX Implementation Guidance

TNYX ke liye Reshape se useful reference:

- Exercise catalog can be local cached, but canonical source backend/database hona chahiye.
- Exercise media URLs CDN-backed honi chahiye, not bundled for all videos.
- Embedding/search index useful hai, but versioned backend-owned index better hai.
- Home widgets should read local widget state, not call backend directly.
- Health Connect/Samsung Health should be permission-gated and provider-specific.
- AI coach action system useful pattern hai, but prompts/actions server-owned hone chahiye.

Recommended TNYX architecture:

1. `backend` owns AI orchestration, prompt templates, tool authorization, provider keys, and idempotent action APIs.
2. `database` owns canonical Supabase tables/RLS/RPCs.
3. `apps/flutter` owns offline-friendly UI, local DB/cache, sync queue, widget updates, and Health Connect client permissions.
4. Mobile client never stores service keys or private AI/provider secrets.
5. Any write action should be idempotent and auditable.
6. App should store local `client_id` for offline-created entities and map it to `server_id` after sync.
7. Use explicit `sync_status`: `local_only`, `pending_upload`, `synced`, `conflict`, `failed`.
8. Keep generated/cache files out of git.

Suggested TNYX local tables:

```text
local_exercise_catalog
- id
- server_id
- name
- primary_muscle
- secondary_muscles_json
- equipment_json
- media_json
- embedding_version
- updated_at

local_workouts
- id
- server_id
- user_id
- title
- started_at
- ended_at
- duration_seconds
- notes
- source
- sync_status
- created_at
- updated_at
- extended_meta_json

local_workout_exercises
- id
- workout_id
- exercise_id
- exercise_name_snapshot
- sort_order
- notes

local_workout_sets
- id
- workout_id
- workout_exercise_id
- set_order
- set_type
- weight
- weight_unit
- reps
- rir
- distance
- duration_seconds
- completed

local_meals
- id
- server_id
- user_id
- meal_type
- name
- logged_at
- photo_uri
- source
- sync_status
- ai_trace_id
- created_at
- updated_at

local_meal_ingredients
- id
- meal_id
- name
- quantity
- unit
- calories
- protein
- carbs
- fat
- fiber
- sugar
- water_ml

local_water_logs
- id
- server_id
- user_id
- amount_ml
- logged_at
- source
- sync_status

local_sync_queue
- id
- entity_type
- entity_id
- operation
- payload_json
- attempt_count
- last_error
- next_retry_at
- created_at
```

Suggested backend/Supabase ownership:

- `exercise_catalog`
- `exercise_media`
- `workouts`
- `workout_exercises`
- `workout_sets`
- `meals`
- `meal_ingredients`
- `water_logs`
- `health_samples`
- `nutrition_targets`
- `ai_conversations`
- `ai_actions`
- `ai_action_audit_log`
- `sync_mutations`

## 10. Risks / Notes

- Archive is decompiled and incomplete for Flutter Dart logic.
- Exact private API routes and local DB table names need original Flutter source or APK/AOT binary.
- `cleartextTrafficPermitted=true` is not production-safe by default.
- Client-bundled prompts are visible to reverse engineers; TNYX should keep private prompts server-side.
- Public Firebase/Google/Facebook client identifiers can appear in apps, but service/admin keys must never be shipped to mobile.
- Do not copy third-party catalog/media/prompt content into TNYX without license review.
