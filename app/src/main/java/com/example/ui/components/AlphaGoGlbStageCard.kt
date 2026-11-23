package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.CosmicLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldBorderSubtle
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * Exact frosted golden-bordered hero card from the UI reference, featuring a dedicated left
 * 3D GLB character stage and interactive grade selection pills on the right.
 */
@Composable
fun AlphaGoGlbStageCard(
    appLanguage: AppLanguage,
    selectedGrade: String = "",
    onSelectGrade: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isArabic = appLanguage == AppLanguage.ARABIC
    val grades = if (isArabic) {
        listOf("أولى إعدادي", "تانية إعدادي", "تالتة إعدادي")
    } else {
        listOf("Prep 1", "Prep 2", "Prep 3")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = GoldGlow.copy(alpha = 0.55f),
                spotColor = GoldAccent.copy(alpha = 0.65f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CosmicCard.copy(alpha = 0.86f),
                        Color(0xE6180B2E),
                        CosmicDark.copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                width = 1.4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GoldGlow.copy(alpha = 0.9f),
                        GoldBorder,
                        RoyalGoldDark.copy(alpha = 0.7f),
                        GoldGlow.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .drawBehind {
                // Warm golden aura around left GLB character stage
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldAccent.copy(alpha = 0.24f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.22f, size.height * 0.44f),
                        radius = size.width * 0.30f
                    ),
                    center = Offset(size.width * 0.22f, size.height * 0.44f),
                    radius = size.width * 0.30f
                )
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldGlow.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.22f, size.height * 0.78f),
                        radius = size.width * 0.2f
                    ),
                    topLeft = Offset(size.width * 0.06f, size.height * 0.70f),
                    size = Size(size.width * 0.32f, size.height * 0.16f)
                )
            }
            .padding(horizontal = 14.dp, vertical = 16.dp)
            .testTag("home_glb_slot")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Left: GLB 3D Character Stage | Right: Welcoming Grade Question
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dedicated Left GLB Character Viewport Area
                    Box(
                        modifier = Modifier
                            .weight(0.38f)
                            .height(144.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(CosmicDark.copy(alpha = 0.45f))
                            .border(1.dp, GoldBorderSubtle, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(CosmicLight.copy(alpha = 0.45f))
                                    .border(1.dp, GoldBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.alpha_go_logo),
                                    contentDescription = "Alpha Go",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .alpha(0.34f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .testTag("alpha_go_home_watermark"),
                                    contentScale = ContentScale.Crop
                                )
                                Icon(
                                    imageVector = Icons.Default.ViewInAr,
                                    contentDescription = null,
                                    tint = GoldGlow.copy(alpha = 0.85f),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "GLB 3D",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = GoldAccent.copy(alpha = 0.85f)
                            )
                        }
                    }

                    // Right Welcoming Copy matching reference image
                    Column(
                        modifier = Modifier
                            .weight(0.62f)
                            .padding(end = 4.dp),
                        horizontalAlignment = if (isArabic) Alignment.End else Alignment.Start
                    ) {
                        Text(
                            text = if (isArabic) "أهلاً بك! في أي سنة دراسية أنت؟" else "Welcome! Which grade are you in?",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            lineHeight = 25.sp,
                            color = TextPrimary,
                            textAlign = if (isArabic) TextAlign.End else TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isArabic) "منطقة نموذج GLB التفاعلي" else "Interactive GLB Stage",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = GoldAccent.copy(alpha = 0.82f),
                            textAlign = if (isArabic) TextAlign.End else TextAlign.Start
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Luminous Golden-Bordered Grade Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                grades.forEachIndexed { idx, gradeLabel ->
                    val isSelected = selectedGrade == gradeLabel
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) {
                            GoldAccent.copy(alpha = 0.26f)
                        } else {
                            CosmicLight.copy(alpha = 0.55f)
                        },
                        border = BorderStroke(
                            width = if (isSelected) 1.4.dp else 1.dp,
                            color = if (isSelected) GoldGlow else GoldBorder
                        ),
                        shadowElevation = if (isSelected) 8.dp else 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 42.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectGrade(gradeLabel) }
                            .testTag("grade_chip_${idx + 1}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = gradeLabel,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) GoldGlow else GoldAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (selectedGrade.isNotBlank()) {
                    if (isArabic) "منهجك المختار: $selectedGrade" else "Selected curriculum: $selectedGrade"
                } else {
                    if (isArabic) "سيتم عرض منهجك المختار" else "Your selected curriculum will appear here"
                },
                fontFamily = CairoFontFamily,
                fontSize = 12.sp,
                color = if (selectedGrade.isNotBlank()) GoldGlow else TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
