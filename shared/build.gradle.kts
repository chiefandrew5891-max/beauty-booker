plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
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
            implementation(libs.coroutines.android)
        }
    }
}

android {
    namespace = "com.beautyplanner.client.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}
