# Tio-app — Testing & Build Guide

> Developer reference for writing and running tests across `:shared`, `:app`, and future `:wearapp` modules.

Last updated: 2026-06-24

---

## 1. Testing Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                  Test Pyramid                        │
│                                                      │
│              ┌──────────┐                           │
│              │    E2E   │  ← Instrumented UI (少)    │
│           ┌──┴──────────┴──┐                        │
│           │  Integration   │  ← Hilt + Room + Flow  │
│        ┌──┴────────────────┴──┐                     │
│        │   Unit Tests (많이)   │  ← :shared, VM, UC  │
│        └──────────────────────┘                     │
└─────────────────────────────────────────────────────┘
```

### Module-wise Test Responsibility

| Module | Test Type | Location | Framework |
|---|---|---|---|
| `:shared` | Unit (pure Kotlin) | `shared/src/test/` | JUnit 4 + Kotlin Test |
| `:app` ViewModel | Unit | `app/src/test/` | JUnit 4 + Turbine + MockK |
| `:app` UI | Instrumented | `app/src/androidTest/` | Compose UI Test + Espresso |
| `:app` DB | Instrumented | `app/src/androidTest/` | Room in-memory |
| `:wearapp` (future) | Unit + Instrumented | `wearapp/src/test/` | Same as :app |
| iOS (future) | Unit + UI | Xcode | XCTest + Swift Testing |

---

## 2. `:shared` Module Tests — Highest Priority ✅

`:shared` में pure Kotlin है — इसलिए JVM पर directly चलते हैं, **emulator की जरूरत नहीं।**

### 2A. Dependencies (shared/build.gradle.kts में already है)

```kotlin
dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)           // add करना होगा
    testImplementation(libs.kotlinx.coroutines.test) // add करना होगा
}
```

### 2B. `libs.versions.toml` additions

```toml
[versions]
kotlinTest = "2.0.21"       # Kotlin version के साथ match करो
coroutinesTest = "1.9.0"    # coroutines version के साथ match करो

