package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.CosmicLight
import com.example.ui.theme.GoldAccent

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CosmicBg, CosmicDark, CosmicBg)
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CosmicLight.copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.2f, size.height * 0.18f),
                        radius = size.maxDimension * 0.45f
                    ),
                    center = Offset(size.width * 0.2f, size.height * 0.18f),
                    radius = size.maxDimension * 0.45f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldAccent.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.82f),
                        radius = size.maxDimension * 0.4f
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.82f),
                    radius = size.maxDimension * 0.4f
                )
            }
    ) {
        content()
    }
}
