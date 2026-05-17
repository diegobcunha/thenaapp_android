package com.diegocunha.thenaapp.sleep.presentation

import app.cash.turbine.test
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.sleep.domain.SleepRepository
import com.diegocunha.thenaapp.sleep.domain.model.SleepDailyStats
import com.diegocunha.thenaapp.sleep.domain.model.SleepWeeklyStats
import io.mockk.coEvery
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepStatisticsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: SleepRepository = mockk()
    private val babyId = "test-baby-id"

    private lateinit var viewModel: SleepStatisticsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        // Success on getDailyStats so init doesn't emit ShowError effects into the channel
        coEvery { repository.getDailyStats(any(), any()) } returns Resource.Success(buildDailyStats())
        coEvery { repository.getWeeklyStats(any(), any()) } returns Resource.Error(Exception())
        viewModel = SleepStatisticsViewModel(repository = repository, babyId = babyId)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN init with TODAY THEN dailyStats populated`() = runTest {
        assertNotNull(viewModel.state.value.dailyStats)
    }

    @Test
    fun `WHEN SelectPeriod WEEK THEN getWeeklyStats called`() = runTest {
        coEvery { repository.getWeeklyStats(any(), any()) } returns Resource.Success(buildWeeklyStats())

        viewModel.sendIntent(SleepStatisticsIntent.SelectPeriod(SleepStatsPeriod.WEEK))

        assertEquals(SleepStatsPeriod.WEEK, viewModel.state.value.selectedPeriod)
        assertNotNull(viewModel.state.value.weeklyStats)
    }

    @Test
    fun `WHEN SelectPeriod TODAY THEN getDailyStats called`() = runTest {
        viewModel.sendIntent(SleepStatisticsIntent.SelectPeriod(SleepStatsPeriod.TODAY))

        assertEquals(SleepStatsPeriod.TODAY, viewModel.state.value.selectedPeriod)
        assertNotNull(viewModel.state.value.dailyStats)
    }

    @Test
    fun `WHEN load succeeds THEN isLoading becomes false`() = runTest {
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `WHEN getDailyStats returns error THEN ShowError emitted and isLoading false`() = runTest {
        coEvery { repository.getDailyStats(any(), any()) } returns Resource.Error(Exception("network"))

        viewModel.effects.test {
            viewModel.sendIntent(SleepStatisticsIntent.SelectPeriod(SleepStatsPeriod.TODAY))

            assertTrue(awaitItem() is SleepStatisticsEffect.ShowError)
        }
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `WHEN getWeeklyStats returns error THEN ShowError emitted`() = runTest {
        viewModel.effects.test {
            viewModel.sendIntent(SleepStatisticsIntent.SelectPeriod(SleepStatsPeriod.WEEK))

            assertTrue(awaitItem() is SleepStatisticsEffect.ShowError)
        }
    }

    @Test
    fun `WHEN SelectPeriod THEN currentPageIndex resets to 0`() = runTest {
        viewModel.sendIntent(SleepStatisticsIntent.PageChanged(3))
        viewModel.sendIntent(SleepStatisticsIntent.SelectPeriod(SleepStatsPeriod.WEEK))

        assertEquals(0, viewModel.state.value.currentPageIndex)
    }

    @Test
    fun `WHEN PageChanged THEN currentPageIndex updated`() = runTest {
        viewModel.sendIntent(SleepStatisticsIntent.PageChanged(2))

        assertEquals(2, viewModel.state.value.currentPageIndex)
    }

    @Test
    fun `WHEN SelectPeriod TODAY after WEEK THEN period updates correctly`() = runTest {
        coEvery { repository.getWeeklyStats(any(), any()) } returns Resource.Success(buildWeeklyStats())
        viewModel.sendIntent(SleepStatisticsIntent.SelectPeriod(SleepStatsPeriod.WEEK))

        viewModel.sendIntent(SleepStatisticsIntent.SelectPeriod(SleepStatsPeriod.TODAY))

        assertEquals(SleepStatsPeriod.TODAY, viewModel.state.value.selectedPeriod)
    }

    @Test
    fun `WHEN dailyStats loaded THEN state dailyStats has correct mapped values`() = runTest {
        val stats = buildDailyStats()
        coEvery { repository.getDailyStats(any(), any()) } returns Resource.Success(stats)

        viewModel.sendIntent(SleepStatisticsIntent.SelectPeriod(SleepStatsPeriod.TODAY))

        val ui = viewModel.state.value.dailyStats
        assertNotNull(ui)
        assertEquals(stats.date, ui?.date)
        assertEquals(stats.sessionCount, ui?.sessionCount)
        assertEquals("6h 0m", ui?.totalDisplay)
    }

    @Test
    fun `WHEN weeklyStats loaded THEN state weeklyStats has correct mapped values`() = runTest {
        val stats = buildWeeklyStats()
        coEvery { repository.getWeeklyStats(any(), any()) } returns Resource.Success(stats)

        viewModel.sendIntent(SleepStatisticsIntent.SelectPeriod(SleepStatsPeriod.WEEK))

        val ui = viewModel.state.value.weeklyStats
        assertNotNull(ui)
        assertEquals(stats.trend, ui?.trend)
        assertEquals("8h 0m", ui?.weeklyAvgDisplay)
    }

    @Test
    fun `WHEN init THEN weeklyStats is null`() = runTest {
        assertNull(viewModel.state.value.weeklyStats)
    }

    private fun buildDailyStats() = SleepDailyStats(
        date = "2026-05-16",
        totalSleepMinutes = 360L,
        sessionCount = 2,
        napMinutes = 120L,
        nightSleepMinutes = 240L,
        efficiency = 0.80f,
        goalMinutes = 600,
        insight = null,
    )

    private fun buildWeeklyStats() = SleepWeeklyStats(
        days = emptyList(),
        weeklyAvgMinutes = 480L,
        trend = "STABLE",
    )
}
