package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.UserProfile
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CosmicCard.copy(alpha = 0.95f))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Control Center Drawer Toggle
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CosmicLight)
                .border(1.dp, GoldBorder, CircleShape)
                .testTag("menu_button")
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = if (isArabic) "لوحة التحكم" else "Control Center",
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
            )
        }

        // 2. Official Alpha Go App Icon Image (User-provided emblem)
        Image(
            painter = painterResource(id = R.drawable.alpha_go_logo),
            contentDescription = "Alpha Go",
            modifier = Modifier
                .size(48.dp)
                .shadow(10.dp, RoundedCornerShape(13.dp), ambientColor = GoldAccent, spotColor = GoldAccent)
                .clip(RoundedCornerShape(13.dp))
                .border(1.5.dp, GoldAccent, RoundedCornerShape(13.dp))
                .clickable { onLogoClick() }
                .testTag("header_alpha_go_logo"),
            contentScale = ContentScale.Crop
        )

        // 3. Profile Avatar / Login Toggle with Status Indicator Dot
        Box(
            modifier = Modifier
                .size(44.dp)
                .testTag("profile_button"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CosmicLight)
                    .border(1.dp, GoldBorder, CircleShape)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                val avatarPath = userProfile.avatarUrl
                if (!avatarPath.isNullOrBlank()) {
                    val modelData: Any = if (avatarPath.startsWith("http")) avatarPath else File(avatarPath)
                    AsyncImage(
                        model = modelData,
                        contentDescription = userProfile.displayName,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = if (isArabic) "الحساب والملف الشخصي" else "Account & Profile",
                        tint = GoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(if (isActiveAccount) EmeraldSuccess else Color(0xFF6B7280))
                    .border(1.5.dp, CosmicBg, CircleShape)
            )
        }
    }
    HorizontalDivider(thickness = 1.dp, color = GoldBorder)
}
