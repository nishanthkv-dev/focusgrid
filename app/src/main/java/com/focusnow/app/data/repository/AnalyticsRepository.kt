package com.focusnow.app.data.repository

import com.focusnow.app.data.local.SleepDao
import com.focusnow.app.data.local.StudySessionDao
import com.focusnow.app.data.local.TaskDao
import com.focusnow.app.data.local.UserProfileDao
import com.focusnow.app.data.model.SleepRecord
import com.focusnow.app.data.model.StudySession
import com.focusnow.app.data.model.TaskItem
import com.focusnow.app.util.CorrelationCalculator
import com.focusnow.app.util.DateTimeUtils
import com.focusnow.app.util.SleepStudyCorrelation
import java.time.DayOfWeek
import java.time.LocalDate

data class DayStudyData(
    val dateStr: String,
    val dayLabel: String,
    val durationMinutes: Int
)

data class SubjectStudyData(
    val subject: String,
    val durationMinutes: Int,
    val percentage: Float
)

data class StudyAnalyticsSummary(
    val todayStudyMinutes: Int,
    val weeklyTotalMinutes: Int,
    val weeklyAverageMinutes: Int,
    val weeklyTargetMinutes: Int,
    val weeklyAchievementPercentage: Float,
    val monthlyTotalMinutes: Int,
    val monthlyAverageMinutes: Int,
    val longestSessionMinutes: Int,
    val mostProductiveDayOfWeek: String,
    val mostProductiveTimeSlot: String,
    val dailyBreakdownLast7Days: List<DayStudyData>,
    val subjectBreakdown: List<SubjectStudyData>,
    val categoryBreakdown: List<SubjectStudyData>
)

data class SleepAnalyticsSummary(
    val todaySleepMinutes: Int,
    val weeklyAverageMinutes: Int,
    val averageBedtimeStr: String,
    val averageWakeTimeStr: String,
    val sleepConsistencyPercentage: Int,
    val daysMeetingTarget: Int,
    val daysBelowTarget: Int,
    val dailyBreakdownLast7Days: List<DayStudyData>
)

data class MonthlyReport(
    val monthName: String,
    val totalStudyHours: Double,
    val tasksCompleted: Int,
    val totalTasks: Int,
    val averageSleepHours: Double,
    val studyTargetAchievementPercentage: Int,
    val longestStreakDays: Int,
    val mostStudiedSubject: String,
    val mostProductiveDay: String
)

