plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
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
            implementation(libs.coroutines.core)
            implementation(libs.datetime)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
        }

        androidMain.dependencies {
            implementation(platform("androidx.compose:compose-bom:2024.06.00"))
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.navigation.compose)
            implementation(libs.coil.compose)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.tooling)
            implementation("androidx.compose.material:material-icons-extended:1.6.8")

            implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
            implementation("com.google.firebase:firebase-functions")
            implementation("com.google.firebase:firebase-firestore")

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