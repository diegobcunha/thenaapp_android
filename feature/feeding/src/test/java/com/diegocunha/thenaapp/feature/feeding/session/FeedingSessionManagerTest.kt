package com.diegocunha.thenaapp.feature.feeding.session

import android.content.Context
import android.content.Intent
import app.cash.turbine.test
import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.feeding.domain.FeedingRepository
import com.diegocunha.thenaapp.feature.feeding.domain.model.ActiveFeedingSession
import com.diegocunha.thenaapp.feature.feeding.domain.model.BottleType
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingType
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedingSessionManagerTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: FeedingRepository = mockk()
    private val context: Context = mockk()
    private val dispatchersProvider: DispatchersProvider = mockk {
        every { io() } returns dispatcher
    }
    private val babyId = "test-baby-id"

    private lateinit var manager: FeedingSessionManager

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkConstructor(Intent::class)
        coEvery { context.stopService(any()) } returns true
        coEvery { context.startForegroundService(any()) } returns mockk()
        coEvery { repository.getActiveSession() } returns null
        manager = buildManager()
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN init with no active session THEN activeSession remains null`() = runTest {
        assertNull(manager.activeSession.value)
    }

    @Test
    fun `WHEN init with active session THEN activeSession is restored`() = runTest {
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session

        val localManager = buildManager()

        assertEquals(session, localManager.activeSession.value)
    }

    @Test
    fun `WHEN startBreastfeeding LEFT THEN createBreastSession is called with babyId and LEFT`() = runTest {
        coEvery { repository.createBreastSession(any(), any(), any()) } returns Resource.Success("server-session-id")

        manager.startBreastfeeding(Breast.LEFT, babyId)

        coVerify { repository.createBreastSession(babyId, any(), Breast.LEFT) }
    }

    @Test
    fun `WHEN startBreastfeeding RIGHT THEN createBreastSession is called with babyId and RIGHT`() = runTest {
        coEvery { repository.createBreastSession(any(), any(), any()) } returns Resource.Success("server-session-id")

        manager.startBreastfeeding(Breast.RIGHT, babyId)

        coVerify { repository.createBreastSession(babyId, any(), Breast.RIGHT) }
    }

    @Test
    fun `WHEN startBreastfeeding THEN activeSession updated with server sessionId and correct breast`() = runTest {
        coEvery { repository.createBreastSession(any(), any(), any()) } returns Resource.Success("server-session-id")

        manager.startBreastfeeding(Breast.LEFT, babyId)

        val session = manager.activeSession.value
        assertEquals("server-session-id", session?.sessionId)
        assertEquals(Breast.LEFT, session?.activeBreast)
        assertEquals(FeedingType.BREAST, session?.type)
        assertEquals(1, session?.leftSegments?.size)
        assertEquals(0, session?.rightSegments?.size)
    }

    @Test
    fun `WHEN startBreastfeeding THEN startForegroundService called`() = runTest {
        coEvery { repository.createBreastSession(any(), any(), any()) } returns Resource.Success("server-session-id")

        manager.startBreastfeeding(Breast.LEFT, babyId)

        verify { context.startForegroundService(any()) }
    }

    @Test
    fun `WHEN switchBreast with no active session THEN nothing happens`() = runTest {
        manager.switchBreast(Breast.RIGHT)

        coVerify(inverse = true) { repository.getActiveSegmentId(any()) }
    }

    @Test
    fun `WHEN switchBreast with active open segment THEN old segment closed and new segment created`() = runTest {
        val segmentId = "seg-123"
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coEvery { repository.getActiveSegmentId(session.sessionId) } returns segmentId
        coJustRun { repository.closeSegment(any(), any()) }
        coJustRun { repository.createSegment(any(), any(), any(), any()) }
        coJustRun { repository.syncSwitchBreast(any(), any()) }
        val localManager = buildManager()

        localManager.switchBreast(Breast.RIGHT)

        coVerify { repository.closeSegment(segmentId, any()) }
        coVerify { repository.createSegment(any(), session.sessionId, Breast.RIGHT, any()) }
    }

    @Test
    fun `WHEN switchBreast with no active segment THEN only new segment created`() = runTest {
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coEvery { repository.getActiveSegmentId(session.sessionId) } returns null
        coJustRun { repository.createSegment(any(), any(), any(), any()) }
        coJustRun { repository.syncSwitchBreast(any(), any()) }
        val localManager = buildManager()

        localManager.switchBreast(Breast.RIGHT)

        coVerify(inverse = true) { repository.closeSegment(any(), any()) }
        coVerify { repository.createSegment(any(), session.sessionId, Breast.RIGHT, any()) }
    }

    @Test
    fun `WHEN switchBreast THEN syncSwitchBreast is called with correct sessionId and breast`() = runTest {
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coEvery { repository.getActiveSegmentId(session.sessionId) } returns null
        coJustRun { repository.createSegment(any(), any(), any(), any()) }
        coJustRun { repository.syncSwitchBreast(any(), any()) }
        val localManager = buildManager()

        localManager.switchBreast(Breast.RIGHT)

        coVerify { repository.syncSwitchBreast(session.sessionId, Breast.RIGHT) }
    }

    @Test
    fun `WHEN pauseCurrentBreast with no active session THEN nothing happens`() = runTest {
        manager.pauseCurrentBreast()

        coVerify(inverse = true) { repository.getActiveSegmentId(any()) }
    }

    @Test
    fun `WHEN pauseCurrentBreast with no active segment THEN nothing happens`() = runTest {
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coEvery { repository.getActiveSegmentId(session.sessionId) } returns null
        val localManager = buildManager()

        localManager.pauseCurrentBreast()

        coVerify(inverse = true) { repository.closeSegment(any(), any()) }
    }

    @Test
    fun `WHEN pauseCurrentBreast THEN active segment closed and session refreshed`() = runTest {
        val segmentId = "seg-456"
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coEvery { repository.getActiveSegmentId(session.sessionId) } returns segmentId
        coJustRun { repository.closeSegment(any(), any()) }
        val localManager = buildManager()
        clearMocks(repository, answers = false)

        localManager.pauseCurrentBreast()

        coVerify { repository.closeSegment(segmentId, any()) }
        coVerify(exactly = 1) { repository.getActiveSession() }
    }

    @Test
    fun `WHEN resumeBreast with no active session THEN nothing happens`() = runTest {
        manager.resumeBreast(Breast.LEFT)

        coVerify(inverse = true) { repository.createSegment(any(), any(), any(), any()) }
    }

    @Test
    fun `WHEN resumeBreast THEN new segment created and session refreshed`() = runTest {
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coJustRun { repository.createSegment(any(), any(), any(), any()) }
        val localManager = buildManager()
        clearMocks(repository, answers = false)

        localManager.resumeBreast(Breast.RIGHT)

        coVerify { repository.createSegment(any(), session.sessionId, Breast.RIGHT, any()) }
        coVerify(exactly = 1) { repository.getActiveSession() }
    }

    @Test
    fun `WHEN finishSession with no active session THEN nothing happens`() = runTest {
        manager.finishSession()

        coVerify(inverse = true) { repository.closeSession(any(), any()) }
    }

    @Test
    fun `WHEN finishSession with active segment THEN segment closed before session closed`() = runTest {
        val segmentId = "seg-789"
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coEvery { repository.getActiveSegmentId(session.sessionId) } returns segmentId
        coJustRun { repository.closeSegment(any(), any()) }
        coJustRun { repository.closeSession(any(), any()) }
        val localManager = buildManager()

        localManager.finishSession()

        coVerifyOrder {
            repository.closeSegment(segmentId, any())
            repository.closeSession(session.sessionId, any())
        }
    }

    @Test
    fun `WHEN finishSession with no active segment THEN only closeSession called`() = runTest {
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coEvery { repository.getActiveSegmentId(session.sessionId) } returns null
        coJustRun { repository.closeSession(any(), any()) }
        val localManager = buildManager()

        localManager.finishSession()

        coVerify(inverse = true) { repository.closeSegment(any(), any()) }
        coVerify { repository.closeSession(session.sessionId, any()) }
    }

    @Test
    fun `WHEN finishSession THEN activeSession set to null`() = runTest {
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coEvery { repository.getActiveSegmentId(session.sessionId) } returns null
        coJustRun { repository.closeSession(any(), any()) }
        val localManager = buildManager()

        localManager.finishSession()

        assertNull(localManager.activeSession.value)
    }

    @Test
    fun `WHEN finishSession THEN stopService called`() = runTest {
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coEvery { repository.getActiveSegmentId(session.sessionId) } returns null
        coJustRun { repository.closeSession(any(), any()) }
        val localManager = buildManager()

        localManager.finishSession()

        verify { context.stopService(any()) }
    }

    @Test
    fun `WHEN startBottleFeeding THEN createBottleSession called with babyId and correct parameters`() = runTest {
        coEvery { repository.createBottleSession(any(), any(), any(), any()) } returns Resource.Success(Unit)

        manager.startBottleFeeding(BottleType.MOTHERS_MILK, 120, babyId)

        coVerify { repository.createBottleSession(babyId, any(), 120, BottleType.MOTHERS_MILK) }
    }

    @Test
    fun `WHEN createBreastSession returns Resource Error THEN startBreastfeeding throws`() = runTest {
        val error = RuntimeException("network failure")
        coEvery { repository.createBreastSession(any(), any(), any()) } returns Resource.Error(error)

        var thrown: Throwable? = null
        runCatching { manager.startBreastfeeding(Breast.LEFT, babyId) }
            .onFailure { thrown = it }

        assertEquals(error, thrown)
    }

    @Test
    fun `WHEN createBreastSession returns Resource Error THEN activeSession stays null`() = runTest {
        coEvery { repository.createBreastSession(any(), any(), any()) } returns Resource.Error(RuntimeException())

        runCatching { manager.startBreastfeeding(Breast.LEFT, babyId) }

        assertNull(manager.activeSession.value)
    }

    @Test
    fun `WHEN createBottleSession returns Resource Error THEN startBottleFeeding throws`() = runTest {
        val error = RuntimeException("network failure")
        coEvery { repository.createBottleSession(any(), any(), any(), any()) } returns Resource.Error(error)

        var thrown: Throwable? = null
        runCatching { manager.startBottleFeeding(BottleType.MOTHERS_MILK, 120, babyId) }
            .onFailure { thrown = it }

        assertEquals(error, thrown)
    }

    @Test
    fun `WHEN updateSessionStartTime with no active session THEN repository not called`() = runTest {
        manager.updateSessionStartTime(System.currentTimeMillis() - 60_000L)

        coVerify(inverse = true) { repository.updateSessionStartTime(any(), any()) }
    }

    @Test
    fun `WHEN updateSessionStartTime THEN repository called with sessionId and new time`() = runTest {
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coEvery { repository.updateSessionStartTime(any(), any()) } returns Resource.Success(Unit)
        val localManager = buildManager()
        clearMocks(repository, answers = false)

        val newTime = System.currentTimeMillis() - 120_000L
        localManager.updateSessionStartTime(newTime)

        coVerify { repository.updateSessionStartTime(session.sessionId, newTime) }
    }

    @Test
    fun `WHEN updateSessionStartTime THEN session refreshed from repository`() = runTest {
        val session = buildSession()
        val updatedSession = session.copy(startedAt = session.startedAt - 120_000L)
        coEvery { repository.getActiveSession() } returnsMany listOf(session, updatedSession)
        coEvery { repository.updateSessionStartTime(any(), any()) } returns Resource.Success(Unit)
        val localManager = buildManager()

        localManager.updateSessionStartTime(updatedSession.startedAt)

        assertEquals(updatedSession, localManager.activeSession.value)
    }

    @Test
    fun `WHEN updateSessionStartTime returns Error THEN throws`() = runTest {
        val error = RuntimeException("network failure")
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coEvery { repository.updateSessionStartTime(any(), any()) } returns Resource.Error(error)
        val localManager = buildManager()

        var thrown: Throwable? = null
        runCatching { localManager.updateSessionStartTime(System.currentTimeMillis() - 60_000L) }
            .onFailure { thrown = it }

        assertEquals(error, thrown)
    }

    @Test
    fun `WHEN updateBreastStartTime with no active session THEN repository not called`() = runTest {
        manager.updateBreastStartTime(Breast.LEFT, System.currentTimeMillis() - 60_000L)

        coVerify(inverse = true) { repository.updateBreastStartTime(any(), any(), any()) }
    }

    @Test
    fun `WHEN updateBreastStartTime THEN repository called with sessionId, breast, and new time`() = runTest {
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coEvery { repository.updateBreastStartTime(any(), any(), any()) } returns Resource.Success(Unit)
        val localManager = buildManager()
        clearMocks(repository, answers = false)

        val newTime = System.currentTimeMillis() - 120_000L
        localManager.updateBreastStartTime(Breast.LEFT, newTime)

        coVerify { repository.updateBreastStartTime(session.sessionId, Breast.LEFT, newTime) }
    }

    @Test
    fun `WHEN updateBreastStartTime THEN session refreshed from repository`() = runTest {
        val session = buildSession()
        val updatedSession = session.copy(startedAt = session.startedAt - 120_000L)
        coEvery { repository.getActiveSession() } returnsMany listOf(session, updatedSession)
        coEvery { repository.updateBreastStartTime(any(), any(), any()) } returns Resource.Success(Unit)
        val localManager = buildManager()

        localManager.updateBreastStartTime(Breast.LEFT, updatedSession.startedAt)

        assertEquals(updatedSession, localManager.activeSession.value)
    }

    @Test
    fun `WHEN updateBreastStartTime returns Error THEN throws`() = runTest {
        val error = RuntimeException("network failure")
        val session = buildSession()
        coEvery { repository.getActiveSession() } returns session
        coEvery { repository.updateBreastStartTime(any(), any(), any()) } returns Resource.Error(error)
        val localManager = buildManager()

        var thrown: Throwable? = null
        runCatching { localManager.updateBreastStartTime(Breast.LEFT, System.currentTimeMillis() - 60_000L) }
            .onFailure { thrown = it }

        assertEquals(error, thrown)
    }

    @Test
    fun `WHEN no active session THEN tickerFlow does not emit`() = runTest {
        manager.tickerFlow.test {
            advanceTimeBy(3_100L)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN active session with running breast THEN tickerFlow emits every second`() = runTest {
        val session = buildSession(activeBreast = Breast.LEFT)
        coEvery { repository.getActiveSession() } returns session
        val localManager = buildManager()

        localManager.tickerFlow.test {
            advanceTimeBy(3_100L)
            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN active session is paused THEN tickerFlow does not emit`() = runTest {
        val session = buildSession(activeBreast = null)
        coEvery { repository.getActiveSession() } returns session
        val localManager = buildManager()

        localManager.tickerFlow.test {
            advanceTimeBy(3_100L)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun buildManager() = FeedingSessionManager(
        repository = repository,
        context = context,
        dispatchersProvider = dispatchersProvider,
    )

    private fun buildSession(activeBreast: Breast? = Breast.LEFT) = ActiveFeedingSession(
        sessionId = "test-session",
        type = FeedingType.BREAST,
        startedAt = System.currentTimeMillis(),
        activeBreast = activeBreast,
        leftSegments = emptyList(),
        rightSegments = emptyList(),
    )
}