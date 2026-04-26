# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug                    # Build debug APK
./gradlew assembleRelease                  # Build release APK
./gradlew testDebugUnitTest                # Run unit tests (matches CI)
./gradlew koverXmlReport koverHtmlReport   # Generate coverage reports
./gradlew koverVerify                      # Enforce 80% coverage threshold
./gradlew connectedAndroidTest             # Run instrumented tests on device/emulator
./gradlew lint                             # Run lint checks
./gradlew clean                            # Clean build artifacts
```

To run a single test class:
```bash
./gradlew :feature:login:testDebugUnitTest --tests "com.diegocunha.thenaapp.feature.login.LoginViewModelTest"
```

## Local Setup

Two files are required that are not checked in:

- `app/google-services.json` — Firebase config (download from Firebase Console)
- `local.properties` — must include:
  ```
  CLOUDINARY_CLOUD_NAME=<your_cloudinary_cloud_name>
  CLOUDINARY_UPLOAD_PRESET=<your_upload_preset>
  ```

Debug builds point to `http://localhost:8080`; release builds point to the Railway backend.

## Project Overview

Android app to help parents manage newborn routines (sleep, feeding, vaccines). Multi-module clean architecture, MVI pattern, 100% Jetpack Compose + Material 3.

## Module Structure

```
:app                      — Application entry point, NavHost, Koin initialization
:core                     — MVI base classes, Resource sealed class, DispatchersProvider
:coreui                   — Material3 theme, shared Compose components, Spacing system
:datasource               — Retrofit API services, DTOs, OkHttp interceptors, SharedPreferences
:feature:onboarding       — 5-slide onboarding carousel
:feature:login            — Email/password + Google Sign-In
:feature:signup           — Email/password + Google Sign-Up with profile completion
:feature:baby             — Multi-step baby creation with photo upload
:feature:home             — Home screen showing user + baby summary
```

Each feature module follows this internal structure:
```
presentation/   — Composables, ViewModels, MVI State/Intent/Effect
domain/         — Repository interfaces + domain models
repository/     — Repository implementations
di/             — Koin module
```

## Architecture & Technology

- **UI:** 100% Jetpack Compose, Material Design 3, no XML layouts
- **Pattern:** MVI via `BaseViewModel<State, Intent, Effect>` in `:core`
- **Language:** Kotlin 2.2.10, JVM target 11
- **Min SDK:** 24, Target/Compile SDK: 36
- **Package root:** `com.diegocunha.thenaapp`
- **DI:** Koin 4.0.4 — all modules loaded in `ThenaApplication`
- **Networking:** Retrofit + OkHttp + Kotlinx Serialization (`ignoreUnknownKeys = true`)
- **Image loading:** Coil 2.7.0
- **Navigation:** AndroidX Navigation3 Compose 1.1.0
- **Auth:** Firebase Auth + Google Credential Manager

## MVI Data Flow

```
Composable
  ├── sendIntent(Intent) → ViewModel.processIntent()
  │                             ├── updateState { copy(...) }  → StateFlow → UI re-renders
  │                             └── sendEffect(Effect)          → Channel  → one-shot events
  └── LaunchedEffect { effects.collectLatest { ... } }          → navigation / snackbar
```

`BaseViewModel` queues intents via a `Channel.BUFFERED` channel, processed serially. Effects use `Channel.UNLIMITED` and are consumed with `collectLatest` in the Composable.

## Navigation

Routes are `@Serializable` objects/data classes implementing `NavKey`. The start destination is resolved by `MainViewModel.startDestination` (a `StateFlow<NavKey?>`) before the NavHost renders. `MainActivity` holds the `rememberNavBackStack` and all wiring between screens.

```
OnboardingNavigation → LoginNavigation → SignupNavigation → CreateBabyNavigation → HomeNavigation
```

`MainViewModel` checks: onboarding seen? → Firebase user present? → token valid? → profile complete? → navigate accordingly.

## Key Files

- `app/.../MainActivity.kt` — NavHost + all screen wiring
- `app/.../MainViewModel.kt` — Startup routing logic
- `app/.../ThenaApplication.kt` — Koin module registration
- `core/.../mvi/BaseViewModel.kt` — MVI base (state, intent, effect channels)
- `core/.../resource/Resource.kt` — `Success / Error / Loading` sealed class
- `datasource/.../network/SafeApiCall.kt` — `safeApiCall(dispatcher) { ... }` wraps any suspend call into `Resource<T>`
- `datasource/.../interceptor/HeaderInterceptor.kt` — Adds `Authorization: Bearer <token>` to every request
- `datasource/.../interceptor/AccessTokenRepositoryImpl.kt` — Calls `Tasks.await(user.getIdToken(false))` synchronously (called from OkHttp thread)
- `datasource/.../di/DatasourceModule.kt` — OkHttp + Retrofit + service singletons
- `gradle/libs.versions.toml` — Centralized version catalog

## Repository Pattern

All repository implementations use `safeApiCall` from `:datasource`:

