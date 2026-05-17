package com.diegocunha.thenaapp.sleep.presentation

import app.cash.turbine.test
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.sleep.domain.SleepRepository
import com.diegocunha.thenaapp.sleep.domain.model.ActiveSleepSession
import com.diegocunha.thenaapp.sleep.domain.model.SleepDailyStats
import com.diegocunha.thenaapp.sleep.domain.model.SleepSession
import com.diegocunha.thenaapp.sleep.domain.model.SleepType
import com.diegocunha.thenaapp.sleep.session.SleepSessionManager
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
class SleepViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val sessionManager: SleepSessionManager = mockk()
    private val repository: SleepRepository = mockk()
    private val activeSessionFlow = MutableStateFlow<ActiveSleepSession?>(null)
    private val babyId = "test-baby-id"

    private lateinit var viewModel: SleepViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        justRun { sessionManager.restoreSession(any()) }
        every { sessionManager.activeSession } returns activeSessionFlow
        every { sessionManager.tickerFlow } returns emptyFlow()
        coEvery { repository.getDailyStats(any(), any()) } returns Resource.Error(Exception())
        coEvery { repository.listSessions(any(), any()) } returns Resource.Error(Exception())
        viewModel = SleepViewModel(sessionManager = sessionManager, repository = repository, babyId = babyId)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN init THEN restoreSession called with babyId`() = runTest {
        coVerify { sessionManager.restoreSession(babyId) }
    }

    @Test
    fun `WHEN active session emitted THEN state reflects running`() = runTest {
        val session = buildSession().toActive()
        activeSessionFlow.value = session

        assertTrue(viewModel.state.value.isRunning)
        assertEquals(session.id, viewModel.state.value.activeSessionId)
    }

    @Test
    fun `WHEN active session becomes null THEN state reflects stopped`() = runTest {
        activeSessionFlow.value = buildSession().toActive()
        activeSessionFlow.value = null

        assertFalse(viewModel.state.value.isRunning)
        assertEquals(null, viewModel.state.value.activeSessionId)
        assertEquals(0L, viewModel.state.value.elapsedSeconds)
    }

    @Test
    fun `WHEN StartSleep THEN sessionManager startSession called`() = runTest {
        coEvery { sessionManager.startSession(any(), any()) } returns buildSession().toActive()

        viewModel.sendIntent(SleepIntent.StartSleep)

        coVerify { sessionManager.startSession(babyId, any()) }
    }

    @Test
    fun `WHEN StartSleep throws THEN ShowError emitted`() = runTest {
        coEvery { sessionManager.startSession(any(), any()) } throws RuntimeException("network")

        viewModel.effects.test {
            viewModel.sendIntent(SleepIntent.StartSleep)

            assertTrue(awaitItem() is SleepEffect.ShowError)
        }
    }

    @Test
    fun `WHEN StopSleep THEN sessionManager stopSession called`() = runTest {
        activeSessionFlow.value = buildSession().toActive()
        coJustRun { sessionManager.stopSession(any()) }
        coEvery { repository.getDailyStats(any(), any()) } returns Resource.Success(buildStats())
        coEvery { repository.listSessions(any(), any()) } returns Resource.Success(emptyList())

        viewModel.sendIntent(SleepIntent.StopSleep)

        coVerify { sessionManager.stopSession(babyId) }
    }

    @Test
    fun `WHEN StopSleep throws THEN ShowError emitted`() = runTest {
        coEvery { sessionManager.stopSession(any()) } throws RuntimeException("network")

        viewModel.effects.test {
            viewModel.sendIntent(SleepIntent.StopSleep)

            assertTrue(awaitItem() is SleepEffect.ShowError)
        }
    }

    @Test
    fun `WHEN SelectSleepType THEN state activeSleepType updated`() = runTest {
        viewModel.sendIntent(SleepIntent.SelectSleepType(SleepType.NIGHT_SLEEP))

        assertEquals(SleepType.NIGHT_SLEEP, viewModel.state.value.activeSleepType)
    }

    @Test
    fun `WHEN ShowLogDialog THEN showLogDialog becomes true`() = runTest {
        viewModel.sendIntent(SleepIntent.ShowLogDialog)

        assertTrue(viewModel.state.value.showLogDialog)
    }

    @Test
    fun `WHEN DismissLogDialog THEN showLogDialog becomes false`() = runTest {
        viewModel.sendIntent(SleepIntent.ShowLogDialog)
        viewModel.sendIntent(SleepIntent.DismissLogDialog)

        assertFalse(viewModel.state.value.showLogDialog)
    }

    @Test
    fun `WHEN LogPastSleep with endMs before startMs THEN ShowError emitted`() = runTest {
        val now = System.currentTimeMillis()

        viewModel.effects.test {
            viewModel.sendIntent(SleepIntent.LogPastSleep(startMs = now, endMs = now - 1000))

            assertTrue(awaitItem() is SleepEffect.ShowError)
        }
    }

    @Test
    fun `WHEN LogPastSleep with valid times THEN repository logPastSession called`() = runTest {
        val start = System.currentTimeMillis() - 3600_000L
        val end = System.currentTimeMillis()
        coEvery { repository.logPastSession(any(), any(), any(), any()) } returns Resource.Success(buildSession())
        coEvery { repository.getDailyStats(any(), any()) } returns Resource.Success(buildStats())

        viewModel.sendIntent(SleepIntent.LogPastSleep(startMs = start, endMs = end))

        coVerify { repository.logPastSession(babyId, any(), start, end) }
    }

    @Test
    fun `WHEN LogPastSleep succeeds THEN session is prepended to list without calling listSessions`() = runTest {
        val session = buildSession()
        coEvery { repository.logPastSession(any(), any(), any(), any()) } returns Resource.Success(session)
        coEvery { repository.getDailyStats(any(), any()) } returns Resource.Error(Exception())

        viewModel.sendIntent(SleepIntent.LogPastSleep(
            startMs = System.currentTimeMillis() - 3600_000L,
            endMs = System.currentTimeMillis(),
        ))

        assertEquals(1, viewModel.state.value.sessions.size)
        assertEquals(session.id, viewModel.state.value.sessions.first().id)
        // exactly 1 = only from init's loadTodayData, not called again after logPastSession
        coVerify(exactly = 1) { repository.listSessions(any(), any()) }
    }

    @Test
    fun `WHEN LogPastSleep succeeds THEN getDailyStats is called to refresh stats`() = runTest {
        val stats = buildStats()
        coEvery { repository.logPastSession(any(), any(), any(), any()) } returns Resource.Success(buildSession())
        coEvery { repository.getDailyStats(any(), any()) } returns Resource.Success(stats)

        viewModel.sendIntent(SleepIntent.LogPastSleep(
            startMs = System.currentTimeMillis() - 3600_000L,
            endMs = System.currentTimeMillis(),
        ))

        assertNotNull(viewModel.state.value.todayStats)
        assertEquals(stats.sessionCount, viewModel.state.value.todayStats?.sessionCount)
    }

    @Test
    fun `WHEN LogPastSleep fails THEN sessions list unchanged and listSessions not called`() = runTest {
        coEvery { repository.logPastSession(any(), any(), any(), any()) } returns Resource.Error(Exception())

        viewModel.effects.test {
            viewModel.sendIntent(SleepIntent.LogPastSleep(
                startMs = System.currentTimeMillis() - 3600_000L,
                endMs = System.currentTimeMillis(),
            ))

            assertTrue(awaitItem() is SleepEffect.ShowError)
        }
        assertTrue(viewModel.state.value.sessions.isEmpty())
        coVerify(exactly = 1) { repository.listSessions(any(), any()) }
    }

    @Test
    fun `WHEN OpenStatistics THEN NavigateToStatistics emitted`() = runTest {
        viewModel.effects.test {
            viewModel.sendIntent(SleepIntent.OpenStatistics)

            assertEquals(SleepEffect.NavigateToStatistics, awaitItem())
        }
    }

    @Test
    fun `WHEN StopSleep succeeds THEN todayStats reloaded`() = runTest {
        val stats = buildStats()
        coJustRun { sessionManager.stopSession(any()) }
        coEvery { repository.getDailyStats(any(), any()) } returns Resource.Success(stats)
        coEvery { repository.listSessions(any(), any()) } returns Resource.Success(emptyList())

        viewModel.sendIntent(SleepIntent.StopSleep)

        assertNotNull(viewModel.state.value.todayStats)
        assertEquals(stats.sessionCount, viewModel.state.value.todayStats?.sessionCount)
        assertEquals(stats.date, viewModel.state.value.todayStats?.date)
    }

    private fun buildSession() = SleepSession(
        id = "test-session",
        startTimeMs = System.currentTimeMillis() - 600_000L,
        endTimeMs = null,
        durationMinutes = null,
        sleepType = SleepType.NAP,
        isActive = true,
    )

    private fun SleepSession.toActive() = ActiveSleepSession(
        id = id,
        startTimeMs = startTimeMs,
        sleepType = sleepType,
    )

    private fun buildStats() = SleepDailyStats(
        date = "2026-05-16",
        totalSleepMinutes = 360L,
        sessionCount = 3,
        napMinutes = 120L,
        nightSleepMinutes = 240L,
        efficiency = 0.85f,
        goalMinutes = 600,
        insight = null,
    )
}
