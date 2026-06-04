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
:datasource               — Retrofit API services, DTOs, OkHttp interceptors, Room DB, SharedPreferences
:feature:onboarding       — 5-slide onboarding carousel
:feature:login            — Email/password + Google Sign-In
:feature:signup           — Email/password + Google Sign-Up with profile completion
:feature:baby             — Multi-step baby creation with photo upload
:feature:home             — Home screen showing user + baby summary
:feature:feeding          — Breast/bottle feeding tracker with local persistence and statistics
:feature:sleep            — Sleep session timer with daily/weekly statistics
```

Each feature module follows this internal structure:
```
presentation/           — Composables, ViewModels, MVI State/Intent/Effect
presentation/navigation — @Serializable route classes implementing NavKey
domain/                 — Repository interfaces + domain models + use cases
repository/             — Repository implementations (may also live in :datasource for shared data)
di/                     — Koin module
```

**Namespace exception:** `:feature:sleep` uses `com.diegocunha.thenaapp.sleep` (not `.feature.sleep`). Use this namespace when creating new files in that module.

## Architecture & Technology

- **UI:** 100% Jetpack Compose, Material Design 3, no XML layouts
- **Pattern:** MVI via `BaseViewModel<State, Intent, Effect>` in `:core`
- **Language:** Kotlin 2.2.21, JVM target 11
- **Min SDK:** 24, Target/Compile SDK: 36
- **Package root:** `com.diegocunha.thenaapp`
- **DI:** Koin 4.0.4 — all modules loaded in `ThenaApplication`
- **Networking:** Retrofit + OkHttp + Kotlinx Serialization (`ignoreUnknownKeys = true`)
- **Local persistence:** Room (feeding sessions), SharedPreferences (onboarding flag, user session ID)
- **Image loading:** Coil 2.7.0
- **Navigation:** AndroidX Navigation3 Compose 1.1.0
- **Auth:** Firebase Auth + Google Credential Manager
- **Immutable collections:** `kotlinx.collections.immutable` for UI state lists

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

Routes are `@Serializable` objects/data classes implementing `NavKey`, located in `presentation/navigation/[FeatureName]Navigation.kt` inside each feature module. The start destination is resolved by `MainViewModel.startDestination` (a `StateFlow<NavKey?>`) before the NavHost renders. `MainActivity` holds `rememberNavBackStack` and all screen wiring.

```
OnboardingNavigation → LoginNavigation → SignupNavigation → CreateBabyNavigation → HomeNavigation
```

`MainViewModel` checks: onboarding seen? → Firebase user logged in? → `ProfileStatus` (`MissingName` / `MissingBaby` / `Complete`) → navigate accordingly.

**Back stack manipulation** (in `MainActivity`):
```kotlin
backStack.add(FeedingNavigation(babyId = id))   // push
backStack.removeLastOrNull()                     // pop (system back)
backStack.clear()                                // reset to root
```

**Parameterized routes** carry data as constructor properties:
```kotlin
@Serializable
data class FeedingNavigation(val babyId: String) : NavKey

@Serializable
data class SignupNavigation(val hasBaby: Boolean, val isProfileCompletion: Boolean) : NavKey
```

**ViewModel wiring in NavDisplay** (MainActivity):
```kotlin
entry<FeedingNavigation> { key ->
    val viewModel = koinViewModel<FeedingViewModel>(parameters = { parametersOf(key.babyId) })
    FeedingScreen(viewModel = viewModel, ...)
}
```

## Offline-First Architecture

Features that track user activity (feeding, sleep) follow an **offline-first** pattern: all mutations write to Room first and sync to the API asynchronously. UI always reads from the local source of truth.

### Room Database

Defined in `:datasource` (`FeedingDatabase`). Current entities:

| Entity | Key fields |
|---|---|
| `FeedingSessionEntity` | `id`, `babyId`, `type`, `startedAt`, `endedAt`, `bottleMl`, `bottleType`, `synced` |
| `BreastSegmentEntity` | `id`, `sessionId`, `breast` (LEFT/RIGHT), `startedAt`, `endedAt` |

The `synced: Boolean` flag on sessions marks records that have been successfully sent to the backend. Unsynced records are candidates for background retry.

DAOs expose **Flow-based queries** so the UI automatically reflects local writes:
```kotlin
@Query("SELECT * FROM feeding_sessions WHERE babyId = :babyId AND endedAt IS NULL LIMIT 1")
fun observeActiveSession(babyId: String): Flow<FeedingSessionEntity?>
```

### Session Manager Pattern

Features with an active session lifecycle (feeding, sleep) use a **Session Manager** — a singleton that owns the session state, persists it locally, and syncs with the API.

```
ViewModel → SessionManager → LocalDataSource (Room) — source of truth for UI
                           → Repository → API         — sync, non-blocking
