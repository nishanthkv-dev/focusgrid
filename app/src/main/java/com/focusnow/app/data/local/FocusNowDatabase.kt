package com.focusnow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.focusnow.app.data.model.BlockedApp
import com.focusnow.app.data.model.BlockingProfile
import com.focusnow.app.data.model.CollegeClass
import com.focusnow.app.data.model.DailyScheduleRoutine
import com.focusnow.app.data.model.DailyStreak
import com.focusnow.app.data.model.ExamDeadline
import com.focusnow.app.data.model.SleepRecord
import com.focusnow.app.data.model.StudyGoal
import com.focusnow.app.data.model.StudySession
import com.focusnow.app.data.model.TaskItem
import com.focusnow.app.data.model.UserProfile

@Database(
    entities = [
        UserProfile::class,
        TaskItem::class,
        StudySession::class,
        StudyGoal::class,
        CollegeClass::class,
        DailyScheduleRoutine::class,
        SleepRecord::class,
        ExamDeadline::class,
        BlockedApp::class,
        BlockingProfile::class,
        DailyStreak::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FocusNowDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun taskDao(): TaskDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun studyGoalDao(): StudyGoalDao
    abstract fun collegeScheduleDao(): CollegeScheduleDao
    abstract fun dailyRoutineDao(): DailyRoutineDao
    abstract fun sleepDao(): SleepDao
    abstract fun examDao(): ExamDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun blockingProfileDao(): BlockingProfileDao
    abstract fun streakDao(): StreakDao

    companion object {
        @Volatile
        private var INSTANCE: FocusNowDatabase? = null

        fun getDatabase(context: Context): FocusNowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusNowDatabase::class.java,
                    "focus_now_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
