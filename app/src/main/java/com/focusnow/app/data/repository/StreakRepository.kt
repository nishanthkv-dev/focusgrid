package com.focusnow.app.data.repository

import com.focusnow.app.data.local.StreakDao
import com.focusnow.app.data.model.DailyStreak
import com.focusnow.app.data.model.StreakType
import com.focusnow.app.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class StreakRepository(private val streakDao: StreakDao) {

    val allStreaks: Flow<List<DailyStreak>> = streakDao.getAllStreaks()

    fun getStreak(streakType: StreakType): Flow<DailyStreak?> =
        streakDao.getStreak(streakType)

    suspend fun recordStudySessionCompleted(studyMinutesToday: Int, targetMinutes: Int) {
        if (studyMinutesToday >= targetMinutes) {
            updateStreakForType(StreakType.STUDY)
            checkOverallProductivity()
        }
    }

    suspend fun recordTaskCompletedToday() {
        updateStreakForType(StreakType.TASKS)
        checkOverallProductivity()
    }

    suspend fun recordSleepTargetAchieved() {
        updateStreakForType(StreakType.SLEEP)
        checkOverallProductivity()
    }

    private suspend fun updateStreakForType(type: StreakType) {
        val today = LocalDate.now()
        val todayStr = DateTimeUtils.formatDateStr(today)
        val yesterdayStr = DateTimeUtils.formatDateStr(today.minusDays(1))

        val currentStreakObj = streakDao.getStreakOnce(type) ?: DailyStreak(streakType = type)

        if (currentStreakObj.lastActiveDateStr == todayStr) {
            // Already counted today
            return
        }

        val newCurrent = if (currentStreakObj.lastActiveDateStr == yesterdayStr) {
            currentStreakObj.currentStreak + 1
        } else {
            1
        }

        val newLongest = maxOf(currentStreakObj.longestStreak, newCurrent)

        val updated = currentStreakObj.copy(
            currentStreak = newCurrent,
            longestStreak = newLongest,
            lastActiveDateStr = todayStr,
            lastUpdatedMillis = System.currentTimeMillis()
        )
        streakDao.insertOrUpdateStreak(updated)
    }

    private suspend fun checkOverallProductivity() {
        val studyStreak = streakDao.getStreakOnce(StreakType.STUDY)
        val taskStreak = streakDao.getStreakOnce(StreakType.TASKS)

        val todayStr = DateTimeUtils.getTodayDateStr()
        if (studyStreak?.lastActiveDateStr == todayStr || taskStreak?.lastActiveDateStr == todayStr) {
            updateStreakForType(StreakType.OVERALL)
        }
    }

    suspend fun evaluateStreaksForNewDay() {
        val today = LocalDate.now()
        val yesterdayStr = DateTimeUtils.formatDateStr(today.minusDays(1))
        val todayStr = DateTimeUtils.formatDateStr(today)

        StreakType.entries.forEach { type ->
            val streak = streakDao.getStreakOnce(type)
            if (streak != null && streak.currentStreak > 0) {
                if (streak.lastActiveDateStr != todayStr && streak.lastActiveDateStr != yesterdayStr) {
                    // Streak was broken
                    streakDao.insertOrUpdateStreak(streak.copy(currentStreak = 0))
                }
            }
        }
    }
}
