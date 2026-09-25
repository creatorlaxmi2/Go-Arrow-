package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GameEntity::class, UserSettingsEntity::class],
    version = 3,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getInstance(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: try {
                    buildDatabase(context)
                } catch (_: Throwable) {
                    try {
                        context.applicationContext.deleteDatabase("go_arrow_puzzle.db")
                    } catch (_: Throwable) {}
                    buildDatabase(context)
                }.also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): GameDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                GameDatabase::class.java,
                "go_arrow_puzzle.db"
            )
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}
