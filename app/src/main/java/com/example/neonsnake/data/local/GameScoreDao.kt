package com.example.neonsnake.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameScoreDao {
    @Query("SELECT MAX(score) FROM game_scores")
    fun getHighestScoreFlow(): Flow<Int?>

    @Query("SELECT MAX(score) FROM game_scores")
    suspend fun getHighestScore(): Int?

    @Query("SELECT * FROM game_scores ORDER BY timestamp DESC LIMIT 10")
    fun getRecentScoresFlow(): Flow<List<GameScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: GameScoreEntity)
}
