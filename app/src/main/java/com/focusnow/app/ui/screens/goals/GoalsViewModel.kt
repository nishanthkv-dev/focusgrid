package com.focusnow.app.ui.screens.goals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.data.model.GoalSubtask
import com.focusnow.app.data.model.StudyGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GoalsUiState(
    val goals: List<StudyGoal> = emptyList(),
    val isAddGoalDialogOpen: Boolean = false,
    val selectedGoalForSubtask: StudyGoal? = null,
    val isAddSubtaskDialogOpen: Boolean = false
)

class GoalsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusNowApplication
    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        observeGoals()
    }

    private fun observeGoals() {
        viewModelScope.launch(Dispatchers.IO) {
            app.goalRepository.allGoals.collect { goals ->
                _uiState.value = _uiState.value.copy(goals = goals)
            }
        }
    }

    fun openAddGoalDialog() {
        _uiState.value = _uiState.value.copy(isAddGoalDialogOpen = true)
    }

    fun closeAddGoalDialog() {
        _uiState.value = _uiState.value.copy(isAddGoalDialogOpen = false)
    }

    fun saveGoal(title: String, category: String, targetHours: Double, deadlineDays: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val deadlineMillis = System.currentTimeMillis() + (deadlineDays * 24L * 60 * 60 * 1000)
            val newGoal = StudyGoal(
                title = title,
                category = category,
                targetHours = targetHours,
                deadlineMillis = deadlineMillis
            )
            app.goalRepository.insertGoal(newGoal)
            closeAddGoalDialog()
        }
    }

    fun deleteGoal(goal: StudyGoal) {
        viewModelScope.launch(Dispatchers.IO) {
            app.goalRepository.deleteGoal(goal)
        }
    }

    fun toggleSubtask(goal: StudyGoal, subtaskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            app.goalRepository.toggleSubtask(goal, subtaskId)
        }
    }

    fun openAddSubtaskDialog(goal: StudyGoal) {
        _uiState.value = _uiState.value.copy(isAddSubtaskDialogOpen = true, selectedGoalForSubtask = goal)
    }

    fun closeAddSubtaskDialog() {
        _uiState.value = _uiState.value.copy(isAddSubtaskDialogOpen = false, selectedGoalForSubtask = null)
    }

    fun addSubtask(subtaskTitle: String) {
        val goal = _uiState.value.selectedGoalForSubtask ?: return
        viewModelScope.launch(Dispatchers.IO) {
            app.goalRepository.addSubtask(goal, subtaskTitle)
            closeAddSubtaskDialog()
        }
    }

    fun parseSubtasks(json: String): List<GoalSubtask> =
        app.goalRepository.parseSubtasks(json)

    fun initializeSampleGoals() {
        viewModelScope.launch(Dispatchers.IO) {
            val dsaSubtasks = listOf(
                GoalSubtask(id = "1", title = "Arrays & Strings", isCompleted = true),
                GoalSubtask(id = "2", title = "Two Pointers & Sliding Window", isCompleted = true),
                GoalSubtask(id = "3", title = "Linked Lists", isCompleted = false),
                GoalSubtask(id = "4", title = "Trees & Binary Search", isCompleted = false),
                GoalSubtask(id = "5", title = "Dynamic Programming", isCompleted = false)
            )
            val dsaGoal = StudyGoal(
                title = "Complete Data Structures & Algorithms",
                category = "Coding",
                targetHours = 50.0,
                progressPercentage = 40,
                subtasksJson = app.goalRepository.serializeSubtasks(dsaSubtasks)
            )

            val gateSubtasks = listOf(
                GoalSubtask(id = "6", title = "Discrete Mathematics", isCompleted = true),
                GoalSubtask(id = "7", title = "Computer Architecture", isCompleted = false),
                GoalSubtask(id = "8", title = "Operating Systems", isCompleted = false)
            )
            val gateGoal = StudyGoal(
                title = "GATE CS Syllabus Prep",
                category = "GATE",
                targetHours = 100.0,
                progressPercentage = 33,
                subtasksJson = app.goalRepository.serializeSubtasks(gateSubtasks)
            )

            app.database.studyGoalDao().insertAllGoals(listOf(dsaGoal, gateGoal))
        }
    }
}
