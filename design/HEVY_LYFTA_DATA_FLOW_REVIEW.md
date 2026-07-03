# Hevy + Lyfta Data Storage and API Flow Review

Last reviewed: 2026-06-30
Scope: `G:\Tnyx-hub\archive\Hevy` and `G:\Tnyx-hub\archive\lyfta`
Status: Archive/reference analysis, not TNYX production architecture.

## 1. Executive Summary

Ye dono folders production source-of-truth nahi hain. Ye archived/reference Android app copies hain. Inka use TNYX workout planning ke liye reference ke roop mein karna sahi hai, direct copy/paste implementation ke roop mein nahi.

High-level result:

- Lyfta local-first model use karta hai: workout pehle local SQLite/Room database mein save hota hai, phir backend sync start hota hai.
- Lyfta backend connection mixed hai: old `webservice/*` OkHttp APIs + newer `https://api.lyftadev.com/v2/*` Retrofit APIs + socket chat + static image/CDN URLs.
- Lyfta exercise images mostly backend/static URL se aati hain, e.g. `https://apilyfta.com/static/GymvisualPNG/...`; Glide local image cache handle karta hai.
- Hevy React Native based app hai. Workout storage ke liye native Android bridge `WorkoutStorageModule` direct SQLite file use karta hai: app-private `BigStorage/workouts.sqlite`.
- Hevy old file/JSON storage bhi rakhta hai: `BigStorage/*.json`, including migration from `workouts.json` to SQLite.
- Hevy backend/API count exact safely bolna mushkil hai because REST JS bundle minified/bundled hai, lekin confirmed surfaces include Hevy API, CloudFront exercise media, Strava, Health Connect, Wear OS, analytics/crash/deeplink services.
- Dono apps ka important lesson: mobile app ko offline-capable local store + explicit sync queue chahiye. Backend ko canonical source of truth rakhna chahiye.

## 2. Files Inspected

TNYX repo governance:

- `G:\Tnyx-hub\README.md`
- `G:\Tnyx-hub\docs\DOC_INDEX.md`
- `G:\Tnyx-hub\docs\product\FOLDER_OWNERSHIP.md`

Lyfta archive:

- `G:\Tnyx-hub\archive\lyfta\Lyfta-1\app\src\main\java\com\lyfta\config\AppConnectionConfig.java`
- `G:\Tnyx-hub\archive\lyfta\Lyfta-1\app\src\main\java\com\lyfta\app\core\network\V2ApiConfig.java`
- `G:\Tnyx-hub\archive\lyfta\Lyfta-1\app\src\main\java\com\lyfta\app\core\network\auth\AuthApiProvider.java`
- `G:\Tnyx-hub\archive\lyfta\Lyfta-1\app\src\main\java\com\lyfta\app\feature\chat\data\ChatSocketIoService.java`
- `G:\Tnyx-hub\archive\lyfta\Lyfta-1\app\src\main\java\com\lyfta\server\BaseServerRequest.java`
- `G:\Tnyx-hub\archive\lyfta\Lyfta-1\app\src\main\java\com\lyfta\persistence\CommonDao.java`
- `G:\Tnyx-hub\archive\lyfta\Lyfta-1\app\src\main\java\com\lyfta\persistence\database\WorkoutDatabase_Impl.java`
- `G:\Tnyx-hub\archive\lyfta\Lyfta-1\app\src\main\java\com\lyfta\persistence\entities\*.java`
- `G:\Tnyx-hub\archive\lyfta\docs\active_workout_logging_flow_details.md`
- `G:\Tnyx-hub\archive\lyfta\docs\explore_exercises_screen_details.md`
- `G:\Tnyx-hub\archive\lyfta\docs\networking_api_architecture.md`

Hevy archive:

- `G:\Tnyx-hub\archive\Hevy\app\src\main\AndroidManifest.xml`
- `G:\Tnyx-hub\archive\Hevy\app\src\main\assets\index.android.bundle`
- `G:\Tnyx-hub\archive\Hevy\app\src\main\java\com\hevy\WorkoutStorageModule.java`
- `G:\Tnyx-hub\archive\Hevy\app\src\main\java\com\hevy\BigStorageModule.java`
- `G:\Tnyx-hub\archive\Hevy\app\src\main\java\com\hevy\FileUtils.java`
- `G:\Tnyx-hub\archive\Hevy\app\src\main\java\com\hevy\WearOSConnectorModule.java`
- `G:\Tnyx-hub\archive\Hevy\app\src\main\java\com\hevy\healthconnect\HealthConnect.java`

