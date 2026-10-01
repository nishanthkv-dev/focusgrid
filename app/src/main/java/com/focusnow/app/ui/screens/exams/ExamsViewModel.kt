package com.focusnow.app.ui.screens.exams

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.data.model.DeadlinePriority
import com.focusnow.app.data.model.DeadlineType
import com.focusnow.app.data.model.ExamDeadline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExamsUiState(
    val exams: List<ExamDeadline> = emptyList(),
    val isAddDialogOpen: Boolean = false
)

class ExamsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusNowApplication
    private val _uiState = MutableStateFlow(ExamsUiState())
    val uiState: StateFlow<ExamsUiState> = _uiState.asStateFlow()

    init {
        observeExams()
    }

    private fun observeExams() {
        viewModelScope.launch(Dispatchers.IO) {
            app.examRepository.allExams.collect { exams ->
                _uiState.value = _uiState.value.copy(exams = exams)
            }
        }
    }

    fun openAddDialog() { _uiState.value = _uiState.value.copy(isAddDialogOpen = true) }
    fun closeAddDialog() { _uiState.value = _uiState.value.copy(isAddDialogOpen = false) }

    fun saveExam(
        title: String,
        type: DeadlineType,
        targetDaysFromNow: Int,
        timeStr: String,
        subject: String,
        priority: DeadlinePriority,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetDateMillis = System.currentTimeMillis() + (targetDaysFromNow * 24L * 60 * 60 * 1000)
            val newExam = ExamDeadline(
                title = title,
                type = type,
                targetDateMillis = targetDateMillis,
                timeStr = timeStr,
                subjectOrCategory = subject,
                priority = priority,
                notes = notes
            )
            app.examRepository.insertExam(newExam)
            closeAddDialog()
        }
    }

    fun toggleCompleted(exam: ExamDeadline) {
        viewModelScope.launch(Dispatchers.IO) {
            app.examRepository.toggleExamCompleted(exam)
        }
    }

    fun deleteExam(exam: ExamDeadline) {
        viewModelScope.launch(Dispatchers.IO) {
            app.examRepository.deleteExam(exam)
        }
    }

    fun initializeSampleExams() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val sampleExams = listOf(
                ExamDeadline(
                    title = "DBMS Mid-Semester Exam",
                    type = DeadlineType.EXAM,
                    targetDateMillis = now + (12L * 24 * 60 * 60 * 1000),
                    subjectOrCategory = "Database Management",
                    priority = DeadlinePriority.URGENT
                ),
                ExamDeadline(
                    title = "Capstone Project Milestone",
                    type = DeadlineType.PROJECT,
                    targetDateMillis = now + (8L * 24 * 60 * 60 * 1000),
                    subjectOrCategory = "Software Engineering",
                    priority = DeadlinePriority.HIGH
                ),
                ExamDeadline(
                    title = "GATE CS 2026 Examination",
                    type = DeadlineType.GATE_EXAM,
                    targetDateMillis = now + (45L * 24 * 60 * 60 * 1000),
                    subjectOrCategory = "GATE",
                    priority = DeadlinePriority.HIGH
                )
            )
            app.database.examDao().insertAllExams(sampleExams)
        }
    }
}
