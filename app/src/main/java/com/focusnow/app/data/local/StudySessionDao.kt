package com.focusnow.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusnow.app.data.model.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions ORDER BY startTimeMillis DESC")
    suspend fun getAllSessionsOnce(): List<StudySession>

    @Query("SELECT * FROM study_sessions WHERE dateStr = :dateStr ORDER BY startTimeMillis DESC")
    fun getSessionsForDate(dateStr: String): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE dateStr = :dateStr ORDER BY startTimeMillis DESC")
    suspend fun getSessionsForDateOnce(dateStr: String): List<StudySession>

    @Query("SELECT * FROM study_sessions WHERE startTimeMillis >= :startMillis AND endTimeMillis <= :endMillis ORDER BY startTimeMillis ASC")
    fun getSessionsBetween(startMillis: Long, endMillis: Long): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE startTimeMillis >= :startMillis AND endTimeMillis <= :endMillis ORDER BY startTimeMillis ASC")
    suspend fun getSessionsBetweenOnce(startMillis: Long, endMillis: Long): List<StudySession>

    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE dateStr = :dateStr")
    fun getTotalStudyMinutesForDate(dateStr: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSessions(sessions: List<StudySession>)

    @Delete
    suspend fun deleteSession(session: StudySession)

    @Query("DELETE FROM study_sessions")
    suspend fun deleteAllSessions()
}
