package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.CosmicLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldGlow

/**
 * Shared Pharaonic-inspired cosmic purple background pattern with carved sandstone registers,
 * subtle hieroglyphic pilasters, starry nebula specks, and warm ambient lighting.
 */
fun DrawScope.drawPharaonicCosmicBackdrop(
    includeHieroglyphicPilasters: Boolean = true,
    includeGoldTopAura: Boolean = false
) {
    // 1. Central purple temple glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                CosmicLight.copy(alpha = 0.62f),
                Color(0xFF1A0B33).copy(alpha = 0.35f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.5f, size.height * 0.36f),
            radius = size.maxDimension * 0.64f
        ),
        center = Offset(size.width * 0.5f, size.height * 0.36f),
        radius = size.maxDimension * 0.64f
    )

    // 2. Warm top golden ambiance (Header glow & Welcome screen)
    if (includeGoldTopAura) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    GoldAccent.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.06f),
                radius = size.maxDimension * 0.44f
            ),
            center = Offset(size.width * 0.5f, size.height * 0.06f),
            radius = size.maxDimension * 0.44f
        )
    }

    // 3. Faint carved temple stone masonry horizontal & vertical grid across backdrop
    if (size.width > 0f && size.height > 0f) {
        val stoneLineColor = Color(0xFF8C6EC9).copy(alpha = 0.045f)
        val rowHeight = (size.height / 11f).coerceAtLeast(64f)
        var rowY = rowHeight
        var rowIndex = 0
        while (rowY < size.height) {
            drawLine(
                color = stoneLineColor,
                start = Offset(0f, rowY),
                end = Offset(size.width, rowY),
                strokeWidth = 1f
            )
            // Staggered vertical stone joint lines
            val jointOffset = if (rowIndex % 2 == 0) size.width * 0.33f else size.width * 0.67f
            drawLine(
                color = stoneLineColor,
                start = Offset(jointOffset, rowY - rowHeight),
                end = Offset(jointOffset, rowY),
                strokeWidth = 1f
            )
            rowY += rowHeight
            rowIndex++
        }
    }

    // 4. Subtle cosmic stardust specks
    val starPoints = listOf(
        0.14f to 0.12f, 0.84f to 0.17f, 0.25f to 0.29f,
        0.76f to 0.38f, 0.18f to 0.55f, 0.85f to 0.62f,
        0.32f to 0.76f, 0.70f to 0.84f, 0.50f to 0.21f,
        0.42f to 0.65f, 0.61f to 0.48f, 0.22f to 0.88f
    )
    starPoints.forEachIndexed { idx, (fx, fy) ->
        val radius = if (idx % 3 == 0) 2.2f else 1.4f
        drawCircle(
            color = if (idx % 2 == 0) GoldGlow.copy(alpha = 0.20f) else Color(0xFFB79CED).copy(alpha = 0.20f),
            radius = radius,
            center = Offset(size.width * fx, size.height * fy)
        )
    }

    // 5. Left & right carved Pharaonic hieroglyphic stone pilasters
    if (includeHieroglyphicPilasters && size.width > 0f && size.height > 0f) {
        val marginOuter = size.width * 0.018f
        val columnWidth = (size.width * 0.088f).coerceIn(26f, 48f)
        val baseGlyphColor = Color(0xFFB5925B).copy(alpha = 0.16f)
        val goldGlyphColor = GoldAccent.copy(alpha = 0.19f)
        val glyphStroke = Stroke(width = 1.6f)

        listOf(marginOuter, size.width - marginOuter - columnWidth).forEach { colX ->
            // Pilaster stone fill wash
            drawRect(
                color = Color(0xFF23133D).copy(alpha = 0.24f),
                topLeft = Offset(colX, size.height * 0.04f),
                size = Size(columnWidth, size.height * 0.92f)
            )
            // Outer & inner carved column borders
            drawLine(
                color = baseGlyphColor,
                start = Offset(colX, size.height * 0.04f),
                end = Offset(colX, size.height * 0.96f),
                strokeWidth = 1.5f
            )
            drawLine(
                color = baseGlyphColor,
                start = Offset(colX + columnWidth, size.height * 0.04f),
                end = Offset(colX + columnWidth, size.height * 0.96f),
                strokeWidth = 1.5f
            )

            val cx = colX + columnWidth * 0.5f
            val step = (size.height * 0.078f).coerceIn(52f, 88f)
            var y = size.height * 0.09f
            var glyphIndex = 0
            while (y < size.height * 0.90f) {
                drawLine(
                    color = baseGlyphColor.copy(alpha = 0.09f),
                    start = Offset(colX + 3f, y + step * 0.45f),
                    end = Offset(colX + columnWidth - 3f, y + step * 0.45f),
                    strokeWidth = 1f
                )

                val color = if (glyphIndex % 3 == 0) goldGlyphColor else baseGlyphColor
                when (glyphIndex % 6) {
                    0 -> {
                        // Ankh
                        val r = columnWidth * 0.17f
                        drawCircle(
                            color = color,
                            radius = r,
                            center = Offset(cx, y - r * 0.7f),
                            style = glyphStroke
                        )
                        drawLine(
                            color = color,
                            start = Offset(cx, y + r * 0.3f),
                            end = Offset(cx, y + r * 2.15f),
                            strokeWidth = 1.6f
                        )
                        drawLine(
                            color = color,
                            start = Offset(cx - r * 1.15f, y + r * 0.85f),
                            end = Offset(cx + r * 1.15f, y + r * 0.85f),
                            strokeWidth = 1.6f
                        )
                    }
                    1 -> {
                        // Royal Cartouche
                        val w = columnWidth * 0.46f
                        val h = columnWidth * 0.78f
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(cx - w / 2f, y - h / 2f),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(w / 2f, w / 2f),
                            style = glyphStroke
                        )
                        drawLine(
                            color = color,
                            start = Offset(cx - w * 0.6f, y + h / 2f + 2f),
                            end = Offset(cx + w * 0.6f, y + h / 2f + 2f),
                            strokeWidth = 1.6f
                        )
                    }
                    2 -> {
                        // Eye of Horus silhouette
                        val rx = columnWidth * 0.27f
                        val eyePath = Path().apply {
                            moveTo(cx - rx, y)
                            quadraticTo(cx, y - rx * 0.65f, cx + rx, y)
                            quadraticTo(cx, y + rx * 0.55f, cx - rx, y)
                        }
                        drawPath(eyePath, color = color, style = glyphStroke)
                        drawCircle(color = color, radius = rx * 0.22f, center = Offset(cx, y), style = glyphStroke)
                        drawLine(
                            color = color,
                            start = Offset(cx, y + rx * 0.3f),
                            end = Offset(cx - rx * 0.3f, y + rx * 0.95f),
                            strokeWidth = 1.4f
                        )
                    }
                    3 -> {
                        // Djed Pillar
                        val w = columnWidth * 0.28f
                        drawLine(
                            color = color,
                            start = Offset(cx, y - w * 1.1f),
                            end = Offset(cx, y + w * 1.1f),
                            strokeWidth = 1.8f
                        )
                        listOf(-0.6f, -0.25f, 0.1f, 0.85f).forEach { factor ->
                            drawLine(
                                color = color,
                                start = Offset(cx - w * 0.85f, y + w * factor),
                                end = Offset(cx + w * 0.85f, y + w * factor),
                                strokeWidth = 1.5f
                            )
                        }
                    }
                    4 -> {
                        // Pyramid & Solar Disk
                        val half = columnWidth * 0.27f
                        val tri = Path().apply {
                            moveTo(cx, y - half * 0.6f)
                            lineTo(cx - half, y + half * 0.85f)
                            lineTo(cx + half, y + half * 0.85f)
                            close()
                        }
                        drawPath(tri, color = color, style = glyphStroke)
                        drawCircle(
                            color = color,
                            radius = columnWidth * 0.085f,
                            center = Offset(cx, y - half * 1.05f),
                            style = glyphStroke
                        )
                    }
                    else -> {
                        // Nile Water Zig-Zag
                        val w = columnWidth * 0.30f
                        val wave = Path().apply {
                            moveTo(cx - w, y)
                            lineTo(cx - w * 0.35f, y - 5f)
                            lineTo(cx, y + 4f)
                            lineTo(cx + w * 0.35f, y - 5f)
                            lineTo(cx + w, y)
                        }
                        drawPath(wave, color = color, style = glyphStroke)
                    }
                }
                y += step
                glyphIndex++
            }
        }
    }
}

