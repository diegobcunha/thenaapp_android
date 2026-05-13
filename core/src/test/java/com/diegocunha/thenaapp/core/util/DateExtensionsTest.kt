package com.diegocunha.thenaapp.core.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class DateExtensionsTest {

    private lateinit var previousLocale: Locale

    @Before
    fun setUp() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(previousLocale)
    }

    @Test
    fun `WHEN valid ISO date THEN returns formatted display date`() {
        // 2024-01-01 was a Monday
        assertEquals("Mon, Jan 1", "2024-01-01".toDisplayDate())
    }

    @Test
    fun `WHEN valid ISO date mid-year THEN formats correctly`() {
        // 2024-12-25 was a Wednesday
        assertEquals("Wed, Dec 25", "2024-12-25".toDisplayDate())
    }

    @Test
    fun `WHEN valid ISO date in February THEN formats correctly`() {
        // 2024-02-29 was a Thursday (2024 is a leap year)
        assertEquals("Thu, Feb 29", "2024-02-29".toDisplayDate())
    }

    @Test
    fun `WHEN invalid date string THEN returns original string unchanged`() {
        assertEquals("not-a-date", "not-a-date".toDisplayDate())
    }

    @Test
    fun `WHEN empty string THEN returns empty string`() {
        assertEquals("", "".toDisplayDate())
    }

    @Test
    fun `WHEN date in wrong format THEN returns original string`() {
        // dd-MM-yyyy is not the expected input format
        assertEquals("13-05-2026", "13-05-2026".toDisplayDate())
    }

    @Test
    fun `WHEN date with invalid day THEN returns original string`() {
        assertEquals("2026-02-30", "2026-02-30".toDisplayDate())
    }
}