package com.example.data.model

enum class Direction(val dx: Int, val dy: Int, val label: String) {
    UP(0, -1, "UP"),
    DOWN(0, 1, "DOWN"),
    LEFT(-1, 0, "LEFT"),
    RIGHT(1, 0, "RIGHT");

    companion object {
        fun fromDelta(dx: Int, dy: Int): Direction {
            return when {
                dx > 0 -> RIGHT
                dx < 0 -> LEFT
                dy > 0 -> DOWN
                else -> UP
            }
        }
    }
}
