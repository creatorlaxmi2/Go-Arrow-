package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.model.Direction
import com.example.data.model.LevelData
import com.example.ui.theme.DeepNavy
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MiniBoardPreview(
    level: LevelData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.72f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cellW = size.width / (level.cols + 1)
            val cellH = size.height / (level.rows + 1)

            level.arrows.forEach { arrow ->
                if (arrow.points.size >= 2) {
                    val path = Path()
                    val p0 = arrow.points[0]
                    path.moveTo((p0.x + 1) * cellW, (p0.y + 1) * cellH)

                    for (i in 1 until arrow.points.size) {
                        val pt = arrow.points[i]
                        path.lineTo((pt.x + 1) * cellW, (pt.y + 1) * cellH)
                    }

                    drawPath(
                        path = path,
                        color = DeepNavy,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw miniature arrow head
                    val tip = arrow.points.last()
                    val tipX = (tip.x + 1) * cellW
                    val tipY = (tip.y + 1) * cellH

                    val headPath = Path()
                    val headLen = 6.dp.toPx()
                    val angle = when (arrow.direction) {
                        Direction.RIGHT -> 0.0
                        Direction.DOWN -> PI / 2
                        Direction.LEFT -> PI
                        Direction.UP -> -PI / 2
                    }

                    val leftWingX = tipX - headLen * cos(angle - PI / 5).toFloat()
                    val leftWingY = tipY - headLen * sin(angle - PI / 5).toFloat()
                    val rightWingX = tipX - headLen * cos(angle + PI / 5).toFloat()
                    val rightWingY = tipY - headLen * sin(angle + PI / 5).toFloat()

                    headPath.moveTo(tipX, tipY)
                    headPath.lineTo(leftWingX, leftWingY)
                    headPath.lineTo(rightWingX, rightWingY)
                    headPath.close()

                    drawPath(headPath, color = DeepNavy)
                }
            }
        }
    }
}
