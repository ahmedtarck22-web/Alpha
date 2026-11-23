package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ChatSession
import com.example.data.model.UserProfile
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.TextPrimary

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(12.dp, CircleShape, ambientColor = GoldGlow, spotColor = GoldAccent)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GoldAccent.copy(alpha = 0.24f),
                                Color(0xFF281648)
                            )
                        )
                    )
                    .border(1.2.dp, GoldGlow.copy(alpha = 0.62f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GoldGlow,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TextPrimary
            )
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
fun SideMenuDrawer(
    userProfile: UserProfile,
    hasPersonalApiKey: Boolean = false,
    sessions: List<ChatSession>,
    currentSessionId: String,
    selectedGem: String = "GEMINI_DEFAULT",
    onSelectGem: (String) -> Unit = {},
    appLanguage: AppLanguage = AppLanguage.ARABIC,
    onToggleLanguage: () -> Unit,
    onSelectSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onClearAllChats: () -> Unit,
    isSearchGroundingEnabled: Boolean,
    onToggleSearchGrounding: () -> Unit,
    onOpenAuthDialog: () -> Unit,
    onSignOut: () -> Unit = {},
    onOpenContactDialog: () -> Unit = {},
    onOpenSettingsDialog: () -> Unit = {},
    onOpenApiKeyDialog: () -> Unit,
    onOpenRagDialog: () -> Unit,
    onOpen3DArtifactsDialog: () -> Unit,
    onNewChatClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onCloseDrawer: () -> Unit,
    quizXpScore: Int = 2450,
    onSelectTab: (AlphaGoTab) -> Unit = {}
) {
    val isArabic = appLanguage == AppLanguage.ARABIC
    val drawerListState = rememberLazyListState()

    ModalDrawerSheet(
        drawerContainerColor = CosmicBg,
        drawerContentColor = TextPrimary,
        drawerShape = RoundedCornerShape(topEnd = 36.dp, bottomEnd = 36.dp),
        modifier = Modifier
            .width(305.dp)
            .shadow(28.dp, RoundedCornerShape(topEnd = 36.dp, bottomEnd = 36.dp), ambientColor = GoldGlow, spotColor = GoldAccent)
            .border(
                width = 1.8.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GoldAccent.copy(alpha = 0.22f),
                        GoldGlow.copy(alpha = 0.96f),
                        Color(0xFFFFEBA8),
                        GoldAccent.copy(alpha = 0.85f),
                        GoldAccent.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(topEnd = 36.dp, bottomEnd = 36.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1F0C3C),
                            Color(0xFF17082E),
                            CosmicDark
                        )
                    )
                )
                .drawBehind {
                    drawStarryDrawerBackdrop()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                // Header: لوحة التحكم + (X) matching IMG_1050
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "لوحة التحكم" else "Control Center",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 23.sp,
                        color = GoldGlow
                    )

                    IconButton(
                        onClick = onCloseDrawer,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("drawer_close_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CosmicCard.copy(alpha = 0.82f))
                                .border(1.dp, GoldBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isArabic) "إغلاق" else "Close",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Main 5 items matching IMG_1050 spacing & golden medallions
                LazyColumn(
                    state = drawerListState,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("side_menu_drawer_scroll"),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(key = "item_account") {
                        DrawerMenuItem(
                            icon = Icons.Default.Person,
                            title = if (isArabic) "حسابي" else "My Account",
                            onClick = {
                                onCloseDrawer()
                                onOpenAuthDialog()
                            },
                            modifier = Modifier.testTag("drawer_my_account_item")
                        )
                    }

                    item(key = "item_curriculum") {
                        DrawerMenuItem(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = if (isArabic) "المنهج الدراسي" else "Curriculum",
                            onClick = {
                                onCloseDrawer()
                                onSelectTab(AlphaGoTab.LIBRARY)
                            },
                            modifier = Modifier.testTag("drawer_curriculum_item")
                        )
                    }

                    item(key = "item_achievements") {
                        DrawerMenuItem(
                            icon = Icons.Default.EmojiEvents,
                            title = if (isArabic) "الإنجازات والجوائز" else "Achievements",
                            onClick = {
                                onCloseDrawer()
                                onSelectTab(AlphaGoTab.QUIZZES)
                            },
                            modifier = Modifier.testTag("drawer_achievements_item")
                        )
                    }

                    item(key = "item_contact") {
                        DrawerMenuItem(
                            icon = Icons.Default.Sms,
                            title = if (isArabic) "تواصل معنا" else "Contact Us",
                            onClick = {
                                onCloseDrawer()
                                onOpenContactDialog()
                            },
                            modifier = Modifier.testTag("drawer_contact_item")
                        )
                    }

                    item(key = "item_settings") {
                        DrawerMenuItem(
                            icon = Icons.Default.Settings,
                            title = if (isArabic) "الإعدادات" else "Settings",
                            onClick = {
                                onCloseDrawer()
                                onOpenSettingsDialog()
                            },
                            modifier = Modifier.testTag("drawer_settings_item")
                        )
                    }
                }

                // Bottom Pinned Sign Out / Sign In Action matching IMG_1050
                DrawerMenuItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = if (userProfile.isSignedIn) {
                        if (isArabic) "تسجيل الخروج" else "Sign Out"
                    } else {
                        if (isArabic) "تسجيل الدخول" else "Sign In"
                    },
                    onClick = {
                        onCloseDrawer()
                        if (userProfile.isSignedIn) {
                            onSignOut()
                        } else {
                            onOpenAuthDialog()
                        }
                    },
                    modifier = Modifier.testTag("drawer_sign_out_item")
                )
            }
        }
    }
}
