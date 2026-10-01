package com.focusnow.app.data.local

import androidx.room.TypeConverter
import com.focusnow.app.data.model.DeadlinePriority
import com.focusnow.app.data.model.DeadlineType
import com.focusnow.app.data.model.RepeatType
import com.focusnow.app.data.model.RoutineCategory
import com.focusnow.app.data.model.SessionType
import com.focusnow.app.data.model.StreakType
import com.focusnow.app.data.model.TaskCategory
import com.focusnow.app.data.model.TaskPriority
import java.time.DayOfWeek

class Converters {
    @TypeConverter
    fun fromTaskCategory(value: TaskCategory): String = value.name

    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory = try {
        TaskCategory.valueOf(value)
    } catch (e: Exception) {
        TaskCategory.OTHER
    }

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = try {
        TaskPriority.valueOf(value)
    } catch (e: Exception) {
        TaskPriority.MEDIUM
    }

    @TypeConverter
    fun fromRepeatType(value: RepeatType): String = value.name

    @TypeConverter
    fun toRepeatType(value: String): RepeatType = try {
        RepeatType.valueOf(value)
    } catch (e: Exception) {
        RepeatType.NONE
    }

    @TypeConverter
    fun fromSessionType(value: SessionType): String = value.name

    @TypeConverter
    fun toSessionType(value: String): SessionType = try {
        SessionType.valueOf(value)
    } catch (e: Exception) {
        SessionType.FOCUS
    }

    @TypeConverter
    fun fromDayOfWeek(value: DayOfWeek): String = value.name

    @TypeConverter
    fun toDayOfWeek(value: String): DayOfWeek = try {
        DayOfWeek.valueOf(value)
    } catch (e: Exception) {
        DayOfWeek.MONDAY
    }

    @TypeConverter
    fun fromRoutineCategory(value: RoutineCategory): String = value.name

    @TypeConverter
    fun toRoutineCategory(value: String): RoutineCategory = try {
        RoutineCategory.valueOf(value)
    } catch (e: Exception) {
        RoutineCategory.OTHER
    }

    @TypeConverter
    fun fromDeadlineType(value: DeadlineType): String = value.name

    @TypeConverter
    fun toDeadlineType(value: String): DeadlineType = try {
        DeadlineType.valueOf(value)
    } catch (e: Exception) {
        DeadlineType.OTHER
    }

    @TypeConverter
    fun fromDeadlinePriority(value: DeadlinePriority): String = value.name

    @TypeConverter
    fun toDeadlinePriority(value: String): DeadlinePriority = try {
        DeadlinePriority.valueOf(value)
    } catch (e: Exception) {
        DeadlinePriority.MEDIUM
    }

    @TypeConverter
    fun fromStreakType(value: StreakType): String = value.name

    @TypeConverter
    fun toStreakType(value: String): StreakType = try {
        StreakType.valueOf(value)
    } catch (e: Exception) {
        StreakType.OVERALL
    }
}
