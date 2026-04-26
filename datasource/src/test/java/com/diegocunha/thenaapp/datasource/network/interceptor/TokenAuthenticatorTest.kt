package com.diegocunha.thenaapp.datasource.network.interceptor

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TokenAuthenticatorTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    private val accessTokenRepository: AccessTokenRepository = mockk {
        every { getAccessToken() } returns "test-token"
    }
    private val authenticator = TokenAuthenticator(accessTokenRepository)

    @Test
    fun `WHEN access token is null THEN returns null`() {
        every { accessTokenRepository.getAccessToken() } returns null
        val response = mockk<Response> {
            every { priorResponse } returns null
        }

        assertNull(authenticator.authenticate(null, response))
    }

    @Test
    fun `WHEN response count is 1 and token exists THEN returns request with Authorization header`() {
        val originalRequest = Request.Builder().url("https://example.com/api").build()
        val response = mockk<Response> {
            every { priorResponse } returns null
            every { request } returns originalRequest
        }

        val result = authenticator.authenticate(null, response)

        assertNotNull(result)
        assertEquals("Bearer test-token", result!!.header("Authorization"))
    }

    @Test
    fun `WHEN response count is 2 THEN returns null to stop retry`() {
        val prior = mockk<Response> { every { priorResponse } returns null }
        val response = mockk<Response> { every { priorResponse } returns prior }

        assertNull(authenticator.authenticate(null, response))
    }

    @Test
    fun `WHEN response count is 3 THEN returns null to stop retry`() {
        val prior2 = mockk<Response> { every { priorResponse } returns null }
        val prior1 = mockk<Response> { every { priorResponse } returns prior2 }
        val response = mockk<Response> { every { priorResponse } returns prior1 }

        assertNull(authenticator.authenticate(null, response))
    }

    @Test
    fun `WHEN valid token THEN request preserves original URL`() {
        val url = "https://example.com/api"
        val originalRequest = Request.Builder().url(url).build()
        val response = mockk<Response> {
            every { priorResponse } returns null
            every { request } returns originalRequest
        }

        val result = authenticator.authenticate(null, response)

        assertEquals(url, result!!.url.toString())
    }
}
