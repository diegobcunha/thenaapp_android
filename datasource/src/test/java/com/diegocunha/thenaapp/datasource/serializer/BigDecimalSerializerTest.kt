package com.diegocunha.thenaapp.datasource.serializer

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class BigDecimalSerializerTest {

    @Serializable
    private data class Wrapper(
        @Serializable(with = BigDecimalSerializer::class)
        val value: BigDecimal
    )

    @Test
    fun `WHEN serializing a simple decimal THEN toPlainString representation is used`() {
        val json = Json.encodeToString(Wrapper(BigDecimal("1.5")))

        assertEquals("{\"value\":\"1.5\"}", json)
    }

    @Test
    fun `WHEN serializing a large number THEN no scientific notation in output`() {
        val json = Json.encodeToString(Wrapper(BigDecimal("123456789.00")))

        assertFalse(json.contains("E+") || json.contains("E-"))
        assertTrue(json.contains("123456789"))
    }

    @Test
    fun `WHEN serializing zero THEN zero string is produced`() {
        val json = Json.encodeToString(Wrapper(BigDecimal.ZERO))

        assertEquals("{\"value\":\"0\"}", json)
    }

    @Test
    fun `WHEN serializing a negative value THEN sign is preserved`() {
        val json = Json.encodeToString(Wrapper(BigDecimal("-3.14")))

        assertTrue(json.contains("-3.14"))
    }

    @Test
    fun `WHEN deserializing a JSON string decimal THEN correct BigDecimal is returned`() {
        val result = Json.decodeFromString<Wrapper>("{\"value\":\"1.5\"}")

        assertEquals(BigDecimal("1.5"), result.value)
    }

    @Test
    fun `WHEN deserializing a JSON number THEN correct BigDecimal is returned`() {
        val result = Json.decodeFromString<Wrapper>("{\"value\":42}")

        assertEquals(0, BigDecimal("42").compareTo(result.value))
    }

    @Test
    fun `WHEN deserializing a value with many decimal places THEN precision is preserved`() {
        val result = Json.decodeFromString<Wrapper>("{\"value\":\"0.123456789\"}")

        assertEquals(0, BigDecimal("0.123456789").compareTo(result.value))
    }
}
