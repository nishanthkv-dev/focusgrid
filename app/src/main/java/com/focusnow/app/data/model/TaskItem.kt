package com.focusnow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskCategory(val displayName: String) {
    COLLEGE("College"),
    GATE("GATE"),
    PLACEMENT("Placement"),
    CODING("Coding"),
    PROJECT("Project"),
    PERSONAL("Personal"),
    OTHER("Other")
}

enum class TaskPriority(val displayName: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4)
}

enum class RepeatType(val displayName: String) {
    NONE("None"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    CUSTOM("Custom Days")
}

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.OTHER,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDateMillis: Long = System.currentTimeMillis(),
    val dueTimeStr: String = "18:00",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isImportant: Boolean = false,
    val repeatType: RepeatType = RepeatType.NONE,
    val repeatDays: String = "", // e.g. "MON,WED,FRI"
    val reminderMinutesBefore: Int = 15,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
