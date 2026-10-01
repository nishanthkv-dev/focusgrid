package com.focusnow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StreakType {
    STUDY,
    TASKS,
    SLEEP,
    OVERALL
}

@Entity(tableName = "daily_streaks")
data class DailyStreak(
    @PrimaryKey val streakType: StreakType,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDateStr: String = "", // YYYY-MM-DD
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)
