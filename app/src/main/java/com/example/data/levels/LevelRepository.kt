package com.example.data.levels

import kotlin.random.Random

enum class ArrowDirection(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    fun opposite(): ArrowDirection = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }
}

data class GridPoint(val col: Int, val row: Int)

data class ArrowPath(
    val id: Int,
    val points: List<GridPoint>, // Ordered from tail (first) to head (last)
    val direction: ArrowDirection,
    val colorIndex: Int = 0
) {
    val head: GridPoint get() = points.last()
}

data class LevelData(
    val id: Int,
    val title: String,
    val difficulty: String,
    val cols: Int,
    val rows: Int,
    val arrows: List<ArrowPath>,
    val isDailyChallenge: Boolean = false
)

object LevelRepository {
    const val TOTAL_LEVELS = 150

    fun getDifficultyForLevel(levelId: Int, difficultySetting: String = "Normal"): String {
        return if (difficultySetting in listOf("Easy", "Medium", "Hard", "Very Hard", "Expert")) {
            difficultySetting
        } else {
            when {
                levelId <= 15 -> "Easy"
                levelId <= 45 -> "Medium"
                levelId <= 75 -> "Hard"
                levelId <= 120 -> "Very Hard"
                else -> "Expert"
            }
        }
    }

    fun getLevel(levelId: Int, difficultySetting: String = "Normal"): LevelData {
        return generateProceduralLevel(levelId, difficultySetting, isDaily = false)
    }

    fun getDailyChallengeLevel(dateSeed: Int, difficultySetting: String = "Normal"): LevelData {
        return generateProceduralLevel(
            levelId = 1000 + (dateSeed % 9000),
            difficultySetting = if (difficultySetting == "Normal") "Hard" else difficultySetting,
            isDaily = true
        )
    }

