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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicCard
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

    // Exact LTR capsule arrangement from IMG_1050:
    // Left: [Floating card & (+)] | Center: [اكتب هنا...] | Right: [🎤] [📞]
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            // Luminous Frosted Plus Menu matching IMG_1050
            AnimatedVisibility(
                visible = showPlusMenu,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                Surface(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .widthIn(min = 186.dp, max = 224.dp)
                        .shadow(20.dp, RoundedCornerShape(16.dp), ambientColor = GoldGlow, spotColor = GoldAccent)
                        .testTag("plus_popup_menu"),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xF22D1C4D),
                    border = BorderStroke(1.2.dp, GoldGlow.copy(alpha = 0.78f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
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
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF5EFFC)
                            )
                            Icon(
                                imageVector = Icons.Outlined.CenterFocusWeak,
                                contentDescription = null,
                                tint = Color(0xFFEBD2A0),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(
                            thickness = 0.8.dp,
                            color = GoldBorderSubtle,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSearchGroundingEnabled) GoldAccent.copy(alpha = 0.16f) else Color.Transparent
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
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSearchGroundingEnabled) GoldGlow else Color(0xFFF5EFFC)
                            )
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = if (isSearchGroundingEnabled) GoldGlow else Color(0xFFEBD2A0),
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
                            .padding(bottom = 6.dp)
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
                                    .size(44.dp)
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

            // Sleek Slim Cosmic Purple & Gold Capsule
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = GoldGlow.copy(alpha = 0.38f), spotColor = GoldAccent.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xDB241242),
                border = BorderStroke(
                    width = 1.1.dp,
                    color = if (isListeningVoiceTyping) EgyptianCyan else Color(0x99DFB054)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Slim Glowing Golden (+) Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(12.dp, CircleShape, ambientColor = GoldGlow, spotColor = GoldAccent)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(GoldAccent.copy(alpha = 0.34f), Color(0xFF2D1852))
                                )
                            )
                            .border(1.3.dp, GoldGlow.copy(alpha = 0.88f), CircleShape)
                            .clickable { showPlusMenu = !showPlusMenu }
                            .testTag("attach_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (showPlusMenu) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (isAr) "خيارات التحليل والبحث" else "Analysis & Search Options",
                            tint = GoldGlow,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Slim Recessed Purple Input Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 34.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xCC180B30))
                            .border(0.9.dp, Color(0x45A88848), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
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
                                color = if (isListeningVoiceTyping) EgyptianCyan else TextTertiary,
                                textAlign = if (isAr) TextAlign.End else TextAlign.Start
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
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                textAlign = if (isAr) TextAlign.End else TextAlign.Start,
                                textDirection = if (isAr) TextDirection.Rtl else TextDirection.Ltr
                            ),
                            cursorBrush = SolidColor(GoldGlow),
                            singleLine = false,
                            maxLines = 3,
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldAccent)
                                .clickable {
                                    onSendMessage(inputText)
                                    focusManager.clearFocus()
                                    showPlusMenu = false
                                }
                                .testTag("send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = if (isAr) "إرسال" else "Send",
                                tint = CosmicBg,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Slim Purple Mic Medallion
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF281548))
                            .border(1.dp, GoldBorder, CircleShape)
                            .clickable { onMicVoiceTypingClick() }
                            .testTag("mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListeningVoiceTyping) Icons.Default.Stop else Icons.Outlined.Mic,
                            contentDescription = if (isAr) "تسجيل رسالة صوتية" else "Voice input",
                            tint = if (isListeningVoiceTyping) RoseError else GoldGlow,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Slim Purple Live Call Medallion
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(10.dp, CircleShape, ambientColor = GoldAccent, spotColor = GoldAccent)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(GoldAccent.copy(alpha = 0.22f), Color(0xFF281548))
                                )
                            )
                            .border(1.1.dp, GoldAccent.copy(alpha = 0.82f), CircleShape)
                            .clickable {
                                showPlusMenu = false
                                onGeminiLiveClick()
                            }
                            .testTag("gemini_live_call_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = if (isAr) "مكالمة صوتية مباشرة" else "Live Voice Call",
                            tint = GoldGlow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
