pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Tnyx"
include(":app")
include(":wear")         // Wear OS Watch App
include(":shared")       // Pure Kotlin — Phone App + Watch App दोनों use करते हैं
include(":core")         // Design System, UI Components & Shell
include(":features:workout")
include(":features:nutrition")
include(":features:onboarding")
include(":features:auth")
include(":features:profile")
include(":features:settings")
include(":features:progress")
