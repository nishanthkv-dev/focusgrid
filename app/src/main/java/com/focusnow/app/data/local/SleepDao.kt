package com.focusnow.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusnow.app.data.model.SleepRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_records ORDER BY wakeTimeMillis DESC")
    fun getAllSleepRecords(): Flow<List<SleepRecord>>

    @Query("SELECT * FROM sleep_records ORDER BY wakeTimeMillis DESC")
    suspend fun getAllSleepRecordsOnce(): List<SleepRecord>

    @Query("SELECT * FROM sleep_records WHERE dateStr = :dateStr LIMIT 1")
    fun getSleepRecordForDate(dateStr: String): Flow<SleepRecord?>

    @Query("SELECT * FROM sleep_records WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getSleepRecordForDateOnce(dateStr: String): SleepRecord?

    @Query("SELECT * FROM sleep_records WHERE wakeTimeMillis >= :startMillis AND wakeTimeMillis <= :endMillis ORDER BY wakeTimeMillis ASC")
    fun getSleepRecordsBetween(startMillis: Long, endMillis: Long): Flow<List<SleepRecord>>

    @Query("SELECT * FROM sleep_records WHERE wakeTimeMillis >= :startMillis AND wakeTimeMillis <= :endMillis ORDER BY wakeTimeMillis ASC")
    suspend fun getSleepRecordsBetweenOnce(startMillis: Long, endMillis: Long): List<SleepRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepRecord(record: SleepRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSleepRecords(records: List<SleepRecord>)

    @Delete
    suspend fun deleteSleepRecord(record: SleepRecord)

    @Query("DELETE FROM sleep_records")
    suspend fun deleteAllSleepRecords()
}
