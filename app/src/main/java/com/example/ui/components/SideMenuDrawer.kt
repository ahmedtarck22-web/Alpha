package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ChatSession
import com.example.data.model.UserProfile
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.RoyalGoldDark

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
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(10.dp, CircleShape, ambientColor = GoldAccent, spotColor = GoldAccent)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF49305F),
                                Color(0xFF2B1948)
                            )
                        )
                    )
                    .border(1.5.dp, GoldAccent.copy(alpha = 0.80f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = title,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp,
                    color = GoldGlow,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = GoldAccent.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun SegmentedLanguageSwitch(
    isArabic: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (isArabic) 46.dp else 2.dp,
        label = "drawer_lang_thumb_offset"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .heightIn(min = 48.dp)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF241238))
                    .border(1.4.dp, GoldAccent.copy(alpha = 0.80f), RoundedCornerShape(50))
            ) {
                // Sliding golden thumb
                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset, y = 3.dp)
                        .width(46.dp)
                        .height(28.dp)
                        .shadow(8.dp, RoundedCornerShape(50), ambientColor = GoldAccent, spotColor = GoldAccent)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(GoldAccent, GoldGlow)
                            )
                        )
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "EN",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = if (!isArabic) Color(0xFF1A0C33) else GoldAccent
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "عربي",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = if (isArabic) Color(0xFF1A0C33) else GoldAccent
                        )
                    }
                }
            }
        }
    }
}

