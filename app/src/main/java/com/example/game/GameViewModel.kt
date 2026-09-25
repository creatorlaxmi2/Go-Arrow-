package com.example.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.levels.LevelRepository
import com.example.data.local.GameEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.model.ArrowModel
import com.example.data.model.ArrowState
import com.example.data.model.LevelData
import com.example.data.repository.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class GameStatus {
    PLAYING,
    LEVEL_COMPLETE,
    GAME_OVER,
    PAUSED
}

data class UndoSnapshot(
    val arrows: List<ArrowModel>,
    val moves: Int,
    val score: Int
)

data class GameUiState(
    val currentLevelId: Int = 1,
    val levelData: LevelData? = null,
    val arrows: List<ArrowModel> = emptyList(),
    val heartsRemaining: Int = 3,
    val moves: Int = 0,
    val score: Int = 0,
    val elapsedTimeSeconds: Int = 0,
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val starsEarned: Int = 0,
    val hintsRemaining: Int = 5,
    val canUndo: Boolean = false,
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val isDarkMode: Boolean = false,
    val completedLevels: Map<Int, GameEntity> = emptyMap(),
    val totalStars: Int = 0,
    val isDailyChallenge: Boolean = false,
    val difficulty: String = "Normal"
)

class GameViewModel(
    private val repository: GameRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<UndoSnapshot>()
    private var timerJob: Job? = null
    private var shakeJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeDefaults()
        }

        viewModelScope.launch {
            repository.userSettingsFlow.collect { settings ->
                soundManager.isSoundEnabled = settings.soundEnabled
                hapticManager.isHapticsEnabled = settings.hapticsEnabled
                _uiState.update {
                    it.copy(
                        hintsRemaining = settings.hintsCount,
                        isSoundEnabled = settings.soundEnabled,
                        isHapticsEnabled = settings.hapticsEnabled,
                        isDarkMode = settings.darkModeEnabled,
                        difficulty = settings.difficulty
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.levelRecordsFlow.collect { records ->
                val recordMap = records.associateBy { it.levelId }
                val total = records.sumOf { it.stars }
                _uiState.update {
                    it.copy(
                        completedLevels = recordMap,
                        totalStars = total
                    )
                }
            }
        }

        loadLevel(1)
    }

    fun loadLevel(levelId: Int, isDaily: Boolean = false) {
        timerJob?.cancel()
        val currentDifficulty = _uiState.value.difficulty
        val level = LevelRepository.getLevel(levelId, isRestart = false, difficultySetting = currentDifficulty)
        undoStack.clear()

        _uiState.update {
            it.copy(
                currentLevelId = levelId,
                levelData = level,
                arrows = level.arrows,
                heartsRemaining = 3,
                moves = 0,
                score = 0,
                elapsedTimeSeconds = 0,
                gameStatus = GameStatus.PLAYING,
                starsEarned = 0,
                canUndo = false,
                isDailyChallenge = isDaily
            )
        }

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.gameStatus == GameStatus.PLAYING) {
                delay(1000)
                _uiState.update { it.copy(elapsedTimeSeconds = it.elapsedTimeSeconds + 1) }
            }
        }
    }

    fun onArrowTap(arrow: ArrowModel) {
        val state = _uiState.value
        if (state.gameStatus != GameStatus.PLAYING) return
        if (arrow.state == ArrowState.FLYING || arrow.state == ArrowState.REMOVED || arrow.state == ArrowState.BLOCKED) return
        if (state.arrows.any { it.state == ArrowState.FLYING }) return

        val level = state.levelData ?: return
        val activeArrows = state.arrows.filter { it.state != ArrowState.REMOVED }

        soundManager.playTap()
        hapticManager.vibrateTap()

        val isClear = CollisionEngine.isArrowClear(
            arrow = arrow,
            otherActiveArrows = activeArrows,
            cols = level.cols,
            rows = level.rows
        )

        if (isClear) {
            handleValidMove(arrow)
        } else {
            handleBlockedMove(arrow)
        }
    }

    private fun handleValidMove(arrow: ArrowModel) {
        // Record undo state before moving
        undoStack.add(
            UndoSnapshot(
                arrows = _uiState.value.arrows,
                moves = _uiState.value.moves,
                score = _uiState.value.score
            )
        )

        val newMoves = _uiState.value.moves + 1
        val newScore = _uiState.value.score + 100

        soundManager.playSuccess()
        hapticManager.vibrateSuccess()

        // Animate flying arrow smoothly with FastOutSlowIn acceleration
        viewModelScope.launch {
            val totalFrames = 20
            val frameDelay = 16L // ~60 FPS

            for (frame in 1..totalFrames) {
                val t = frame.toFloat() / totalFrames.toFloat()
                // Smooth acceleration curve: smooth launch, fast slither and shoot
                val progress = t * t * (3f - 2f * t)
                _uiState.update { curr ->
                    curr.copy(
                        arrows = curr.arrows.map {
                            if (it.id == arrow.id) {
                                it.copy(state = ArrowState.FLYING, flyProgress = progress)
                            } else it
                        },
                        moves = newMoves,
                        score = newScore,
                        canUndo = true
                    )
                }
                delay(frameDelay)
            }

            // Mark as removed
            _uiState.update { curr ->
                curr.copy(
                    arrows = curr.arrows.map {
                        if (it.id == arrow.id) {
                            it.copy(state = ArrowState.REMOVED, flyProgress = 1f)
                        } else it
                    }
                )
            }

            // Check if level complete
            val remaining = _uiState.value.arrows.count { it.state != ArrowState.REMOVED }
            if (remaining == 0) {
                handleLevelComplete()
            }
        }
    }

    private fun handleBlockedMove(arrow: ArrowModel) {
        val newHearts = _uiState.value.heartsRemaining - 1

        soundManager.playBlocked()
        hapticManager.vibrateBlocked()

        val level = _uiState.value.levelData
        val activeArrows = _uiState.value.arrows.filter { it.state != ArrowState.REMOVED }
        val blockingArrow = if (level != null) {
            CollisionEngine.findBlockingArrow(
                arrow = arrow,
                otherActiveArrows = activeArrows,
                cols = level.cols,
                rows = level.rows
            )
        } else null

        shakeJob?.cancel()
        shakeJob = viewModelScope.launch {
            val steps = 16
            val stepDelay = 16L
            for (step in 1..steps) {
                val t = step.toFloat() / steps.toFloat()
                // Damped elastic bump recoil in direction of arrow
                val offset = sin(t * Math.PI.toFloat() * 3.5f) * (1f - t) * 11f
                _uiState.update { curr ->
                    curr.copy(
                        arrows = curr.arrows.map {
                            when (it.id) {
                                arrow.id -> it.copy(state = ArrowState.BLOCKED, shakeOffset = offset)
                                blockingArrow?.id -> it.copy(state = ArrowState.HINTED) // flash blocking obstacle!
                                else -> it
                            }
                        },
                        heartsRemaining = newHearts
                    )
                }
                delay(stepDelay)
            }

            // Reset to idle
            _uiState.update { curr ->
                curr.copy(
                    arrows = curr.arrows.map {
                        if (it.id == arrow.id || it.id == blockingArrow?.id) {
                            it.copy(state = ArrowState.IDLE, shakeOffset = 0f)
                        } else it
                    }
                )
            }

            if (newHearts <= 0) {
                timerJob?.cancel()
                _uiState.update { it.copy(gameStatus = GameStatus.GAME_OVER) }
            }
        }
    }

    private fun handleLevelComplete() {
        timerJob?.cancel()
        val hearts = _uiState.value.heartsRemaining
        val stars = when (hearts) {
            3 -> 3
            2 -> 2
            else -> 1
        }

        val finalScore = _uiState.value.score + (hearts * 150) - (_uiState.value.moves * 5)
            .coerceAtLeast(0)

        soundManager.playLevelComplete()
        hapticManager.vibrateLevelComplete()

        _uiState.update {
            it.copy(
                gameStatus = GameStatus.LEVEL_COMPLETE,
                starsEarned = stars,
                score = maxOf(finalScore, 100)
            )
        }

        viewModelScope.launch {
            repository.saveLevelCompletion(
                levelId = _uiState.value.currentLevelId,
                stars = stars,
                score = maxOf(finalScore, 100),
                moves = _uiState.value.moves,
                timeSeconds = _uiState.value.elapsedTimeSeconds
            )
        }
    }

    fun useHint() {
        if (_uiState.value.hintsRemaining <= 0) return
        val level = _uiState.value.levelData ?: return
        val active = _uiState.value.arrows.filter { it.state != ArrowState.REMOVED && it.state != ArrowState.FLYING }
        val safeArrows = CollisionEngine.findSafeArrows(active, level.cols, level.rows)

        if (safeArrows.isNotEmpty()) {
            val chosen = safeArrows.first()
            viewModelScope.launch {
                repository.consumeHint()
            }

            soundManager.playTap()
            hapticManager.vibrateTap()

            _uiState.update { curr ->
                curr.copy(
                    arrows = curr.arrows.map {
                        if (it.id == chosen.id) {
                            it.copy(state = ArrowState.HINTED)
                        } else if (it.state == ArrowState.HINTED) {
                            it.copy(state = ArrowState.IDLE)
                        } else it
                    }
                )
            }

            // Remove hint after 4 seconds
            viewModelScope.launch {
                delay(4000)
                _uiState.update { curr ->
                    curr.copy(
                        arrows = curr.arrows.map {
                            if (it.id == chosen.id && it.state == ArrowState.HINTED) {
                                it.copy(state = ArrowState.IDLE)
                            } else it
                        }
                    )
                }
            }
        }
    }

    fun addBonusHints(amount: Int = 3) {
        viewModelScope.launch {
            repository.addHints(amount)
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val lastState = undoStack.removeAt(undoStack.lastIndex)
        soundManager.playUndo()

        _uiState.update {
            it.copy(
                arrows = lastState.arrows,
                moves = lastState.moves,
                score = lastState.score,
                canUndo = undoStack.isNotEmpty()
            )
        }
    }

    fun restartLevel() {
        val currentId = _uiState.value.currentLevelId
        val isDaily = _uiState.value.isDailyChallenge
        val currentDifficulty = _uiState.value.difficulty
        timerJob?.cancel()
        undoStack.clear()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    arrows = emptyList(),
                    gameStatus = GameStatus.PLAYING,
                    moves = 0,
                    score = 0,
                    elapsedTimeSeconds = 0,
                    heartsRemaining = 3,
                    starsEarned = 0,
                    canUndo = false
                )
            }
            delay(150)

            val level = LevelRepository.getLevel(currentId, isRestart = true, difficultySetting = currentDifficulty)
            val initialArrows = level.arrows.map { it.copy(spawnProgress = 0f) }

            _uiState.update {
                it.copy(
                    currentLevelId = currentId,
                    levelData = level,
                    arrows = initialArrows,
                    isDailyChallenge = isDaily
                )
            }

            for (i in initialArrows.indices) {
                delay(70)
                viewModelScope.launch {
                    val anim = androidx.compose.animation.core.Animatable(0f)
                    anim.animateTo(
                        targetValue = 1f,
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 350,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        )
                    ) {
                        val currentVal = value
                        _uiState.update { state ->
                            val updated = state.arrows.toMutableList()
                            if (i < updated.size) {
                                updated[i] = updated[i].copy(spawnProgress = currentVal)
                            }
                            state.copy(arrows = updated)
                        }
                    }
                }
            }

            startTimer()
        }
    }

    fun setDifficulty(difficulty: String) {
        _uiState.update { it.copy(difficulty = difficulty) }
        viewModelScope.launch {
            repository.updateSettings(difficulty = difficulty)
        }
        loadLevel(_uiState.value.currentLevelId, _uiState.value.isDailyChallenge)
    }

    fun nextLevel() {
        loadLevel(_uiState.value.currentLevelId + 1)
    }

    fun pauseGame() {
        if (_uiState.value.gameStatus == GameStatus.PLAYING) {
            timerJob?.cancel()
            _uiState.update { it.copy(gameStatus = GameStatus.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameStatus == GameStatus.PAUSED) {
            _uiState.update { it.copy(gameStatus = GameStatus.PLAYING) }
            startTimer()
        }
    }

    fun reviveGame() {
        _uiState.update {
            it.copy(
                heartsRemaining = 3,
                gameStatus = GameStatus.PLAYING
            )
        }
        startTimer()
    }

    fun toggleSound(enabled: Boolean) {
        soundManager.isSoundEnabled = enabled
        viewModelScope.launch { repository.updateSettings(sound = enabled) }
    }

    fun toggleHaptics(enabled: Boolean) {
        hapticManager.isHapticsEnabled = enabled
        viewModelScope.launch { repository.updateSettings(haptics = enabled) }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings(darkMode = enabled) }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetProgress()
            loadLevel(1)
        }
    }
}
