package com.diegocunha.thenaapp.datasource.network

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CancellationException
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
    fun `WHEN call succeeds THEN returns Resource Success with the result`() = runTest {
        val result = safeApiCall(testDispatchers) { "response" }
        assertTrue(result is Resource.Success)
        assertEquals("response", (result as Resource.Success).data)
    }

    @Test
    fun `WHEN call throws a regular exception THEN returns Resource Error wrapping it`() = runTest {
        val exception = RuntimeException("network error")
        val result = safeApiCall(testDispatchers) { throw exception }
        assertTrue(result is Resource.Error)
        assertSame(exception, (result as Resource.Error).exception)
    }

    @Test
    fun `WHEN call returns null THEN returns Resource Success with null data`() = runTest {
        val result = safeApiCall(testDispatchers) { null }
        assertTrue(result is Resource.Success)
        assertNull((result as Resource.Success).data)
    }

    @Test
    fun `WHEN call throws CancellationException THEN it is rethrown and not wrapped as Resource Error`() = runTest {
        val cancellation = CancellationException("coroutine cancelled")
        var caughtException: Throwable? = null

        try {
            safeApiCall(testDispatchers) { throw cancellation }
        } catch (e: CancellationException) {
            caughtException = e
        }

        assertSame(cancellation, caughtException)
    }

    @Test
    fun `WHEN call throws CancellationException subclass THEN it is rethrown and not wrapped as Resource Error`() = runTest {
        val cancellation = object : CancellationException("job cancelled") {}
        var caughtException: Throwable? = null

        try {
            safeApiCall(testDispatchers) { throw cancellation }
        } catch (e: CancellationException) {
            caughtException = e
        }

        assertSame(cancellation, caughtException)
    }
}
