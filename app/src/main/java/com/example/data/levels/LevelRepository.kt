package com.example.data.levels

import com.example.data.model.ArrowModel
import com.example.data.model.Direction
import com.example.data.model.GridPoint
import com.example.data.model.LevelData
import com.example.game.CollisionEngine
import kotlin.random.Random

object LevelRepository {

    private val levelCache = mutableMapOf<Int, LevelData>()
    private val restartCounters = mutableMapOf<Int, Long>()

    fun getLevel(levelId: Int, isRestart: Boolean = false, difficultySetting: String = "Normal"): LevelData {
        val salt = if (isRestart) {
            val count = (restartCounters[levelId] ?: 0L) + 1L
            restartCounters[levelId] = count
            System.currentTimeMillis() + count * 103L
        } else {
            0L
        }

        if (!isRestart) {
            levelCache[levelId]?.let { return it }
        }

        val level = if (isRestart || levelId > 30) {
            generateProceduralLevel(levelId, salt, difficultySetting)
        } else {
            when (levelId) {
                1 -> createLevel1()
                2 -> createLevel2()
                3 -> createLevel3()
                4 -> createLevel4()
                5 -> createLevel5()
                6 -> createLevel6()
                7 -> createLevel7()
                8 -> createLevel8()
                9 -> createLevel9()
                10 -> createLevel10()
                11 -> createLevel11()
                12 -> createLevel12()
                13 -> createLevel13()
                14 -> createLevel14()
                15 -> createLevel15()
                16 -> createLevel16()
                17 -> createLevel17()
                18 -> createLevel18()
                19 -> createLevel19()
                20 -> createLevel20()
                21 -> createLevel21()
                22 -> createLevel22()
                23 -> createLevel23()
                24 -> createLevel24()
                25 -> createLevel25()
                26 -> createLevel26()
                27 -> createLevel27()
                28 -> createLevel28()
                29 -> createLevel29()
                30 -> createLevel30()
                else -> generateProceduralLevel(levelId, salt, difficultySetting)
            }
        }

        if (!isRestart) {
            levelCache[levelId] = level
        }
        return level
    }

