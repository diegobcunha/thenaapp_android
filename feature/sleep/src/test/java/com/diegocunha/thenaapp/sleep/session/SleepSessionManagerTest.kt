package com.diegocunha.thenaapp.sleep.session

import android.content.Context
import android.content.Intent
import app.cash.turbine.test
import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.sleep.domain.SleepRepository
import com.diegocunha.thenaapp.sleep.domain.model.ActiveSleepSession
import com.diegocunha.thenaapp.sleep.domain.model.SleepSession
import com.diegocunha.thenaapp.sleep.domain.model.SleepType
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepSessionManagerTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: SleepRepository = mockk()
    private val context: Context = mockk()
    private val dispatchersProvider: DispatchersProvider = mockk {
        every { io() } returns dispatcher
    }
    private val babyId = "test-baby-id"

    private lateinit var manager: SleepSessionManager

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkConstructor(Intent::class)
        coEvery { context.stopService(any()) } returns true
        coEvery { context.startForegroundService(any()) } returns mockk()
        coEvery { repository.getActiveSession(any()) } returns null
        manager = buildManager()
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN init THEN activeSession is null`() = runTest {
        Assert.assertNull(manager.activeSession.value)
    }

    @Test
    fun `WHEN restoreSession called AND no active session THEN activeSession remains null`() =
        runTest {
            coEvery { repository.getActiveSession(any()) } returns null

            manager.restoreSession(babyId)

            Assert.assertNull(manager.activeSession.value)
        }

    @Test
    fun `WHEN restoreSession called AND active session exists THEN activeSession is restored`() =
        runTest {
            val session = buildActiveSession()
            coEvery { repository.getActiveSession(babyId) } returns session

            manager.restoreSession(babyId)

            Assert.assertEquals(session, manager.activeSession.value)
        }

    @Test
    fun `WHEN restoreSession called AND active session exists THEN startForegroundService called`() =
        runTest {
            coEvery { repository.getActiveSession(babyId) } returns buildActiveSession()

            manager.restoreSession(babyId)

            verify { context.startForegroundService(any()) }
        }

    @Test
    fun `WHEN startSession succeeds THEN activeSession updated`() = runTest {
        val session = buildSession()
        coEvery { repository.startSession(any(), any(), any()) } returns Resource.Success(session)

        manager.startSession(babyId, SleepType.NAP)

        val active = manager.activeSession.value
        Assert.assertEquals(session.id, active?.id)
        Assert.assertEquals(session.sleepType, active?.sleepType)
    }

    @Test
    fun `WHEN startSession succeeds THEN startForegroundService called`() = runTest {
        coEvery { repository.startSession(any(), any(), any()) } returns Resource.Success(
            buildSession()
        )

        manager.startSession(babyId, SleepType.NAP)

        verify { context.startForegroundService(any()) }
    }

    @Test
    fun `WHEN startSession returns error THEN throws`() = runTest {
        val error = RuntimeException("network failure")
        coEvery { repository.startSession(any(), any(), any()) } returns Resource.Error(error)

        var thrown: Throwable? = null
        runCatching { manager.startSession(babyId, SleepType.NAP) }
            .onFailure { thrown = it }

        Assert.assertEquals(error, thrown)
    }

    @Test
    fun `WHEN startSession returns error THEN activeSession stays null`() = runTest {
        coEvery { repository.startSession(any(), any(), any()) } returns Resource.Error(
            RuntimeException()
        )

        runCatching { manager.startSession(babyId, SleepType.NAP) }

        Assert.assertNull(manager.activeSession.value)
    }

    @Test
    fun `WHEN stopSession with no active session THEN endSession not called`() = runTest {
        manager.stopSession(babyId)

        coVerify(inverse = true) { repository.endSession(any(), any(), any()) }
    }

    @Test
    fun `WHEN stopSession THEN repository endSession called`() = runTest {
        val session = buildSession()
        coEvery { repository.startSession(any(), any(), any()) } returns Resource.Success(session)
        coEvery { repository.endSession(any(), any(), any()) } returns Resource.Success(session)
        manager.startSession(babyId, SleepType.NAP)

        manager.stopSession(babyId)

        coVerify { repository.endSession(babyId, session.id, any()) }
    }

    @Test
    fun `WHEN stopSession THEN activeSession set to null`() = runTest {
        coEvery { repository.startSession(any(), any(), any()) } returns Resource.Success(
            buildSession()
        )
        coEvery {
            repository.endSession(
                any(),
                any(),
                any()
            )
        } returns Resource.Success(buildSession())
        manager.startSession(babyId, SleepType.NAP)

        manager.stopSession(babyId)

        Assert.assertNull(manager.activeSession.value)
    }

    @Test
    fun `WHEN stopSession THEN stopService called`() = runTest {
        coEvery { repository.startSession(any(), any(), any()) } returns Resource.Success(
            buildSession()
        )
        coEvery {
            repository.endSession(
                any(),
                any(),
                any()
            )
        } returns Resource.Success(buildSession())
        manager.startSession(babyId, SleepType.NAP)

        manager.stopSession(babyId)

        verify { context.stopService(any()) }
    }

    @Test
    fun `WHEN stopSession returns error THEN throws`() = runTest {
        val error = RuntimeException("network failure")
        coEvery { repository.startSession(any(), any(), any()) } returns Resource.Success(
            buildSession()
        )
        coEvery { repository.endSession(any(), any(), any()) } returns Resource.Error(error)
        manager.startSession(babyId, SleepType.NAP)

        var thrown: Throwable? = null
        runCatching { manager.stopSession(babyId) }
            .onFailure { thrown = it }

        Assert.assertEquals(error, thrown)
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
    fun `WHEN active session present THEN tickerFlow emits every second`() = runTest {
        coEvery { repository.startSession(any(), any(), any()) } returns Resource.Success(
            buildSession()
        )
        manager.startSession(babyId, SleepType.NAP)

        manager.tickerFlow.test {
            advanceTimeBy(3_100L)
            Assert.assertEquals(Unit, awaitItem())
            Assert.assertEquals(Unit, awaitItem())
            Assert.assertEquals(Unit, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun buildManager() = SleepSessionManager(
        repository = repository,
        context = context,
        dispatchersProvider = dispatchersProvider,
    )

    private fun buildSession() = SleepSession(
        id = "test-session",
        startTimeMs = System.currentTimeMillis() - 600_000L,
        endTimeMs = null,
        durationMinutes = null,
        sleepType = SleepType.NAP,
        isActive = true,
    )

    private fun buildActiveSession() = ActiveSleepSession(
        id = "test-session",
        startTimeMs = System.currentTimeMillis() - 600_000L,
        sleepType = SleepType.NAP,
    )
}
