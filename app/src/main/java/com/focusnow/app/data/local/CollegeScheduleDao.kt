package com.focusnow.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusnow.app.data.model.CollegeClass
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

@Dao
interface CollegeScheduleDao {
    @Query("SELECT * FROM college_classes ORDER BY startTimeStr ASC")
    fun getAllClasses(): Flow<List<CollegeClass>>

    @Query("SELECT * FROM college_classes ORDER BY startTimeStr ASC")
    suspend fun getAllClassesOnce(): List<CollegeClass>

    @Query("SELECT * FROM college_classes WHERE dayOfWeek = :dayOfWeek ORDER BY startTimeStr ASC")
    fun getClassesForDay(dayOfWeek: DayOfWeek): Flow<List<CollegeClass>>

    @Query("SELECT * FROM college_classes WHERE dayOfWeek = :dayOfWeek ORDER BY startTimeStr ASC")
    suspend fun getClassesForDayOnce(dayOfWeek: DayOfWeek): List<CollegeClass>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(collegeClass: CollegeClass): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllClasses(classes: List<CollegeClass>)

    @Update
    suspend fun updateClass(collegeClass: CollegeClass)

    @Delete
    suspend fun deleteClass(collegeClass: CollegeClass)

    @Query("DELETE FROM college_classes WHERE id = :id")
    suspend fun deleteClassById(id: Long)

    @Query("DELETE FROM college_classes")
    suspend fun deleteAllClasses()
}
