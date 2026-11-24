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
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.CosmicTop
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldGlow

private val MAIN_STAR_COORDS = listOf(
    Triple(0.08f, 0.14f, 1.3f), Triple(0.21f, 0.09f, 0.8f), Triple(0.36f, 0.16f, 1.5f),
    Triple(0.52f, 0.12f, 0.9f), Triple(0.67f, 0.18f, 1.2f), Triple(0.83f, 0.11f, 1.4f),
    Triple(0.92f, 0.22f, 0.8f), Triple(0.14f, 0.27f, 1.1f), Triple(0.29f, 0.24f, 0.7f),
    Triple(0.45f, 0.31f, 1.6f), Triple(0.61f, 0.28f, 0.9f), Triple(0.76f, 0.34f, 1.3f),
    Triple(0.89f, 0.39f, 1.0f), Triple(0.11f, 0.43f, 1.4f), Triple(0.26f, 0.48f, 0.8f),
    Triple(0.41f, 0.44f, 1.2f), Triple(0.58f, 0.51f, 1.5f), Triple(0.73f, 0.47f, 0.7f),
    Triple(0.86f, 0.54f, 1.1f), Triple(0.17f, 0.61f, 0.9f), Triple(0.34f, 0.58f, 1.4f),
    Triple(0.49f, 0.64f, 0.8f), Triple(0.65f, 0.60f, 1.3f), Triple(0.81f, 0.67f, 1.0f),
    Triple(0.12f, 0.73f, 1.2f), Triple(0.28f, 0.77f, 1.5f), Triple(0.44f, 0.72f, 0.7f),
    Triple(0.59f, 0.79f, 1.1f), Triple(0.74f, 0.75f, 1.4f), Triple(0.90f, 0.81f, 0.9f),
    Triple(0.19f, 0.86f, 1.3f), Triple(0.37f, 0.89f, 0.8f), Triple(0.54f, 0.85f, 1.2f),
    Triple(0.71f, 0.90f, 1.5f), Triple(0.84f, 0.87f, 0.9f), Triple(0.48f, 0.20f, 1.0f),
    Triple(0.23f, 0.37f, 1.1f), Triple(0.68f, 0.40f, 0.8f), Triple(0.32f, 0.68f, 1.3f),
    Triple(0.63f, 0.71f, 0.9f), Triple(0.15f, 0.52f, 1.0f), Triple(0.88f, 0.63f, 1.2f)
)

/**
 * Mauve-purple cosmic backdrop matching the New UI specification:
 * - Vertical gradient (#2F1742 -> #211033 -> #150924)
 * - Soft radial mauve glow & warm #FFE4C1 starry specks
 * - Left & right pill-shaped Egyptian hieroglyphic pilasters in #FFD18A
 */
fun DrawScope.drawPharaonicCosmicBackdrop(
    includeHieroglyphicPilasters: Boolean = true,
    includeGoldTopAura: Boolean = true
) {
    // 1. Central mauve nebula luminosity
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x3D823E78),
                Color(0x1C4A2259),
                Color.Transparent
            ),
            center = Offset(size.width * 0.5f, size.height * 0.36f),
            radius = size.maxDimension * 0.62f
        ),
        center = Offset(size.width * 0.5f, size.height * 0.36f),
        radius = size.maxDimension * 0.62f
    )

    // 2. Warm top golden ambiance behind the curved top arch
    if (includeGoldTopAura) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    GoldAccent.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.04f),
                radius = size.maxDimension * 0.42f
            ),
            center = Offset(size.width * 0.5f, size.height * 0.04f),
            radius = size.maxDimension * 0.42f
        )
    }

    // 3. Warm #FFE4C1 starry specks matching mainBgCanvas
    MAIN_STAR_COORDS.forEachIndexed { idx, (fx, fy, rScale) ->
        val alpha = 0.10f + (idx % 5) * 0.075f
        drawCircle(
            color = Color(0xFFFFE4C1).copy(alpha = alpha),
            radius = (rScale * 1.15f).dp.toPx(),
            center = Offset(size.width * fx, size.height * fy)
        )
    }

    // 4. Left & right rounded-pill Egyptian hieroglyphic side pillars
    if (includeHieroglyphicPilasters && size.width > 0f && size.height > 0f) {
        val insetSide = 5.dp.toPx()
        val pillarWidth = 22.dp.toPx()
        val topY = 98.dp.toPx().coerceAtMost(size.height * 0.16f)
        val bottomY = (size.height - 84.dp.toPx()).coerceAtLeast(topY + 120f)
        val pillarHeight = (bottomY - topY).coerceAtLeast(80f)
        val corner = CornerRadius(pillarWidth / 2f, pillarWidth / 2f)

        val pillarFillBrush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                GoldAccent.copy(alpha = 0.06f),
                GoldAccent.copy(alpha = 0.06f),
                Color.Transparent
            ),
            startY = topY,
            endY = bottomY
        )
        val pillarBorderBrush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                GoldAccent.copy(alpha = 0.28f),
                GoldAccent.copy(alpha = 0.28f),
                Color.Transparent
            ),
            startY = topY,
            endY = bottomY
        )

        val glyphStroke = Stroke(width = 1.25.dp.toPx())
        val glyphGlowStroke = Stroke(width = 2.4.dp.toPx())

        listOf(true to insetSide, false to (size.width - insetSide - pillarWidth)).forEach { (isLeft, x) ->
            // Pill container fill & soft golden hairline border
            drawRoundRect(
                brush = pillarFillBrush,
                topLeft = Offset(x, topY),
                size = Size(pillarWidth, pillarHeight),
                cornerRadius = corner
            )
            drawRoundRect(
                brush = pillarBorderBrush,
                topLeft = Offset(x, topY),
                size = Size(pillarWidth, pillarHeight),
                cornerRadius = corner,
                style = Stroke(width = 1.dp.toPx())
            )

            // Vertical registers of golden hieroglyphs inside the side pill
            val cx = x + pillarWidth * 0.5f
            val step = 28.dp.toPx()
            var y = topY + 20.dp.toPx()
            var glyphIdx = if (isLeft) 0 else 3
            while (y < bottomY - 16.dp.toPx()) {
                val distFromEdges = minOf(y - topY, bottomY - y) / (pillarHeight * 0.18f)
                val edgeFade = distFromEdges.coerceIn(0.15f, 1f)
                val glyphColor = GoldAccent.copy(alpha = 0.45f * edgeFade)
                val glowColor = GoldAccent.copy(alpha = 0.14f * edgeFade)

                drawHieroglyphSymbol(
                    type = glyphIdx % 8,
                    cx = cx,
                    cy = y,
                    unit = pillarWidth * 0.24f,
                    color = glyphColor,
                    glowColor = glowColor,
                    stroke = glyphStroke,
                    glowStroke = glyphGlowStroke
                )
                y += step
                glyphIdx++
            }
        }
    }
}

