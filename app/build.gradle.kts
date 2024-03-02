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
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        namespace = "com.lnzpk.chat_app"
        applicationId = "com.lnzpk.chat_app"
        minSdk = 31
        targetSdk = 34
        versionCode = 61
        versionName = "0.4"
        resourceConfigurations += listOf("de", "en")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        named("debug") {
            isDebuggable = true

            buildConfigField("String", "BASE_URL", "\"${properties.getProperty("DEBUG_BASE_URL")}\"")
        }

        named("release") {
            isDebuggable = false
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }
    kapt {
        correctErrorTypes = true
    }
}

val okHttpVersion by extra("4.12.0")

dependencies {
    implementation("androidx.compose.material3:material3-android:1.2.0")
    val roomVersion = "2.6.1"
    val compose_version = "1.6.2"
    val composeDestinationsVersion = "1.10.1"

    // KotlinX Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    // Compose destinations
    implementation("io.github.raamcosta.compose-destinations:core:$composeDestinationsVersion")
    ksp("io.github.raamcosta.compose-destinations:ksp:$composeDestinationsVersion")

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

    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-android-compiler:2.51")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.moshi:moshi-adapters:1.12.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("androidx.compose.material:material-icons-extended:$compose_version")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.ui:ui-tooling:$compose_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    implementation("androidx.databinding:viewbinding:8.3.0")

    implementation("com.android.billingclient:billing-ktx:6.1.0")

    // Room
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-rxjava2:$roomVersion")
    implementation("androidx.room:room-rxjava3:$roomVersion")
    implementation("androidx.room:room-guava:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(project.dependencies.platform("androidx.compose:compose-bom:2024.01.00")) // FIXME - DO NOT UPDATE TO 2024.02 (No MaterialYou colors)
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.21")

    // Extensions = ViewModel + LiveData
    implementation("android.arch.lifecycle:extensions:1.1.1")
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.3")
    implementation("com.google.firebase:firebase-database-ktx:20.3.1")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.7.0")

    implementation("com.google.accompanist:accompanist-swiperefresh:0.35.0-alpha")
    implementation("androidx.compose.material3:material3:1.1.2") // FIXME Remember Version 1.1.2
    implementation("androidx.compose.material3:material3-window-size-class:1.1.2") // FIXME Remember Version 1.1.2

    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(project.dependencies.platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference-ktx:1.2.1")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.firebase:firebase-analytics:21.5.1")
    implementation("net.yslibrary.keyboardvisibilityevent:keyboardvisibilityevent:3.0.0-RC3")
    implementation("com.google.firebase:firebase-crashlytics:18.6.2")
}
repositories {
    mavenCentral()
    google()
}