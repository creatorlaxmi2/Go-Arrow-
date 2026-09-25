package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM level_records ORDER BY levelId ASC")
    fun getAllLevelRecords(): Flow<List<GameEntity>>

    @Query("SELECT * FROM level_records WHERE levelId = :levelId")
    suspend fun getLevelRecord(levelId: Int): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: GameEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(records: List<GameEntity>)

    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettingsFlow(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getUserSettings(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUserSettings(settings: UserSettingsEntity)

    @Query("DELETE FROM level_records")
    suspend fun resetAllProgress()
}
