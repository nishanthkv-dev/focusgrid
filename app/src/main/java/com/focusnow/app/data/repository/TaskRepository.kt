package com.focusnow.app.data.repository

import com.focusnow.app.data.local.TaskDao
import com.focusnow.app.data.model.RepeatType
import com.focusnow.app.data.model.TaskCategory
import com.focusnow.app.data.model.TaskItem
import com.focusnow.app.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TaskRepository(
    private val taskDao: TaskDao,
    private val streakRepository: StreakRepository
) {
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()

    fun getTasksByCategory(category: TaskCategory): Flow<List<TaskItem>> =
        taskDao.getTasksByCategory(category)

    suspend fun getTaskById(id: Long): TaskItem? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskItem): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskItem) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskItem) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun toggleTaskCompletion(task: TaskItem) {
        val newCompleted = !task.isCompleted
        val completedAt = if (newCompleted) System.currentTimeMillis() else null
        val updated = task.copy(isCompleted = newCompleted, completedAt = completedAt)
        taskDao.updateTask(updated)

        if (newCompleted) {
            streakRepository.recordTaskCompletedToday()

            // Handle recurring task creation if needed
            if (task.repeatType != RepeatType.NONE) {
                handleRecurringTask(task)
            }
        }
    }

    private suspend fun handleRecurringTask(task: TaskItem) {
        val nextDueDateMillis = when (task.repeatType) {
            RepeatType.DAILY -> task.dueDateMillis + (24L * 60 * 60 * 1000)
            RepeatType.WEEKLY -> task.dueDateMillis + (7L * 24 * 60 * 60 * 1000)
            RepeatType.MONTHLY -> task.dueDateMillis + (30L * 24 * 60 * 60 * 1000)
            RepeatType.CUSTOM -> task.dueDateMillis + (24L * 60 * 60 * 1000)
            RepeatType.NONE -> return
        }

        val nextTask = task.copy(
            id = 0,
            dueDateMillis = nextDueDateMillis,
            isCompleted = false,
            completedAt = null,
            createdAt = System.currentTimeMillis()
        )
        taskDao.insertTask(nextTask)
    }

    suspend fun reorderTasks(tasks: List<TaskItem>) {
        tasks.forEachIndexed { index, task ->
            taskDao.updateTask(task.copy(orderIndex = index))
        }
    }

    fun getCompletedTodayCount(): Flow<Int> {
        val start = DateTimeUtils.getStartOfDayMillis(LocalDate.now())
        val end = DateTimeUtils.getEndOfDayMillis(LocalDate.now())
        return taskDao.getCompletedTasksCountForDay(start, end)
    }
}
