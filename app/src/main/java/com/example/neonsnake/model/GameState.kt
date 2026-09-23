package com.example.neonsnake.model

data class Point(
    val x: Int,
    val y: Int
)

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    fun isOpposite(other: Direction): Boolean {
        return this.dx + other.dx == 0 && this.dy + other.dy == 0
    }
}

enum class GameStatus {
    RUNNING,
    PAUSED,
    GAME_OVER
}

data class GameState(
    val snake: List<Point> = listOf(
        Point(12, 13),
        Point(11, 13),
        Point(10, 13)
    ),
    val food: Point = Point(16, 13),
    val direction: Direction = Direction.RIGHT,
    val score: Int = 0,
    val bestScore: Int = 0,
    val status: GameStatus = GameStatus.RUNNING,
    val currentSpeedMs: Long = 135L,
    val gridSize: Int = 24
)
