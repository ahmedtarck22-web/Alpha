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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.TextTertiary

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
            .background(CosmicCard)
    ) {
        HorizontalDivider(thickness = 1.dp, color = GoldBorder)
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
                val tint = if (isSelected) GoldAccent else TextTertiary
                val label = if (isArabic) item.labelAr else item.labelEn

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) GoldAccent.copy(alpha = 0.12f)
                            else CosmicCard
                        )
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
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
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
