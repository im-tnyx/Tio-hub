# Tio-app — Wear OS Feature Plan

> **Status: Post-Production Roadmap**  
> **Current Progress:** [docs/WEAR_OS_PROGRESS.md](WEAR_OS_PROGRESS.md) (Live Tracking)  
> यह document production-ready Android app के बाद implement होगा।  
> अभी सिर्फ planning reference है।

Last updated: 2026-06-26

---

## 1. Competitive Analysis — Watch Apps

तीन leading apps का analysis करके Tio के लिए best combination निकाला गया है।

| Feature | Hevy | MyFitnessPal | Lyfta | **Tio (Planned)** |
|---|---|---|---|---|
| **Focus** | Workout only | Nutrition only | Workout only | ✅ Workout + Nutrition |
| **Wear OS** | ✅ Stable | ✅ Stable | ⚠️ Beta | ✅ Full |
| **Apple Watch** | ✅ | ✅ | ✅ | ✅ (Phase 2) |
| **Set/Rep logging** | ✅ | ❌ | ✅ | ✅ |
| **Food logging** | ❌ | ✅ | ❌ | ✅ |
| **Water logging** | ❌ | ✅ | ❌ | ✅ |
| **Rest Timer + Vibration** | ✅ | ❌ | ✅ | ✅ |
| **Heart Rate (live)** | ✅ | ❌ | ❌ | ✅ |
| **Tiles** | 1 | 3 | 1 | ✅ 4 (custom) |
| **Complications** | ✅ | ✅ 8 | ❌ | ✅ 6+ |
| **Health Connect** | ❌ | ✅ | ❌ | ✅ |
| **Set Types** | ✅ Drop/Warmup/Failure | ❌ | ❌ | ✅ |
| **Phone-free mode** | ✅ | ❌ | ❌ | ✅ |
| **Macro rings** | ❌ | ✅ Color rings | ❌ | ✅ |
| **Calorie quick-add** | ❌ | ✅ | ❌ | ✅ |

### Market Gap
> **कोई भी app watch पर Workout + Nutrition दोनों नहीं देता।**  
> Tio इस gap को fill करेगा — gym में workout log करो, cafeteria में meal log करो — सब एक wrist से।

---

## 2. Tio Watch App — Feature Set

### 2A. Workout Features (Hevy + Lyfta से बेहतर)

#### Active Workout Screen
- Current exercise name + muscle group icon
- Set number (e.g., Set 2 of 4)
- Previous performance hint (e.g., "Last time: 60kg × 10")
- Weight input (rotary/scroll input)
- Reps input
- Set type selector: Normal / Warm-up / Drop Set / Failure / Superset
- ✅ Complete Set button
- Next exercise preview (bottom strip)

> **✅ CONFIRMED:** Set Types और Phone-free mode दोनों Tio Watch में होंगे।  
> Phone App में पहले build होगा → फिर Watch पर automatically available।

#### Rest Timer
- Automatic timer start after set completion
- Circular progress ring countdown
- Wrist vibration at:
  - 10 seconds remaining (gentle buzz)
  - Timer end (strong buzz × 2)
- Skip / +30s / -15s buttons

#### Routine Selection
- Pre-built routines list (synced from phone)
- Quick start (last used routine)
- Scrollable exercise list before starting

#### Heart Rate
- Live BPM display during workout
- Average HR at workout end
- Syncs to Health Connect

#### Workout Tile
- Quick launch active workout
- Shows: today's workout name OR "No workout planned"
- Tap → instant workout start

---

### 2A-i. Set Types — Confirmed Feature ✅

Hevy में 5 set types हैं — Tio में सभी 5 होंगे:

| Set Type | Icon | Behavior |
|---|---|---|
| **Normal** | — | Standard working set |
| **Warm-up** | 🔥 | Volume में count नहीं होगा, lighter weight |
| **Drop Set** | ⬇️ | Previous set से कम weight, no rest |
| **Failure** | ❌ | Max effort set, failure तक |
| **Superset** | ⚡ | दो exercises back-to-back, no rest between |

**Phone App Prerequisite:**
```
Phone app में पहले बनाओ:
├── WorkoutSet domain model (type: SetType enum)
├── SetType: NORMAL, WARMUP, DROP_SET, FAILURE, SUPERSET
├── UI: Set row में type selector (chip/icon toggle)
└── Volume calculation: Warm-up sets exclude होंगी
```

**Watch पर behavior:**
- Set log screen पर circular type selector (swipe to change)
- Type icon set के साथ always visible
- Warm-up sets → lighter color indicator
- Drop set → "No rest" timer skip

---

### 2A-ii. Phone-Free Mode — Confirmed Feature ✅

Hevy watch phone के बिना काम करता है — Tio में भी यही होगा।

**कैसे काम करेगा:**
```
Phone Connected:     Watch को realtime sync मिलती है
Phone Disconnected:  Watch locally cached data use करता है
Workout done:        Watch local storage में save करता है
Phone reconnects:    Automatic sync होती है
```

