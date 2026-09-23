package com.example.neonsnake.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GameScoreEntity::class], version = 1, exportSchema = false)
abstract class SnakeDatabase : RoomDatabase() {
    abstract fun gameScoreDao(): GameScoreDao

    companion object {
        @Volatile
        private var INSTANCE: SnakeDatabase? = null

        fun getDatabase(context: Context): SnakeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SnakeDatabase::class.java,
                    "snake_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
