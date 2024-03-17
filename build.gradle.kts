plugins {
    id("com.android.application") version "8.3.0" apply false
    id("com.android.library") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("androidx.navigation.safeargs") version "2.7.2" apply false
    kotlin("plugin.serialization") version "1.9.0" apply false
}

buildscript {
    repositories {
        google()
    }
}
repositories {
    google()
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}