**Watch पर locally store होगा:**
- Last synced routines (SQLite via Room on Watch)
- Recent 5 foods (for quick-log)
- Today's nutrition snapshot
- Active workout session data

**Sync strategy:**
- Phone → Watch: हर 15 minutes (background) + app open पर
- Watch → Phone: हर completed set पर (if connected) OR batch sync on reconnect
- Conflict resolution: Watch data = source of truth for active workout

**Phone App Prerequisite:**
```
Phone app में पहले:
├── Workout session local DB (Room)
├── Routine offline storage
└── WearSyncManager (data push to watch)
```

---

### 2B. Nutrition Features (MyFitnessPal से बेहतर)

#### Daily Dashboard (main watch screen)
- Circular macro rings (Calories, Protein, Carbs, Fat)
- Color-coded: Calories=primary, Protein=blue, Carbs=orange, Fat=yellow
- Remaining calories (large number)
- Water intake progress bar

#### Quick Log
- Calorie quick-add (number scroll)
- Recent foods list (top 5 from phone)
- Meal type: Breakfast / Lunch / Dinner / Snack
- Water quick-add: +250ml / +500ml buttons

#### Nutrition Tiles (3 tiles)
1. **Calorie Tile** — Remaining calories + macro ring summary
2. **Macro Tile** — Protein / Carbs / Fat progress bars
3. **Water Tile** — Water intake + goal progress

#### Complications (6)
- Calories remaining
- Protein grams
- Carbs grams
- Fat grams
- Water intake
- Workout streak

---

### 2C. Home Screen (Smart Context)

Watch smart context detect करेगा:

```
Time: 6–10 AM → Breakfast quick-log suggest करो
Time: 12–2 PM → Lunch quick-log suggest करो
Time: 5–9 PM  → Active workout screen show करो (if routine planned)
Post-workout   → Protein intake reminder
Bedtime        → Water goal remaining show करो
```

---

## 3. Technical Architecture

### 3A. Gradle Module Structure

```
Tio-app/
├── shared/                 ← ⭐ Pure Kotlin — Phone + Watch दोनों यहाँ से लेते हैं
│   └── src/.../com/tnyx/shared/
│       ├── workout/domain/ ← WorkoutSet, SetType, WorkoutSession, WorkoutRepository
│       └── nutrition/domain/ ← (future) NutritionLog, NutritionRepository
│
├── app/                    ← Android phone app (existing)
│   └── build.gradle.kts    ← implementation(project(":shared")) add होगा
│
├── wear/               ← NEW: Wear OS module
│   ├── build.gradle.kts   ← implementation(project(":shared")) — same models!
│   └── src/main/
│       ├── java/com/tnyx/wear/
│       │   ├── MainActivity.kt         ← WearActivity entry
│       │   ├── WearApp.kt              ← Hilt Application class
│       │   ├── presentation/
│       │   │   ├── workout/            ← Workout screens
│       │   │   │   ├── ActiveWorkoutScreen.kt
│       │   │   │   ├── RestTimerScreen.kt
│       │   │   │   ├── ExerciseLogScreen.kt
│       │   │   │   └── RoutinePickerScreen.kt
│       │   │   ├── nutrition/          ← Nutrition screens
│       │   │   │   ├── NutritionDashboardScreen.kt
│       │   │   │   ├── QuickLogScreen.kt
│       │   │   │   └── WaterLogScreen.kt
│       │   │   └── home/
│       │   │       └── WearHomeScreen.kt
│       │   ├── tile/                   ← Wear OS Tiles
│       │   │   ├── WorkoutTileService.kt
│       │   │   ├── CalorieTileService.kt
│       │   │   ├── MacroTileService.kt
│       │   │   └── WaterTileService.kt
│       │   ├── complication/           ← Watch face complications
│       │   │   └── TioComplicationService.kt
│       │   └── sync/                   ← Phone ↔ Watch data sync
│       │       ├── WearDataLayerService.kt
│       │       └── WearSyncManager.kt
│       └── res/
│
└── settings.gradle.kts     ← include(":shared", ":app", ":wear")
```

### 3B. Key Dependencies (wear/build.gradle.kts)

```kotlin
dependencies {
    // Wear OS UI
    implementation("androidx.wear.compose:compose-material:1.4.0")
    implementation("androidx.wear.compose:compose-navigation:1.4.0")
    implementation("androidx.wear.compose:compose-foundation:1.4.0")

    // Tiles
    implementation("androidx.wear.tiles:tiles:1.4.0")
    implementation("androidx.wear.tiles:tiles-material:1.4.0")

    // Complications
    implementation("androidx.wear.watchface:watchface-complications-data-source:1.2.1")

    // Phone ↔ Watch Communication
    implementation("com.google.android.gms:play-services-wearable:18.2.0")

    // Horologist (Google's Wear OS helper)
    implementation("com.google.android.horologist:horologist-compose-layout:0.6.17")
    implementation("com.google.android.horologist:horologist-health-composables:0.6.17")

    // Health Connect (steps, heart rate)
    implementation("androidx.health.connect:connect-client:1.1.0-alpha07")

    // Hilt for Wear
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-android-compiler:2.52")
}
```

