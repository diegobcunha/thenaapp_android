# SDD — Feeding Network Integration

## Context

The feeding feature was implemented with a fully local architecture (Room DB only). `FeedingService` was already defined and registered in Koin DI but never injected or called. This integration connects the local implementation to the backend, keeping the timer/UI experience unchanged.

## Decisions Made

- **Local-first, optimistic sync**: The local Room DB remains the source of truth for the timer UI. Network calls happen alongside local operations. This ensures the breastfeeding timer is never blocked by network latency.
- **Server-assigned session IDs**: The backend assigns its own UUID on `POST /v1/baby/{babyId}/feeding/breastfeeding`. The locally-generated UUID is replaced by the server's ID, which is required for all subsequent calls (`switchBreastSide`, `completeSession`). `createBreastSession` return type is changed from `Unit` to `String` to propagate the server ID upward.
- **babyId via navigation**: The user selects which baby the feeding belongs to. `babyId` travels through the navigation stack: `HomeEffect.NavigateToFeeding(babyId)` → `FeedingNavigation(babyId)` → `FeedingViewModel(babyId)` → `FeedingSessionManager` → `FeedingRepository`. This keeps the `babyId` parameter explicit in `createBreastSession` and `createBottleSession`. For `closeSession` and `syncSwitchBreast` — which have no creation-time babyId — the repository reads `entity.babyId` from the existing `FeedingSessionEntity`.
- **Switch breast — fire-and-forget**: `switchBreastSide` network call is fire-and-forget. Local segment creation/close is the source of truth for elapsed time. The server call syncs state but does not block the UI.
- **Pause/Resume — local only**: There is no server endpoint for pause or resume. These actions remain local only. Segments created by pause/resume are tracked in the local DB and used for elapsed time calculation.
- **Complete session — network required**: `completeSession` is called on finish. Local DB is updated regardless of network outcome. This ensures the session is always marked closed locally even on network failure.
- **Bottle feeding — network required**: `recordBottleFeeding` is called before local insert. If the network call fails, the error propagates to the UI via `FeedingEffect.ShowError`. Bottle feeding has no ongoing timer, so the user can retry.
- **Error handling**: `startBreastfeeding` and `recordBottleFeeding` propagate exceptions to the ViewModel via `try/catch` in `FeedingSessionManager`. The ViewModel sends `FeedingEffect.ShowError`. Non-critical network calls (switch, complete) are fire-and-forget — local operations succeed regardless.
- **Domain ↔ Network mappers**: Private extension functions defined in `FeedingRepositoryImpl`. No separate mapper file to avoid over-engineering for two simple mappings.
  - `Breast.LEFT / RIGHT` ↔ `BreastSide.LEFT / RIGHT`
  - `BottleType.MOTHERS_MILK` ↔ `MilkType.BREAST_MILK`
  - `BottleType.POWDERED` ↔ `MilkType.POWDERED_MILK`
- **No UseCase layer**: Network integration goes directly into `FeedingRepositoryImpl`. The logic is a straightforward fetch+map pattern with a single repository, which does not meet the UseCase threshold defined in the architecture rules.

## Technical Features Implemented

| Layer | What was built |
|---|---|
| `HomeBabyInformation.kt` | Added `babyId: String` field |
| `HomeRepositoryImpl.kt` | Maps `BabyResponse.id.toString()` into `babyId` |
| `HomeState` / `HomeIntent.kt` | Added `babyId: String? = null` to `HomeState` |
| `HomeEffect.kt` | Changed `NavigateToFeeding` from `object` to `data class NavigateToFeeding(val babyId: String)` |
| `HomeViewModel.kt` | Sets `babyId` from API result; passes it in the `NavigateToFeeding` effect |
| `HomeScreen.kt` | Changed `onNavigateToFeeding: () -> Unit` to `onNavigateToFeeding: (String) -> Unit` |
| `FeedingNavigation.kt` | Changed from `data object` to `data class FeedingNavigation(val babyId: String) : NavKey` |
| `FeedingRepository.kt` | Kept `babyId` in `createBreastSession` and `createBottleSession`; added `syncSwitchBreast(sessionId, newBreast)` |
| `FeedingRepositoryImpl.kt` | Injected `FeedingService`; network calls for start/switch/complete/bottle; reads `entity.babyId` for close/switch; domain ↔ network mappers |
| `FeedingSessionManager.kt` | Added `babyId` param to `startBreastfeeding` and `startBottleFeeding`; removed UUID for session; calls `syncSwitchBreast` |
| `FeedingModule.kt` | Koin `viewModel { (babyId: String) -> FeedingViewModel(...) }`; injected `FeedingService` into repo |
| `FeedingViewModel.kt` | Added `babyId: String` constructor param; passes babyId to session manager; added `try/catch` + `ShowError` |
| `MainActivity.kt` | Wires `onNavigateToFeeding = { babyId -> backStack.add(FeedingNavigation(babyId)) }`; passes `parametersOf(key.babyId)` to ViewModel |
| `FeedingViewModel.kt` | Added `try/catch` around session manager calls; sends `FeedingEffect.ShowError` on network failure |

