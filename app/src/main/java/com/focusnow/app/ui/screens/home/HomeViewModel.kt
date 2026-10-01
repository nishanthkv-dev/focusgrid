package com.focusnow.app.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.data.model.CollegeClass
import com.focusnow.app.data.model.DailyStreak
import com.focusnow.app.data.model.ExamDeadline
import com.focusnow.app.data.model.SleepRecord
import com.focusnow.app.data.model.StreakType
import com.focusnow.app.data.model.TaskItem
import com.focusnow.app.data.model.UserProfile
import com.focusnow.app.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val userProfile: UserProfile? = null,
    val studyMinutesToday: Int = 0,
    val studyTargetMinutes: Int = 240,
    val completedTasksToday: Int = 0,
    val totalTasksToday: Int = 0,
    val pendingTasks: List<TaskItem> = emptyList(),
    val todaySleepRecord: SleepRecord? = null,
    val overallStreak: Int = 0,
    val studyStreak: Int = 0,
    val todayClasses: List<CollegeClass> = emptyList(),
    val upcomingExams: List<ExamDeadline> = emptyList(),
    val productivityPercentage: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusNowApplication
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStr = DateTimeUtils.getTodayDateStr()
            val todayDate = LocalDate.now()

            // Observe UserProfile
            launch {
                app.database.userProfileDao().getUserProfile().collect { profile ->
                    _uiState.value = _uiState.value.copy(
                        userProfile = profile,
                        studyTargetMinutes = profile?.dailyStudyTargetMinutes ?: 240
                    )
                    calculateProductivity()
                }
            }

            // Observe Today's Study Minutes
            launch {
                app.studyRepository.getTotalStudyMinutesForDate(todayStr).collect { studyMins ->
                    _uiState.value = _uiState.value.copy(studyMinutesToday = studyMins ?: 0)
                    calculateProductivity()
                }
            }

            // Observe Tasks
            launch {
                app.taskRepository.allTasks.collect { tasks ->
                    val todayTasks = tasks.filter {
                        val taskDate = DateTimeUtils.formatMillisToDateStr(it.dueDateMillis)
                        taskDate == todayStr
                    }
                    val completed = todayTasks.count { it.isCompleted }
                    val pending = tasks.filter { !it.isCompleted }.take(4)

                    _uiState.value = _uiState.value.copy(
                        completedTasksToday = completed,
                        totalTasksToday = todayTasks.size,
                        pendingTasks = pending
                    )
                    calculateProductivity()
                }
            }

            // Observe Sleep Record for Today
            launch {
                app.sleepRepository.getSleepRecordForDate(todayStr).collect { sleep ->
                    _uiState.value = _uiState.value.copy(todaySleepRecord = sleep)
                }
            }

            // Observe Streaks
            launch {
                app.streakRepository.getStreak(StreakType.OVERALL).collect { streak ->
                    _uiState.value = _uiState.value.copy(overallStreak = streak?.currentStreak ?: 0)
                }
            }
            launch {
                app.streakRepository.getStreak(StreakType.STUDY).collect { streak ->
                    _uiState.value = _uiState.value.copy(studyStreak = streak?.currentStreak ?: 0)
                }
            }

            // Today's College Classes
            val classes = app.collegeScheduleRepository.getClassesForDate(todayDate)
            _uiState.value = _uiState.value.copy(todayClasses = classes)

            // Upcoming Exams
            launch {
                app.examRepository.getUpcomingExams().collect { exams ->
                    _uiState.value = _uiState.value.copy(upcomingExams = exams.take(3))
                }
            }
        }
    }

    private fun calculateProductivity() {
        val state = _uiState.value
        val studyScore = if (state.studyTargetMinutes > 0) {
            (state.studyMinutesToday.toFloat() / state.studyTargetMinutes.toFloat()) * 50f
        } else 0f

        val taskScore = if (state.totalTasksToday > 0) {
            (state.completedTasksToday.toFloat() / state.totalTasksToday.toFloat()) * 50f
        } else 25f

        val total = (studyScore + taskScore).coerceIn(0f, 100f).toInt()
        _uiState.value = state.copy(productivityPercentage = total)
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch(Dispatchers.IO) {
            app.taskRepository.toggleTaskCompletion(task)
        }
    }
}
