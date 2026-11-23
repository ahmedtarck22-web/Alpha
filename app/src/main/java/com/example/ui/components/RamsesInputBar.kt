package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.CosmicLight
import com.example.ui.theme.EgyptianCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldBorderSubtle
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.RoseError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun RamsesInputBar(
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onAttachClick: () -> Unit,
    onMicVoiceTypingClick: () -> Unit,
    onGeminiLiveClick: () -> Unit,
    selectedImageBitmap: Bitmap?,
    onClearAttachedImage: () -> Unit,
    isListeningVoiceTyping: Boolean = false,
    isSearchGroundingEnabled: Boolean = false,
    onToggleSearchGrounding: () -> Unit = {},
    onOpenRagDialog: () -> Unit = {},
    appLanguage: AppLanguage = AppLanguage.ARABIC,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val isAr = appLanguage == AppLanguage.ARABIC
    var showPlusMenu by remember { mutableStateOf(false) }

    // Keep exact LTR bar arrangement from IMG_1050:
    // Left: [Floating card & (+)] | Center: [اكتب هنا...] | Right: [🎤] [📞]
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Luminous Frosted Plus Menu matching IMG_1050
            AnimatedVisibility(
                visible = showPlusMenu,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                Surface(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .widthIn(min = 185.dp, max = 230.dp)
                        .shadow(18.dp, RoundedCornerShape(18.dp), ambientColor = GoldGlow, spotColor = GoldAccent)
                        .testTag("plus_popup_menu"),
                    shape = RoundedCornerShape(18.dp),
                    color = CosmicCard.copy(alpha = 0.94f),
                    border = BorderStroke(1.2.dp, GoldBorder)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showPlusMenu = false
                                    onAttachClick()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("plus_menu_image_analysis"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isAr) "تحليل الصور" else "Image Analysis",
                                fontFamily = CairoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = GoldGlow,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(
                            thickness = 0.8.dp,
                            color = GoldBorderSubtle,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSearchGroundingEnabled) GoldAccent.copy(alpha = 0.14f) else Color.Transparent
                                )
                                .clickable {
                                    showPlusMenu = false
                                    onToggleSearchGrounding()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("plus_menu_rag_library"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isAr) "البحث العميق" else "Deep Book Search",
                                fontFamily = CairoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSearchGroundingEnabled) GoldGlow else TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = if (isSearchGroundingEnabled) GoldGlow else GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            val validPreviewBitmap = selectedImageBitmap?.takeIf {
                !it.isRecycled && it.width > 0 && it.height > 0
            }

            AnimatedVisibility(
                visible = validPreviewBitmap != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (validPreviewBitmap != null) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CosmicCard.copy(alpha = 0.92f))
                            .border(1.dp, GoldBorder, RoundedCornerShape(16.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                bitmap = validPreviewBitmap.asImageBitmap(),
                                contentDescription = if (isAr) "الصورة المختارة للتحليل" else "Selected image",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isAr) "صورة جاهزة للتحليل" else "Image ready for analysis",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = GoldGlow
                                )
                                Text(
                                    text = if (isAr) "اضغط إرسال للتحليل الفوري" else "Tap send to analyze",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            IconButton(
                                onClick = onClearAttachedImage,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = if (isAr) "إزالة الصورة" else "Remove image",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Unified Cosmic Purple & Gold Capsule matching IMG_1050
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(18.dp, RoundedCornerShape(32.dp), ambientColor = GoldGlow.copy(alpha = 0.45f), spotColor = GoldAccent.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(32.dp),
                color = CosmicCard.copy(alpha = 0.88f),
                border = BorderStroke(
                    width = 1.2.dp,
                    color = if (isListeningVoiceTyping) EgyptianCyan else GoldBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Glowing Golden (+) Button
                    IconButton(
                        onClick = { showPlusMenu = !showPlusMenu },
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(14.dp, CircleShape, ambientColor = GoldGlow, spotColor = GoldAccent)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(GoldAccent.copy(alpha = 0.36f), CosmicLight)
                                )
                            )
                            .border(1.3.dp, GoldGlow.copy(alpha = 0.85f), CircleShape)
                            .testTag("attach_button")
                    ) {
                        Icon(
                            imageVector = if (showPlusMenu) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (isAr) "خيارات التحليل والبحث" else "Analysis & Search Options",
                            tint = GoldGlow,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    // Recessed Dark Purple Input Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 42.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(CosmicDark.copy(alpha = 0.68f))
                            .border(1.dp, GoldBorderSubtle, RoundedCornerShape(22.dp))
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = if (isAr) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = if (isListeningVoiceTyping) {
                                    if (isAr) "جاري الاستماع لصوتك..." else "Listening to your voice..."
                                } else {
                                    if (isAr) "اكتب هنا..." else "Type here..."
                                },
                                fontFamily = CairoFontFamily,
                                fontSize = 13.sp,
                                color = if (isListeningVoiceTyping) EgyptianCyan else TextTertiary
                            )
                        }

                        BasicTextField(
                            value = inputText,
                            onValueChange = onInputTextChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chat_input_field"),
                            textStyle = TextStyle(
                                fontFamily = CairoFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                textDirection = if (isAr) TextDirection.Rtl else TextDirection.Ltr
                            ),
                            cursorBrush = SolidColor(GoldGlow),
                            singleLine = false,
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputText.isNotBlank() || selectedImageBitmap != null) {
                                        onSendMessage(inputText)
                                        focusManager.clearFocus()
                                        showPlusMenu = false
                                    }
                                }
                            )
                        )
                    }

                    // Dynamic Send Button (appears when user types or attaches image)
                    if (inputText.isNotBlank() || selectedImageBitmap != null) {
                        IconButton(
                            onClick = {
                                onSendMessage(inputText)
                                focusManager.clearFocus()
                                showPlusMenu = false
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldAccent)
                                .testTag("send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = if (isAr) "إرسال" else "Send",
                                tint = CosmicBg,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Dark Purple Mic Medallion with Gold Rim & Icon
                    IconButton(
                        onClick = onMicVoiceTypingClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CosmicLight.copy(alpha = 0.65f))
                            .border(1.dp, GoldBorder, CircleShape)
                            .testTag("mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListeningVoiceTyping) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isAr) "تسجيل رسالة صوتية" else "Voice input",
                            tint = if (isListeningVoiceTyping) RoseError else GoldGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Dark Purple Live Call Medallion with Gold Rim & Icon matching IMG_1050
                    IconButton(
                        onClick = {
                            showPlusMenu = false
                            onGeminiLiveClick()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(10.dp, CircleShape, ambientColor = GoldAccent, spotColor = GoldAccent)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(GoldAccent.copy(alpha = 0.20f), CosmicLight)
                                )
                            )
                            .border(1.2.dp, GoldAccent.copy(alpha = 0.8f), CircleShape)
                            .testTag("gemini_live_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = if (isAr) "مكالمة صوتية مباشرة" else "Live Voice Call",
                            tint = GoldGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
