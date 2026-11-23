package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppLanguage
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldBorderSubtle
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.RoseError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ContactUsDialog(
    appLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val isAr = appLanguage == AppLanguage.ARABIC
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CosmicBg,
            border = BorderStroke(1.2.dp, GoldBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("contact_us_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = GoldGlow,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "تواصل معنا" else "Contact Us",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = GoldGlow
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CosmicCard,
                    border = BorderStroke(1.dp, GoldBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isAr) "فريق دعم Alpha Go" else "Alpha Go Support Team",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GoldAccent
                        )
                        Text(
                            text = if (isAr) {
                                "لأي استفسار عن الكتب، المنهج، أو الدعم الفني تواصل معنا مباشرة عبر البريد الإلكتروني الرسمي:"
                            } else {
                                "For curriculum, books, or technical support, reach out to our team via official email:"
                            },
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = "mugraduationproject2027@gmail.com",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                }

                Button(
                    onClick = {
                        runCatching {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:mugraduationproject2027@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Alpha Go Support")
                            }
                            context.startActivity(emailIntent)
                        }.onFailure {
                            Toast.makeText(
                                context,
                                if (isAr) "لا يوجد تطبيق بريد مثبت على الجهاز" else "No email client installed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = CosmicBg),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) "إرسال رسالة بريد إلكتروني" else "Send Support Email",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AppSettingsDialog(
    appLanguage: AppLanguage,
    hasPersonalApiKey: Boolean,
    isSearchGroundingEnabled: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleSearchGrounding: () -> Unit,
    onOpenApiKeyDialog: () -> Unit,
    onStartVoiceCall: () -> Unit,
    onNewChatClick: () -> Unit,
    onClearAllChats: () -> Unit,
    onDismiss: () -> Unit
) {
    val isAr = appLanguage == AppLanguage.ARABIC

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CosmicBg,
            border = BorderStroke(1.2.dp, GoldBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = GoldGlow,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "الإعدادات" else "Settings",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = GoldGlow
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Language Toggle
                SettingsActionRow(
                    icon = Icons.Default.Language,
                    title = if (isAr) "لغة التطبيق (عربي / EN)" else "App Language (EN / عربي)",
                    subtitle = if (isAr) "الحالية: العربية" else "Current: English",
                    onClick = onToggleLanguage
                )

                // Deep Book Search Switch
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CosmicCard,
                    border = BorderStroke(1.dp, GoldBorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAr) "البحث العميق داخل الكتب" else "Deep Book Search",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isAr) "توسيع البحث عبر جميع فصول المكتبة" else "Expand retrieval across all chapters",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = isSearchGroundingEnabled,
                            onCheckedChange = { onToggleSearchGrounding() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CosmicBg,
                                checkedTrackColor = GoldAccent
                            )
                        )
                    }
                }

                SettingsActionRow(
                    icon = Icons.Default.Key,
                    title = if (isAr) "مفتاح Gemini API الشخصي" else "Personal Gemini API Key",
                    subtitle = if (hasPersonalApiKey) {
                        if (isAr) "مفتاح شخصي مفعّل" else "Custom key active"
                    } else {
                        if (isAr) "تعيين أو إدارة المفتاح" else "Configure or manage key"
                    },
                    onClick = {
                        onDismiss()
                        onOpenApiKeyDialog()
                    }
                )

                SettingsActionRow(
                    icon = Icons.Default.Phone,
                    title = if (isAr) "مكالمة صوتية مباشرة" else "Go Alpha Live Call",
                    subtitle = if (isAr) "بدء جلسة صوتية تفاعلية" else "Start interactive voice study",
                    onClick = {
                        onDismiss()
                        onStartVoiceCall()
                    }
                )

                SettingsActionRow(
                    icon = Icons.Default.AddComment,
                    title = if (isAr) "بدء محادثة جديدة" else "Start New Chat",
                    subtitle = if (isAr) "فتح سجل محادثة جديد في الرئيسية" else "Open a fresh conversation",
                    onClick = {
                        onDismiss()
                        onNewChatClick()
                    }
                )

                HorizontalDivider(color = GoldBorderSubtle)

                SettingsActionRow(
                    icon = Icons.Default.DeleteSweep,
                    title = if (isAr) "مسح سجل المحادثات" else "Clear Chat History",
                    subtitle = if (isAr) "حذف جميع المحادثات السابقة" else "Delete all saved chats",
                    accentColor = RoseError,
                    onClick = {
                        onClearAllChats()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: androidx.compose.ui.graphics.Color = GoldGlow,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CosmicCard,
        border = BorderStroke(1.dp, GoldBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CosmicLight)
                    .border(1.dp, GoldBorderSubtle, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontFamily = CairoFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
