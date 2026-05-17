# SDD — Sleep Tracking

## Context

Parents need to monitor their baby's sleep to ensure healthy sleep habits aligned with WHO/NSF
recommendations. This feature introduces a full sleep tracking module covering:

1. **SleepScreen** — live timer with Start/Stop, today stats (total sleep, sessions, efficiency),
   a daily sleep-goal card with circular progress, and a sessions log with manual retroactive entry.
2. **SleepStatisticsScreen** — dedicated statistics screen with Today/Week/Month period filter,
   daily breakdown pager, and a Canvas bar chart.
3. **Home screen update** — the existing Sleep quick-access card is wired to navigate to
   `SleepScreen` and shows today's total sleep minutes.

Backend: `POST|PATCH /v1/baby/{babyId}/sleep/sessions`, `GET /stats/daily`, `GET /stats/weekly`,
`GET /next-nap`, `GET /schedule` — see backend SDD v1.0.

---

## Decisions Made

- **Immediate save on Stop**: No end-session bottom sheet. Tapping Stop calls
  `PATCH /sessions/{id}/end` immediately. Optional fields (quality, location, onset, noise) are
  deferred to a future iteration.
- **Separate statistics screen**: Mirrors the FeedingStatistics pattern. The calendar icon (📅)
  in `SleepScreen`'s TopAppBar navigates to `SleepStatisticsNavigation(babyId)`.
- **Manual past-session logging**: FAB on `SleepScreen` opens `LogSleepDialog` with two time
  pickers (start / end). Calls `POST /sessions` with both `startTime` and `endTime` populated.
  Validates `endTime > startTime` client-side.
- **No pause support**: Backend `PATCH /sessions/{id}/end` finalises a session; there is no
  pause/resume API. The timer only shows Start and Stop.
- **SleepSessionManager singleton**: App-scoped Koin singleton mirroring `FeedingSessionManager`.
  Restores active session on init by calling `SleepApiService.getActiveSession()`. Starts/stops
  `SleepTimerService` (foreground service) automatically.
- **SleepTimerService**: Dedicated foreground service, notification channel `"sleep_timer"`,
  notification ID `1002`. Updates notification with `formatElapsedSeconds()` every second.
  Stops itself when `activeSession` becomes null.
- **CircularProgressWithLabel added to coreui**: The sleep goal card needs a circular chart with
  a label (e.g. "6h") and sublabel (e.g. "of 10h") drawn in the centre. The same component will
  serve the future VaccineScreen, so it belongs in `:coreui`.
- **Reused coreui components**: `CardButtonInformation`, `PeriodFilerComponent`,
  `PagerIndicator`, `StartTimePickerDialog`, `ElapsedTimeFormatter` — no new coreui components
  beyond `CircularProgressWithLabel`.
- **No UseCase layer**: All repository calls are straightforward fetch+map. Does not meet the
  UseCase threshold from the architecture rules.
- **Home card wired via datasource**: `HomeRepositoryImpl` injects `SleepApiService` (added to
  datasource) to load `todaySleepMinutes`. The home module already depends on `:datasource`; no
  new dependency added. Failure to load sleep data is silent (card falls back to icon-only).
- **Stats period mapping**:
  - TODAY → `date = today (yyyy-MM-dd)`
  - WEEK → `weekStart = today-6 (yyyy-MM-dd)`
  - MONTH → `month = today (yyyy-MM)` via `/stats/monthly`
- **Weekly bar chart**: Canvas-drawn, one bar per day, height proportional to `totalSleepMinutes`
  vs. daily maximum. Color: `ThenaTheme.extendedColors.sleepFill`. Mirrors FeedingStatisticsScreen's
  bar chart pattern.

---

## Architecture

### New Module: `:feature:sleep`

```
feature/sleep/
  di/                     SleepModule.kt
  domain/
    SleepRepository.kt
    model/                SleepSession, ActiveSleepSession, SleepType,
                          SleepDailyStats, SleepWeeklyStats, NextNapSuggestion, SleepSchedule
  repository/             SleepRepositoryImpl.kt
  session/                SleepSessionManager.kt
  service/                SleepTimerService.kt
  presentation/
    SleepState/Intent/Effect/ViewModel/Screen.kt
    SleepStatistics(State/Intent/Effect/ViewModel/Screen).kt
    SleepStatsPeriod.kt
    components/           SleepTimerCard, SleepGoalCard, SleepSessionItem, LogSleepDialog
    navigation/           SleepNavigation(babyId), SleepStatisticsNavigation(babyId)
  src/main/res/values/    strings.xml
  src/test/               SleepViewModelTest, SleepStatisticsViewModelTest, SleepSessionManagerTest
```

### Datasource additions

| File | Purpose |
|------|---------|
| `SleepApiService.kt` | Retrofit interface (start, end, getActive, list, dailyStats, weeklyStats, nextNap, schedule) |
| `StartSleepSessionRequest` | startTime (ISO8601 offset), endTime?, timezone |
| `EndSleepSessionRequest` | endTime (ISO8601 offset) |
| `SleepSessionResponse` | id, startTime, endTime?, durationMinutes?, sleepType, isActive |
| `SleepActiveSessionResponse` | id, startTime, elapsedMinutes, sleepType |
| `SleepDailyStatsResponse` | all daily aggregates + insight string |
| `SleepWeeklyStatsResponse` | days list + weeklyAvg + trend enum |
| `NextNapSuggestionResponse` | suggestedNapStart, windowOpen/Close, urgency, wakeWindowMinutes |
| `SleepScheduleResponse` | date, naps (NapScheduleItem list), nightSleep, totals |
| `SleepTypeResponse` enum | NAP, NIGHT_SLEEP, EARLY_MORNING, CATNAP, CONTACT_NAP, CAR_NAP |
| `DatasourceModule.kt` | Register `SleepApiService` singleton |