```

The ViewModel collects from the Session Manager's Flow and never calls the API directly for mutations. Example flow:

```kotlin
// ViewModel collects local truth
feedingSessionManager.observeActiveSession(babyId)
    .collectLatest { session -> updateState { copy(activeSession = session) } }

// Mutation: write local first, then sync
fun stopSession() {
    viewModelScope.launch {
        sessionManager.stopSession()  // writes Room, then calls API
    }
}
```

### Shared ViewModel Base Pattern

When two screens in the same feature share transformation logic (e.g., domain → UI model conversion, formatting), extract a `Base*ViewModel`:

```kotlin
abstract class BaseSleepViewModel<State : MviState, Intent : MviIntent, Effect : MviEffect>(
    initialState: State
) : BaseViewModel<State, Intent, Effect>(initialState) {

    protected fun SleepSession.toUi(): SleepSessionUiModel = ...
    protected fun SleepDailyStats.toUi(): SleepDailyStatsUiModel = ...
    protected fun formatDuration(minutes: Int): String = "${minutes / 60}h ${minutes % 60}m"
}
```

`SleepViewModel` and `SleepStatisticsViewModel` both extend `BaseSleepViewModel`.

## Koin DI Patterns

All Koin modules are registered in `ThenaApplication`:
```
coreModule, datasourceModule, appModule, onboardingModule, loginModule,
signupModule, babyModule, homeModule, feedingModule, sleepModule
```

**Scoping rules:**
- `single { ... }` — services, repositories, database, interceptors, OkHttpClient
- `viewModel { ... }` — ViewModels with no parameters
- `viewModel { (param: Type) -> ... }` — ViewModels that require route parameters

**Parameterized ViewModel declaration:**
```kotlin
// In featureModule
viewModel { (babyId: String) ->
    FeedingViewModel(sessionManager = get(), repository = get(), babyId = babyId)
}

// In MainActivity
val viewModel = koinViewModel<FeedingViewModel>(parameters = { parametersOf(key.babyId) })
```

## Repository Pattern

All repository implementations use `safeApiCall` from `:datasource`:

```kotlin
override suspend fun getSomething(): Resource<MyModel> =
    safeApiCall(dispatchersProvider) {
        val dto = service.getSomething()
        dto.toDomain()   // map DTO → domain model here, never leak DTOs upward
    }
```

**Repository location rule:**
- Feature-specific repositories → `feature/*/repository/` (implements interface from `feature/*/domain/`)
- Cross-feature or auth-related repositories → `datasource/repository/` (e.g., `UserProfileRepository`, `AccessTokenRepository`)

## Networking

**Interceptors:**
- `HeaderInterceptor` — adds `Authorization: Bearer <token>`, `Accept: application/json`, `Content-Type: application/json`
- `TokenAuthenticator` — on 401, calls `getAccessToken(forceRefresh = true)` and retries up to 2 times

**Token management:** `AccessTokenRepositoryImpl` calls `Tasks.await(user.getIdToken(forceRefresh))` synchronously from the OkHttp interceptor thread (never use coroutines here — OkHttp runs on its own thread pool).

**Base URL:** `BuildConfig.BASE_URL` — `http://localhost:8080` in debug, Railway URL in release.

## Image Upload (Cloudinary)

Baby profile photos are compressed locally before upload.

**Compression rules (`ImageCompressor`):**
- Max 1024×1024 px (proportional scale)
- JPEG at 80% quality, max 500 KB
- Quality reduced by 10% per iteration until under 500 KB

**Upload:** A separate Retrofit instance at `named(CLOUDINARY_NAME)` posts `multipart/form-data` to `https://api.cloudinary.com/`. Credentials (`CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_UPLOAD_PRESET`) are injected via `BuildConfig` from `local.properties`.

**Photo source:** Camera (`TakePicture` contract + `FileProvider`) or gallery (`PickVisualMedia`). Raw URI is compressed before sending.

## Firebase

- **Auth:** email/password + Google Sign-In. `firebaseAuth.signInWithEmailAndPassword(...).await()` in repositories.
- **Crashlytics:** enabled in release only. In debug: `FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false` — prevents polluting analytics during development.
- No Analytics or Remote Config currently in use.

## Immutable UI Models

Use `@Immutable` and `kotlinx.collections.immutable` for UI state types that contain collections. This prevents Compose from treating them as unstable and avoids spurious recompositions.