private fun DrawScope.drawHieroglyphSymbol(
    type: Int,
    cx: Float,
    cy: Float,
    unit: Float,
    color: Color,
    glowColor: Color,
    stroke: Stroke,
    glowStroke: Stroke
) {
    when (type) {
        0 -> {
            // Ankh (☥)
            drawCircle(color = glowColor, radius = unit * 0.55f, center = Offset(cx, cy - unit * 0.65f), style = glowStroke)
            drawCircle(color = color, radius = unit * 0.55f, center = Offset(cx, cy - unit * 0.65f), style = stroke)
            drawLine(color = color, start = Offset(cx, cy - unit * 0.10f), end = Offset(cx, cy + unit * 1.15f), strokeWidth = stroke.width)
            drawLine(color = color, start = Offset(cx - unit * 0.78f, cy + unit * 0.22f), end = Offset(cx + unit * 0.78f, cy + unit * 0.22f), strokeWidth = stroke.width)
        }
        1 -> {
            // Eye of Horus (𓂀)
            val eye = Path().apply {
                moveTo(cx - unit * 1.0f, cy)
                quadraticTo(cx, cy - unit * 0.68f, cx + unit * 1.0f, cy)
                quadraticTo(cx, cy + unit * 0.58f, cx - unit * 1.0f, cy)
            }
            drawPath(eye, color = glowColor, style = glowStroke)
            drawPath(eye, color = color, style = stroke)
            drawCircle(color = color, radius = unit * 0.26f, center = Offset(cx, cy))
            drawLine(color = color, start = Offset(cx, cy + unit * 0.35f), end = Offset(cx - unit * 0.35f, cy + unit * 0.95f), strokeWidth = stroke.width)
        }
        2 -> {
            // Royal Cartouche (𓍷)
            val w = unit * 1.15f
            val h = unit * 2.0f
            drawRoundRect(
                color = color,
                topLeft = Offset(cx - w / 2f, cy - h / 2f),
                size = Size(w, h),
                cornerRadius = CornerRadius(w / 2f, w / 2f),
                style = stroke
            )
            drawLine(color = color, start = Offset(cx - w * 0.68f, cy + h * 0.58f), end = Offset(cx + w * 0.68f, cy + h * 0.58f), strokeWidth = stroke.width)
            drawCircle(color = color, radius = unit * 0.18f, center = Offset(cx, cy - unit * 0.35f))
        }
        3 -> {
            // Pyramid & Solar Disk (𓉴𓇳)
            val tri = Path().apply {
                moveTo(cx, cy - unit * 0.55f)
                lineTo(cx - unit * 0.95f, cy + unit * 0.85f)
                lineTo(cx + unit * 0.95f, cy + unit * 0.85f)
                close()
            }
            drawPath(tri, color = glowColor, style = glowStroke)
            drawPath(tri, color = color, style = stroke)
            drawCircle(color = color, radius = unit * 0.24f, center = Offset(cx, cy - unit * 0.95f), style = stroke)
        }
        4 -> {
            // Djed Pillar (𓊽)
            drawLine(color = color, start = Offset(cx, cy - unit * 0.95f), end = Offset(cx, cy + unit * 0.95f), strokeWidth = stroke.width)
            listOf(-0.62f, -0.25f, 0.12f, 0.88f).forEach { yMul ->
                drawLine(color = color, start = Offset(cx - unit * 0.72f, cy + unit * yMul), end = Offset(cx + unit * 0.72f, cy + unit * yMul), strokeWidth = stroke.width)
            }
        }
        5 -> {
            // Scarab Beetle Silhouette (𓆣)
            drawOval(color = color, topLeft = Offset(cx - unit * 0.52f, cy - unit * 0.65f), size = Size(unit * 1.04f, unit * 1.35f), style = stroke)
            drawLine(color = color, start = Offset(cx, cy - unit * 0.65f), end = Offset(cx, cy + unit * 0.70f), strokeWidth = stroke.width)
            drawLine(color = color, start = Offset(cx - unit * 0.88f, cy - unit * 0.25f), end = Offset(cx - unit * 0.52f, cy - unit * 0.10f), strokeWidth = stroke.width)
            drawLine(color = color, start = Offset(cx + unit * 0.88f, cy - unit * 0.25f), end = Offset(cx + unit * 0.52f, cy - unit * 0.10f), strokeWidth = stroke.width)
            drawLine(color = color, start = Offset(cx - unit * 0.90f, cy + unit * 0.35f), end = Offset(cx - unit * 0.52f, cy + unit * 0.22f), strokeWidth = stroke.width)
            drawLine(color = color, start = Offset(cx + unit * 0.90f, cy + unit * 0.35f), end = Offset(cx + unit * 0.52f, cy + unit * 0.22f), strokeWidth = stroke.width)
        }
        6 -> {
            // Nile Water Zig-Zag (𓈖)
            val wave = Path().apply {
                moveTo(cx - unit * 0.95f, cy)
                lineTo(cx - unit * 0.48f, cy - unit * 0.42f)
                lineTo(cx, cy + unit * 0.32f)
                lineTo(cx + unit * 0.48f, cy - unit * 0.42f)
                lineTo(cx + unit * 0.95f, cy)
            }
            drawPath(wave, color = color, style = stroke)
            val wave2 = Path().apply {
                moveTo(cx - unit * 0.95f, cy + unit * 0.55f)
                lineTo(cx - unit * 0.48f, cy + unit * 0.13f)
                lineTo(cx, cy + unit * 0.85f)
                lineTo(cx + unit * 0.48f, cy + unit * 0.13f)
                lineTo(cx + unit * 0.95f, cy + unit * 0.55f)
            }
            drawPath(wave2, color = color, style = stroke)
        }
        else -> {
            // Solar Disk & Wings (𓅃 / 𓇳)
            drawCircle(color = color, radius = unit * 0.36f, center = Offset(cx, cy), style = stroke)
            drawCircle(color = color, radius = unit * 0.13f, center = Offset(cx, cy))
            drawLine(color = color, start = Offset(cx - unit * 1.05f, cy - unit * 0.18f), end = Offset(cx - unit * 0.38f, cy), strokeWidth = stroke.width)
            drawLine(color = color, start = Offset(cx + unit * 1.05f, cy - unit * 0.18f), end = Offset(cx + unit * 0.38f, cy), strokeWidth = stroke.width)
        }
    }
}