### coreui addition

`CircularProgressWithLabel.kt` — draws an M3 `CircularProgressIndicator` with a `label` (large)
and `sublabel` (small) overlaid in the centre via `Box`. Parameters: `progress: Float`,
`size: Dp`, `color: Color`, `label: String`, `sublabel: String`, `strokeWidth: Dp`.

---

## Technical Features Planned

| Layer | What will be built |
|-------|--------------------|
| `SleepApiService.kt` | 8 Retrofit endpoints |
| `Sleep*Response` DTOs (×9) | Datasource model layer |
| `DatasourceModule.kt` | Register SleepApiService |
| `CircularProgressWithLabel.kt` (coreui) | Reusable circular progress component |
| `SleepRepository.kt` | Domain interface (9 methods) |
| `SleepSession`, `ActiveSleepSession`, `SleepType` | Core domain models |
| `SleepDailyStats`, `SleepWeeklyStats`, `NextNapSuggestion`, `SleepSchedule` | Domain aggregates |
| `SleepRepositoryImpl.kt` | safeApiCall + DTO→domain mapping |
| `SleepSessionManager.kt` | Active session StateFlow + ticker + service lifecycle |
| `SleepTimerService.kt` | Foreground service (channel: sleep_timer, id: 1002) |
| `SleepState/Intent/Effect.kt` | MVI contracts for main screen |
| `SleepViewModel.kt` | Start, stop, log-past, load stats, ticker |
| `SleepTimerCard.kt` | Gradient card with HH:MM:SS, Start/Stop buttons |
| `SleepGoalCard.kt` | CircularProgressWithLabel + LinearProgress + insight |
| `SleepSessionItem.kt` | Session log row (icon, type, start→end, duration) |
| `LogSleepDialog.kt` | Past-session AlertDialog with two time pickers |
| `SleepScreen.kt` | Full screen — @TraceRecomposition |
| `SleepNavigation.kt` | NavKey data class with babyId |
| `SleepStatsPeriod.kt` | TODAY / WEEK / MONTH enum |
| `SleepStatisticsState/Intent/Effect.kt` | MVI contracts for statistics screen |
| `SleepStatisticsViewModel.kt` | Period selection + API load |
| `SleepStatisticsScreen.kt` | PeriodFilter, pager, Canvas bar chart — @TraceRecomposition |
| `SleepStatisticsNavigation.kt` | NavKey data class with babyId |
| `SleepModule.kt` | Koin module (session manager singleton, VMs factory) |
| `settings.gradle.kts` | Include `:feature:sleep` |
| `ThenaApplication.kt` | loadKoinModules(sleepModule) |
| `MainActivity.kt` | entry<SleepNavigation> + entry<SleepStatisticsNavigation> |
| `app/build.gradle.kts` | implementation(project(":feature:sleep")) |
| `HomeEffect.kt` | NavigateToSleep(babyId) |
| `HomeState.kt` | todaySleepMinutes: Int? |
| `HomeViewModel.kt` | SleepInfo → NavigateToSleep; load today sleep |
| `HomeScreen.kt` | Sleep card update + onNavigateToSleep callback |
| `HomeRepositoryImpl.kt` | getTodaySleepMinutes() via SleepApiService |
| `strings.xml` (sleep) | All UI string resources |
| `SleepViewModelTest.kt` | 5+ unit tests (start, stop, log, error, nav) |
| `SleepStatisticsViewModelTest.kt` | Period switching + load success/error |
| `SleepSessionManagerTest.kt` | Start, stop, restore session |

---

## Unit Test Coverage Targets

| Class | Target |
|-------|--------|
| `SleepViewModel` | ≥80% |
| `SleepStatisticsViewModel` | ≥80% |
| `SleepSessionManager` | ≥80% |
| `SleepRepositoryImpl` | ≥80% |

Test naming convention: `` `WHEN <condition> THEN <expectation>` ``

Tool chain: UnconfinedTestDispatcher + MockK + Turbine

---

## Files Modified (Existing Modules)

| Module | File | Change |
|--------|------|--------|
| `:datasource` | `DatasourceModule.kt` | +SleepApiService singleton |
| `:coreui` | `CircularProgressWithLabel.kt` | New component |
| `:app` | `ThenaApplication.kt` | +sleepModule |
| `:app` | `MainActivity.kt` | +2 nav entries + HomeScreen callback |
| `:app` | `build.gradle.kts` | +feature:sleep dep |
| `:feature:home` | `HomeEffect.kt` | +NavigateToSleep |
| `:feature:home` | `HomeState.kt` | +todaySleepMinutes |
| `:feature:home` | `HomeViewModel.kt` | SleepInfo wired |
| `:feature:home` | `HomeScreen.kt` | Sleep card + callback |
| `:feature:home` | `HomeRepositoryImpl.kt` | +getTodaySleepMinutes |
| `settings.gradle.kts` | — | +:feature:sleep |

---

## Current Status

📋 Specification complete — implementation pending.

---

## Last Updated

2026-05-16
