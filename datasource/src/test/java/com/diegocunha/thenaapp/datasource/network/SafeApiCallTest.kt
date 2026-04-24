package com.diegocunha.thenaapp.datasource.network

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeApiCallTest {

    private val testDispatchers = mockk<DispatchersProvider>() {
        every { io() } returns Dispatchers.Unconfined
        every { main() } returns Dispatchers.Unconfined
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `safeApiCall returns Success when call succeeds`() = runTest {
        val result = safeApiCall(testDispatchers) { "response" }
        assertTrue(result is Resource.Success)
        assertEquals("response", (result as Resource.Success).data)
    }

    @Test
    fun `safeApiCall returns Error when call throws`() = runTest {
        val exception = RuntimeException("network error")
        val result = safeApiCall(testDispatchers) { throw exception }
        assertTrue(result is Resource.Error)
        assertSame(exception, (result as Resource.Error).exception)
    }

    @Test
    fun `safeApiCall returns Success with null value`() = runTest {
        val result = safeApiCall(testDispatchers) { null }
        assertTrue(result is Resource.Success)
        assertNull((result as Resource.Success).data)
    }
}
