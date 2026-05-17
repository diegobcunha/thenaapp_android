package com.diegocunha.thenaapp.feature.home.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CalculateBabyAgeUseCaseTest {

    private val useCase = CalculateBabyAgeUseCase()

    @Test
    fun `WHEN birth date is invalid THEN null is returned`() {
        assertNull(useCase("invalid-date"))
    }

    @Test
    fun `WHEN birth date uses wrong separator THEN null is returned`() {
        assertNull(useCase("2023/01/01"))
    }

    @Test
    fun `WHEN birth date is valid THEN result is not null`() {
        assertNotNull(useCase("2023-01-01"))
    }

    @Test
    fun `WHEN birth date is valid THEN totalMonths equals years times 12 plus remainderMonths`() {
        val result = useCase("2023-01-01")!!

        assertEquals(result.years * 12 + result.remainderMonths, result.totalMonths)
    }

    @Test
    fun `WHEN birth date is valid THEN remainderMonths is between 0 and 11`() {
        val result = useCase("2023-01-01")!!

        assertTrue(result.remainderMonths in 0..11)
    }

    @Test
    fun `WHEN birth month is after current month THEN month rollover keeps remainderMonths non-negative`() {
        val birthYear = Calendar.getInstance().get(Calendar.YEAR) - 2
        // December of 2 years ago always triggers month rollover for any month except December
        val result = useCase("$birthYear-12-01")!!

        assertTrue(result.remainderMonths >= 0)
        assertTrue(result.remainderMonths <= 11)
    }

    @Test
    fun `WHEN birth date is in the past THEN years is non-negative`() {
        val result = useCase("2020-06-15")!!

        assertTrue(result.years >= 0)
    }
}