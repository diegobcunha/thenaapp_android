# SDD — Feeding Feature

## Decisions Made

- **Cross-module data sharing**: `FeedingLocalDataSource` interface and `ActiveFeedingSnapshot` model live in `:datasource` so `:feature:home` can observe an active session without creating a dependency on `:feature:feeding`. `FeedingLocalDataSourceImpl` (also in `:datasource`) joins session + segment DAOs via `flatMapLatest`.

- **Session manager as single source of truth**: `FeedingSessionManager` is a Koin `single` that owns `_activeSession: MutableStateFlow<ActiveFeedingSession?>`. Both `FeedingViewModel` and `FeedingTimerService` inject and observe it. The manager owns all Room writes and service lifecycle calls (`startForegroundService` / `stopService`).

- **Timer persistence strategy**: No running counter is stored. Elapsed time is always computed on-the-fly from `BreastSegmentEntity.startedAt` / `endedAt` timestamps in Room. App-kill recovery works because `FeedingSessionManager.restoreSession()` rebuilds `ActiveFeedingSession` from the database on startup.

- **TapBreast single intent**: A single `TapBreast(breast: Breast)` intent replaces separate Start/Pause/Resume/Switch intents. `FeedingViewModel.handleTapBreast` branches on current state: no session → `startBreastfeeding`, same active breast → `pauseCurrentBreast`, different active breast → `switchBreast`, no active breast → `resumeBreast`. This keeps the ViewModel logic explicit and trivially testable.

- **No DispatchersProvider in ViewModels**: `FeedingViewModel` uses plain `viewModelScope.launch {}`. Dispatcher responsibility is delegated to `FeedingSessionManager`, which runs on `CoroutineScope(SupervisorJob() + Dispatchers.IO)`. This follows the existing project pattern (LoginViewModel, HomeViewModel, etc.).

- **ForegroundService in `:app` manifest**: The `<service>` declaration for `FeedingTimerService` and the required permissions (`FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_HEALTH`, `POST_NOTIFICATIONS`) were added to `app/src/main/AndroidManifest.xml`, not to `feature/feeding`'s manifest, because the app module is the one actually implementing the feature module.

- **One file per domain type**: `Breast`, `FeedingType`, `BottleType`, `BreastSegment`, and `ActiveFeedingSession` each have their own Kotlin file under `domain/model/`. No aggregated `FeedingModels.kt`.

- **`@Immutable` on MVI State**: `FeedingState` is annotated `@Immutable`. ViewModel never mutates state instances; `updateState { copy(...) }` always produces a new object, making `@Immutable` correct and stronger than `@Stable`.

- **HomeRepository mediates FeedingLocalDataSource**: `HomeViewModel` accesses active feeding data through `HomeRepository.observeActiveFeeding()`, not by directly injecting `FeedingLocalDataSource`. This preserves the ViewModel→Repository boundary consistent with the rest of the project.

- **Room in `:datasource`**: First use of Room in the project. KSP was already applied to `:datasource`; only the runtime, ktx, and compiler dependencies were added. Two databases are intentionally separate: the existing `AppDatabase` (user/baby) and the new `FeedingDatabase` (sessions/segments).

- **Design tokens**: Feeding UI uses `feedFill` (`0xFFFFD8EE`) as the gradient start and `0xFFFFE8F0` as the gradient end. The large timer text uses `ThenaTheme.colors.secondary` (`0xFFC774A0`) at 52sp bold, matching the Claude design reference.

---

## Technical Features Implemented

| Module / Layer | What was built |
|---|---|
| `:datasource` | `FeedingSessionEntity`, `BreastSegmentEntity`, `FeedingSessionDao`, `BreastSegmentDao`, `FeedingDatabase`, `FeedingLocalDataSource` interface, `FeedingLocalDataSourceImpl`; Koin registrations added to `DatasourceModule` |
| `:feature:feeding` domain | `Breast`, `FeedingType`, `BottleType`, `BreastSegment`, `ActiveFeedingSession` models; `FeedingRepository` interface |
| `:feature:feeding` repository | `FeedingRepositoryImpl` — maps entities to domain models; `BreastSegmentDao.getById` added to fix `closeSegment` (was incorrectly using `getActiveSegment` with a segment ID) |
| `:feature:feeding` session | `FeedingSessionManager` — orchestrates all session state, Room writes, and foreground service lifecycle |
| `:feature:feeding` service | `FeedingTimerService` — `START_STICKY`, updates notification every second, `stopSelf()` when session ends |
| `:feature:feeding` MVI | `FeedingState`, `FeedingIntent`, `FeedingEffect`, `FeedingViewModel` |
| `:feature:feeding` UI | `FeedingScreen` (top-level), `FeedingTypePicker`, `BreastfeedingTimerCard`, `BreastPill`, `BottleFeedingCard`; `FeedingNavigation` NavKey |
| `:feature:feeding` DI | `FeedingModule` — `FeedingRepository` (factory), `FeedingSessionManager` (single), `FeedingViewModel` (viewModel) |
| `:feature:home` | `HomeEffect.NavigateToFeeding`, `HomeState` += `activeFeedingSession` + `feedingBannerElapsedSeconds`, `HomeViewModel` observes active feeding + ticker, `HomeScreen` `ActiveFeedingBanner`, `HomeRepository.observeActiveFeeding()`, `HomeRepositoryImpl` wired to `FeedingLocalDataSource` |
| `:app` | `FOREGROUND_SERVICE*` + `POST_NOTIFICATIONS` permissions; `FeedingTimerService` service declaration; `feedingModule` registered in `ThenaApplication`; `FeedingNavigation` entry in `MainActivity` |

---

## Current Status

✅ Feature complete and building (`assembleDebug` passes).

**Backend not yet implemented**: All sessions are persisted locally in Room. The `synced: Boolean` flag on `FeedingSessionEntity` is reserved for future server sync.

**Single device only**: No cloud sync in this iteration.

**Retrospective manual entry**: The UI supports entering past bottle feedings immediately (no time picker). Breastfeeding sessions are always live-timed.

---

## Last Updated

2026-04-30