package com.diegocunha.thenaapp.feature.signup

import app.cash.turbine.test
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.UserResponse
import com.diegocunha.thenaapp.feature.signup.domain.GoogleSignUpResponse
import com.diegocunha.thenaapp.feature.signup.domain.SignupRepository
import com.diegocunha.thenaapp.feature.signup.presentation.SignupEffect
import com.diegocunha.thenaapp.feature.signup.presentation.SignupIntent
import com.diegocunha.thenaapp.feature.signup.presentation.SignupViewModel
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SignupViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val signupRepository: SignupRepository = mockk()
    private lateinit var viewModel: SignupViewModel

    private val mockUser = UserResponse(
        id = UUID.randomUUID(),
        name = "Test User",
        email = "test@example.com",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SignupViewModel(signupRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    // region State update intents

    @Test
    fun `WHEN UpdateName is sent THEN name state is updated and errors cleared`() = runTest {
        viewModel.sendIntent(SignupIntent.UpdateName("Diego"))

        val state = viewModel.state.value
        assertEquals("Diego", state.name)
        assertNull(state.nameError)
        assertNull(state.generalError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
    }

    @Test
    fun `WHEN UpdateEmail is sent THEN email state is updated and errors cleared`() = runTest {
        viewModel.sendIntent(SignupIntent.UpdateEmail("test@example.com"))

        val state = viewModel.state.value
        assertEquals("test@example.com", state.email)
        assertNull(state.emailError)
        assertNull(state.generalError)
    }

    @Test
    fun `WHEN UpdatePassword is sent THEN password state is updated and errors cleared`() = runTest {
        viewModel.sendIntent(SignupIntent.UpdatePassword("Password@1"))

        val state = viewModel.state.value
        assertEquals("Password@1", state.password)
        assertNull(state.passwordError)
        assertNull(state.generalError)
    }

    @Test
    fun `WHEN UpdateConfirmPassword is sent THEN confirmPassword state is updated and errors cleared`() = runTest {
        viewModel.sendIntent(SignupIntent.UpdateConfirmPassword("Password@1"))

        val state = viewModel.state.value
        assertEquals("Password@1", state.confirmPassword)
        assertNull(state.confirmPasswordError)
        assertNull(state.generalError)
    }

    // endregion

    // region SubmitSignup validation

    @Test
    fun `WHEN SubmitSignup with empty name THEN nameError is set and no API call`() = runTest {
        viewModel.sendIntent(SignupIntent.UpdateEmail("test@example.com"))
        viewModel.sendIntent(SignupIntent.UpdatePassword("Password@1"))
        viewModel.sendIntent(SignupIntent.UpdateConfirmPassword("Password@1"))
        viewModel.sendIntent(SignupIntent.SubmitSignup)

        assertNotNull(viewModel.state.value.nameError)
        coVerify(exactly = 0) { signupRepository.createUser(any(), any(), any()) }
    }

    @Test
    fun `WHEN SubmitSignup with invalid email THEN emailError is set and no API call`() = runTest {
        viewModel.sendIntent(SignupIntent.UpdateName("Diego"))
        viewModel.sendIntent(SignupIntent.UpdateEmail("not-an-email"))
        viewModel.sendIntent(SignupIntent.UpdatePassword("Password@1"))
        viewModel.sendIntent(SignupIntent.UpdateConfirmPassword("Password@1"))
        viewModel.sendIntent(SignupIntent.SubmitSignup)

        assertNotNull(viewModel.state.value.emailError)
        coVerify(exactly = 0) { signupRepository.createUser(any(), any(), any()) }
    }

    @Test
    fun `WHEN SubmitSignup with weak password THEN passwordError is set and no API call`() = runTest {
        viewModel.sendIntent(SignupIntent.UpdateName("Diego"))
        viewModel.sendIntent(SignupIntent.UpdateEmail("test@example.com"))
        viewModel.sendIntent(SignupIntent.UpdatePassword("weak"))
        viewModel.sendIntent(SignupIntent.UpdateConfirmPassword("weak"))
        viewModel.sendIntent(SignupIntent.SubmitSignup)

        assertNotNull(viewModel.state.value.passwordError)
        coVerify(exactly = 0) { signupRepository.createUser(any(), any(), any()) }
    }

    @Test
    fun `WHEN SubmitSignup with mismatched passwords THEN confirmPasswordError is set and no API call`() = runTest {
        viewModel.sendIntent(SignupIntent.UpdateName("Diego"))
        viewModel.sendIntent(SignupIntent.UpdateEmail("test@example.com"))
        viewModel.sendIntent(SignupIntent.UpdatePassword("Password@1"))
        viewModel.sendIntent(SignupIntent.UpdateConfirmPassword("Password@2"))
        viewModel.sendIntent(SignupIntent.SubmitSignup)

        assertNotNull(viewModel.state.value.confirmPasswordError)
        coVerify(exactly = 0) { signupRepository.createUser(any(), any(), any()) }
    }

    // endregion

    // region SubmitSignup standard flow

    @Test
    fun `WHEN SubmitSignup with valid data and success THEN NavigateToOnboarding effect emitted`() = runTest {
        coEvery { signupRepository.createUser(any(), any(), any()) } returns Resource.Success(mockUser)

        viewModel.effects.test {
            viewModel.sendIntent(SignupIntent.UpdateName("Diego"))
            viewModel.sendIntent(SignupIntent.UpdateEmail("test@example.com"))
            viewModel.sendIntent(SignupIntent.UpdatePassword("Password@1"))
            viewModel.sendIntent(SignupIntent.UpdateConfirmPassword("Password@1"))
            viewModel.sendIntent(SignupIntent.SubmitSignup)

            assertEquals(SignupEffect.NavigateToOnboarding, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN SubmitSignup with valid data and error THEN generalError is set and isLoading is false`() = runTest {
        coEvery { signupRepository.createUser(any(), any(), any()) } returns Resource.Error(Exception("Registration failed"))

        viewModel.sendIntent(SignupIntent.UpdateName("Diego"))
        viewModel.sendIntent(SignupIntent.UpdateEmail("test@example.com"))
        viewModel.sendIntent(SignupIntent.UpdatePassword("Password@1"))
        viewModel.sendIntent(SignupIntent.UpdateConfirmPassword("Password@1"))
        viewModel.sendIntent(SignupIntent.SubmitSignup)

        val state = viewModel.state.value
        assertNotNull(state.generalError)
        assertEquals(false, state.isLoading)
    }

    // endregion

    // region Google Sign-Up flow

    @Test
    fun `WHEN TriggerGoogleSignIn starts THEN isLoading is set to true`() = runTest {
        val deferred = CompletableDeferred<Resource<GoogleSignUpResponse>>()
        coEvery { signupRepository.createUserWithGoogle() } coAnswers { deferred.await() }

        viewModel.state.test {
            awaitItem()
            viewModel.sendIntent(SignupIntent.TriggerGoogleSignIn)

            assertEquals(true, awaitItem().isLoading)

            deferred.complete(Resource.Success(GoogleSignUpResponse("test@example.com")))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN TriggerGoogleSignIn succeeds THEN isSignupByGoogle is true and email is prefilled`() = runTest {
        coEvery { signupRepository.createUserWithGoogle() } returns Resource.Success(GoogleSignUpResponse("test@example.com"))

        viewModel.sendIntent(SignupIntent.TriggerGoogleSignIn)

        val state = viewModel.state.value
        assertEquals(true, state.isSignupByGoogle)
        assertEquals("test@example.com", state.email)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `WHEN TriggerGoogleSignIn fails THEN generalError is set and isLoading is false`() = runTest {
        coEvery { signupRepository.createUserWithGoogle() } returns Resource.Error(Exception("Google error"))

        viewModel.sendIntent(SignupIntent.TriggerGoogleSignIn)

        val state = viewModel.state.value
        assertNotNull(state.generalError)
        assertEquals(false, state.isLoading)
    }

    // endregion

    // region SubmitSignup in Google mode

    @Test
    fun `WHEN SubmitSignup with isSignupByGoogle true and valid name THEN NavigateToOnboarding effect emitted`() = runTest {
        coEvery { signupRepository.createUserWithGoogle() } returns Resource.Success(GoogleSignUpResponse("test@example.com"))
        coEvery { signupRepository.updateUser(any()) } returns Resource.Success(mockUser)

        viewModel.effects.test {
            viewModel.sendIntent(SignupIntent.TriggerGoogleSignIn)
            viewModel.sendIntent(SignupIntent.UpdateName("Diego"))
            viewModel.sendIntent(SignupIntent.SubmitSignup)

            assertEquals(SignupEffect.NavigateToOnboarding, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN SubmitSignup with isSignupByGoogle true and empty name THEN nameError is set and updateUser not called`() = runTest {
        coEvery { signupRepository.createUserWithGoogle() } returns Resource.Success(GoogleSignUpResponse("test@example.com"))

        viewModel.sendIntent(SignupIntent.TriggerGoogleSignIn)
        viewModel.sendIntent(SignupIntent.SubmitSignup)

        assertNotNull(viewModel.state.value.nameError)
        coVerify(exactly = 0) { signupRepository.updateUser(any()) }
    }

    // endregion

    // region validateConfirmPassword

    @Test
    fun `WHEN validateConfirmPassword with matching valid passwords THEN returns null`() {
        val result = viewModel.validateConfirmPassword("Password@1", "Password@1")

        assertNull(result)
    }

    @Test
    fun `WHEN validateConfirmPassword with non-matching passwords THEN returns match error`() {
        val result = viewModel.validateConfirmPassword("Password@1", "Password@2")

        assertNotNull(result)
    }

    // endregion
}