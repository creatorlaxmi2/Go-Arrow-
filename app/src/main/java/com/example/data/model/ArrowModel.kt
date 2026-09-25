package com.example.data.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class GridPoint(
    val x: Int,
    val y: Int
) {
    fun plus(dx: Int, dy: Int): GridPoint = GridPoint(x + dx, y + dy)
}

enum class ArrowState {
    IDLE,
    SELECTED,
    BLOCKED,
    HINTED,
    FLYING,
    REMOVED
}

data class ArrowModel(
    val id: String,
    val points: List<GridPoint>,
    val direction: Direction = if (points.size >= 2) {
        val last = points.last()
        val prev = points[points.size - 2]
        Direction.fromDelta(last.x - prev.x, last.y - prev.y)
    } else {
        Direction.RIGHT
    },
    val state: ArrowState = ArrowState.IDLE,
    val flyProgress: Float = 0f,
    val shakeOffset: Float = 0f,
    val spawnProgress: Float = 1f
) {
    val head: GridPoint get() = points.last()
    val tail: GridPoint get() = points.first()

    /**
     * Returns all grid line segments making up this arrow's path.
     */
    fun segments(): List<Pair<GridPoint, GridPoint>> {
        val list = mutableListOf<Pair<GridPoint, GridPoint>>()
        for (i in 0 until points.size - 1) {
            list.add(Pair(points[i], points[i + 1]))
        }
        return list
    }

    /**
     * Returns all integer grid coordinates occupied by this arrow.
     */
    fun allGridPoints(): Set<GridPoint> {
        val set = mutableSetOf<GridPoint>()
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val minX = min(p1.x, p2.x)
            val maxX = max(p1.x, p2.x)
            val minY = min(p1.y, p2.y)
            val maxY = max(p1.y, p2.y)
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    set.add(GridPoint(x, y))
                }
            }
        }
        return set
    }

    /**
     * Total length of the arrow along its orthogonal segments in grid units.
     */
    fun totalLength(): Float {
        var len = 0f
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            len += abs(p2.x - p1.x) + abs(p2.y - p1.y)
        }
        return len
    }
}