    fun generateProceduralLevel(
        levelId: Int,
        difficultySetting: String = "Normal",
        isDaily: Boolean = false
    ): LevelData {
        val difficulty = getDifficultyForLevel(levelId, difficultySetting)
        val stageProgress = ((levelId - 1).coerceAtLeast(0))

        val (cols, rows, targetCount, maxLen) = when (difficulty) {
            "Easy" -> {
                val c = (6 + (stageProgress / 6).coerceAtMost(2))
                val r = (7 + (stageProgress / 5).coerceAtMost(2))
                Quad(c, r, (c * r * 55) / 100, 4)
            }
            "Medium" -> {
                val sub = (levelId - 16).coerceAtLeast(0)
                val c = (8 + (sub / 12).coerceAtMost(2))
                val r = (9 + (sub / 10).coerceAtMost(2))
                Quad(c, r, (c * r * 62) / 100, 5)
            }
            "Hard" -> {
                val sub = (levelId - 46).coerceAtLeast(0)
                val c = (9 + (sub / 12).coerceAtMost(2))
                val r = (11 + (sub / 12).coerceAtMost(2))
                Quad(c, r, (c * r * 66) / 100, 5)
            }
            "Very Hard" -> {
                val sub = (levelId - 76).coerceAtLeast(0)
                val c = (10 + (sub / 20).coerceAtMost(2))
                val r = (12 + (sub / 18).coerceAtMost(2))
                Quad(c, r, (c * r * 70) / 100, 6)
            }
            else -> {
                val sub = (levelId - 121).coerceAtLeast(0)
                val c = (11 + (sub / 15).coerceAtMost(1))
                val r = (14 + (sub / 15).coerceAtMost(1))
                Quad(c, r, (c * r * 72) / 100, 6)
            }
        }

        val rng = Random(levelId.toLong() * 1103515245L + difficulty.hashCode().toLong())
        val occupied = Array(cols) { BooleanArray(rows) }
        val placedArrows = mutableListOf<ArrowPath>()

        // Build solvable board by placing arrows in reverse elimination order:
        // At the moment an arrow is added to `placedArrows`, its exit ray to the edge
        // must be completely free of already-placed arrows!
        // That guarantees that if the player removes them in reverse order (last placed -> first removed),
        // every single arrow has a 100% clear exit path!
        var nextId = 1
        val allDirections = ArrowDirection.entries

        val candidateCells = mutableListOf<GridPoint>()
        for (c in 0 until cols) {
            for (r in 0 until rows) {
                candidateCells.add(GridPoint(c, r))
            }
        }

        var passes = 0
        while (placedArrows.size < targetCount && passes < 4) {
            passes++
            candidateCells.shuffle(rng)

            for (headCandidate in candidateCells) {
                if (placedArrows.size >= targetCount) break
                if (occupied[headCandidate.col][headCandidate.row]) continue

                // Find valid directions where the ray from headCandidate to board edge is completely unoccupied
                val shuffledDirs = allDirections.shuffled(rng)
                var chosenDir: ArrowDirection? = null
                for (dir in shuffledDirs) {
                    if (isRayClear(headCandidate, dir, cols, rows, occupied)) {
                        chosenDir = dir
                        break
                    }
                }
                if (chosenDir == null) continue

                // Grow a winding tail backwards from headCandidate
                val body = mutableListOf(headCandidate)
                occupied[headCandidate.col][headCandidate.row] = true

                val desiredLen = rng.nextInt(2, maxLen + 1)
                var current = headCandidate
                var prevStepDir = chosenDir.opposite()

                for (step in 1 until desiredLen) {
                    // Prefer continuing or turning 90 degrees so the arrow looks sleek and winding
                    val neighborDirs = allDirections.shuffled(rng).sortedByDescending { d ->
                        if (d == prevStepDir) 2 else if (d != prevStepDir.opposite()) 1 else 0
                    }
                    var nextTail: GridPoint? = null
                    var chosenTailDir: ArrowDirection? = null
                    for (nd in neighborDirs) {
                        // Do not step into the forward exit ray of headCandidate!
                        val nc = current.col + nd.dx
                        val nr = current.row + nd.dy
                        if (nc in 0 until cols && nr in 0 until rows && !occupied[nc][nr]) {
                            val candidatePt = GridPoint(nc, nr)
                            if (!isOnRay(headCandidate, chosenDir, candidatePt)) {
                                nextTail = candidatePt
                                chosenTailDir = nd
                                break
                            }
                        }
                    }
                    if (nextTail != null && chosenTailDir != null) {
                        body.add(0, nextTail) // prepend so `headCandidate` stays last
                        occupied[nextTail.col][nextTail.row] = true
                        current = nextTail
                        prevStepDir = chosenTailDir
                    } else {
                        break
                    }
                }

                // Make sure the exit direction matches the final segment direction (or points clearly outward)
                val finalDirection = if (body.size >= 2) {
                    val secondLast = body[body.size - 2]
                    val segmentDir = allDirections.firstOrNull {
                        secondLast.col + it.dx == headCandidate.col && secondLast.row + it.dy == headCandidate.row
                    }
                    if (segmentDir != null && isRayClear(headCandidate, segmentDir, cols, rows, occupied)) {
                        segmentDir
                    } else {
                        chosenDir
                    }
                } else {
                    chosenDir
                }

                placedArrows.add(
                    ArrowPath(
                        id = nextId++,
                        points = body,
                        direction = finalDirection,
                        colorIndex = (nextId + rng.nextInt(6)) % 6
                    )
                )
            }
        }

        val title = if (isDaily) "Daily Challenge" else "Level $levelId"
        return LevelData(
            id = levelId,
            title = title,
            difficulty = difficulty,
            cols = cols,
            rows = rows,
            arrows = placedArrows,
            isDailyChallenge = isDaily
        )
    }

    private fun isRayClear(
        origin: GridPoint,
        dir: ArrowDirection,
        cols: Int,
        rows: Int,
        occupied: Array<BooleanArray>
    ): Boolean {
        var c = origin.col + dir.dx
        var r = origin.row + dir.dy
        while (c in 0 until cols && r in 0 until rows) {
            if (occupied[c][r]) return false
            c += dir.dx
            r += dir.dy
        }
        return true
    }

    private fun isOnRay(origin: GridPoint, dir: ArrowDirection, pt: GridPoint): Boolean {
        var c = origin.col + dir.dx
        var r = origin.row + dir.dy
        while (c in 0..30 && r in 0..30 && c >= -1 && r >= -1) {
            if (c == pt.col && r == pt.row) return true
            c += dir.dx
            r += dir.dy
        }
        return false
    }

    private data class Quad(val cols: Int, val rows: Int, val targetCount: Int, val maxLen: Int)
}
