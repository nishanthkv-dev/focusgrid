package com.focusnow.app.ui.screens.smartplan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.data.model.DailyScheduleRoutine
import com.focusnow.app.util.SmartPlanGenerator
import com.focusnow.app.util.SmartPlanItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SmartPlanUiState(
    val suggestedPlan: List<SmartPlanItem> = emptyList(),
    val isAppliedSuccess: Boolean = false,
    val isGenerating: Boolean = false
)

class SmartPlanViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusNowApplication
    private val _uiState = MutableStateFlow(SmartPlanUiState())
    val uiState: StateFlow<SmartPlanUiState> = _uiState.asStateFlow()

    init {
        generatePlan()
    }

    fun generatePlan() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isGenerating = true, isAppliedSuccess = false)

            val profile = app.database.userProfileDao().getUserProfileOnce()
            val todayClasses = app.collegeScheduleRepository.getClassesForDate(LocalDate.now())
            val pendingTasks = app.database.taskDao().getAllTasksOnce().filter { !it.isCompleted }
            val upcomingExams = app.database.examDao().getAllExamsOnce().filter { !it.isCompleted }
            val routines = app.database.dailyRoutineDao().getAllRoutinesOnce()

            val plan = SmartPlanGenerator.generateSmartPlan(
                userProfile = profile,
                todayClasses = todayClasses,
                pendingTasks = pendingTasks,
                upcomingExams = upcomingExams,
                existingRoutines = routines
            )

            _uiState.value = _uiState.value.copy(
                suggestedPlan = plan,
                isGenerating = false
            )
        }
    }

    fun applyPlanToRoutines() {
        viewModelScope.launch(Dispatchers.IO) {
            val plan = _uiState.value.suggestedPlan
            val newRoutines = plan.mapIndexed { index, item ->
                DailyScheduleRoutine(
                    title = item.title,
                    startTimeStr = item.startTimeStr,
                    endTimeStr = item.endTimeStr,
                    category = item.category,
                    notes = item.notes,
                    orderIndex = index,
                    isEnabled = true
                )
            }

            app.database.dailyRoutineDao().deleteAllRoutines()
            app.database.dailyRoutineDao().insertAllRoutines(newRoutines)

            _uiState.value = _uiState.value.copy(isAppliedSuccess = true)
        }
    }
}
