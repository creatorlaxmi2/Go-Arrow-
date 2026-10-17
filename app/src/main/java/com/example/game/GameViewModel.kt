package com.example.game

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.levels.ArrowPath
import com.example.data.levels.GridPoint
import com.example.data.levels.LevelData
import com.example.data.levels.LevelRepository
import com.example.data.local.GameDao
import com.example.data.local.GameEntity
import com.example.data.local.UserSettingsEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class GameUiState(
    val currentLevelId: Int = 1,
    val levelData: LevelData = LevelRepository.getLevel(1),
    val remainingArrows: List<ArrowPath> = emptyList(),
    val exitingArrowIds: Set<Int> = emptySet(),
    val blockedArrowId: Int? = null,
    val hintArrowId: Int? = null,
    val lives: Int = 3,
    val maxLives: Int = 3,
    val moves: Int = 0,
    val isLevelComplete: Boolean = false,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val earnedStars: Int = 3,
    val unlockedLevels: Set<Int> = setOf(1),
    val levelStars: Map<Int, Int> = emptyMap(),
    val totalStars: Int = 0,
    val completedLevelsCount: Int = 0,
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val isDarkMode: Boolean = false,
    val difficulty: String = "Normal",
    val canUndo: Boolean = false
)

class GameViewModel(
    private val gameDao: GameDao?,
    private val appContext: Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        run {
            val initialLevel = LevelRepository.getLevel(1, "Normal")
            GameUiState(
                currentLevelId = 1,
                levelData = initialLevel,
                remainingArrows = initialLevel.arrows
            )
        }
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val removedHistory = ArrayDeque<ArrowPath>()

    init {
        observePersistence()
    }

    private fun observePersistence() {
        if (gameDao == null) return

        viewModelScope.launch {
            val existing = gameDao.getLevelProgress(1)
            if (existing == null) {
                gameDao.upsertProgress(
                    GameEntity(levelId = 1, isUnlocked = true, isCompleted = false, stars = 0, bestMoves = 0)
                )
            }
        }

        viewModelScope.launch {
            gameDao.observeAllProgress().collect { list ->
                val unlocked = mutableSetOf(1)
                val starsMap = mutableMapOf<Int, Int>()
                var completedCount = 0

                for (item in list) {
                    if (item.isUnlocked || item.isCompleted) {
                        unlocked.add(item.levelId)
                    }
                    if (item.isCompleted) {
                        completedCount++
                        starsMap[item.levelId] = item.stars
                        if (item.levelId < LevelRepository.TOTAL_LEVELS) {
                            unlocked.add(item.levelId + 1)
                        }
                    }
                }

                val highestUnlocked = unlocked.maxOrNull() ?: 1
                _uiState.update { state ->
                    state.copy(
                        unlockedLevels = unlocked,
                        levelStars = starsMap,
                        totalStars = starsMap.values.sum(),
                        completedLevelsCount = completedCount,
                        currentLevelId = if (state.moves == 0 && !state.levelData.isDailyChallenge) highestUnlocked else state.currentLevelId
                    )
                }
            }
        }

        viewModelScope.launch {
            gameDao.observeSettings().collect { settings ->
                if (settings != null) {
                    _uiState.update { state ->
                        state.copy(
                            isSoundEnabled = settings.isSoundEnabled,
                            isHapticsEnabled = settings.isHapticsEnabled,
                            isDarkMode = settings.isDarkMode,
                            difficulty = settings.difficulty
                        )
                    }
                }
            }
        }
    }

    fun loadLevel(levelId: Int) {
        val targetId = levelId.coerceIn(1, LevelRepository.TOTAL_LEVELS)
        val diff = _uiState.value.difficulty
        val level = LevelRepository.getLevel(targetId, diff)
        removedHistory.clear()
        _uiState.update {
            it.copy(
                currentLevelId = targetId,
                levelData = level,
                remainingArrows = level.arrows,
                exitingArrowIds = emptySet(),
                blockedArrowId = null,
                hintArrowId = null,
                lives = 3,
                moves = 0,
                isLevelComplete = false,
                isGameOver = false,
                isPaused = false,
                earnedStars = 3,
                canUndo = false
            )
        }
    }

    fun startNextAvailableLevel() {
        val highest = _uiState.value.unlockedLevels.maxOrNull() ?: 1
        loadLevel(highest)
    }

    fun startDailyChallenge() {
        val cal = Calendar.getInstance()
        val seed = cal.get(Calendar.YEAR) * 10000 + (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH)
        val level = LevelRepository.getDailyChallengeLevel(seed, _uiState.value.difficulty)
        removedHistory.clear()
        _uiState.update {
            it.copy(
                currentLevelId = level.id,
                levelData = level,
                remainingArrows = level.arrows,
                exitingArrowIds = emptySet(),
                blockedArrowId = null,
                hintArrowId = null,
                lives = 3,
                moves = 0,
                isLevelComplete = false,
                isGameOver = false,
                isPaused = false,
                earnedStars = 3,
                canUndo = false
            )
        }
    }

    fun retryLevel() {
        val current = _uiState.value.levelData
        removedHistory.clear()
        _uiState.update {
            it.copy(
                remainingArrows = current.arrows,
                exitingArrowIds = emptySet(),
                blockedArrowId = null,
                hintArrowId = null,
                lives = 3,
                moves = 0,
                isLevelComplete = false,
                isGameOver = false,
                isPaused = false,
                earnedStars = 3,
                canUndo = false
            )
        }
    }

    fun nextLevel() {
        val nextId = if (_uiState.value.levelData.isDailyChallenge) {
            _uiState.value.unlockedLevels.maxOrNull() ?: 1
        } else {
            (_uiState.value.currentLevelId + 1).coerceAtMost(LevelRepository.TOTAL_LEVELS)
        }
        loadLevel(nextId)
    }

    fun onArrowTapped(arrowId: Int) {
        val state = _uiState.value
        if (state.isLevelComplete || state.isGameOver || state.isPaused) return
        if (arrowId in state.exitingArrowIds) return

        val arrow = state.remainingArrows.firstOrNull { it.id == arrowId } ?: return
        val occupiedCells = HashSet<GridPoint>(state.remainingArrows.size * 4)
        for (other in state.remainingArrows) {
            if (other.id != arrowId && other.id !in state.exitingArrowIds) {
                occupiedCells.addAll(other.points)
            }
        }

        val clear = isPathClear(arrow, state.levelData.cols, state.levelData.rows, occupiedCells)
        if (clear) {
            playFeedback(success = true)
            removedHistory.addLast(arrow)
            _uiState.update {
                it.copy(
                    exitingArrowIds = it.exitingArrowIds + arrowId,
                    blockedArrowId = null,
                    hintArrowId = if (it.hintArrowId == arrowId) null else it.hintArrowId,
                    moves = it.moves + 1,
                    canUndo = true
                )
            }

            viewModelScope.launch {
                delay(140)
                var completedNow = false
                var finalStars = 3
                var clearedLevelId = 1
                var finalMoves = 0
                var isDaily = false

                _uiState.update { current ->
                    val updatedRemaining = current.remainingArrows.filterNot { it.id == arrowId }
                    val updatedExiting = current.exitingArrowIds - arrowId
                    completedNow = updatedRemaining.isEmpty()
                    finalStars = current.lives.coerceIn(1, 3)
                    clearedLevelId = current.currentLevelId
                    finalMoves = current.moves
                    isDaily = current.levelData.isDailyChallenge

                    val newUnlocked = if (completedNow && !isDaily && clearedLevelId < LevelRepository.TOTAL_LEVELS) {
                        current.unlockedLevels + (clearedLevelId + 1)
                    } else {
                        current.unlockedLevels
                    }
                    val prevStars = current.levelStars[clearedLevelId] ?: 0
                    val newLevelStars = if (completedNow && !isDaily) {
                        current.levelStars + (clearedLevelId to maxOf(prevStars, finalStars))
                    } else {
                        current.levelStars
                    }

                    current.copy(
                        remainingArrows = updatedRemaining,
                        exitingArrowIds = updatedExiting,
                        isLevelComplete = completedNow,
                        earnedStars = finalStars,
                        unlockedLevels = newUnlocked,
                        levelStars = newLevelStars,
                        totalStars = newLevelStars.values.sum(),
                        completedLevelsCount = newLevelStars.size
                    )
                }

                if (completedNow && !isDaily && gameDao != null) {
                    val existing = gameDao.getLevelProgress(clearedLevelId)
                    val bestStars = maxOf(existing?.stars ?: 0, finalStars)
                    val bestMoves = if (existing == null || existing.bestMoves == 0) finalMoves else minOf(existing.bestMoves, finalMoves)
                    gameDao.upsertProgress(
                        GameEntity(
                            levelId = clearedLevelId,
                            isUnlocked = true,
                            isCompleted = true,
                            stars = bestStars,
                            bestMoves = bestMoves
                        )
                    )
                    if (clearedLevelId < LevelRepository.TOTAL_LEVELS) {
                        val nextExisting = gameDao.getLevelProgress(clearedLevelId + 1)
                        if (nextExisting == null) {
                            gameDao.upsertProgress(
                                GameEntity(
                                    levelId = clearedLevelId + 1,
                                    isUnlocked = true,
                                    isCompleted = false,
                                    stars = 0,
                                    bestMoves = 0
                                )
                            )
                        }
                    }
                }
            }
        } else {
            playFeedback(success = false)
            val newLives = (state.lives - 1).coerceAtLeast(0)
            _uiState.update {
                it.copy(
                    blockedArrowId = arrowId,
                    lives = newLives,
                    moves = it.moves + 1,
                    isGameOver = newLives <= 0
                )
            }
            viewModelScope.launch {
                delay(450)
                _uiState.update {
                    if (it.blockedArrowId == arrowId) it.copy(blockedArrowId = null) else it
                }
            }
        }
    }

    fun useHint() {
        val state = _uiState.value
        if (state.isLevelComplete || state.isGameOver || state.remainingArrows.isEmpty()) return

        val allPointsByArrow = state.remainingArrows.associate { it.id to it.points }
        for (candidate in state.remainingArrows) {
            if (candidate.id in state.exitingArrowIds) continue
            val occupied = HashSet<GridPoint>()
            for ((otherId, pts) in allPointsByArrow) {
                if (otherId != candidate.id && otherId !in state.exitingArrowIds) {
                    occupied.addAll(pts)
                }
            }
            if (isPathClear(candidate, state.levelData.cols, state.levelData.rows, occupied)) {
                _uiState.update { it.copy(hintArrowId = candidate.id) }
                return
            }
        }
    }

    fun undoLastMove() {
        val state = _uiState.value
        if (removedHistory.isEmpty() || state.isLevelComplete || state.isGameOver) return
        val restored = removedHistory.removeLast()
        _uiState.update {
            it.copy(
                remainingArrows = it.remainingArrows + restored,
                hintArrowId = null,
                blockedArrowId = null,
                canUndo = removedHistory.isNotEmpty()
            )
        }
    }

    fun setPaused(paused: Boolean) {
        _uiState.update { it.copy(isPaused = paused) }
    }

    fun toggleSound(enabled: Boolean) {
        _uiState.update { it.copy(isSoundEnabled = enabled) }
        persistCurrentSettings()
    }

    fun toggleHaptics(enabled: Boolean) {
        _uiState.update { it.copy(isHapticsEnabled = enabled) }
        persistCurrentSettings()
    }

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
        persistCurrentSettings()
    }

    fun setDifficulty(difficulty: String) {
        _uiState.update { it.copy(difficulty = difficulty) }
        persistCurrentSettings()
        loadLevel(_uiState.value.currentLevelId)
    }

    fun resetAllProgress() {
        removedHistory.clear()
        val level1 = LevelRepository.getLevel(1, _uiState.value.difficulty)
        _uiState.update {
            it.copy(
                currentLevelId = 1,
                levelData = level1,
                remainingArrows = level1.arrows,
                exitingArrowIds = emptySet(),
                blockedArrowId = null,
                hintArrowId = null,
                lives = 3,
                moves = 0,
                isLevelComplete = false,
                isGameOver = false,
                isPaused = false,
                unlockedLevels = setOf(1),
                levelStars = emptyMap(),
                totalStars = 0,
                completedLevelsCount = 0,
                canUndo = false
            )
        }
        if (gameDao != null) {
            viewModelScope.launch {
                gameDao.clearAllProgress()
                gameDao.upsertProgress(
                    GameEntity(levelId = 1, isUnlocked = true, isCompleted = false, stars = 0, bestMoves = 0)
                )
            }
        }
    }

    private fun persistCurrentSettings() {
        if (gameDao == null) return
        val s = _uiState.value
        viewModelScope.launch {
            gameDao.saveSettings(
                UserSettingsEntity(
                    id = 1,
                    isSoundEnabled = s.isSoundEnabled,
                    isHapticsEnabled = s.isHapticsEnabled,
                    isDarkMode = s.isDarkMode,
                    difficulty = s.difficulty
                )
            )
        }
    }

    private fun isPathClear(
        arrow: ArrowPath,
        cols: Int,
        rows: Int,
        occupiedByOthers: Set<GridPoint>
    ): Boolean {
        var c = arrow.head.col + arrow.direction.dx
        var r = arrow.head.row + arrow.direction.dy
        while (c in 0 until cols && r in 0 until rows) {
            if (GridPoint(c, r) in occupiedByOthers) {
                return false
            }
            c += arrow.direction.dx
            r += arrow.direction.dy
        }
        return true
    }

    private var toneGenerator: ToneGenerator? = null

    private fun playFeedback(success: Boolean) {
        val state = _uiState.value
        if (state.isSoundEnabled) {
            try {
                if (toneGenerator == null) {
                    toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 45)
                }
                val tone = if (success) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_NACK
                toneGenerator?.startTone(tone, if (success) 60 else 120)
            } catch (_: Throwable) {}
        }
        if (state.isHapticsEnabled && appContext != null) {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(
                            if (success) 18L else 55L,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                }
            } catch (_: Throwable) {}
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        toneGenerator?.release()
        toneGenerator = null
    }

    companion object {
        fun provideFactory(gameDao: GameDao?, context: Context? = null): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GameViewModel(gameDao, context?.applicationContext) as T
                }
            }
        }
    }
}
