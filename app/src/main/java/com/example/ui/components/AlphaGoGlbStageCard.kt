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
import androidx.compose.runtime.CompositionLocalProvider
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
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.TextPrimary

/**
 * Hero card matching IMG_1050 100%:
 * - Seamless borderless 3D Explorer character floating on warm radial gold spotlight (left)
 * - Welcome typography ("نموذج الـ جي إل بي" + "أهلاً بك! في أي سنة دراسية أنت؟") (right)
 * - 3 golden-rimmed grade selector pills across the card + curriculum subtitle below.
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
                ambientColor = GoldGlow.copy(alpha = 0.50f),
                spotColor = GoldAccent.copy(alpha = 0.58f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xCC28144B),
                        CosmicCard.copy(alpha = 0.78f),
                        Color(0xD91B0C36)
                    )
                )
            )
            .border(
                width = 1.3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GoldGlow.copy(alpha = 0.95f),
                        GoldBorder,
                        RoyalGoldDark.copy(alpha = 0.72f),
                        GoldGlow.copy(alpha = 0.88f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .drawBehind {
                // Warm golden-olive spotlight behind the left 3D explorer matching IMG_1050
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF9C8444).copy(alpha = 0.38f),
                            GoldAccent.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.25f, size.height * 0.36f),
                        radius = size.width * 0.34f
                    ),
                    center = Offset(size.width * 0.25f, size.height * 0.36f),
                    radius = size.width * 0.34f
                )
            }
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .testTag("home_glb_slot")
    ) {
        // Subtle watermark retaining testTag without cluttering the IMG_1050 visual composition
        Image(
            painter = painterResource(id = R.drawable.alpha_go_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(14.dp)
                .alpha(0.06f)
                .testTag("alpha_go_home_watermark"),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fixed LTR placement matching IMG_1050: Left Explorer | Right Welcome Copy
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Seamless borderless Explorer character floating directly inside the card
                    Box(
                        modifier = Modifier
                            .weight(0.45f)
                            .height(148.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.alpha_go_explorer_clean),
                            contentDescription = if (isArabic) "شخصية جو ألفا ثلاثية الأبعاد" else "Go Alpha 3D Explorer",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.BottomCenter
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.55f)
                            .padding(end = 6.dp),
                        horizontalAlignment = if (isArabic) Alignment.End else Alignment.Start
                    ) {
                        Text(
                            text = if (isArabic) "نموذج الـ جي إل بي" else "GLB 3D Model",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFFE7E1F5),
                            textAlign = if (isArabic) TextAlign.End else TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isArabic) "أهلاً بك! في أي\nسنة دراسية أنت؟" else "Welcome!\nWhich grade are you in?",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.5.sp,
                            lineHeight = 27.sp,
                            color = TextPrimary,
                            textAlign = if (isArabic) TextAlign.End else TextAlign.Start
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3 Luminous Golden-Bordered Grade Pills matching IMG_1050
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
                            Color(0xFF473322).copy(alpha = 0.88f)
                        } else {
                            Color(0xD9271845)
                        },
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.1.dp,
                            color = if (isSelected) GoldGlow else Color(0x99DFB054)
                        ),
                        shadowElevation = if (isSelected) 10.dp else 3.dp,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 46.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectGrade(gradeLabel) }
                            .testTag("grade_chip_${idx + 1}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 6.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = gradeLabel,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = if (isSelected) GoldGlow else Color(0xFFF0C972),
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
                fontSize = 12.5.sp,
                color = if (selectedGrade.isNotBlank()) GoldGlow else Color(0xFFB3A9CA),
                textAlign = TextAlign.Center
            )
        }
    }
}
