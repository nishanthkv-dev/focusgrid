package com.focusnow.app.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateTimeUtils {
    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    val DISPLAY_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault())
    val SHORT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    val TIME_FORMATTER_12H: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
    val TIME_FORMATTER_24H: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    fun getTodayDateStr(): String {
        return LocalDate.now().format(DATE_FORMATTER)
    }

    fun formatDateStr(date: LocalDate): String {
        return date.format(DATE_FORMATTER)
    }

    fun parseDateStr(dateStr: String): LocalDate {
        return try {
            LocalDate.parse(dateStr, DATE_FORMATTER)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr, DATE_FORMATTER)
            date.format(DISPLAY_DATE_FORMATTER)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatMillisToDate(millis: Long): String {
        val instant = Instant.ofEpochMilli(millis)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        return localDate.format(DISPLAY_DATE_FORMATTER)
    }

    fun formatMillisToDateStr(millis: Long): String {
        val instant = Instant.ofEpochMilli(millis)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        return localDate.format(DATE_FORMATTER)
    }

    fun formatMillisToTime(millis: Long): String {
        val instant = Instant.ofEpochMilli(millis)
        val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        return localTime.format(TIME_FORMATTER_12H)
    }

    fun formatTime24to12(time24: String): String {
        return try {
            val localTime = LocalTime.parse(time24, TIME_FORMATTER_24H)
            localTime.format(TIME_FORMATTER_12H)
        } catch (e: Exception) {
            time24
        }
    }

    fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
    }

    fun formatSecondsToTimer(totalSeconds: Int): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    fun getStartOfDayMillis(date: LocalDate = LocalDate.now()): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getEndOfDayMillis(date: LocalDate = LocalDate.now()): Long {
        return date.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getDaysDifference(targetDateMillis: Long, fromDateMillis: Long = System.currentTimeMillis()): Long {
        val targetDate = Instant.ofEpochMilli(targetDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val fromDate = Instant.ofEpochMilli(fromDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return ChronoUnit.DAYS.between(fromDate, targetDate)
    }

    fun calculateSleepMinutes(bedtimeStr: String, wakeTimeStr: String): Int {
        return try {
            val bed = LocalTime.parse(bedtimeStr, TIME_FORMATTER_24H)
            val wake = LocalTime.parse(wakeTimeStr, TIME_FORMATTER_24H)
            val bedMinutes = bed.hour * 60 + bed.minute
            val wakeMinutes = wake.hour * 60 + wake.minute

            if (wakeMinutes >= bedMinutes) {
                wakeMinutes - bedMinutes
            } else {
                // Crosses midnight
                (24 * 60 - bedMinutes) + wakeMinutes
            }
        } catch (e: Exception) {
            480 // 8 hours default
        }
    }

    fun timeStringToMinutes(time24: String): Int {
        return try {
            val parts = time24.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun minutesToTimeString(minutes: Int): String {
        val h = (minutes / 60) % 24
        val m = minutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }

    fun getPastDaysDateStrings(count: Int): List<String> {
        val today = LocalDate.now()
        return (0 until count).map { i ->
            today.minusDays(i.toLong()).format(DATE_FORMATTER)
        }.reversed()
    }

    fun getDayOfWeekForDate(dateStr: String): DayOfWeek {
        return try {
            LocalDate.parse(dateStr, DATE_FORMATTER).dayOfWeek
        } catch (e: Exception) {
            DayOfWeek.MONDAY
        }
    }
}
