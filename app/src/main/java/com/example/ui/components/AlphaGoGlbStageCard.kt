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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.LiquidGlassBottom
import com.example.ui.theme.LiquidGlassTop
import com.example.ui.theme.RoyalGoldDark

/**
 * Hero AI Mascot & Grade Selection Card matching the New UI `.liquid-glass` card:
 * - Mauve liquid-glass frosted surface with luminous #FFD18A border & top white-gold specular line
 * - Pill badge ("مساعدك الذكي للتعلم ✨") + metallic gold heading ("أهلاً بك! في أي سنة دراسية أنت؟")
 * - Floating 3D Explorer mascot over a soft #FFD18A radial spotlight
 * - 3 pill-shaped grade selectors with active `.pill-active` mauve-gold highlight
 * - Selected curriculum hint below
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

    val cardShape = RoundedCornerShape(28.dp)
    val goldMetallicTextBrush = Brush.linearGradient(
        colors = listOf(
            GoldAccent,
            GoldAccent,
            GoldHighlight,
            GoldAccent,
            RoyalGoldDark
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .shadow(
                elevation = 24.dp,
                shape = cardShape,
                ambientColor = GoldAccent.copy(alpha = 0.42f),
                spotColor = GoldAccent.copy(alpha = 0.48f)
            )
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        LiquidGlassTop,
                        LiquidGlassBottom
                    )
                )
            )
            .border(
                width = 1.8.dp,
                color = GoldAccent.copy(alpha = 0.85f),
                shape = cardShape
            )
            .drawBehind {
                // Soft inner glass highlight streak on left/top edge matching .liquid-glass
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xE6FFFAEB),
                            Color.Transparent
                        )
                    ),
                    start = Offset(size.width * 0.12f, 1.5.dp.toPx()),
                    end = Offset(size.width * 0.88f, 1.5.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
            .testTag("home_glb_slot")
    ) {
        // Subtle watermark retaining testTag cleanly
        Image(
            painter = painterResource(id = R.drawable.alpha_go_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(12.dp)
                .alpha(0.05f)
                .testTag("alpha_go_home_watermark"),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // RTL in Arabic: Copy on Right, Explorer on Left (matches dir="rtl" in HTML)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(GoldAccent.copy(alpha = 0.15f))
                            .border(1.dp, GoldAccent.copy(alpha = 0.50f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isArabic) "مساعدك الذكي للتعلم ✨" else "Your Smart Learning Assistant ✨",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isArabic) "أهلاً بك! في أي\nسنة دراسية أنت؟" else "Welcome!\nWhich grade are you in?",
                        style = TextStyle(
                            brush = goldMetallicTextBrush,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.5.sp,
                            lineHeight = 27.sp
                        )
                    )
                }

                // Explorer Mascot with soft golden spotlight
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        GoldAccent.copy(alpha = 0.22f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.minDimension * 0.52f
                                ),
                                center = center,
                                radius = size.minDimension * 0.52f
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.alpha_go_explorer_clean),
                        contentDescription = if (isArabic) "شخصية جو ألفا ثلاثية الأبعاد" else "Go Alpha 3D Explorer",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Grade Selection Pills matching HTML .grade-pill & .pill-active
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                grades.forEachIndexed { idx, gradeLabel ->
                    val isSelected = selectedGrade == gradeLabel
                    val pillShape = RoundedCornerShape(50)
                    val pillBrush = if (isSelected) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0x6BBA689E),
                                Color(0xCC4C245C)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x99482357),
                                Color(0x99482357)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .then(
                                if (isSelected) {
                                    Modifier.shadow(14.dp, pillShape, ambientColor = GoldAccent, spotColor = GoldAccent)
                                } else {
                                    Modifier
                                }
                            )
                            .clip(pillShape)
                            .background(pillBrush)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.4.dp,
                                color = if (isSelected) GoldAccent else GoldAccent.copy(alpha = 0.70f),
                                shape = pillShape
                            )
                            .clickable { onSelectGrade(gradeLabel) }
                            .padding(horizontal = 6.dp, vertical = 9.dp)
                            .testTag("grade_chip_${idx + 1}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = gradeLabel,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = GoldAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (selectedGrade.isNotBlank()) {
                    if (isArabic) "تم اختيار منهج: $selectedGrade ✅" else "Selected curriculum: $selectedGrade ✅"
                } else {
                    if (isArabic) "سيتم عرض منهجك المختار" else "Your selected curriculum will appear here"
                },
                fontFamily = CairoFontFamily,
                fontWeight = if (selectedGrade.isNotBlank()) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
                color = GoldAccent.copy(alpha = if (selectedGrade.isNotBlank()) 1f else 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}
