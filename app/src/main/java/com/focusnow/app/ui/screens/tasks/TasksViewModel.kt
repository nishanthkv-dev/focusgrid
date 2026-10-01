package com.focusnow.app.ui.screens.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.data.model.RepeatType
import com.focusnow.app.data.model.TaskCategory
import com.focusnow.app.data.model.TaskItem
import com.focusnow.app.data.model.TaskPriority
import com.focusnow.app.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class TaskTab(val title: String) {
    TODAY("Today"),
    UPCOMING("Upcoming"),
    COMPLETED("Completed"),
    OVERDUE("Overdue")
}

data class TasksUiState(
    val selectedTab: TaskTab = TaskTab.TODAY,
    val selectedCategoryFilter: TaskCategory? = null,
    val searchQuery: String = "",
    val allTasks: List<TaskItem> = emptyList(),
    val todayTasks: List<TaskItem> = emptyList(),
    val upcomingTasks: List<TaskItem> = emptyList(),
    val completedTasks: List<TaskItem> = emptyList(),
    val overdueTasks: List<TaskItem> = emptyList(),
    val isAddEditDialogOpen: Boolean = false,
    val editingTask: TaskItem? = null
)

class TasksViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusNowApplication
    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            app.taskRepository.allTasks.collect { tasks ->
                processTasks(tasks, _uiState.value.selectedCategoryFilter, _uiState.value.searchQuery)
            }
        }
    }

    private fun processTasks(
        tasks: List<TaskItem>,
        categoryFilter: TaskCategory?,
        query: String
    ) {
        val todayStr = DateTimeUtils.getTodayDateStr()
        val startOfToday = DateTimeUtils.getStartOfDayMillis(LocalDate.now())
        val endOfToday = DateTimeUtils.getEndOfDayMillis(LocalDate.now())

        var filtered = tasks
        if (categoryFilter != null) {
            filtered = filtered.filter { it.category == categoryFilter }
        }
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
            }
        }

        val completed = filtered.filter { it.isCompleted }
        val pending = filtered.filter { !it.isCompleted }

        val today = pending.filter {
            val taskDateStr = DateTimeUtils.formatMillisToDateStr(it.dueDateMillis)
            taskDateStr == todayStr || (it.dueDateMillis in startOfToday..endOfToday)
        }

        val overdue = pending.filter {
            it.dueDateMillis < startOfToday && DateTimeUtils.formatMillisToDateStr(it.dueDateMillis) != todayStr
        }

        val upcoming = pending.filter {
            it.dueDateMillis > endOfToday
        }

        _uiState.value = _uiState.value.copy(
            allTasks = tasks,
            todayTasks = today,
            upcomingTasks = upcoming,
            completedTasks = completed,
            overdueTasks = overdue
        )
    }

    fun selectTab(tab: TaskTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun filterByCategory(category: TaskCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = category)
        processTasks(_uiState.value.allTasks, category, _uiState.value.searchQuery)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        processTasks(_uiState.value.allTasks, _uiState.value.selectedCategoryFilter, query)
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch(Dispatchers.IO) {
            app.taskRepository.toggleTaskCompletion(task)
        }
    }

    fun toggleImportant(task: TaskItem) {
        viewModelScope.launch(Dispatchers.IO) {
            app.taskRepository.updateTask(task.copy(isImportant = !task.isImportant))
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch(Dispatchers.IO) {
            app.taskRepository.deleteTask(task)
        }
    }

    fun openAddTaskDialog() {
        _uiState.value = _uiState.value.copy(isAddEditDialogOpen = true, editingTask = null)
    }

    fun openEditTaskDialog(task: TaskItem) {
        _uiState.value = _uiState.value.copy(isAddEditDialogOpen = true, editingTask = task)
    }

    fun closeAddEditDialog() {
        _uiState.value = _uiState.value.copy(isAddEditDialogOpen = false, editingTask = null)
    }

    fun saveTask(
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        dueDateMillis: Long,
        dueTimeStr: String,
        isImportant: Boolean,
        repeatType: RepeatType
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val editing = _uiState.value.editingTask
            if (editing != null) {
                val updated = editing.copy(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueDateMillis = dueDateMillis,
                    dueTimeStr = dueTimeStr,
                    isImportant = isImportant,
                    repeatType = repeatType
                )
                app.taskRepository.updateTask(updated)
            } else {
                val newTask = TaskItem(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueDateMillis = dueDateMillis,
                    dueTimeStr = dueTimeStr,
                    isImportant = isImportant,
                    repeatType = repeatType
                )
                app.taskRepository.insertTask(newTask)
            }
            closeAddEditDialog()
        }
    }
}
