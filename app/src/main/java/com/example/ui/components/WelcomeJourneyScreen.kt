package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppLanguage

private const val SPLASH_IMAGE_ASPECT_RATIO = 1408f / 3040f

@Composable
fun WelcomeJourneyScreen(
    appLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    onStartJourney: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = appLanguage == AppLanguage.ARABIC
    val startLabel = if (isArabic) "ابدأ الرحلة!" else "Start the Journey!"
    val interactionSource = remember { MutableInteractionSource() }

    // Render user-provided IMG_1049 unchanged inside its exact aspect ratio frame,
    // blending edges seamlessly into its cosmic purple corner tone (#211030).
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF211030))
            .testTag("opening_logo_splash"),
        contentAlignment = Alignment.Center
    ) {
        val containerAspectRatio = if (maxHeight > 0.dp) maxWidth / maxHeight else SPLASH_IMAGE_ASPECT_RATIO
        val frameWidth = if (containerAspectRatio > SPLASH_IMAGE_ASPECT_RATIO) {
            maxHeight * SPLASH_IMAGE_ASPECT_RATIO
        } else {
            maxWidth
        }
        val frameHeight = frameWidth / SPLASH_IMAGE_ASPECT_RATIO

        Box(
            modifier = Modifier.size(width = frameWidth, height = frameHeight)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_1049),
                contentDescription = if (isArabic) "شاشة الترحيب جو ألفا" else "Go Alpha Welcome Screen",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            // Interactive hitbox aligned accurately over the "ابدأ الرحلة!" glossy gold pill in img_1049
            val pillWidth = frameWidth * 0.88f
            val pillHeight = (frameHeight * 0.082f).coerceAtLeast(48.dp)
            val pillOffsetX = frameWidth * 0.06f
            val pillOffsetY = frameHeight * 0.758f

            Box(
                modifier = Modifier
                    .offset(x = pillOffsetX, y = pillOffsetY)
                    .size(width = pillWidth, height = pillHeight)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = Color(0xFFFFE599)),
                        role = Role.Button,
                        onClickLabel = startLabel,
                        onClick = onStartJourney
                    )
                    .semantics {
                        contentDescription = startLabel
                    }
                    .testTag("start_journey_button")
            )
        }
    }
}
