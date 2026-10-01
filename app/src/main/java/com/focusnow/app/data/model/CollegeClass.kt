package com.focusnow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "college_classes")
data class CollegeClass(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val faculty: String = "",
    val room: String = "",
    val dayOfWeek: DayOfWeek,
    val startTimeStr: String, // "09:00"
    val endTimeStr: String,   // "10:00"
    val notes: String = "",
    val isRecurring: Boolean = true,
    val semesterEndDateMillis: Long = System.currentTimeMillis() + (120L * 24 * 60 * 60 * 1000), // ~4 months
    val colorHex: String = "#3B82F6",
    val createdAt: Long = System.currentTimeMillis()
)
