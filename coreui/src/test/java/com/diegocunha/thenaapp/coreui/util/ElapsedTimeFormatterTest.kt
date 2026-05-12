package com.diegocunha.thenaapp.coreui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ElapsedTimeFormatterTest {

    @Test
    fun `WHEN totalSeconds is 0 THEN format is 00_00`() {
        assertEquals("00:00", formatElapsedSeconds(0))
    }

    @Test
    fun `WHEN totalSeconds is 59 THEN format is mm_ss`() {
        assertEquals("00:59", formatElapsedSeconds(59))
    }

    @Test
    fun `WHEN totalSeconds is 90 THEN format shows minutes and seconds`() {
        assertEquals("01:30", formatElapsedSeconds(90))
    }

    @Test
    fun `WHEN totalSeconds is 3599 THEN format is still mm_ss`() {
        assertEquals("59:59", formatElapsedSeconds(3599))
    }

    @Test
    fun `WHEN totalSeconds is exactly 3600 THEN format switches to HH_mm_ss`() {
        assertEquals("01:00:00", formatElapsedSeconds(3600))
    }

    @Test
    fun `WHEN totalSeconds is 3661 THEN format shows hours minutes and seconds`() {
        assertEquals("01:01:01", formatElapsedSeconds(3661))
    }

    @Test
    fun `WHEN totalSeconds is 7384 THEN format shows two hours`() {
        assertEquals("02:03:04", formatElapsedSeconds(7384))
    }

    @Test
    fun `WHEN totalSeconds exceeds 99 hours THEN hours are zero padded correctly`() {
        assertEquals("100:00:00", formatElapsedSeconds(360000))
    }
}