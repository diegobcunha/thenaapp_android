package com.diegocunha.thenaapp.datasource.serializer

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class UUIDSerializerTest {

    @Serializable
    private data class Wrapper(
        @Serializable(with = UUIDSerializer::class)
        val id: UUID
    )

    @Test
    fun `WHEN serializing a UUID THEN string representation is used`() {
        val uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        val json = Json.encodeToString(Wrapper(uuid))

        assertEquals("{\"id\":\"550e8400-e29b-41d4-a716-446655440000\"}", json)
    }

    @Test
    fun `WHEN deserializing a UUID string THEN correct UUID is returned`() {
        val result = Json.decodeFromString<Wrapper>("{\"id\":\"550e8400-e29b-41d4-a716-446655440000\"}")

        assertEquals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), result.id)
    }

    @Test
    fun `WHEN serializing and then deserializing THEN original UUID is recovered`() {
        val original = UUID.randomUUID()

        val json = Json.encodeToString(Wrapper(original))
        val result = Json.decodeFromString<Wrapper>(json)

        assertEquals(original, result.id)
    }
}
