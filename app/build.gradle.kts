plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

val mapsApiKey: String = gradle.rootProject.file("local.properties")
    .readLines()
    .firstOrNull { it.startsWith("MAPS_API_KEY=") }
    ?.substringAfter("=")
    ?: ""

android {
    namespace = "com.example.diarys22387"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.diarys22387"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8" // Consider updating to the latest version
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi"
        )
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.core.ktx) // Use the version from the Version Catalog
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //implementation(libs.material.v1110) // Remove redundant entry, use material instead
    implementation(libs.material)
    implementation("androidx.compose.material3:material3-window-size-class:1.3.1")

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.firebase.bom)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)

    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)

    implementation(libs.coil.compose)

    implementation(libs.androidx.security.crypto)

    implementation(libs.accompanist.permissions)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit) // Use the version from the Version Catalog
    androidTestImplementation(libs.androidx.espresso.core) // Use the version from the Version Catalog
    androidTestImplementation(libs.ui.test.junit4)
}

apply(plugin = "com.google.gms.google-services")