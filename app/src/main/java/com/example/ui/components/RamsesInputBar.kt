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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicLight
import com.example.ui.theme.EgyptianCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldBorderSubtle
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Plus Popup Menu matching HTML #plusMenu
        AnimatedVisibility(
            visible = showPlusMenu,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .widthIn(min = 230.dp, max = 280.dp)
                    .testTag("plus_popup_menu"),
                shape = RoundedCornerShape(18.dp),
                color = CosmicCard.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, GoldBorder),
                shadowElevation = 12.dp
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showPlusMenu = false
                                onAttachClick()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("plus_menu_image_analysis"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isAr) "تحليل الصور والواجبات" else "Image & Homework Analysis",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
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
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showPlusMenu = false
                                onOpenRagDialog()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("plus_menu_rag_library"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isAr) "رفع وتحويل كتب PDF (المكتبة)" else "Upload & Convert PDF Books (Library)",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        val validPreviewBitmap = selectedImageBitmap?.takeIf {
            !it.isRecycled && it.width > 0 && it.height > 0
        }

        // Attached Image Preview Chip
        AnimatedVisibility(
            visible = validPreviewBitmap != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (validPreviewBitmap != null) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CosmicCard.copy(alpha = 0.92f))
                        .border(1.dp, GoldBorder, RoundedCornerShape(14.dp))
                        .padding(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            bitmap = validPreviewBitmap.asImageBitmap(),
                            contentDescription = if (isAr) "الصورة المختارة للتحليل" else "Selected image",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isAr) "صورة جاهزة للتحليل" else "Image ready for analysis",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = GoldAccent
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
                            modifier = Modifier.size(32.dp)
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

        // Unified One-Bar Chat Input matching HTML prototype
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            color = CosmicCard.copy(alpha = 0.95f),
            border = BorderStroke(
                width = 1.dp,
                color = if (isListeningVoiceTyping) EgyptianCyan else GoldBorder
            ),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // (+) Plus Button
                IconButton(
                    onClick = { showPlusMenu = !showPlusMenu },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CosmicLight)
                        .border(1.dp, GoldBorder, CircleShape)
                        .testTag("attach_button")
                ) {
                    Icon(
                        imageVector = if (showPlusMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (isAr) "خيارات التحليل والبحث" else "Analysis & Search Options",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = if (isListeningVoiceTyping) {
                                if (isAr) "جاري الاستماع لصوتك..." else "Listening to your voice..."
                            } else {
                                com.example.data.model.AppStrings.inputPlaceholder(appLanguage)
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
                            color = TextPrimary
                        ),
                        cursorBrush = SolidColor(GoldAccent),
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

                // Send Button (when text or image present)
                if (inputText.isNotBlank() || selectedImageBitmap != null) {
                    IconButton(
                        onClick = {
                            onSendMessage(inputText)
                            focusManager.clearFocus()
                            showPlusMenu = false
                        },
                        modifier = Modifier
                            .size(38.dp)
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

                // Mic Voice Button
                IconButton(
                    onClick = onMicVoiceTypingClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .testTag("mic_button")
                ) {
                    Icon(
                        imageVector = if (isListeningVoiceTyping) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isAr) "تسجيل رسالة صوتية" else "Voice input",
                        tint = if (isListeningVoiceTyping) RoseError else GoldAccent,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Gold Live Voice Call Button
                IconButton(
                    onClick = {
                        showPlusMenu = false
                        onGeminiLiveClick()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(8.dp, CircleShape, ambientColor = GoldAccent, spotColor = GoldAccent)
                        .clip(CircleShape)
                        .background(GoldAccent)
                        .testTag("gemini_live_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = if (isAr) "مكالمة صوتية مباشرة" else "Live Voice Call",
                        tint = CosmicBg,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
