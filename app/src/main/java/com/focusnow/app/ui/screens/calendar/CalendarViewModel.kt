package com.focusnow.app.ui.screens.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.data.model.CollegeClass
import com.focusnow.app.data.model.ExamDeadline
import com.focusnow.app.data.model.SleepRecord
import com.focusnow.app.data.model.StudySession
import com.focusnow.app.data.model.TaskItem
import com.focusnow.app.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class DayActivitySummary(
    val hasClasses: Boolean = false,
    val hasStudy: Boolean = false,
    val hasTasks: Boolean = false,
    val hasExams: Boolean = false
)

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val dateActivitiesMap: Map<String, DayActivitySummary> = emptyMap(),
    val selectedDateClasses: List<CollegeClass> = emptyList(),
    val selectedDateSessions: List<StudySession> = emptyList(),
    val selectedDateTasks: List<TaskItem> = emptyList(),
    val selectedDateExams: List<ExamDeadline> = emptyList(),
    val selectedDateSleep: SleepRecord? = null
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusNowApplication
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadCalendarActivities()
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadActivitiesForDate(date)
    }

    fun changeMonth(monthDelta: Long) {
        val newMonth = _uiState.value.currentMonth.plusMonths(monthDelta)
        _uiState.value = _uiState.value.copy(currentMonth = newMonth)
        loadCalendarActivities()
    }

    private fun loadCalendarActivities() {
        viewModelScope.launch(Dispatchers.IO) {
            val sessions = app.studyRepository.allSessions
            val tasks = app.taskRepository.allTasks
            val exams = app.examRepository.allExams

            // Build indicator map
            val map = mutableMapOf<String, DayActivitySummary>()
            val allSessions = app.database.studySessionDao().getAllSessionsOnce()
            val allTasks = app.database.taskDao().getAllTasksOnce()
            val allExams = app.database.examDao().getAllExamsOnce()

            allSessions.forEach {
                val current = map[it.dateStr] ?: DayActivitySummary()
                map[it.dateStr] = current.copy(hasStudy = true)
            }

            allTasks.forEach {
                val dStr = DateTimeUtils.formatMillisToDateStr(it.dueDateMillis)
                val current = map[dStr] ?: DayActivitySummary()
                map[dStr] = current.copy(hasTasks = true)
            }

            allExams.forEach {
                val dStr = DateTimeUtils.formatMillisToDateStr(it.targetDateMillis)
                val current = map[dStr] ?: DayActivitySummary()
                map[dStr] = current.copy(hasExams = true)
            }

            _uiState.value = _uiState.value.copy(dateActivitiesMap = map)
            loadActivitiesForDate(_uiState.value.selectedDate)
        }
    }

    private fun loadActivitiesForDate(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            val dateStr = DateTimeUtils.formatDateStr(date)
            val classes = app.collegeScheduleRepository.getClassesForDate(date)
            val sessions = app.database.studySessionDao().getSessionsForDateOnce(dateStr)
            val allTasks = app.database.taskDao().getAllTasksOnce()
            val tasks = allTasks.filter { DateTimeUtils.formatMillisToDateStr(it.dueDateMillis) == dateStr }
            val allExams = app.database.examDao().getAllExamsOnce()
            val exams = allExams.filter { DateTimeUtils.formatMillisToDateStr(it.targetDateMillis) == dateStr }
            val sleep = app.database.sleepDao().getSleepRecordForDateOnce(dateStr)

            _uiState.value = _uiState.value.copy(
                selectedDateClasses = classes,
                selectedDateSessions = sessions,
                selectedDateTasks = tasks,
                selectedDateExams = exams,
                selectedDateSleep = sleep
            )
        }
    }
}
