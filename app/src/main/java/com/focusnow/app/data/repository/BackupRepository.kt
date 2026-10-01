package com.focusnow.app.data.repository

import com.focusnow.app.data.local.BlockedAppDao
import com.focusnow.app.data.local.BlockingProfileDao
import com.focusnow.app.data.local.CollegeScheduleDao
import com.focusnow.app.data.local.DailyRoutineDao
import com.focusnow.app.data.local.ExamDao
import com.focusnow.app.data.local.SleepDao
import com.focusnow.app.data.local.StreakDao
import com.focusnow.app.data.local.StudyGoalDao
import com.focusnow.app.data.local.StudySessionDao
import com.focusnow.app.data.local.TaskDao
import com.focusnow.app.data.local.UserProfileDao
import com.focusnow.app.data.model.BackupData
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class BackupRepository(
    private val userProfileDao: UserProfileDao,
    private val taskDao: TaskDao,
    private val studySessionDao: StudySessionDao,
    private val studyGoalDao: StudyGoalDao,
    private val collegeScheduleDao: CollegeScheduleDao,
    private val dailyRoutineDao: DailyRoutineDao,
    private val sleepDao: SleepDao,
    private val examDao: ExamDao,
    private val blockedAppDao: BlockedAppDao,
    private val profileDao: BlockingProfileDao,
    private val streakDao: StreakDao
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportDataToJson(): String {
        val backup = BackupData(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            userProfile = userProfileDao.getUserProfileOnce(),
            tasks = taskDao.getAllTasksOnce(),
            studySessions = studySessionDao.getAllSessionsOnce(),
            studyGoals = studyGoalDao.getAllGoalsOnce(),
            collegeClasses = collegeScheduleDao.getAllClassesOnce(),
            dailyRoutines = dailyRoutineDao.getAllRoutinesOnce(),
            sleepRecords = sleepDao.getAllSleepRecordsOnce(),
            examDeadlines = examDao.getAllExamsOnce(),
            blockedApps = blockedAppDao.getAllBlockedAppsOnce(),
            blockingProfiles = profileDao.getAllProfilesOnce(),
            streaks = streakDao.getAllStreaksOnce()
        )
        return gson.toJson(backup)
    }

    suspend fun restoreDataFromJson(jsonString: String): Boolean {
        return try {
            val backup = gson.fromJson(jsonString, BackupData::class.java) ?: return false

            backup.userProfile?.let { userProfileDao.insertOrUpdateProfile(it) }
            if (backup.tasks.isNotEmpty()) taskDao.insertAllTasks(backup.tasks)
            if (backup.studySessions.isNotEmpty()) studySessionDao.insertAllSessions(backup.studySessions)
            if (backup.studyGoals.isNotEmpty()) studyGoalDao.insertAllGoals(backup.studyGoals)
            if (backup.collegeClasses.isNotEmpty()) collegeScheduleDao.insertAllClasses(backup.collegeClasses)
            if (backup.dailyRoutines.isNotEmpty()) dailyRoutineDao.insertAllRoutines(backup.dailyRoutines)
            if (backup.sleepRecords.isNotEmpty()) sleepDao.insertAllSleepRecords(backup.sleepRecords)
            if (backup.examDeadlines.isNotEmpty()) examDao.insertAllExams(backup.examDeadlines)
            if (backup.blockedApps.isNotEmpty()) blockedAppDao.insertAllBlockedApps(backup.blockedApps)
            if (backup.blockingProfiles.isNotEmpty()) profileDao.insertAllProfiles(backup.blockingProfiles)
            if (backup.streaks.isNotEmpty()) streakDao.insertAllStreaks(backup.streaks)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun resetAllData() {
        taskDao.deleteAllTasks()
        studySessionDao.deleteAllSessions()
        studyGoalDao.deleteAllGoals()
        collegeScheduleDao.deleteAllClasses()
        dailyRoutineDao.deleteAllRoutines()
        sleepDao.deleteAllSleepRecords()
        examDao.deleteAllExams()
        blockedAppDao.deleteAllBlockedApps()
        profileDao.deleteAllProfiles()
        streakDao.deleteAllStreaks()
    }
}
