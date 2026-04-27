// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.stability.analyzer) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

dependencies {
    kover(project(":core"))
    kover(project(":datasource"))
    kover(project(":feature:baby"))
    kover(project(":feature:home"))
    kover(project(":feature:login"))
    kover(project(":feature:onboarding"))
    kover(project(":feature:signup"))
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
                    "com.diegocunha.thenaapp.datasource.network.model",
                    "com.diegocunha.thenaapp.coreui",
                )
                annotatedBy("androidx.compose.runtime.Composable")
                classes(
                    "*.BuildConfig",
                    "*.ComposableSingletons*",
                    "*.di.*",
                    "*.navigation.*",
                    "*.create.components.*",
                    "*Screen",
                    "*Exception*",
                    "*LoginCredentialsManager*",
                    "*DateMaskVisualTransformation*",
                    "*OnboardingSlide*",
                    "*ImageCompressor*",
                    "*AccessTokenRepositoryImpl*",
                    "*CustomSharedPreferencesImpl*",
                    "*RetrofitFactory*",
                    "*DispatchersProviderImpl*",
                    "*GoogleSignUpResponse*"
                )
            }
        }
    }
}
