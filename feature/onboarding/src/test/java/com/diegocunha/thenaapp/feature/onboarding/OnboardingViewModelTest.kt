package com.diegocunha.thenaapp.feature.onboarding

import app.cash.turbine.test
import com.diegocunha.thenaapp.feature.onboarding.domain.repository.OnboardingRepository
import com.diegocunha.thenaapp.feature.onboarding.presentation.OnboardingEffect
import com.diegocunha.thenaapp.feature.onboarding.presentation.OnboardingIntent
import com.diegocunha.thenaapp.feature.onboarding.presentation.OnboardingViewModel
import io.mockk.justRun
import io.mockk.mockk
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val onboardingRepository: OnboardingRepository = mockk()
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        justRun { onboardingRepository.markOnboardingSeen() }
        viewModel = OnboardingViewModel(onboardingRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN PageChanged THEN currentSlide updates to the given page`() = runTest {
        viewModel.sendIntent(OnboardingIntent.PageChanged(3))

        assertEquals(3, viewModel.state.value.currentSlide)
    }

    @Test
    fun `WHEN PageChanged multiple times THEN currentSlide always reflects the latest page`() = runTest {
        viewModel.sendIntent(OnboardingIntent.PageChanged(1))
        viewModel.sendIntent(OnboardingIntent.PageChanged(2))
        viewModel.sendIntent(OnboardingIntent.PageChanged(1))

        assertEquals(1, viewModel.state.value.currentSlide)
    }

    @Test
    fun `WHEN Skip THEN markOnboardingSeen is called and NavigateToLogin is emitted`() = runTest {
        viewModel.effects.test {
            viewModel.sendIntent(OnboardingIntent.Skip)

            assertEquals(OnboardingEffect.NavigateToLogin, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) { onboardingRepository.markOnboardingSeen() }
    }

    @Test
    fun `WHEN Done THEN markOnboardingSeen is called and NavigateToLogin is emitted`() = runTest {
        viewModel.effects.test {
            viewModel.sendIntent(OnboardingIntent.Done)

            assertEquals(OnboardingEffect.NavigateToLogin, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) { onboardingRepository.markOnboardingSeen() }
    }
}