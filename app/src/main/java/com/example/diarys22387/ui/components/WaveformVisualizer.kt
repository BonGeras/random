package com.example.diarys22387.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier.height(60.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        amplitudes.forEach { amplitude ->
            val animatedHeight by animateFloatAsState(
                targetValue = amplitude,
                label = "amplitude"
            )
            
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp)
            ) {
                val barWidth = size.width * 0.8f
                val barHeight = size.height * animatedHeight
                val xOffset = (size.width - barWidth) / 2
                val yOffset = (size.height - barHeight) / 2
                
                drawRoundRect(
                    color = color,
                    topLeft = Offset(xOffset, yOffset),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )
            }
        }
    }
} 