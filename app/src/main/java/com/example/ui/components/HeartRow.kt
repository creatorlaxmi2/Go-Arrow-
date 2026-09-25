package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BlockedRed

@Composable
fun HeartRow(
    heartsRemaining: Int,
    maxHearts: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.testTag("hearts_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxHearts) {
            val isFilled = i <= heartsRemaining
            val targetScale = if (isFilled) 1f else 0.82f
            val scale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "heart_scale_$i"
            )
            val color by animateColorAsState(
                targetValue = if (isFilled) BlockedRed else Color(0xFFCBD5E1),
                label = "heart_color_$i"
            )

            Icon(
                imageVector = if (isFilled) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFilled) "Heart active" else "Heart lost",
                tint = color,
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale)
            )
        }
    }
}
