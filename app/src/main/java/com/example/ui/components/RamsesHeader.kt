package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.UserProfile
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldGlow
import java.io.File

/**
 * Top App Bar matching the New UI HTML `.top-gold-arch`:
 * - Curved warm bronze-gold header (#4B3822 -> #3D2C1E -> #2B1D24) with bottom rounded-32dp corners
 * - Top white-gold and bottom golden specular highlights
 * - Left: Single Control Center hamburger circle (40dp)
 * - Center: Alpha Go App Icon ONLY (56dp rounded-16dp with golden rim & glow, no text below)
 * - Right: Single circular User Profile / Sign-In button (40dp)
 * - No language pill button in the top bar (language switch lives in the Control Center drawer)
 */
@Composable
fun RamsesHeader(
    hasPersonalApiKey: Boolean,
    userProfile: UserProfile = UserProfile(),
    appLanguage: AppLanguage = AppLanguage.ARABIC,
    selectedModelName: String = "gemini-3.1-flash-lite",
    onModelClick: () -> Unit = {},
    onToggleLanguage: () -> Unit = {},
    onProfileClick: () -> Unit,
    onVoiceCallClick: () -> Unit = {},
    onOpen3DArtifactsClick: () -> Unit = {},
    is3DAvatarMode: Boolean = false,
    onToggle3DAvatarMode: () -> Unit = {},
    onApiKeyClick: () -> Unit = {},
    onMenuClick: () -> Unit,
    onLogoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isArabic = appLanguage == AppLanguage.ARABIC
    val isActiveAccount = userProfile.isSignedIn || hasPersonalApiKey
    val archShape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = archShape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(archShape)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFF4B3822),
                        0.6f to Color(0xFF3D2C1E),
                        1.0f to Color(0xFF2B1D24)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                color = GoldAccent.copy(alpha = 0.75f),
                shape = archShape
            )
    ) {
        // Top specular white-gold edge highlight
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .height(1.5.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xB3FFF5DB),
                            Color.Transparent
                        )
                    )
                )
        )

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Left Menu Button & Right User Avatar Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Control Center Trigger
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("menu_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(10.dp, CircleShape, ambientColor = Color.Black, spotColor = GoldAccent)
                                .clip(CircleShape)
                                .background(Color(0xD9241638))
                                .border(1.5.dp, GoldAccent.copy(alpha = 0.65f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = if (isArabic) "لوحة التحكم" else "Control Center",
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Right: User Avatar / Sign-In Button
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("profile_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .shadow(10.dp, CircleShape, ambientColor = Color.Black, spotColor = GoldAccent)
                                    .clip(CircleShape)
                                    .background(Color(0xD9241638))
                                    .border(1.5.dp, GoldAccent.copy(alpha = 0.75f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val avatarPath = userProfile.avatarUrl
                                if (!avatarPath.isNullOrBlank()) {
                                    val modelData: Any = if (avatarPath.startsWith("http")) avatarPath else File(avatarPath)
                                    AsyncImage(
                                        model = modelData,
                                        contentDescription = userProfile.displayName,
                                        modifier = Modifier
                                            .size(37.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = if (isArabic) "تسجيل الدخول وحسابي" else "Sign In & My Account",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(20.dp)
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

                // Center: App Icon ONLY (56dp rounded-2xl)
                Image(
                    painter = painterResource(id = R.drawable.alpha_go_logo),
                    contentDescription = "Alpha Go",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = GoldGlow,
                            spotColor = GoldAccent
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, GoldAccent.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                        .clickable { onLogoClick() }
                        .testTag("header_alpha_go_logo"),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Bottom luminous golden specular line
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(1.5.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GoldAccent.copy(alpha = 0.90f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
