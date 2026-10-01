package com.focusnow.app

import android.app.Application
import com.focusnow.app.data.datastore.UserPreferencesRepository
import com.focusnow.app.data.local.FocusNowDatabase
import com.focusnow.app.data.repository.AnalyticsRepository
import com.focusnow.app.data.repository.AppBlockingRepository
import com.focusnow.app.data.repository.BackupRepository
import com.focusnow.app.data.repository.CollegeScheduleRepository
import com.focusnow.app.data.repository.DailyRoutineRepository
import com.focusnow.app.data.repository.ExamRepository
import com.focusnow.app.data.repository.GoalRepository
import com.focusnow.app.data.repository.SleepRepository
import com.focusnow.app.data.repository.StreakRepository
import com.focusnow.app.data.repository.StudyRepository
import com.focusnow.app.data.repository.TaskRepository
import com.focusnow.app.util.NotificationHelper
import com.focusnow.app.worker.DailyStreakWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FocusNowApplication : Application() {

    val database by lazy { FocusNowDatabase.getDatabase(this) }
    val preferencesRepository by lazy { UserPreferencesRepository(this) }

    val streakRepository by lazy { StreakRepository(database.streakDao()) }
    val taskRepository by lazy { TaskRepository(database.taskDao(), streakRepository) }
    val studyRepository by lazy { StudyRepository(database.studySessionDao(), streakRepository) }
    val collegeScheduleRepository by lazy { CollegeScheduleRepository(database.collegeScheduleDao()) }
    val dailyRoutineRepository by lazy { DailyRoutineRepository(database.dailyRoutineDao()) }
    val sleepRepository by lazy { SleepRepository(database.sleepDao(), streakRepository) }
    val goalRepository by lazy { GoalRepository(database.studyGoalDao()) }
    val examRepository by lazy { ExamRepository(database.examDao()) }
    val appBlockingRepository by lazy {
        AppBlockingRepository(
            this,
            database.blockedAppDao(),
            database.blockingProfileDao(),
            preferencesRepository
        )
    }
    val analyticsRepository by lazy {
        AnalyticsRepository(
            database.studySessionDao(),
            database.sleepDao(),
            database.taskDao(),
            database.userProfileDao(),
            streakRepository
        )
    }
    val backupRepository by lazy {
        BackupRepository(
            database.userProfileDao(),
            database.taskDao(),
            database.studySessionDao(),
            database.studyGoalDao(),
            database.collegeScheduleDao(),
            database.dailyRoutineDao(),
            database.sleepDao(),
            database.examDao(),
            database.blockedAppDao(),
            database.blockingProfileDao(),
            database.streakDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        DailyStreakWorker.enqueuePeriodicWork(this)

        CoroutineScope(Dispatchers.IO).launch {
            appBlockingRepository.createDefaultProfilesIfEmpty()
        }
    }
}
