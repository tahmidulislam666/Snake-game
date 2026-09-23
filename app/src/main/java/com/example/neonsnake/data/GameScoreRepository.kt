package com.example.neonsnake.data

import com.example.neonsnake.data.local.GameScoreDao
import com.example.neonsnake.data.local.GameScoreEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameScoreRepository(private val gameScoreDao: GameScoreDao) {

    val highestScore: Flow<Int> = gameScoreDao.getHighestScoreFlow().map { it ?: 0 }

    suspend fun recordSessionScore(score: Int) {
        if (score > 0) {
            gameScoreDao.insertScore(GameScoreEntity(score = score))
        }
    }

    suspend fun seedInitialBest(initialBest: Int) {
        val current = gameScoreDao.getHighestScore() ?: 0
        if (initialBest > current) {
            gameScoreDao.insertScore(GameScoreEntity(score = initialBest))
        }
    }
}
