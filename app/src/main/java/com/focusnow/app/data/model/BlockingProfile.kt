package com.focusnow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocking_profiles")
data class BlockingProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileName: String, // e.g. "GATE Mode", "Coding Mode", "Deep Focus"
    val description: String = "",
    val blockedPackageNamesJson: String = "[]", // List<String> as JSON
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
