package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldAccent

enum class AlphaGoTab(val testTag: String) {
    HOME("nav_home"),
    QUIZZES("nav_quizzes"),
    VIDEOS("nav_videos"),
    LIBRARY("nav_library")
}

private data class BottomNavSpec(
    val tab: AlphaGoTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val labelAr: String,
    val labelEn: String
)

private val ALPHA_GO_TABS = listOf(
    BottomNavSpec(
        AlphaGoTab.HOME,
        Icons.Filled.Home,
        Icons.Outlined.Home,
        "الرئيسية",
        "Home"
    ),
    BottomNavSpec(
        AlphaGoTab.QUIZZES,
        Icons.AutoMirrored.Filled.Assignment,
        Icons.AutoMirrored.Outlined.Assignment,
        "اختبارات",
        "Quizzes"
    ),
    BottomNavSpec(
        AlphaGoTab.VIDEOS,
        Icons.Filled.SmartDisplay,
        Icons.Outlined.SmartDisplay,
        "فيديوهات",
        "Videos"
    ),
    BottomNavSpec(
        AlphaGoTab.LIBRARY,
        Icons.AutoMirrored.Filled.MenuBook,
        Icons.AutoMirrored.Outlined.MenuBook,
        "المكتبة",
        "Library"
    )
)

/**
 * Bottom Navigation Bar matching the New UI HTML specification:
 * - Mauve-purple gradient (#210F30 @ 0.75 -> #341845 @ 0.90)
 * - Top 1.5dp #FFD18A @ 0.65 hairline border
 * - Active tab icon wrapped inside a glowing rounded-12dp #FFD18A badge
 */
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
                        Color(0xBF210F30),
                        Color(0xE6341845)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(GoldAccent.copy(alpha = 0.65f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ALPHA_GO_TABS.forEach { item ->
                val isSelected = selectedTab == item.tab
                val tint = if (isSelected) GoldAccent else GoldAccent.copy(alpha = 0.85f)
                val label = if (isArabic) item.labelAr else item.labelEn
                val badgeShape = RoundedCornerShape(12.dp)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectTab(item.tab) }
                        .padding(vertical = 4.dp)
                        .testTag(item.tab.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .then(
                                if (isSelected) {
                                    Modifier.shadow(12.dp, badgeShape, ambientColor = GoldAccent, spotColor = GoldAccent)
                                } else {
                                    Modifier
                                }
                            )
                            .clip(badgeShape)
                            .background(if (isSelected) GoldAccent.copy(alpha = 0.15f) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GoldAccent.copy(alpha = 0.50f) else Color.Transparent,
                                shape = badgeShape
                            )
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = label,
                            tint = tint,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = GoldAccent,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
