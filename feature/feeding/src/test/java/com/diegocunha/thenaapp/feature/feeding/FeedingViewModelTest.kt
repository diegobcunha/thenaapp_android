package com.diegocunha.thenaapp.feature.feeding

import app.cash.turbine.test
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.feeding.domain.FeedingRepository
import com.diegocunha.thenaapp.feature.feeding.domain.model.ActiveFeedingSession
import com.diegocunha.thenaapp.feature.feeding.domain.model.BottleType
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingStatistics
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingType
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingEffect
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingIntent
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingViewModel
import com.diegocunha.thenaapp.feature.feeding.session.FeedingSessionManager
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedingViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val sessionManager: FeedingSessionManager = mockk()
    private val repository: FeedingRepository = mockk()
    private val activeSessionFlow = MutableStateFlow<ActiveFeedingSession?>(null)
    private val babyId = "test-baby-id"

    private lateinit var viewModel: FeedingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        with(sessionManager) {
            coJustRun { pauseCurrentBreast() }
            coJustRun { finishSession() }
            coJustRun { switchBreast(any()) }
            coJustRun { resumeBreast(any()) }
            coJustRun { startBottleFeeding(any(), any(), any()) }
            coJustRun { updateSessionStartTime(any()) }
            coJustRun { updateBreastStartTime(any(), any()) }
        }
        every { sessionManager.activeSession } returns activeSessionFlow
        every { sessionManager.tickerFlow } returns emptyFlow()
        coEvery { repository.getStatistics(any(), any(), any(), any()) } returns Resource.Error(Exception())
        viewModel = FeedingViewModel(sessionManager = sessionManager, repository = repository, babyId = babyId)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN SelectBreastfeeding THEN feedingType is BREAST`() = runTest {
        viewModel.sendIntent(FeedingIntent.SelectBreastfeeding)

        assertEquals(FeedingType.BREAST, viewModel.state.value.feedingType)
    }

    @Test
    fun `WHEN SelectBottle THEN feedingType is BOTTLE`() = runTest {
        viewModel.sendIntent(FeedingIntent.SelectBottle)

        assertEquals(FeedingType.BOTTLE, viewModel.state.value.feedingType)
    }

    @Test
    fun `WHEN TapBreast with no active session THEN startBreastfeeding is called with babyId`() = runTest {
        coEvery { sessionManager.startBreastfeeding(Breast.LEFT, babyId) } coAnswers {
            activeSessionFlow.emit(buildSession(activeBreast = Breast.LEFT))
        }

        with(viewModel) {
            sendIntent(FeedingIntent.SelectBreastfeeding)
            sendIntent(FeedingIntent.TapBreast(Breast.LEFT))
        }

        coVerify { sessionManager.startBreastfeeding(Breast.LEFT, babyId) }
    }

    @Test
    fun `WHEN TapBreast on same active breast THEN pauseCurrentBreast is called`() = runTest {
        activeSessionFlow.value = buildSession(activeBreast = Breast.LEFT)

        viewModel.sendIntent(FeedingIntent.TapBreast(Breast.LEFT))

        coVerify { sessionManager.pauseCurrentBreast() }
    }

    @Test
    fun `WHEN TapBreast on other breast while one is active THEN switchBreast is called`() = runTest {
        activeSessionFlow.value = buildSession(activeBreast = Breast.LEFT)

        viewModel.sendIntent(FeedingIntent.TapBreast(Breast.RIGHT))

        coVerify { sessionManager.switchBreast(Breast.RIGHT) }
    }

    @Test
    fun `WHEN TapBreast while no breast active THEN resumeBreast is called`() = runTest {
        activeSessionFlow.value = buildSession(activeBreast = null)

        viewModel.sendIntent(FeedingIntent.TapBreast(Breast.RIGHT))

        coVerify { sessionManager.resumeBreast(Breast.RIGHT) }
    }

    @Test
    fun `WHEN StopSession THEN finishSession called and no NavigateBack effect emitted`() = runTest {
        activeSessionFlow.value = buildSession(activeBreast = Breast.LEFT)
        coEvery { repository.getStatistics(any(), any(), any(), any()) } returns Resource.Success(buildStats())

        viewModel.effects.test {
            viewModel.sendIntent(FeedingIntent.StopSession)

            coVerify { sessionManager.finishSession() }
            expectNoEvents()
        }
    }

    @Test
    fun `WHEN StopSession succeeds THEN todayStats is reloaded`() = runTest {
        val stats = buildStats()
        coEvery { repository.getStatistics(any(), any(), any(), any()) } returns Resource.Success(stats)
        activeSessionFlow.value = buildSession(activeBreast = Breast.LEFT)

        viewModel.sendIntent(FeedingIntent.StopSession)

        assertEquals(stats, viewModel.state.value.todayStats)
    }

    @Test
    fun `WHEN SaveBottleFeeding with empty ml THEN ShowError emitted`() = runTest {
        with(viewModel) {
            sendIntent(FeedingIntent.SelectBottle)
            sendIntent(FeedingIntent.SelectBottleType(BottleType.MOTHERS_MILK))
        }

        viewModel.effects.test {
            viewModel.sendIntent(FeedingIntent.SaveBottleFeeding)

            val effect = awaitItem()
            assert(effect is FeedingEffect.ShowError)
        }
    }

    @Test
    fun `WHEN SaveBottleFeeding with no bottle type THEN ShowError emitted`() = runTest {
        viewModel.sendIntent(FeedingIntent.SelectBottle)
        viewModel.sendIntent(FeedingIntent.UpdateBottleMl("120"))

        viewModel.effects.test {
            viewModel.sendIntent(FeedingIntent.SaveBottleFeeding)

            val effect = awaitItem()
            assert(effect is FeedingEffect.ShowError)
        }
    }

    @Test
    fun `WHEN SaveBottleFeeding with valid data THEN startBottleFeeding called and no NavigateBack emitted`() = runTest {
        coEvery { repository.getStatistics(any(), any(), any(), any()) } returns Resource.Success(buildStats())
        with(viewModel) {
            sendIntent(FeedingIntent.SelectBottle)
            sendIntent(FeedingIntent.UpdateBottleMl("120"))
            sendIntent(FeedingIntent.SelectBottleType(BottleType.POWDERED))
        }

        viewModel.effects.test {
            viewModel.sendIntent(FeedingIntent.SaveBottleFeeding)

            coVerify { sessionManager.startBottleFeeding(BottleType.POWDERED, 120, babyId) }
            expectNoEvents()
        }
    }

    @Test
    fun `WHEN SaveBottleFeeding succeeds THEN bottle fields reset and todayStats reloaded`() = runTest {
        val stats = buildStats()
        coEvery { repository.getStatistics(any(), any(), any(), any()) } returns Resource.Success(stats)
        with(viewModel) {
            sendIntent(FeedingIntent.SelectBottle)
            sendIntent(FeedingIntent.UpdateBottleMl("120"))
            sendIntent(FeedingIntent.SelectBottleType(BottleType.POWDERED))
        }

        viewModel.sendIntent(FeedingIntent.SaveBottleFeeding)

        val state = viewModel.state.value
        assertEquals(null, state.feedingType)
        assertEquals("", state.bottleMl)
        assertEquals(null, state.bottleType)
        assertEquals(stats, state.todayStats)
    }

    @Test
    fun `WHEN active session is restored from manager THEN state reflects it`() = runTest {
        val session = buildSession(activeBreast = Breast.RIGHT)
        activeSessionFlow.value = session

        val state = viewModel.state.value
        assertEquals(session.sessionId, state.sessionId)
        assertEquals(FeedingType.BREAST, state.feedingType)
        assertEquals(Breast.RIGHT, state.activeBreast)
    }

    @Test
    fun `WHEN TapBreast with no session and startBreastfeeding throws THEN ShowError is emitted`() = runTest {
        coEvery { sessionManager.startBreastfeeding(any(), any()) } throws RuntimeException("network failure")

        viewModel.effects.test {
            viewModel.sendIntent(FeedingIntent.TapBreast(Breast.LEFT))

            val effect = awaitItem()
            assert(effect is FeedingEffect.ShowError)
        }
    }

    @Test
    fun `WHEN StopSession and finishSession throws THEN ShowError is emitted and NavigateBack is not`() = runTest {
        coEvery { sessionManager.finishSession() } throws RuntimeException("network failure")

        viewModel.effects.test {
            viewModel.sendIntent(FeedingIntent.StopSession)

            val effect = awaitItem()
            assert(effect is FeedingEffect.ShowError)
            expectNoEvents()
        }
    }

    @Test
    fun `WHEN SaveBottleFeeding with valid data and network fails THEN ShowError emitted and no NavigateBack`() = runTest {
        coEvery { sessionManager.startBottleFeeding(any(), any(), any()) } throws RuntimeException("network failure")
        with(viewModel) {
            sendIntent(FeedingIntent.SelectBottle)
            sendIntent(FeedingIntent.UpdateBottleMl("120"))
            sendIntent(FeedingIntent.SelectBottleType(BottleType.POWDERED))
        }

        viewModel.effects.test {
            viewModel.sendIntent(FeedingIntent.SaveBottleFeeding)

            val effect = awaitItem()
            assert(effect is FeedingEffect.ShowError)
            expectNoEvents()
        }
    }

    @Test
    fun `WHEN UpdateBottleMl THEN state bottleMl is updated`() = runTest {
        viewModel.sendIntent(FeedingIntent.UpdateBottleMl("90"))

        assertEquals("90", viewModel.state.value.bottleMl)
    }

    @Test
    fun `WHEN SelectBottleType THEN state bottleType is updated`() = runTest {
        viewModel.sendIntent(FeedingIntent.SelectBottleType(BottleType.MOTHERS_MILK))

        assertEquals(BottleType.MOTHERS_MILK, viewModel.state.value.bottleType)
    }

    @Test
    fun `WHEN UpdateDateTime THEN showStartTimePicker becomes true`() = runTest {
        viewModel.sendIntent(FeedingIntent.UpdateDateTime)

        assertEquals(true, viewModel.state.value.showStartTimePicker)
    }

    @Test
    fun `WHEN DismissStartTimePicker THEN showStartTimePicker becomes false`() = runTest {
        viewModel.sendIntent(FeedingIntent.UpdateDateTime)
        viewModel.sendIntent(FeedingIntent.DismissStartTimePicker)

        assertEquals(false, viewModel.state.value.showStartTimePicker)
    }

    @Test
    fun `WHEN ConfirmStartTime with future time THEN ShowError emitted and picker stays closed`() = runTest {
        val futureTime = System.currentTimeMillis() + 60_000L

        viewModel.effects.test {
            viewModel.sendIntent(FeedingIntent.ConfirmStartTime(futureTime))

            assert(awaitItem() is FeedingEffect.ShowError)
        }
        assertEquals(false, viewModel.state.value.showStartTimePicker)
    }

    @Test
    fun `WHEN ConfirmStartTime with valid past time THEN breast picker shown and start time picker dismissed`() = runTest {
        val pastTime = System.currentTimeMillis() - 60_000L
        viewModel.sendIntent(FeedingIntent.UpdateDateTime)
        viewModel.sendIntent(FeedingIntent.ConfirmStartTime(pastTime))

        assertEquals(false, viewModel.state.value.showStartTimePicker)
        assertEquals(true, viewModel.state.value.showBreastPickerForTimeChange)
        assertEquals(pastTime, viewModel.state.value.pendingNewStartedAtMs)
    }

    @Test
    fun `WHEN ConfirmBreastForTimeChange and updateBreastStartTime throws network error THEN network ShowError emitted`() = runTest {
        coEvery { sessionManager.updateBreastStartTime(any(), any()) } throws RuntimeException("network error")
        val pastTime = System.currentTimeMillis() - 60_000L
        viewModel.sendIntent(FeedingIntent.ConfirmStartTime(pastTime))

        viewModel.effects.test {
            viewModel.sendIntent(FeedingIntent.ConfirmBreastForTimeChange(Breast.LEFT))

            val effect = awaitItem()
            assert(effect is FeedingEffect.ShowError)
        }
    }

    @Test
    fun `WHEN ConfirmBreastForTimeChange and updateBreastStartTime throws IllegalArgumentException THEN overlap ShowError emitted`() = runTest {
        coEvery { sessionManager.updateBreastStartTime(any(), any()) } throws IllegalArgumentException("overlap")
        val pastTime = System.currentTimeMillis() - 60_000L
        viewModel.sendIntent(FeedingIntent.ConfirmStartTime(pastTime))

        viewModel.effects.test {
            viewModel.sendIntent(FeedingIntent.ConfirmBreastForTimeChange(Breast.LEFT))

            val effect = awaitItem() as FeedingEffect.ShowError
            assertEquals(R.string.feeding_error_start_time_overlap, effect.message)
        }
    }

    @Test
    fun `WHEN ConfirmBreastForTimeChange THEN updateBreastStartTime called with correct breast and pending time`() = runTest {
        val pastTime = System.currentTimeMillis() - 60_000L
        viewModel.sendIntent(FeedingIntent.ConfirmStartTime(pastTime))
        viewModel.sendIntent(FeedingIntent.ConfirmBreastForTimeChange(Breast.LEFT))

        coVerify { sessionManager.updateBreastStartTime(Breast.LEFT, pastTime) }
        assertEquals(false, viewModel.state.value.showBreastPickerForTimeChange)
        assertEquals(null, viewModel.state.value.pendingNewStartedAtMs)
    }

    @Test
    fun `WHEN ConfirmBreastForTimeChange with no pending time THEN nothing happens`() = runTest {
        viewModel.sendIntent(FeedingIntent.ConfirmBreastForTimeChange(Breast.RIGHT))

        coVerify(inverse = true) { sessionManager.updateBreastStartTime(any(), any()) }
    }

    @Test
    fun `WHEN DismissBreastPickerForTimeChange THEN picker dismissed and pending time cleared`() = runTest {
        val pastTime = System.currentTimeMillis() - 60_000L
        viewModel.sendIntent(FeedingIntent.ConfirmStartTime(pastTime))
        viewModel.sendIntent(FeedingIntent.DismissBreastPickerForTimeChange)

        assertEquals(false, viewModel.state.value.showBreastPickerForTimeChange)
        assertEquals(null, viewModel.state.value.pendingNewStartedAtMs)
    }

    @Test
    fun `WHEN active session becomes null THEN session state fields are cleared`() = runTest {
        activeSessionFlow.value = buildSession(activeBreast = Breast.LEFT)

        activeSessionFlow.value = null

        val state = viewModel.state.value
        assertEquals(null, state.sessionId)
        assertEquals(null, state.feedingType)
        assertEquals(null, state.activeBreast)
        assertEquals(null, state.sessionStartedAt)
        assertEquals(0L, state.leftElapsedSeconds)
        assertEquals(0L, state.rightElapsedSeconds)
        assertEquals(0L, state.totalElapsedSeconds)
    }

    private fun buildSession(activeBreast: Breast?) = ActiveFeedingSession(
        sessionId = "test-session",
        type = FeedingType.BREAST,
        startedAt = System.currentTimeMillis(),
        activeBreast = activeBreast,
        leftSegments = emptyList(),
        rightSegments = emptyList(),
    )

    private fun buildStats() = FeedingStatistics(
        periodStart = "2026-05-12",
        periodEnd = "2026-05-12",
        totalSessions = 3,
        breastfeedingSessions = 2,
        bottleSessions = 1,
        totalBreastfeedingDurationSeconds = 600L,
        averageBreastfeedingDurationSeconds = 300L,
        totalBottleVolumeMl = 120,
        averageBottleVolumeMl = 120,
        volumeByMilkType = emptyMap(),
        dailyBreakdown = emptyList(),
    )
}
