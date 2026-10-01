package com.focusnow.app.data.repository

import com.focusnow.app.data.local.DailyRoutineDao
import com.focusnow.app.data.model.DailyScheduleRoutine
import com.focusnow.app.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow

data class ScheduleConflict(
    val titleA: String,
    val timeA: String,
    val titleB: String,
    val timeB: String,
    val description: String
)

class DailyRoutineRepository(private val dailyRoutineDao: DailyRoutineDao) {
    val allRoutines: Flow<List<DailyScheduleRoutine>> = dailyRoutineDao.getAllRoutines()

    suspend fun getAllRoutinesOnce(): List<DailyScheduleRoutine> =
        dailyRoutineDao.getAllRoutinesOnce()

    suspend fun insertRoutine(routine: DailyScheduleRoutine): Long =
        dailyRoutineDao.insertRoutine(routine)

    suspend fun updateRoutine(routine: DailyScheduleRoutine) =
        dailyRoutineDao.updateRoutine(routine)

    suspend fun deleteRoutine(routine: DailyScheduleRoutine) =
        dailyRoutineDao.deleteRoutine(routine)

    suspend fun deleteRoutineById(id: Long) =
        dailyRoutineDao.deleteRoutineById(id)

    fun detectConflicts(routines: List<DailyScheduleRoutine>): List<ScheduleConflict> {
        val conflicts = mutableListOf<ScheduleConflict>()
        val sorted = routines.filter { it.isEnabled }.sortedBy { DateTimeUtils.timeStringToMinutes(it.startTimeStr) }

        for (i in 0 until sorted.size - 1) {
            val current = sorted[i]
            val next = sorted[i + 1]

            val currentEndMin = DateTimeUtils.timeStringToMinutes(current.endTimeStr)
            val nextStartMin = DateTimeUtils.timeStringToMinutes(next.startTimeStr)

            if (currentEndMin > nextStartMin) {
                conflicts.add(
                    ScheduleConflict(
                        titleA = current.title,
                        timeA = "${current.startTimeStr} - ${current.endTimeStr}",
                        titleB = next.title,
                        timeB = "${next.startTimeStr} - ${next.endTimeStr}",
                        description = "Overlap of ${currentEndMin - nextStartMin} minutes between '${current.title}' and '${next.title}'"
                    )
                )
            }
        }
        return conflicts
    }
}
