package com.focusnow.app.data.repository

import com.focusnow.app.data.local.StudyGoalDao
import com.focusnow.app.data.model.GoalSubtask
import com.focusnow.app.data.model.StudyGoal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val studyGoalDao: StudyGoalDao) {
    private val gson = Gson()

    val allGoals: Flow<List<StudyGoal>> = studyGoalDao.getAllGoals()

    suspend fun getGoalById(id: Long): StudyGoal? = studyGoalDao.getGoalById(id)

    suspend fun insertGoal(goal: StudyGoal): Long = studyGoalDao.insertGoal(goal)

    suspend fun updateGoal(goal: StudyGoal) = studyGoalDao.updateGoal(goal)

    suspend fun deleteGoal(goal: StudyGoal) = studyGoalDao.deleteGoal(goal)

    suspend fun deleteGoalById(id: Long) = studyGoalDao.deleteGoalById(id)

    fun parseSubtasks(json: String): List<GoalSubtask> {
        return try {
            val type = object : TypeToken<List<GoalSubtask>>() {}.type
            gson.fromJson<List<GoalSubtask>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeSubtasks(subtasks: List<GoalSubtask>): String {
        return gson.toJson(subtasks)
    }

    suspend fun toggleSubtask(goal: StudyGoal, subtaskId: String) {
        val currentSubtasks = parseSubtasks(goal.subtasksJson)
        val updatedSubtasks = currentSubtasks.map { subtask ->
            if (subtask.id == subtaskId) {
                subtask.copy(isCompleted = !subtask.isCompleted)
            } else {
                subtask
            }
        }

        val completedCount = updatedSubtasks.count { it.isCompleted }
        val totalCount = updatedSubtasks.size
        val newProgress = if (totalCount > 0) {
            (completedCount * 100) / totalCount
        } else {
            goal.progressPercentage
        }

        val isCompleted = newProgress >= 100

        val updatedGoal = goal.copy(
            subtasksJson = serializeSubtasks(updatedSubtasks),
            progressPercentage = newProgress,
            isCompleted = isCompleted
        )
        studyGoalDao.updateGoal(updatedGoal)
    }

    suspend fun addSubtask(goal: StudyGoal, subtaskTitle: String) {
        val currentSubtasks = parseSubtasks(goal.subtasksJson).toMutableList()
        val newSubtask = GoalSubtask(
            id = System.currentTimeMillis().toString(),
            title = subtaskTitle,
            isCompleted = false
        )
        currentSubtasks.add(newSubtask)

        val completedCount = currentSubtasks.count { it.isCompleted }
        val totalCount = currentSubtasks.size
        val newProgress = (completedCount * 100) / totalCount

        val updatedGoal = goal.copy(
            subtasksJson = serializeSubtasks(currentSubtasks),
            progressPercentage = newProgress
        )
        studyGoalDao.updateGoal(updatedGoal)
    }
}
