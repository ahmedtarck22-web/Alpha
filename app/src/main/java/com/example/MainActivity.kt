package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.AppLanguage
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MessageSender
import com.example.ui.components.AlphaGoBottomBar
import com.example.ui.components.AlphaGoGlbStageCard
import com.example.ui.components.AlphaGoTab
import com.example.ui.components.ApiKeyManagementDialog
import com.example.ui.components.AppSettingsDialog
import com.example.ui.components.AuthSignInDialog
import com.example.ui.components.ChatMessageBubble
import com.example.ui.components.ContactUsDialog
import com.example.ui.components.GeminiLiveVoiceScreen
import com.example.ui.components.LibraryTabView
import com.example.ui.components.LiquidGlassBackground
import com.example.service.GeminiLiveWebSocketClient
import com.example.ui.components.QuizzesTabView
import com.example.ui.components.RagKnowledgeDialog
import com.example.ui.components.RamsesHeader
import com.example.ui.components.RamsesInputBar
import com.example.ui.components.RiggingStudioDialog
import com.example.ui.components.SideMenuDrawer
import com.example.ui.components.VideosTabView
import com.example.ui.components.WelcomeJourneyScreen
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.CosmicBg
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicLight
import com.example.ui.theme.EgyptianCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.PharaonicMidnight
import com.example.ui.theme.RamsesTheme
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.firebase.configureAppCheck
import com.example.ui.viewmodel.RamsesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: RamsesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            RamsesTheme {
                RamsesApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RamsesApp(viewModel: RamsesViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSearchGroundingEnabled by viewModel.isSearchGroundingEnabled.collectAsStateWithLifecycle()
    val selectedImageBitmap by viewModel.selectedImageBitmap.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val ragDocuments by viewModel.ragDocuments.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()

    val isGeminiLiveActive by viewModel.isGeminiLiveActive.collectAsStateWithLifecycle()
    val voiceCallState by viewModel.voiceCallState.collectAsStateWithLifecycle()
    val currentVoiceTranscript by viewModel.currentVoiceTranscript.collectAsStateWithLifecycle()
    val liveModelSpeechTranscript by viewModel.liveModelSpeechTranscript.collectAsStateWithLifecycle()
    val liveConnectionStatus by viewModel.liveConnectionStatus.collectAsStateWithLifecycle()
    val isLiveMicMuted by viewModel.isLiveMicMuted.collectAsStateWithLifecycle()
    val isLiveSpeakerMuted by viewModel.isLiveSpeakerMuted.collectAsStateWithLifecycle()
    val isVoiceTyping by viewModel.isVoiceTyping.collectAsStateWithLifecycle()
    val liveRmsDb by viewModel.combinedRmsDb.collectAsStateWithLifecycle()
    val streamingMessage by viewModel.streamingMessage.collectAsStateWithLifecycle()
    val is3DViewMode by viewModel.is3DViewMode.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val selectedGem by viewModel.selectedGem.collectAsStateWithLifecycle()
    val avatarRigConfig by viewModel.avatarRigConfig.collectAsStateWithLifecycle()
    val fileSearchPipelineState by viewModel.fileSearchPipelineState.collectAsStateWithLifecycle()
    val linkedSearchInstance by viewModel.linkedSearchInstance.collectAsStateWithLifecycle()
    val searchInstanceConfig by viewModel.searchInstanceConfig.collectAsStateWithLifecycle()
    val cloudRagBooks by viewModel.cloudRagBooks.collectAsStateWithLifecycle()
    val cloudRagFolders by viewModel.cloudRagFolders.collectAsStateWithLifecycle()
    val cloudRagSyncState by viewModel.cloudRagSyncState.collectAsStateWithLifecycle()
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val videoUploadState by viewModel.videoUploadState.collectAsStateWithLifecycle()
    val quizQuestions by viewModel.quizQuestions.collectAsStateWithLifecycle()
    val selectedQuizBookId by viewModel.selectedQuizBookId.collectAsStateWithLifecycle()
    val isGeneratingQuiz by viewModel.isGeneratingQuiz.collectAsStateWithLifecycle()
    val quizGenerationError by viewModel.quizGenerationError.collectAsStateWithLifecycle()
    val quizXpScore by viewModel.quizXpScore.collectAsStateWithLifecycle()
    val currentUid = remember(userProfile.isSignedIn) {
        runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
    }

    var selectedTab by rememberSaveable { mutableStateOf(AlphaGoTab.HOME) }
    var inputText by remember { mutableStateOf("") }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showRagDialog by remember { mutableStateOf(false) }
    var showModelSelectorDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showOpeningSplash by rememberSaveable { mutableStateOf(true) }
    val uiPrefs = remember {
        context.getSharedPreferences("alpha_go_ui_prefs", android.content.Context.MODE_PRIVATE)
    }
    var selectedGrade by rememberSaveable {
        mutableStateOf(uiPrefs.getString("selected_grade", "") ?: "")
    }

    val homeListState = rememberLazyListState()
    val activeListState = homeListState
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var autoFollowStream by remember { mutableStateOf(true) }
    val canScrollDown by remember {
        derivedStateOf { activeListState.canScrollForward }
    }

    // Track manual user scroll gestures so streaming never fights or blocks manual scrolling
    LaunchedEffect(activeListState) {
        snapshotFlow {
            activeListState.isScrollInProgress to activeListState.canScrollForward
        }.collect { (scrolling, canForward) ->
            if (scrolling && canForward) {
                autoFollowStream = false
            } else if (!canForward) {
                autoFollowStream = true
            }
        }
    }

    BackHandler(enabled = drawerState.isOpen && !isGeminiLiveActive) {
        coroutineScope.launch { drawerState.close() }
    }

    BackHandler(enabled = selectedTab != AlphaGoTab.HOME && !isGeminiLiveActive && !drawerState.isOpen) {
        selectedTab = AlphaGoTab.HOME
    }

    BackHandler(enabled = isGeminiLiveActive) {
        viewModel.endGeminiLiveCall()
    }

    // Scroll to bottom when a new message is sent/added
    LaunchedEffect(messages.size, is3DViewMode) {
        if (!isGeminiLiveActive && messages.isNotEmpty()) {
            autoFollowStream = true
            kotlinx.coroutines.yield()
            val total = activeListState.layoutInfo.totalItemsCount
            if (total > 0) {
                try {
                    activeListState.animateScrollToItem((total - 1).coerceAtLeast(0))
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (_: Exception) {}
            }
        }
    }

    // Follow streaming text smoothly only when user has not scrolled up
    LaunchedEffect(streamingMessage?.text?.length, isLoading, is3DViewMode) {
        if (!isGeminiLiveActive && autoFollowStream && !activeListState.isScrollInProgress) {
            kotlinx.coroutines.yield()
            val total = activeListState.layoutInfo.totalItemsCount
            if (total > 0) {
                try {
                    activeListState.scrollToItem((total - 1).coerceAtLeast(0))
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (_: Exception) {}
            }
        }
    }

    // Audio record permission launcher for Gemini Live call
    val geminiLivePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startGeminiLiveCall()
        } else {
            Toast.makeText(context, if (appLanguage == AppLanguage.ARABIC) "إذن الميكروفون مطلوب للتفاعل الصوتي" else "Microphone permission required for voice interaction", Toast.LENGTH_SHORT).show()
        }
    }

    // Audio record permission launcher for voice typing
    val voiceTypingPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceTyping()
        } else {
            Toast.makeText(context, "Audio permission required for voice typing", Toast.LENGTH_SHORT).show()
        }
    }

    // Android Photo Picker launcher for multimodal analysis (clamped to <= 1536px to prevent OOM)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val maxDimension = 1536
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                        val width = info.size.width
                        val height = info.size.height
                        if (width > maxDimension || height > maxDimension) {
                            val scale = maxDimension.toFloat() / maxOf(width, height).toFloat()
                            decoder.setTargetSize(
                                (width * scale).toInt().coerceAtLeast(1),
                                (height * scale).toInt().coerceAtLeast(1)
                            )
                        }
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, boundsOpts)
                    }
                    var sampleSize = 1
                    while (
                        boundsOpts.outWidth / sampleSize > maxDimension ||
                        boundsOpts.outHeight / sampleSize > maxDimension
                    ) {
                        sampleSize *= 2
                    }
                    val decodeOpts = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, decodeOpts)
                    } ?: throw IllegalStateException("Unable to open image stream")
                }
                viewModel.attachImage(bitmap)
                Toast.makeText(context, "Image attached for analysis", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Could not load image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (appLanguage == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    SideMenuDrawer(
                        userProfile = userProfile,
                        hasPersonalApiKey = customApiKey.isNotBlank(),
                        sessions = sessions,
                        currentSessionId = currentSessionId,
                        selectedGem = selectedGem,
                        onSelectGem = { gemId -> viewModel.selectGem(gemId) },
                        appLanguage = appLanguage,
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onSelectSession = { sid -> viewModel.switchSession(sid) },
                        onDeleteSession = { sid -> viewModel.deleteSession(sid) },
                        onClearAllChats = { viewModel.clearAllConversations() },
                        isSearchGroundingEnabled = isSearchGroundingEnabled,
                        onToggleSearchGrounding = { viewModel.toggleSearchGrounding() },
                        onOpenAuthDialog = { showAuthDialog = true },
                        onSignOut = { viewModel.signOut() },
                        onOpenContactDialog = { showContactDialog = true },
                        onOpenSettingsDialog = { showSettingsDialog = true },
                        onOpenApiKeyDialog = { showApiKeyDialog = true },
                        onOpenRagDialog = { selectedTab = AlphaGoTab.LIBRARY },
                        onOpen3DArtifactsDialog = {},
                        onNewChatClick = {
                            selectedTab = AlphaGoTab.HOME
                            viewModel.startNewSession()
                        },
                        onVoiceCallClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                viewModel.startGeminiLiveCall()
                            } else {
                                geminiLivePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onCloseDrawer = {
                            coroutineScope.launch { drawerState.close() }
                        },
                        quizXpScore = quizXpScore,
                        onSelectTab = { tab -> selectedTab = tab }
                    )
                }
            ) {
                LiquidGlassBackground {
                    Scaffold(
                        containerColor = Color.Transparent,
                        topBar = {
                            RamsesHeader(
                                hasPersonalApiKey = customApiKey.isNotBlank(),
                                userProfile = userProfile,
                                appLanguage = appLanguage,
                                selectedModelName = selectedModel,
                                onModelClick = { showModelSelectorDialog = true },
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onProfileClick = { showAuthDialog = true },
                                onOpen3DArtifactsClick = {},
                                is3DAvatarMode = is3DViewMode,
                                onToggle3DAvatarMode = { viewModel.toggle3DViewMode() },
                                onApiKeyClick = { showApiKeyDialog = true },
                                onMenuClick = {
                                    coroutineScope.launch { drawerState.open() }
                                },
                                onLogoClick = { selectedTab = AlphaGoTab.HOME }
                            )
                        },
                bottomBar = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (selectedTab == AlphaGoTab.HOME) {
                            RamsesInputBar(
                                inputText = inputText,
                                onInputTextChanged = { inputText = it },
                                onSendMessage = { text ->
                                    viewModel.sendMessage(text)
                                    inputText = ""
                                },
                                onAttachClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onMicVoiceTypingClick = {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        if (isVoiceTyping) {
                                            viewModel.stopVoiceTyping()
                                        } else {
                                            viewModel.startVoiceTyping()
                                        }
                                    } else {
                                        voiceTypingPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                onGeminiLiveClick = {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.startGeminiLiveCall()
                                    } else {
                                        geminiLivePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                selectedImageBitmap = selectedImageBitmap,
                                onClearAttachedImage = { viewModel.clearAttachedImage() },
                                isListeningVoiceTyping = isVoiceTyping,
                                isSearchGroundingEnabled = isSearchGroundingEnabled,
                                onToggleSearchGrounding = { viewModel.toggleSearchGrounding() },
                                onOpenRagDialog = { selectedTab = AlphaGoTab.LIBRARY },
                                appLanguage = appLanguage
                            )
                        }
                        AlphaGoBottomBar(
                            selectedTab = selectedTab,
                            appLanguage = appLanguage,
                            onSelectTab = { selectedTab = it }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        AlphaGoTab.QUIZZES -> {
                            QuizzesTabView(
                                appLanguage = appLanguage,
                                xpScore = quizXpScore,
                                quizQuestions = quizQuestions,
                                cloudRagBooks = cloudRagBooks,
                                selectedBookId = selectedQuizBookId,
                                isGeneratingQuiz = isGeneratingQuiz,
                                quizGenerationError = quizGenerationError,
                                onSelectBookFilter = { bookId -> viewModel.selectQuizBookFilter(bookId) },
                                onGenerateQuizFromBooks = { bookId -> viewModel.generateQuizFromBooks(bookId) },
                                onEarnXp = { points -> viewModel.addQuizXp(points) },
                                onAskAlphaGoAboutQuiz = { prompt ->
                                    selectedTab = AlphaGoTab.HOME
                                    viewModel.sendMessage(prompt)
                                },
                                onGoToLibrary = { selectedTab = AlphaGoTab.LIBRARY }
                            )
                        }
                        AlphaGoTab.VIDEOS -> {
                            VideosTabView(
                                appLanguage = appLanguage,
                                videos = videos,
                                uploadState = videoUploadState,
                                currentUid = currentUid,
                                isUserSignedIn = userProfile.isSignedIn,
                                onSignInClick = { showAuthDialog = true },
                                onUploadVideo = { uri, title, desc, isShared ->
                                    viewModel.uploadVideo(uri, title, desc, isShared) { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                },
                                onDeleteVideo = { video ->
                                    viewModel.deleteVideo(video) { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onResetUploadState = { viewModel.resetVideoUploadState() },
                                onAskAlphaGoAboutVideo = { prompt ->
                                    selectedTab = AlphaGoTab.HOME
                                    viewModel.sendMessage(prompt)
                                }
                            )
                        }
                        AlphaGoTab.LIBRARY -> {
                            LibraryTabView(
                                appLanguage = appLanguage,
                                cloudRagBooks = cloudRagBooks,
                                cloudRagSyncState = cloudRagSyncState,
                                currentUid = currentUid,
                                isUserSignedIn = userProfile.isSignedIn,
                                onSignInClick = { showAuthDialog = true },
                                onUploadAndConvertPdfs = { uris ->
                                    viewModel.convertAndUploadPdfsToCloudFirebase(uris) { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                },
                                onDeleteBook = { book ->
                                    viewModel.deleteCloudBook(book.bookId) { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onOpenFullRagHub = { showRagDialog = true },
                                onAskAlphaGoAboutBook = { prompt ->
                                    selectedTab = AlphaGoTab.HOME
                                    viewModel.sendMessage(prompt)
                                }
                            )
                        }
                        AlphaGoTab.HOME -> {
                            val hasMessages = messages.isNotEmpty() || streamingMessage != null
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 26.dp, vertical = 6.dp)
                            ) {
                                if (!hasMessages) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 4.dp, vertical = 14.dp),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        AlphaGoGlbStageCard(
                                            appLanguage = appLanguage,
                                            selectedGrade = selectedGrade,
                                            onSelectGrade = { grade ->
                                                selectedGrade = grade
                                                uiPrefs.edit().putString("selected_grade", grade).apply()
                                            }
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        val allDisplayedMessages = remember(messages, streamingMessage) {
                                            val streamMsg = streamingMessage
                                            val combined = if (
                                                streamMsg != null &&
                                                messages.none { it.id == streamMsg.id || "streaming_${it.id}" == streamMsg.id }
                                            ) {
                                                messages + streamMsg
                                            } else {
                                                messages
                                            }
                                            combined.distinctBy { it.id }
                                        }

                                        Box(modifier = Modifier.fillMaxSize()) {
                                            LazyColumn(
                                                state = homeListState,
                                                modifier = Modifier.fillMaxSize(),
                                                contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                items(allDisplayedMessages, key = { it.id }) { message ->
                                                    ChatMessageBubble(
                                                        message = message,
                                                        appLanguage = appLanguage,
                                                        onSpeakClick = { textToSpeak ->
                                                            viewModel.speakMessage(textToSpeak)
                                                        },
                                                        onOpenApiKeyClick = {
                                                            showApiKeyDialog = true
                                                        },
                                                        onRetryClick = {
                                                            val lastUserMessage = messages.findLast { it.sender == MessageSender.USER }
                                                            if (lastUserMessage != null) {
                                                                viewModel.sendMessage(lastUserMessage.text)
                                                            }
                                                        },
                                                        onDismissErrorClick = {
                                                            viewModel.deleteMessage(message.id)
                                                        }
                                                    )
                                                }

                                                if (isLoading && streamingMessage == null) {
                                                    item {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 8.dp),
                                                            horizontalArrangement = Arrangement.Start,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(16.dp))
                                                                    .background(SurfaceGlass)
                                                                    .border(1.dp, RoyalGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    CircularProgressIndicator(
                                                                        modifier = Modifier.size(16.dp),
                                                                        color = RoyalGold,
                                                                        strokeWidth = 2.dp
                                                                    )
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    Text(
                                                                        text = com.example.data.model.AppStrings.thinkingMessage(appLanguage, false),
                                                                        fontFamily = AmiriFontFamily,
                                                                        fontSize = 13.sp,
                                                                        color = RoyalGold
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                item(key = "chat_bottom_anchor_home") {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                }
                                            }

                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = canScrollDown,
                                                enter = fadeIn(),
                                                exit = fadeOut(),
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(end = 8.dp, bottom = 8.dp)
                                            ) {
                                                SmallFloatingActionButton(
                                                    onClick = {
                                                        autoFollowStream = true
                                                        coroutineScope.launch {
                                                            val total = homeListState.layoutInfo.totalItemsCount
                                                            if (total > 0) {
                                                                try {
                                                                    homeListState.animateScrollToItem((total - 1).coerceAtLeast(0))
                                                                } catch (e: kotlinx.coroutines.CancellationException) {
                                                                    throw e
                                                                } catch (_: Exception) {}
                                                            }
                                                        }
                                                    },
                                                    shape = CircleShape,
                                                    containerColor = RoyalGold,
                                                    contentColor = PharaonicMidnight,
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .testTag("scroll_to_bottom_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.KeyboardArrowDown,
                                                        contentDescription = stringResource(R.string.scroll_to_bottom_desc)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                }
            }

            // Full-Screen Full-Duplex Gemini Live Voice Overlay - Above Drawer & Scaffold
            if (isGeminiLiveActive) {
                val liveSpeech = liveModelSpeechTranscript.takeIf { it.isNotBlank() }
                    ?: streamingMessage?.text?.takeIf { it.isNotBlank() }
                    ?: messages.lastOrNull { it.sender == com.example.data.model.MessageSender.RAMSES }?.text
                    ?: ""

                GeminiLiveVoiceScreen(
                    voiceCallState = voiceCallState,
                    lastTranscript = currentVoiceTranscript,
                    lastRamsesSpeech = liveSpeech,
                    rmsDb = liveRmsDb,
                    appLanguage = appLanguage,
                    rigConfig = avatarRigConfig,
                    selectedGem = selectedGem,
                    isNativeLiveConnected = liveConnectionStatus == GeminiLiveWebSocketClient.LiveConnectionStatus.CONNECTED,
                    isConnectingLive = liveConnectionStatus == GeminiLiveWebSocketClient.LiveConnectionStatus.CONNECTING,
                    isMicMuted = isLiveMicMuted,
                    isSpeakerMuted = isLiveSpeakerMuted,
                    onToggleMicMute = { viewModel.toggleLiveMicMute() },
                    onToggleSpeakerMute = { viewModel.toggleLiveSpeakerMute() },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onStartListening = { viewModel.startListeningForGeminiLive() },
                    onStopListening = { viewModel.stopListeningForGeminiLive() },
                    onStopSpeaking = { viewModel.stopSpeaking() },
                    onQuickPromptClick = { prompt -> viewModel.sendLiveVoicePrompt(prompt) },
                    onEndCall = { viewModel.endGeminiLiveCall() }
                )
            }

            // Contact Us Dialog (from Control Center drawer)
            if (showContactDialog) {
                ContactUsDialog(
                    appLanguage = appLanguage,
                    onDismiss = { showContactDialog = false }
                )
            }

            // App Settings Dialog (from Control Center drawer)
            if (showSettingsDialog) {
                AppSettingsDialog(
                    appLanguage = appLanguage,
                    hasPersonalApiKey = customApiKey.isNotBlank(),
                    isSearchGroundingEnabled = isSearchGroundingEnabled,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onToggleSearchGrounding = { viewModel.toggleSearchGrounding() },
                    onOpenApiKeyDialog = { showApiKeyDialog = true },
                    onStartVoiceCall = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            viewModel.startGeminiLiveCall()
                        } else {
                            geminiLivePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onNewChatClick = {
                        selectedTab = AlphaGoTab.HOME
                        viewModel.startNewSession()
                    },
                    onClearAllChats = { viewModel.clearAllConversations() },
                    onDismiss = { showSettingsDialog = false }
                )
            }

            // Google Sign-In & Profile Account Dialog
            if (showAuthDialog) {
                AuthSignInDialog(
                    userProfile = userProfile,
                    hasPersonalApiKey = customApiKey.isNotBlank(),
                    onSignInWithGoogle = { email, name ->
                        viewModel.signInWithGoogle(email, name)
                    },
                    onSignInWithApple = { email, name ->
                        viewModel.signInWithApple(email, name)
                    },
                    onSignOut = { viewModel.signOut() },
                    onDismiss = { showAuthDialog = false },
                    onGoogleSignInClick = {
                        val act = context as? android.app.Activity
                        if (act != null) {
                            viewModel.launchGoogleSignIn(act) { success, errorMsg ->
                                if (success) {
                                    showAuthDialog = false
                                    Toast.makeText(
                                        context,
                                        if (appLanguage == AppLanguage.ARABIC) "تم تسجيل الدخول بنجاح عبر Google" else "Successfully signed in with Google",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (errorMsg != null) {
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    onOpenApiKeyDialog = { showApiKeyDialog = true },
                    onUpdateLocalProfile = { name, uri ->
                        viewModel.updateLocalUserProfile(name, uri)
                    }
                )
            }

            // Dedicated API Key & Profile Photo Management Dialog
            if (showApiKeyDialog) {
                ApiKeyManagementDialog(
                    currentApiKey = customApiKey,
                    userProfile = userProfile,
                    onSaveApiKey = { newKey ->
                        viewModel.setCustomApiKey(newKey)
                    },
                    onUpdateLocalProfile = { name, uri ->
                        viewModel.updateLocalUserProfile(name, uri)
                    },
                    onDismiss = { showApiKeyDialog = false },
                    appLanguage = appLanguage
                )
            }

            // RAG Knowledge Dialog
            if (showRagDialog) {
                RagKnowledgeDialog(
                    documents = ragDocuments,
                    onAddDocument = { title, content, cat ->
                        viewModel.addRagDocument(title, content, cat)
                    },
                    onToggleDocument = { doc ->
                        viewModel.toggleRagDocument(doc)
                    },
                    onDeleteDocument = { id ->
                        viewModel.deleteRagDocument(id)
                    },
                    onDismiss = { showRagDialog = false },
                    pipelineState = fileSearchPipelineState,
                    linkedInstance = linkedSearchInstance,
                    searchConfig = searchInstanceConfig,
                    onUploadPdfUris = { uris ->
                        viewModel.uploadPdfBooksToCloudAndLink(uris) { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSaveSearchConfig = { updatedCfg ->
                        viewModel.updateSearchInstanceConfig(updatedCfg)
                    },
                    cloudRagBooks = cloudRagBooks,
                    cloudRagFolders = cloudRagFolders,
                    cloudRagSyncState = cloudRagSyncState,
                    isUserSignedIn = userProfile.isSignedIn,
                    onSignInClick = {
                        showRagDialog = false
                        showAuthDialog = true
                    },
                    onConvertPdfUrisToCloud = { uris ->
                        viewModel.convertAndUploadPdfsToCloudFirebase(uris) { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    onLoadFolderChunks = { folderId, callback ->
                        viewModel.loadChunksForFolderPreview(folderId, callback)
                    }
                )
            }

            // Google Gemini Model Selector Dialog (3.1 Flash Lite vs 3.8 Flash)
            if (showModelSelectorDialog) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { showModelSelectorDialog = false }
                ) {
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF1E1F20),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333538)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF8AB4F8),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (appLanguage == AppLanguage.ARABIC) "اختيار نموذج Gemini" else "Select Gemini Model",
                                        fontFamily = AmiriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = { showModelSelectorDialog = false },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Option 1: Gemini 3.1 Flash Lite (Sub-second ~0.5s TTFT)
                            val isFastFlash = selectedModel != "gemini-3.8-flash"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isFastFlash) Color(0x334285F4) else Color(0xFF282A2C))
                                    .border(1.dp, if (isFastFlash) Color(0xFF8AB4F8) else Color(0xFF3C4043), RoundedCornerShape(16.dp))
                                    .clickable {
                                        viewModel.selectModel("gemini-3.1-flash-lite")
                                        showModelSelectorDialog = false
                                    }
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Gemini 3.1 Flash Lite",
                                            fontFamily = CinzelFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (appLanguage == AppLanguage.ARABIC) "موصى به • فائق السرعة" else "Recommended • Ultra Fast",
                                            fontFamily = AmiriFontFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFF8AB4F8)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (appLanguage == AppLanguage.ARABIC) "أسرع استجابة لحظية للمحادثات اليومية والبرمجة والمكالمات الصوتية المباشرة." else "Fastest sub-second responses for daily chats, coding, and live stream voice calls.",
                                        fontFamily = AmiriFontFamily,
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Option 2: Gemini 3.8 Flash
                            val is38Flash = selectedModel == "gemini-3.8-flash"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (is38Flash) Color(0x334285F4) else Color(0xFF282A2C))
                                    .border(1.dp, if (is38Flash) Color(0xFF8AB4F8) else Color(0xFF3C4043), RoundedCornerShape(16.dp))
                                    .clickable {
                                        viewModel.selectModel("gemini-3.8-flash")
                                        showModelSelectorDialog = false
                                    }
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Gemini 3.8 Flash",
                                            fontFamily = CinzelFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (appLanguage == AppLanguage.ARABIC) "تفكير عميق • Firebase AI" else "Deep Reasoning • Firebase AI",
                                            fontFamily = AmiriFontFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFFF59E0B)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (appLanguage == AppLanguage.ARABIC) "الأفضل في التحليل التاريخي العميق، فهرسة كتب الـ RAG السحابية، والبحوث المفصلة." else "Best for deep Egyptology synthesis, Cloud Firebase RAG analysis, and detailed research.",
                                        fontFamily = AmiriFontFamily,
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Full-Screen Cosmic Purple & Gold Welcome / Start Journey Screen
            AnimatedVisibility(
                visible = showOpeningSplash,
                enter = fadeIn(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(320))
            ) {
                WelcomeJourneyScreen(
                    appLanguage = appLanguage,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onStartJourney = { showOpeningSplash = false }
                )
            }
        }
    }
}