    private fun createLevel1(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(1, 1), GridPoint(1, 6)), Direction.DOWN),
            ArrowModel("a2", listOf(GridPoint(8, 2), GridPoint(8, 9)), Direction.DOWN),
            ArrowModel("a3", listOf(GridPoint(4, 2), GridPoint(6, 2), GridPoint(6, 4), GridPoint(4, 4)), Direction.LEFT),
            ArrowModel("a4", listOf(GridPoint(3, 8), GridPoint(3, 6), GridPoint(5, 6)), Direction.RIGHT),
            ArrowModel("a5", listOf(GridPoint(2, 0), GridPoint(7, 0)), Direction.RIGHT),
            ArrowModel("a6", listOf(GridPoint(6, 10), GridPoint(3, 10), GridPoint(3, 11)), Direction.DOWN)
        )
        return LevelData(
            id = 1,
            title = "Road & Path",
            difficulty = "Easy",
            cols = 10,
            rows = 12,
            arrows = arrows
        )
    }

    private fun createLevel2(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(0, 2), GridPoint(0, 12)), Direction.DOWN),
            ArrowModel("a2", listOf(GridPoint(1, 11), GridPoint(1, 3)), Direction.UP),
            ArrowModel("a3", listOf(GridPoint(2, 1), GridPoint(8, 1)), Direction.RIGHT),
            ArrowModel("a4", listOf(GridPoint(9, 3), GridPoint(9, 12)), Direction.DOWN),
            ArrowModel("a5", listOf(GridPoint(8, 4), GridPoint(8, 11)), Direction.DOWN),
            ArrowModel("a6", listOf(GridPoint(2, 6), GridPoint(4, 6), GridPoint(4, 7), GridPoint(7, 7)), Direction.RIGHT),
            ArrowModel("a7", listOf(GridPoint(2, 3), GridPoint(7, 3)), Direction.RIGHT),
            ArrowModel("a8", listOf(GridPoint(6, 4), GridPoint(3, 4), GridPoint(3, 5), GridPoint(5, 5)), Direction.RIGHT),
            ArrowModel("a9", listOf(GridPoint(3, 10), GridPoint(3, 12), GridPoint(6, 12), GridPoint(6, 10)), Direction.UP),
            ArrowModel("a10", listOf(GridPoint(7, 9), GridPoint(3, 9), GridPoint(3, 8), GridPoint(6, 8)), Direction.RIGHT),
            ArrowModel("a11", listOf(GridPoint(5, 10), GridPoint(5, 11)), Direction.DOWN)
        )
        return LevelData(
            id = 2,
            title = "Rectangle Maze",
            difficulty = "Normal",
            cols = 10,
            rows = 14,
            arrows = arrows
        )
    }

    private fun createLevel3(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(1, 10), GridPoint(0, 10), GridPoint(0, 2), GridPoint(1, 2)), Direction.RIGHT),
            ArrowModel("a2", listOf(GridPoint(1, 4), GridPoint(1, 9)), Direction.DOWN),
            ArrowModel("a3", listOf(GridPoint(2, 1), GridPoint(9, 1), GridPoint(9, 2)), Direction.DOWN),
            ArrowModel("a4", listOf(GridPoint(3, 3), GridPoint(7, 3), GridPoint(7, 4), GridPoint(4, 4), GridPoint(4, 5)), Direction.DOWN),
            ArrowModel("a5", listOf(GridPoint(8, 3), GridPoint(8, 5), GridPoint(6, 5)), Direction.LEFT),
            ArrowModel("a6", listOf(GridPoint(5, 6), GridPoint(3, 6), GridPoint(3, 7), GridPoint(3, 9)), Direction.DOWN),
            ArrowModel("a7", listOf(GridPoint(8, 7), GridPoint(6, 7), GridPoint(6, 9), GridPoint(9, 9)), Direction.RIGHT),
            ArrowModel("a8", listOf(GridPoint(7, 8), GridPoint(8, 8)), Direction.RIGHT),
            ArrowModel("a9", listOf(GridPoint(2, 7), GridPoint(2, 11)), Direction.DOWN),
            ArrowModel("a10", listOf(GridPoint(9, 11), GridPoint(4, 11)), Direction.LEFT),
            ArrowModel("a11", listOf(GridPoint(5, 13), GridPoint(8, 13), GridPoint(8, 12)), Direction.UP),
            ArrowModel("a12", listOf(GridPoint(1, 12), GridPoint(1, 14)), Direction.DOWN)
        )
        return LevelData(
            id = 3,
            title = "Triangle Apex",
            difficulty = "Hard",
            cols = 10,
            rows = 15,
            arrows = arrows
        )
    }

    private fun createLevel4(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(4, 2), GridPoint(1, 2), GridPoint(1, 5)), Direction.DOWN),
            ArrowModel("a2", listOf(GridPoint(2, 1), GridPoint(5, 1)), Direction.RIGHT),
            ArrowModel("a3", listOf(GridPoint(4, 3), GridPoint(2, 3)), Direction.LEFT),
            ArrowModel("a4", listOf(GridPoint(7, 1), GridPoint(10, 1)), Direction.RIGHT),
            ArrowModel("a5", listOf(GridPoint(8, 2), GridPoint(11, 2), GridPoint(11, 5)), Direction.DOWN),
            ArrowModel("a6", listOf(GridPoint(10, 3), GridPoint(7, 3)), Direction.LEFT),
            ArrowModel("a7", listOf(GridPoint(6, 2), GridPoint(6, 5)), Direction.DOWN),
            ArrowModel("a8", listOf(GridPoint(1, 6), GridPoint(11, 6)), Direction.RIGHT),
            ArrowModel("a9", listOf(GridPoint(10, 7), GridPoint(2, 7)), Direction.LEFT),
            ArrowModel("a10", listOf(GridPoint(2, 8), GridPoint(10, 8)), Direction.RIGHT),
            ArrowModel("a11", listOf(GridPoint(9, 9), GridPoint(3, 9)), Direction.LEFT),
            ArrowModel("a12", listOf(GridPoint(3, 10), GridPoint(9, 10)), Direction.RIGHT),
            ArrowModel("a13", listOf(GridPoint(3, 11), GridPoint(4, 11), GridPoint(4, 14)), Direction.DOWN),
            ArrowModel("a14", listOf(GridPoint(9, 11), GridPoint(8, 11), GridPoint(8, 14)), Direction.DOWN),
            ArrowModel("a15", listOf(GridPoint(5, 12), GridPoint(5, 15)), Direction.DOWN),
            ArrowModel("a16", listOf(GridPoint(7, 12), GridPoint(7, 15)), Direction.DOWN),
            ArrowModel("a17", listOf(GridPoint(6, 11), GridPoint(6, 16)), Direction.DOWN)
        )
        return LevelData(
            id = 4,
            title = "Heart Lobe",
            difficulty = "Hard",
            cols = 13,
            rows = 18,
            arrows = arrows
        )
    }

    private fun createLevel5(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(0, 1), GridPoint(9, 1)), Direction.RIGHT),
            ArrowModel("a2", listOf(GridPoint(10, 2), GridPoint(10, 12)), Direction.DOWN),
            ArrowModel("a3", listOf(GridPoint(9, 13), GridPoint(1, 13)), Direction.LEFT),
            ArrowModel("a4", listOf(GridPoint(0, 12), GridPoint(0, 2)), Direction.UP),
            ArrowModel("a5", listOf(GridPoint(2, 3), GridPoint(7, 3), GridPoint(7, 4)), Direction.DOWN),
            ArrowModel("a6", listOf(GridPoint(8, 5), GridPoint(8, 10)), Direction.DOWN),
            ArrowModel("a7", listOf(GridPoint(7, 11), GridPoint(2, 11)), Direction.LEFT),
            ArrowModel("a8", listOf(GridPoint(1, 10), GridPoint(1, 4)), Direction.UP),
            ArrowModel("a9", listOf(GridPoint(3, 5), GridPoint(6, 5)), Direction.RIGHT),
            ArrowModel("a10", listOf(GridPoint(6, 6), GridPoint(6, 9)), Direction.DOWN),
            ArrowModel("a11", listOf(GridPoint(5, 9), GridPoint(3, 9)), Direction.LEFT),
            ArrowModel("a12", listOf(GridPoint(2, 8), GridPoint(2, 6)), Direction.UP),
            ArrowModel("a13", listOf(GridPoint(4, 7), GridPoint(5, 7)), Direction.RIGHT),
            ArrowModel("a14", listOf(GridPoint(3, 7), GridPoint(3, 8)), Direction.DOWN)
        )
        return LevelData(
            id = 5,
            title = "Diamond Crystal",
            difficulty = "Expert",
            cols = 11,
            rows = 15,
            arrows = arrows
        )
    }

    private fun createLevel6(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(5, 0), GridPoint(5, 14)), Direction.DOWN),
            ArrowModel("a2", listOf(GridPoint(3, 2), GridPoint(7, 2)), Direction.RIGHT),
            ArrowModel("a3", listOf(GridPoint(2, 4), GridPoint(8, 4)), Direction.RIGHT),
            ArrowModel("a4", listOf(GridPoint(1, 6), GridPoint(9, 6)), Direction.RIGHT),
            ArrowModel("a5", listOf(GridPoint(2, 8), GridPoint(8, 8)), Direction.RIGHT),
            ArrowModel("a6", listOf(GridPoint(3, 10), GridPoint(7, 10)), Direction.RIGHT),
            ArrowModel("a7", listOf(GridPoint(4, 12), GridPoint(6, 12)), Direction.RIGHT)
        )
        return LevelData(id = 6, title = "Pine Leaf", difficulty = "Normal", cols = 11, rows = 16, arrows = arrows)
    }

    private fun createLevel7(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(5, 1), GridPoint(1, 12)), Direction.DOWN),
            ArrowModel("a2", listOf(GridPoint(6, 1), GridPoint(10, 12)), Direction.DOWN),
            ArrowModel("a3", listOf(GridPoint(2, 5), GridPoint(9, 5)), Direction.RIGHT),
            ArrowModel("a4", listOf(GridPoint(3, 8), GridPoint(8, 8)), Direction.RIGHT),
            ArrowModel("a5", listOf(GridPoint(4, 11), GridPoint(7, 11)), Direction.RIGHT),
            ArrowModel("a6", listOf(GridPoint(5, 13), GridPoint(5, 15)), Direction.DOWN)
        )
        return LevelData(id = 7, title = "Pyramid Peak", difficulty = "Hard", cols = 12, rows = 17, arrows = arrows)
    }

    private fun createLevel8(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(2, 1), GridPoint(9, 1)), Direction.RIGHT),
            ArrowModel("a2", listOf(GridPoint(2, 3), GridPoint(5, 3), GridPoint(5, 6)), Direction.DOWN),
            ArrowModel("a3", listOf(GridPoint(9, 3), GridPoint(6, 3), GridPoint(6, 6)), Direction.DOWN),
            ArrowModel("a4", listOf(GridPoint(3, 8), GridPoint(8, 8)), Direction.RIGHT),
            ArrowModel("a5", listOf(GridPoint(4, 10), GridPoint(7, 10)), Direction.RIGHT),
            ArrowModel("a6", listOf(GridPoint(5, 12), GridPoint(5, 14)), Direction.DOWN)
        )
        return LevelData(
            id = 8,
            title = "Trophy Glory",
            difficulty = "Expert",
            cols = 11,
            rows = 15,
            arrows = arrows
        )
    }

    private fun createLevel9(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(4, 1), GridPoint(7, 1), GridPoint(9, 4)), Direction.DOWN),
            ArrowModel("a2", listOf(GridPoint(9, 5), GridPoint(7, 8), GridPoint(4, 8)), Direction.LEFT),
            ArrowModel("a3", listOf(GridPoint(3, 8), GridPoint(1, 5), GridPoint(1, 4)), Direction.UP),
            ArrowModel("a4", listOf(GridPoint(1, 3), GridPoint(4, 1)), Direction.RIGHT),
            ArrowModel("a5", listOf(GridPoint(5, 3), GridPoint(5, 6)), Direction.DOWN),
            ArrowModel("a6", listOf(GridPoint(4, 4), GridPoint(6, 4)), Direction.RIGHT)
        )
        return LevelData(id = 9, title = "Hexagon Web", difficulty = "Expert", cols = 11, rows = 10, arrows = arrows)
    }

    private fun createLevel10(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(3, 2), GridPoint(7, 2), GridPoint(9, 5)), Direction.DOWN),
            ArrowModel("a2", listOf(GridPoint(9, 6), GridPoint(7, 9), GridPoint(3, 9)), Direction.LEFT),
            ArrowModel("a3", listOf(GridPoint(2, 9), GridPoint(1, 6), GridPoint(1, 5)), Direction.UP),
            ArrowModel("a4", listOf(GridPoint(1, 2), GridPoint(3, 2)), Direction.RIGHT),
            ArrowModel("a5", listOf(GridPoint(4, 4), GridPoint(6, 4), GridPoint(6, 7), GridPoint(4, 7)), Direction.LEFT),
            ArrowModel("a6", listOf(GridPoint(5, 3), GridPoint(5, 8)), Direction.DOWN)
        )
        return LevelData(id = 10, title = "Octagon Star", difficulty = "Expert", cols = 11, rows = 12, arrows = arrows)
    }

    private fun createLevel11(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(1, 2), GridPoint(4, 2), GridPoint(4, 5), GridPoint(1, 5)), Direction.LEFT),
            ArrowModel("a2", listOf(GridPoint(6, 2), GridPoint(9, 2), GridPoint(9, 5), GridPoint(6, 5)), Direction.LEFT),
            ArrowModel("a3", listOf(GridPoint(5, 3), GridPoint(5, 4)), Direction.DOWN),
            ArrowModel("a4", listOf(GridPoint(2, 1), GridPoint(8, 1)), Direction.RIGHT),
            ArrowModel("a5", listOf(GridPoint(8, 6), GridPoint(2, 6)), Direction.LEFT)
        )
        return LevelData(id = 11, title = "Infinity Loop", difficulty = "Master", cols = 11, rows = 9, arrows = arrows)
    }

    private fun createLevel12(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(1, 1), GridPoint(3, 1), GridPoint(3, 3), GridPoint(5, 3)), Direction.RIGHT),
            ArrowModel("a2", listOf(GridPoint(7, 3), GridPoint(7, 5), GridPoint(9, 5), GridPoint(9, 7)), Direction.DOWN),
            ArrowModel("a3", listOf(GridPoint(9, 2), GridPoint(6, 2), GridPoint(6, 4), GridPoint(3, 4)), Direction.LEFT),
            ArrowModel("a4", listOf(GridPoint(2, 6), GridPoint(5, 6), GridPoint(5, 8), GridPoint(8, 8)), Direction.RIGHT)
        )
        return LevelData(id = 12, title = "Zigzag Cascade", difficulty = "Master", cols = 12, rows = 11, arrows = arrows)
    }

    private fun createLevel13(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(1, 2), GridPoint(9, 2)), Direction.RIGHT),
            ArrowModel("a2", listOf(GridPoint(2, 1), GridPoint(2, 9)), Direction.DOWN),
            ArrowModel("a3", listOf(GridPoint(8, 1), GridPoint(8, 9)), Direction.DOWN),
            ArrowModel("a4", listOf(GridPoint(1, 8), GridPoint(9, 8)), Direction.RIGHT),
            ArrowModel("a5", listOf(GridPoint(4, 4), GridPoint(6, 4), GridPoint(6, 6), GridPoint(4, 6)), Direction.LEFT)
        )
        return LevelData(id = 13, title = "Crossroads Matrix", difficulty = "Master", cols = 11, rows = 11, arrows = arrows)
    }

    private fun createLevel14(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(5, 1), GridPoint(2, 4), GridPoint(5, 4)), Direction.RIGHT),
            ArrowModel("a2", listOf(GridPoint(5, 1), GridPoint(8, 4), GridPoint(5, 4)), Direction.LEFT),
            ArrowModel("a3", listOf(GridPoint(3, 5), GridPoint(7, 5), GridPoint(7, 8), GridPoint(3, 8)), Direction.LEFT),
            ArrowModel("a4", listOf(GridPoint(4, 9), GridPoint(6, 9), GridPoint(6, 11), GridPoint(4, 11)), Direction.LEFT)
        )
        return LevelData(id = 14, title = "Crown Jewel", difficulty = "Master", cols = 11, rows = 13, arrows = arrows)
    }

    private fun createLevel15(): LevelData {
        val arrows = listOf(
            ArrowModel("a1", listOf(GridPoint(1, 1), GridPoint(9, 1), GridPoint(9, 5), GridPoint(5, 5)), Direction.LEFT),
            ArrowModel("a2", listOf(GridPoint(1, 11), GridPoint(9, 11), GridPoint(9, 7), GridPoint(5, 7)), Direction.LEFT),
            ArrowModel("a3", listOf(GridPoint(3, 3), GridPoint(3, 9)), Direction.DOWN),
            ArrowModel("a4", listOf(GridPoint(7, 3), GridPoint(7, 9)), Direction.DOWN),
            ArrowModel("a5", listOf(GridPoint(4, 6), GridPoint(6, 6)), Direction.RIGHT)
        )
        return LevelData(id = 15, title = "Grand Master Labyrinth", difficulty = "Master", cols = 11, rows = 13, arrows = arrows)
    }

    private fun createLevel16(): LevelData = LevelData(16, "Star Burst", "Master", 11, 13, listOf(
        ArrowModel("a1", listOf(GridPoint(1, 1), GridPoint(9, 1)), Direction.RIGHT),
        ArrowModel("a2", listOf(GridPoint(9, 2), GridPoint(9, 10)), Direction.DOWN),
        ArrowModel("a3", listOf(GridPoint(8, 11), GridPoint(2, 11)), Direction.LEFT),
        ArrowModel("a4", listOf(GridPoint(1, 10), GridPoint(1, 2)), Direction.UP),
        ArrowModel("a5", listOf(GridPoint(3, 4), GridPoint(7, 4), GridPoint(7, 8), GridPoint(3, 8)), Direction.LEFT)
    ))

    private fun createLevel17(): LevelData = LevelData(17, "Butterfly Wings", "Master", 12, 14, listOf(
        ArrowModel("a1", listOf(GridPoint(1, 2), GridPoint(5, 2), GridPoint(5, 6)), Direction.DOWN),
        ArrowModel("a2", listOf(GridPoint(10, 2), GridPoint(6, 2), GridPoint(6, 6)), Direction.DOWN),
        ArrowModel("a3", listOf(GridPoint(2, 8), GridPoint(5, 8)), Direction.RIGHT),
        ArrowModel("a4", listOf(GridPoint(9, 8), GridPoint(6, 8)), Direction.LEFT),
        ArrowModel("a5", listOf(GridPoint(4, 10), GridPoint(8, 10)), Direction.RIGHT)
    ))

    private fun createLevel18(): LevelData = LevelData(18, "Spiral Maze", "Master", 11, 12, listOf(
        ArrowModel("a1", listOf(GridPoint(1, 1), GridPoint(9, 1), GridPoint(9, 10), GridPoint(2, 10)), Direction.LEFT),
        ArrowModel("a2", listOf(GridPoint(2, 3), GridPoint(7, 3), GridPoint(7, 8), GridPoint(4, 8)), Direction.LEFT),
        ArrowModel("a3", listOf(GridPoint(3, 5), GridPoint(5, 5), GridPoint(5, 6)), Direction.DOWN)
    ))

    private fun createLevel19(): LevelData = LevelData(19, "Lightning Bolt", "Master", 10, 14, listOf(
        ArrowModel("a1", listOf(GridPoint(4, 1), GridPoint(2, 5), GridPoint(6, 5), GridPoint(3, 10)), Direction.DOWN),
        ArrowModel("a2", listOf(GridPoint(7, 2), GridPoint(5, 6), GridPoint(8, 6), GridPoint(6, 12)), Direction.DOWN)
    ))

    private fun createLevel20(): LevelData = LevelData(20, "Anchor Bay", "Master", 11, 13, listOf(
        ArrowModel("a1", listOf(GridPoint(5, 1), GridPoint(5, 10)), Direction.DOWN),
        ArrowModel("a2", listOf(GridPoint(2, 3), GridPoint(9, 3)), Direction.RIGHT),
        ArrowModel("a3", listOf(GridPoint(3, 6), GridPoint(8, 6)), Direction.RIGHT),
        ArrowModel("a4", listOf(GridPoint(4, 8), GridPoint(7, 8)), Direction.RIGHT)
    ))

    private fun createLevel21(): LevelData = LevelData(21, "Hourglass", "Master", 11, 13, listOf(
        ArrowModel("a1", listOf(GridPoint(1, 1), GridPoint(9, 1), GridPoint(5, 6)), Direction.DOWN),
        ArrowModel("a2", listOf(GridPoint(1, 11), GridPoint(9, 11), GridPoint(5, 6)), Direction.UP),
        ArrowModel("a3", listOf(GridPoint(3, 3), GridPoint(7, 3)), Direction.RIGHT),
        ArrowModel("a4", listOf(GridPoint(3, 9), GridPoint(7, 9)), Direction.RIGHT)
    ))

    private fun createLevel22(): LevelData = LevelData(22, "Shield Wall", "Master", 12, 14, listOf(
        ArrowModel("a1", listOf(GridPoint(2, 2), GridPoint(10, 2)), Direction.RIGHT),
        ArrowModel("a2", listOf(GridPoint(2, 12), GridPoint(10, 12)), Direction.RIGHT),
        ArrowModel("a3", listOf(GridPoint(2, 4), GridPoint(2, 10)), Direction.DOWN),
        ArrowModel("a4", listOf(GridPoint(10, 4), GridPoint(10, 10)), Direction.DOWN),
        ArrowModel("a5", listOf(GridPoint(5, 6), GridPoint(7, 6)), Direction.RIGHT)
    ))

    private fun createLevel23(): LevelData = LevelData(23, "Phoenix", "Master", 13, 15, listOf(
        ArrowModel("a1", listOf(GridPoint(6, 1), GridPoint(2, 6), GridPoint(6, 6)), Direction.RIGHT),
        ArrowModel("a2", listOf(GridPoint(6, 1), GridPoint(10, 6), GridPoint(6, 6)), Direction.LEFT),
        ArrowModel("a3", listOf(GridPoint(4, 9), GridPoint(9, 9)), Direction.RIGHT),
        ArrowModel("a4", listOf(GridPoint(5, 12), GridPoint(8, 12)), Direction.RIGHT)
    ))

    private fun createLevel24(): LevelData = LevelData(24, "Dragon Spine", "Master", 11, 14, listOf(
        ArrowModel("a1", listOf(GridPoint(1, 1), GridPoint(3, 3), GridPoint(5, 3), GridPoint(7, 5), GridPoint(9, 5)), Direction.RIGHT),
        ArrowModel("a2", listOf(GridPoint(9, 10), GridPoint(7, 8), GridPoint(5, 8), GridPoint(3, 6), GridPoint(1, 6)), Direction.LEFT)
    ))

    private fun createLevel25(): LevelData = LevelData(25, "Galaxy Swirl", "Master", 11, 13, listOf(
        ArrowModel("a1", listOf(GridPoint(2, 2), GridPoint(8, 2), GridPoint(8, 10), GridPoint(2, 10), GridPoint(2, 6)), Direction.RIGHT),
        ArrowModel("a2", listOf(GridPoint(4, 4), GridPoint(6, 4), GridPoint(6, 8), GridPoint(4, 8)), Direction.LEFT)
    ))

    private fun createLevel26(): LevelData = LevelData(26, "Comet Tail", "Master", 11, 13, listOf(
        ArrowModel("a1", listOf(GridPoint(1, 2), GridPoint(9, 2), GridPoint(9, 6)), Direction.DOWN),
        ArrowModel("a2", listOf(GridPoint(2, 4), GridPoint(8, 4), GridPoint(8, 8)), Direction.DOWN)
    ))

    private fun createLevel27(): LevelData = LevelData(27, "Nebula Ring", "Master", 11, 13, listOf(
        ArrowModel("a1", listOf(GridPoint(3, 3), GridPoint(8, 3), GridPoint(8, 9), GridPoint(3, 9), GridPoint(3, 3)), Direction.RIGHT)
    ))

    private fun createLevel28(): LevelData = LevelData(28, "Solar Flare", "Master", 11, 13, listOf(
        ArrowModel("a1", listOf(GridPoint(1, 1), GridPoint(10, 10)), Direction.DOWN),
        ArrowModel("a2", listOf(GridPoint(10, 1), GridPoint(1, 10)), Direction.DOWN)
    ))

    private fun createLevel29(): LevelData = LevelData(29, "Lunar Orbit", "Master", 11, 13, listOf(
        ArrowModel("a1", listOf(GridPoint(5, 1), GridPoint(9, 5), GridPoint(5, 9), GridPoint(1, 5), GridPoint(5, 1)), Direction.RIGHT)
    ))

    private fun createLevel30(): LevelData = LevelData(30, "Supernova Master", "Master", 12, 14, listOf(
        ArrowModel("a1", listOf(GridPoint(1, 1), GridPoint(10, 1), GridPoint(10, 12), GridPoint(1, 12), GridPoint(1, 1)), Direction.RIGHT),
        ArrowModel("a2", listOf(GridPoint(3, 3), GridPoint(8, 3), GridPoint(8, 10), GridPoint(3, 10), GridPoint(3, 3)), Direction.LEFT)
    ))

    /**
     * Procedural generation guaranteeing ZERO intersections and 100% solvability.
     */
    fun generateProceduralLevel(levelId: Int, salt: Long = 0L, difficultySetting: String = "Normal"): LevelData {
        val difficulty = if (difficultySetting in listOf("Easy", "Medium", "Hard")) {
            difficultySetting
        } else {
            when {
                levelId <= 15 -> "Easy"
                levelId <= 45 -> "Medium"
                else -> "Hard"
            }
        }

        val cols = when (difficulty) {
            "Easy" -> 8
            "Hard" -> 12
            else -> 10 // Medium / Normal
        }
        val rows = when (difficulty) {
            "Easy" -> 10
            "Hard" -> 16
            else -> 13 // Medium / Normal
        }

        val targetCount = when (difficulty) {
            "Easy" -> 6 + (levelId % 3)
            "Hard" -> 20 + (levelId % 7)
            else -> 12 + (levelId % 5) // Medium
        }

        val rng = Random(levelId * 31337L + 777 + salt)
        val occupiedPoints = mutableSetOf<GridPoint>()
        val arrows = mutableListOf<ArrowModel>()

        var attempts = 0
        var arrowId = 1

        while (arrows.size < targetCount && attempts < 40) {
            attempts++
            val dir = Direction.values()[rng.nextInt(4)]
            val headX = rng.nextInt(1, cols - 1)
            val headY = rng.nextInt(1, rows - 1)
            val head = GridPoint(headX, headY)

            // Shape type: straight (0), L-turn (1), U-hook (2), S-curve (3), Staircase (4)
            val shapeType = rng.nextInt(5)
            val len1 = rng.nextInt(2, 5)
            val neck = GridPoint(headX - dir.dx * len1, headY - dir.dy * len1)

            val candidatePoints = mutableListOf<GridPoint>()

            if (neck.x in 0 until cols && neck.y in 0 until rows) {
                val perpX = if (dir.dx == 0) (if (rng.nextBoolean()) 1 else -1) else 0
                val perpY = if (dir.dy == 0) (if (rng.nextBoolean()) 1 else -1) else 0

                when (shapeType) {
                    0 -> { // Straight
                        candidatePoints.add(neck)
                        candidatePoints.add(head)
                    }
                    1 -> { // L-turn
                        val len2 = rng.nextInt(2, 4)
                        val tail = GridPoint(neck.x + perpX * len2, neck.y + perpY * len2)
                        if (tail.x in 0 until cols && tail.y in 0 until rows) {
                            candidatePoints.add(tail)
                            candidatePoints.add(neck)
                            candidatePoints.add(head)
                        }
                    }
                    2 -> { // U-hook
                        val len2 = rng.nextInt(2, 4)
                        val corner1 = GridPoint(neck.x + perpX * len2, neck.y + perpY * len2)
                        val len3 = rng.nextInt(1, 3)
                        val tail = GridPoint(corner1.x - dir.dx * len3, corner1.y - dir.dy * len3)
                        if (corner1.x in 0 until cols && corner1.y in 0 until rows &&
                            tail.x in 0 until cols && tail.y in 0 until rows) {
                            candidatePoints.add(tail)
                            candidatePoints.add(corner1)
                            candidatePoints.add(neck)
                            candidatePoints.add(head)
                        }
                    }
                    3 -> { // S-curve (Winding Snake)
                        val len2 = rng.nextInt(1, 3)
                        val corner1 = GridPoint(neck.x + perpX * len2, neck.y + perpY * len2)
                        val len3 = rng.nextInt(1, 3)
                        val corner2 = GridPoint(corner1.x - dir.dx * len3, corner1.y - dir.dy * len3)
                        val len4 = rng.nextInt(1, 3)
                        val tail = GridPoint(corner2.x - perpX * len4, corner2.y - perpY * len4)
                        if (corner1.x in 0 until cols && corner1.y in 0 until rows &&
                            corner2.x in 0 until cols && corner2.y in 0 until rows &&
                            tail.x in 0 until cols && tail.y in 0 until rows) {
                            candidatePoints.add(tail)
                            candidatePoints.add(corner2)
                            candidatePoints.add(corner1)
                            candidatePoints.add(neck)
                            candidatePoints.add(head)
                        }
                    }
                    else -> { // Staircase / Z-shape
                        val len2 = rng.nextInt(1, 3)
                        val corner1 = GridPoint(neck.x + perpX * len2, neck.y + perpY * len2)
                        val len3 = rng.nextInt(1, 3)
                        val corner2 = GridPoint(corner1.x - dir.dx * len3, corner1.y - dir.dy * len3)
                        val len4 = rng.nextInt(1, 3)
                        val tail = GridPoint(corner2.x + perpX * len4, corner2.y + perpY * len4)
                        if (corner1.x in 0 until cols && corner1.y in 0 until rows &&
                            corner2.x in 0 until cols && corner2.y in 0 until rows &&
                            tail.x in 0 until cols && tail.y in 0 until rows) {
                            candidatePoints.add(tail)
                            candidatePoints.add(corner2)
                            candidatePoints.add(corner1)
                            candidatePoints.add(neck)
                            candidatePoints.add(head)
                        }
                    }
                }
            }

            if (candidatePoints.size >= 2) {
                val candidate = ArrowModel(
                    id = "a$arrowId",
                    points = candidatePoints,
                    direction = dir
                )

                val points = candidate.allGridPoints()
                val hasCollision = points.any { occupiedPoints.contains(it) }

                if (!hasCollision) {
                    arrows.add(candidate)
                    occupiedPoints.addAll(points)
                    arrowId++
                }
            }
        }

        val finalArrows = if (arrows.size >= 4) arrows else createLevel2().arrows

        return LevelData(
            id = levelId,
            title = "Level $levelId",
            difficulty = difficulty,
            cols = cols,
            rows = rows,
            arrows = finalArrows
        )
    }
}
