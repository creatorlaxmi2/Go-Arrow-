package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_records")
data class GameEntity(
    @PrimaryKey val levelId: Int,
    val completed: Boolean = false,
    val stars: Int = 0,
    val bestScore: Int = 0,
    val bestMoves: Int = 0,
    val bestTimeSeconds: Int = 0,
    val unlocked: Boolean = false
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val hintsCount: Int = 5,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val currentLevelId: Int = 1,
    val dailyStreak: Int = 0,
    val lastDailyDate: String = "",
    val difficulty: String = "Normal"
)
