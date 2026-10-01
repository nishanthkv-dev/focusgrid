package com.focusnow.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusnow.app.data.model.ExamDeadline
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams_deadlines ORDER BY isCompleted ASC, targetDateMillis ASC")
    fun getAllExams(): Flow<List<ExamDeadline>>

    @Query("SELECT * FROM exams_deadlines ORDER BY isCompleted ASC, targetDateMillis ASC")
    suspend fun getAllExamsOnce(): List<ExamDeadline>

    @Query("SELECT * FROM exams_deadlines WHERE isCompleted = 0 AND targetDateMillis >= :nowMillis ORDER BY targetDateMillis ASC")
    fun getUpcomingExams(nowMillis: Long): Flow<List<ExamDeadline>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamDeadline): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExams(exams: List<ExamDeadline>)

    @Update
    suspend fun updateExam(exam: ExamDeadline)

    @Delete
    suspend fun deleteExam(exam: ExamDeadline)

    @Query("DELETE FROM exams_deadlines WHERE id = :id")
    suspend fun deleteExamById(id: Long)

    @Query("DELETE FROM exams_deadlines")
    suspend fun deleteAllExams()
}
