// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.google.services) apply false
}

dependencies {
    kover(project(":core"))
    kover(project(":datasource"))
}

kover {
    reports {
        verify {
            rule {
                minBound(80)
            }
        }

        filters {
            excludes {
                packages(
                    "com.diegocunha.thenaapp.core.di",
                    "com.diegocunha.thenaapp.datasource.di",
                    "com.diegocunha.thenaapp.datasource.model"
                )
            }
        }
    }
}