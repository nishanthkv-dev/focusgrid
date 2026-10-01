package com.focusnow.app.util

import com.focusnow.app.data.model.SleepRecord
import com.focusnow.app.data.model.StudySession

data class SleepStudyCorrelation(
    val hasEnoughData: Boolean,
    val bestSleepRangeStr: String,
    val averageStudyMinutesInBestRange: Int,
    val insightMessage: String
)

object CorrelationCalculator {

    fun calculateCorrelation(
        sessions: List<StudySession>,
        sleepRecords: List<SleepRecord>
    ): SleepStudyCorrelation {
        if (sessions.isEmpty() || sleepRecords.isEmpty()) {
            return SleepStudyCorrelation(
                hasEnoughData = false,
                bestSleepRangeStr = "N/A",
                averageStudyMinutesInBestRange = 0,
                insightMessage = "Log at least 3 days of study and sleep to discover your personal peak productivity correlation."
            )
        }

        // Group study minutes by date
        val studyByDate = sessions.groupBy { it.dateStr }
            .mapValues { (_, daySessions) -> daySessions.sumOf { it.durationMinutes } }

        // Match with sleep records by date
        // Bucket sleep durations: < 6h, 6-7h, 7-8h, 8-9h, > 9h
        val buckets = mapOf(
            "< 6 hours" to mutableListOf<Int>(),
            "6–7 hours" to mutableListOf<Int>(),
            "7–8 hours" to mutableListOf<Int>(),
            "8–9 hours" to mutableListOf<Int>(),
            "> 9 hours" to mutableListOf<Int>()
        )

        var matchedDays = 0

        for (sleep in sleepRecords) {
            val studyMinutes = studyByDate[sleep.dateStr] ?: 0
            val sleepHours = sleep.durationMinutes / 60.0

            val bucketKey = when {
                sleepHours < 6.0 -> "< 6 hours"
                sleepHours < 7.0 -> "6–7 hours"
                sleepHours < 8.0 -> "7–8 hours"
                sleepHours < 9.0 -> "8–9 hours"
                else -> "> 9 hours"
            }

            buckets[bucketKey]?.add(studyMinutes)
            matchedDays++
        }

        if (matchedDays < 2) {
            return SleepStudyCorrelation(
                hasEnoughData = false,
                bestSleepRangeStr = "N/A",
                averageStudyMinutesInBestRange = 0,
                insightMessage = "Keep logging your daily study and sleep to unlock your personalized productivity insights."
            )
        }

        // Find bucket with highest average study minutes (only consider buckets with at least 1 entry)
        val validBuckets = buckets.filter { it.value.isNotEmpty() }
        val bestBucket = validBuckets.maxByOrNull { it.value.average() }

        if (bestBucket == null || bestBucket.value.average() <= 0) {
            return SleepStudyCorrelation(
                hasEnoughData = false,
                bestSleepRangeStr = "7–8 hours",
                averageStudyMinutesInBestRange = 0,
                insightMessage = "Log study sessions on your sleep recorded days to see your correlation."
            )
        }

        val bestAvgMinutes = bestBucket.value.average().toInt()
        val formattedStudy = DateTimeUtils.formatDuration(bestAvgMinutes)

        return SleepStudyCorrelation(
            hasEnoughData = true,
            bestSleepRangeStr = bestBucket.key,
            averageStudyMinutesInBestRange = bestAvgMinutes,
            insightMessage = "Your study time was highest (averaging $formattedStudy) on days when you slept ${bestBucket.key}."
        )
    }
}
