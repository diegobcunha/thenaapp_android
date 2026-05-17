package com.diegocunha.thenaapp.feature.home.presentation

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.home.domain.BabyAgeResult
import com.diegocunha.thenaapp.feature.home.domain.CalculateBabyAgeUseCase
import com.diegocunha.thenaapp.feature.home.domain.HomeRepository
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
    )
    private val mockBabyAgeResult = BabyAgeResult(totalMonths = 28, years = 2, remainderMonths = 4)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { homeRepository.getUserInformation() } returns Resource.Success(mockHomeData)
        every { homeRepository.observeActiveFeeding() } returns emptyFlow()
        coEvery { homeRepository.getTodaySleepMinutes(any()) } returns Resource.Error(Exception())
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
        coEvery { homeRepository.getUserInformation() } coAnswers { deferred.await() }
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
        coEvery { homeRepository.getUserInformation() } returns Resource.Error(Exception("API error"))
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `WHEN getTodaySleepMinutes succeeds THEN todaySleepMinutes is populated`() {
        coEvery { homeRepository.getTodaySleepMinutes(any()) } returns Resource.Success(120)
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        assertEquals(120, vm.state.value.todaySleepMinutes)
    }

    @Test
    fun `WHEN getTodaySleepMinutes fails THEN todaySleepMinutes remains null`() {
        coEvery { homeRepository.getTodaySleepMinutes(any()) } returns Resource.Error(Exception())
        val vm = HomeViewModel(homeRepository, calculateBabyAge)

        assertNull(vm.state.value.todaySleepMinutes)
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
    fun `WHEN loadContent succeeds THEN babyInfo height and weight are populated`() {
        val babyInfo = viewModel.state.value.babyInfo

        assertNotNull(babyInfo)
        assertTrue(babyInfo!!.height.isNotBlank())
        assertTrue(babyInfo.weight.isNotBlank())
    }
}