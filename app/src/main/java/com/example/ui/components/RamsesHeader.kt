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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
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
                        Color(0x993D2824),
                        Color(0x7025123D),
                        Color.Transparent
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldAccent.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.34f),
                        radius = size.width * 0.38f
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.34f),
                    radius = size.width * 0.38f
                )
            }
    ) {
        // Left: [Control Center ≡ Circle] [عربي / EN Pill]
        // Center: [Main App Icon (alpha_go_logo) + Alpha Go]
        // Right: [Circular Person Sign-In Button]
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Cluster: Control Center Hamburger Circle + Language Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("menu_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(10.dp, CircleShape, ambientColor = GoldAccent, spotColor = GoldAccent)
                                .clip(CircleShape)
                                .background(Color(0xB8271447))
                                .border(1.1.dp, GoldBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = if (isArabic) "لوحة التحكم" else "Control Center",
                                tint = GoldGlow,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xB8271447),
                        border = BorderStroke(1.dp, GoldBorder),
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { onToggleLanguage() }
                            .testTag("header_language_toggle")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isArabic) "عربي / EN" else "EN / عربي",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Center: Main App Icon positioned cleanly between Control Center & Sign-In Person Circle
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
                        fontSize = 11.5.sp,
                        color = GoldGlow
                    )
                }

                // Right: Circular Person Icon Sign-In Button
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("profile_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(10.dp, CircleShape, ambientColor = GoldAccent, spotColor = GoldAccent)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            GoldAccent.copy(alpha = 0.28f),
                                            Color(0xCC271447)
                                        )
                                    )
                                )
                                .border(1.2.dp, GoldGlow.copy(alpha = 0.78f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val avatarPath = userProfile.avatarUrl
                            if (!avatarPath.isNullOrBlank()) {
                                val modelData: Any = if (avatarPath.startsWith("http")) avatarPath else File(avatarPath)
                                AsyncImage(
                                    model = modelData,
                                    contentDescription = userProfile.displayName,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = if (isArabic) "تسجيل الدخول" else "Sign In",
                                    tint = GoldGlow,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }

                        if (isActiveAccount) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                                    .border(1.dp, CosmicBg, CircleShape)
                            )
                        }
                    }
                }
            }
        }

        // Luminous tapered gold divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GoldAccent.copy(alpha = 0.75f),
                            GoldGlow,
                            GoldAccent.copy(alpha = 0.75f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
