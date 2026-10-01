package com.focusnow.app

import com.focusnow.app.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateTimeUtilsTest {

    @Test
    fun testFormatDuration_hoursAndMinutes() {
        val result = DateTimeUtils.formatDuration(155)
        assertEquals("2h 35m", result)
    }

    @Test
    fun testFormatDuration_exactHours() {
        val result = DateTimeUtils.formatDuration(120)
        assertEquals("2h", result)
    }

    @Test
    fun testFormatDuration_minutesOnly() {
        val result = DateTimeUtils.formatDuration(45)
        assertEquals("45m", result)
    }

    @Test
    fun testFormatSecondsToTimer() {
        val timer = DateTimeUtils.formatSecondsToTimer(1500)
        assertEquals("25:00", timer)

        val timerShort = DateTimeUtils.formatSecondsToTimer(65)
        assertEquals("01:05", timerShort)
    }

    @Test
    fun testCalculateSleepMinutes_normal() {
        val minutes = DateTimeUtils.calculateSleepMinutes("22:00", "06:00")
        assertEquals(480, minutes) // 8 hours
    }

    @Test
    fun testCalculateSleepMinutes_fractionalMidnight() {
        val minutes = DateTimeUtils.calculateSleepMinutes("22:45", "06:20")
        assertEquals(455, minutes) // 7h 35m
    }

    @Test
    fun testTimeStringToMinutesAndBack() {
        val minutes = DateTimeUtils.timeStringToMinutes("14:30")
        assertEquals(14 * 60 + 30, minutes)

        val timeStr = DateTimeUtils.minutesToTimeString(minutes)
        assertEquals("14:30", timeStr)
    }

    @Test
    fun testFormatTime24to12() {
        val time12 = DateTimeUtils.formatTime24to12("18:30")
        assertTrue(time12.contains("06:30") || time12.contains("6:30"))
        assertTrue(time12.contains("PM", ignoreCase = true))
    }
}
