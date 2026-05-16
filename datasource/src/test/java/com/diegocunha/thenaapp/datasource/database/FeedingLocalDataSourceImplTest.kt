package com.diegocunha.thenaapp.datasource.database

import app.cash.turbine.test
import com.diegocunha.thenaapp.datasource.database.dao.BreastSegmentDao
import com.diegocunha.thenaapp.datasource.database.dao.FeedingSessionDao
import com.diegocunha.thenaapp.datasource.database.entity.BreastSegmentEntity
import com.diegocunha.thenaapp.datasource.database.entity.FeedingSessionEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class FeedingLocalDataSourceImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val sessionDao: FeedingSessionDao = mockk()
    private val segmentDao: BreastSegmentDao = mockk()

    private val dataSource = FeedingLocalDataSourceImpl(sessionDao, segmentDao)

    private val sessionFlow = MutableStateFlow<FeedingSessionEntity?>(null)
    private val segmentFlow = MutableStateFlow<List<BreastSegmentEntity>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { sessionDao.observeActiveSession() } returns sessionFlow
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN no active session THEN emits null`() = runTest {
        sessionFlow.value = null

        dataSource.observeActiveSession().test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN active session with no segments THEN snapshot has zero closed ms and no active breast`() = runTest {
        val session = buildSession()
        sessionFlow.value = session
        every { segmentDao.observeBySession(session.id) } returns segmentFlow
        segmentFlow.value = emptyList()

        dataSource.observeActiveSession().test {
            val snapshot = awaitItem()!!
            assertEquals(session.id, snapshot.sessionId)
            assertEquals(session.startedAt, snapshot.startedAt)
            assertNull(snapshot.activeBreast)
            assertEquals(0L, snapshot.closedSegmentsTotalMs)
            assertNull(snapshot.activeSegmentStartedAt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN active session with one active segment THEN snapshot reflects that segment`() = runTest {
        val session = buildSession()
        val activeSegment = buildSegment(sessionId = session.id, breast = "LEFT", startedAt = 1000L, endedAt = null)
        sessionFlow.value = session
        every { segmentDao.observeBySession(session.id) } returns segmentFlow
        segmentFlow.value = listOf(activeSegment)

        dataSource.observeActiveSession().test {
            val snapshot = awaitItem()!!
            assertEquals("LEFT", snapshot.activeBreast)
            assertEquals(1000L, snapshot.activeSegmentStartedAt)
            assertEquals(0L, snapshot.closedSegmentsTotalMs)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN active session with closed segments THEN closedSegmentsTotalMs is sum of their durations`() = runTest {
        val session = buildSession()
        val closed1 = buildSegment(sessionId = session.id, breast = "LEFT", startedAt = 0L, endedAt = 3000L)
        val closed2 = buildSegment(sessionId = session.id, breast = "RIGHT", startedAt = 4000L, endedAt = 9000L)
        val active = buildSegment(sessionId = session.id, breast = "LEFT", startedAt = 10000L, endedAt = null)
        sessionFlow.value = session
        every { segmentDao.observeBySession(session.id) } returns segmentFlow
        segmentFlow.value = listOf(closed1, closed2, active)

        dataSource.observeActiveSession().test {
            val snapshot = awaitItem()!!
            assertEquals(8000L, snapshot.closedSegmentsTotalMs) // (3000-0) + (9000-4000)
            assertEquals("LEFT", snapshot.activeBreast)
            assertEquals(10000L, snapshot.activeSegmentStartedAt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN getActiveSession called THEN returns first emission from observeActiveSession`() = runTest {
        sessionFlow.value = null

        val result = dataSource.getActiveSession()

        assertNull(result)
    }

    private fun buildSession(
        id: String = "session-1",
        babyId: String = "baby-1",
        type: String = "BREAST",
        startedAt: Long = 0L,
    ) = FeedingSessionEntity(id = id, babyId = babyId, type = type, startedAt = startedAt)

    private fun buildSegment(
        id: String = "seg-${System.nanoTime()}",
        sessionId: String = "session-1",
        breast: String = "LEFT",
        startedAt: Long = 0L,
        endedAt: Long? = null,
    ) = BreastSegmentEntity(id = id, sessionId = sessionId, breast = breast, startedAt = startedAt, endedAt = endedAt)
}
