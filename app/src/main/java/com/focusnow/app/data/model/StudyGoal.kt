package com.focusnow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class GoalSubtask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "study_goals")
data class StudyGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "Academic",
    val targetHours: Double = 0.0,
    val currentHours: Double = 0.0,
    val deadlineMillis: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // default 30 days
    val progressPercentage: Int = 0,
    val subtasksJson: String = "[]", // List<GoalSubtask> serialized as JSON
    val isCompleted: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