## 3. Lyfta: Backend/API Connections

### 3.1 Primary API Surfaces

| Surface | Base / path | Purpose | Evidence |
|---|---|---|---|
| Legacy backend | `https://lyftadev.com/webservice/*` | Auth, profile, feed, workout CRUD, sync, settings, comments, measurements, templates, collections | `AppConnectionConfig.java`, `BaseServerRequest.java`, `server/requests/*` |
| V2 REST backend | `https://api.lyftadev.com/v2/*` | Newer typed APIs for workout media/sync, feed, explore, notifications, chat, support | `V2ApiConfig.java`, `*V2Api.java` |
| V2 auth backend | `https://api.lyftadev.com/v2/auth/*` | logout, token migration, token refresh | `AuthApiProvider.java`, `AuthApi.java` |
| Socket backend | `https://api.lyftadev.com/v2/socket.io` | realtime chat events | `ChatSocketIoService.java` |
| Static/CDN | `https://apilyfta.com/static/GymvisualPNG/*`, `https://cdnlyfta.com/*` | exercise images/media/static files | `ExerciseRepo.java`, `BuildConfig.java`, docs |
| Web bridge | `https://my.lyfta.app/*` | support/mobile web bridge/profile/settings links | `WebBridgeHelper.java`, `LinkType.java` |
| External APIs | `https://www.strava.com/api/v3/*` | Strava OAuth/activity publish | `StravaUtils.java` |

### 3.2 API Count

Lyfta mein exact total endpoint count high hai because old `BaseServerRequest.createRequest(...)` pattern many request classes mein use hota hai.

Confirmed count from typed Retrofit interfaces inspected:

- V2 typed endpoints: at least 41.
- Legacy `webservice/*` endpoints: dozens. Examples include `webservice/login`, `webservice/registrationV2`, `webservice/addWorkoutV4`, `webservice/updateWorkout`, `webservice/deleteWorkout2`, `webservice/syncAllExcercises5`, `webservice/syncAllWorkoutTemplatesCollectionModel`, `webservice/syncAllWorkoutCollections`, `webservice/getMeasurements`, `webservice/addMeasurementLog`, `webservice/setUnitSettings`, `webservice/viewPrivacySettings`, etc.
- External integrations add more calls: Strava OAuth/activity, Health Connect native APIs, support web pages, static media fetches.

Practical answer: Lyfta backend se sirf 1 API nahi judi hai. Ye multiple API generations ka app hai: legacy REST, V2 REST, auth REST, socket, CDN/static, aur external integrations.

## 4. Lyfta: Data Save/Edit Flow

### 4.1 Workout Save Flow

Observed flow:

1. User workout screen mein set/weight/reps/duration enter karta hai.
2. Active state `BaseLogWorkout` + `BaseLogWorkoutVM` mein maintain hoti hai.
3. In-progress workout local DB table `workout_in_progress_entity` mein persist ho sakta hai.
4. Finish par `FinishWorkoutFragment` final metadata collect karta hai: RPE, notes, workout date, media, Strava/Health status.
5. `saveWorkoutOffline` background worker local SQLite/Room tables mein write karta hai.
6. `DataSyncManager.sendWorkoutDone()` local changes backend sync ke liye trigger karta hai.
7. Successful finish ke baad `CommonDao.deleteAllWorkoutsInProgress()` in-progress workspace clean karta hai.

Important production behavior: network fail hone par workout pehle local DB mein hota hai. Backend sync later retry/refresh path se ho sakta hai.

### 4.2 Main Local Tables / Entities

Lyfta Room/SQLite style persistence use karta hai. Important tables/entities:

