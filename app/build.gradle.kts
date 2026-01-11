// =========================================================
// ANDROID APPLICATION MODULE - Gradle Kotlin DSL
// =========================================================

plugins {
    // -----------------------------------------------------
    // Plugin Android Application
    // -----------------------------------------------------
    id("com.android.application")

    // -----------------------------------------------------
    // Kotlin Android
    // -----------------------------------------------------
    id("org.jetbrains.kotlin.android")
}

// =========================================================
// ANDROID CONFIG
// =========================================================

android {
    namespace = "com.saile.utmconverter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.saile.utmconverter"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

        signingConfigs {
        create("release") {

            val storeFilePath =
                project.properties["RELEASE_STORE_FILE"] as String
            val storePasswordValue =
                project.properties["RELEASE_STORE_PASSWORD"] as String
            val keyAliasValue =
                project.properties["RELEASE_KEY_ALIAS"] as String
            val keyPasswordValue =
                project.properties["RELEASE_KEY_PASSWORD"] as String

            storeFile = file(storeFilePath)
            storePassword = storePasswordValue
            keyAlias = keyAliasValue
            keyPassword = keyPasswordValue
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// =========================================================
// DEPENDENCIES
// =========================================================

dependencies {

    // -----------------------------------------------------
    // ANDROIDX / UI
    // -----------------------------------------------------
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // -----------------------------------------------------
    // PROJ4J - MOTOR GEODÉSICO
    // -----------------------------------------------------
    implementation("org.locationtech.proj4j:proj4j:1.2.3")

    // -----------------------------------------------------
    // PROJ4J - EPSG DATABASE (OBRIGATÓRIO NO ANDROID)
    // -----------------------------------------------------
    implementation("org.locationtech.proj4j:proj4j-epsg:1.2.3")
}
