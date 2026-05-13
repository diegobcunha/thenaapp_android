package com.diegocunha.thenaapp.feature.feeding

import app.cash.turbine.test
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.feeding.domain.FeedingRepository
import com.diegocunha.thenaapp.feature.feeding.domain.model.DailyFeedingStatistics
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingStatistics
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingStatisticsEffect
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingStatisticsIntent
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingStatisticsViewModel
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingStatsPeriod
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedingStatisticsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: FeedingRepository = mockk()
    private val babyId = "test-baby-id"

    private lateinit var viewModel: FeedingStatisticsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { repository.getStatistics(any(), any(), any(), any()) } returns Resource.Success(buildFeedingStatistics())
        viewModel = FeedingStatisticsViewModel(repository = repository, babyId = babyId)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN initialized THEN loads today statistics with date param`() = runTest {
        coVerify { repository.getStatistics(babyId, date = any(), startDate = null, endDate = null) }
        assertNotNull(viewModel.state.value.statistics)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `WHEN period is WEEK THEN loads statistics with 7-day range`() = runTest {
        viewModel.sendIntent(FeedingStatisticsIntent.SelectPeriod(FeedingStatsPeriod.WEEK))

        coVerify {
            repository.getStatistics(
                babyId,
                date = null,
                startDate = any(),
                endDate = any(),
            )
        }
        assertEquals(FeedingStatsPeriod.WEEK, viewModel.state.value.selectedPeriod)
    }

    @Test
    fun `WHEN period is MONTH THEN loads statistics with 30-day range`() = runTest {
        viewModel.sendIntent(FeedingStatisticsIntent.SelectPeriod(FeedingStatsPeriod.MONTH))

        coVerify {
            repository.getStatistics(
                babyId,
                date = null,
                startDate = any(),
                endDate = any(),
            )
        }
        assertEquals(FeedingStatsPeriod.MONTH, viewModel.state.value.selectedPeriod)
    }

    @Test
    fun `WHEN CUSTOM period selected THEN shows date range picker`() = runTest {
        viewModel.sendIntent(FeedingStatisticsIntent.SelectPeriod(FeedingStatsPeriod.CUSTOM))

        assertEquals(FeedingStatsPeriod.CUSTOM, viewModel.state.value.selectedPeriod)
        assertTrue(viewModel.state.value.showDateRangePicker)
    }

    @Test
    fun `WHEN custom date range confirmed THEN loads statistics with custom range and dismisses picker`() = runTest {
        viewModel.sendIntent(FeedingStatisticsIntent.SelectPeriod(FeedingStatsPeriod.CUSTOM))

        val startMs = 1746057600000L // 2025-05-01
        val endMs = 1748649600000L   // 2025-05-31
        viewModel.sendIntent(FeedingStatisticsIntent.SelectCustomDateRange(startMs, endMs))

        assertFalse(viewModel.state.value.showDateRangePicker)
        assertEquals(FeedingStatsPeriod.CUSTOM, viewModel.state.value.selectedPeriod)
        assertNotNull(viewModel.state.value.customStartDate)
        assertNotNull(viewModel.state.value.customEndDate)
        coVerify {
            repository.getStatistics(
                babyId,
                date = null,
                startDate = any(),
                endDate = any(),
            )
        }
    }

    @Test
    fun `WHEN statistics load succeeds THEN state has statistics and isLoading false`() = runTest {
        val stats = buildFeedingStatistics()
        coEvery { repository.getStatistics(any(), any(), any(), any()) } returns Resource.Success(stats)

        viewModel.sendIntent(FeedingStatisticsIntent.SelectPeriod(FeedingStatsPeriod.TODAY))

        assertEquals(stats, viewModel.state.value.statistics)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `WHEN statistics load fails THEN ShowError effect emitted and isLoading false`() = runTest {
        coEvery { repository.getStatistics(any(), any(), any(), any()) } returns Resource.Error(Exception("network error"))

        viewModel.effects.test {
            viewModel.sendIntent(FeedingStatisticsIntent.SelectPeriod(FeedingStatsPeriod.TODAY))

            val effect = awaitItem()
            assertTrue(effect is FeedingStatisticsEffect.ShowError)
        }
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `WHEN page changes THEN currentPageIndex updated in state`() = runTest {
        viewModel.sendIntent(FeedingStatisticsIntent.PageChanged(2))

        assertEquals(2, viewModel.state.value.currentPageIndex)
    }

    @Test
    fun `WHEN period changes THEN currentPageIndex resets to 0`() = runTest {
        viewModel.sendIntent(FeedingStatisticsIntent.PageChanged(3))
        assertEquals(3, viewModel.state.value.currentPageIndex)

        viewModel.sendIntent(FeedingStatisticsIntent.SelectPeriod(FeedingStatsPeriod.WEEK))

        assertEquals(0, viewModel.state.value.currentPageIndex)
    }

    @Test
    fun `WHEN dismiss date range picker THEN showDateRangePicker is false`() = runTest {
        viewModel.sendIntent(FeedingStatisticsIntent.SelectPeriod(FeedingStatsPeriod.CUSTOM))
        assertTrue(viewModel.state.value.showDateRangePicker)

        viewModel.sendIntent(FeedingStatisticsIntent.DismissDateRangePicker)

        assertFalse(viewModel.state.value.showDateRangePicker)
    }

    private fun buildFeedingStatistics() = FeedingStatistics(
        periodStart = "2026-05-12",
        periodEnd = "2026-05-12",
        totalSessions = 5,
        breastfeedingSessions = 3,
        bottleSessions = 2,
        totalBreastfeedingDurationSeconds = 3600,
        averageBreastfeedingDurationSeconds = 1200,
        totalBottleVolumeMl = 240,
        averageBottleVolumeMl = 120,
        volumeByMilkType = mapOf("BREAST_MILK" to 120, "POWDERED_MILK" to 120),
        dailyBreakdown = listOf(
            DailyFeedingStatistics(
                date = "2026-05-12",
                totalSessions = 5,
                breastfeedingSessions = 3,
                bottleSessions = 2,
                totalBreastfeedingDurationSeconds = 3600,
                totalBottleVolumeMl = 240,
            ),
        ),
    )
}
