package com.focusnow.app

import com.focusnow.app.data.model.BackupData
import com.focusnow.app.data.model.BlockedApp
import com.focusnow.app.data.model.BlockingProfile
import com.focusnow.app.data.model.CollegeClass
import com.focusnow.app.data.model.DailyScheduleRoutine
import com.focusnow.app.data.model.DailyStreak
import com.focusnow.app.data.model.ExamDeadline
import com.focusnow.app.data.model.RoutineCategory
import com.focusnow.app.data.model.SleepRecord
import com.focusnow.app.data.model.StreakType
import com.focusnow.app.data.model.StudyGoal
import com.focusnow.app.data.model.StudySession
import com.focusnow.app.data.model.TaskCategory
import com.focusnow.app.data.model.TaskItem
import com.focusnow.app.data.model.TaskPriority
import com.focusnow.app.data.model.UserProfile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.DayOfWeek

class BackupRestoreTest {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    @Test
    fun testSerializationAndDeserialization_preservesAllData() {
        val userProfile = UserProfile(
            name = "Test Student",
            college = "MIT",
            department = "CS",
            dailyStudyTargetMinutes = 300,
            sleepTargetMinutes = 480
        )

        val task = TaskItem(
            id = 1,
            title = "Study DBMS Indexing",
            category = TaskCategory.COLLEGE,
            priority = TaskPriority.HIGH,
            isCompleted = true
        )

        val session = StudySession(
            id = 1,
            subject = "Algorithms",
            startTimeMillis = 1000L,
            endTimeMillis = 5000L,
            durationMinutes = 60,
            dateStr = "2026-10-01"
        )

        val goal = StudyGoal(
            id = 1,
            title = "Master React",
            progressPercentage = 75
        )

        val cls = CollegeClass(
            id = 1,
            subject = "Computer Networks",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            startTimeStr = "11:15",
            endTimeStr = "12:15"
        )

        val streak = DailyStreak(
            streakType = StreakType.STUDY,
            currentStreak = 12,
            longestStreak = 18
        )

        val backup = BackupData(
            version = 1,
            exportedAt = 999999L,
            userProfile = userProfile,
            tasks = listOf(task),
            studySessions = listOf(session),
            studyGoals = listOf(goal),
            collegeClasses = listOf(cls),
            dailyRoutines = emptyList(),
            sleepRecords = emptyList(),
            examDeadlines = emptyList(),
            blockedApps = listOf(BlockedApp(packageName = "com.instagram.android", appName = "Instagram", isBlocked = true)),
            blockingProfiles = emptyList(),
            streaks = listOf(streak)
        )

        val json = gson.toJson(backup)
        assertNotNull(json)

        val restored = gson.fromJson(json, BackupData::class.java)
        assertEquals("Test Student", restored.userProfile?.name)
        assertEquals(1, restored.tasks.size)
        assertEquals("Study DBMS Indexing", restored.tasks.first().title)
        assertEquals(12, restored.streaks.first().currentStreak)
        assertEquals("com.instagram.android", restored.blockedApps.first().packageName)
    }
}