## Network Endpoints Used

| Operation | Endpoint | Strategy |
|---|---|---|
| Start breastfeeding | `POST /v1/baby/{babyId}/feeding/breastfeeding` | Blocking — returns server sessionId |
| Switch breast side | `PUT /v1/baby/{babyId}/feeding/{sessionId}/breastfeeding/switch` | Fire-and-forget |
| Complete session | `POST /v1/baby/{babyId}/feeding/{sessionId}/complete` | Fire-and-forget (local always closes) |
| Record bottle feeding | `POST /v1/baby/{babyId}/feeding/bottle` | Blocking — must succeed before local insert |
| Update session start time | `PUT /v1/baby/{babyId}/feeding/{sessionId}/start-time` | Blocking — local DB updated first, then network |

## Edit Start Time Feature

Allows the user to correct the start time of an active breastfeeding session after it has begun.

### Decisions Made

- **Two-step date/time picker**: A `DatePickerDialog` (full calendar) is shown first. On confirmation, an `AlertDialog` with a `TimePicker` is shown. This matches Material3 conventions and avoids a single complex combined picker.
- **Picker pre-populated from state**: `sessionStartedAt: Long?` was added to `FeedingState` and populated in `observeActiveSession`. This allows the date/time picker to open on the actual session start time instead of the current time.
- **Client-side future-time validation**: The ViewModel rejects any `ConfirmStartTime` intent where `newStartedAtMs >= System.currentTimeMillis()`. A `ShowError` effect is sent without touching the repository.
- **Local DB updated atomically with network call**: Inside `updateSessionStartTime` in the repository, the session entity's `startedAt` and the **first segment's** `startedAt` are updated in local DB. The first segment is identified by the smallest `startedAt` value among all segments of the session. Only after both local updates does the network call fire.
- **babyId read from local DB**: The repository reads `entity.babyId` from the existing `FeedingSessionEntity` (same pattern as `closeSession` and `syncSwitchBreast`). No `babyId` parameter is needed in `updateSessionStartTime`.
- **ISO-8601 UTC format**: The timestamp is serialized as `"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"` using `SimpleDateFormat` with `UTC` timezone, consistent with the project's existing date formatting approach.
- **Timezone-correct date+time combination**: The `DatePicker` returns epoch millis at midnight UTC. `StartTimePickerDialog` extracts the year/month/day components in UTC, then builds a `Calendar` in the device's local timezone using those components plus the selected hour/minute. This avoids off-by-one-day issues for users in non-UTC timezones.
- **State-driven dialog visibility**: `showStartTimePicker: Boolean` in `FeedingState` controls dialog display. This is state (not a one-shot effect) because the dialog can be dismissed and re-opened while a session is active.

### Components Added/Modified

| File | Change |
|---|---|
| `UpdateStartTimeRequest.kt` | New DTO — `started_at: String` (ISO-8601 UTC) |
| `FeedingService.kt` | New `PUT /v1/baby/{babyId}/feeding/{sessionId}/start-time` endpoint |
| `FeedingRepository.kt` | New `updateSessionStartTime(sessionId, newStartedAt)` method |
| `FeedingRepositoryImpl.kt` | Implementation: updates session + first segment in local DB, calls network; private `Long.toIso8601()` helper |
| `FeedingSessionManager.kt` | New `updateSessionStartTime(newStartedAt)`: delegates to repo, throws on `Resource.Error`, refreshes active session |
| `FeedingState.kt` | Added `sessionStartedAt: Long?` and `showStartTimePicker: Boolean` |
| `FeedingIntent.kt` | Added `UpdateDateTime` handler, `ConfirmStartTime(newStartedAtMs)`, `DismissStartTimePicker` |
| `FeedingViewModel.kt` | Handles new intents; `applyStartTimeChange` validates time, dismisses picker, calls session manager |
| `StartTimePickerDialog.kt` | New composable — two-step `DatePickerDialog` → `TimePicker` flow with timezone-safe combination |
| `FeedingScreen.kt` | Renders `StartTimePickerDialog` when `state.showStartTimePicker == true`; wires confirm/dismiss callbacks |
| `strings.xml` | Added `feeding_error_start_time_future`, `feeding_select_time` |

## Current Status

✅ Implementation complete.

**Known limitation:** `FeedingEffect.ShowError` carries a `@StringRes Int` but `FeedingScreen` displays it via `.toString()` (showing the raw resource ID integer). This is a pre-existing issue, not introduced by this integration.

## Last Updated

2026-05-09