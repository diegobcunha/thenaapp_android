package com.diegocunha.thenaapp.core.resource

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceTest {

    @Test
    fun `WHEN Result is success THEN toResource returns Success `() {
        val result = Result.success("data")
        val resource = result.toResource()
        assertTrue(resource is Resource.Success)
        assertEquals("data", (resource as Resource.Success).data)
    }

    @Test
    fun `WHEN Result is failure THEN toResource returns Error `() {
        val exception = RuntimeException("error")
        val result = Result.failure<String>(exception)
        val resource = result.toResource()
        assertTrue(resource is Resource.Error)
        assertSame(exception, (resource as Resource.Error).exception)
    }

    @Test
    fun `WHEN Resource Success THEN holds correct data`() {
        val resource: Resource<Int> = Resource.Success(42)
        assertEquals(42, (resource as Resource.Success).data)
    }

    @Test
    fun `WHEN Resource Error THEN holds correct exception`() {
        val exception = IllegalStateException("fail")
        val resource: Resource<Nothing> = Resource.Error(exception)
        assertSame(exception, (resource as Resource.Error).exception)
    }

    @Test
    fun `WHEN Resource Loading THEN is singleton`() {
        assertSame(Resource.Loading, Resource.Loading)
    }
}
