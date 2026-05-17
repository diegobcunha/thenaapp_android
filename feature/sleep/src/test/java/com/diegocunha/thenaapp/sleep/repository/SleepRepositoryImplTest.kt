package com.diegocunha.thenaapp.sleep.repository

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.sleep.NapScheduleItemResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.NextNapSuggestionResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepActiveSessionResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepDailyStatsResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepScheduleResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepSessionResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepTypeResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepWeeklyStatsResponse
import com.diegocunha.thenaapp.datasource.network.service.SleepApiService
import com.diegocunha.thenaapp.sleep.domain.model.SleepType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepRepositoryImplTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val sleepApiService: SleepApiService = mockk()
    private val dispatchersProvider: DispatchersProvider = mockk()
    private val babyId = "test-baby-id"

    private lateinit var repository: SleepRepositoryImpl

    @Before
    fun setUp() {
        every { dispatchersProvider.io() } returns dispatcher
        repository = SleepRepositoryImpl(sleepApiService, dispatchersProvider)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // region getActiveSession

    @Test
    fun `WHEN getActiveSession returns response THEN returns mapped domain model`() = runTest {
        coEvery { sleepApiService.getActiveSession(babyId) } returns buildActiveSessionResponse()

        val result = repository.getActiveSession(babyId)

        assertNotNull(result)
        assertEquals("active-session-id", result!!.id)
        assertEquals(SleepType.NAP, result.sleepType)
    }

    @Test
    fun `WHEN getActiveSession returns null THEN returns null`() = runTest {
        coEvery { sleepApiService.getActiveSession(babyId) } returns null

        assertNull(repository.getActiveSession(babyId))
    }

    @Test
    fun `WHEN getActiveSession throws THEN returns null`() = runTest {
        coEvery { sleepApiService.getActiveSession(babyId) } throws RuntimeException("network error")

        assertNull(repository.getActiveSession(babyId))
    }

    // endregion

    // region startSession

    @Test
    fun `WHEN startSession succeeds THEN returns Resource Success with domain model`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } returns buildSessionResponse()

        val result = repository.startSession(babyId, SleepType.NAP, System.currentTimeMillis())

        assertTrue(result is Resource.Success)
        assertEquals("session-id", (result as Resource.Success).data.id)
        assertEquals(SleepType.NAP, result.data.sleepType)
    }

    @Test
    fun `WHEN startSession throws THEN returns Resource Error`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } throws RuntimeException("network error")

        assertTrue(repository.startSession(babyId, SleepType.NAP, System.currentTimeMillis()) is Resource.Error)
    }

    // endregion

    // region endSession

    @Test
    fun `WHEN endSession succeeds THEN returns Resource Success with domain model`() = runTest {
        val sessionId = "session-1"
        coEvery { sleepApiService.endSession(babyId, sessionId, any()) } returns buildSessionResponse(isActive = false)

        val result = repository.endSession(babyId, sessionId, System.currentTimeMillis())

        assertTrue(result is Resource.Success)
        assertEquals("session-id", (result as Resource.Success).data.id)
    }

    @Test
    fun `WHEN endSession throws THEN returns Resource Error`() = runTest {
        coEvery { sleepApiService.endSession(babyId, any(), any()) } throws RuntimeException("network error")

        assertTrue(repository.endSession(babyId, "session-1", System.currentTimeMillis()) is Resource.Error)
    }

    // endregion

    // region logPastSession

    @Test
    fun `WHEN logPastSession succeeds THEN returns Resource Success`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } returns buildSessionResponse()

        val result = repository.logPastSession(
            babyId = babyId,
            sleepType = SleepType.NAP,
            startTimeMs = System.currentTimeMillis() - 3_600_000L,
            endTimeMs = System.currentTimeMillis(),
        )

        assertTrue(result is Resource.Success)
    }

    @Test
    fun `WHEN logPastSession THEN request includes endTime`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } returns buildSessionResponse()

        repository.logPastSession(
            babyId = babyId,
            sleepType = SleepType.NIGHT_SLEEP,
            startTimeMs = System.currentTimeMillis() - 3_600_000L,
            endTimeMs = System.currentTimeMillis(),
        )

        coVerify { sleepApiService.startSession(babyId, match { it.endTime != null }) }
    }

    @Test
    fun `WHEN logPastSession throws THEN returns Resource Error`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } throws RuntimeException("network error")

        assertTrue(
            repository.logPastSession(babyId, SleepType.NAP, System.currentTimeMillis() - 1000L, System.currentTimeMillis()) is Resource.Error
        )
    }

    // endregion

    // region listSessions

    @Test
    fun `WHEN listSessions returns list THEN returns Resource Success with mapped sessions`() = runTest {
        coEvery { sleepApiService.listSessions(babyId, any()) } returns listOf(
            buildSessionResponse(id = "s1"),
            buildSessionResponse(id = "s2"),
        )

        val result = repository.listSessions(babyId, "2026-05-16")

        assertTrue(result is Resource.Success)
        assertEquals(2, (result as Resource.Success).data.size)
        assertEquals("s1", result.data[0].id)
        assertEquals("s2", result.data[1].id)
    }

    @Test
    fun `WHEN listSessions returns empty THEN returns Resource Success with empty list`() = runTest {
        coEvery { sleepApiService.listSessions(babyId, any()) } returns emptyList()

        val result = repository.listSessions(babyId, "2026-05-16")

        assertTrue(result is Resource.Success)
        assertTrue((result as Resource.Success).data.isEmpty())
    }

    @Test
    fun `WHEN listSessions throws THEN returns Resource Error`() = runTest {
        coEvery { sleepApiService.listSessions(babyId, any()) } throws RuntimeException("error")

        assertTrue(repository.listSessions(babyId, "2026-05-16") is Resource.Error)
    }

    // endregion

    // region getDailyStats

    @Test
    fun `WHEN getDailyStats succeeds THEN returns Resource Success with all fields mapped`() = runTest {
        val response = buildDailyStatsResponse()
        coEvery { sleepApiService.getDailyStats(babyId, any()) } returns response

        val result = repository.getDailyStats(babyId, "2026-05-16")

        assertTrue(result is Resource.Success)
        val stats = (result as Resource.Success).data
        assertEquals(response.date, stats.date)
        assertEquals(response.totalSleepMinutes, stats.totalSleepMinutes)
        assertEquals(response.sessionCount, stats.sessionCount)
        assertEquals(response.napMinutes, stats.napMinutes)
        assertEquals(response.nightSleepMinutes, stats.nightSleepMinutes)
        assertEquals((response.efficiencyPercent ?: 0f) / 100f, stats.efficiency)
        assertEquals(0, stats.goalMinutes)
        assertEquals(response.insight, stats.insight)
    }

    @Test
    fun `WHEN getDailyStats throws THEN returns Resource Error`() = runTest {
        coEvery { sleepApiService.getDailyStats(babyId, any()) } throws RuntimeException("error")

        assertTrue(repository.getDailyStats(babyId, "2026-05-16") is Resource.Error)
    }

    // endregion

    // region getWeeklyStats

    @Test
    fun `WHEN getWeeklyStats succeeds THEN returns Resource Success with mapped days`() = runTest {
        val response = SleepWeeklyStatsResponse(
            days = listOf(buildDailyStatsResponse()),
            weeklyAvgMinutes = 480L,
            trend = "stable",
        )
        coEvery { sleepApiService.getWeeklyStats(babyId, any()) } returns response

        val result = repository.getWeeklyStats(babyId, "2026-05-12")

        assertTrue(result is Resource.Success)
        val stats = (result as Resource.Success).data
        assertEquals(1, stats.days.size)
        assertEquals(480L, stats.weeklyAvgMinutes)
        assertEquals("stable", stats.trend)
    }

    @Test
    fun `WHEN getWeeklyStats throws THEN returns Resource Error`() = runTest {
        coEvery { sleepApiService.getWeeklyStats(babyId, any()) } throws RuntimeException("error")

        assertTrue(repository.getWeeklyStats(babyId, "2026-05-12") is Resource.Error)
    }

    // endregion

    // region getNextNap

    @Test
    fun `WHEN getNextNap succeeds THEN returns Resource Success with all fields mapped`() = runTest {
        val response = NextNapSuggestionResponse(
            suggestedNapStart = "09:30",
            windowOpen = "09:00",
            windowClose = "10:00",
            urgency = "high",
            wakeWindowMinutes = 90,
        )
        coEvery { sleepApiService.getNextNap(babyId) } returns response

        val result = repository.getNextNap(babyId)

        assertTrue(result is Resource.Success)
        val suggestion = (result as Resource.Success).data
        assertEquals("09:30", suggestion.suggestedNapStart)
        assertEquals("09:00", suggestion.windowOpen)
        assertEquals("10:00", suggestion.windowClose)
        assertEquals("high", suggestion.urgency)
        assertEquals(90, suggestion.wakeWindowMinutes)
    }

    @Test
    fun `WHEN getNextNap throws THEN returns Resource Error`() = runTest {
        coEvery { sleepApiService.getNextNap(babyId) } throws RuntimeException("error")

        assertTrue(repository.getNextNap(babyId) is Resource.Error)
    }

    // endregion

    // region getSchedule

    @Test
    fun `WHEN getSchedule succeeds THEN returns Resource Success with mapped naps`() = runTest {
        val response = SleepScheduleResponse(
            date = "2026-05-16",
            naps = listOf(NapScheduleItemResponse("09:00", 45, SleepTypeResponse.NAP)),
            nightSleepMinutes = 540,
            totalSleepMinutes = 585,
        )
        coEvery { sleepApiService.getSchedule(babyId) } returns response

        val result = repository.getSchedule(babyId)

        assertTrue(result is Resource.Success)
        val schedule = (result as Resource.Success).data
        assertEquals("2026-05-16", schedule.date)
        assertEquals(1, schedule.naps.size)
        assertEquals("09:00", schedule.naps[0].startTime)
        assertEquals(45, schedule.naps[0].durationMinutes)
        assertEquals(SleepType.NAP, schedule.naps[0].sleepType)
        assertEquals(540, schedule.nightSleepMinutes)
        assertEquals(585, schedule.totalSleepMinutes)
    }

    @Test
    fun `WHEN getSchedule throws THEN returns Resource Error`() = runTest {
        coEvery { sleepApiService.getSchedule(babyId) } throws RuntimeException("error")

        assertTrue(repository.getSchedule(babyId) is Resource.Error)
    }

    // endregion

    // region SleepType mapping

    @Test
    fun `WHEN SleepTypeResponse is NIGHT_SLEEP THEN domain type is NIGHT_SLEEP`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } returns buildSessionResponse(sleepType = SleepTypeResponse.NIGHT_SLEEP)

        val result = repository.startSession(babyId, SleepType.NIGHT_SLEEP, System.currentTimeMillis())

        assertEquals(SleepType.NIGHT_SLEEP, (result as Resource.Success).data.sleepType)
    }

    @Test
    fun `WHEN SleepTypeResponse is EARLY_MORNING THEN domain type is EARLY_MORNING`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } returns buildSessionResponse(sleepType = SleepTypeResponse.EARLY_MORNING)

        val result = repository.startSession(babyId, SleepType.EARLY_MORNING, System.currentTimeMillis())

        assertEquals(SleepType.EARLY_MORNING, (result as Resource.Success).data.sleepType)
    }

    @Test
    fun `WHEN SleepTypeResponse is CATNAP THEN domain type is CATNAP`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } returns buildSessionResponse(sleepType = SleepTypeResponse.CATNAP)

        val result = repository.startSession(babyId, SleepType.CATNAP, System.currentTimeMillis())

        assertEquals(SleepType.CATNAP, (result as Resource.Success).data.sleepType)
    }

    @Test
    fun `WHEN SleepTypeResponse is CONTACT_NAP THEN domain type is CONTACT_NAP`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } returns buildSessionResponse(sleepType = SleepTypeResponse.CONTACT_NAP)

        val result = repository.startSession(babyId, SleepType.CONTACT_NAP, System.currentTimeMillis())

        assertEquals(SleepType.CONTACT_NAP, (result as Resource.Success).data.sleepType)
    }

    @Test
    fun `WHEN SleepTypeResponse is CAR_NAP THEN domain type is CAR_NAP`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } returns buildSessionResponse(sleepType = SleepTypeResponse.CAR_NAP)

        val result = repository.startSession(babyId, SleepType.CAR_NAP, System.currentTimeMillis())

        assertEquals(SleepType.CAR_NAP, (result as Resource.Success).data.sleepType)
    }

    // endregion

    // region ISO 8601 parsing

    @Test
    fun `WHEN startTime is valid ISO 8601 THEN startTimeMs is parsed to positive value`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } returns buildSessionResponse(startTime = "2026-05-16T09:30:00+00:00")

        val result = repository.startSession(babyId, SleepType.NAP, System.currentTimeMillis())

        assertTrue((result as Resource.Success).data.startTimeMs > 0L)
    }

    @Test
    fun `WHEN startTime is invalid THEN startTimeMs falls back to 0`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } returns buildSessionResponse(startTime = "not-a-date")

        val result = repository.startSession(babyId, SleepType.NAP, System.currentTimeMillis())

        assertEquals(0L, (result as Resource.Success).data.startTimeMs)
    }

    @Test
    fun `WHEN session has endTime THEN endTimeMs is parsed to non-null positive value`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } returns buildSessionResponse(endTime = "2026-05-16T10:30:00+00:00")

        val result = repository.startSession(babyId, SleepType.NAP, System.currentTimeMillis())

        val endTimeMs = (result as Resource.Success).data.endTimeMs
        assertNotNull(endTimeMs)
        assertTrue(endTimeMs!! > 0L)
    }

    @Test
    fun `WHEN session has no endTime THEN endTimeMs is null`() = runTest {
        coEvery { sleepApiService.startSession(babyId, any()) } returns buildSessionResponse(endTime = null)

        val result = repository.startSession(babyId, SleepType.NAP, System.currentTimeMillis())

        assertNull((result as Resource.Success).data.endTimeMs)
    }

    // endregion

    // region helpers

    private fun buildActiveSessionResponse(
        id: String = "active-session-id",
        startTime: String = "2026-05-16T08:00:00+00:00",
        elapsedMinutes: Long = 30L,
        sleepType: SleepTypeResponse = SleepTypeResponse.NAP,
    ) = SleepActiveSessionResponse(
        id = id,
        startTime = startTime,
        elapsedMinutes = elapsedMinutes,
        sleepType = sleepType,
    )

    private fun buildSessionResponse(
        id: String = "session-id",
        startTime: String = "2026-05-16T08:00:00+00:00",
        endTime: String? = null,
        durationMinutes: Long? = null,
        sleepType: SleepTypeResponse = SleepTypeResponse.NAP,
        isActive: Boolean = true,
    ) = SleepSessionResponse(
        id = id,
        startTime = startTime,
        endTime = endTime,
        durationMinutes = durationMinutes,
        sleepType = sleepType,
        isActive = isActive,
    )

    private fun buildDailyStatsResponse(
        date: String = "2026-05-16",
        totalSleepMinutes: Long = 600L,
        sessionCount: Int = 3,
        napMinutes: Long = 120L,
        nightSleepMinutes: Long = 480L,
        efficiencyPercent: Float? = 85f,
        insight: String? = "Good sleep today",
    ) = SleepDailyStatsResponse(
        date = date,
        totalSleepMinutes = totalSleepMinutes,
        sessionCount = sessionCount,
        napMinutes = napMinutes,
        nightSleepMinutes = nightSleepMinutes,
        efficiencyPercent = efficiencyPercent,
        insight = insight,
    )

    // endregion
}