[libraries]
kotlin-test = { group = "org.jetbrains.kotlin", name = "kotlin-test", version.ref = "kotlinTest" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
```

### 2C. Test folder structure

```
shared/src/
├── main/java/com/tnyx/shared/
│   ├── workout/domain/model/WorkoutSet.kt
│   └── workout/domain/model/WorkoutSession.kt
└── test/java/com/tnyx/shared/          ← यहाँ tests लिखो
    └── workout/domain/
        ├── model/
        │   ├── WorkoutSetTest.kt
        │   └── WorkoutSessionTest.kt
        └── usecase/
            └── CalculateVolumeUseCaseTest.kt
```

### 2D. Example Tests

```kotlin
// shared/src/test/java/com/tnyx/shared/workout/domain/model/WorkoutSetTest.kt
package com.tnyx.shared.workout.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkoutSetTest {

    // ✅ Normal set — volume में count होगा
    @Test
    fun `normal set counts towards volume`() {
        val set = WorkoutSet(
            id = "1", exerciseId = "e1", setNumber = 1,
            type = SetType.NORMAL, weightKg = 60.0, reps = 10
        )
        assertTrue(set.countsToVolume)
        assertEquals(600.0, set.volume)
    }

    // ✅ Warm-up set — volume में count नहीं
    @Test
    fun `warmup set does not count towards volume`() {
        val set = WorkoutSet(
            id = "2", exerciseId = "e1", setNumber = 1,
            type = SetType.WARMUP, weightKg = 40.0, reps = 12
        )
        assertFalse(set.countsToVolume)
        assertEquals(0.0, set.volume)
    }

    // ✅ Failure set — volume में count होगा
    @Test
    fun `failure set counts towards volume`() {
        val set = WorkoutSet(
            id = "3", exerciseId = "e1", setNumber = 3,
            type = SetType.FAILURE, weightKg = 80.0, reps = 6
        )
        assertTrue(set.countsToVolume)
        assertEquals(480.0, set.volume)
    }
}
```

```kotlin
// shared/src/test/java/com/tnyx/shared/workout/domain/model/WorkoutSessionTest.kt
package com.tnyx.shared.workout.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkoutSessionTest {

    private fun makeSet(type: SetType, weight: Double, reps: Int) = WorkoutSet(
        id = java.util.UUID.randomUUID().toString(),
        exerciseId = "e1", setNumber = 1,
        type = type, weightKg = weight, reps = reps, isCompleted = true
    )

    @Test
    fun `total volume excludes warmup sets`() {
        val session = WorkoutSession(
            id = "s1", routineId = null, routineName = "Push Day",
            startTimeMs = 0L,
            sets = listOf(
                makeSet(SetType.WARMUP, 40.0, 12),   // excluded
                makeSet(SetType.NORMAL, 80.0, 8),    // 640
                makeSet(SetType.NORMAL, 80.0, 8),    // 640
                makeSet(SetType.FAILURE, 80.0, 5),   // 400
            )
        )
        assertEquals(1680.0, session.totalVolumeKg)
    }

    @Test
    fun `active session has no endTime`() {
        val session = WorkoutSession(
            id = "s1", routineId = null, routineName = "Test",
            startTimeMs = 1000L, endTimeMs = null, sets = emptyList()
        )
        assertTrue(session.isActive)
    }
}
```

### 2E. Run Command

```bash
# Sirf :shared tests run karo — fast, no emulator
./gradlew :shared:test

# Verbose output ke saath
./gradlew :shared:test --info

# Specific test class
./gradlew :shared:test --tests "com.tnyx.shared.workout.domain.model.WorkoutSetTest"
```

---

## 3. `:app` Unit Tests (ViewModel + UseCase)

### 3A. Missing Dependencies — add karo

`libs.versions.toml` में:
```toml
[versions]
mockk = "1.13.12"
turbine = "1.2.0"
coroutinesTest = "1.9.0"

[libraries]
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
```

`app/build.gradle.kts` में:
```kotlin
testImplementation(libs.mockk)
testImplementation(libs.turbine)
testImplementation(libs.kotlinx.coroutines.test)
```

### 3B. ViewModel Test Example

```kotlin
// app/src/test/java/com/tnyx/core/ui/shell/ShellViewModelTest.kt
package com.tnyx.core.ui.shell

import app.cash.turbine.test
import com.tnyx.core.ui.shell.presentation.action.ShellAction
import com.tnyx.core.ui.shell.presentation.state.ShellTab
import com.tnyx.core.ui.shell.presentation.view_model.ShellViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class ShellViewModelTest {

    private val viewModel = ShellViewModel()

    @Test
    fun `tab selection updates uiState`() = runTest {
        viewModel.uiState.test {
            // Initial state
            val initial = awaitItem()
            assertEquals(ShellTab.HOME, initial.selectedTab)

            // Workout tab select karo
            viewModel.handleAction(ShellAction.TabSelected(ShellTab.WORKOUT))
            val updated = awaitItem()
            assertEquals(ShellTab.WORKOUT, updated.selectedTab)
        }
    }

    @Test
    fun `scroll opacity clamps between 0 and 1`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            // Over-scroll
            viewModel.handleAction(ShellAction.ScrollChanged(999f))
            assertEquals(1f, awaitItem().appBarOpacity)

            // Negative scroll
            viewModel.handleAction(ShellAction.ScrollChanged(-50f))
            assertEquals(0f, awaitItem().appBarOpacity)
        }
    }
}
```

### 3C. Test Folder Structure

```
app/src/test/java/com/tnyx/
├── core/
│   └── ui/shell/
│       └── ShellViewModelTest.kt
└── features/
    ├── nutrition/
    │   └── presentation/
    │       └── MealDiaryViewModelTest.kt  (future)
    └── workout/
        └── presentation/
            └── WorkoutViewModelTest.kt   (future)
```

---

## 4. `:app` Instrumented Tests (UI + DB)

Emulator/device चाहिए। इन्हें कम लिखो — critical flows के लिए।

### 4A. Compose UI Test Example

```kotlin
// app/src/androidTest/java/com/tnyx/core/ui/shell/TnyxShellTest.kt
package com.tnyx.core.ui.shell

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.tnyx.core.theme.TnyxThemeProvider
import com.tnyx.core.ui.shell.presentation.shell.TnyxShell
import org.junit.Rule
import org.junit.Test

class TnyxShellTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `bottom nav shows all 5 tabs`() {
        composeTestRule.setContent {
            TnyxThemeProvider {
                // Shell render karo minimal state ke saath
            }
        }
        // 5 tab items visible हैं
        composeTestRule.onAllNodesWithTag("bottom_nav_item")
            .assertCountEquals(5)
    }
}
```

### 4B. Room DB Instrumented Test Example

```kotlin
// app/src/androidTest/java/com/tnyx/features/workout/WorkoutDaoTest.kt
package com.tnyx.features.workout

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class WorkoutDaoTest {

    private lateinit var db: TnyxDatabase
    private lateinit var dao: WorkoutDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(     // ← In-memory: fast, no cleanup needed
            ApplicationProvider.getApplicationContext(),
            TnyxDatabase::class.java
        ).build()
        dao = db.workoutDao()
    }

    @After
    fun teardown() { db.close() }

    @Test
    fun insertAndRetrieveSession() = runTest {
        // Test will be added when WorkoutDao is implemented
    }
}
```

### 4C. Run Commands

```bash
# Unit tests (no emulator)
./gradlew :app:test

# Instrumented tests (emulator/device chahiye)
./gradlew :app:connectedAndroidTest

# Specific test
./gradlew :app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.tnyx.core.ui.shell.TnyxShellTest
```

---

## 5. iOS Tests (Phase 4 — Post KMP Migration)

> अभी iOS नहीं है। KMP + Compose Multiplatform migrate होने के बाद यह section activate होगा।

### 5A. `:shared` Module iOS Tests (future)

KMP migrate होने के बाद `:shared` में `iosTest` source set आएगा:

```kotlin
// shared/build.gradle.kts (future KMP version)
kotlin {
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        // iosTest — platform-specific iOS tests (usually empty for pure logic)
    }
}
```

```bash
# iOS simulator par :shared tests run karo (future)
./gradlew :shared:iosSimulatorArm64Test
```

### 5B. iOS App Tests (future — Xcode)

```swift
// iosApp/iosAppTests/WorkoutSessionTests.swift
import XCTest
@testable import iosApp

final class WorkoutSessionTests: XCTestCase {

    func testSetTypeNormalCountsVolume() {
        // shared KMP code Swift se access hoga
        let set = WorkoutSet(
            id: "1", exerciseId: "e1", setNumber: 1,
            type: .normal, weightKg: 60.0, reps: 10,
            isCompleted: true
        )
        XCTAssertTrue(set.countsToVolume)
        XCTAssertEqual(set.volume, 600.0)
    }
}
```

```bash
# Xcode se run karo
xcodebuild test -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16'
```

---

## 6. CI/CD Build Plan (future)

### Recommended Pipeline (GitHub Actions)

```yaml
# .github/workflows/ci.yml (future)
name: CI

on: [push, pull_request]

jobs:
  shared-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21' }
      - run: ./gradlew :shared:test    # Fast — no emulator

  android-unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21' }
      - run: ./gradlew :app:test       # Fast — no emulator

  android-instrumented:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21' }
      - uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 35
          script: ./gradlew :app:connectedAndroidTest
```

---

## 7. What to Test — Priority Guide

### ✅ हमेशा test करो (`:shared`)
- `SetType` rules (volume counting)
- `WorkoutSession` calculations (totalVolume, completedSets)
- Future: Nutrition calorie math, macro calculations
- Future: UseCase business logic

### ✅ ViewModel tests (`:app`)
- State transitions (tab selection, scroll opacity)
- Action → State mapping
- Edge cases (empty state, error state)

### ⚠️ जरूरत पड़ने पर (instrumented)
- Critical navigation flows
- DB read/write
- Compose UI — only for complex custom widgets

### ❌ मत लिखो
- Trivial getters/setters
- Third-party library behavior (Room, Hilt)
- Pixel-perfect UI tests (flaky होते हैं)

---

## 8. Quick Reference

```bash
# सबसे fast — daily use
./gradlew :shared:test          # Pure Kotlin tests, no emulator
./gradlew :app:test             # ViewModel + UseCase tests

# Device/Emulator चाहिए
./gradlew :app:connectedAndroidTest

# सब एक साथ
./gradlew test connectedAndroidTest

# Report देखो
open app/build/reports/tests/testDebugUnitTest/index.html
```
