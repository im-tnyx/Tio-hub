/**
 * :shared — Pure Kotlin module
 *
 * यह module phone app (:app) और watch app (:wearapp) दोनों use करते हैं।
 * कोई Android-specific dependency नहीं है यहाँ।
 *
 * KMP migration के समय सिर्फ plugin बदलनी होगी:
 *   kotlin("jvm")  →  kotlin("multiplatform")
 */
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Coroutines — Flow/StateFlow for domain layer
    implementation(libs.kotlinx.coroutines.core)

    // Serialization — @Serializable models (API/DB ke liye)
    implementation(libs.kotlinx.serialization.json)

    // Unit Testing — JVM par directly chalta hai, emulator ki zaroorat nahi
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
