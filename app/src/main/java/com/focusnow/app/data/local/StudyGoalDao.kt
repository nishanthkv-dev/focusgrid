package com.focusnow.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusnow.app.data.model.StudyGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyGoalDao {
    @Query("SELECT * FROM study_goals ORDER BY isCompleted ASC, deadlineMillis ASC")
    fun getAllGoals(): Flow<List<StudyGoal>>

    @Query("SELECT * FROM study_goals ORDER BY isCompleted ASC, deadlineMillis ASC")
    suspend fun getAllGoalsOnce(): List<StudyGoal>

    @Query("SELECT * FROM study_goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Long): StudyGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: StudyGoal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoals(goals: List<StudyGoal>)

    @Update
    suspend fun updateGoal(goal: StudyGoal)

    @Delete
    suspend fun deleteGoal(goal: StudyGoal)

    @Query("DELETE FROM study_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    @Query("DELETE FROM study_goals")
    suspend fun deleteAllGoals()
}
