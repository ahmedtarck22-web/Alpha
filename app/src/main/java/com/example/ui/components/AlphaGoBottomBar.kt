package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldGlow

enum class AlphaGoTab(val testTag: String) {
    HOME("nav_home"),
    QUIZZES("nav_quizzes"),
    VIDEOS("nav_videos"),
    LIBRARY("nav_library")
}

private data class BottomNavSpec(
    val tab: AlphaGoTab,
    val icon: ImageVector,
    val labelAr: String,
    val labelEn: String
)

private val ALPHA_GO_TABS = listOf(
    BottomNavSpec(AlphaGoTab.HOME, Icons.Default.Home, "الرئيسية", "Home"),
    BottomNavSpec(AlphaGoTab.QUIZZES, Icons.Default.Quiz, "اختبارات", "Quizzes"),
    BottomNavSpec(AlphaGoTab.VIDEOS, Icons.Default.PlayArrow, "فيديوهات", "Videos"),
    BottomNavSpec(AlphaGoTab.LIBRARY, Icons.AutoMirrored.Filled.MenuBook, "المكتبة", "Library")
)

@Composable
fun AlphaGoBottomBar(
    selectedTab: AlphaGoTab,
    appLanguage: AppLanguage,
    onSelectTab: (AlphaGoTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = appLanguage == AppLanguage.ARABIC

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CosmicCard.copy(alpha = 0.96f),
                        Color(0xFF140826),
                        CosmicBg
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.4.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GoldAccent.copy(alpha = 0.85f),
                            GoldGlow,
                            GoldAccent.copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ALPHA_GO_TABS.forEach { item ->
                val isSelected = selectedTab == item.tab
                val tint = if (isSelected) GoldGlow else Color(0xFF998BB5)
                val label = if (isArabic) item.labelAr else item.labelEn

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .drawBehind {
                            if (isSelected) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            GoldGlow.copy(alpha = 0.30f),
                                            GoldAccent.copy(alpha = 0.10f),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width * 0.5f, size.height * 0.25f),
                                        radius = size.width * 0.65f
                                    ),
                                    center = Offset(size.width * 0.5f, size.height * 0.25f),
                                    radius = size.width * 0.65f
                                )
                            }
                        }
                        .clickable { onSelectTab(item.tab) }
                        .padding(vertical = 6.dp)
                        .testTag(item.tab.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = label,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontFamily = CairoFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = tint,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
