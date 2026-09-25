package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ArrowModel
import com.example.data.model.ArrowState
import com.example.data.model.Direction
import com.example.data.model.GridPoint
import com.example.data.model.LevelData
import com.example.ui.theme.BlockedRed
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DotGridColor
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun GameBoard(
    level: LevelData,
    arrows: List<ArrowModel>,
    onArrowTapped: (ArrowModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_pulse"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_scale"
    )

    var tapRipplePos by remember { mutableStateOf<Offset?>(null) }
    val rippleAnim = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x10000000), spotColor = Color(0x18000000))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .testTag("game_board_box")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(level, arrows) {
                    detectTapGestures { tapOffset ->
                        val cellW = size.width.toFloat() / (level.cols + 1)
                        val cellH = size.height.toFloat() / (level.rows + 1)
                        val touchRadiusPx = 34.dp.toPx()

                        // Trigger expanding tap ripple
                        tapRipplePos = tapOffset
                        coroutineScope.launch {
                            rippleAnim.snapTo(0f)
                            rippleAnim.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 350, easing = LinearEasing)
                            )
                            tapRipplePos = null
                        }

                        // Find closest arrow to tap
                        var bestArrow: ArrowModel? = null
                        var minDistance = Float.MAX_VALUE

                        arrows.filter { it.state != ArrowState.REMOVED && it.state != ArrowState.FLYING }.forEach { arrow ->
                            val dist = distanceToArrow(arrow, tapOffset, cellW, cellH)
                            if (dist < touchRadiusPx && dist < minDistance) {
                                minDistance = dist
                                bestArrow = arrow
                            }
                        }

                        bestArrow?.let { onArrowTapped(it) }
                    }
                }
                .testTag("game_board_canvas")
        ) {
            val cellW = size.width / (level.cols + 1)
            val cellH = size.height / (level.rows + 1)

            // 1. Draw Dot Grid for unoccupied intersections
            val occupiedPoints = mutableSetOf<GridPoint>()
            arrows.filter { it.state != ArrowState.REMOVED }.forEach { arrow ->
                for (i in 0 until arrow.points.size - 1) {
                    val p1 = arrow.points[i]
                    val p2 = arrow.points[i + 1]
                    val minX = min(p1.x, p2.x)
                    val maxX = max(p1.x, p2.x)
                    val minY = min(p1.y, p2.y)
                    val maxY = max(p1.y, p2.y)
                    for (x in minX..maxX) {
                        for (y in minY..maxY) {
                            occupiedPoints.add(GridPoint(x, y))
                        }
                    }
                }
            }

            val dotRadius = 2.4.dp.toPx()
            for (x in 0 until level.cols) {
                for (y in 0 until level.rows) {
                    if (!occupiedPoints.contains(GridPoint(x, y))) {
                        drawCircle(
                            color = DotGridColor,
                            radius = dotRadius,
                            center = Offset((x + 1) * cellW, (y + 1) * cellH)
                        )
                    }
                }
            }

            // 2. Draw Arrows
            arrows.forEach { arrow ->
                if (arrow.state != ArrowState.REMOVED) {
                    drawArrow(
                        arrow = arrow,
                        cellW = cellW,
                        cellH = cellH,
                        pulseGlow = pulseGlow,
                        pulseScale = pulseScale
                    )
                }
            }

            // 3. Draw Touch Ripple Circles (Concentric blue rings as in screenshots)
            val currentRippleCenter = tapRipplePos
            if (currentRippleCenter != null) {
                val progress = rippleAnim.value
                val maxRadius = 36.dp.toPx()
                val radius1 = progress * maxRadius
                val radius2 = progress * 0.7f * maxRadius
                val alpha = (1f - progress).coerceIn(0f, 1f)

                if (radius1 > 0.5f) {
                    drawCircle(
                        color = ElectricBlue.copy(alpha = alpha * 0.45f),
                        radius = radius1,
                        center = currentRippleCenter,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
                if (radius2 > 0.5f) {
                    drawCircle(
                        color = ElectricBlueLight.copy(alpha = alpha * 0.6f),
                        radius = radius2,
                        center = currentRippleCenter,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // Hint Pointing Hand Overlay
        val hintedArrow = arrows.find { it.state == ArrowState.HINTED }
        if (hintedArrow != null && hintedArrow.points.isNotEmpty()) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val cellW = maxWidth / (level.cols + 1)
                val cellH = maxHeight / (level.rows + 1)
                val head = hintedArrow.points.last()

                val handX = cellW * (head.x + 1) + when (hintedArrow.direction) {
                    Direction.RIGHT -> (-40).dp
                    Direction.LEFT -> 40.dp
                    else -> 0.dp
                }
                val handY = cellH * (head.y + 1) + when (hintedArrow.direction) {
                    Direction.DOWN -> (-40).dp
                    Direction.UP -> 40.dp
                    else -> 0.dp
                }

                val bounceAnim = rememberInfiniteTransition(label = "hand_bounce")
                val offsetY by bounceAnim.animateFloat(
                    initialValue = 0f,
                    targetValue = 14f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bounce"
                )

                Box(
                    modifier = Modifier
                        .offset(x = handX, y = handY + offsetY.dp)
                        .size(46.dp)
                        .background(ElectricBlue, CircleShape)
                        .shadow(8.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Hint Pointing Hand",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawArrow(
    arrow: ArrowModel,
    cellW: Float,
    cellH: Float,
    pulseGlow: Float,
    pulseScale: Float
) {
    if (arrow.points.size < 2 || arrow.spawnProgress <= 0f) return

    val scale = when (arrow.state) {
        ArrowState.HINTED -> pulseScale
        ArrowState.SELECTED -> 1.08f
        ArrowState.FLYING -> 1.05f
        ArrowState.BLOCKED -> 0.94f
        ArrowState.IDLE, ArrowState.REMOVED -> 1.0f
    }

    val maxDimension = max(size.width, size.height)
    val (path, tip) = buildArrowPathAndTip(
        gridPoints = arrow.points,
        direction = arrow.direction,
        progress = arrow.flyProgress,
        shakeOffset = arrow.shakeOffset,
        cellW = cellW,
        cellH = cellH,
        maxBoardDim = maxDimension
    )

    val strokeWidth = 7.5.dp.toPx() * scale
    val baseColor = when (arrow.state) {
        ArrowState.FLYING, ArrowState.SELECTED -> ElectricBlue
        ArrowState.BLOCKED -> BlockedRed
        ArrowState.HINTED -> ElectricBlue
        ArrowState.IDLE -> DeepNavy
        ArrowState.REMOVED -> Color.Transparent
    }

    val alpha = (if (arrow.state == ArrowState.FLYING) {
        (1f - arrow.flyProgress * 0.7f).coerceIn(0f, 1f)
    } else 1f) * arrow.spawnProgress

    val color = baseColor.copy(alpha = alpha)

    // Chevron Arrowhead Geometry
    val dirX = arrow.direction.dx.toFloat()
    val dirY = arrow.direction.dy.toFloat()
    val perpX = -dirY
    val perpY = dirX

    val headLen = 16.dp.toPx() * scale
    val headHalfWidth = 10.5.dp.toPx() * scale
    val notchDepth = 3.8.dp.toPx() * scale

    val frontTipX = tip.x + dirX * headLen
    val frontTipY = tip.y + dirY * headLen

    val leftWingX = tip.x + perpX * headHalfWidth
    val leftWingY = tip.y + perpY * headHalfWidth

    val rightWingX = tip.x - perpX * headHalfWidth
    val rightWingY = tip.y - perpY * headHalfWidth

    val notchX = tip.x + dirX * notchDepth
    val notchY = tip.y + dirY * notchDepth

    val headPath = Path().apply {
        moveTo(frontTipX, frontTipY)
        lineTo(leftWingX, leftWingY)
        lineTo(notchX, notchY)
        lineTo(rightWingX, rightWingY)
        close()
    }

    // 1. Tactile Vector Drop Shadow (For Idle / Resting Arrows)
    if (arrow.state != ArrowState.REMOVED && arrow.state != ArrowState.FLYING) {
        val shadowOffsetX = 1.5.dp.toPx()
        val shadowOffsetY = 2.5.dp.toPx()
        val shadowColor = Color(0x180B1736)

        val shadowPath = Path().apply {
            addPath(path, Offset(shadowOffsetX, shadowOffsetY))
        }
        drawPath(
            path = shadowPath,
            color = shadowColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        val shadowHeadPath = Path().apply {
            addPath(headPath, Offset(shadowOffsetX, shadowOffsetY))
        }
        drawPath(path = shadowHeadPath, color = shadowColor)
    }

    // 2. Multi-layer Neon Glow Bloom for Hinted / Selected / Flying
    if (arrow.state == ArrowState.HINTED || arrow.state == ArrowState.SELECTED || arrow.state == ArrowState.FLYING) {
        val glowAlpha = if (arrow.state == ArrowState.HINTED) pulseGlow else if (arrow.state == ArrowState.FLYING) alpha * 0.75f else 0.85f

        // Broad ambient soft halo
        drawPath(
            path = path,
            color = ElectricBlueLight.copy(alpha = glowAlpha * 0.35f),
            style = Stroke(
                width = strokeWidth + 14.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Concentrated neon cyan core glow
        drawPath(
            path = path,
            color = Color(0xFF00E5FF).copy(alpha = glowAlpha * 0.7f),
            style = Stroke(
                width = strokeWidth + 6.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Arrowhead neon aura
        drawPath(
            path = headPath,
            color = ElectricBlueLight.copy(alpha = glowAlpha * 0.35f),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = headPath,
            color = Color(0xFF00E5FF).copy(alpha = glowAlpha * 0.7f),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    // 3. Main Deep Navy Vector Stroke with rounded caps and joins
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 4. Solid Precision Arrowhead
    drawPath(path = headPath, color = color)
}

private fun buildArrowPathAndTip(
    gridPoints: List<GridPoint>,
    direction: Direction,
    progress: Float,
    shakeOffset: Float,
    cellW: Float,
    cellH: Float,
    maxBoardDim: Float
): Pair<Path, Offset> {
    val pixelPoints = gridPoints.map { pt ->
        Offset((pt.x + 1) * cellW, (pt.y + 1) * cellH)
    }
    if (pixelPoints.size < 2) return Pair(Path(), Offset.Zero)

    // Segment lengths and total path length
    val segLengths = mutableListOf<Float>()
    var totalLength = 0f
    for (i in 0 until pixelPoints.size - 1) {
        val len = (pixelPoints[i + 1] - pixelPoints[i]).getDistance()
        segLengths.add(len)
        totalLength += len
    }

    val dirUnitX = direction.dx.toFloat()
    val dirUnitY = direction.dy.toFloat()

    if (progress <= 0f) {
        val shakeX = direction.dx * shakeOffset
        val shakeY = direction.dy * shakeOffset

        val path = Path()
        path.moveTo(pixelPoints[0].x + shakeX, pixelPoints[0].y + shakeY)
        for (i in 1 until pixelPoints.size) {
            path.lineTo(pixelPoints[i].x + shakeX, pixelPoints[i].y + shakeY)
        }
        val tip = pixelPoints.last() + Offset(shakeX, shakeY)
        return Pair(path, tip)
    }

    val travelDist = progress * (totalLength + maxBoardDim * 1.3f)
    val originalHead = pixelPoints.last()
    val currentTip = Offset(
        originalHead.x + dirUnitX * travelDist,
        originalHead.y + dirUnitY * travelDist
    )

    val path = Path()

    if (travelDist >= totalLength) {
        // Arrow has slithered completely past the initial track
        val currentTail = Offset(
            originalHead.x + dirUnitX * (travelDist - totalLength),
            originalHead.y + dirUnitY * (travelDist - totalLength)
        )
        path.moveTo(currentTail.x, currentTail.y)
        path.lineTo(currentTip.x, currentTip.y)
    } else {
        // Tail is moving along the original track
        var accumulated = 0f
        var tailPoint: Offset? = null
        var tailSegIndex = 0

        for (i in segLengths.indices) {
            val segLen = segLengths[i]
            if (accumulated + segLen >= travelDist) {
                val frac = if (segLen > 0f) (travelDist - accumulated) / segLen else 0f
                tailPoint = Offset(
                    pixelPoints[i].x + (pixelPoints[i + 1].x - pixelPoints[i].x) * frac,
                    pixelPoints[i].y + (pixelPoints[i + 1].y - pixelPoints[i].y) * frac
                )
                tailSegIndex = i
                break
            }
            accumulated += segLen
        }

        if (tailPoint != null) {
            path.moveTo(tailPoint.x, tailPoint.y)
            for (i in (tailSegIndex + 1) until pixelPoints.size) {
                path.lineTo(pixelPoints[i].x, pixelPoints[i].y)
            }
            path.lineTo(currentTip.x, currentTip.y)
        } else {
            path.moveTo(originalHead.x, originalHead.y)
            path.lineTo(currentTip.x, currentTip.y)
        }
    }

    return Pair(path, currentTip)
}

private fun distanceToArrow(
    arrow: ArrowModel,
    tap: Offset,
    cellW: Float,
    cellH: Float
): Float {
    var minDist = Float.MAX_VALUE
    for (i in 0 until arrow.points.size - 1) {
        val p1 = arrow.points[i]
        val p2 = arrow.points[i + 1]
        val a = Offset((p1.x + 1) * cellW, (p1.y + 1) * cellH)
        val b = Offset((p2.x + 1) * cellW, (p2.y + 1) * cellH)
        val d = distancePointToSegment(tap, a, b)
        if (d < minDist) {
            minDist = d
        }
    }
    // Also consider the arrowhead region
    val headPoint = arrow.points.last()
    val headOffset = Offset((headPoint.x + 1) * cellW, (headPoint.y + 1) * cellH)
    val tipOffset = Offset(
        headOffset.x + arrow.direction.dx * (cellW * 0.4f),
        headOffset.y + arrow.direction.dy * (cellH * 0.4f)
    )
    val headDist = distancePointToSegment(tap, headOffset, tipOffset)
    if (headDist < minDist) {
        minDist = headDist
    }
    return minDist
}

private fun distancePointToSegment(p: Offset, a: Offset, b: Offset): Float {
    val ab = b - a
    val lengthSq = ab.x * ab.x + ab.y * ab.y
    if (lengthSq == 0f) return (p - a).getDistance()

    val t = ((p.x - a.x) * ab.x + (p.y - a.y) * ab.y) / lengthSq
    val clampedT = t.coerceIn(0f, 1f)
    val projection = Offset(a.x + clampedT * ab.x, a.y + clampedT * ab.y)
    return (p - projection).getDistance()
}
