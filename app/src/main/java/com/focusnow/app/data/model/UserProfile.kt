package com.focusnow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val college: String = "",
    val department: String = "",
    val currentYear: String = "1st Year",
    val dailyStudyTargetMinutes: Int = 240, // Default 4 hours
    val sleepTargetMinutes: Int = 480,       // Default 8 hours
    val normalSleepTime: String = "23:00",
    val normalWakeTime: String = "07:00",
    val isOnboardingCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
