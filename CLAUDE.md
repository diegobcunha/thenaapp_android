# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests on device/emulator
./gradlew lint                   # Run lint checks
./gradlew clean                  # Clean build artifacts
```

To run a single test class:
```bash
./gradlew test --tests "com.diegocunha.thenaapp.ExampleUnitTest"
```

## Project Overview

Android Thena App app with the purpose to help parents to handle the day-to-day of newborns routine like feed, sleep, vaccines and other important things. Architecture is multi-module clean architecture with MVI pattern, Material3 follow up and 100% Compose UI.

## Module Structure

```
:app                  — Application entry point, NavHost, Koin initialization
:core                 — MVI base classes, Resource sealed class, DispatchersProvider
:coreui               — Material3 theme, shared Compose components, Spacing system
:datasource           — Retrofit API, Repository, DTOs, DAOs
```

## Architecture & Technology

- **UI:** 100% Jetpack Compose with Material Design 3, no XML layouts
- **Pattern:** MVI via `BaseViewModel<State, Intent>` in `:core`
- **Language:** Kotlin 2.2.10
- **Min SDK:** 24 (Android 7.0), Target/Compile SDK: 36
- **Package root:** `com.diegocunha.thenaapp`
- **DI:** Koin 4.0.4 — modules loaded in `ThenaApplication`
- **Networking:** Retrofit 2.11.0 + OkHttp 4.12.0 + Kotlinx Serialization
- **Image loading:** Coil 2.7.0
- **Navigation:** AndroidX Navigation3 Compose 1.1.0

## Key Files

- `app/src/main/java/com/diegocunha/thenaapp/MainActivity.kt` — Entry point with NavHost
- `app/src/main/java/com/diegocunha/thenaapp/ThenaApplication.kt` — Koin setup
- `core/src/main/java/com/diegocunha/thenaapp/core/mvi/BaseViewModel.kt` — MVI base
- `core/src/main/java/com/diegocunha/thenaapp/core/Resource.kt` — Success/Error/Loading sealed class
- `datasource/src/main/java/com/diegocunha/thenaapp/datasource/network/ThenaAppService.kt` — Retrofit API
- `coreui/src/main/java/com/diegocunha/thenaapp/coreui/theme/` — Material3 theme
- `gradle/libs.versions.toml` — Centralized version catalog for all dependencies

## Dependencies

All dependency versions are managed via the version catalog at `gradle/libs.versions.toml`. Add new dependencies there first, then reference them in `build.gradle.kts`.

## Unit Testing

- **Mocking:** MockK
- **Async:** Kotlin Coroutines Test
- **Flow testing:** Turbine
- **Coverage:** kotlinx-kover
  - Compose views and Compose screens are excluded from coverage
  - Minimum coverage threshold: **80%**

## Theme

Material Design 3 with dynamic color support (Android 12+) and automatic light/dark mode. Theme in `coreui/src/main/java/com/diegocunha/thenaapp/coreui/theme/`. Custom `Spacing` via `CompositionLocal`.

## Architecture Rules

**Clean Architecture DTO rule:** DTOs from `:datasource` must never appear in ViewModels or Composables. Each feature owns its domain models in `feature/<name>/domain/model/` and mappers in `feature/<name>/domain/mapper/`.

**UseCase rule:** Create a UseCase only when: (1) non-trivial business logic exists beyond fetching and mapping, (2) multiple repositories are orchestrated, or (3) logic is shared across two or more ViewModels. Direct repository calls from the ViewModel are the default.

## Development Methodology

This project uses **SDD (Specification-Driven Development)**. All feature work follows these phases:

1. **Interview** — Ask the user about functional, technical, and documentation requirements before any implementation
2. **Specification** — Enter plan mode, present the full implementation plan for review before any code is written
3. **Implementation** — Execute step by step, phase-gated, only after the user approves the plan

**Rules:**
- Never advance to the next phase until the user explicitly says the current phase is ready
- The user may return to any previous phase at any time (flow possibility, not a phase). When this happens, analyze the impact and replan forward from there
- When development is finished, create a SDD documentation file for the feature in the project containing:
  - Decisions made
  - Technical features implemented
  - Current status of the feature
  - Last updated date
