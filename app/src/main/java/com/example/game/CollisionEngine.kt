package com.example.game

import com.example.data.model.ArrowModel
import com.example.data.model.Direction
import com.example.data.model.GridPoint
import kotlin.math.max
import kotlin.math.min

object CollisionEngine {

    data class Segment(
        val x1: Int,
        val y1: Int,
        val x2: Int,
        val y2: Int
    ) {
        val isHorizontal: Boolean get() = y1 == y2
        val isVertical: Boolean get() = x1 == x2

        val minX: Int get() = min(x1, x2)
        val maxX: Int get() = max(x1, x2)
        val minY: Int get() = min(y1, y2)
        val maxY: Int get() = max(y1, y2)

        fun containsPoint(px: Int, py: Int): Boolean {
            return if (isHorizontal) {
                py == y1 && px in minX..maxX
            } else if (isVertical) {
                px == x1 && py in minY..maxY
            } else false
        }
    }

    private fun ArrowModel.toSegments(): List<Segment> {
        val list = mutableListOf<Segment>()
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            list.add(Segment(p1.x, p1.y, p2.x, p2.y))
        }
        return list
    }

    /**
     * Checks if moving Arrow in its pointing direction collides with any other active arrow.
     */
    fun isArrowClear(
        arrow: ArrowModel,
        otherActiveArrows: List<ArrowModel>,
        cols: Int,
        rows: Int
    ): Boolean {
        return findBlockingArrow(arrow, otherActiveArrows, cols, rows) == null
    }

    /**
     * Returns the first arrow that blocks the given arrow, or null if path is completely clear.
     * Following Section 23:
     * Check everything directly in front of the arrow until it reaches the board boundary.
     */
    fun findBlockingArrow(
        arrow: ArrowModel,
        otherActiveArrows: List<ArrowModel>,
        cols: Int,
        rows: Int
    ): ArrowModel? {
        val head = arrow.head
        val dir = arrow.direction

        // Other active arrows with their segments
        val others = otherActiveArrows.filter { it.id != arrow.id }
            .map { it to it.toSegments() }

        // Step forward from head until out of bounds
        var cx = head.x + dir.dx
        var cy = head.y + dir.dy

        while (cx in 0 until cols && cy in 0 until rows) {
            for ((otherArrow, segments) in others) {
                for (seg in segments) {
                    if (seg.containsPoint(cx, cy)) {
                        return otherArrow
                    }
                }
            }
            cx += dir.dx
            cy += dir.dy
        }

        return null
    }

    /**
     * Finds all currently unblocked (free) arrows that the player can safely tap.
     */
    fun findSafeArrows(
        activeArrows: List<ArrowModel>,
        cols: Int,
        rows: Int
    ): List<ArrowModel> {
        return activeArrows.filter { arrow ->
            isArrowClear(arrow, activeArrows, cols, rows)
        }
    }

    /**
     * Verifies if a puzzle state can be solved to completion.
     */
    fun isPuzzleSolvable(
        initialArrows: List<ArrowModel>,
        cols: Int,
        rows: Int
    ): Boolean {
        val remaining = initialArrows.toMutableList()
        while (remaining.isNotEmpty()) {
            val safe = findSafeArrows(remaining, cols, rows)
            if (safe.isEmpty()) {
                return false
            }
            remaining.remove(safe.first())
        }
        return true
    }
}
