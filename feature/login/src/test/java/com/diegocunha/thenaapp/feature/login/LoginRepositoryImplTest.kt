package com.diegocunha.thenaapp.feature.login

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.UserResponse
import com.diegocunha.thenaapp.datasource.network.service.UserService
import com.diegocunha.thenaapp.feature.login.domain.GoogleSignInException
import com.diegocunha.thenaapp.feature.login.domain.LoginCredentialsManager
import com.diegocunha.thenaapp.feature.login.repository.LoginRepositoryImpl
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class LoginRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val userService: UserService = mockk()
    private val firebaseAuth: FirebaseAuth = mockk()
    private val loginCredentialsManager: LoginCredentialsManager = mockk()
    private val dispatchersProvider: DispatchersProvider = mockk {
        every { io() } returns testDispatcher
    }

    private lateinit var repository: LoginRepositoryImpl

    private val mockUser = UserResponse(
        id = UUID.randomUUID(),
        name = "Test User",
        email = "test@example.com",
    )
    private val mockFirebaseUser: FirebaseUser = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = LoginRepositoryImpl(
            userService = userService,
            dispatchersProvider = dispatchersProvider,
            firebaseAuth = firebaseAuth,
            loginCredentialsManager = loginCredentialsManager,
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN performLogin succeeds THEN Resource Success with user data is returned`() = runTest {
        val authResult = successfulAuthResult(mockFirebaseUser)
        every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns authResult
        coEvery { userService.getUsersInformation() } returns mockUser

        val result = repository.performLogin("test@example.com", "Password@1")

        assertTrue(result is Resource.Success)
        assertEquals(mockUser, (result as Resource.Success).data)
    }

    @Test
    fun `WHEN performLogin returns null user THEN Resource Error is returned`() = runTest {
        val authResult = successfulAuthResult(user = null)
        every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns authResult

        val result = repository.performLogin("test@example.com", "Password@1")

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN performLogin Firebase throws THEN Resource Error is returned`() = runTest {
        every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns failedAuthTask(Exception("Invalid credentials"))

        val result = repository.performLogin("test@example.com", "wrong-password")

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN performLogin API call fails THEN Resource Error is returned`() = runTest {
        val authResult = successfulAuthResult(mockFirebaseUser)
        every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns authResult
        coEvery { userService.getUsersInformation() } throws Exception("Network error")

        val result = repository.performLogin("test@example.com", "Password@1")

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN loginWithGoogle succeeds THEN Resource Success with user data is returned`() = runTest {
        coEvery { loginCredentialsManager.getGoogleIdToken() } returns "valid-id-token"
        val authResult = successfulAuthResult(mockFirebaseUser)
        every { firebaseAuth.signInWithCredential(any()) } returns authResult
        coEvery { userService.getUsersInformation() } returns mockUser

        val result = repository.loginWithGoogle()

        assertTrue(result is Resource.Success)
        assertEquals(mockUser, (result as Resource.Success).data)
    }

    @Test
    fun `WHEN loginWithGoogle getGoogleIdToken throws THEN Resource Error is returned`() = runTest {
        coEvery { loginCredentialsManager.getGoogleIdToken() } throws GoogleSignInException("User cancelled")

        val result = repository.loginWithGoogle()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN loginWithGoogle Firebase signIn fails THEN Resource Error is returned`() = runTest {
        coEvery { loginCredentialsManager.getGoogleIdToken() } returns "valid-id-token"
        every { firebaseAuth.signInWithCredential(any()) } returns failedAuthTask(Exception("Firebase error"))

        val result = repository.loginWithGoogle()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN loginWithGoogle returns null user THEN Resource Error is returned`() = runTest {
        coEvery { loginCredentialsManager.getGoogleIdToken() } returns "valid-id-token"
        val authResult = successfulAuthResult(user = null)
        every { firebaseAuth.signInWithCredential(any()) } returns authResult

        val result = repository.loginWithGoogle()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN loginWithGoogle API call fails THEN Resource Error is returned`() = runTest {
        coEvery { loginCredentialsManager.getGoogleIdToken() } returns "valid-id-token"
        val authResult = successfulAuthResult(mockFirebaseUser)
        every { firebaseAuth.signInWithCredential(any()) } returns authResult
        coEvery { userService.getUsersInformation() } throws Exception("Network error")

        val result = repository.loginWithGoogle()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN sendPasswordResetEmail succeeds THEN Resource Success Unit is returned`() = runTest {
        every { firebaseAuth.sendPasswordResetEmail(any()) } returns voidSuccessTask()

        val result = repository.sendPasswordResetEmail("test@example.com")

        assertTrue(result is Resource.Success)
        assertEquals(Unit, (result as Resource.Success).data)
    }

    @Test
    fun `WHEN sendPasswordResetEmail Firebase throws THEN Resource Error is returned`() = runTest {
        every { firebaseAuth.sendPasswordResetEmail(any()) } returns voidFailedTask(Exception("Email not found"))

        val result = repository.sendPasswordResetEmail("test@example.com")

        assertTrue(result is Resource.Error)
    }

    private fun successfulAuthResult(user: FirebaseUser?): Task<AuthResult> = mockk {
        every { isComplete } returns true
        every { isCanceled } returns false
        every { exception } returns null
        every { result } returns mockk { every { this@mockk.user } returns user }
    }

    private fun failedAuthTask(exception: Exception): Task<AuthResult> = mockk {
        every { isComplete } returns true
        every { isCanceled } returns false
        every { this@mockk.exception } returns exception
    }

    private fun voidSuccessTask(): Task<Void> = mockk {
        every { isComplete } returns true
        every { isCanceled } returns false
        every { exception } returns null
        @Suppress("UNCHECKED_CAST")
        every { result } returns (null as Void?)
    }

    private fun voidFailedTask(exception: Exception): Task<Void> = mockk {
        every { isComplete } returns true
        every { isCanceled } returns false
        every { this@mockk.exception } returns exception
    }
}
