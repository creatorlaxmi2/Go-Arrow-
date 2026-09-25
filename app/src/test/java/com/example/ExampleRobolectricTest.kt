package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.levels.LevelRepository
import com.example.game.CollisionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Go Arrow Puzzle", appName)
    }

    @Test
    fun `verify no arrows intersect in any level`() {
        for (levelId in 1..5) {
            val level = LevelRepository.getLevel(levelId)
            val occupied = mutableSetOf<com.example.data.model.GridPoint>()

            for (arrow in level.arrows) {
                val arrowPoints = arrow.allGridPoints()
                for (pt in arrowPoints) {
                    val alreadyOccupied = occupied.contains(pt)
                    assertTrue("Level $levelId arrow ${arrow.id} overlaps at ($pt)!", !alreadyOccupied)
                    occupied.add(pt)
                }
            }
        }
    }

    @Test
    fun `verify all arrow segments are strictly orthogonal`() {
        for (levelId in 1..5) {
            val level = LevelRepository.getLevel(levelId)
            for (arrow in level.arrows) {
                assertTrue("Arrow ${arrow.id} in level $levelId must have at least 2 points", arrow.points.size >= 2)
                for (i in 0 until arrow.points.size - 1) {
                    val p1 = arrow.points[i]
                    val p2 = arrow.points[i + 1]
                    val isOrthogonal = (p1.x == p2.x && p1.y != p2.y) || (p1.y == p2.y && p1.x != p2.x)
                    assertTrue("Segment from $p1 to $p2 in arrow ${arrow.id} level $levelId must be orthogonal!", isOrthogonal)
                }
            }
        }
    }

    @Test
    fun `verify levels are solvable`() {
        for (levelId in 1..5) {
            val level = LevelRepository.getLevel(levelId)
            val solvable = CollisionEngine.isPuzzleSolvable(level.arrows, level.cols, level.rows)
            assertTrue("Level $levelId must be 100% solvable to completion!", solvable)
        }
    }
}
