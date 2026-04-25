# Thena — Newborn Care Companion

![Coverage](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/diegobcunha/50d738ca736d944326dd848d6235f418/raw/thenaapp-coverage.json)

**Thena** is an Android app designed to help parents manage the day-to-day routine of newborns — tracking sleep, feeding, vaccinations, and daily insights, all in one place.

---

## Features

### Currently Implemented

| Feature     | Status | Description                                             |
|-------------|---|---------------------------------------------------------|
| Onboarding  | ✅ Done | 5-slide carousel introducing app features               |
| Login       | ✅ Done | Email/password + Google Sign-In with validation         |
| Signup      | ✅ Done | Email/password + Google Sign-Up with profile completion |
| Create_Baby | ✅ Done | Create a new baby and upload a photo                    |

### In Development

| Feature | Description |
|---|---|
| 🌙 Sleep Tracking | Live sleep timer, daily & weekly goals, sleep quality insights |
| 🍼 Feeding Log | Breast & bottle logging, feeding reminders, volume & duration tracking |
| 💉 Vaccination Schedule | Full vaccine calendar, due date reminders, progress tracking |
| 📊 Daily Insights | Daily & weekly reports, growth tracking, personalized tips |

---

## App Flow

```
First launch:  Onboarding → Login → Home
Return launch: Login → Home
Signup flow:   Onboarding → Login → Signup → Create Baby
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVI (Model-View-Intent) |
| DI | Koin 4.0.4 |
| Navigation | AndroidX Navigation3 Compose 1.1.0 |
| Networking | Retrofit 2.11.0 + OkHttp 4.12.0 + Kotlinx Serialization |
| Auth | Firebase Auth + Google Sign-In |
| Storage | SharedPreferences |
| Image Loading | Coil 2.7.0 |
| Min SDK | 24 (Android 7.0) |
| Target/Compile SDK | 36 |

---

## Module Structure

```
:app                  — Application entry point, NavHost, Koin initialization
:core                 — MVI base classes, Resource sealed class, DispatchersProvider
:coreui               — Material3 theme, shared Compose components, Spacing system
:datasource           — Retrofit API, Repositories, DTOs, DAOs
:feature:onboarding   — 5-slide onboarding carousel
:feature:login        — Login screen with email/password and Google Sign-In
:feature:signup       — Signup screen with email/password and Google Sign-Up
:feature:baby         — Responsible to handle the baby CRUD and informations
```

Each feature module follows clean architecture with:
- `presentation/` — Composables, ViewModels, MVI State/Intent/Effect
- `domain/` — Repository interfaces
- `data/` — Repository implementations
- `di/` — Koin module registration

---

## Architecture

The app uses **MVI (Model-View-Intent)** via `BaseViewModel<State, Intent>` in `:core`.

```
UI (Composable)
  │── Intent → ViewModel
  │                └── updates State → UI re-renders
  └── Effect (one-shot) → Navigation / Snackbar
```

**Clean Architecture rules:**
- DTOs from `:datasource` never appear in ViewModels or Composables
- Each feature owns domain models in `feature/<name>/domain/model/` and mappers in `feature/<name>/domain/mapper/`
- UseCases are created only when logic is non-trivial, orchestrates multiple repositories, or is shared across ViewModels

---

## Getting Started

### Prerequisites

- Android Studio Meerkat or newer
- JDK 17+
- Android SDK 36
- Google Services JSON (`google-services.json`) placed in `app/`
- Account created at Cloudinary to upload the images

### Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests on device/emulator
./gradlew lint                   # Run lint checks
./gradlew clean                  # Clean build artifacts
```

Run a single test class:

```bash
./gradlew test --tests "com.diegocunha.thenaapp.ExampleUnitTest"
```

---

## Testing

| Tool | Purpose |
|---|---|
| MockK | Mocking |
| Kotlin Coroutines Test | Async testing |
| Turbine | Flow testing |
| kotlinx-kover | Coverage reporting |

**Coverage threshold:** 80% minimum (Compose views/screens excluded).

---

## Design System

Material Design 3 with:
- Dynamic color support (Android 12+)
- Automatic light/dark mode
- **Nunito** font family
- Custom `Spacing` via `CompositionLocal` (xs → xxxl scale)
- Extended color tokens: `sleepFill`, `feedFill`, `vaccineFill`, `summaryFill`

Theme entrypoint: `coreui/src/main/java/com/diegocunha/thenaapp/coreui/theme/`

---

## Development Methodology

This project uses **SDD (Specification-Driven Development)**:

1. **Interview** — Requirements gathered before any implementation
2. **Specification** — Full plan reviewed and approved before coding
3. **Implementation** — Executed step-by-step after plan approval

---

## Package

`com.diegocunha.thenaapp`
