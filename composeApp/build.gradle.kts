plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
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
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.coroutines.core)
            implementation(libs.datetime)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.navigation.compose)
            implementation(libs.coil.compose)
            implementation(libs.coroutines.android)
            implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
            implementation("com.google.firebase:firebase-firestore")
            implementation("com.google.firebase:firebase-auth:22.3.1")
            implementation("com.google.firebase:firebase-functions")
        }
    }
}

kotlin.sourceSets.named("commonMain") {
    kotlin.srcDirs(
        "../shared/src/commonMain/kotlin",
        "src/commonMain/kotlin"
    )
}

kotlin.sourceSets.named("androidMain") {
    kotlin.srcDirs(
        "../androidApp/src/main/kotlin/com/beautyplanner/client/android/data",
        "../androidApp/src/main/kotlin/com/beautyplanner/client/android/navigation",
        "../androidApp/src/main/kotlin/com/beautyplanner/client/android/ui/booking",
        "../androidApp/src/main/kotlin/com/beautyplanner/client/android/ui/common",
        "../androidApp/src/main/kotlin/com/beautyplanner/client/android/ui/discover",
        "../androidApp/src/main/kotlin/com/beautyplanner/client/android/ui/main",
        "../androidApp/src/main/kotlin/com/beautyplanner/client/android/ui/master",
        "../androidApp/src/main/kotlin/com/beautyplanner/client/android/ui/review",
        "src/androidMain/kotlin"
    )
}

kotlin.sourceSets.named("iosMain") {
    kotlin.srcDir("src/iosMain/kotlin")
}

kotlin.sourceSets.named("commonTest") {
    kotlin.srcDir("src/commonTest/kotlin")
}

android {
    namespace = "com.beautyplanner.client.composeapp"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets["main"].manifest.srcFile("../androidApp/src/main/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("../androidApp/src/main/res")
}
