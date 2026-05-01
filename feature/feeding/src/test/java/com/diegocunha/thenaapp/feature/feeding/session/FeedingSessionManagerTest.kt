package com.diegocunha.thenaapp.feature.feeding.session

import android.content.Context
import android.content.Intent
import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
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
    fun `WHEN startBreastfeeding LEFT THEN createBreastSession and createSegment called with LEFT`() = runTest {
        coJustRun { repository.createBreastSession(any(), any(), any()) }
        coJustRun { repository.createSegment(any(), any(), any(), any()) }

        manager.startBreastfeeding(Breast.LEFT)

        coVerify { repository.createBreastSession(any(), "", any()) }
        coVerify { repository.createSegment(any(), any(), Breast.LEFT, any()) }
    }

    @Test
    fun `WHEN startBreastfeeding RIGHT THEN createBreastSession and createSegment called with RIGHT`() = runTest {
        coJustRun { repository.createBreastSession(any(), any(), any()) }
        coJustRun { repository.createSegment(any(), any(), any(), any()) }

        manager.startBreastfeeding(Breast.RIGHT)

        coVerify { repository.createSegment(any(), any(), Breast.RIGHT, any()) }
    }

    @Test
    fun `WHEN startBreastfeeding THEN activeSession updated with correct active breast and segments`() = runTest {
        coJustRun { repository.createBreastSession(any(), any(), any()) }
        coJustRun { repository.createSegment(any(), any(), any(), any()) }

        manager.startBreastfeeding(Breast.LEFT)

        val session = manager.activeSession.value
        assertEquals(Breast.LEFT, session?.activeBreast)
        assertEquals(FeedingType.BREAST, session?.type)
        assertEquals(1, session?.leftSegments?.size)
        assertEquals(0, session?.rightSegments?.size)
    }

    @Test
    fun `WHEN startBreastfeeding THEN startForegroundService called`() = runTest {
        coJustRun { repository.createBreastSession(any(), any(), any()) }
        coJustRun { repository.createSegment(any(), any(), any(), any()) }

        manager.startBreastfeeding(Breast.LEFT)

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
        val localManager = buildManager()

        localManager.switchBreast(Breast.RIGHT)

        coVerify(inverse = true) { repository.closeSegment(any(), any()) }
        coVerify { repository.createSegment(any(), session.sessionId, Breast.RIGHT, any()) }
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
    fun `WHEN startBottleFeeding THEN createBottleSession called with correct parameters`() = runTest {
        coJustRun { repository.createBottleSession(any(), any(), any(), any(), any()) }

        manager.startBottleFeeding(BottleType.MOTHERS_MILK, 120)

        coVerify { repository.createBottleSession(any(), "", any(), 120, BottleType.MOTHERS_MILK) }
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
