package com.example.neonsnake.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.neonsnake.data.GameScoreRepository
import com.example.neonsnake.data.ScorePreferences
import com.example.neonsnake.data.local.SnakeDatabase
import com.example.neonsnake.model.Direction
import com.example.neonsnake.model.GameState
import com.example.neonsnake.model.GameStatus
import com.example.neonsnake.model.Point
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class SnakeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SnakeDatabase.getDatabase(application)
    private val repository = GameScoreRepository(database.gameScoreDao())
    private val scorePreferences = ScorePreferences(application)

    private val _gameState = MutableStateFlow(
        GameState(bestScore = scorePreferences.getBestScore())
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var pendingDirection: Direction = Direction.RIGHT

    init {
        // Seed Room database with any existing high score
        viewModelScope.launch {
            repository.seedInitialBest(scorePreferences.getBestScore())
        }

        // Reactively observe highest score from Room database
        viewModelScope.launch {
            repository.highestScore.collectLatest { roomBestScore ->
                val highest = maxOf(roomBestScore, scorePreferences.getBestScore())
                _gameState.update { it.copy(bestScore = highest) }
                scorePreferences.saveBestScore(highest)
            }
        }

        startGame()
    }

    fun startGame() {
        gameLoopJob?.cancel()
        val initialSnake = listOf(
            Point(12, 13),
            Point(11, 13),
            Point(10, 13)
        )
        val initialFood = generateFood(initialSnake, 24)
        pendingDirection = Direction.RIGHT

        _gameState.update { current ->
            GameState(
                snake = initialSnake,
                food = initialFood,
                direction = Direction.RIGHT,
                score = 0,
                bestScore = current.bestScore,
                status = GameStatus.RUNNING,
                currentSpeedMs = calculateSpeed(0),
                gridSize = 24
            )
        }

        startLoop()
    }

    private fun startLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                val currentSpeed = _gameState.value.currentSpeedMs
                delay(currentSpeed)

                if (_gameState.value.status == GameStatus.RUNNING) {
                    tick()
                }
            }
        }
    }

    fun changeDirection(newDirection: Direction) {
        val currentDir = _gameState.value.direction
        if (!newDirection.isOpposite(currentDir) && newDirection != currentDir) {
            pendingDirection = newDirection
        }
    }

    fun togglePause() {
        _gameState.update { current ->
            when (current.status) {
                GameStatus.RUNNING -> current.copy(status = GameStatus.PAUSED)
                GameStatus.PAUSED -> current.copy(status = GameStatus.RUNNING)
                GameStatus.GAME_OVER -> current
            }
        }
    }

    private fun tick() {
        _gameState.update { current ->
            if (current.status != GameStatus.RUNNING) return@update current

            val activeDirection = pendingDirection
            val head = current.snake.first()
            val newHead = Point(
                x = head.x + activeDirection.dx,
                y = head.y + activeDirection.dy
            )

            val hitWall = newHead.x < 0 || newHead.x >= current.gridSize ||
                    newHead.y < 0 || newHead.y >= current.gridSize
            val hitSelf = current.snake.any { it.x == newHead.x && it.y == newHead.y }

            if (hitWall || hitSelf) {
                val finalScore = current.score
                val newBest = maxOf(current.bestScore, finalScore)
                viewModelScope.launch {
                    repository.recordSessionScore(finalScore)
                }
                scorePreferences.saveBestScore(newBest)
                current.copy(
                    status = GameStatus.GAME_OVER,
                    bestScore = newBest,
                    direction = activeDirection
                )
            } else {
                val ateFood = (newHead.x == current.food.x && newHead.y == current.food.y)
                if (ateFood) {
                    val newScore = current.score + 1
                    val newBest = maxOf(current.bestScore, newScore)
                    viewModelScope.launch {
                        repository.recordSessionScore(newScore)
                    }
                    scorePreferences.saveBestScore(newBest)
                    val newSnake = listOf(newHead) + current.snake
                    val nextFood = generateFood(newSnake, current.gridSize)
                    val newSpeed = calculateSpeed(newScore)

                    current.copy(
                        snake = newSnake,
                        food = nextFood,
                        direction = activeDirection,
                        score = newScore,
                        bestScore = newBest,
                        currentSpeedMs = newSpeed
                    )
                } else {
                    val newSnake = listOf(newHead) + current.snake.dropLast(1)
                    current.copy(
                        snake = newSnake,
                        direction = activeDirection
                    )
                }
            }
        }
    }

    private fun generateFood(snake: List<Point>, gridSize: Int): Point {
        val occupied = snake.toSet()
        val availablePoints = mutableListOf<Point>()
        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                val p = Point(x, y)
                if (p !in occupied) {
                    availablePoints.add(p)
                }
            }
        }
        return if (availablePoints.isNotEmpty()) {
            availablePoints[Random.nextInt(availablePoints.size)]
        } else {
            Point(0, 0)
        }
    }

    private fun calculateSpeed(score: Int): Long {
        val baseSpeed = 135L
        val minSpeed = 62L
        return maxOf(minSpeed, baseSpeed - (score / 5) * 10L)
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
