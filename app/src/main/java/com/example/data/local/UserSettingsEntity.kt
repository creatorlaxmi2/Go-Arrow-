package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val isDarkMode: Boolean = false,
    val difficulty: String = "Normal",
    val lastCompletedDailyDate: String = ""
)
