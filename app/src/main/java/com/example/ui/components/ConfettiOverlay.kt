package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiPiece(
    val xFraction: Float,
    val speed: Float,
    val swaySpeed: Float,
    val color: Color,
    val size: Float,
    val initialAngle: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    active: Boolean = true
) {
    if (!active) return

    val confettiList = remember {
        val colors = listOf(
            Color(0xFF38BDF8), // Sky Blue
            Color(0xFF007AFF), // Electric Blue
            Color(0xFFF59E0B), // Gold
            Color(0xFF10B981), // Emerald
            Color(0xFFEC4899), // Pink
            Color(0xFF8B5CF6)  // Purple
        )
        val rng = Random(12345)
        List(60) {
            ConfettiPiece(
                xFraction = rng.nextFloat(),
                speed = 0.5f + rng.nextFloat() * 0.7f,
                swaySpeed = 2f + rng.nextFloat() * 4f,
                color = colors[rng.nextInt(colors.size)],
                size = 12f + rng.nextFloat() * 12f,
                initialAngle = rng.nextFloat() * 360f,
                rotationSpeed = (rng.nextFloat() - 0.5f) * 600f
            )
        }
    }

    var progress by remember { mutableStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2800, easing = LinearEasing),
        label = "confetti_anim"
    )

    LaunchedEffect(active) {
        if (active) {
            progress = 0f
            progress = 1f
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        confettiList.forEach { piece ->
            val y = (piece.speed * animatedProgress * (h + 100f)) - 50f
            val sway = sin(animatedProgress * piece.swaySpeed * Math.PI.toFloat()) * 30f
            val x = (piece.xFraction * w) + sway
            val rot = piece.initialAngle + (piece.rotationSpeed * animatedProgress)

            rotate(rot, pivot = Offset(x, y)) {
                drawRect(
                    color = piece.color,
                    topLeft = Offset(x - piece.size / 2, y - piece.size / 4),
                    size = Size(piece.size, piece.size / 2)
                )
            }
        }
    }
}
