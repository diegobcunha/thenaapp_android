package com.diegocunha.thenaapp.feature.home.repository

import app.cash.turbine.test
import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.database.model.ActiveFeedingSnapshot
import com.diegocunha.thenaapp.datasource.network.model.home.ActiveSleepSessionDto
import com.diegocunha.thenaapp.datasource.network.model.home.BabyHomeDto
import com.diegocunha.thenaapp.datasource.network.model.home.HomeResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepSessionResponse
import com.diegocunha.thenaapp.datasource.network.service.HomeService
import com.diegocunha.thenaapp.datasource.network.service.SleepApiService
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class HomeRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val homeService: HomeService = mockk()
    private val sleepApiService: SleepApiService = mockk()
    private val dispatchersProvider: DispatchersProvider = mockk {
        every { io() } returns testDispatcher
    }
    private val feedingLocalDataSource: com.diegocunha.thenaapp.datasource.database.FeedingLocalDataSource = mockk()
    private lateinit var repository: HomeRepositoryImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = HomeRepositoryImpl(homeService, sleepApiService, dispatchersProvider, feedingLocalDataSource)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN getHome succeeds with baby THEN Resource Success with HomeUserInformation is returned`() = runTest {
        coEvery { homeService.getHome(any()) } returns makeHomeResponse(userName = "Test User")

        val result = repository.getHomeData()

        assertTrue(result is Resource.Success)
        assertEquals("Test User", (result as Resource.Success).data.userName)
        assertEquals("Baby Luna", result.data.babyInformation.babyName)
    }

    @Test
    fun `WHEN getHome returns null baby THEN Resource Error is returned`() = runTest {
        coEvery { homeService.getHome(any()) } returns HomeResponse(
            userName = "Test User",
            baby = null,
            todaySleepMinutes = null,
            expectedSleepMinutes = null,
        )

        val result = repository.getHomeData()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN homeService throws THEN Resource Error is returned`() = runTest {
        coEvery { homeService.getHome(any()) } throws Exception("network error")

        val result = repository.getHomeData()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN getHome succeeds THEN babyHeight is scaled to 0 decimal places`() = runTest {
        coEvery { homeService.getHome(any()) } returns makeHomeResponse(
            baby = makeBabyHomeDto(birthHeight = BigDecimal("65.7"))
        )

        val result = repository.getHomeData()

        assertEquals(0, (result as Resource.Success).data.babyInformation.babyHeight.scale())
    }

    @Test
    fun `WHEN getHome succeeds THEN babyWeight is scaled to 2 decimal places`() = runTest {
        coEvery { homeService.getHome(any()) } returns makeHomeResponse(
            baby = makeBabyHomeDto(birthWeight = BigDecimal("4.123"))
        )

        val result = repository.getHomeData()

        assertEquals(2, (result as Resource.Success).data.babyInformation.babyWeight.scale())
    }

    @Test
    fun `WHEN getHome returns sleepMinutes THEN HomeUserInformation contains todaySleepMinutes`() = runTest {
        coEvery { homeService.getHome(any()) } returns makeHomeResponse(todaySleepMinutes = 180)

        val result = repository.getHomeData()

        assertEquals(180L, (result as Resource.Success).data.todaySleepMinutes)
    }

    @Test
    fun `WHEN getHome returns null sleepMinutes THEN todaySleepMinutes is null`() = runTest {
        coEvery { homeService.getHome(any()) } returns makeHomeResponse(todaySleepMinutes = null)

        val result = repository.getHomeData()

        assertNull((result as Resource.Success).data.todaySleepMinutes)
    }

    @Test
    fun `WHEN getHome returns activeSleepSession THEN HomeUserInformation contains activeSleepSession`() = runTest {
        val sessionDto = ActiveSleepSessionDto(
            id = UUID.fromString("6b7eefae-49e5-4eb9-b063-3cf182c418aa"),
            startTime = "2026-05-16T23:54:46Z",
            sleepType = "NAP",
        )
        coEvery { homeService.getHome(any()) } returns makeHomeResponse(activeSleepSession = sessionDto)

        val result = repository.getHomeData()

        val session = (result as Resource.Success).data.activeSleepSession
        assertEquals("6b7eefae-49e5-4eb9-b063-3cf182c418aa", session?.id)
        assertEquals("NAP", session?.sleepType)
    }

    @Test
    fun `WHEN getHome returns null activeSleepSession THEN activeSleepSession is null`() = runTest {
        coEvery { homeService.getHome(any()) } returns makeHomeResponse(activeSleepSession = null)

        val result = repository.getHomeData()

        assertNull((result as Resource.Success).data.activeSleepSession)
    }

    @Test
    fun `WHEN closeSleepSession succeeds THEN Resource Success is returned`() = runTest {
        coEvery { sleepApiService.endSession(any(), any(), any()) } returns mockk<SleepSessionResponse>()

        val result = repository.closeSleepSession("baby-id", "session-id", System.currentTimeMillis())

        assertTrue(result is Resource.Success)
    }

    @Test
    fun `WHEN sleepApiService endSession throws THEN Resource Error is returned`() = runTest {
        coEvery { sleepApiService.endSession(any(), any(), any()) } throws Exception("network error")

        val result = repository.closeSleepSession("baby-id", "session-id", System.currentTimeMillis())

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN photoUrl is null THEN babyPhotoUrl is null`() = runTest {
        coEvery { homeService.getHome(any()) } returns makeHomeResponse(
            baby = makeBabyHomeDto(photoUrl = null)
        )

        val result = repository.getHomeData()

        assertNull((result as Resource.Success).data.babyInformation.babyPhotoUrl)
    }

    @Test
    fun `WHEN observeActiveFeeding emits a snapshot THEN it is mapped to ActiveFeedingInfo`() = runTest {
        val snapshot = ActiveFeedingSnapshot(
            sessionId = "session-1",
            startedAt = System.currentTimeMillis() - 60_000L,
            activeBreast = "LEFT",
            type = "BREASTFEEDING",
            closedSegmentsTotalMs = 30_000L,
            activeSegmentStartedAt = System.currentTimeMillis() - 15_000L,
        )
        every { feedingLocalDataSource.observeActiveSession() } returns flowOf(snapshot)

        repository.observeActiveFeeding().test {
            val result = awaitItem()
            assertEquals("LEFT", result?.activeBreast)
            assertEquals(30_000L, result?.closedSegmentsTotalMs)
            assertEquals(snapshot.activeSegmentStartedAt, result?.activeSegmentStartedAt)
            awaitComplete()
        }
    }

    @Test
    fun `WHEN observeActiveFeeding emits null THEN ActiveFeedingInfo is null`() = runTest {
        every { feedingLocalDataSource.observeActiveSession() } returns flowOf(null)

        repository.observeActiveFeeding().test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    private fun makeBabyHomeDto(
        name: String = "Baby Luna",
        birthDate: String = "2023-01-01",
        photoUrl: String? = "https://example.com/photo.jpg",
        birthWeight: BigDecimal = BigDecimal("4.00"),
        birthHeight: BigDecimal = BigDecimal("50.00"),
    ) = BabyHomeDto(
        id = UUID.randomUUID(),
        name = name,
        birthDate = birthDate,
        sex = "MALE",
        photoUrl = photoUrl,
        birthWeight = birthWeight,
        birthHeight = birthHeight,
    )

    private fun makeHomeResponse(
        userName: String = "Test User",
        baby: BabyHomeDto? = makeBabyHomeDto(),
        todaySleepMinutes: Long? = 120L,
        expectedSleepMinutes: Long? = 240L,
        activeSleepSession: ActiveSleepSessionDto? = null,
    ) = HomeResponse(
        userName = userName,
        baby = baby,
        todaySleepMinutes = todaySleepMinutes,
        expectedSleepMinutes = expectedSleepMinutes,
        activeSleepSession = activeSleepSession,
    )
}
