plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.github.ttnguyen2505.emptyactivity"
    compileSdk {
        version = release(version = 36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "io.github.ttnguyen2505.emptyactivity"
        minSdk = 31
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
//    Compose and relative libraries version management
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

//    AndroidX core and lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

//    Compose Preview tool
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

//    Compose foundation
    implementation(libs.androidx.compose.foundation)

//    Compose navigation
    implementation(libs.androidx.navigation.compose)

//    JSON serialization library, works with the Kotlin serialization plugin
    implementation(libs.kotlinx.serialization.json)

//    Local unit test
    testImplementation(libs.junit)

//    Instrumented UI test
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}