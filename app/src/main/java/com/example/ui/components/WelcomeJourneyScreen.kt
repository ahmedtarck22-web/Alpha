package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.TextSecondary

@Composable
fun WelcomeJourneyScreen(
    appLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    onStartJourney: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = appLanguage == AppLanguage.ARABIC

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF17082D), CosmicBg, CosmicDark)
                )
            )
            .drawBehind {
                drawPharaonicCosmicBackdrop(
                    includeHieroglyphicPilasters = true,
                    includeGoldTopAura = true
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("opening_logo_splash")
    ) {
        // Subtle outer royal frame border inside safe insets
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GoldAccent.copy(alpha = 0.55f),
                            GoldBorder.copy(alpha = 0.25f),
                            GoldAccent.copy(alpha = 0.55f)
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 22.dp, vertical = 24.dp)
        ) {
            // Top-end compact language toggle pill
            Surface(
                shape = RoundedCornerShape(50),
                color = CosmicCard.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, GoldBorder),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable { onToggleLanguage() }
                    .testTag("welcome_language_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = if (isArabic) "تغيير اللغة" else "Switch Language",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isArabic) "EN / عربي" else "عربي / EN",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GoldGlow
                    )
                }
            }

            // Center Welcome Typography + Golden "Start the Journey / ابدأ الرحلة!" Action Pill
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(GoldAccent.copy(alpha = 0.26f), CosmicCard.copy(alpha = 0.88f))
                            )
                        )
                        .border(1.2.dp, GoldAccent.copy(alpha = 0.7f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GoldGlow,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isArabic) "ابدأ الرحلة\nمع جو ألفا" else "Start the Journey\nwith Go Alpha",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    lineHeight = 42.sp,
                    color = GoldGlow,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isArabic) {
                        "مساعدك الذكي الموثق من داخل كتبك ومناهجك"
                    } else {
                        "Your intelligent learning companion grounded in your books"
                    },
                    fontFamily = CairoFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(34.dp))

                // Golden 3D Pill Button matching "ابدأ الرحلة!"
                Box(
                    modifier = Modifier
                        .widthIn(min = 220.dp, max = 290.dp)
                        .fillMaxWidth(0.78f)
                        .heightIn(min = 56.dp)
                        .shadow(
                            elevation = 18.dp,
                            shape = RoundedCornerShape(50),
                            ambientColor = GoldAccent,
                            spotColor = GoldAccent
                        )
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFD99E32),
                                    Color(0xFFF6CF6B),
                                    GoldGlow,
                                    RoyalGoldDark
                                )
                            )
                        )
                        .border(
                            width = 1.4.dp,
                            color = Color(0xFFFFEBA8),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { onStartJourney() }
                        .padding(horizontal = 28.dp, vertical = 14.dp)
                        .testTag("start_journey_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isArabic) "ابدأ الرحلة!" else "Start the Journey!",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = Color(0xFF200C34),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Alpha Go",
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = GoldAccent
                )
            }
        }
    }
}
