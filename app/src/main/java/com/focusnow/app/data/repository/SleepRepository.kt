package com.focusnow.app.data.repository

import com.focusnow.app.data.local.SleepDao
import com.focusnow.app.data.model.SleepRecord
import com.focusnow.app.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow

class SleepRepository(
    private val sleepDao: SleepDao,
    private val streakRepository: StreakRepository
) {
    val allSleepRecords: Flow<List<SleepRecord>> = sleepDao.getAllSleepRecords()

    fun getSleepRecordForDate(dateStr: String): Flow<SleepRecord?> =
        sleepDao.getSleepRecordForDate(dateStr)

    suspend fun logSleep(
        bedtimeStr: String,
        wakeTimeStr: String,
        dateStr: String = DateTimeUtils.getTodayDateStr(),
        qualityRating: Int = 4,
        notes: String = "",
        sleepTargetMinutes: Int = 480
    ): Long {
        val durationMinutes = DateTimeUtils.calculateSleepMinutes(bedtimeStr, wakeTimeStr)
        val now = System.currentTimeMillis()

        val record = SleepRecord(
            bedtimeMillis = now - (durationMinutes * 60 * 1000L),
            wakeTimeMillis = now,
            durationMinutes = durationMinutes,
            dateStr = dateStr,
            bedtimeStr = bedtimeStr,
            wakeTimeStr = wakeTimeStr,
            qualityRating = qualityRating,
            notes = notes
        )

        val id = sleepDao.insertSleepRecord(record)

        // Check if sleep target met (e.g. within target range)
        if (durationMinutes >= (sleepTargetMinutes - 60)) {
            streakRepository.recordSleepTargetAchieved()
        }

        return id
    }

    suspend fun deleteSleepRecord(record: SleepRecord) =
        sleepDao.deleteSleepRecord(record)
}
