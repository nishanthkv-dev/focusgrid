package com.focusnow.app

import com.focusnow.app.data.model.CollegeClass
import com.focusnow.app.data.model.DailyScheduleRoutine
import com.focusnow.app.data.model.DeadlinePriority
import com.focusnow.app.data.model.DeadlineType
import com.focusnow.app.data.model.ExamDeadline
import com.focusnow.app.data.model.RoutineCategory
import com.focusnow.app.data.model.TaskCategory
import com.focusnow.app.data.model.TaskItem
import com.focusnow.app.data.model.TaskPriority
import com.focusnow.app.data.model.UserProfile
import com.focusnow.app.data.repository.DailyRoutineRepository
import com.focusnow.app.util.SmartPlanGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class SmartPlanGeneratorTest {

    @Test
    fun testGenerateSmartPlan_includesWakeUpAndClassesAndTasks() {
        val userProfile = UserProfile(
            normalWakeTime = "06:30",
            normalSleepTime = "23:00",
            dailyStudyTargetMinutes = 240
        )

        val classes = listOf(
            CollegeClass(subject = "Data Structures", startTimeStr = "09:00", endTimeStr = "10:00", dayOfWeek = DayOfWeek.MONDAY),
            CollegeClass(subject = "Operating Systems", startTimeStr = "10:15", endTimeStr = "11:15", dayOfWeek = DayOfWeek.MONDAY)
        )

        val pendingTasks = listOf(
            TaskItem(title = "Complete LeetCode DP", category = TaskCategory.CODING, priority = TaskPriority.HIGH)
        )

        val upcomingExams = listOf(
            ExamDeadline(title = "GATE Exam", type = DeadlineType.GATE_EXAM, targetDateMillis = System.currentTimeMillis() + 864000000L, subjectOrCategory = "GATE CS")
        )

        val plan = SmartPlanGenerator.generateSmartPlan(
            userProfile = userProfile,
            todayClasses = classes,
            pendingTasks = pendingTasks,
            upcomingExams = upcomingExams,
            existingRoutines = emptyList()
        )

        assertTrue(plan.isNotEmpty())
        assertEquals("06:30", plan.first().startTimeStr)
        assertTrue(plan.any { it.title.contains("Data Structures") })
        assertTrue(plan.any { it.title.contains("LeetCode") })
        assertTrue(plan.any { it.category == RoutineCategory.SLEEP })
    }
}
