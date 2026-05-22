package com.diegocunha.thenaapp.feature.home.presentation

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.home.domain.BabyAgeResult
import com.diegocunha.thenaapp.feature.home.domain.CalculateBabyAgeUseCase
import com.diegocunha.thenaapp.feature.home.domain.HomeRepository
import com.diegocunha.thenaapp.feature.home.domain.dto.ActiveFeedingInfo
import com.diegocunha.thenaapp.feature.home.domain.dto.ActiveSleepSessionInfo
import com.diegocunha.thenaapp.feature.home.domain.dto.HomeBabyInformation
import com.diegocunha.thenaapp.feature.home.domain.dto.HomeUserInformation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
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
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val homeRepository: HomeRepository = mockk()
    private val calculateBabyAge: CalculateBabyAgeUseCase = mockk()
    private lateinit var viewModel: HomeViewModel

    private val mockBabyInfo = HomeBabyInformation(
        babyId = "test-baby-id",
        babyName = "Baby Luna",
        babyBirthDate = "2023-01-01",
        babyWeight = BigDecimal("4.00"),
        babyHeight = BigDecimal("50"),
        babyPhotoUrl = null,
    )
    private val mockHomeData = HomeUserInformation(
        userName = "Test User",
        babyInformation = mockBabyInfo,
        todaySleepMinutes = null,
        expectedSleepMinutes = null,
    )
    private val mockBabyAgeResult = BabyAgeResult(totalMonths = 28, years = 2, remainderMonths = 4)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { homeRepository.getHomeData() } returns Resource.Success(mockHomeData)
        every { homeRepository.observeActiveFeeding() } returns emptyFlow()
        coEvery { homeRepository.closeSleepSession(any(), any(), any()) } returns Resource.Success(Unit)
        every { calculateBabyAge(any()) } returns mockBabyAgeResult
        viewModel = HomeViewModel(homeRepository, calculateBabyAge)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN ViewModel is created THEN initial state has isLoading = true`() {
        val deferred = CompletableDeferred<Resource<HomeUserInformation>>()
        coEvery { homeRepository.getHomeData() } coAnswers { deferred.await() }
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        assertTrue(vm.state.value.isLoading)

        deferred.complete(Resource.Success(mockHomeData))
    }

    @Test
    fun `WHEN loadContent succeeds THEN isLoading is false`() {
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `WHEN loadContent succeeds THEN userName is populated`() {
        assertEquals("Test User", viewModel.state.value.userName)
    }

    @Test
    fun `WHEN loadContent succeeds THEN babyName is populated`() {
        assertEquals("Baby Luna", viewModel.state.value.babyName)
    }

    @Test
    fun `WHEN calculateBabyAge returns a result THEN babyAge is mapped to presentation model`() {
        val babyAge = viewModel.state.value.babyAge

        assertNotNull(babyAge)
        assertEquals(28, babyAge!!.totalMonths)
        assertEquals(2, babyAge.years)
        assertEquals(4, babyAge.remainderMonths)
    }

    @Test
    fun `WHEN calculateBabyAge returns null THEN babyAge is null`() {
        every { calculateBabyAge(any()) } returns null
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        assertNull(vm.state.value.babyAge)
    }

    @Test
    fun `WHEN loadContent fails THEN error is set and isLoading is false`() {
        coEvery { homeRepository.getHomeData() } returns Resource.Error(Exception("API error"))
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `WHEN getHomeData returns sleepMinutes THEN todaySleepMinutes is populated`() {
        coEvery { homeRepository.getHomeData() } returns Resource.Success(
            mockHomeData.copy(todaySleepMinutes = 120, expectedSleepMinutes = 240)
        )
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        assertEquals(120L, vm.state.value.todaySleepMinutes)
        assertEquals(240L, vm.state.value.expectedSleepMinutes)
    }

    @Test
    fun `WHEN getHomeData returns null sleepMinutes THEN todaySleepMinutes is null`() {
        coEvery { homeRepository.getHomeData() } returns Resource.Success(
            mockHomeData.copy(todaySleepMinutes = null, expectedSleepMinutes = null)
        )
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        assertNull(vm.state.value.todaySleepMinutes)
        assertNull(vm.state.value.expectedSleepMinutes)
    }

    @Test
    fun `WHEN unimplemented intents are sent THEN NotDevelopedYet effect is emitted`() = runTest {
        val intents = listOf(
            HomeIntent.EditBabyInfo,
            HomeIntent.UserProfile,
            HomeIntent.VaccineInfo,
            HomeIntent.SummaryInfo,
        )

        viewModel.effects.test {
            intents.forEach { intent ->
                viewModel.sendIntent(intent)
                assertEquals(HomeEffect.NotDevelopedYet, awaitItem())
            }
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `WHEN FeedInfo intent is sent THEN NavigateToFeeding effect is emitted with babyId`() = runTest {
        viewModel.effects.test {
            viewModel.sendIntent(HomeIntent.FeedInfo)
            assertEquals(HomeEffect.NavigateToFeeding("test-baby-id"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `WHEN loadContent returns activeSleepSession THEN activeSleepSession state is populated`() {
        val sessionInfo = ActiveSleepSessionInfo(
            id = "session-id",
            startTimeMs = System.currentTimeMillis() - 60_000L,
            sleepType = "NAP",
        )
        coEvery { homeRepository.getHomeData() } returns Resource.Success(
            mockHomeData.copy(activeSleepSession = sessionInfo)
        )
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        assertEquals("session-id", vm.state.value.activeSleepSession?.id)
        assertEquals("NAP", vm.state.value.activeSleepSession?.sleepType)
        assertNotNull(vm.state.value.sleepBannerElapsedSeconds)
    }

    @Test
    fun `WHEN loadContent returns null activeSleepSession THEN activeSleepSession is null`() {
        coEvery { homeRepository.getHomeData() } returns Resource.Success(
            mockHomeData.copy(activeSleepSession = null)
        )
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        assertNull(vm.state.value.activeSleepSession)
        assertNull(vm.state.value.sleepBannerElapsedSeconds)
    }

    @Test
    fun `WHEN ActiveSleepBannerTapped THEN showCloseSessionPicker is true`() {
        viewModel.sendIntent(HomeIntent.ActiveSleepBannerTapped)

        assertTrue(viewModel.state.value.showCloseSessionPicker)
    }

    @Test
    fun `WHEN CloseSleepSession succeeds THEN activeSleepSession is cleared and SleepSessionClosed effect is emitted`() = runTest {
        val sessionInfo = ActiveSleepSessionInfo(
            id = "session-id",
            startTimeMs = System.currentTimeMillis() - 60_000L,
            sleepType = "NAP",
        )
        coEvery { homeRepository.getHomeData() } returns Resource.Success(
            mockHomeData.copy(activeSleepSession = sessionInfo)
        )
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        vm.effects.test {
            vm.sendIntent(HomeIntent.CloseSleepSession(System.currentTimeMillis()))
            assertEquals(HomeEffect.SleepSessionClosed, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertNull(vm.state.value.activeSleepSession)
        assertFalse(vm.state.value.showCloseSessionPicker)
        assertFalse(vm.state.value.isClosingSession)
        vm.viewModelScope.cancel()
    }

    @Test
    fun `WHEN CloseSleepSession fails THEN CloseSessionError effect is emitted`() = runTest {
        val sessionInfo = ActiveSleepSessionInfo(
            id = "session-id",
            startTimeMs = System.currentTimeMillis() - 60_000L,
            sleepType = "NAP",
        )
        coEvery { homeRepository.getHomeData() } returns Resource.Success(
            mockHomeData.copy(activeSleepSession = sessionInfo)
        )
        coEvery { homeRepository.closeSleepSession(any(), any(), any()) } returns Resource.Error(Exception("error"))
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        vm.effects.test {
            vm.sendIntent(HomeIntent.CloseSleepSession(System.currentTimeMillis()))
            assertEquals(HomeEffect.CloseSessionError, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(vm.state.value.isClosingSession)
        vm.viewModelScope.cancel()
    }

    @Test
    fun `WHEN loadContent succeeds THEN babyInfo height and weight are populated`() {
        val babyInfo = viewModel.state.value.babyInfo

        assertNotNull(babyInfo)
        assertTrue(babyInfo!!.height.isNotBlank())
        assertTrue(babyInfo.weight.isNotBlank())
    }

    @Test
    fun `WHEN observeActiveFeeding emits a session THEN activeFeedingSession and feedingBannerElapsedSeconds are populated`() {
        val feedingInfo = ActiveFeedingInfo(
            activeBreast = "LEFT",
            closedSegmentsTotalMs = 0L,
            activeSegmentStartedAt = System.currentTimeMillis() - 30_000L,
        )
        every { homeRepository.observeActiveFeeding() } returns flowOf(feedingInfo)
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        assertEquals(feedingInfo, vm.state.value.activeFeedingSession)
        assertNotNull(vm.state.value.feedingBannerElapsedSeconds)
    }

    @Test
    fun `WHEN observeActiveFeeding emits null THEN activeFeedingSession is null and feedingBannerElapsedSeconds is null`() {
        every { homeRepository.observeActiveFeeding() } returns flowOf(null)
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        assertNull(vm.state.value.activeFeedingSession)
        assertNull(vm.state.value.feedingBannerElapsedSeconds)
    }
}