```kotlin
override suspend fun getSomething(): Resource<MyModel> =
    safeApiCall(dispatchersProvider) {
        val dto = service.getSomething()
        dto.toDomain()   // map DTO → domain model here, never leak DTOs upward
    }
```

## Dependencies

All versions are in `gradle/libs.versions.toml`. Add entries there first, then reference with `libs.<alias>` in `build.gradle.kts`.

## Unit Testing

- **Mocking:** MockK (`mockk()`, `coEvery`, `coVerify`)
- **Async:** `UnconfinedTestDispatcher` set as `Dispatchers.Main` in `@Before`
- **Flow testing:** Turbine (`flow.test { awaitItem() }`)
- **Coverage:** kotlinx-kover — Compose screens excluded, minimum **80%** threshold enforced in CI via `koverVerify`
- Test naming convention: `` `WHEN <condition> THEN <expectation>` ``

## Screenshot Testing

Screenshot tests use the [AGP Compose Screenshot Testing](https://developer.android.com/studio/test/compose-screenshot-testing) plugin (`com.android.compose.screenshot`). Currently only `:coreui` has screenshot tests; other modules will be added incrementally.

```bash
./gradlew :coreui:updateDebugScreenshotTest    # Regenerate golden reference images
./gradlew :coreui:validateDebugScreenshotTest  # Compare against goldens (runs in CI)
```

**Rules for adding screenshot tests to a module:**

1. Enable the plugin and experimental flag in `build.gradle.kts`:
   ```kotlin
   plugins { alias(libs.plugins.screenshot) }
   android { experimentalProperties["android.experimental.enableScreenshotTest"] = true }
   dependencies { screenshotTestImplementation(libs.screenshot.validation.api) }
   ```
2. Create test files under `src/screenshotTest/java/…` — **not** `src/main/`.
3. Functions must **not** be `private` — the runner uses reflection to discover them.
4. Always annotate with both `@PreviewTest` and `@Preview`. Keep plain `@Preview` in the main source set for Android Studio previews.
5. **Dark mode:** pass `darkTheme = true` explicitly to `ThenaTheme` — do not rely on `uiMode` in `@Preview` or `isSystemInDarkTheme()`, neither propagates correctly in the Robolectric renderer.
6. **Stale goldens:** `updateDebugScreenshotTest` never deletes old images when the `@Preview` parameters change (the hash suffix in the filename changes). Delete orphaned files manually before committing.

Golden reference images are stored in `src/screenshotTestDebug/reference/` and **must be committed** to the repository so CI can validate against them.

## Theme & Design System

Material Design 3, dynamic color (Android 12+), automatic light/dark. Theme entry point: `coreui/.../theme/ThenaTheme`.

- **Font:** Nunito family
- **Spacing:** `ThenaTheme.spacing` via `CompositionLocal` — scale: `xxs → xs → sm → md → m → lg → xl → xxl → xxxl`
- **Extended color tokens:** `sleepFill`, `feedFill`, `vaccineFill`, `summaryFill` (for feature cards)

## Compose Performance & Recomposition Tracing

The project uses [Skydoves Compose Stability Analyzer](https://github.com/skydoves/compose-stability-analyzer) (version `0.6.4`) to detect unstable Composables that cause unnecessary recompositions.

- **Gradle plugin:** `com.github.skydoves.compose.stability.analyzer` — applied in every feature module and `:app`/`:coreui`
- **Runtime annotation:** `com.skydoves.compose.stability.runtime.TraceRecomposition`

**Rule:** Every new feature screen Composable **must** be annotated with `@TraceRecomposition`. See `LoginScreen.kt` as the reference:

```kotlin
import com.skydoves.compose.stability.runtime.TraceRecomposition

@TraceRecomposition
@Composable
fun LoginScreen(...) { ... }
```

Apply the annotation to the top-level screen Composable (the one registered in the NavHost), not to every internal sub-Composable.

## Architecture Rules

**DTO rule:** DTOs from `:datasource` must never appear in ViewModels or Composables. Map to domain models in the repository layer.

**UseCase rule:** Create a UseCase only when: (1) non-trivial business logic beyond fetch + map, (2) multiple repositories are orchestrated, or (3) logic is shared across two or more ViewModels. Direct repository calls from the ViewModel are the default.

## Development Methodology

This project uses **SDD (Specification-Driven Development)**. All feature work follows these phases:

1. **Interview** — Ask the user about functional, technical, and documentation requirements before any implementation
2. **Specification** — Enter plan mode, present the full implementation plan for review before any code is written
3. **Implementation** — Execute step by step, phase-gated, only after the user approves the plan

**Rules:**
- Never advance to the next phase until the user explicitly says the current phase is ready
- The user may return to any previous phase at any time. When this happens, analyze the impact and replan forward from there
- When development is finished, create a SDD documentation file for the feature (see `feature/baby/SDD_CREATE_BABY.md` as reference) containing: decisions made, technical features implemented, current status, and last updated date
