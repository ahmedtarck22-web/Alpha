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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.UserProfile
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.TextPrimary
import java.io.File

@Composable
fun RamsesHeader(
    hasPersonalApiKey: Boolean,
    userProfile: UserProfile = UserProfile(),
    appLanguage: AppLanguage = AppLanguage.ARABIC,
    selectedModelName: String = "gemini-3.1-flash-lite",
    onModelClick: () -> Unit = {},
    onToggleLanguage: () -> Unit,
    onProfileClick: () -> Unit,
    onVoiceCallClick: () -> Unit = {},
    onOpen3DArtifactsClick: () -> Unit = {},
    is3DAvatarMode: Boolean = false,
    onToggle3DAvatarMode: () -> Unit = {},
    onApiKeyClick: () -> Unit,
    onMenuClick: () -> Unit,
    onLogoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isArabic = appLanguage == AppLanguage.ARABIC
    val isActiveAccount = userProfile.isSignedIn || hasPersonalApiKey

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF543C16),
                        Color(0xFF312021),
                        CosmicBg.copy(alpha = 0.96f)
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldAccent.copy(alpha = 0.28f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.24f),
                        radius = size.width * 0.45f
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.24f),
                    radius = size.width * 0.45f
                )
            }
    ) {
        // Fixed visual horizontal placement matching reference screenshot:
        // Left: [Menu Circle] [عربي / EN Pill]  |  Center: [Alpha Go]  |  Right: [تسجيل الدخول Pill]
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Cluster: Hamburger Circle + Language Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(10.dp, CircleShape, ambientColor = GoldAccent, spotColor = GoldAccent)
                            .clip(CircleShape)
                            .background(CosmicLight.copy(alpha = 0.85f))
                            .border(1.dp, GoldBorder, CircleShape)
                            .testTag("menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = if (isArabic) "لوحة التحكم" else "Control Center",
                            tint = GoldGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CosmicCard.copy(alpha = 0.82f),
                        border = BorderStroke(1.dp, GoldBorder),
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { onToggleLanguage() }
                            .testTag("header_language_toggle")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isArabic) "عربي / EN" else "EN / عربي",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = GoldGlow,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Center: Official Alpha Go Emblem + Label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onLogoClick() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.alpha_go_logo),
                        contentDescription = "Alpha Go",
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(14.dp, RoundedCornerShape(12.dp), ambientColor = GoldGlow, spotColor = GoldAccent)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.3.dp, GoldAccent, RoundedCornerShape(12.dp))
                            .testTag("header_alpha_go_logo"),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Alpha Go",
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = GoldGlow
                    )
                }

                // Right: Sign-In / Profile Pill Button matching "تسجيل الدخول"
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CosmicCard.copy(alpha = 0.86f),
                    border = BorderStroke(1.dp, GoldBorder),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .heightIn(min = 42.dp)
                        .widthIn(max = 138.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { onProfileClick() }
                        .testTag("profile_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (userProfile.isSignedIn) {
                                userProfile.displayName.substringBefore(" ")
                            } else {
                                if (isArabic) "تسجيل الدخول" else "Sign In"
                            },
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(GoldAccent.copy(alpha = 0.35f), CosmicLight)
                                        )
                                    )
                                    .border(1.dp, GoldAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val avatarPath = userProfile.avatarUrl
                                if (!avatarPath.isNullOrBlank()) {
                                    val modelData: Any = if (avatarPath.startsWith("http")) avatarPath else File(avatarPath)
                                    AsyncImage(
                                        model = modelData,
                                        contentDescription = userProfile.displayName,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = if (isArabic) "الحساب" else "Account",
                                        tint = GoldGlow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (isActiveAccount) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldSuccess)
                                        .border(1.dp, CosmicBg, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Luminous tapered gold divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.2.dp)
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
    }
}
