package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.local.GameDatabase
import com.example.data.repository.GameRepository
import com.example.game.GameViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HowToPlayDialog
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    HOME,
    GAME,
    LEVEL_SELECT,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = try {
            val database = GameDatabase.getInstance(applicationContext)
            val repository = GameRepository(database.gameDao())
            val soundManager = SoundManager(applicationContext)
            val hapticManager = HapticManager(applicationContext)
            GameViewModel(repository, soundManager, hapticManager)
        } catch (_: Throwable) {
            val inMem = androidx.room.Room.inMemoryDatabaseBuilder(applicationContext, GameDatabase::class.java).build()
            GameViewModel(GameRepository(inMem.gameDao()), SoundManager(applicationContext), HapticManager(applicationContext))
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val isDark = uiState.isDarkMode

            MyApplicationTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    GoArrowPuzzleApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun GoArrowPuzzleApp(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var showHowToPlay by remember { mutableStateOf(false) }

    // Android back navigation
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        currentScreen = AppScreen.HOME
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            AppScreen.HOME -> {
                HomeScreen(
                    totalStars = uiState.totalStars,
                    completedLevelsCount = uiState.completedLevels.values.count { it.completed },
                    onPlayClicked = {
                        val nextLevel = uiState.currentLevelId
                        viewModel.loadLevel(nextLevel)
                        currentScreen = AppScreen.GAME
                    },
                    onLevelsClicked = {
                        currentScreen = AppScreen.LEVEL_SELECT
                    },
                    onDailyChallengeClicked = {
                        // Daily challenge level based on date hash
                        val daySeed = 100 + (System.currentTimeMillis() / (1000 * 60 * 60 * 24)).toInt() % 50
                        viewModel.loadLevel(daySeed, isDaily = true)
                        currentScreen = AppScreen.GAME
                    },
                    onHowToPlayClicked = {
                        showHowToPlay = true
                    },
                    onSettingsClicked = {
                        currentScreen = AppScreen.SETTINGS
                    }
                )
            }

            AppScreen.GAME -> {
                GameScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.HOME }
                )
            }

            AppScreen.LEVEL_SELECT -> {
                LevelSelectScreen(
                    completedLevels = uiState.completedLevels,
                    totalStars = uiState.totalStars,
                    onLevelSelected = { levelId ->
                        viewModel.loadLevel(levelId)
                        currentScreen = AppScreen.GAME
                    },
                    onBack = { currentScreen = AppScreen.HOME }
                )
            }

            AppScreen.SETTINGS -> {
                SettingsScreen(
                    isSoundEnabled = uiState.isSoundEnabled,
                    isHapticsEnabled = uiState.isHapticsEnabled,
                    isDarkMode = uiState.isDarkMode,
                    difficulty = uiState.difficulty,
                    onToggleSound = { viewModel.toggleSound(it) },
                    onToggleHaptics = { viewModel.toggleHaptics(it) },
                    onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                    onSetDifficulty = { viewModel.setDifficulty(it) },
                    onResetProgress = { viewModel.resetAllProgress() },
                    onBack = { currentScreen = AppScreen.HOME }
                )
            }
        }
    }

    if (showHowToPlay) {
        HowToPlayDialog(onDismiss = { showHowToPlay = false })
    }
}
