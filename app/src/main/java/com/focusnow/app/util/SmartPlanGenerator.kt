package com.focusnow.app.util

import com.focusnow.app.data.model.CollegeClass
import com.focusnow.app.data.model.DailyScheduleRoutine
import com.focusnow.app.data.model.ExamDeadline
import com.focusnow.app.data.model.RoutineCategory
import com.focusnow.app.data.model.TaskItem
import com.focusnow.app.data.model.UserProfile

data class SmartPlanItem(
    val startTimeStr: String,
    val endTimeStr: String,
    val title: String,
    val category: RoutineCategory,
    val isCollegeClass: Boolean = false,
    val notes: String = ""
)

object SmartPlanGenerator {

    fun generateSmartPlan(
        userProfile: UserProfile?,
        todayClasses: List<CollegeClass>,
        pendingTasks: List<TaskItem>,
        upcomingExams: List<ExamDeadline>,
        existingRoutines: List<DailyScheduleRoutine>
    ): List<SmartPlanItem> {
        val wakeTimeStr = userProfile?.normalWakeTime ?: "06:30"
        val sleepTimeStr = userProfile?.normalSleepTime ?: "23:00"
        val studyTargetMinutes = userProfile?.dailyStudyTargetMinutes ?: 240

        val wakeMinutes = DateTimeUtils.timeStringToMinutes(wakeTimeStr)
        val sleepMinutes = DateTimeUtils.timeStringToMinutes(sleepTimeStr)

        val plan = mutableListOf<SmartPlanItem>()

        // 1. Wake up block
        plan.add(
            SmartPlanItem(
                startTimeStr = wakeTimeStr,
                endTimeStr = DateTimeUtils.minutesToTimeString(wakeMinutes + 30),
                title = "Wake up & Morning Routine",
                category = RoutineCategory.WAKE_UP
            )
        )

        var currentMin = wakeMinutes + 30

        // If time before 8:30 AM, insert Morning Study / Exam Prep
        if (currentMin < 510) { // 8:30 AM is 510 mins
            val examSubject = upcomingExams.firstOrNull()?.subjectOrCategory
            val morningStudyTitle = if (!examSubject.isNullOrBlank()) {
                "Prep: $examSubject"
            } else {
                "Morning Deep Work"
            }

            val morningStudyEnd = minOf(currentMin + 60, 500)
            if (morningStudyEnd - currentMin >= 30) {
                plan.add(
                    SmartPlanItem(
                        startTimeStr = DateTimeUtils.minutesToTimeString(currentMin),
                        endTimeStr = DateTimeUtils.minutesToTimeString(morningStudyEnd),
                        title = morningStudyTitle,
                        category = RoutineCategory.STUDY
                    )
                )
                currentMin = morningStudyEnd
            }
        }

        // Breakfast / Commute
        if (currentMin < 540) {
            plan.add(
                SmartPlanItem(
                    startTimeStr = DateTimeUtils.minutesToTimeString(currentMin),
                    endTimeStr = DateTimeUtils.minutesToTimeString(minOf(currentMin + 30, 540)),
                    title = "Breakfast & College Prep",
                    category = RoutineCategory.DINNER
                )
            )
            currentMin = minOf(currentMin + 30, 540)
        }

        // 2. College Classes
        val sortedClasses = todayClasses.sortedBy { DateTimeUtils.timeStringToMinutes(it.startTimeStr) }
        for (cls in sortedClasses) {
            val classStartMin = DateTimeUtils.timeStringToMinutes(cls.startTimeStr)
            val classEndMin = DateTimeUtils.timeStringToMinutes(cls.endTimeStr)

            // Fill gap between currentMin and classStartMin if any
            if (classStartMin > currentMin + 15) {
                plan.add(
                    SmartPlanItem(
                        startTimeStr = DateTimeUtils.minutesToTimeString(currentMin),
                        endTimeStr = DateTimeUtils.minutesToTimeString(classStartMin),
                        title = "Study Break / Library",
                        category = RoutineCategory.REST
                    )
                )
            }

            plan.add(
                SmartPlanItem(
                    startTimeStr = cls.startTimeStr,
                    endTimeStr = cls.endTimeStr,
                    title = "Class: ${cls.subject} (${cls.room})",
                    category = RoutineCategory.COLLEGE,
                    isCollegeClass = true,
                    notes = "Faculty: ${cls.faculty}"
                )
            )
            currentMin = maxOf(currentMin, classEndMin)
        }

        // After college rest
        if (currentMin < 1020) { // 5:00 PM is 1020
            currentMin = maxOf(currentMin, 990) // 4:30 PM
            plan.add(
                SmartPlanItem(
                    startTimeStr = DateTimeUtils.minutesToTimeString(currentMin),
                    endTimeStr = DateTimeUtils.minutesToTimeString(currentMin + 30),
                    title = "Return Home & Refresh",
                    category = RoutineCategory.REST
                )
            )
            currentMin += 30
        }

        // 3. Prioritized Tasks allocation
        val urgentTasks = pendingTasks.filter { !it.isCompleted }
            .sortedByDescending { it.priority.level }

        if (urgentTasks.isNotEmpty()) {
            val topTask = urgentTasks.first()
            val taskCat = when (topTask.category.name) {
                "CODING" -> RoutineCategory.CODING
                "GATE" -> RoutineCategory.GATE_PREP
                "COLLEGE", "PROJECT" -> RoutineCategory.STUDY
                else -> RoutineCategory.STUDY
            }

            plan.add(
                SmartPlanItem(
                    startTimeStr = DateTimeUtils.minutesToTimeString(currentMin),
                    endTimeStr = DateTimeUtils.minutesToTimeString(currentMin + 60),
                    title = "Focus: ${topTask.title}",
                    category = taskCat,
                    notes = "Priority: ${topTask.priority.displayName}"
                )
            )
            currentMin += 60
        }

        // Dinner break
        val dinnerMin = maxOf(currentMin, 1170) // 7:30 PM
        if (dinnerMin > currentMin) {
            plan.add(
                SmartPlanItem(
                    startTimeStr = DateTimeUtils.minutesToTimeString(currentMin),
                    endTimeStr = DateTimeUtils.minutesToTimeString(dinnerMin),
                    title = "Coding / Problem Solving",
                    category = RoutineCategory.CODING
                )
            )
        }

        plan.add(
            SmartPlanItem(
                startTimeStr = DateTimeUtils.minutesToTimeString(dinnerMin),
                endTimeStr = DateTimeUtils.minutesToTimeString(dinnerMin + 45),
                title = "Dinner & Family Time",
                category = RoutineCategory.DINNER
            )
        )
        currentMin = dinnerMin + 45

        // Evening Study / Revision block before sleep
        val eveningStudyEnd = minOf(currentMin + 75, sleepMinutes - 30)
        if (eveningStudyEnd > currentMin + 20) {
            plan.add(
                SmartPlanItem(
                    startTimeStr = DateTimeUtils.minutesToTimeString(currentMin),
                    endTimeStr = DateTimeUtils.minutesToTimeString(eveningStudyEnd),
                    title = "Evening Revision & Planning",
                    category = RoutineCategory.REVISION
                )
            )
            currentMin = eveningStudyEnd
        }

        // Wind down & Sleep
        if (currentMin < sleepMinutes) {
            plan.add(
                SmartPlanItem(
                    startTimeStr = DateTimeUtils.minutesToTimeString(currentMin),
                    endTimeStr = sleepTimeStr,
                    title = "Wind Down & Disconnect",
                    category = RoutineCategory.REST
                )
            )
        }

        plan.add(
            SmartPlanItem(
                startTimeStr = sleepTimeStr,
                endTimeStr = wakeTimeStr,
                title = "Sleep & Recovery",
                category = RoutineCategory.SLEEP
            )
        )

        return plan
    }
}