| Entity/table | Role |
|---|---|
| `performed_workout_entity` | Completed workout snapshot. Includes `media`, `extendedMetaJson`, `pendingMediaJson`. |
| `workout_in_progress_entity` | Draft/current workout state. Includes pause state and pending media. |
| `workout_sets` | Individual set rows: weight, reps, RIR, distance, duration, set type, weight unit. |
| `workout_exercises` | Exercises inside a workout. |
| `timeline` | Calendar/feed/history row with duration, total volume, weight unit, date. |
| `exercise_entity` | Exercise catalog; includes image URL/path and muscle/equipment mapping. |
| `workout_template_entity` | Saved routines/templates. |
| `collection_entity` | Workout collections/folders/program structures. |
| `goals_entity` | Workout/fitness goals. |
| `pinned_note_entity` | Exercise/workout notes. |

### 4.3 Edit Flow

Edits are local-first:

- Workout template edit: `insertOrUpdateTemplate(...)` inserts/replaces local template row.
- Timeline workout replacement: `replaceTimelineWorkout(...)` deletes old timeline/exercise/set rows for `workoutId`, then reinserts updated `TimelineEntity`, `WorkoutExercisesEntity`, and `WorkoutSetsEntity` rows.
- Exercise favorite/muscle changes have DAO update methods such as `updateExerciseFavorite(...)` and `updateExerciseMuscles(...)`.
- Server edit/delete calls exist through legacy APIs such as `webservice/updateWorkout`, `webservice/deleteWorkout2`, template/collection sync APIs, and V2 workout media/meta APIs.

### 4.4 Local Retention

No fixed TTL was found in inspected files.

Data normally remains local until one of these happens:

- user uninstalls app;
- user clears app data;
- app logout/reset/sync flow deletes local tables;
- full resync calls `deleteAllDataForSync(...)` and replaces local data from backend;
- user deletes a workout/template/exercise/custom item;
- DB migration or app cleanup explicitly changes storage.

So answer: local data time-based automatic expiry se nahi dikha; app sandbox persistence hai, with explicit sync/delete/reset paths.

### 4.5 Data Format

Lyfta local format mixed hai:

- SQLite/Room tables for structured workout, exercise, timeline, template, collection data.
- JSON strings inside columns such as `exercises`, `media`, `extendedMetaJson`, `pendingMediaJson`.
- `SharedPreferences` for settings/tokens/small flags; V2 token storage uses encrypted preferences with fallback SharedPreferences.
- Exercise images are URL strings in DB and loaded through Glide cache.

## 5. Lyfta: Images Kahan Se Load Hoti Hain

Exercise thumbnails/images ka main flow:

1. Backend sync API `webservice/syncAllExcercises5` exercise JSON deta hai.
2. JSON field `image_name` DB ke `exercise_entity.image` / model image field mein store hota hai.
3. Example value: `https://apilyfta.com/static/GymvisualPNG/04331101-Dumbbell-Straight-Arm-Pullover_Chest-FIX_small.png`.
4. UI adapter `ImageHelper.setExerciseImage(...)` / Glide se image load karta hai.
5. Glide remote image download + memory/disk cache handle karta hai.
6. Offline first launch fallback ke liye raw resource `res/raw/exercises` referenced hai.

Iska matlab screenshot mein jo exercise illustration dikh rahi thi, wo usually app APK ke drawable se nahi, backend/static URL se aane wali image hoti hai, aur local cache mein save ho sakti hai. Lekin body-muscle overlay type assets app `res/drawable` se local load ho sakte hain.

## 6. Hevy: Backend/API Connections

Hevy archive React Native app hai. Java/Kotlin side native modules readable hain, lekin remote REST ka major part `index.android.bundle` mein minified JS ke andar hai. Isliye exact endpoint count safely claim nahi karna chahiye without deeper reverse engineering.

Confirmed surfaces:

| Surface | Purpose | Evidence |
|---|---|---|
| Hevy API | App backend calls; bundle contains `api.hevyapp.com` strings including Health Connect workout result endpoint | `index.android.bundle` |
| CloudFront CDN | Exercise thumbnails/assets/videos | `index.android.bundle`, resource keep list |
| Strava | OAuth / integration | manifest package query, bundle strings/resources |
| Health Connect | write/read workouts, calories, heart rate, body fat, weight | `AndroidManifest.xml`, `HealthConnect.java` |
| Wear OS | phone-watch message sync | `WearOSConnectorModule.java`, `WearListenerService` manifest entry |
| Firebase/FCM | push notifications | `AndroidManifest.xml` |
| RevenueCat/Billing | subscription/purchases | `AndroidManifest.xml` |
| Analytics/crash/deeplink | Amplitude, Sentry, Branch/links | `index.android.bundle`, manifest |
| Web/share | `hevy.com`, `hevycoach.com`, app links | `AndroidManifest.xml`, bundle strings |

