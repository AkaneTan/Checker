plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.akanework.checker"
    compileSdk = 34
    android.buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "org.akanework.checker"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "1.0.3"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("com.google.android.material:material:1.10.0")
}