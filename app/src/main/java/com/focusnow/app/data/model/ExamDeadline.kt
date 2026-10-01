package com.focusnow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeadlineType(val displayName: String) {
    EXAM("Exam"),
    ASSIGNMENT("Assignment"),
    PROJECT("Project Submission"),
    HACKATHON("Hackathon"),
    CERTIFICATION("Certification"),
    PLACEMENT_TEST("Placement Test"),
    GATE_EXAM("GATE Exam"),
    OTHER("Other")
}

enum class DeadlinePriority(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent")
}

@Entity(tableName = "exams_deadlines")
data class ExamDeadline(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: DeadlineType = DeadlineType.EXAM,
    val targetDateMillis: Long,
    val timeStr: String = "10:00",
    val subjectOrCategory: String = "",
    val priority: DeadlinePriority = DeadlinePriority.HIGH,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
