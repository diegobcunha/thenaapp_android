# SDD — Vaccine Tracking Feature

## Decisions Made

- **Module**: `:feature:vaccine` — standalone library module following the same structure as `:feature:sleep` and `:feature:feeding`. Package root: `com.diegocunha.thenaapp.feature.vaccine`.
- **Offline-first scope**: Only administered vaccine records are cached in Room (`vaccine_records` table). PNI schedule templates are fetched from the backend on demand — they are static reference data seeded by Flyway and change only when the Ministry of Health revises the schedule.
- **Save-then-sync strategy**: When a user registers a vaccine, the record is written to Room immediately with `pendingSync = true`. A backend POST follows; on success the row is updated with the server UUID and `pendingSync = false`. If the network is unavailable the record remains visible in the UI and syncs on next app launch via `VaccineRepository.syncPendingRecords()`.
- **Room database**: A dedicated `VaccineDatabase` (separate from `FeedingDatabase`) to keep schema versions independent. Version 1 contains only `VaccineRecordEntity`.
- **Domain enums**: `DoseType`, `InjectionSite`, `ReactionSeverity`, and `VaccineScheduleStatus` live in `feature/vaccine/domain/model/`. String values match the backend `CHECK` constraints exactly (e.g., `"PRIMARY"`, `"LEFT_THIGH"`), allowing lossless round-trips through the DTO layer.
- **No UseCase layer**: Direct repository calls from ViewModels — business logic (validation, status computation) is handled server-side. The UseCase rule from CLAUDE.md is not triggered.
- **Manual + PNI entries**: The register form accepts both. When launched from a schedule item, `pniTemplateId` is pre-populated and `vaccineName` is locked (comes from the template). When launched from the FAB, the name is free-text.
- **Screen navigation**: `HomeEffect.NavigateToVaccine(babyId)` added to the existing Home MVI. `HomeViewModel.VaccineInfo` intent now emits this effect instead of `NotDevelopedYet`.
- **Icons**: `ThenaIcons.Vaccine` (`Icons.Rounded.Vaccines`) used throughout — no emojis.
- **Recomposition tracing**: `VaccineScreen` and `RegisterVaccineScreen` annotated with `@TraceRecomposition`.
- **Date storage**: `administeredDate` stored as ISO string `"yyyy-MM-dd"` in Room and sent as-is to the API. Conversion to `LocalDate` for display is done in the presentation layer.

## Technical Features Implemented

| Layer | What was built |
|---|---|
| `datasource` — Network | `VaccineApiService` with 5 endpoints (schedule, templates, list, register, delete); DTOs: `RegisterVaccineRequest`, `VaccineRecordResponse`, `VaccineRecordSummaryResponse`, `VaccineScheduleResponse`, `VaccineScheduleItemResponse`, `PniTemplateResponse` |
| `datasource` — Room | `VaccineRecordEntity` (with `pendingSync` flag); `VaccineRecordDao` (upsert, observe by baby, getById, deleteById, getPendingSync); `VaccineDatabase` |
| `datasource` — DI | `DatasourceModule` extended with `VaccineApiService`, `VaccineDatabase`, and `VaccineRecordDao` singletons |
| `feature/vaccine` — Domain | Enums: `DoseType`, `InjectionSite`, `ReactionSeverity`, `VaccineScheduleStatus`; domain models: `VaccineRecord`, `VaccineScheduleItem`, `PniTemplate`, `VaccineSchedule`, `RegisterVaccineInput` |
| `feature/vaccine` — Repository | `VaccineRepository` interface; `VaccineRepositoryImpl` using `safeApiCall` + Room observe + save-then-sync |
| `feature/vaccine` — VaccineScreen | MVI: `VaccineState` (schedule items, records, tab, progress counts), `VaccineIntent`, `VaccineEffect`, `VaccineViewModel`; UI: progress card (gradient + circular progress), urgent alert card, Upcoming/Completed segment tabs with badges, `VaccineListItem` with status-aware colors |
| `feature/vaccine` — RegisterVaccineScreen | MVI: `RegisterVaccineState` (all form fields + validation flags), `RegisterVaccineIntent`, `RegisterVaccineEffect`, `RegisterVaccineViewModel`; UI: scrollable form with `OutlinedTextField`, date picker, dropdown menus for enum fields, reaction toggle with conditional fields |
| `feature/vaccine` — Navigation | `VaccineNavigation(babyId)`, `RegisterVaccineNavigation(babyId, pniTemplateId?)` — both `@Serializable : NavKey` |
| `feature/vaccine` — DI | `VaccineModule` with `single<VaccineRepository>` and two `viewModel` factories |
| `feature/home` | `HomeEffect.NavigateToVaccine(babyId)` added; `HomeViewModel` routes `VaccineInfo` intent; `HomeScreen` accepts `onNavigateToVaccine` callback |
| `app` | `MainActivity` wired with `VaccineNavigation` and `RegisterVaccineNavigation` entries; `ThenaApplication` registers `vaccineModule`; `settings.gradle.kts` includes `:feature:vaccine` |
| Tests | `VaccineViewModelTest` (5 cases); `RegisterVaccineViewModelTest` (5 cases) — all using `UnconfinedTestDispatcher`, MockK, Turbine |

## Current Status

🔄 In development.

## Last Updated

2026-05-22