package com.example

import com.example.data.levels.LevelRepository
import com.example.game.CollisionEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testNoArrowsIntersectInHandcraftedLevels() {
    for (lvl in 1..5) {
      val level = LevelRepository.getLevel(lvl)
      val occupied = mutableMapOf<com.example.data.model.GridPoint, String>()

      for (arrow in level.arrows) {
        val points = arrow.allGridPoints()
        for (pt in points) {
          val existing = occupied[pt]
          assertNull(
            "Level $lvl arrow ${arrow.id} overlaps with $existing at point ($pt)",
            existing
          )
          occupied[pt] = arrow.id
        }
      }
    }
  }

  @Test
  fun testRestartLevelsSolvability() {
    for (lvl in listOf(1, 2, 5, 10, 31)) {
      for (attempt in 1..5) {
        val restarted = LevelRepository.getLevel(lvl, isRestart = true)
        assertNotNull(restarted)
        assertTrue("Restarted level $lvl attempt $attempt must have arrows", restarted.arrows.isNotEmpty())
        val solvable = CollisionEngine.isPuzzleSolvable(restarted.arrows, restarted.cols, restarted.rows)
        assertTrue("Restarted level $lvl attempt $attempt must be solvable", solvable)
      }
    }
  }
}
