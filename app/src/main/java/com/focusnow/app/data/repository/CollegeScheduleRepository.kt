package com.focusnow.app.data.repository

import com.focusnow.app.data.local.CollegeScheduleDao
import com.focusnow.app.data.model.CollegeClass
import com.focusnow.app.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate

class CollegeScheduleRepository(private val collegeScheduleDao: CollegeScheduleDao) {
    val allClasses: Flow<List<CollegeClass>> = collegeScheduleDao.getAllClasses()

    fun getClassesForDayOfWeek(dayOfWeek: DayOfWeek): Flow<List<CollegeClass>> =
        collegeScheduleDao.getClassesForDay(dayOfWeek)

    suspend fun getClassesForDate(date: LocalDate): List<CollegeClass> {
        val dayOfWeek = date.dayOfWeek
        val classes = collegeScheduleDao.getClassesForDayOnce(dayOfWeek)
        val dateMillis = DateTimeUtils.getStartOfDayMillis(date)

        return classes.filter { cls ->
            !cls.isRecurring || dateMillis <= cls.semesterEndDateMillis
        }.sortedBy { it.startTimeStr }
    }

    suspend fun insertClass(collegeClass: CollegeClass): Long =
        collegeScheduleDao.insertClass(collegeClass)

    suspend fun updateClass(collegeClass: CollegeClass) =
        collegeScheduleDao.updateClass(collegeClass)

    suspend fun deleteClass(collegeClass: CollegeClass) =
        collegeScheduleDao.deleteClass(collegeClass)

    suspend fun deleteClassById(id: Long) =
        collegeScheduleDao.deleteClassById(id)
}