### 3C. Phone ↔ Watch Communication Architecture

```
Phone App                    Watch App
─────────────────            ─────────────────
WearSyncManager  ←──────→   WearDataLayerService
      │                              │
      │  Wearable Data API           │
      │  (Bluetooth/WiFi)            │
      │                              │
  Sends:                        Receives:
  - Active routines             - Routine list
  - Today's nutrition data      - Macro/calorie data
  - User preferences            - Recent foods
  - Workout history             - Settings
      │                              │
  Receives:                     Sends:
  - Logged sets/reps            - Completed sets
  - Quick-add calories          - Food log entries
  - Water log entries           - Water intake
  - HR data                     - Heart rate data
```

### 3D. Data Flow — Workout Sync

```
1. User selects routine on Watch
2. Watch starts workout session (local state)
3. Each completed set → sent via DataClient.putDataItem()
4. Phone receives → saves to local DB
5. Workout end → full sync confirmation
6. Phone shows completed workout in history
```

### 3E. Data Flow — Nutrition Sync

```
1. Phone app → pushes today's nutrition snapshot to Watch every 15 min
   (via MessageClient.sendMessage())
2. Watch shows cached data (works offline too)
3. User quick-adds on Watch → sent to Phone via ChannelClient
4. Phone saves → updates Watch tile/complication via TileService.getUpdater()
```

---

## 4. UX Flow

### Workout Flow (Watch)
```
Watch Home
    └── "Start Workout" tap
        └── Routine Picker (scroll list)
            └── Routine selected
                └── Exercise 1
                    ├── Log Set (weight + reps + type)
                    │   └── ✅ Done → Rest Timer starts
                    │           └── Timer ends (vibrate)
                    │               └── Back to Log Set (Set 2...)
                    └── All sets done → Next Exercise
                        └── ... repeat ...
                            └── Workout Complete screen
                                └── Duration | Sets | Volume
                                    └── Sync to phone ✅
```

### Nutrition Flow (Watch)
```
Watch Home / Nutrition Tile
    └── Calorie ring tap
        └── Quick Log screen
            ├── Recent foods list
            │   └── Tap food → confirm quantity → log ✅
            ├── Quick-add calories
            │   └── Scroll number → confirm → log ✅
            └── Water log
                └── +250ml / +500ml / custom → log ✅
```

---

## 5. Watch Face Complications Layout

```
┌─────────────────────────────┐
│  🏃 Streak    💧 Water      │  ← Top complications
│                             │
│      [WATCH FACE]           │
│                             │
│  🔥 Cal Rem   🥩 Protein    │  ← Bottom complications
└─────────────────────────────┘
```

---

## 6. Implementation Phases

### Phase 1 — Workout Watch (3–4 weeks)
- [ ] `:wear` Gradle module setup
- [ ] Wear Compose UI scaffold
- [ ] Routine sync (Phone → Watch)
- [ ] Active workout screen
- [ ] Set logging
- [ ] Rest timer + vibration
- [ ] Workout Tile
- [ ] Workout data sync (Watch → Phone)

### Phase 2 — Nutrition Watch (2–3 weeks)
- [ ] Nutrition data sync (Phone → Watch)
- [ ] Daily dashboard screen (macro rings)
- [ ] Quick-add calories
- [ ] Water logging
- [ ] 3 Nutrition Tiles
- [ ] 6 Complications

### Phase 3 — Polish (1–2 weeks)
- [ ] Smart home screen context
- [ ] Heart rate integration (Health Connect)
- [ ] Heart rate during workout
- [ ] Offline mode (cached data)
- [ ] Always-on display support
- [ ] Performance optimization

### Phase 4 — Apple Watch (separate timeline)
- [ ] KMP/CMP migration के बाद
- [ ] watchOS app (SwiftUI)
- [ ] HealthKit integration

---

## 7. Compatibility

| Device | Support |
|---|---|
| Wear OS 3+ (Samsung Galaxy Watch 4+) | ✅ Full |
| Wear OS 2 | ⚠️ Tiles नहीं (basic app only) |
| Samsung Tizen (old watches) | ❌ |
| Apple Watch | 📋 Phase 4 |

---

## 8. Hevy vs MFP vs Lyfta vs Tio — Final Differentiator

```
Hevy Watch:      Workout ✅  |  Nutrition ❌  |  Heart Rate ✅
MFP Watch:       Workout ❌  |  Nutrition ✅  |  Heart Rate ❌
Lyfta Watch:     Workout ✅  |  Nutrition ❌  |  Heart Rate ❌
─────────────────────────────────────────────────────────────
Tio Watch:       Workout ✅  |  Nutrition ✅  |  Heart Rate ✅
```

> **Tio = Gym companion + Nutrition tracker — सब एक wrist पर।**  
> यह market में unique positioning है।

---

*यह document production launch के बाद implementation guide बनेगा।*  
*Tio Android app feature-complete और stable होने के बाद इस roadmap को execute करें।*
