package com.example.neonsnake.data

import android.content.Context
import android.content.SharedPreferences

class ScorePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBestScore(): Int {
        return prefs.getInt(KEY_BEST_SCORE, 0)
    }

    fun saveBestScore(score: Int) {
        val currentBest = getBestScore()
        if (score > currentBest) {
            prefs.edit().putInt(KEY_BEST_SCORE, score).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "neon_snake_prefs"
        private const val KEY_BEST_SCORE = "neon_snake_best"
    }
}
