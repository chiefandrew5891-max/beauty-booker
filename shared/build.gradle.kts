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
        }
        androidMain.dependencies {
            implementation(platform(libs.compose.bom))
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.navigation.compose)
            implementation(libs.coil.compose)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.tooling)
            implementation("androidx.compose.material:material-icons-extended")

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
