package com.diegocunha.thenaapp.feature.home.presentation

import app.cash.turbine.test
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.home.domain.HomeRepository
import com.diegocunha.thenaapp.feature.home.domain.dto.HomeBabyInformation
import com.diegocunha.thenaapp.feature.home.domain.dto.HomeUserInformation
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var viewModel: HomeViewModel

    private val mockBabyInfo = HomeBabyInformation(
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { homeRepository.getUserInformation() } returns Resource.Success(mockHomeData)
        viewModel = HomeViewModel(homeRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN ViewModel is created THEN initial state has isLoading = true`() = runTest {
        val deferred = CompletableDeferred<Resource<HomeUserInformation>>()
        coEvery { homeRepository.getUserInformation() } coAnswers { deferred.await() }
        val vm = HomeViewModel(homeRepository)

        assertTrue(vm.state.value.isLoading)

        deferred.complete(Resource.Success(mockHomeData))
    }

    @Test
    fun `WHEN loadContent succeeds THEN isLoading is false`() = runTest {
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `WHEN loadContent succeeds THEN userName is populated`() = runTest {
        assertEquals("Test User", viewModel.state.value.userName)
    }

    @Test
    fun `WHEN loadContent succeeds THEN babyName is populated`() = runTest {
        assertEquals("Baby Luna", viewModel.state.value.babyName)
    }

    @Test
    fun `WHEN loadContent succeeds with valid birthDate THEN babyAge is not null`() = runTest {
        assertNotNull(viewModel.state.value.babyAge)
    }

    @Test
    fun `WHEN loadContent succeeds with invalid birthDate THEN babyAge is null`() = runTest {
        val dataWithInvalidDate = mockHomeData.copy(
            babyInformation = mockBabyInfo.copy(babyBirthDate = "invalid-date")
        )
        coEvery { homeRepository.getUserInformation() } returns Resource.Success(dataWithInvalidDate)
        val vm = HomeViewModel(homeRepository)

        assertNull(vm.state.value.babyAge)
    }

    @Test
    fun `WHEN loadContent fails THEN error is set and isLoading is false`() = runTest {
        coEvery { homeRepository.getUserInformation() } returns Resource.Error(Exception("API error"))
        val vm = HomeViewModel(homeRepository)

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `WHEN any intent is sent THEN NotDevelopedYet effect is emitted`() = runTest {
        val intents = listOf(
            HomeIntent.EditBabyInfo,
            HomeIntent.UserProfile,
            HomeIntent.SleepInfo,
            HomeIntent.FeedInfo,
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
    }

    @Test
    fun `WHEN loadContent succeeds THEN babyInfo height and weight are populated`() = runTest {
        val babyInfo = viewModel.state.value.babyInfo

        assertNotNull(babyInfo)
        assertTrue(babyInfo!!.height.isNotBlank())
        assertTrue(babyInfo.weight.isNotBlank())
    }
}
