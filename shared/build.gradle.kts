plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)

            implementation(libs.coroutines.core)
            implementation(libs.datetime)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
        }

        androidMain.dependencies {
            implementation(libs.navigation.compose)
            implementation(libs.coil.compose)
            implementation(libs.compose.activity)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.tooling)
            implementation("androidx.compose.material:material-icons-extended:1.6.8")

            implementation("com.google.firebase:firebase-functions:21.0.0")
            implementation("com.google.firebase:firebase-firestore:25.1.2")

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
        }
    }
}

android {
    namespace = "com.beautyplanner.client.shared"
    compileSdk = 34

    buildFeatures {
        compose = true
    }

    defaultConfig {
        minSdk = 24
    }
}