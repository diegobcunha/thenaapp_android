plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
    alias(libs.plugins.stability.analyzer)
    alias(libs.plugins.screenshot)
}

android {
    namespace = "com.diegocunha.thenaapp.coreui"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    experimentalProperties["android.experimental.enableScreenshotTest"] = true
    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        screenshotTests {
            imageDifferenceThreshold = 0.05f // Example tolerance
        }
    }
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.activity.compose)
    api(libs.koin.android)
    api(libs.androidx.navigation3.ui)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.koin.androidx.compose)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.coil.compose)
    api(libs.kotlinx.collections.immutable)

    debugImplementation(libs.androidx.compose.ui.tooling)

    screenshotTestImplementation(libs.androidx.compose.ui.tooling)
    screenshotTestImplementation(libs.androidx.compose.ui.graphics)
    screenshotTestImplementation(libs.screenshot.validation.api)
}
