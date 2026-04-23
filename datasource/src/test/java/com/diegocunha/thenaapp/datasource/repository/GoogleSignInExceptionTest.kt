package com.diegocunha.thenaapp.datasource.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class GoogleSignInExceptionTest {

    @Test
    fun `WHEN constructed with message THEN message is set`() {
        val exception = GoogleSignInException("Sign-in failed")

        assertEquals("Sign-in failed", exception.message)
    }

    @Test
    fun `WHEN constructed without cause THEN cause is null`() {
        val exception = GoogleSignInException("Sign-in failed")

        assertNull(exception.cause)
    }

    @Test
    fun `WHEN constructed with message and cause THEN both are set`() {
        val cause = RuntimeException("underlying error")
        val exception = GoogleSignInException("Sign-in failed", cause)

        assertEquals("Sign-in failed", exception.message)
        assertSame(cause, exception.cause)
    }
}