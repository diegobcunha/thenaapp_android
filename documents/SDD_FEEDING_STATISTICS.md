# SDD — Feeding Statistics

## Context

This feature provides caregivers with aggregated data about a baby's feeding history. It surfaces
two surfaces:

1. **FeedingScreen summary** — a compact today-only stat card shown below the timer when no session
   is active.
2. **FeedingStatisticsScreen** — a dedicated screen with a period selector, aggregate cards, milk
   type breakdown, and a per-day `HorizontalPager` with a custom Canvas bar chart.

---

## Decisions Made

- **Today stats on FeedingScreen**: Loaded in `FeedingViewModel.init` via `repository.getStatistics(babyId)`
  (no params → defaults to today on the backend). Shown only when `sessionId == null` to avoid
  clutter during an active session. Errors are silently ignored (no snackbar for background loads).
- **Navigation to statistics**: `FeedingIntent.OpenStatistics` → `FeedingEffect.NavigateToStatistics`
  → `MainActivity` pushes `FeedingStatisticsNavigation(babyId)`. The 📊 icon is in the TopBar of
  `FeedingScreen`.
- **Period mapping to API params**:
  - TODAY → `date = today`
  - WEEK → `startDate = today-6, endDate = today`
  - MONTH → `startDate = today-29, endDate = today`
  - CUSTOM → `startDate = pickerStart, endDate = pickerEnd`
- **Custom date picker**: Material3 `DateRangePicker` inside `DatePickerDialog`. Confirm button
  is disabled until both dates are selected. Selection fires `SelectCustomDateRange`.
- **HorizontalPager for daily breakdown**: One page per `DailyFeedingStatistics` entry returned by
  the API. Page index tracked in `FeedingStatisticsState.currentPageIndex`. Pager-to-state sync
  uses `snapshotFlow { pagerState.currentPage }`.
- **Canvas bar chart**: Two bars side by side — primary color for breastfeeding, secondary for
  bottle. Heights are proportional to session counts relative to the maximum between the two. No
  charting library added.
- **No UseCase layer**: Single repository call + domain mapping only. Does not meet the UseCase
  threshold defined in the architecture rules.
- **FeedingRepository.getStatistics** added with nullable `date`, `startDate`, `endDate` params.
  All three were already nullable in the backend API spec; the Retrofit `FeedingService.getStatistics`
  params have been updated from `String` to `String?` accordingly.
- **FeedingStatisticsResponse DTO** updated to include `volumeByMilkType: Map<String, Long>` and
  `dailyBreakdown: List<DailyFeedingStatisticsResponse>` (both have defaults to preserve
  backwards compatibility with older server responses that omit these fields).

---

## Technical Features Implemented

| Layer | What was built |
|---|---|
| `DailyFeedingStatisticsResponse.kt` | New DTO for daily breakdown entries |
| `FeedingStatisticsResponse.kt` | Added `volumeByMilkType` and `dailyBreakdown` fields |
| `FeedingService.kt` | `getStatistics` params changed to nullable; `date` param added |
| `FeedingStatistics.kt` | New domain model (full API response mapped) |
| `DailyFeedingStatistics.kt` | New domain model for per-day entries |
| `FeedingRepository.kt` | Added `getStatistics(babyId, date?, startDate?, endDate?)` |
| `FeedingRepositoryImpl.kt` | `getStatistics` via `safeApiCall`; private mappers for both response types |
| `FeedingState.kt` | Added `todayStats: FeedingStatistics?` |
| `FeedingIntent.kt` | Added `OpenStatistics` |
| `FeedingEffect.kt` | Added `NavigateToStatistics` |
| `FeedingViewModel.kt` | Added `repository` param; `loadTodayStats()` in `init`; `OpenStatistics` handler |
| `FeedingModule.kt` | `FeedingViewModel` now injects `repository`; `FeedingStatisticsViewModel` registered |
| `FeedingScreen.kt` | 📊 icon in TopBar; `TodayFeedingSummary` card; `onNavigateToStatistics` callback |
| `FeedingStatisticsNavigation.kt` | New `NavKey` with `babyId` |
| `FeedingStatsPeriod.kt` | `TODAY / WEEK / MONTH / CUSTOM` enum |
| `FeedingStatisticsState.kt` | MVI state for statistics screen |
| `FeedingStatisticsIntent.kt` | `SelectPeriod`, `SelectCustomDateRange`, `DismissDateRangePicker`, `PageChanged` |
| `FeedingStatisticsEffect.kt` | `ShowError` |
| `FeedingStatisticsViewModel.kt` | Period→API param mapping; load statistics; date string helpers |
| `FeedingStatisticsScreen.kt` | Full screen: FilterChips, aggregate stat cards, milk type card, daily pager, Canvas bar chart |
| `strings.xml` | 18 new string resources for the statistics UI |
| `MainActivity.kt` | `onNavigateToStatistics` wired in `FeedingNavigation` entry; new `FeedingStatisticsNavigation` entry |
| `FeedingStatisticsViewModelTest.kt` | 9 unit tests covering period selection, date range, success/error, pager state |
| `FeedingViewModelTest.kt` | Updated to mock `FeedingRepository` for the new constructor param |

---

## Current Status

✅ Implementation complete. All unit tests pass (10 new + 25 existing feeding VM + 35 session manager).

---

## Last Updated

2026-05-12