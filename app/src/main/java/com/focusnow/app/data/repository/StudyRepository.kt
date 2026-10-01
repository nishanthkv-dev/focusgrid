package com.focusnow.app.data.repository

import com.focusnow.app.data.local.StudySessionDao
import com.focusnow.app.data.model.StudySession
import com.focusnow.app.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class StudyRepository(
    private val studySessionDao: StudySessionDao,
    private val streakRepository: StreakRepository
) {
    val allSessions: Flow<List<StudySession>> = studySessionDao.getAllSessions()

    fun getSessionsForDate(dateStr: String): Flow<List<StudySession>> =
        studySessionDao.getSessionsForDate(dateStr)

    fun getTodaySessions(): Flow<List<StudySession>> =
        studySessionDao.getSessionsForDate(DateTimeUtils.getTodayDateStr())

    fun getTotalStudyMinutesForDate(dateStr: String): Flow<Int?> =
        studySessionDao.getTotalStudyMinutesForDate(dateStr)

    suspend fun saveStudySession(session: StudySession, dailyTargetMinutes: Int): Long {
        val id = studySessionDao.insertSession(session)

        // Check if today's study target was achieved to update streak
        val todaySessions = studySessionDao.getSessionsForDateOnce(session.dateStr)
        val totalMinutesToday = todaySessions.sumOf { it.durationMinutes }
        streakRepository.recordStudySessionCompleted(totalMinutesToday, dailyTargetMinutes)

        return id
    }

    suspend fun deleteSession(session: StudySession) =
        studySessionDao.deleteSession(session)

    suspend fun getSessionsBetweenOnce(startMillis: Long, endMillis: Long): List<StudySession> =
        studySessionDao.getSessionsBetweenOnce(startMillis, endMillis)
}