```kotlin
@Immutable
data class SleepDailyStatsUiModel(
    val sessions: ImmutableList<SleepSessionUiModel>,
    val totalMinutes: Int,
)

// In ViewModel/repository:
val uiModels = domainList.map { it.toUi() }.toPersistentList()
```

Apply `@Immutable` to all UI model data classes that are part of a MVI `State`. Do **not** apply it to domain models or DTOs.

## Date Input Pattern

Text fields that accept dates use `DateMaskVisualTransformation` — a locale-aware visual transformation:

- PT locale: `DD/MM/YYYY`
- Other locales: `YYYY-MM-DD`

Raw state stores **digits only**. The repository always sends **ISO 8601** (`YYYY-MM-DD`) to the API regardless of display locale.

## Dependencies

All versions are in `gradle/libs.versions.toml`. Add entries there first, then reference with `libs.<alias>` in `build.gradle.kts`.

## Unit Testing

- **Mocking:** MockK (`mockk()`, `coEvery`, `coVerify`)
- **Async:** `UnconfinedTestDispatcher` set as `Dispatchers.Main` in `@Before`
- **Flow testing:** Turbine (`flow.test { awaitItem() }`)
- **Coverage:** kotlinx-kover — Compose screens excluded, minimum **80%** threshold enforced in CI via `koverVerify`
- Test naming convention: `` `WHEN <condition> THEN <expectation>` ``

**Kover exclusions** (already configured): `*.network.model`, `*.database.entity`, `coreui`, `@Composable` classes, `BuildConfig`, `*.di.*`, `*Screen`, `*Exception`, generated code.

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
- **Spacing:** `ThenaTheme.spacing` via `CompositionLocal` — scale: `xxs(2) → xs(4) → sm(8) → md(16) → m(20) → lg(24) → xl(32) → xxl(40) → xxxl(48)` dp
- **Extended color tokens:** `sleepFill`, `feedFill`, `vaccineFill`, `summaryFill` (for feature cards)

Access in Composables:
```kotlin
ThenaTheme.spacing.md          // Dp spacing token
ThenaTheme.extendedColors.feedFill  // feature card fill color
MaterialTheme.colorScheme.primary   // standard M3 color
```

## Compose Performance & Recomposition Tracing

The project uses [Skydoves Compose Stability Analyzer](https://github.com/skydoves/compose-stability-analyzer) (version `0.6.4`) to detect unstable Composables that cause unnecessary recompositions.

- **Gradle plugin:** `com.github.skydoves.compose.stability.analyzer` — applied in every feature module and `:app`/`:coreui`
- **Runtime annotation:** `com.skydoves.compose.stability.runtime.TraceRecomposition`
- **Enabled in debug** via `ComposeStabilityAnalyzer.setEnabled(true)` in `ThenaApplication`

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

**Offline-first rule:** Features that write user activity data (feeding, sleep) must persist to Room before calling the API. The UI observes Room via Flow — never the API response directly.

**Immutable collections rule:** Any `List` in a MVI `State` class must be `ImmutableList` from `kotlinx.collections.immutable`. Use `.toPersistentList()` when building state in the ViewModel.

**API sync flag rule:** Room entities that are synced to the backend carry a `synced: Boolean` field. Set it to `false` on local write and `true` after a successful API response.

## SDD Documents

Feature specification documents live in `/documents/`. Reference when understanding past decisions:

- `SDD_CREATE_BABY.md` — multi-step pager, gender enum, date masking, photo compression
- `SDD_SLEEP_TRACKING.md` — sleep types, nap schedule, daily/weekly stats, efficiency calculation
- `SDD_FEEDING_STATISTICS.md` — breast/bottle statistics, daily aggregation, date range queries
- `SDD_FEEDING_NETWORK_INTEGRATION.md` — API endpoints, session sync, segment management

When a feature is complete, create a new SDD in this directory following the same format.

## Development Methodology

This project uses **SDD (Specification-Driven Development)**. All feature work follows these phases:

1. **Interview** — Ask the user about functional, technical, and documentation requirements before any implementation
2. **Specification** — Enter plan mode, present the full implementation plan for review before any code is written. After the plan is approved, clear context with `/clear` to ensure implementation runs from documentation with a clean context.
3. **Implementation** — Execute step by step, phase-gated, only after the user approves the plan

**Rules:**
- Never advance to the next phase until the user explicitly says the current phase is ready
- The user may return to any previous phase at any time. When this happens, analyze the impact and replan forward from there
- When development is finished, create a SDD documentation file for the feature (see `/documents/SDD_CREATE_BABY.md` as reference) containing: decisions made, technical features implemented, current status, and last updated date
- Before any implementation, clear context with `/clear` to avoid hallucination