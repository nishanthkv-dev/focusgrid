package com.focusnow.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusnow.app.data.model.DailyScheduleRoutine
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRoutineDao {
    @Query("SELECT * FROM daily_routine_blocks ORDER BY startTimeStr ASC")
    fun getAllRoutines(): Flow<List<DailyScheduleRoutine>>

    @Query("SELECT * FROM daily_routine_blocks ORDER BY startTimeStr ASC")
    suspend fun getAllRoutinesOnce(): List<DailyScheduleRoutine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: DailyScheduleRoutine): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRoutines(routines: List<DailyScheduleRoutine>)

    @Update
    suspend fun updateRoutine(routine: DailyScheduleRoutine)

    @Delete
    suspend fun deleteRoutine(routine: DailyScheduleRoutine)

    @Query("DELETE FROM daily_routine_blocks WHERE id = :id")
    suspend fun deleteRoutineById(id: Long)

    @Query("DELETE FROM daily_routine_blocks")
    suspend fun deleteAllRoutines()
}
