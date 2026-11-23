package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.data.model.ChatSession
import com.example.data.model.UserProfile
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.CosmicLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldBorderSubtle
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

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
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = TextTertiary
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

    val groupedHistory = remember(sessions, appLanguage) {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        val todayHeader = if (isArabic) "اليوم" else "Today"
        val yesterdayHeader = if (isArabic) "أمس" else "Yesterday"
        val last7DaysHeader = if (isArabic) "آخر 7 أيام" else "Previous 7 Days"
        val olderHeader = if (isArabic) "المحادثات السابقة" else "Older"

        val map = linkedMapOf<String, MutableList<ChatSession>>()
        map[todayHeader] = mutableListOf()
        map[yesterdayHeader] = mutableListOf()
        map[last7DaysHeader] = mutableListOf()
        map[olderHeader] = mutableListOf()

        for (session in sessions.distinctBy { it.id }) {
            val diff = now - session.updatedAt
            when {
                diff < oneDay -> map[todayHeader]?.add(session)
                diff < 2 * oneDay -> map[yesterdayHeader]?.add(session)
                diff < 7 * oneDay -> map[last7DaysHeader]?.add(session)
                else -> map[olderHeader]?.add(session)
            }
        }
        map.filterValues { it.isNotEmpty() }
    }

    val drawerListState = rememberLazyListState()

    ModalDrawerSheet(
        drawerContainerColor = CosmicBg,
        drawerContentColor = TextPrimary,
        modifier = Modifier
            .width(320.dp)
            .border(1.dp, GoldBorder)
    ) {
        LazyColumn(
            state = drawerListState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("side_menu_drawer_scroll"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. Control Center Header (Alpha Go Logo + Title + Close)
            item(key = "drawer_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.alpha_go_logo),
                            contentDescription = "Alpha Go",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, GoldAccent, RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "لوحة التحكم" else "Control Center",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = GoldAccent
                            )
                            Text(
                                text = if (isArabic) "جو ألفا • Alpha Go" else "Go Alpha • Alpha Go",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = TextTertiary
                            )
                        }
                    }

                    IconButton(
                        onClick = onCloseDrawer,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("drawer_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isArabic) "إغلاق" else "Close",
                            tint = GoldAccent
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = GoldBorderSubtle)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 2. Language Switcher Pill Bar (Matching HTML Prototype)
            item(key = "drawer_language_bar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CosmicCard)
                        .border(1.dp, GoldBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "اللغة / Language" else "Language",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CosmicDark)
                            .border(1.dp, GoldBorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { onToggleLanguage() }
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (!isArabic) GoldAccent else Color.Transparent)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "EN",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (!isArabic) CosmicBg else TextTertiary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (isArabic) GoldAccent else Color.Transparent)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "عربي",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isArabic) CosmicBg else TextTertiary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 3. "+ New chat" Gold Pill Action
            item(key = "drawer_new_chat") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CosmicCard)
                        .border(1.dp, GoldBorder, RoundedCornerShape(24.dp))
                        .clickable {
                            onCloseDrawer()
                            onSelectTab(AlphaGoTab.HOME)
                            onNewChatClick()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("drawer_new_chat_button"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AddComment,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isArabic) "محادثة جديدة" else "New chat",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 4. Control Center Core Sections
            item(key = "drawer_control_center_items") {
                DrawerMenuItem(
                    icon = Icons.Default.Person,
                    title = if (isArabic) "حسابي الشخصي" else "My Account",
                    subtitle = if (userProfile.isSignedIn) userProfile.email ?: userProfile.displayName else (if (isArabic) "تسجيل الدخول بحساب Google" else "Sign in with Google"),
                    onClick = {
                        onCloseDrawer()
                        onOpenAuthDialog()
                    },
                    modifier = Modifier.testTag("drawer_my_account_item")
                )

                DrawerMenuItem(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = if (isArabic) "المنهج الدراسي والمكتبة" else "Curriculum & Library",
                    subtitle = if (isArabic) "الكتب والملخصات الرقمية" else "Books & digital summaries",
                    onClick = {
                        onCloseDrawer()
                        onSelectTab(AlphaGoTab.LIBRARY)
                    },
                    modifier = Modifier.testTag("drawer_curriculum_item")
                )

                DrawerMenuItem(
                    icon = Icons.Default.EmojiEvents,
                    title = if (isArabic) "الإنجازات والجوائز" else "Achievements",
                    subtitle = if (isArabic) "التحديات والاختبارات التفاعلية" else "Interactive quizzes & challenges",
                    onClick = {
                        onCloseDrawer()
                        onSelectTab(AlphaGoTab.QUIZZES)
                    },
                    trailing = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldAccent.copy(alpha = 0.16f))
                                .border(1.dp, GoldBorderSubtle, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$quizXpScore XP",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = GoldGlow
                            )
                        }
                    },
                    modifier = Modifier.testTag("drawer_achievements_item")
                )

                DrawerMenuItem(
                    icon = Icons.Default.Phone,
                    title = if (isArabic) "مكالمة صوتية مباشرة (Live Call)" else "Go Alpha Live Call",
                    subtitle = if (isArabic) "حوار صوتي فوري من كتب المكتبة" else "Real-time voice study from Library books",
                    onClick = {
                        onCloseDrawer()
                        onVoiceCallClick()
                    },
                    modifier = Modifier.testTag("drawer_voice_call_item")
                )

                DrawerMenuItem(
                    icon = Icons.Default.Key,
                    title = if (isArabic) "مفتاح API الشخصي" else "Personal API Key",
                    subtitle = if (isArabic) "إدارة مفتاح Gemini AI Studio" else "Manage Gemini AI Studio key",
                    onClick = {
                        onCloseDrawer()
                        onOpenApiKeyDialog()
                    },
                    modifier = Modifier.testTag("drawer_api_key_item")
                )
            }

            // 7. Recent Chats History
            item(key = "drawer_recent_chats_header") {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = GoldBorderSubtle)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "المحادثات الأخيرة" else "Recent Chats",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GoldAccent,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )

                    if (sessions.isNotEmpty()) {
                        IconButton(
                            onClick = onClearAllChats,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = AppStrings.clearAllChats(appLanguage),
                                tint = TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (groupedHistory.isEmpty()) {
                item(key = "drawer_empty_history") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isArabic) "لا توجد محادثات سابقة بعد" else "No recent chats yet",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = TextTertiary
                        )
                    }
                }
            } else {
                groupedHistory.forEach { (header, itemsList) ->
                    item(key = "history_header_$header") {
                        Text(
                            text = header,
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextTertiary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 6.dp)
                        )
                    }

                    items(itemsList, key = { "session_${it.id}" }) { session ->
                        val isSelected = session.id == currentSessionId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) CosmicLight else Color.Transparent)
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) GoldBorderSubtle else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    onCloseDrawer()
                                    onSelectTab(AlphaGoTab.HOME)
                                    onSelectSession(session.id)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = if (isSelected) GoldAccent else TextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = session.title.take(28),
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = if (isSelected) GoldGlow else TextSecondary,
                                    maxLines = 1
                                )
                            }

                            IconButton(
                                onClick = { onDeleteSession(session.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = if (isArabic) "حذف" else "Delete",
                                    tint = TextTertiary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 8. Profile Footer Card & Alpha Go Brand
            item(key = "drawer_profile_footer") {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = GoldBorderSubtle)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CosmicCard)
                        .border(1.dp, GoldBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            onCloseDrawer()
                            onOpenAuthDialog()
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAccountAvatar(
                        userProfile = userProfile,
                        hasApiKey = hasPersonalApiKey,
                        size = 40.dp,
                        contentDescription = userProfile.displayName
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile.displayName,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = when {
                                userProfile.isSignedIn -> {
                                    userProfile.email ?: (if (isArabic) "حساب متصل" else "Connected account")
                                }
                                hasPersonalApiKey -> {
                                    if (isArabic) "متصل عبر مفتاح API الشخصي" else "Connected via Personal API Key"
                                }
                                else -> {
                                    if (isArabic) "تسجيل الدخول بحساب Google" else "Sign in with Google"
                                }
                            },
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = if (userProfile.isSignedIn || hasPersonalApiKey) GoldAccent else TextTertiary,
                            maxLines = 1
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (userProfile.isSignedIn || hasPersonalApiKey) GoldAccent else TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isArabic) "جو ألفا • Alpha Go" else "Go Alpha • Alpha Go",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GoldAccent.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
