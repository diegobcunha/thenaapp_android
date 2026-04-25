package com.diegocunha.thenaapp.datasource.repository

import com.diegocunha.thenaapp.datasource.storage.sharedpreferences.CustomSharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserSessionRepositoryImplTest {

    private val preferences: CustomSharedPreferences = mockk(relaxed = true)
    private val repository = UserSessionRepositoryImpl(preferences)

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
}
