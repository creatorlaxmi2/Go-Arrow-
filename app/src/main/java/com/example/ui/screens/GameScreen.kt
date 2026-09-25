package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.data.model.ArrowState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameStatus
import com.example.game.GameUiState
import com.example.game.GameViewModel
import com.example.ui.components.GameBoard
import com.example.ui.components.HeartRow
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight

@Composable
fun GameScreen(
    uiState: GameUiState,
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHintExhaustedDialog by remember { mutableStateOf(false) }

    // Number of arrows remaining
    val remainingArrows = remember(uiState.arrows) {
        uiState.arrows.count { it.state != ArrowState.REMOVED }
    }

    // Dynamic headline matching reference screenshots
    val headerTitle = when {
        uiState.arrows.any { it.state == ArrowState.HINTED } -> "Use Helpful Hints"
        uiState.currentLevelId % 3 == 0 -> "Find the Free Path"
        uiState.currentLevelId % 3 == 1 -> "Tap Away Arrows"
        else -> "Clear the Arrows"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Big Bold Reference Header Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = headerTitle,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2E3A59),
                    letterSpacing = (-0.5).sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 36.sp
                )
            }

            // 2. Navigation Row: < Back, Level XX, ⚙ Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp).testTag("game_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ElectricBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = if (uiState.isDailyChallenge) "Daily Challenge" else "Level ${uiState.currentLevelId}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                IconButton(
                    onClick = { viewModel.pauseGame() },
                    modifier = Modifier.size(40.dp).testTag("game_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = ElectricBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 3. Status Bar matching screenshots: [ ↗ 42 ]   ❤️ ❤️ ❤️   [ Normal ]
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .testTag("game_hud")
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Remaining arrows pill with flight icon
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.testTag("moves_counter")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = "Remaining Arrows",
                                tint = DeepNavy,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$remainingArrows",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DeepNavy
                            )
                        }
                    }

                    // Taps / Moves counter pill tracking user taps
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.testTag("taps_counter")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Taps: ${uiState.moves}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DeepNavy
                            )
                        }
                    }

                    // 3 Hearts
                    HeartRow(heartsRemaining = uiState.heartsRemaining)

                    // Difficulty pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.testTag("difficulty_badge")
                    ) {
                        Text(
                            text = uiState.levelData?.difficulty ?: "Normal",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = DeepNavy,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 4. Center Game Board
            uiState.levelData?.let { level ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoard(
                        level = level,
                        arrows = uiState.arrows,
                        onArrowTapped = { arrow ->
                            viewModel.onArrowTap(arrow)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // 5. Bottom Circular Floating Controls matching Screenshots: 💡 and #
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hint Button: Big circular white button with blue border & count badge
                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier.size(76.dp)
                ) {
                    Surface(
                        onClick = {
                            if (uiState.hintsRemaining > 0) {
                                viewModel.useHint()
                            } else {
                                showHintExhaustedDialog = true
                            }
                        },
                        shape = CircleShape,
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(2.5.dp, ElectricBlue),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.Center)
                            .testTag("hint_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint",
                                tint = ElectricBlue,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    // Circular Hint Count Badge on top-right corner
                    Surface(
                        shape = CircleShape,
                        color = ElectricBlue,
                        modifier = Modifier
                            .size(26.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${uiState.hintsRemaining}",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(36.dp))

                // Undo / Helper Button: Circular button with '#' symbol in electric blue
                Surface(
                    onClick = { viewModel.undo() },
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 8.dp,
                    enabled = uiState.canUndo,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("undo_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.canUndo) ElectricBlue else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // Modals overlay
        if (uiState.gameStatus == GameStatus.PAUSED) {
            PauseModal(
                isSoundEnabled = uiState.isSoundEnabled,
                isHapticsEnabled = uiState.isHapticsEnabled,
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.restartLevel() },
                onToggleSound = { viewModel.toggleSound(it) },
                onToggleHaptics = { viewModel.toggleHaptics(it) },
                onHome = onBack
            )
        }

        if (uiState.gameStatus == GameStatus.GAME_OVER) {
            GameOverModal(
                onRestart = { viewModel.restartLevel() },
                onHome = onBack,
                onWatchAdToRevive = { viewModel.reviveGame() }
            )
        }

        if (uiState.gameStatus == GameStatus.LEVEL_COMPLETE && uiState.levelData != null) {
            LevelCompleteModal(
                level = uiState.levelData,
                stars = uiState.starsEarned,
                score = uiState.score,
                moves = uiState.moves,
                timeSeconds = uiState.elapsedTimeSeconds,
                onNextLevel = { viewModel.nextLevel() },
                onReplay = { viewModel.restartLevel() },
                onHome = onBack
            )
        }

        if (showHintExhaustedDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showHintExhaustedDialog = false },
                title = { Text("Need More Hints?", fontWeight = FontWeight.Bold) },
                text = { Text("You are out of hints! Tap below to claim 3 bonus hints.") },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = {
                            viewModel.addBonusHints(3)
                            showHintExhaustedDialog = false
                        }
                    ) {
                        Text("Get 3 Hints")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { showHintExhaustedDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
