package com.focusnow.app.data.model

data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val userProfile: UserProfile?,
    val tasks: List<TaskItem> = emptyList(),
    val studySessions: List<StudySession> = emptyList(),
    val studyGoals: List<StudyGoal> = emptyList(),
    val collegeClasses: List<CollegeClass> = emptyList(),
    val dailyRoutines: List<DailyScheduleRoutine> = emptyList(),
    val sleepRecords: List<SleepRecord> = emptyList(),
    val examDeadlines: List<ExamDeadline> = emptyList(),
    val blockedApps: List<BlockedApp> = emptyList(),
    val blockingProfiles: List<BlockingProfile> = emptyList(),
    val streaks: List<DailyStreak> = emptyList()
)
