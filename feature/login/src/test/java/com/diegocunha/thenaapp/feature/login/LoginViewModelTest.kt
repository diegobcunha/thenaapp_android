package com.diegocunha.thenaapp.feature.login

import app.cash.turbine.test
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.UserResponse
import com.diegocunha.thenaapp.feature.login.domain.LoginRepository
import com.diegocunha.thenaapp.feature.login.presentation.LoginEffect
import com.diegocunha.thenaapp.feature.login.presentation.LoginIntent
import com.diegocunha.thenaapp.feature.login.presentation.LoginViewModel
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val loginRepository: LoginRepository = mockk()
    private lateinit var viewModel: LoginViewModel

    private val mockUser = UserResponse(
        id = UUID.randomUUID(),
        name = "Test User",
        email = "test@example.com",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(loginRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN UpdateEmail is sent THEN email state is updated and errors cleared`() = runTest {
        viewModel.sendIntent(LoginIntent.UpdateEmail("test@example.com"))

        val state = viewModel.state.value
        assertEquals("test@example.com", state.email)
        assertNull(state.emailError)
        assertNull(state.generalError)
    }

    @Test
    fun `WHEN UpdatePassword is sent THEN password state is updated and errors cleared`() = runTest {
        viewModel.sendIntent(LoginIntent.UpdatePassword("Password@1"))

        val state = viewModel.state.value
        assertEquals("Password@1", state.password)
        assertNull(state.passwordError)
    }

    @Test
    fun `WHEN SubmitLogin with invalid email THEN emailError is set and no API call`() = runTest {
        viewModel.sendIntent(LoginIntent.UpdateEmail("not-an-email"))
        viewModel.sendIntent(LoginIntent.UpdatePassword("Password@1"))
        viewModel.sendIntent(LoginIntent.SubmitLogin)

        assertNotNull(viewModel.state.value.emailError)
        coVerify(exactly = 0) { loginRepository.performLogin(any(), any()) }
    }

    @Test
    fun `WHEN SubmitLogin with weak password THEN passwordError is set and no API call`() = runTest {
        viewModel.sendIntent(LoginIntent.UpdateEmail("test@example.com"))
        viewModel.sendIntent(LoginIntent.UpdatePassword("weak"))
        viewModel.sendIntent(LoginIntent.SubmitLogin)

        assertNotNull(viewModel.state.value.passwordError)
        coVerify(exactly = 0) { loginRepository.performLogin(any(), any()) }
    }

    @Test
    fun `WHEN SubmitLogin with valid credentials and success THEN NavigateToHome effect emitted`() = runTest {
        coEvery { loginRepository.performLogin(any(), any()) } returns Resource.Success(mockUser)

        viewModel.effects.test {
            viewModel.sendIntent(LoginIntent.UpdateEmail("test@example.com"))
            viewModel.sendIntent(LoginIntent.UpdatePassword("Password@1"))
            viewModel.sendIntent(LoginIntent.SubmitLogin)
    
            assertEquals(LoginEffect.NavigateToHome, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN SubmitLogin fails THEN generalError is set and isLoading is false`() = runTest {
        coEvery { loginRepository.performLogin(any(), any()) } returns Resource.Error(Exception("Network error"))

        viewModel.sendIntent(LoginIntent.UpdateEmail("test@example.com"))
        viewModel.sendIntent(LoginIntent.UpdatePassword("Password@1"))
        viewModel.sendIntent(LoginIntent.SubmitLogin)

        val state = viewModel.state.value
        assertNotNull(state.generalError)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `WHEN ForgotPassword with blank email THEN emailError is set`() = runTest {
        viewModel.sendIntent(LoginIntent.ForgotPassword)

        assertNotNull(viewModel.state.value.emailError)
        coVerify(exactly = 0) { loginRepository.sendPasswordResetEmail(any()) }
    }

    @Test
    fun `WHEN ForgotPassword with valid email and success THEN ShowSnackbar effect emitted`() = runTest {
        coEvery { loginRepository.sendPasswordResetEmail(any()) } returns Resource.Success(Unit)

        viewModel.effects.test {
            viewModel.sendIntent(LoginIntent.UpdateEmail("test@example.com"))
            viewModel.sendIntent(LoginIntent.ForgotPassword)
    
            assertEquals(LoginEffect.ShowSnackbar(R.string.login_password_reset_sent), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN NavigateToSignUp intent THEN NavigateToSignUp effect emitted`() = runTest {
        viewModel.effects.test {
            viewModel.sendIntent(LoginIntent.NavigateToSignUp)
    
            assertEquals(LoginEffect.NavigateToSignUp, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN TriggerGoogleSignIn intent THEN LaunchGoogleSignIn effect emitted`() = runTest {
        viewModel.effects.test {
            viewModel.sendIntent(LoginIntent.TriggerGoogleSignIn)
    
            assertEquals(LoginEffect.LaunchGoogleSignIn, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN LoginWithGoogle with valid token and success THEN NavigateToHome effect emitted`() = runTest {
        coEvery { loginRepository.loginWithGoogle(any()) } returns Resource.Success(mockUser)

        viewModel.effects.test {
            viewModel.sendIntent(LoginIntent.LoginWithGoogle("valid-id-token"))
    
            assertEquals(LoginEffect.NavigateToHome, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN LoginWithGoogle fails THEN generalError is set`() = runTest {
        coEvery { loginRepository.loginWithGoogle(any()) } returns Resource.Error(Exception("Google error"))

        viewModel.sendIntent(LoginIntent.LoginWithGoogle("invalid-token"))

        assertNotNull(viewModel.state.value.generalError)
    }
}