/**
 * Dense cosmic starry nebula draw helper used inside the Control Center drawer.
 */
fun DrawScope.drawStarryDrawerBackdrop() {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF3E1C70).copy(alpha = 0.55f), Color.Transparent),
            center = Offset(size.width * 0.42f, size.height * 0.45f),
            radius = size.maxDimension * 0.55f
        ),
        center = Offset(size.width * 0.42f, size.height * 0.45f),
        radius = size.maxDimension * 0.55f
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(GoldAccent.copy(alpha = 0.16f), Color.Transparent),
            center = Offset(size.width * 0.96f, size.height * 0.42f),
            radius = size.width * 0.52f
        ),
        center = Offset(size.width * 0.96f, size.height * 0.42f),
        radius = size.width * 0.52f
    )
    val stars = listOf(
        0.12f to 0.08f, 0.48f to 0.06f, 0.78f to 0.11f, 0.22f to 0.19f,
        0.65f to 0.24f, 0.88f to 0.28f, 0.15f to 0.35f, 0.44f to 0.39f,
        0.72f to 0.44f, 0.31f to 0.52f, 0.82f to 0.56f, 0.19f to 0.63f,
        0.55f to 0.68f, 0.86f to 0.72f, 0.28f to 0.79f, 0.64f to 0.83f,
        0.41f to 0.91f, 0.77f to 0.94f, 0.11f to 0.89f, 0.59f to 0.15f
    )
    stars.forEachIndexed { idx, (fx, fy) ->
        drawCircle(
            color = if (idx % 3 == 0) GoldGlow.copy(alpha = 0.28f) else Color(0xFFD9C6FF).copy(alpha = 0.24f),
            radius = if (idx % 4 == 0) 2.2f else 1.3f,
            center = Offset(size.width * fx, size.height * fy)
        )
    }
}

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A0938))
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.alpha_go_cosmic_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        content()
    }
}