Practical answer: Hevy backend/API integration count exact nahi bol sakte, but app is connected to multiple backend/provider surfaces, not a single backend.

## 7. Hevy: Local Data Save/Edit Flow

### 7.1 Workout SQLite Storage

Hevy ka concrete native storage module:

- Module name: `WorkoutStorage`
- File: `G:\Tnyx-hub\archive\Hevy\app\src\main\java\com\hevy\WorkoutStorageModule.java`
- Storage file: app-private `BigStorage/workouts.sqlite`
- Table created by native code:

```sql
CREATE TABLE IF NOT EXISTS workouts (
  id TEXT PRIMARY KEY,
  end_time INTEGER,
  json TEXT
)
```

Save/edit behavior:

- JS side workout list JSON string native bridge ko bhejta hai.
- `storeWorkouts(...)` JSON array parse karta hai.
- Har workout object se `id`, `end_time`, aur full JSON snapshot nikala jata hai.
- `insertWithOnConflict(..., CONFLICT_REPLACE)` use hota hai, so same `id` edit/update par row replace ho jati hai.
- `fetchWorkouts()` rows ko `end_time DESC` order mein return karta hai.
- `deleteWorkouts(...)` selected ids delete karta hai.
- `clearWorkouts()` table drop karta hai.

### 7.2 BigStorage JSON Storage

Hevy also uses `BigStorage` native module:

- Module name: `BigStorage`
- Storage path: app-private `BigStorage/<key>.json`
- `FileUtils.storeJson(...)` uses Android `AtomicFile`, so write failure par partial file risk lower hota hai.
- `fetchJson(...)` returns stored JSON string or `null` if file missing.
- Migration exists from older app data path `workouts.json` to `BigStorage/workouts.json`.
- Workout migration then moves old `workouts.json` content into SQLite and deletes old JSON file.

### 7.3 Hevy Data Format

Hevy local format mixed hai:

- `BigStorage/workouts.sqlite` for completed workout snapshots.
- `workouts` table stores full workout as JSON string, not normalized set/exercise relational schema.
- `BigStorage/<key>.json` for large JSON blobs like routines/cache/config-like data.
- `SharedPreferences` for widget preferences and some small native state.
- React Native/JS side likely uses additional key-value stores/caches, visible through bundle storage keys.
- Media and exercise assets are a mix of bundled drawable assets, local cache, and CloudFront URLs.

### 7.4 Hevy Local Retention

No fixed TTL was found in inspected native storage code.

Data remains in app-private storage until:

- user uninstalls app;
- user clears app data;
- app explicitly calls `deleteWorkouts(...)` or `clearWorkouts()`;
- app migration deletes old JSON after copying to SQLite;
- logout/reset/JS-side cleanup removes app data;
- OS removes cache-only media, if stored in cache rather than app data.

Workout SQLite itself is durable app data, not short-lived cache.

## 8. Hevy vs Lyfta Storage Difference

| Area | Lyfta | Hevy |
|---|---|---|
| App stack | Native Android / decompiled Kotlin-Java | React Native + native Android modules |
| Workout local DB | Room/SQLite normalized tables | SQLite table with JSON snapshot per workout |
| Current workout | `workout_in_progress_entity` + ViewModel/service | JS state + native/BigStorage signals in bundle |
| Completed workout | `performed_workout_entity`, `timeline`, `workout_sets`, `workout_exercises` | `BigStorage/workouts.sqlite`, table `workouts(id,end_time,json)` |
| Routines/templates | SQLite entities + JSON columns | BigStorage JSON / JS store signals |
| Exercise images | backend/static URLs + Glide cache; some local drawables | bundled drawables + CloudFront assets + cache |
| Sync model | local first, then backend sync | local storage + backend/provider sync; exact JS sync flow needs deeper bundle reverse engineering |
| Local retention | app data persists until clear/uninstall/delete/resync | app data persists until clear/uninstall/delete/migration/reset |

