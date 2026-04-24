import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.ksp)
}

fun getLocalProperties(): Properties {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists() && localPropertiesFile.isFile) {
        localPropertiesFile.inputStream().use { properties.load(it) }
    }
    return properties
}

val localProps = getLocalProperties()

android {
    namespace = "com.diegocunha.thenaapp.datasource"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://localhost:8080\"")
            buildConfigField(
                "String",
                "CLOUDINARY_CLOUD_NAME",
                "\"${localProps.getProperty("CLOUDINARY_CLOUD_NAME") ?: ""}\""
            )
            buildConfigField(
                "String",
                "CLOUDINARY_UPLOAD_PRESET",
                "\"${localProps.getProperty("CLOUDINARY_UPLOAD_PRESET") ?: ""}\""
            )
        }

        release {
            buildConfigField("String", "BASE_URL", "")
            buildConfigField(
                "String",
                "CLOUDINARY_CLOUD_NAME",
                "\"${localProps.getProperty("CLOUDINARY_CLOUD_NAME") ?: ""}\""
            )
            buildConfigField(
                "String",
                "CLOUDINARY_UPLOAD_PRESET",
                "\"${localProps.getProperty("CLOUDINARY_UPLOAD_PRESET") ?: ""}\""
            )
        }
    }

}

dependencies {
    implementation(project(":core"))
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    api(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.koin.android)
    api(platform(libs.firebase.bom))
    api(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.paging.testing)
    testImplementation(libs.turbine)
}
