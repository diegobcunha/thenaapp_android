package com.diegocunha.thenaapp.datasource.repository

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.datasource.storage.sharedpreferences.CustomSharedPreferences
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import io.mockk.every
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserSessionRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val preferences: CustomSharedPreferences = mockk(relaxed = true)
    private val mockFirebaseUser: FirebaseUser = mockk()
    private val firebaseAuth: FirebaseAuth = mockk()
    private val dispatchersProvider: DispatchersProvider = mockk {
        every { io() } returns testDispatcher
    }
    private val repository = UserSessionRepositoryImpl(preferences, firebaseAuth, dispatchersProvider)

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
    fun `WHEN saveUserId called THEN preferences putString is called with correct key and value`() = runTest {
        repository.saveUserId("user-123")

        verify { preferences.putString("user_session_id", "user-123") }
    }

    @Test
    fun `WHEN getUserId called and value exists THEN returns stored id`() = runTest {
        every { preferences.getString("user_session_id") } returns "user-456"

        val result = repository.getUserId()

        assertEquals("user-456", result)
    }

    @Test
    fun `WHEN getUserId called and no value stored THEN returns null`() = runTest {
        every { preferences.getString("user_session_id") } returns null

        val result = repository.getUserId()

        assertNull(result)
    }

    @Test
    fun `WHEN hasUser called and currentUser is not null THEN returns true`() = runTest {
        every { firebaseAuth.currentUser } returns mockFirebaseUser

        val result = repository.hasUser()

        assertTrue(result)
    }

    @Test
    fun `WHEN hasUser called and currentUser is null THEN returns false`() = runTest {
        every { firebaseAuth.currentUser } returns null

        val result = repository.hasUser()

        assertFalse(result)
    }

    @Test
    fun `WHEN refreshToken called and currentUser is present THEN completes without error`() = runTest {
        every { firebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.getIdToken(true) } returns successfulGetIdTokenTask()

        repository.refreshToken()
    }

    @Test
    fun `WHEN refreshToken called and currentUser is null THEN completes without error`() = runTest {
        every { firebaseAuth.currentUser } returns null

        repository.refreshToken()
    }

    private fun successfulGetIdTokenTask(): Task<GetTokenResult> = mockk {
        every { isComplete } returns true
        every { isCanceled } returns false
        every { exception } returns null
        every { result } returns mockk(relaxed = true)
    }
}