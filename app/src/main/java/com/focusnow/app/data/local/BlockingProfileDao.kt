package com.focusnow.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusnow.app.data.model.BlockingProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockingProfileDao {
    @Query("SELECT * FROM blocking_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<BlockingProfile>>

    @Query("SELECT * FROM blocking_profiles ORDER BY id ASC")
    suspend fun getAllProfilesOnce(): List<BlockingProfile>

    @Query("SELECT * FROM blocking_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): BlockingProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BlockingProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProfiles(profiles: List<BlockingProfile>)

    @Update
    suspend fun updateProfile(profile: BlockingProfile)

    @Delete
    suspend fun deleteProfile(profile: BlockingProfile)

    @Query("DELETE FROM blocking_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)

    @Query("DELETE FROM blocking_profiles")
    suspend fun deleteAllProfiles()
}
