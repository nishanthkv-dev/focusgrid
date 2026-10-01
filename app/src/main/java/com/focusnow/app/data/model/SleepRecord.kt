package com.focusnow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_records")
data class SleepRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bedtimeMillis: Long,
    val wakeTimeMillis: Long,
    val durationMinutes: Int,
    val dateStr: String, // YYYY-MM-DD (typically the wake date)
    val bedtimeStr: String = "23:00",
    val wakeTimeStr: String = "07:00",
    val qualityRating: Int = 4, // 1 to 5
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