/**
 * Starry mauve nebula canvas used inside the Control Center drawer matching drawerBgCanvas in HTML.
 */
fun DrawScope.drawStarryDrawerBackdrop() {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x2E823E78), Color.Transparent),
            center = Offset(size.width * 0.35f, size.height * 0.38f),
            radius = size.width * 0.8f
        ),
        center = Offset(size.width * 0.35f, size.height * 0.38f),
        radius = size.width * 0.8f
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(GoldAccent.copy(alpha = 0.10f), Color.Transparent),
            center = Offset(size.width * 0.92f, size.height * 0.45f),
            radius = size.width * 0.45f
        ),
        center = Offset(size.width * 0.92f, size.height * 0.45f),
        radius = size.width * 0.45f
    )
    MAIN_STAR_COORDS.forEachIndexed { idx, (fx, fy, rScale) ->
        val alpha = 0.12f + (idx % 5) * 0.08f
        drawCircle(
            color = Color(0xFFFFE4C1).copy(alpha = alpha),
            radius = (rScale * 1.2f).dp.toPx(),
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CosmicTop,
                        CosmicBg,
                        CosmicDark
                    )
                )
            )
            .drawBehind {
                drawPharaonicCosmicBackdrop(
                    includeHieroglyphicPilasters = true,
                    includeGoldTopAura = true
                )
            }
    ) {
        content()
    }
}
