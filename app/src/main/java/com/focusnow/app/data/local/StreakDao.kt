package com.focusnow.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusnow.app.data.model.DailyStreak
import com.focusnow.app.data.model.StreakType
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Query("SELECT * FROM daily_streaks")
    fun getAllStreaks(): Flow<List<DailyStreak>>

    @Query("SELECT * FROM daily_streaks")
    suspend fun getAllStreaksOnce(): List<DailyStreak>

    @Query("SELECT * FROM daily_streaks WHERE streakType = :streakType LIMIT 1")
    fun getStreak(streakType: StreakType): Flow<DailyStreak?>

    @Query("SELECT * FROM daily_streaks WHERE streakType = :streakType LIMIT 1")
    suspend fun getStreakOnce(streakType: StreakType): DailyStreak?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStreak(streak: DailyStreak)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStreaks(streaks: List<DailyStreak>)

    @Update
    suspend fun updateStreak(streak: DailyStreak)

    @Query("DELETE FROM daily_streaks")
    suspend fun deleteAllStreaks()
}
