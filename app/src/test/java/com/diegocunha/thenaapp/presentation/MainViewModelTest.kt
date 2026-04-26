package com.diegocunha.thenaapp.presentation

import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.repository.UserSessionRepository
import com.diegocunha.thenaapp.datasource.repository.userprofile.ProfileStatus
import com.diegocunha.thenaapp.datasource.repository.userprofile.UserProfileRepository
import com.diegocunha.thenaapp.feature.baby.presentation.create.navigation.CreateBabyNavigation
import com.diegocunha.thenaapp.feature.home.presentation.navigation.HomeNavigation
import com.diegocunha.thenaapp.feature.login.presentation.navigation.LoginNavigation
import com.diegocunha.thenaapp.feature.onboarding.domain.repository.OnboardingRepository
import com.diegocunha.thenaapp.feature.onboarding.presentation.navigation.OnboardingNavigation
import com.diegocunha.thenaapp.feature.signup.presentation.navigation.SignupNavigation
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
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
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val onboardingRepository: OnboardingRepository = mockk()
    private val userSessionRepository: UserSessionRepository = mockk()
    private val userProfileRepository: UserProfileRepository = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = MainViewModel(onboardingRepository, userSessionRepository, userProfileRepository)

    @Test
    fun `WHEN onboarding not seen THEN startDestination is OnboardingNavigation`() = runTest {
        every { onboardingRepository.hasSeenOnboarding() } returns false

        assertEquals(OnboardingNavigation, buildViewModel().startDestination.value)
    }

    @Test
    fun `WHEN onboarding seen and no user THEN startDestination is LoginNavigation`() = runTest {
        every { onboardingRepository.hasSeenOnboarding() } returns true
        coEvery { userSessionRepository.hasUser() } returns false

        assertEquals(LoginNavigation, buildViewModel().startDestination.value)
    }

    @Test
    fun `WHEN profile is complete THEN startDestination is HomeNavigation`() = runTest {
        every { onboardingRepository.hasSeenOnboarding() } returns true
        coEvery { userSessionRepository.hasUser() } returns true
        coJustRun { userSessionRepository.refreshToken() }
        coEvery { userProfileRepository.getProfileStatus() } returns Resource.Success(ProfileStatus.Complete)

        assertEquals(HomeNavigation, buildViewModel().startDestination.value)
    }

    @Test
    fun `WHEN profile has missing name and no baby THEN startDestination is SignupNavigation with isProfileCompletion and hasBaby false`() = runTest {
        every { onboardingRepository.hasSeenOnboarding() } returns true
        coEvery { userSessionRepository.hasUser() } returns true
        coJustRun { userSessionRepository.refreshToken() }
        coEvery { userProfileRepository.getProfileStatus() } returns Resource.Success(ProfileStatus.MissingName(hasBaby = false))

        assertEquals(SignupNavigation(isProfileCompletion = true, hasBaby = false), buildViewModel().startDestination.value)
    }

    @Test
    fun `WHEN profile has missing name but has baby THEN startDestination is SignupNavigation with hasBaby true`() = runTest {
        every { onboardingRepository.hasSeenOnboarding() } returns true
        coEvery { userSessionRepository.hasUser() } returns true
        coJustRun { userSessionRepository.refreshToken() }
        coEvery { userProfileRepository.getProfileStatus() } returns Resource.Success(ProfileStatus.MissingName(hasBaby = true))

        assertEquals(SignupNavigation(isProfileCompletion = true, hasBaby = true), buildViewModel().startDestination.value)
    }

    @Test
    fun `WHEN profile is missing baby THEN startDestination is CreateBabyNavigation`() = runTest {
        every { onboardingRepository.hasSeenOnboarding() } returns true
        coEvery { userSessionRepository.hasUser() } returns true
        coJustRun { userSessionRepository.refreshToken() }
        coEvery { userProfileRepository.getProfileStatus() } returns Resource.Success(ProfileStatus.MissingBaby)

        assertEquals(CreateBabyNavigation, buildViewModel().startDestination.value)
    }

    @Test
    fun `WHEN profile status returns error THEN startDestination is LoginNavigation`() = runTest {
        every { onboardingRepository.hasSeenOnboarding() } returns true
        coEvery { userSessionRepository.hasUser() } returns true
        coJustRun { userSessionRepository.refreshToken() }
        coEvery { userProfileRepository.getProfileStatus() } returns Resource.Error(Exception("Network error"))

        assertEquals(LoginNavigation, buildViewModel().startDestination.value)
    }

    @Test
    fun `WHEN refreshToken throws THEN startDestination is LoginNavigation`() = runTest {
        every { onboardingRepository.hasSeenOnboarding() } returns true
        coEvery { userSessionRepository.hasUser() } returns true
        coEvery { userSessionRepository.refreshToken() } throws Exception("Token refresh failed")

        assertEquals(LoginNavigation, buildViewModel().startDestination.value)
    }
}