/**
 * Control Center Drawer matching the New UI HTML `.drawer-panel` design 100%,
 * and integrating all legacy Control Center tools & chat history in the exact same medallion style.
 */
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
    val drawerShape = RoundedCornerShape(topEnd = 40.dp, bottomEnd = 40.dp)
    val goldHeadingBrush = Brush.linearGradient(
        colors = listOf(GoldAccent, GoldAccent, GoldHighlight, GoldAccent, RoyalGoldDark)
    )

    ModalDrawerSheet(
        drawerContainerColor = CosmicBg,
        drawerContentColor = GoldGlow,
        drawerShape = drawerShape,
        modifier = Modifier
            .width(314.dp)
            .shadow(28.dp, drawerShape, ambientColor = GoldAccent, spotColor = GoldAccent)
            .border(width = 2.2.dp, color = GoldAccent, shape = drawerShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF331845),
                            Color(0xFF241036),
                            Color(0xFF170A26)
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
                // Header: "لوحة التحكم" + (X) circle + bottom gold hairline matching HTML
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "لوحة التحكم" else "Control Center",
                        style = TextStyle(
                            brush = goldHeadingBrush,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 23.sp
                        )
                    )

                    IconButton(
                        onClick = onCloseDrawer,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("drawer_close_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(12.dp, CircleShape, ambientColor = GoldAccent, spotColor = GoldAccent)
                                .clip(CircleShape)
                                .background(Color(0xFF452157))
                                .border(1.6.dp, GoldAccent.copy(alpha = 0.85f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isArabic) "إغلاق" else "Close",
                                tint = GoldAccent,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = GoldAccent.copy(alpha = 0.35f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable list: 6 primary HTML items + Legacy Control Center tools & chat history in exact same style
                LazyColumn(
                    state = drawerListState,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("side_menu_drawer_scroll"),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. حسابي
                    item(key = "item_account") {
                        DrawerMenuItem(
                            icon = Icons.Default.Person,
                            title = if (isArabic) "حسابي" else "My Account",
                            subtitle = if (userProfile.isSignedIn) userProfile.displayName else null,
                            onClick = {
                                onCloseDrawer()
                                onOpenAuthDialog()
                            },
                            modifier = Modifier.testTag("drawer_my_account_item")
                        )
                    }

                    // 2. المنهج الدراسي
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

                    // 3. الإنجازات والجوائز
                    item(key = "item_achievements") {
                        DrawerMenuItem(
                            icon = Icons.Default.EmojiEvents,
                            title = if (isArabic) "الإنجازات والجوائز" else "Achievements & Rewards",
                            onClick = {
                                onCloseDrawer()
                                onSelectTab(AlphaGoTab.QUIZZES)
                            },
                            modifier = Modifier.testTag("drawer_achievements_item")
                        )
                    }

                    // 4. اللغة (Globe Icon + Segmented [EN | عربي] Switch)
                    item(key = "item_language") {
                        DrawerMenuItem(
                            icon = Icons.Default.Language,
                            title = if (isArabic) "اللغة" else "Language",
                            onClick = onToggleLanguage,
                            modifier = Modifier.testTag("drawer_language_row"),
                            trailing = {
                                SegmentedLanguageSwitch(
                                    isArabic = isArabic,
                                    onToggle = onToggleLanguage,
                                    modifier = Modifier.testTag("drawer_language_toggle")
                                )
                            }
                        )
                    }

                    // 5. تواصل معنا
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

                    // 6. الإعدادات
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

                    // Divider before Old Control Center tools styled identically in New UI style
                    item(key = "legacy_tools_divider") {
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = GoldAccent.copy(alpha = 0.30f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isArabic) "أدوات الدراسة والمحادثات" else "Study Tools & Chats",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = GoldAccent.copy(alpha = 0.78f),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    // Old Control Center: محادثة جديدة
                    item(key = "item_new_chat") {
                        DrawerMenuItem(
                            icon = Icons.Default.AddComment,
                            title = if (isArabic) "محادثة جديدة" else "New Chat",
                            onClick = {
                                onCloseDrawer()
                                onSelectTab(AlphaGoTab.HOME)
                                onNewChatClick()
                            },
                            modifier = Modifier.testTag("drawer_new_chat_item")
                        )
                    }

                    // Old Control Center: مكالمة صوتية مباشرة
                    item(key = "item_voice_call") {
                        DrawerMenuItem(
                            icon = Icons.Outlined.Phone,
                            title = if (isArabic) "مكالمة صوتية مباشرة" else "Live Voice Call",
                            onClick = {
                                onCloseDrawer()
                                onVoiceCallClick()
                            },
                            modifier = Modifier.testTag("drawer_voice_call_item")
                        )
                    }

                    // Old Control Center: البحث العميق داخل الكتب
                    item(key = "item_deep_search") {
                        DrawerMenuItem(
                            icon = Icons.Default.Search,
                            title = if (isArabic) "البحث العميق" else "Deep Book Search",
                            onClick = onToggleSearchGrounding,
                            modifier = Modifier.testTag("drawer_deep_search_item"),
                            trailing = {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (isSearchGroundingEnabled) {
                                                GoldAccent.copy(alpha = 0.22f)
                                            } else {
                                                Color(0xFF241238)
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isSearchGroundingEnabled) GoldAccent else GoldAccent.copy(alpha = 0.45f),
                                            RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isSearchGroundingEnabled) {
                                            if (isArabic) "مفعّل" else "ON"
                                        } else {
                                            if (isArabic) "متوقف" else "OFF"
                                        },
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = GoldAccent
                                    )
                                }
                            }
                        )
                    }

                    // Old Control Center: مفتاح API الشخصي
                    item(key = "item_api_key") {
                        DrawerMenuItem(
                            icon = Icons.Default.Key,
                            title = if (isArabic) "مفتاح API الشخصي" else "Personal API Key",
                            subtitle = if (hasPersonalApiKey) {
                                if (isArabic) "مفعّل" else "Active"
                            } else {
                                null
                            },
                            onClick = {
                                onCloseDrawer()
                                onOpenApiKeyDialog()
                            },
                            modifier = Modifier.testTag("drawer_api_key_item"),
                            trailing = if (hasPersonalApiKey) {
                                {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldSuccess)
                                    )
                                }
                            } else {
                                null
                            }
                        )
                    }

                    // Old Control Center: سجل المحادثات
                    if (sessions.isNotEmpty()) {
                        item(key = "recent_chats_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = null,
                                        tint = GoldAccent.copy(alpha = 0.85f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) "المحادثات السابقة" else "Recent Chats",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = GoldAccent
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFF241238))
                                        .border(1.dp, GoldAccent.copy(alpha = 0.45f), RoundedCornerShape(50))
                                        .clickable { onClearAllChats() }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                        .testTag("drawer_clear_chats_button"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) "مسح الكل" else "Clear",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        color = GoldAccent
                                    )
                                }
                            }
                        }

                        items(sessions.take(8), key = { it.id }) { session ->
                            val isActive = session.id == currentSessionId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isActive) Color(0x8C52266E) else Color(0x593E1E59)
                                    )
                                    .border(
                                        width = if (isActive) 1.4.dp else 1.dp,
                                        color = if (isActive) GoldAccent else GoldAccent.copy(alpha = 0.40f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        onCloseDrawer()
                                        onSelectTab(AlphaGoTab.HOME)
                                        onSelectSession(session.id)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = session.title.ifBlank {
                                        if (isArabic) "محادثة تعليمية" else "Study Chat"
                                    },
                                    fontFamily = CairoFontFamily,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.5.sp,
                                    color = if (isActive) GoldAccent else GoldGlow,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onDeleteSession(session.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = if (isArabic) "حذف المحادثة" else "Delete chat",
                                        tint = GoldAccent.copy(alpha = 0.72f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Pinned Sign Out / Sign In Action with top divider matching HTML
                HorizontalDivider(
                    thickness = 1.dp,
                    color = GoldAccent.copy(alpha = 0.40f)
                )
                Spacer(modifier = Modifier.height(6.dp))
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
