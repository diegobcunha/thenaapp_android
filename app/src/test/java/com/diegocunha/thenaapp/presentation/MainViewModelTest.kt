package com.diegocunha.thenaapp.presentation

import com.diegocunha.thenaapp.feature.baby.presentation.create.navigation.CreateBabyNavigation
import com.diegocunha.thenaapp.feature.login.presentation.navigation.LoginNavigation
import com.diegocunha.thenaapp.feature.onboarding.domain.repository.OnboardingRepository
import com.diegocunha.thenaapp.feature.onboarding.presentation.navigation.OnboardingNavigation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
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
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val onboardingRepository: OnboardingRepository = mockk()
    private val firebaseAuth: FirebaseAuth = mockk()
    private val mockFirebaseUser: FirebaseUser = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN onboarding not seen THEN startDestination is OnboardingNavigation`() = runTest {
        every { onboardingRepository.hasSeenOnboarding() } returns false
        every { firebaseAuth.currentUser } returns null

        val viewModel = MainViewModel(onboardingRepository, firebaseAuth)
        Assert.assertEquals(OnboardingNavigation, viewModel.startDestination.value)
    }

    @Test
    fun `WHEN onboarding seen and no Firebase user THEN startDestination is LoginNavigation`() =
        runTest {
            every { onboardingRepository.hasSeenOnboarding() } returns true
            every { firebaseAuth.currentUser } returns null

            val viewModel = MainViewModel(onboardingRepository, firebaseAuth)

            Assert.assertEquals(LoginNavigation, viewModel.startDestination.value)
        }

    @Test
    fun `WHEN onboarding seen and token refresh succeeds THEN startDestination is CreateBabyNavigation`() =
        runTest {
            every { onboardingRepository.hasSeenOnboarding() } returns true
            every { firebaseAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.getIdToken(true) } returns successfulGetTokenTask()

            val viewModel = MainViewModel(onboardingRepository, firebaseAuth)

            Assert.assertEquals(CreateBabyNavigation, viewModel.startDestination.value)
        }

    @Test
    fun `WHEN onboarding seen and token refresh fails THEN startDestination is LoginNavigation`() =
        runTest {
            every { onboardingRepository.hasSeenOnboarding() } returns true
            every { firebaseAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.getIdToken(true) } returns failedGetTokenTask(Exception("Token refresh failed"))

            val viewModel = MainViewModel(onboardingRepository, firebaseAuth)

            Assert.assertEquals(LoginNavigation, viewModel.startDestination.value)
        }

    private fun successfulGetTokenTask(): Task<GetTokenResult> = mockk {
        every { isComplete } returns true
        every { isCanceled } returns false
        every { exception } returns null
        every { result } returns mockk()
    }

    private fun failedGetTokenTask(exception: Exception): Task<GetTokenResult> = mockk {
        every { isComplete } returns true
        every { isCanceled } returns false
        every { this@mockk.exception } returns exception
    }
}
