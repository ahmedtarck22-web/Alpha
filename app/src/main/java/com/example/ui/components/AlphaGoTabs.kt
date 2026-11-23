package com.example.ui.components

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppLanguage
import com.example.data.model.CloudRagBook
import com.example.data.model.CloudRagSyncState
import com.example.data.model.CloudVideoItem
import com.example.data.model.CloudVideoUploadState
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.CosmicLight
import com.example.ui.theme.EgyptianCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldBorderSubtle
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.RoseError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.BookQuizQuestion

@Composable
fun QuizzesTabView(
    appLanguage: AppLanguage,
    xpScore: Int,
    quizQuestions: List<BookQuizQuestion>,
    cloudRagBooks: List<CloudRagBook>,
    selectedBookId: String?,
    isGeneratingQuiz: Boolean,
    quizGenerationError: String?,
    onSelectBookFilter: (String?) -> Unit,
    onGenerateQuizFromBooks: (String?) -> Unit,
    onEarnXp: (Int) -> Unit,
    onAskAlphaGoAboutQuiz: (String) -> Unit,
    onGoToLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isAr = appLanguage == AppLanguage.ARABIC
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedOption by rememberSaveable { mutableIntStateOf(-1) }
    var quizCompleted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(quizQuestions) {
        currentIndex = 0
        selectedOption = -1
        quizCompleted = false
    }

    if (quizQuestions.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp)
                .testTag("quizzes_tab_view"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CosmicCard.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, GoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = if (isAr) "اختبارات من داخل الكتب المرفوعة" else "Quizzes From Uploaded Books",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GoldAccent,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (cloudRagBooks.isEmpty()) {
                            if (isAr) {
                                "لا توجد كتب في المكتبة حالياً. ارفع كتبك بصيغة PDF من تبويب «المكتبة» ليتم توليد الأسئلة والإجابات من داخلها."
                            } else {
                                "No books in Library yet. Upload your PDF books in the Library tab to generate book-only quizzes."
                            }
                        } else {
                            if (isAr) {
                                "لديك ${cloudRagBooks.size} كتب في المكتبة. اضغط أدناه لتوليد أسئلة وإجابات مستخرجة من داخل كتبك."
                            } else {
                                "You have ${cloudRagBooks.size} books in Library. Tap below to generate questions strictly from them."
                            }
                        },
                        fontFamily = CairoFontFamily,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    if (!quizGenerationError.isNullOrBlank()) {
                        Text(
                            text = quizGenerationError,
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = RoseError,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (cloudRagBooks.isEmpty()) {
                        Button(
                            onClick = onGoToLibrary,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = CosmicBg
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("quizzes_go_to_library_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAr) "الانتقال للمكتبة لرفع كتب PDF" else "Open Library to Upload PDFs",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = { onGenerateQuizFromBooks(selectedBookId) },
                            enabled = !isGeneratingQuiz,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = CosmicBg
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("generate_book_quiz_button")
                        ) {
                            if (isGeneratingQuiz) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = CosmicBg,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isAr) "توليد أسئلة من داخل الكتب" else "Generate Book-Only Quiz",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
        return
    }

    val safeIndex = currentIndex.coerceIn(0, quizQuestions.lastIndex)
    val question = quizQuestions[safeIndex]
    val options = if (isAr) question.optionsAr else question.optionsEn
    val hasAnswered = selectedOption != -1
    val progress = (safeIndex + 1).toFloat() / quizQuestions.size.toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("quizzes_tab_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAr) "اختبارات من داخل الكتب" else "Book-Only Quizzes",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = GoldAccent
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(GoldAccent.copy(alpha = 0.16f))
                    .border(1.dp, GoldBorder, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isAr) "موثق من الكتاب" else "Book Grounded",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = GoldAccent
                )
            }
        }

        // Book Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val allSelected = selectedBookId == null
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (allSelected) GoldAccent else CosmicCard)
                    .border(1.dp, if (allSelected) GoldAccent else GoldBorderSubtle, RoundedCornerShape(14.dp))
                    .clickable { onSelectBookFilter(null) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("quiz_filter_all_books"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isAr) "جميع كتب المكتبة" else "All Library Books",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (allSelected) CosmicBg else TextPrimary
                )
            }

            for (book in cloudRagBooks) {
                val isSelected = selectedBookId == book.bookId
                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) GoldAccent else CosmicCard)
                        .border(1.dp, if (isSelected) GoldAccent else GoldBorderSubtle, RoundedCornerShape(14.dp))
                        .clickable { onSelectBookFilter(book.bookId) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = book.title.take(28),
                        fontFamily = CairoFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (isSelected) CosmicBg else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        if (!quizGenerationError.isNullOrBlank()) {
            Text(
                text = quizGenerationError,
                fontFamily = CairoFontFamily,
                fontSize = 12.sp,
                color = RoseError
            )
        }

        // Quiz Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = CosmicCard.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, GoldBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                if (quizCompleted) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = GoldGlow,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isAr) "أحسنت! أكملت اختبار الكتاب" else "Well Done! Book Quiz Complete",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isAr) "مجموع نقاطك الحالي: $xpScore XP" else "Your current score: $xpScore XP",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                currentIndex = 0
                                selectedOption = -1
                                quizCompleted = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = CosmicBg
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAr) "إعادة الاختبار" else "Retry Quiz",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Book & Page Badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CosmicDark.copy(alpha = 0.75f))
                            .border(1.dp, GoldBorderSubtle, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("quiz_book_source_badge"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = question.bookTitle,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = GoldGlow,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = question.pageRange,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = EgyptianCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAr) {
                                "السؤال ${safeIndex + 1} من ${quizQuestions.size}"
                            } else {
                                "Question ${safeIndex + 1} of ${quizQuestions.size}"
                            },
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = TextTertiary
                        )
                        Text(
                            text = if (isAr) "النقاط: $xpScore XP" else "Score: $xpScore XP",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50)),
                        color = GoldAccent,
                        trackColor = CosmicLight
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isAr) question.questionAr else question.questionEn,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        options.forEachIndexed { idx, optionText ->
                            val isCorrectOption = idx == question.correctIndex
                            val isChosenOption = idx == selectedOption

                            val borderColor = when {
                                !hasAnswered -> GoldBorderSubtle
                                isCorrectOption -> EmeraldSuccess
                                isChosenOption -> RoseError
                                else -> GoldBorderSubtle
                            }
                            val bgColor = when {
                                !hasAnswered -> CosmicLight.copy(alpha = 0.55f)
                                isCorrectOption -> EmeraldSuccess.copy(alpha = 0.16f)
                                isChosenOption -> RoseError.copy(alpha = 0.16f)
                                else -> CosmicLight.copy(alpha = 0.35f)
                            }
                            val textColor = when {
                                !hasAnswered -> TextSecondary
                                isCorrectOption -> EmeraldSuccess
                                isChosenOption -> RoseError
                                else -> TextTertiary
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(bgColor)
                                    .border(1.4.dp, borderColor, RoundedCornerShape(14.dp))
                                    .clickable(enabled = !hasAnswered) {
                                        selectedOption = idx
                                        if (idx == question.correctIndex) {
                                            onEarnXp(100)
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = optionText,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = if (hasAnswered && (isCorrectOption || isChosenOption)) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = textColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = when {
                                        !hasAnswered -> Icons.Default.RadioButtonUnchecked
                                        isCorrectOption -> Icons.Default.CheckCircle
                                        isChosenOption -> Icons.Default.Cancel
                                        else -> Icons.Default.RadioButtonUnchecked
                                    },
                                    contentDescription = null,
                                    tint = textColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (hasAnswered) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CosmicDark.copy(alpha = 0.78f))
                                .border(1.dp, GoldBorderSubtle, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isAr) {
                                        "📚 الإجابة من الكتاب: ${question.bookTitle} (${question.pageRange})"
                                    } else {
                                        "📚 Book Source: ${question.bookTitle} (${question.pageRange})"
                                    },
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = GoldGlow
                                )
                                Text(
                                    text = if (isAr) question.explanationAr else question.explanationEn,
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                !hasAnswered -> if (isAr) "اختر الإجابة الصحيحة من الكتاب!" else "Pick the book-grounded answer!"
                                selectedOption == question.correctIndex -> if (isAr) "إجابة صحيحة! +100 XP" else "Correct! +100 XP"
                                else -> if (isAr) "راجع التفسير من الكتاب ثم أكمل" else "Review the book citation & continue"
                            },
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = when {
                                !hasAnswered -> TextSecondary
                                selectedOption == question.correctIndex -> EmeraldSuccess
                                else -> RoseError
                            },
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                if (safeIndex < quizQuestions.lastIndex) {
                                    currentIndex++
                                    selectedOption = -1
                                } else {
                                    quizCompleted = true
                                }
                            },
                            enabled = hasAnswered,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = CosmicBg,
                                disabledContainerColor = GoldAccent.copy(alpha = 0.25f),
                                disabledContentColor = CosmicBg.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = if (isAr) "التالي" else "Next",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Generate Fresh Questions Strictly From Selected Book(s)
        Button(
            onClick = { onGenerateQuizFromBooks(selectedBookId) },
            enabled = !isGeneratingQuiz,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldAccent,
                contentColor = CosmicBg
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("generate_book_quiz_button")
        ) {
            if (isGeneratingQuiz) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = CosmicBg,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isAr) "جاري توليد أسئلة من داخل الكتاب..." else "Generating questions from books...",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAr) "توليد أسئلة جديدة من داخل الكتب" else "Generate New Book-Only Questions",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Ask Go Alpha about current question inside book
        OutlinedButton(
            onClick = {
                val prompt = if (isAr) {
                    "اشرح لي الإجابة بالتفصيل من كتاب «${question.bookTitle}» (${question.pageRange}) للسؤال: ${question.questionAr}"
                } else {
                    "Explain in detail from «${question.bookTitle}» (${question.pageRange}): ${question.questionEn}"
                }
                onAskAlphaGoAboutQuiz(prompt)
            },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GoldBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldGlow),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isAr) "اطلب من جو ألفا شرح السؤال من الكتاب" else "Ask Go Alpha to explain from book",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun VideosTabView(
    appLanguage: AppLanguage,
    videos: List<CloudVideoItem>,
    uploadState: CloudVideoUploadState,
    currentUid: String?,
    isUserSignedIn: Boolean,
    onSignInClick: () -> Unit,
    onUploadVideo: (Uri, String, String, Boolean) -> Unit,
    onDeleteVideo: (CloudVideoItem) -> Unit,
    onResetUploadState: () -> Unit,
    onAskAlphaGoAboutVideo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAr = appLanguage == AppLanguage.ARABIC
    val context = LocalContext.current

    var activeVideo by remember { mutableStateOf<CloudVideoItem?>(null) }
    var pendingVideoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingTitle by remember { mutableStateOf("") }
    var pendingDescription by remember { mutableStateOf("") }
    var pendingIsShared by remember { mutableStateOf(true) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingVideoUri = uri
            pendingTitle = ""
            pendingDescription = ""
            pendingIsShared = true
            onResetUploadState()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("videos_tab_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAr) "الفيديوهات السحابية" else "Cloud Video Lessons",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = GoldAccent
                )
            }

            Button(
                onClick = {
                    if (!isUserSignedIn) {
                        onSignInClick()
                    } else {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = CosmicBg
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("upload_video_button")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAr) "رفع فيديو" else "Upload Video",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Upload Progress / Result Banner
        when (uploadState) {
            is CloudVideoUploadState.Uploading -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CosmicCard,
                    border = BorderStroke(1.dp, GoldAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = uploadState.progressMessage,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = GoldGlow
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { uploadState.fraction.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            color = GoldAccent,
                            trackColor = CosmicLight
                        )
                    }
                }
            }
            is CloudVideoUploadState.Success -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = EmeraldSuccess.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, EmeraldSuccess),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isAr) {
                                "تم رفع الفيديو «${uploadState.video.title}» بنجاح وأصبح متاحاً أونلاين!"
                            } else {
                                "Uploaded «${uploadState.video.title}» successfully for online playback!"
                            },
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = EmeraldSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onResetUploadState, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = EmeraldSuccess)
                        }
                    }
                }
            }
            is CloudVideoUploadState.Error -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RoseError.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, RoseError),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uploadState.message,
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = RoseError,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onResetUploadState, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = RoseError)
                        }
                    }
                }
            }
            CloudVideoUploadState.Idle -> Unit
        }

        if (videos.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CosmicCard.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, GoldBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("videos_empty_guide")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isAr) "لا توجد فيديوهات مرفوعة بعد — طريقة الرفع:" else "No videos uploaded yet — How to upload:",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GoldAccent
                    )
                    Text(
                        text = if (isAr) {
                            "1. سجّل الدخول بحساب Google من الأيقونة أعلى اليسار.\n" +
                                "2. اضغط على زر «رفع فيديو» واختر ملف الفيديو من هاتفك (MP4 أو غيره بدون أذونات تخزين معقدة).\n" +
                                "3. اكتب عنوان الدرس واختر مشاركته ليتم رفعه أونلاين على Firebase Storage وتشغيله مباشرة داخل التطبيق."
                        } else {
                            "1. Sign in with Google via the profile icon.\n" +
                                "2. Tap «Upload Video» and pick any video using Android Photo Picker.\n" +
                                "3. Enter a title and upload to Firebase Storage for instant online streaming."
                        },
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(videos, key = { it.videoId }) { video ->
                    val sizeMb = String.format(java.util.Locale.US, "%.1f MB", video.fileSizeBytes / (1024.0 * 1024.0))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { activeVideo = video }
                            .testTag("video_item_${video.videoId}"),
                        shape = RoundedCornerShape(16.dp),
                        color = CosmicCard.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, GoldBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(82.dp)
                                    .height(58.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CosmicDark)
                                    .border(1.dp, GoldBorder, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = video.title,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.title,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (video.description.isNotBlank()) {
                                    Text(
                                        text = video.description,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GoldAccent.copy(alpha = 0.18f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isAr) "أونلاين • $sizeMb" else "Online • $sizeMb",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 10.sp,
                                            color = GoldGlow
                                        )
                                    }
                                }
                            }

                            if (currentUid != null && video.ownerId == currentUid) {
                                IconButton(
                                    onClick = { onDeleteVideo(video) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = if (isAr) "حذف الفيديو" else "Delete Video",
                                        tint = RoseError.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Metadata Entry Dialog Before Uploading Picked Video
    pendingVideoUri?.let { uri ->
        Dialog(onDismissRequest = { pendingVideoUri = null }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CosmicBg,
                border = BorderStroke(1.dp, GoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isAr) "تفاصيل الفيديو قبل الرفع" else "Video Details Before Upload",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GoldAccent
                    )

                    OutlinedTextField(
                        value = pendingTitle,
                        onValueChange = { pendingTitle = it },
                        label = { Text(if (isAr) "عنوان الفيديو" else "Video Title", fontFamily = CairoFontFamily) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = GoldBorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = GoldAccent,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_title_input")
                    )

                    OutlinedTextField(
                        value = pendingDescription,
                        onValueChange = { pendingDescription = it },
                        label = { Text(if (isAr) "وصف قصير (اختياري)" else "Short Description (optional)", fontFamily = CairoFontFamily) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = GoldBorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = GoldAccent,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAr) "متاح لجميع المستخدمين أونلاين" else "Share publicly online",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Switch(
                            checked = pendingIsShared,
                            onCheckedChange = { pendingIsShared = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CosmicBg,
                                checkedTrackColor = GoldAccent
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { pendingVideoUri = null },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GoldBorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = if (isAr) "إلغاء" else "Cancel",
                                fontFamily = CairoFontFamily,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                val finalTitle = pendingTitle.trim().ifBlank {
                                    if (isAr) "درس فيديو تعليمي" else "Educational Video"
                                }
                                onUploadVideo(uri, finalTitle, pendingDescription.trim(), pendingIsShared)
                                pendingVideoUri = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = CosmicBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("confirm_upload_video_button")
                        ) {
                            Text(
                                text = if (isAr) "رفع الآن" else "Upload Now",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Real Online Streaming VideoPlayer Dialog (VideoView + MediaController)
    activeVideo?.let { video ->
        var isBuffering by remember(video.videoId) { mutableStateOf(true) }
        var playbackError by remember(video.videoId) { mutableStateOf<String?>(null) }

        Dialog(onDismissRequest = { activeVideo = null }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CosmicBg,
                border = BorderStroke(1.dp, GoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = video.title,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GoldAccent,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { activeVideo = null },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black)
                            .border(1.dp, GoldBorder, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (playbackError == null) {
                            var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
                            DisposableEffect(video.videoId) {
                                onDispose {
                                    runCatching { videoViewRef?.stopPlayback() }
                                }
                            }
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        videoViewRef = this
                                        val controller = MediaController(ctx)
                                        controller.setAnchorView(this)
                                        setMediaController(controller)
                                        setOnPreparedListener { mp ->
                                            isBuffering = false
                                            mp.isLooping = false
                                            start()
                                        }
                                        setOnErrorListener { _, _, _ ->
                                            isBuffering = false
                                            playbackError = if (isAr) {
                                                "تعذر تشغيل الفيديو أونلاين. تحقق من اتصال الإنترنت."
                                            } else {
                                                "Could not stream video online. Check internet connection."
                                            }
                                            true
                                        }
                                        setVideoURI(Uri.parse(video.videoDownloadUrl))
                                        requestFocus()
                                    }
                                }
                            )
                        }

                        if (isBuffering && playbackError == null) {
                            CircularProgressIndicator(
                                color = GoldAccent,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        playbackError?.let { err ->
                            Text(
                                text = err,
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = RoseError,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (video.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = video.description,
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 19.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val title = video.title
                            activeVideo = null
                            val prompt = if (isAr) {
                                "ابحث في كتب المكتبة عمّا يرتبط بعنوان هذا الدرس: $title"
                            } else {
                                "Find Library book passages related to this video topic: $title"
                            }
                            onAskAlphaGoAboutVideo(prompt)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = CosmicBg
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "ربط الدرس بكتب المكتبة مع جو ألفا" else "Connect to Library Books with Go Alpha",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryTabView(
    appLanguage: AppLanguage,
    cloudRagBooks: List<CloudRagBook>,
    cloudRagSyncState: CloudRagSyncState,
    currentUid: String? = null,
    isUserSignedIn: Boolean,
    onSignInClick: () -> Unit,
    onUploadAndConvertPdfs: (List<Uri>) -> Unit,
    onDeleteBook: (CloudRagBook) -> Unit = {},
    onOpenFullRagHub: () -> Unit,
    onAskAlphaGoAboutBook: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAr = appLanguage == AppLanguage.ARABIC
    val context = LocalContext.current
    var selectedBook by remember { mutableStateOf<CloudRagBook?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onUploadAndConvertPdfs(uris)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("library_tab_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAr) "المكتبة والكتب الرقمية" else "Digital Books Library",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = GoldAccent
                )
            }
            Text(
                text = if (isAr) "${cloudRagBooks.size} كتب" else "${cloudRagBooks.size} Books",
                fontFamily = CairoFontFamily,
                fontSize = 12.sp,
                color = TextTertiary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sync Progress Banner (PDF -> RAG + Firebase Storage)
        when (cloudRagSyncState) {
            is CloudRagSyncState.Syncing -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CosmicCard,
                    border = BorderStroke(1.dp, GoldAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = cloudRagSyncState.progressMessage,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = GoldGlow
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val frac = if (cloudRagSyncState.totalSteps > 0) {
                            cloudRagSyncState.currentStep.toFloat() / cloudRagSyncState.totalSteps.toFloat()
                        } else 0.5f
                        LinearProgressIndicator(
                            progress = { frac.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            color = GoldAccent,
                            trackColor = CosmicLight
                        )
                    }
                }
            }
            is CloudRagSyncState.Error -> {
                Text(
                    text = cloudRagSyncState.message,
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    color = RoseError,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            else -> Unit
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(2) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (!isUserSignedIn) {
                                onSignInClick()
                            } else {
                                pdfPickerLauncher.launch(arrayOf("application/pdf"))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = CosmicBg
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                            .testTag("library_upload_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "رفع PDF: تحويل لـ RAG + حفظ أونلاين" else "Upload PDF: Convert to RAG & Save Online",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenFullRagHub() },
                        shape = RoundedCornerShape(14.dp),
                        color = CosmicLight.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, GoldBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isAr) "عرض المجلدات السحابية وإدارة الفهرسة" else "Browse Cloud Folders & Indexing",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (cloudRagBooks.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = CosmicCard.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, GoldBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("library_empty_guide")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isAr) "المكتبة السحابية فارغة وجاهزة لكتبك" else "Cloud Library Is Empty & Ready",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GoldAccent
                            )
                            Text(
                                text = if (isAr) {
                                    "1. اضغط على «رفع PDF: تحويل لـ RAG + حفظ أونلاين».\n" +
                                        "2. اختر ملفات الـ PDF الخاصة بك لتحويلها تلقائياً إلى مجلدات RAG في Firebase وحفظ نسخة أونلاين قابلة للتحميل.\n" +
                                        "3. بعد الرفع سيعتمد جو ألفا والاختبارات على كتبك الجديدة فقط."
                                } else {
                                    "1. Tap «Upload PDF: Convert to RAG & Save Online».\n" +
                                        "2. Pick your PDFs to index into Firestore RAG chunks and store online.\n" +
                                        "3. Go Alpha and Quizzes will then answer strictly from your uploaded books."
                                },
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            items(cloudRagBooks, key = { it.bookId }) { book ->
                val hasOnlinePdf = book.pdfDownloadUrl.isNotBlank()
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 162.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedBook = book }
                        .testTag("library_book_card_${book.bookId}"),
                    shape = RoundedCornerShape(16.dp),
                    color = CosmicCard.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, GoldBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            GoldAccent.copy(alpha = 0.28f),
                                            CosmicLight
                                        )
                                    )
                                )
                                .border(1.dp, GoldAccent, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasOnlinePdf) Icons.Default.PictureAsPdf else Icons.Default.Book,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = book.title,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CosmicLight.copy(alpha = 0.72f))
                                .border(1.dp, GoldBorderSubtle, RoundedCornerShape(10.dp))
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val badge = buildString {
                                if (book.pageCount > 0) append("${book.pageCount} ص")
                                else append("${book.chunkCount} فقرة")
                                if (hasOnlinePdf) append(" • PDF")
                            }
                            Text(
                                text = badge,
                                fontFamily = CairoFontFamily,
                                fontSize = 10.sp,
                                color = if (hasOnlinePdf) EgyptianCyan else GoldGlow,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    selectedBook?.let { book ->
        val hasOnlinePdf = book.pdfDownloadUrl.isNotBlank()
        Dialog(onDismissRequest = { selectedBook = null }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CosmicBg,
                border = BorderStroke(1.dp, GoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = book.title,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GoldAccent,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { selectedBook = null },
                            modifier = Modifier.size(48.dp)
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
                                text = "${book.eraCategory} • ${book.pageCount} صفحة • ${book.folderCount} مجلد RAG",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = GoldGlow
                            )
                            Text(
                                text = book.summary.ifBlank {
                                    if (isAr) "كتاب مفهرس في نظام RAG للبحث والإجابة الموثقة برقم الصفحة." else "Indexed in Cloud RAG for page-cited answers."
                                },
                                fontFamily = CairoFontFamily,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 21.sp
                            )
                        }
                    }

                    if (hasOnlinePdf) {
                        Button(
                            onClick = {
                                downloadOnlinePdf(context, book.pdfDownloadUrl, book.title, isAr)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EgyptianCyan,
                                contentColor = CosmicBg
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("download_online_pdf_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAr) "تحميل الكتاب PDF أونلاين" else "Download PDF Book Online",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Text(
                            text = if (isAr) {
                                "هذا المرجع مفهرس بالـ RAG. عند رفعك لملف PDF جديد سيُحفظ تلقائياً بنسختي RAG + PDF قابل للتحميل أونلاين."
                            } else {
                                "Pre-indexed in RAG. Uploaded PDFs automatically store both RAG chunks and an online downloadable PDF."
                            },
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = TextTertiary,
                            lineHeight = 18.sp
                        )
                    }

                    if (currentUid != null && book.ownerId == currentUid) {
                        OutlinedButton(
                            onClick = {
                                val toDelete = book
                                selectedBook = null
                                onDeleteBook(toDelete)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                            border = BorderStroke(1.dp, RoseError.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("delete_cloud_book_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAr) "حذف الكتاب من Firebase" else "Delete Book from Firebase",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val title = book.title
                            selectedBook = null
                            val prompt = if (isAr) {
                                "لخّص لي أهم موضوعات كتاب «$title» مع ذكر الصفحات"
                            } else {
                                "Summarize the main topics from «$title» with page citations"
                            }
                            onAskAlphaGoAboutBook(prompt)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = CosmicBg
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "تلخيص من الكتاب مع جو ألفا" else "Summarize Book with Go Alpha",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

private fun downloadOnlinePdf(
    context: Context,
    pdfUrl: String,
    bookTitle: String,
    isArabic: Boolean
) {
    val safeName = bookTitle
        .replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_ -]+"), "_")
        .trim()
        .ifBlank { "AlphaGo_Book" }
        .take(50) + ".pdf"

    val dmSuccess = runCatching {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            ?: return@runCatching false
        val req = DownloadManager.Request(Uri.parse(pdfUrl))
            .setTitle(bookTitle)
            .setDescription(if (isArabic) "جاري تحميل الكتاب من Alpha Go" else "Downloading PDF from Alpha Go")
            .setMimeType("application/pdf")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
        dm.enqueue(req)
        true
    }.getOrDefault(false)

    if (dmSuccess) {
        Toast.makeText(
            context,
            if (isArabic) "بدأ تحميل «$safeName» في مجلد التنزيلات" else "Downloading $safeName to Downloads",
            Toast.LENGTH_LONG
        ).show()
    } else {
        runCatching {
            val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
            context.startActivity(viewIntent)
        }.onFailure {
            Toast.makeText(
                context,
                if (isArabic) "تعذر فتح رابط التحميل" else "Unable to open PDF link",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
