package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.LiquidGlassPillBottom
import com.example.ui.theme.LiquidGlassPillTop
import com.example.ui.theme.RoseError
import com.example.ui.theme.TextSecondary

/**
 * Wide Liquid Glass Input Pill matching the New UI `.liquid-glass-pill` + `#plusMenuPopover`:
 * - Left: Glowing gradient (+) button (rotates 45deg when open) + subtle vertical gold divider
 * - Center: Bare transparent text input ("اكتب هنا...") in #FFD18A (no heavy recessed inner box)
 * - Right: Mic medallion + subtle vertical gold divider + Call medallion
 * - Plus Popover: Frosted mauve liquid-glass card above (+) with "تحليل الصور" and "البحث العميق"
 */
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
    val plusRotation by animateFloatAsState(
        targetValue = if (showPlusMenu) 45f else 0f,
        label = "plus_rotation"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 6.dp)
        ) {
            // Luminous Frosted Liquid-Glass Plus Menu Popover
            AnimatedVisibility(
                visible = showPlusMenu,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .widthIn(min = 184.dp, max = 224.dp)
                        .shadow(22.dp, RoundedCornerShape(16.dp), ambientColor = GoldAccent, spotColor = GoldAccent)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xEB663672),
                                    Color(0xF52A143E)
                                )
                            )
                        )
                        .border(1.5.dp, GoldAccent.copy(alpha = 0.80f), RoundedCornerShape(16.dp))
                        .testTag("plus_popup_menu")
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
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
                            Icon(
                                imageVector = Icons.Outlined.CenterFocusWeak,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isAr) "تحليل الصور" else "Image Analysis",
                                fontFamily = CairoFontFamily,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldAccent
                            )
                        }

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = GoldAccent.copy(alpha = 0.35f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = if (isSearchGroundingEnabled) GoldGlow else GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isAr) "البحث العميق" else "Deep Book Search",
                                fontFamily = CairoFontFamily,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSearchGroundingEnabled) GoldGlow else GoldAccent
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
                            .border(1.2.dp, GoldBorder, RoundedCornerShape(16.dp))
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

            // Unified Wide Liquid Glass Pill matching HTML `.liquid-glass-pill`
            val pillShape = RoundedCornerShape(50)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 18.dp,
                        shape = pillShape,
                        ambientColor = GoldAccent.copy(alpha = 0.35f),
                        spotColor = GoldAccent.copy(alpha = 0.42f)
                    )
                    .clip(pillShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                LiquidGlassPillTop,
                                LiquidGlassPillBottom
                            )
                        )
                    )
                    .border(
                        width = 1.8.dp,
                        color = if (isListeningVoiceTyping) EgyptianCyan else GoldAccent.copy(alpha = 0.85f),
                        shape = pillShape
                    )
                    .drawBehind {
                        // Top specular streak matching `.liquid-glass-pill::before`
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xEBFFFCF0),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(size.width * 0.16f, 1.5.dp.toPx()),
                            end = Offset(size.width * 0.84f, 1.5.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Glowing Mauve-Gold (+) Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(10.dp, CircleShape, ambientColor = GoldAccent, spotColor = GoldAccent)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x66B8659B),
                                        Color(0xBF4F245E)
                                    )
                                )
                            )
                            .border(1.5.dp, GoldAccent.copy(alpha = 0.85f), CircleShape)
                            .clickable { showPlusMenu = !showPlusMenu }
                            .testTag("attach_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (isAr) "أدوات إضافية" else "Additional Tools",
                            tint = GoldAccent,
                            modifier = Modifier
                                .size(21.dp)
                                .rotate(plusRotation)
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(1.dp)
                            .height(20.dp)
                            .background(GoldAccent.copy(alpha = 0.40f))
                    )

                    // Center: Bare Transparent Text Input matching HTML `bg-transparent`
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 36.dp)
                            .padding(horizontal = 4.dp),
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
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (isListeningVoiceTyping) EgyptianCyan else GoldAccent.copy(alpha = 0.75f),
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldAccent,
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

                    // Dynamic Send Button when user types or attaches an image
                    if (inputText.isNotBlank() || selectedImageBitmap != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(GoldAccent, GoldGlow)
                                    )
                                )
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

                    Spacer(modifier = Modifier.width(6.dp))

                    // Mic Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x73562963))
                            .border(1.dp, GoldAccent.copy(alpha = 0.60f), CircleShape)
                            .clickable { onMicVoiceTypingClick() }
                            .testTag("mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListeningVoiceTyping) Icons.Default.Stop else Icons.Outlined.Mic,
                            contentDescription = if (isAr) "تسجيل صوتي" else "Voice input",
                            tint = if (isListeningVoiceTyping) RoseError else GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(1.dp)
                            .height(20.dp)
                            .background(GoldAccent.copy(alpha = 0.40f))
                    )

                    // Right: Call Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(10.dp, CircleShape, ambientColor = GoldAccent, spotColor = GoldAccent)
                            .clip(CircleShape)
                            .background(Color(0xA6562963))
                            .border(1.5.dp, GoldAccent.copy(alpha = 0.80f), CircleShape)
                            .clickable {
                                showPlusMenu = false
                                onGeminiLiveClick()
                            }
                            .testTag("gemini_live_call_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = if (isAr) "مكالمة" else "Live Voice Call",
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