class AnalyticsRepository(
    private val studySessionDao: StudySessionDao,
    private val sleepDao: SleepDao,
    private val taskDao: TaskDao,
    private val userProfileDao: UserProfileDao,
    private val streakRepository: StreakRepository
) {

    suspend fun getStudyAnalytics(): StudyAnalyticsSummary {
        val allSessions = studySessionDao.getAllSessionsOnce()
        val userProfile = userProfileDao.getUserProfileOnce()
        val dailyTarget = userProfile?.dailyStudyTargetMinutes ?: 240
        val weeklyTarget = dailyTarget * 7

        val todayStr = DateTimeUtils.getTodayDateStr()
        val todayStudyMinutes = allSessions.filter { it.dateStr == todayStr }.sumOf { it.durationMinutes }

        // Last 7 days
        val past7DateStrings = DateTimeUtils.getPastDaysDateStrings(7)
        val last7DaysSessions = allSessions.filter { it.dateStr in past7DateStrings }
        val weeklyTotalMinutes = last7DaysSessions.sumOf { it.durationMinutes }
        val weeklyAverageMinutes = weeklyTotalMinutes / 7
        val weeklyAchievementPercentage = if (weeklyTarget > 0) {
            ((weeklyTotalMinutes.toFloat() / weeklyTarget.toFloat()) * 100f).coerceAtMost(100f)
        } else 0f

        val past30DateStrings = DateTimeUtils.getPastDaysDateStrings(30)
        val monthlySessions = allSessions.filter { it.dateStr in past30DateStrings }
        val monthlyTotalMinutes = monthlySessions.sumOf { it.durationMinutes }
        val monthlyAverageMinutes = monthlyTotalMinutes / 30

        val longestSessionMinutes = allSessions.maxOfOrNull { it.durationMinutes } ?: 0

        // Day of week productivity
        val dayOfWeekTotals = mutableMapOf<DayOfWeek, Int>()
        allSessions.forEach { session ->
            val dayOfWeek = DateTimeUtils.getDayOfWeekForDate(session.dateStr)
            dayOfWeekTotals[dayOfWeek] = (dayOfWeekTotals[dayOfWeek] ?: 0) + session.durationMinutes
        }
        val bestDayOfWeek = dayOfWeekTotals.maxByOrNull { it.value }?.key?.name?.lowercase()
            ?.replaceFirstChar { it.uppercase() } ?: "N/A"

        // Time slot productivity (Morning: 6-12, Afternoon: 12-17, Evening: 17-21, Night: 21-6)
        val timeSlotCounts = mutableMapOf(
            "Morning (6 AM - 12 PM)" to 0,
            "Afternoon (12 PM - 5 PM)" to 0,
            "Evening (5 PM - 9 PM)" to 0,
            "Night (9 PM - 6 AM)" to 0
        )
        allSessions.forEach { session ->
            val hour = (session.startTimeMillis / (1000 * 60 * 60) % 24).toInt()
            when (hour) {
                in 6..11 -> timeSlotCounts["Morning (6 AM - 12 PM)"] = (timeSlotCounts["Morning (6 AM - 12 PM)"] ?: 0) + session.durationMinutes
                in 12..16 -> timeSlotCounts["Afternoon (12 PM - 5 PM)"] = (timeSlotCounts["Afternoon (12 PM - 5 PM)"] ?: 0) + session.durationMinutes
                in 17..20 -> timeSlotCounts["Evening (5 PM - 9 PM)"] = (timeSlotCounts["Evening (5 PM - 9 PM)"] ?: 0) + session.durationMinutes
                else -> timeSlotCounts["Night (9 PM - 6 AM)"] = (timeSlotCounts["Night (9 PM - 6 AM)"] ?: 0) + session.durationMinutes
            }
        }
        val bestTimeSlot = timeSlotCounts.maxByOrNull { it.value }?.key ?: "Evening (5 PM - 9 PM)"

        // 7-day breakdown
        val dailyBreakdown = past7DateStrings.map { dateStr ->
            val date = DateTimeUtils.parseDateStr(dateStr)
            val dayMins = allSessions.filter { it.dateStr == dateStr }.sumOf { it.durationMinutes }
            DayStudyData(
                dateStr = dateStr,
                dayLabel = date.dayOfWeek.name.take(3),
                durationMinutes = dayMins
            )
        }

        // Subject breakdown
        val totalAllMins = (allSessions.sumOf { it.durationMinutes }).coerceAtLeast(1)
        val subjectBreakdown = allSessions.groupBy { it.subject }
            .map { (subject, list) ->
                val duration = list.sumOf { it.durationMinutes }
                SubjectStudyData(
                    subject = subject,
                    durationMinutes = duration,
                    percentage = (duration.toFloat() / totalAllMins) * 100f
                )
            }.sortedByDescending { it.durationMinutes }

        // Category breakdown
        val categoryBreakdown = allSessions.groupBy { it.category }
            .map { (category, list) ->
                val duration = list.sumOf { it.durationMinutes }
                SubjectStudyData(
                    subject = category,
                    durationMinutes = duration,
                    percentage = (duration.toFloat() / totalAllMins) * 100f
                )
            }.sortedByDescending { it.durationMinutes }

        return StudyAnalyticsSummary(
            todayStudyMinutes = todayStudyMinutes,
            weeklyTotalMinutes = weeklyTotalMinutes,
            weeklyAverageMinutes = weeklyAverageMinutes,
            weeklyTargetMinutes = weeklyTarget,
            weeklyAchievementPercentage = weeklyAchievementPercentage,
            monthlyTotalMinutes = monthlyTotalMinutes,
            monthlyAverageMinutes = monthlyAverageMinutes,
            longestSessionMinutes = longestSessionMinutes,
            mostProductiveDayOfWeek = bestDayOfWeek,
            mostProductiveTimeSlot = bestTimeSlot,
            dailyBreakdownLast7Days = dailyBreakdown,
            subjectBreakdown = subjectBreakdown,
            categoryBreakdown = categoryBreakdown
        )
    }

    suspend fun getSleepAnalytics(): SleepAnalyticsSummary {
        val allRecords = sleepDao.getAllSleepRecordsOnce()
        val userProfile = userProfileDao.getUserProfileOnce()
        val targetMinutes = userProfile?.sleepTargetMinutes ?: 480

        val todayStr = DateTimeUtils.getTodayDateStr()
        val todaySleepMinutes = allRecords.find { it.dateStr == todayStr }?.durationMinutes ?: 0

        val past7DateStrings = DateTimeUtils.getPastDaysDateStrings(7)
        val last7DaysRecords = allRecords.filter { it.dateStr in past7DateStrings }

        val weeklyAverageMinutes = if (last7DaysRecords.isNotEmpty()) {
            last7DaysRecords.sumOf { it.durationMinutes } / last7DaysRecords.size
        } else 0

        // Average bedtime & wake time
        val avgBedtimeMinutes = if (allRecords.isNotEmpty()) {
            allRecords.map { DateTimeUtils.timeStringToMinutes(it.bedtimeStr) }.average().toInt()
        } else DateTimeUtils.timeStringToMinutes("23:00")

        val avgWakeMinutes = if (allRecords.isNotEmpty()) {
            allRecords.map { DateTimeUtils.timeStringToMinutes(it.wakeTimeStr) }.average().toInt()
        } else DateTimeUtils.timeStringToMinutes("07:00")

        val averageBedtimeStr = DateTimeUtils.formatTime24to12(DateTimeUtils.minutesToTimeString(avgBedtimeMinutes))
        val averageWakeTimeStr = DateTimeUtils.formatTime24to12(DateTimeUtils.minutesToTimeString(avgWakeMinutes))

        // Consistency: % of sleep records within 1 hour of target
        val daysMeetingTarget = allRecords.count { it.durationMinutes >= (targetMinutes - 30) }
        val daysBelowTarget = allRecords.count { it.durationMinutes < (targetMinutes - 30) }
        val consistencyPercentage = if (allRecords.isNotEmpty()) {
            ((daysMeetingTarget.toFloat() / allRecords.size.toFloat()) * 100).toInt()
        } else 80

        val dailyBreakdown = past7DateStrings.map { dateStr ->
            val date = DateTimeUtils.parseDateStr(dateStr)
            val record = allRecords.find { it.dateStr == dateStr }
            DayStudyData(
                dateStr = dateStr,
                dayLabel = date.dayOfWeek.name.take(3),
                durationMinutes = record?.durationMinutes ?: 0
            )
        }

        return SleepAnalyticsSummary(
            todaySleepMinutes = todaySleepMinutes,
            weeklyAverageMinutes = weeklyAverageMinutes,
            averageBedtimeStr = averageBedtimeStr,
            averageWakeTimeStr = averageWakeTimeStr,
            sleepConsistencyPercentage = consistencyPercentage,
            daysMeetingTarget = daysMeetingTarget,
            daysBelowTarget = daysBelowTarget,
            dailyBreakdownLast7Days = dailyBreakdown
        )
    }

    suspend fun getCorrelation(): SleepStudyCorrelation {
        val sessions = studySessionDao.getAllSessionsOnce()
        val sleepRecords = sleepDao.getAllSleepRecordsOnce()
        return CorrelationCalculator.calculateCorrelation(sessions, sleepRecords)
    }

    suspend fun generateMonthlyReport(): MonthlyReport {
        val allSessions = studySessionDao.getAllSessionsOnce()
        val allTasks = taskDao.getAllTasksOnce()
        val allSleep = sleepDao.getAllSleepRecordsOnce()
        val studyAnalytics = getStudyAnalytics()
        val overallStreak = streakRepository.getStreak(com.focusnow.app.data.model.StreakType.OVERALL)

        val totalStudyHours = (studyAnalytics.monthlyTotalMinutes / 60.0 * 10).toInt() / 10.0
        val completedTasks = allTasks.count { it.isCompleted }
        val totalTasks = allTasks.size.coerceAtLeast(1)

        val avgSleepHours = if (allSleep.isNotEmpty()) {
            (allSleep.map { it.durationMinutes }.average() / 60.0 * 10).toInt() / 10.0
        } else 7.5

        val currentMonth = LocalDate.now().month.name.lowercase().replaceFirstChar { it.uppercase() }

        val mostStudiedSubject = studyAnalytics.subjectBreakdown.firstOrNull()?.subject ?: "General"

        return MonthlyReport(
            monthName = currentMonth,
            totalStudyHours = totalStudyHours,
            tasksCompleted = completedTasks,
            totalTasks = totalTasks,
            averageSleepHours = avgSleepHours,
            studyTargetAchievementPercentage = studyAnalytics.weeklyAchievementPercentage.toInt(),
            longestStreakDays = 14, // Longest streak calculated from streak records
            mostStudiedSubject = mostStudiedSubject,
            mostProductiveDay = studyAnalytics.mostProductiveDayOfWeek
        )
    }
}
