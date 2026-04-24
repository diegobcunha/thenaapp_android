package com.diegocunha.thenaapp.feature.signup

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.user.UserResponse
import com.diegocunha.thenaapp.datasource.network.service.UserService
import com.diegocunha.thenaapp.datasource.repository.GoogleSignInException
import com.diegocunha.thenaapp.datasource.repository.LoginCredentialsManager
import com.diegocunha.thenaapp.feature.signup.domain.GoogleSignUpResponse
import com.diegocunha.thenaapp.feature.signup.repository.SignupRepositoryImpl
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
class SignupRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val userService: UserService = mockk()
    private val firebaseAuth: FirebaseAuth = mockk()
    private val loginCredentialsManager: LoginCredentialsManager = mockk()
    private val dispatchersProvider: DispatchersProvider = mockk {
        every { io() } returns testDispatcher
    }

    private lateinit var repository: SignupRepositoryImpl

    private val mockUser = UserResponse(
        id = UUID.randomUUID(),
        name = "Test User",
        email = "test@example.com",
    )
    private val mockFirebaseUser: FirebaseUser = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = SignupRepositoryImpl(
            userService = userService,
            firebaseAuth = firebaseAuth,
            dispatchersProvider = dispatchersProvider,
            loginCredentialsManager = loginCredentialsManager,
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    // region createUser

    @Test
    fun `WHEN createUser succeeds THEN Resource Success with UserResponse is returned`() = runTest {
        val authResult = successfulAuthResult(mockFirebaseUser)
        every { firebaseAuth.createUserWithEmailAndPassword(any(), any()) } returns authResult
        coEvery { userService.updateProfile(any()) } returns mockUser

        val result = repository.createUser("test@example.com", "Password@1", "Test User")

        assertTrue(result is Resource.Success)
        assertEquals(mockUser, (result as Resource.Success).data)
    }

    @Test
    fun `WHEN createUser Firebase returns null user THEN Resource Error is returned`() = runTest {
        val authResult = successfulAuthResult(user = null)
        every { firebaseAuth.createUserWithEmailAndPassword(any(), any()) } returns authResult

        val result = repository.createUser("test@example.com", "Password@1", "Test User")

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN createUser Firebase throws THEN Resource Error is returned`() = runTest {
        every { firebaseAuth.createUserWithEmailAndPassword(any(), any()) } returns failedAuthTask(Exception("Email already in use"))

        val result = repository.createUser("test@example.com", "Password@1", "Test User")

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN createUser updateProfile API call fails THEN Resource Error is returned`() = runTest {
        val authResult = successfulAuthResult(mockFirebaseUser)
        every { firebaseAuth.createUserWithEmailAndPassword(any(), any()) } returns authResult
        coEvery { userService.updateProfile(any()) } throws Exception("Network error")

        val result = repository.createUser("test@example.com", "Password@1", "Test User")

        assertTrue(result is Resource.Error)
    }

    // endregion

    // region createUserWithGoogle

    @Test
    fun `WHEN createUserWithGoogle succeeds THEN Resource Success with email is returned`() = runTest {
        coEvery { loginCredentialsManager.getGoogleIdToken() } returns "valid-id-token"
        val authResult = successfulAuthResult(mockFirebaseUser)
        every { firebaseAuth.signInWithCredential(any()) } returns authResult
        coEvery { userService.getUsersInformation() } returns mockUser

        val result = repository.createUserWithGoogle()

        assertTrue(result is Resource.Success)
        assertEquals(GoogleSignUpResponse(email = mockUser.email), (result as Resource.Success).data)
    }

    @Test
    fun `WHEN createUserWithGoogle getGoogleIdToken throws THEN Resource Error is returned`() = runTest {
        coEvery { loginCredentialsManager.getGoogleIdToken() } throws GoogleSignInException("User cancelled")

        val result = repository.createUserWithGoogle()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN createUserWithGoogle Firebase signIn fails THEN Resource Error is returned`() = runTest {
        coEvery { loginCredentialsManager.getGoogleIdToken() } returns "valid-id-token"
        every { firebaseAuth.signInWithCredential(any()) } returns failedAuthTask(Exception("Firebase error"))

        val result = repository.createUserWithGoogle()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN createUserWithGoogle getUsersInformation fails THEN Resource Error is returned`() = runTest {
        coEvery { loginCredentialsManager.getGoogleIdToken() } returns "valid-id-token"
        val authResult = successfulAuthResult(mockFirebaseUser)
        every { firebaseAuth.signInWithCredential(any()) } returns authResult
        coEvery { userService.getUsersInformation() } throws Exception("Network error")

        val result = repository.createUserWithGoogle()

        assertTrue(result is Resource.Error)
    }

    // endregion

    // region updateUser

    @Test
    fun `WHEN updateUser succeeds THEN Resource Success with UserResponse is returned`() = runTest {
        coEvery { userService.updateProfile(any()) } returns mockUser

        val result = repository.updateUser("New Name")

        assertTrue(result is Resource.Success)
        assertEquals(mockUser, (result as Resource.Success).data)
    }

    @Test
    fun `WHEN updateUser API call fails THEN Resource Error is returned`() = runTest {
        coEvery { userService.updateProfile(any()) } throws Exception("Network error")

        val result = repository.updateUser("New Name")

        assertTrue(result is Resource.Error)
    }

    // endregion

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
}
