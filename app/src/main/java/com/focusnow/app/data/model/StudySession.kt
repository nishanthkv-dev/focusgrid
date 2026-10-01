package com.focusnow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SessionType(val displayName: String) {
    FOCUS("Focus Timer"),
    POMODORO("Pomodoro")
}

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val category: String = "General",
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMinutes: Int,
    val sessionType: SessionType = SessionType.FOCUS,
    val notes: String = "",
    val dateStr: String, // YYYY-MM-DD
    val targetDurationMinutes: Int = 25,
    val createdAt: Long = System.currentTimeMillis()
)
