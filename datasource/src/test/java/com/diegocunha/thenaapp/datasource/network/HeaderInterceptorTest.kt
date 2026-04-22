package com.diegocunha.thenaapp.datasource.network

import com.diegocunha.thenaapp.datasource.network.interceptor.HeaderInterceptor
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Test

class HeaderInterceptorTest {

    private val interceptor = HeaderInterceptor()

    private val mockResponse = mockk<Response>()
    private val chain = mockk<Interceptor.Chain>()
    private val capturedRequest = slot<Request>()

    @Test
    fun `WHEN called THEN adds Accept header with application json`() {
        val originalRequest = Request.Builder().url("https://localhost:8080/login").build()
        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(capturedRequest)) } returns mockResponse

        interceptor.intercept(chain)

        assertEquals("application/json", capturedRequest.captured.header("Accept"))
    }

    @Test
    fun `WHEN called THEN adds Content-Type header with application json`() {
        val originalRequest = Request.Builder().url("https://localhost:8080/login").build()
        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(capturedRequest)) } returns mockResponse

        interceptor.intercept(chain)

        assertEquals("application/json", capturedRequest.captured.header("Content-Type"))
    }

    @Test
    fun `WHEN called THEN proceeds with modified request`() {
        val originalRequest = Request.Builder().url("https://localhost:8080/login").build()
        every { chain.request() } returns originalRequest
        every { chain.proceed(any()) } returns mockResponse

        interceptor.intercept(chain)

        verify(exactly = 1) { chain.proceed(any()) }
    }

    @Test
    fun `WHEN called THEN returns response from chain`() {
        val originalRequest = Request.Builder().url("https://localhost:8080/login").build()
        every { chain.request() } returns originalRequest
        every { chain.proceed(any()) } returns mockResponse

        val result = interceptor.intercept(chain)

        assertEquals(mockResponse, result)
    }

    @Test
    fun `WHEN called THEN preserves original request url`() {
        val url = "https://localhost:8080/login"
        val originalRequest = Request.Builder().url(url).build()
        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(capturedRequest)) } returns mockResponse

        interceptor.intercept(chain)

        assertEquals(url, capturedRequest.captured.url.toString())
    }
}