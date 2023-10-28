@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
    }
}

include(":app")