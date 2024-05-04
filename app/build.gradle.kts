import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.google.devtools.ksp")
    id("com.android.application")
    id("com.google.firebase.crashlytics")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("kotlinx-serialization")
    kotlin("plugin.serialization")
}

android {
    val properties = Properties().apply {
        load(FileInputStream(File(rootProject.rootDir, "app.properties")))
    }

    compileSdk = 34

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    kotlinOptions {
        jvmTarget = "19"
    }

    defaultConfig {
        namespace = "de.ljz.talktome"
        applicationId = "de.ljz.talktome"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"
        resourceConfigurations += listOf("en", "de")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }

    buildTypes {
        getByName("release") {
          signingConfig = signingConfigs.getByName("debug")
        }
      named("debug") {
            isDebuggable = true

            buildConfigField("String", "BASE_URL", "\"${properties.getProperty("DEBUG_BASE_URL")}\"")
        }

        named("release") {
            isDebuggable = false
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))

            buildConfigField("String", "BASE_URL", "\"${properties.getProperty("RELEASE_BASE_URL")}\"")
        }
    }
    kapt {
        correctErrorTypes = true
    }
}

val okHttpVersion by extra("4.12.0")
val roomVersion by extra("2.6.1")
val composeVersion by extra("1.6.5")
val composeDestinationsVersion by extra("1.10.2")
val ktorVersion by extra("2.2.1")

dependencies {
    // KotlinX Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Compose destinations
    ksp("io.github.raamcosta.compose-destinations:ksp:$composeDestinationsVersion")
    implementation("io.github.raamcosta.compose-destinations:core:$composeDestinationsVersion")

    // Arrow
    implementation(platform("io.arrow-kt:arrow-stack:1.1.2"))
    implementation("io.arrow-kt:arrow-core")

    // For AppWidgets support
    implementation("androidx.glance:glance-appwidget:1.0.0")

    // For interop APIs with Material 3
    implementation("androidx.glance:glance-material3:1.0.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    // Dagger & Hilt
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Moshi
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.moshi:moshi-adapters:1.14.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // Ktor Client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Room
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-rxjava2:$roomVersion")
    implementation("androidx.room:room-rxjava3:$roomVersion")
    implementation("androidx.room:room-guava:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // Jetpack Compose
    implementation(project.dependencies.platform("androidx.compose:compose-bom:2024.05.00")) // FIXME - DO NOT UPDATE TO 2024.02 (No MaterialYou colors) - REMEMBER 2024.02.00
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // Google extensions
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.35.0-alpha")

    // Material
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.material3:material3:1.2.1") // FIXME Remember Version 1.1.2
    implementation("androidx.compose.material3:material3-window-size-class:1.2.1") // FIXME Remember Version 1.1.2

    // androidx
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.9.0")

    // Lifecycle
    implementation("android.arch.lifecycle:extensions:1.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Firebase
    implementation("com.google.firebase:firebase-analytics:22.0.0")
    implementation("com.google.firebase:firebase-crashlytics:19.0.0")
    implementation(project.dependencies.platform("com.google.firebase:firebase-bom:33.0.0"))

    // Other
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.23")
    implementation("com.android.billingclient:billing-ktx:6.2.1")
}
repositories {
    mavenCentral()
    google()
}