## 9. TNYX Backend Planning Recommendation

TNYX ke liye direct provider API mobile app mein lagana galat pattern hoga, especially agar API key/secret involved ho.

Recommended production architecture:

1. Backend canonical source of truth ho.
2. Mobile/Flutter local-first workout engine rakhe: active workout draft + completed workout + sync queue.
3. Local DB normalized rakho for analytics: `workouts`, `workout_exercises`, `workout_sets`, `exercise_catalog`, `media`, `sync_queue`.
4. Flexible non-critical fields ke liye JSON columns allowed: `extended_meta`, `provider_payload`, `pending_media`.
5. Backend APIs idempotent hon: repeated sync same result de.
6. Every local mutation ko `sync_status` do: `local_only`, `pending_upload`, `synced`, `conflict`, `failed`.
7. Server IDs aur client IDs dono rakho: offline-created workout ka temporary client UUID backend sync ke baad server ID se map ho.
8. Exercise image/catalog ko backend-owned catalog endpoint se serve karo; static CDN URL DB mein store ho sakta hai.
9. Provider secrets/service keys mobile app mein kabhi mat rakho.
10. Health Connect/Google Fit/Strava write flows user-permission based native/client side ho sakte hain, but token exchange/secret operations backend-owned hone chahiye.

Suggested minimum local schema for TNYX mobile:

```text
local_workouts
- id/client_id
- server_id nullable
- user_id
- title
- started_at
- ended_at nullable
- duration_seconds
- notes
- rpe nullable
- source
- sync_status
- created_at
- updated_at
- extended_meta_json

local_workout_exercises
- id/client_id
- workout_id
- exercise_id
- exercise_name_snapshot
- sort_order
- notes
- superset_group_id nullable

local_workout_sets
- id/client_id
- workout_id
- workout_exercise_id
- set_order
- set_type
- weight
- weight_unit
- reps
- rir nullable
- distance nullable
- duration_seconds nullable
- completed

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

## 10. Direct Answers

### Data kaise save hota hai?

- Lyfta: Room/SQLite tables mein normalized workout data save hota hai; media/meta/exercises ke kuch fields JSON strings hain.
- Hevy: completed workouts `BigStorage/workouts.sqlite` mein JSON snapshot ke roop mein save hote hain; extra large data `BigStorage/*.json` files mein bhi save hota hai.

### Data kaise edit hota hai?

- Lyfta: DAO insert/update/delete methods local DB update karte hain; backend sync APIs later changes push/pull karte hain.
- Hevy: `WorkoutStorage.storeWorkouts(...)` same `id` par SQLite `INSERT OR REPLACE` karta hai; delete/clear methods bhi native module mein hain.

### Backend se kitni API connected hain?

- Lyfta: at least 41 typed V2 Retrofit endpoints + dozens of legacy `webservice/*` endpoints + socket + CDN/static + external integrations.
- Hevy: exact endpoint count minified bundle ke kaaran reliable nahi; confirmed multiple surfaces hain: Hevy API, CloudFront, Strava, Health Connect, Wear OS, Firebase, analytics/crash/deeplink, billing/subscription.

### Local data kab tak save rehta hai?

- Dono apps mein inspected code se fixed TTL nahi mila.
- App-private durable data uninstall, clear app data, explicit delete/reset, migration cleanup, ya resync delete tak rahta hai.
- Image/media cache OS/app cache policy se clear ho sakta hai; structured workout DB durable hota hai.

### Kis format mein save hota hai?

- Lyfta: SQLite/Room tables + JSON string columns + SharedPreferences.
- Hevy: SQLite table with full JSON workout snapshots + BigStorage JSON files + SharedPreferences.

## 11. Risks / Notes

- Archive code decompiled/minified hai. Naming and control flow partially obfuscated ho sakte hain.
- Hevy JS sync flow exact trace karne ke liye deeper Hermes/React Native bundle reverse engineering chahiye.
- Lyfta endpoint list is already large; total legacy endpoint count exhaustive nahi nikala gaya because request planning-level analysis ka tha.
- Kisi bhi third-party API key/token ko client app mein expose nahi karna chahiye. Backend proxy/provider-service model use karo.