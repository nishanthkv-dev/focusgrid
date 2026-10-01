package com.focusnow.app

import com.focusnow.app.data.model.SleepRecord
import com.focusnow.app.data.model.StudySession
import com.focusnow.app.util.CorrelationCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CorrelationCalculatorTest {

    @Test
    fun testCalculateCorrelation_withValidData() {
        val sessions = listOf(
            StudySession(subject = "DSA", startTimeMillis = 1000L, endTimeMillis = 1000L + (180 * 60 * 1000), durationMinutes = 180, dateStr = "2026-10-01"),
            StudySession(subject = "Math", startTimeMillis = 2000L, endTimeMillis = 2000L + (60 * 60 * 1000), durationMinutes = 60, dateStr = "2026-10-02"),
            StudySession(subject = "GATE", startTimeMillis = 3000L, endTimeMillis = 3000L + (240 * 60 * 1000), durationMinutes = 240, dateStr = "2026-10-03")
        )

        val sleepRecords = listOf(
            SleepRecord(bedtimeMillis = 0L, wakeTimeMillis = 0L, durationMinutes = 450, dateStr = "2026-10-01"), // 7.5 hours
            SleepRecord(bedtimeMillis = 0L, wakeTimeMillis = 0L, durationMinutes = 300, dateStr = "2026-10-02"), // 5 hours
            SleepRecord(bedtimeMillis = 0L, wakeTimeMillis = 0L, durationMinutes = 460, dateStr = "2026-10-03")  // 7.6 hours
        )

        val correlation = CorrelationCalculator.calculateCorrelation(sessions, sleepRecords)

        assertTrue(correlation.hasEnoughData)
        assertEquals("7–8 hours", correlation.bestSleepRangeStr)
        assertTrue(correlation.averageStudyMinutesInBestRange > 100)
        assertTrue(correlation.insightMessage.contains("highest"))
    }

    @Test
    fun testCalculateCorrelation_withEmptyData() {
        val correlation = CorrelationCalculator.calculateCorrelation(emptyList(), emptyList())
        assertEquals(false, correlation.hasEnoughData)
    }
}
