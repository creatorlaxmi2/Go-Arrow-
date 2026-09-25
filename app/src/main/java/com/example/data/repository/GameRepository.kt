package com.example.data.repository

import android.util.Log
import com.example.data.local.GameDao
import com.example.data.local.GameEntity
import com.example.data.local.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GameRepository(private val dao: GameDao) {

    val levelRecordsFlow: Flow<List<GameEntity>> = dao.getAllLevelRecords()
        .catch { e ->
            Log.e("GameRepository", "Error reading level records flow", e)
            emit(listOf(GameEntity(levelId = 1, unlocked = true)))
        }

    val userSettingsFlow: Flow<UserSettingsEntity> = dao.getUserSettingsFlow()
        .catch { e ->
            Log.e("GameRepository", "Error reading user settings flow", e)
            emit(UserSettingsEntity())
        }
        .map { it ?: UserSettingsEntity() }

    suspend fun initializeDefaults() {
        try {
            val currentSettings = dao.getUserSettings()
            if (currentSettings == null) {
                dao.updateUserSettings(UserSettingsEntity())
            }
            // Ensure Level 1 is unlocked
            val level1 = dao.getLevelRecord(1)
            if (level1 == null) {
                dao.insertOrUpdateRecord(GameEntity(levelId = 1, unlocked = true))
            }
        } catch (e: Throwable) {
            Log.e("GameRepository", "Error in initializeDefaults", e)
        }
    }

    suspend fun saveLevelCompletion(
        levelId: Int,
        stars: Int,
        score: Int,
        moves: Int,
        timeSeconds: Int
    ) {
        try {
            val existing = dao.getLevelRecord(levelId)
            val bestStars = maxOf(stars, existing?.stars ?: 0)
            val bestScore = maxOf(score, existing?.bestScore ?: 0)
            val bestMoves = if (existing?.bestMoves != null && existing.bestMoves > 0) {
                minOf(moves, existing.bestMoves)
            } else moves
            val bestTime = if (existing?.bestTimeSeconds != null && existing.bestTimeSeconds > 0) {
                minOf(timeSeconds, existing.bestTimeSeconds)
            } else timeSeconds

            dao.insertOrUpdateRecord(
                GameEntity(
                    levelId = levelId,
                    completed = true,
                    stars = bestStars,
                    bestScore = bestScore,
                    bestMoves = bestMoves,
                    bestTimeSeconds = bestTime,
                    unlocked = true
                )
            )

            // Unlock next level!
            val nextLevelId = levelId + 1
            val nextRecord = dao.getLevelRecord(nextLevelId)
            if (nextRecord == null || !nextRecord.unlocked) {
                dao.insertOrUpdateRecord(
                    GameEntity(
                        levelId = nextLevelId,
                        unlocked = true
                    )
                )
            }

            // Update current level
            val settings = dao.getUserSettings() ?: UserSettingsEntity()
            dao.updateUserSettings(settings.copy(currentLevelId = nextLevelId))
        } catch (e: Throwable) {
            Log.e("GameRepository", "Error in saveLevelCompletion", e)
        }
    }

    suspend fun consumeHint(): Boolean {
        return try {
            val settings = dao.getUserSettings() ?: UserSettingsEntity()
            if (settings.hintsCount > 0) {
                dao.updateUserSettings(settings.copy(hintsCount = settings.hintsCount - 1))
                true
            } else {
                false
            }
        } catch (e: Throwable) {
            Log.e("GameRepository", "Error in consumeHint", e)
            false
        }
    }

    suspend fun addHints(amount: Int) {
        try {
            val settings = dao.getUserSettings() ?: UserSettingsEntity()
            dao.updateUserSettings(settings.copy(hintsCount = settings.hintsCount + amount))
        } catch (e: Throwable) {
            Log.e("GameRepository", "Error in addHints", e)
        }
    }

    suspend fun updateSettings(
        sound: Boolean? = null,
        haptics: Boolean? = null,
        darkMode: Boolean? = null,
        difficulty: String? = null
    ) {
        try {
            val settings = dao.getUserSettings() ?: UserSettingsEntity()
            dao.updateUserSettings(
                settings.copy(
                    soundEnabled = sound ?: settings.soundEnabled,
                    hapticsEnabled = haptics ?: settings.hapticsEnabled,
                    darkModeEnabled = darkMode ?: settings.darkModeEnabled,
                    difficulty = difficulty ?: settings.difficulty
                )
            )
        } catch (e: Throwable) {
            Log.e("GameRepository", "Error in updateSettings", e)
        }
    }

    suspend fun resetProgress() {
        try {
            dao.resetAllProgress()
            dao.insertOrUpdateRecord(GameEntity(levelId = 1, unlocked = true))
            dao.updateUserSettings(UserSettingsEntity(hintsCount = 5, currentLevelId = 1))
        } catch (e: Throwable) {
            Log.e("GameRepository", "Error in resetProgress", e)
        }
    }
}
