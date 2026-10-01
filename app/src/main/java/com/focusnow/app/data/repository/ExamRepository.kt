package com.focusnow.app.data.repository

import com.focusnow.app.data.local.ExamDao
import com.focusnow.app.data.model.ExamDeadline
import kotlinx.coroutines.flow.Flow

class ExamRepository(private val examDao: ExamDao) {
    val allExams: Flow<List<ExamDeadline>> = examDao.getAllExams()

    fun getUpcomingExams(): Flow<List<ExamDeadline>> =
        examDao.getUpcomingExams(System.currentTimeMillis())

    suspend fun insertExam(exam: ExamDeadline): Long = examDao.insertExam(exam)

    suspend fun updateExam(exam: ExamDeadline) = examDao.updateExam(exam)

    suspend fun deleteExam(exam: ExamDeadline) = examDao.deleteExam(exam)

    suspend fun deleteExamById(id: Long) = examDao.deleteExamById(id)

    suspend fun toggleExamCompleted(exam: ExamDeadline) {
        examDao.updateExam(exam.copy(isCompleted = !exam.isCompleted))
    }
}
