package com.focusnow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RoutineCategory(val displayName: String, val defaultIcon: String) {
    WAKE_UP("Wake up", "☀️"),
    EXERCISE("Exercise", "🏃"),
    STUDY("Study", "📚"),
    COLLEGE("College", "🏛️"),
    REST("Break/Rest", "☕"),
    CODING("Coding", "💻"),
    DINNER("Meal", "🍽️"),
    GATE_PREP("GATE / Prep", "🎯"),
    REVISION("Revision", "📝"),
    SLEEP("Sleep", "🌙"),
    OTHER("Other", "⭐")
}

@Entity(tableName = "daily_routine_blocks")
data class DailyScheduleRoutine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startTimeStr: String, // "06:00"
    val endTimeStr: String,   // "06:30"
    val category: RoutineCategory = RoutineCategory.OTHER,
    val daysOfWeek: String = "ALL", // "ALL", "WEEKDAYS", "WEEKENDS", or "MON,TUE,WED"
    val isEnabled: Boolean = true,
    val notes: String = "",
    val orderIndex: Int = 0
)
