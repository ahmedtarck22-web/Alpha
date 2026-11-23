package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.db.AppDatabase
import android.net.Uri
import com.example.data.model.AppLanguage
import com.example.data.model.AuthProvider
import com.example.data.model.AvatarRigConfig
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSession
import com.example.data.model.CloudRagBook
import com.example.data.model.CloudRagChunk
import com.example.data.model.CloudRagFolder
import com.example.data.model.CloudRagSyncState
import com.example.data.model.CloudVideoItem
import com.example.data.model.CloudVideoUploadState
import com.example.data.model.FileSearchPipelineState
import com.example.data.model.GroundingSource
import com.example.data.model.LinkedSearchInstance
import com.example.data.model.MessageSender
import com.example.data.model.PdfBookUploadRequest
import com.example.data.model.RagDocument
import com.example.data.model.SearchInstanceConfig
import com.example.data.model.SearchLinkType
import com.example.data.model.UserProfile
import com.example.data.model.VoiceCallState
import com.example.data.repository.FirestoreChatRepository
import com.example.data.repository.FirestoreRagRepository
import com.example.data.repository.FirestoreVideoRepository
import com.example.firebase.FirebaseAuthManager
import com.example.security.SecureKeyStoreHelper
import com.example.service.GeminiLiveWebSocketClient
import com.example.service.PdfToCloudRagConverter
import com.example.service.RamsesFileSearchOrchestrator
import com.example.service.SpeechRecognizerHelper
import com.example.service.TextToSpeechHelper
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import android.util.Base64
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

data class BookQuizQuestion(
    val id: String,
    val questionAr: String,
    val questionEn: String,
    val optionsAr: List<String>,
    val optionsEn: List<String>,
    val correctIndex: Int,
    val explanationAr: String,
    val explanationEn: String,
    val bookId: String,
    val bookTitle: String,
    val pageRange: String
)

@OptIn(ExperimentalCoroutinesApi::class)
class RamsesViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ramses_ai_prefs", Context.MODE_PRIVATE)
    private val db = AppDatabase.getInstance(application)
    private val chatDao = db.chatDao()
    private val ragDao = db.ragDao()
    private val geminiClient = GeminiApiClient()
    val fileSearchOrchestrator = RamsesFileSearchOrchestrator(
        appContext = application.applicationContext,
        ragDao = ragDao,
        prefs = prefs
    )

    private val authManager by lazy {
        com.example.firebase.ensureFirebaseInitialized(application)
        FirebaseAuthManager.getInstance(application)
    }
    private val firestoreRepo by lazy {
        com.example.firebase.ensureFirebaseInitialized(application)
        FirestoreChatRepository.getInstance(application)
    }
    private val cloudRagRepo by lazy {
        com.example.firebase.ensureFirebaseInitialized(application)
        FirestoreRagRepository(application)
    }
    private val pdfCloudConverter by lazy {
        PdfToCloudRagConverter(application.applicationContext, cloudRagRepo)
    }
    private val cloudVideoRepo by lazy {
        com.example.firebase.ensureFirebaseInitialized(application)
        FirestoreVideoRepository(application)
    }

    val ttsHelper = TextToSpeechHelper(application)
    val speechHelper = SpeechRecognizerHelper(application)
    val geminiLiveClient = GeminiLiveWebSocketClient(application.applicationContext)

    // Language setting (Arabic / English)
    private val _appLanguage = MutableStateFlow(
        if (prefs.getString("app_language", "AR") == "EN") AppLanguage.ENGLISH else AppLanguage.ARABIC
    )
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    // Current Chat Session
    private val _currentSessionId = MutableStateFlow(
        prefs.getString("current_session_id", null) ?: UUID.randomUUID().toString()
    )
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    // Sessions List
    val sessions: StateFlow<List<ChatSession>> = chatDao.getAllSessions()
        .catch { e ->
            Log.e("RamsesVM", "Room getAllSessions error: ${e.message}", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Messages for Current Session
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // RAG Documents List (Eagerly loaded so sendMessage & Gemini Live have 0ms access)
    val ragDocuments: StateFlow<List<RagDocument>> = ragDao.getAllDocuments()
        .catch { e ->
            Log.e("RamsesVM", "Room getAllDocuments error: ${e.message}", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Vertex AI / Gemini File Search & Google Cloud Storage 5-PDF Pipeline State
    val fileSearchPipelineState: StateFlow<FileSearchPipelineState> = fileSearchOrchestrator.pipelineState
    val linkedSearchInstance: StateFlow<LinkedSearchInstance?> = fileSearchOrchestrator.linkedInstance
    val searchInstanceConfig: StateFlow<SearchInstanceConfig> = fileSearchOrchestrator.searchConfig

    // Cloud Firebase Small-Folder RAG State (5 Pre-Indexed Books + Up to 1,000+ Cloud PDF Books)
    private val _cloudRagBooks = MutableStateFlow<List<CloudRagBook>>(emptyList())
    val cloudRagBooks: StateFlow<List<CloudRagBook>> = _cloudRagBooks.asStateFlow()

    private val _cloudRagFolders = MutableStateFlow<List<CloudRagFolder>>(emptyList())
    val cloudRagFolders: StateFlow<List<CloudRagFolder>> = _cloudRagFolders.asStateFlow()

    private val _cloudRagSyncState = MutableStateFlow<CloudRagSyncState>(CloudRagSyncState.Idle)
    val cloudRagSyncState: StateFlow<CloudRagSyncState> = _cloudRagSyncState.asStateFlow()

    // Cloud Video Lessons State (Firebase Storage + Firestore /videos)
    val videos: StateFlow<List<CloudVideoItem>> = cloudVideoRepo
        .observeAccessibleVideos(authManager.currentUser.map { it?.uid })
        .catch { e ->
            Log.w("RamsesVM", "Cloud video listener notice: ${e.message}")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _videoUploadState = MutableStateFlow<CloudVideoUploadState>(CloudVideoUploadState.Idle)
    val videoUploadState: StateFlow<CloudVideoUploadState> = _videoUploadState.asStateFlow()

    // Strictly Book-Grounded Quizzes State (Populated dynamically from user-uploaded Library books)
    private val _quizQuestions = MutableStateFlow<List<BookQuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<BookQuizQuestion>> = _quizQuestions.asStateFlow()

    private val _selectedQuizBookId = MutableStateFlow<String?>(null)
    val selectedQuizBookId: StateFlow<String?> = _selectedQuizBookId.asStateFlow()

    private val _isGeneratingQuiz = MutableStateFlow(false)
    val isGeneratingQuiz: StateFlow<Boolean> = _isGeneratingQuiz.asStateFlow()

    private val _quizGenerationError = MutableStateFlow<String?>(null)
    val quizGenerationError: StateFlow<String?> = _quizGenerationError.asStateFlow()

    // 3D Avatar Rig & Custom .GLB Model Config
    private val _avatarRigConfig = MutableStateFlow(
        AvatarRigConfig.fromJsonString(prefs.getString("avatar_rig_config_json", null))
    )
    val avatarRigConfig: StateFlow<AvatarRigConfig> = _avatarRigConfig.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSearchGroundingEnabled = MutableStateFlow(false)
    val isSearchGroundingEnabled: StateFlow<Boolean> = _isSearchGroundingEnabled.asStateFlow()

    private val _selectedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedImageBitmap: StateFlow<Bitmap?> = _selectedImageBitmap.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Voice Call (Real Gemini Live WebSocket + Fallback) State
    private val _isGeminiLiveActive = MutableStateFlow(false)
    val isGeminiLiveActive: StateFlow<Boolean> = _isGeminiLiveActive.asStateFlow()

    private val _usingFallbackLocalVoice = MutableStateFlow(false)

    private val _voiceCallState = MutableStateFlow(VoiceCallState.IDLE)
    val voiceCallState: StateFlow<VoiceCallState> = _voiceCallState.asStateFlow()

    private val _currentVoiceTranscript = MutableStateFlow("")
    val currentVoiceTranscript: StateFlow<String> = _currentVoiceTranscript.asStateFlow()

    private val _liveModelSpeechTranscript = MutableStateFlow("")
    val liveModelSpeechTranscript: StateFlow<String> = _liveModelSpeechTranscript.asStateFlow()

    val liveConnectionStatus: StateFlow<GeminiLiveWebSocketClient.LiveConnectionStatus> =
        geminiLiveClient.connectionStatus
    val isLiveMicMuted: StateFlow<Boolean> = geminiLiveClient.isMicMuted
    val isLiveSpeakerMuted: StateFlow<Boolean> = geminiLiveClient.isSpeakerMuted

    // Unified RMS dB combining real Gemini Live 24kHz PCM audio RMS + SpeechRecognizer RMS
    val combinedRmsDb: StateFlow<Float> = combine(
        geminiLiveClient.liveRmsDb,
        speechHelper.rmsDb,
        _isGeminiLiveActive,
        _usingFallbackLocalVoice
    ) { wsRms, localRms, liveActive, isLocalFallback ->
        if (liveActive && !isLocalFallback) wsRms else localRms
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // Voice typing mode (Mic button in input bar)
    private val _isVoiceTyping = MutableStateFlow(false)
    val isVoiceTyping: StateFlow<Boolean> = _isVoiceTyping.asStateFlow()

    // 3D Avatar Split View mode (synchronized with selected Gem: RAMSES -> 3D Split, GEMINI_DEFAULT -> Alpha Go Open Chat)
    private val _is3DViewMode = MutableStateFlow(
        (prefs.getString("selected_gem", "GEMINI_DEFAULT") ?: "GEMINI_DEFAULT") == "RAMSES"
    )
    val is3DViewMode: StateFlow<Boolean> = _is3DViewMode.asStateFlow()

    // Persistent Quiz & Achievements XP Score
    private val _quizXpScore = MutableStateFlow(prefs.getInt("alpha_go_xp_score", 2450))
    val quizXpScore: StateFlow<Int> = _quizXpScore.asStateFlow()

    fun addQuizXp(points: Int) {
        if (points <= 0) return
        val next = _quizXpScore.value + points
        _quizXpScore.value = next
        prefs.edit().putInt("alpha_go_xp_score", next).apply()
    }

    // Auto-speak responses in 3D Avatar mode so Ramses speaks with lipsync and body movement
    private val _isAutoSpeakEnabled = MutableStateFlow(true)
    val isAutoSpeakEnabled: StateFlow<Boolean> = _isAutoSpeakEnabled.asStateFlow()

    val isSpeaking: StateFlow<Boolean> = combine(
        ttsHelper.isSpeaking,
        _voiceCallState,
        _isGeminiLiveActive
    ) { ttsSpeaking, callState, liveActive ->
        ttsSpeaking || (liveActive && callState == VoiceCallState.SPEAKING)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Decoupled in-memory live streaming message for 60/120 FPS buttery smooth rendering
    private val _streamingMessage = MutableStateFlow<ChatMessage?>(null)
    val streamingMessage: StateFlow<ChatMessage?> = _streamingMessage.asStateFlow()

    private fun resolveEffectiveUserProfile(): UserProfile {
        val firebaseUser = authManager.currentUser.value
        val customAvatarPath = prefs.getString("custom_user_avatar_path", null)
            ?.takeIf { it.isNotBlank() && File(it).exists() }
        val customDisplayName = prefs.getString("custom_user_display_name", null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        val googlePhotoUrl = firebaseUser?.photoUrl?.toString()
            ?: firebaseUser?.providerData?.firstNotNullOfOrNull { it.photoUrl?.toString() }
        val effectiveAvatar = customAvatarPath ?: googlePhotoUrl
        val hasApiKey = GeminiApiClient.sanitizeApiKey(_customApiKey.value).isNotBlank()

        return if (firebaseUser != null) {
            UserProfile(
                email = firebaseUser.email,
                displayName = customDisplayName
                    ?: firebaseUser.displayName
                    ?: prefs.getString("user_display_name", "مستخدم مسجل")
                    ?: "مستخدم مسجل",
                provider = AuthProvider.GOOGLE,
                isSignedIn = true,
                avatarUrl = effectiveAvatar
            )
        } else if (hasApiKey || customAvatarPath != null || customDisplayName != null) {
            UserProfile(
                email = null,
                displayName = customDisplayName
                    ?: if (_appLanguage.value == AppLanguage.ARABIC) "مستخدم Gemini API" else "Gemini API User",
                provider = AuthProvider.GUEST,
                isSignedIn = false,
                avatarUrl = effectiveAvatar
            )
        } else {
            UserProfile(
                email = null,
                displayName = if (_appLanguage.value == AppLanguage.ARABIC) "مستخدم Alpha Go" else "Alpha Go Explorer",
                provider = AuthProvider.GUEST,
                isSignedIn = false,
                avatarUrl = null
            )
        }
    }

    // User authentication & local/API profile state
    private val _userProfile = MutableStateFlow(
        UserProfile(
            email = null,
            displayName = if (prefs.getString("app_language", "AR") == "EN") "Alpha Go Explorer" else "مستخدم Alpha Go",
            provider = AuthProvider.GUEST,
            isSignedIn = false
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Selected Gemini Model (preserve supported Gemini 3.x models; upgrade legacy 1.5/2.0 prefs)
    private val _selectedModel = MutableStateFlow(
        prefs.getString("selected_gemini_model", "gemini-3.1-flash-lite").let { saved ->
            if (saved.isNullOrBlank() || !saved.startsWith("gemini-3")) {
                "gemini-3.1-flash-lite"
            } else {
                saved
            }
        }
    )
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    // Selected Gem Persona: "GEMINI_DEFAULT" (Alpha Go), "RAMSES", "CODE", "WRITER"
    private val _selectedGem = MutableStateFlow(
        prefs.getString("selected_gem", "GEMINI_DEFAULT") ?: "GEMINI_DEFAULT"
    )
    val selectedGem: StateFlow<String> = _selectedGem.asStateFlow()

    private var messageCollectorJob: Job? = null
    private var currentMessageJob: Job? = null
    private var cloudBooksListenerJob: Job? = null
    private var cloudFoldersListenerJob: Job? = null

    init {
        _customApiKey.value = GeminiApiClient.sanitizeApiKey(
            SecureKeyStoreHelper.readEncryptedPref(
                prefs = prefs,
                encryptedKey = "custom_gemini_key_enc",
                legacyPlainKey = "custom_gemini_key"
            )
        )
        _userProfile.value = resolveEffectiveUserProfile()
        _isSearchGroundingEnabled.value = prefs.getBoolean("search_grounding", false)
        GeminiApiClient.preferredModel = _selectedModel.value

        viewModelScope.launch(Dispatchers.IO) {
            removeLegacySeededRoomDocuments()
            ensureCurrentSessionExists(_currentSessionId.value)
            observeMessagesForSession(_currentSessionId.value)
        }

        viewModelScope.launch {
            authManager.currentUser.collect { user ->
                if (user != null) {
                    val resolvedName = user.displayName ?: "مستخدم مسجل"
                    prefs.edit()
                        .putString("user_display_name", resolvedName)
                        .putString("user_provider", "GOOGLE")
                        .remove("user_is_signed_in")
                        .apply()
                    _userProfile.value = resolveEffectiveUserProfile()
                    firestoreRepo.syncUserProfile(user)
                    launch(Dispatchers.IO) {
                        cloudRagRepo.purgeLegacyFiveSeedBooks()
                    }
                    startCloudRagListeners()
                } else {
                    stopCloudRagListenersAndClearLibrary()
                    _userProfile.value = resolveEffectiveUserProfile()
                }
            }
        }

        // Observe Real Gemini Live WebSocket states
        viewModelScope.launch {
            geminiLiveClient.voiceState.collect { wsVoiceState ->
                if (_isGeminiLiveActive.value && !_usingFallbackLocalVoice.value) {
                    _voiceCallState.value = wsVoiceState
                }
            }
        }

        viewModelScope.launch {
            geminiLiveClient.liveUserTranscript.collect { transcript ->
                if (_isGeminiLiveActive.value && !_usingFallbackLocalVoice.value && transcript.isNotBlank()) {
                    _currentVoiceTranscript.value = transcript
                }
            }
        }

        viewModelScope.launch {
            geminiLiveClient.liveModelTranscript.collect { modelSpeech ->
                if (_isGeminiLiveActive.value && !_usingFallbackLocalVoice.value) {
                    _liveModelSpeechTranscript.value = modelSpeech
                }
            }
        }

        geminiLiveClient.onTurnCompleted = { userSpoken, modelSpoken ->
            val sid = _currentSessionId.value
            viewModelScope.launch(Dispatchers.IO) {
                val uid = authManager.currentUser.value?.uid
                val now = System.currentTimeMillis()
                val titleSnippet = userSpoken.ifBlank { modelSpoken }.take(40)
                if (titleSnippet.isNotBlank()) {
                    chatDao.updateSessionTitleAndTimestamp(sid, titleSnippet, now)
                    if (uid != null) {
                        firestoreRepo.upsertSessionMetadata(sid, titleSnippet, uid)
                    }
                }
                if (userSpoken.isNotBlank()) {
                    val uEntity = ChatMessageEntity(
                        sessionId = sid,
                        sender = "USER",
                        text = userSpoken,
                        timestamp = now,
                        isVoiceResponse = true
                    )
                    chatDao.insertMessage(uEntity)
                    if (uid != null) firestoreRepo.saveMessage(uEntity, uid)
                }
                if (modelSpoken.isNotBlank()) {
                    val mEntity = ChatMessageEntity(
                        sessionId = sid,
                        sender = "RAMSES",
                        text = modelSpoken,
                        timestamp = now + 5L,
                        isVoiceResponse = true
                    )
                    chatDao.insertMessage(mEntity)
                    if (uid != null) firestoreRepo.saveMessage(mEntity, uid)
                }
            }
        }

        geminiLiveClient.onFallbackRequired = { reason ->
            Log.w("RamsesVM", "Switching to local voice fallback: $reason")
            if (_isGeminiLiveActive.value) {
                viewModelScope.launch {
                    startLocalFallbackVoiceLoop()
                }
            }
        }

        // Wire TTS state to local fallback Voice Call state
        viewModelScope.launch {
            ttsHelper.isSpeaking.collect { speaking ->
                if (_isGeminiLiveActive.value && _usingFallbackLocalVoice.value && speaking) {
                    _voiceCallState.value = VoiceCallState.SPEAKING
                }
            }
        }

        // Wire Speech recognition callbacks (used for chat Mic button + offline fallback)
        speechHelper.onSpeechCompleted = { text ->
            if (_isVoiceTyping.value) {
                _isVoiceTyping.value = false
                if (text.isNotBlank()) {
                    sendMessage(text)
                }
            } else if (_isGeminiLiveActive.value && _usingFallbackLocalVoice.value) {
                _currentVoiceTranscript.value = text
                sendMessage(text, isVoice = true)
            }
        }

        speechHelper.onPartialSpeech = { partial ->
            if (_isGeminiLiveActive.value && _usingFallbackLocalVoice.value && partial.isNotBlank()) {
                _currentVoiceTranscript.value = partial
            }
        }

        speechHelper.onEndOfSpeechDetected = {
            if (_isGeminiLiveActive.value && _usingFallbackLocalVoice.value && _voiceCallState.value == VoiceCallState.LISTENING) {
                _voiceCallState.value = VoiceCallState.THINKING
            }
        }

        speechHelper.onErrorOccurred = { errorMsg, errorCode ->
            Log.d("RamsesVM", "Speech error: $errorMsg (code $errorCode)")
            if (_isVoiceTyping.value) {
                _isVoiceTyping.value = false
            }
            val isRecoverable = errorCode == android.speech.SpeechRecognizer.ERROR_NO_MATCH ||
                errorCode == android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
                errorCode == android.speech.SpeechRecognizer.ERROR_NETWORK ||
                errorCode == android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT ||
                errorCode == android.speech.SpeechRecognizer.ERROR_AUDIO ||
                errorCode == android.speech.SpeechRecognizer.ERROR_SERVER ||
                errorCode == android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY

            if (_isGeminiLiveActive.value && _usingFallbackLocalVoice.value) {
                if (isRecoverable) {
                    viewModelScope.launch {
                        delay(650)
                        if (
                            _isGeminiLiveActive.value &&
                            _usingFallbackLocalVoice.value &&
                            _voiceCallState.value != VoiceCallState.SPEAKING &&
                            !ttsHelper.isSpeaking.value
                        ) {
                            _voiceCallState.value = VoiceCallState.LISTENING
                            val langTag = if (_appLanguage.value == AppLanguage.ENGLISH) "en-US" else "ar-EG"
                            speechHelper.startListening(langTag)
                        }
                    }
                } else {
                    _voiceCallState.value = VoiceCallState.IDLE
                }
            }
        }
    }

    fun saveAvatarRigConfig(config: AvatarRigConfig) {
        _avatarRigConfig.value = config
        prefs.edit().putString("avatar_rig_config_json", config.toJsonString()).apply()
    }

    fun setLanguage(lang: AppLanguage) {
        _appLanguage.value = lang
        prefs.edit().putString("app_language", if (lang == AppLanguage.ENGLISH) "EN" else "AR").apply()
        if (_isGeminiLiveActive.value) {
            if (!_usingFallbackLocalVoice.value) {
                // Reconnect live session seamlessly with new language system instruction
                startGeminiLiveCall()
            } else if (_voiceCallState.value == VoiceCallState.LISTENING) {
                val langCode = if (lang == AppLanguage.ENGLISH) "en-US" else "ar-EG"
                speechHelper.startListening(langCode)
            }
        }
    }

    fun toggleLanguage() {
        val next = if (_appLanguage.value == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
        setLanguage(next)
    }

    private suspend fun ensureCurrentSessionExists(sessionId: String) {
        val existing = chatDao.getSessionById(sessionId)
        if (existing == null) {
            val sessionTitle = if (_appLanguage.value == AppLanguage.ARABIC) "محادثة Alpha Go" else "Alpha Go Chat"
            val session = ChatSession(
                id = sessionId,
                title = sessionTitle,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            chatDao.insertSession(session)
            val uid = authManager.currentUser.value?.uid
            if (uid != null) {
                firestoreRepo.saveSession(session, uid)
            }
        }
        prefs.edit().putString("current_session_id", sessionId).apply()
    }

    private fun observeMessagesForSession(sessionId: String) {
        messageCollectorJob?.cancel()
        messageCollectorJob = viewModelScope.launch {
            chatDao.getMessagesForSession(sessionId)
                .catch { e ->
                    Log.e("RamsesVM", "Room getMessagesForSession error: ${e.message}", e)
                    emit(emptyList())
                }
                .collect { entities ->
                    val mapped = entities.map { entity ->
                        ChatMessage(
                            id = entity.id,
                            sender = if (entity.sender == "USER") MessageSender.USER else MessageSender.RAMSES,
                            text = entity.text,
                            timestamp = entity.timestamp,
                            imageBase64 = entity.imageBase64,
                            groundingSources = GroundingSource.fromJsonString(entity.groundingSourcesJson),
                            isVoiceResponse = entity.isVoiceResponse,
                            isError = entity.isError,
                            responderPersona = entity.responderPersona
                        )
                    }.distinctBy { it.id }

                    val currentStreamId = _streamingMessage.value?.id
                    if (currentStreamId != null && mapped.any { "streaming_${it.id}" == currentStreamId || it.id == currentStreamId }) {
                        _streamingMessage.value = null
                    }
                    _messages.value = mapped
                }
        }
    }

    private suspend fun removeLegacySeededRoomDocuments() {
        runCatching {
            val existing = ragDao.getActiveDocuments()
            for (doc in existing) {
                if (isSeededEgyptologyDoc(doc)) {
                    ragDao.deleteDocument(doc.id)
                }
            }
        }
    }

    // --- RAG Document Management ---
    fun addRagDocument(title: String, content: String, category: String = "عام") {
        viewModelScope.launch(Dispatchers.IO) {
            val newDoc = RagDocument(
                title = title,
                content = content,
                category = category,
                isEnabled = true
            )
            ragDao.insertDocument(newDoc)
        }
    }

    fun toggleRagDocument(document: RagDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            ragDao.updateDocument(document.copy(isEnabled = !document.isEnabled))
        }
    }

    fun deleteRagDocument(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            ragDao.deleteDocument(id)
        }
    }

    /**
     * Uploads up to 5 PDF books to Google Cloud Storage (`gs://...`) and links them
     * to the configured Vertex AI / Gemini File Search instance.
     */
    fun uploadPdfBooksToCloudAndLink(
        uris: List<Uri>,
        enforceExactFive: Boolean = false,
        onFinished: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val result = fileSearchOrchestrator.uploadUrisAndLinkToSearch(
                uris = uris,
                customApiKey = _customApiKey.value,
                enforceExactFive = enforceExactFive
            )
            result.fold(
                onSuccess = { instance ->
                    val msg = if (_appLanguage.value == AppLanguage.ARABIC) {
                        "تم رفع وربط ${instance.books.size} كتب PDF بنجاح بـ ${instance.storeResourceName}"
                    } else {
                        "Uploaded & linked ${instance.books.size} PDF books to ${instance.storeResourceName}"
                    }
                    onFinished(true, msg)
                },
                onFailure = { err ->
                    onFinished(false, err.localizedMessage ?: "PDF Cloud Storage / File Search error")
                }
            )
        }
    }

    /**
     * Programmatic entry point to upload the 5 PDF book requests to Google Cloud Storage
     * and link them to the Search instance.
     */
    fun uploadFivePdfBooksAndLink(
        requests: List<PdfBookUploadRequest>,
        gcpAccessToken: String? = null,
        onFinished: (Result<LinkedSearchInstance>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = fileSearchOrchestrator.uploadRequestsAndLinkToSearch(
                requests = requests,
                customApiKey = _customApiKey.value,
                gcpAccessToken = gcpAccessToken,
                enforceExactFive = true
            )
            onFinished(res)
        }
    }

    fun updateSearchInstanceConfig(config: SearchInstanceConfig) {
        fileSearchOrchestrator.updateSearchInstanceConfig(config)
    }

    // --- Cloud Firebase Small-Folder RAG (User-Uploaded Books Only) ---
    private fun startCloudRagListeners() {
        cloudBooksListenerJob?.cancel()
        cloudFoldersListenerJob?.cancel()

        cloudBooksListenerJob = viewModelScope.launch(Dispatchers.IO) {
            cloudRagRepo.observeAccessibleBooks()
                .catch { e ->
                    Log.w("RamsesVM", "Cloud RAG books listener error: ${e.message}", e)
                }
                .collect { cloudBooks ->
                    _cloudRagBooks.value = cloudBooks
                    val folders = _cloudRagFolders.value
                    _cloudRagSyncState.value = CloudRagSyncState.Ready(
                        syncedBooksCount = cloudBooks.size,
                        syncedFoldersCount = folders.size,
                        syncedChunksCount = cloudBooks.sumOf { it.chunkCount },
                        isCloudConnected = true,
                        statusMessage = if (cloudBooks.isEmpty()) {
                            "المكتبة السحابية فارغة وجاهزة لرفع كتبك بصيغة PDF"
                        } else {
                            "متصل بسحابة Firebase • ${cloudBooks.size} كتب و${folders.size} مجلداً جاهزة"
                        }
                    )
                }
        }

        cloudFoldersListenerJob = viewModelScope.launch(Dispatchers.IO) {
            cloudRagRepo.observeAccessibleFolders()
                .catch { e ->
                    Log.w("RamsesVM", "Cloud RAG folders listener error: ${e.message}", e)
                }
                .collect { cloudFolders ->
                    _cloudRagFolders.value = cloudFolders
                }
        }
    }

    private fun stopCloudRagListenersAndClearLibrary() {
        cloudBooksListenerJob?.cancel()
        cloudBooksListenerJob = null
        cloudFoldersListenerJob?.cancel()
        cloudFoldersListenerJob = null

        _cloudRagBooks.value = emptyList()
        _cloudRagFolders.value = emptyList()
        _quizQuestions.value = emptyList()
        _cloudRagSyncState.value = CloudRagSyncState.Idle
    }

    fun convertAndUploadPdfsToCloudFirebase(
        uris: List<Uri>,
        onFinished: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (uris.isEmpty()) return
        if (authManager.currentUser.value == null) {
            val msg = "يرجى تسجيل الدخول بحساب Google أولاً لتحويل الكتب ورفعها إلى سحابة Firebase."
            _cloudRagSyncState.value = CloudRagSyncState.Error(msg)
            onFinished(false, msg)
            return
        }
        viewModelScope.launch {
            val result = pdfCloudConverter.convertAndUploadPdfUris(uris) { msg, step, total ->
                _cloudRagSyncState.value = CloudRagSyncState.Syncing(
                    progressMessage = msg,
                    currentStep = step,
                    totalSteps = total
                )
            }
            result.fold(
                onSuccess = { convertedCount ->
                    val books = _cloudRagBooks.value
                    val folders = _cloudRagFolders.value
                    val successMsg = "تم تحويل ورفع $convertedCount كتاب PDF إلى مجلدات صغيرة ونسخ RAG على سحابة Firebase بنجاح!"
                    _cloudRagSyncState.value = CloudRagSyncState.Ready(
                        syncedBooksCount = books.size,
                        syncedFoldersCount = folders.size,
                        syncedChunksCount = books.sumOf { it.chunkCount },
                        isCloudConnected = true,
                        statusMessage = successMsg
                    )
                    onFinished(true, successMsg)
                },
                onFailure = { err ->
                    val errMsg = err.localizedMessage ?: "تعذر تحويل ملف الـ PDF إلى نسخ RAG سحابية"
                    _cloudRagSyncState.value = CloudRagSyncState.Error(errMsg)
                    onFinished(false, errMsg)
                }
            )
        }
    }

    fun loadChunksForFolderPreview(
        folderId: String,
        onResult: (List<CloudRagChunk>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val cloudChunks = if (authManager.currentUser.value != null) {
                cloudRagRepo.getChunksForFolder(folderId).getOrNull().orEmpty()
            } else {
                emptyList()
            }
            withContext(Dispatchers.Main) {
                onResult(cloudChunks)
            }
        }
    }

    fun setCloudBookShared(
        bookId: String,
        isShared: Boolean,
        onFinished: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val res = cloudRagRepo.setBookShared(bookId, isShared)
            res.fold(
                onSuccess = {
                    onFinished(true, if (isShared) "تم تفعيل مشاركة الكتاب." else "تم إيقاف مشاركة الكتاب.")
                },
                onFailure = { err ->
                    onFinished(false, err.localizedMessage ?: "تعذر تحديث صلاحيات المشاركة.")
                }
            )
        }
    }

    fun deleteCloudBook(
        bookId: String,
        onFinished: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val res = cloudRagRepo.deleteBookBundle(bookId)
            res.fold(
                onSuccess = {
                    if (_selectedQuizBookId.value == bookId) {
                        _selectedQuizBookId.value = null
                    }
                    onFinished(true, "تم حذف الكتاب ومجلداته وفقراته من السحابة.")
                },
                onFailure = { err -> onFinished(false, err.localizedMessage ?: "تعذر حذف الكتاب.") }
            )
        }
    }

    // --- Session Management ---
    fun startNewSession() {
        currentMessageJob?.cancel()
        _streamingMessage.value = null
        _isLoading.value = false
        val newId = UUID.randomUUID().toString()
        _currentSessionId.value = newId
        viewModelScope.launch(Dispatchers.IO) {
            ensureCurrentSessionExists(newId)
            observeMessagesForSession(newId)
        }
    }

    fun selectSession(sessionId: String) {
        if (_currentSessionId.value == sessionId) return
        currentMessageJob?.cancel()
        _streamingMessage.value = null
        _isLoading.value = false
        _currentSessionId.value = sessionId
        prefs.edit().putString("current_session_id", sessionId).apply()
        observeMessagesForSession(sessionId)
    }

    fun switchSession(sessionId: String) = selectSession(sessionId)

    fun renameSession(sessionId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                chatDao.renameSession(sessionId, newTitle.trim())
                val uid = authManager.currentUser.value?.uid
                if (uid != null) {
                    firestoreRepo.renameSession(sessionId, newTitle.trim(), uid)
                }
            } catch (e: Exception) {
                Log.e("RamsesVM", "Failed to rename session", e)
            }
        }
    }

    fun deleteSession(sessionId: String) {
        if (_currentSessionId.value == sessionId) {
            currentMessageJob?.cancel()
            _streamingMessage.value = null
            _isLoading.value = false
        }
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.deleteSession(sessionId)
            chatDao.deleteMessagesForSession(sessionId)
            val uid = authManager.currentUser.value?.uid
            if (uid != null) {
                firestoreRepo.deleteSession(sessionId, uid)
            }
            if (_currentSessionId.value == sessionId) {
                startNewSession()
            }
        }
    }

    fun clearAllConversations() {
        currentMessageJob?.cancel()
        _streamingMessage.value = null
        _isLoading.value = false
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.clearAllMessages()
            chatDao.clearAllSessions()
            val uid = authManager.currentUser.value?.uid
            if (uid != null) {
                firestoreRepo.clearAllSessions(uid)
            }
            startNewSession()
        }
    }

    fun deleteMessage(messageId: String) {
        val sid = _currentSessionId.value
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.deleteMessage(messageId)
            val uid = authManager.currentUser.value?.uid
            if (uid != null) {
                firestoreRepo.deleteMessage(sid, messageId, uid)
            }
        }
    }

    fun selectModel(modelName: String) {
        _selectedModel.value = modelName
        GeminiApiClient.preferredModel = modelName
        prefs.edit().putString("selected_gemini_model", modelName).apply()
    }

    fun selectGem(gemId: String) {
        runCatching { ttsHelper.stop() }
        stopVoiceTyping()
        _selectedGem.value = gemId
        prefs.edit().putString("selected_gem", gemId).apply()
        val target3D = (gemId == "RAMSES")
        if (_is3DViewMode.value != target3D) {
            _is3DViewMode.value = target3D
        }
        startNewSession()
    }

    private fun encodeBitmapForChatBubble(bitmap: Bitmap?, maxDim: Int = 720): String? {
        if (bitmap == null || bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return null
        return try {
            val soft = if (
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                bitmap.config == Bitmap.Config.HARDWARE
            ) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return null
            } else {
                bitmap
            }
            val scaled = if (soft.width > maxDim || soft.height > maxDim) {
                val ratio = minOf(maxDim.toFloat() / soft.width.toFloat(), maxDim.toFloat() / soft.height.toFloat())
                Bitmap.createScaledBitmap(
                    soft,
                    (soft.width * ratio).toInt().coerceAtLeast(1),
                    (soft.height * ratio).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                soft
            }
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 76, out)
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        } catch (t: Throwable) {
            Log.w("RamsesVM", "Failed encoding attached image for bubble: ${t.message}")
            null
        }
    }

    private fun isSeededEgyptologyDoc(doc: RagDocument): Boolean {
        return doc.title.startsWith("معاهدة السلام المصرية الحيثية") ||
            doc.title.startsWith("معبد أبو سمبل وتعامد الشمس") ||
            doc.title.startsWith("المدينة العاصمة بر-رعمسيس")
    }

    // --- Ultra-Fast Sub-Second Message Streaming ---
    fun sendMessage(promptText: String, isVoice: Boolean = false) {
        val trimmed = promptText.trim()
        val image = _selectedImageBitmap.value
        if (trimmed.isEmpty() && image == null) return

        // If real Gemini Live WebSocket is active and this is a voice/live prompt, route directly over WebSocket!
        if (isVoice && _isGeminiLiveActive.value && !_usingFallbackLocalVoice.value) {
            geminiLiveClient.sendTextMessage(trimmed)
            return
        }

        currentMessageJob?.cancel()

        val sessionId = _currentSessionId.value
        val userText = trimmed.ifEmpty {
            if (_appLanguage.value == AppLanguage.ARABIC) "حلّل هذه الصورة بدقة وتفصيل" else "Analyze this image accurately and in detail"
        }

        // Go Alpha enforces a unified educational persona grounded strictly in Library books
        val effectivePersona = "ALPHA_GO"
        val encodedUserImage = encodeBitmapForChatBubble(image)

        val now = System.currentTimeMillis()
        val historySnapshot = _messages.value
        val uid = authManager.currentUser.value?.uid

        _selectedImageBitmap.value = null
        _isLoading.value = true
        if (isVoice) {
            _voiceCallState.value = VoiceCallState.THINKING
        }

        // Optimistically display user message immediately (with imageBase64 above analysis!) + prepare streaming bubble
        val userEntity = ChatMessageEntity(
            sessionId = sessionId,
            sender = "USER",
            text = userText,
            timestamp = now,
            imageBase64 = encodedUserImage
        )
        _messages.value = historySnapshot + ChatMessage(
            id = userEntity.id,
            sender = MessageSender.USER,
            text = userText,
            timestamp = now,
            imageBase64 = encodedUserImage
        )

        val tempRamsesId = UUID.randomUUID().toString()
        _streamingMessage.value = ChatMessage(
            id = "streaming_$tempRamsesId",
            sender = MessageSender.RAMSES,
            text = "",
            timestamp = now + 1L,
            isVoiceResponse = isVoice,
            isError = false,
            responderPersona = effectivePersona
        )

        // Persist user message & session metadata in background without delaying HTTP SSE stream!
        viewModelScope.launch(Dispatchers.IO) {
            try {
                chatDao.insertMessage(userEntity)
                chatDao.updateSessionTitleAndTimestamp(
                    sessionId = sessionId,
                    title = userText.take(40),
                    updatedAt = now
                )
                if (uid != null) {
                    firestoreRepo.upsertSessionMetadata(
                        sessionId = sessionId,
                        title = userText.take(40),
                        userId = uid
                    )
                    firestoreRepo.saveMessage(userEntity, uid)
                }
            } catch (e: Exception) {
                Log.w("RamsesVM", "Background user message persistence notice: ${e.message}")
            }
        }

        currentMessageJob = viewModelScope.launch {
            try {
                var currentLiveChunk = ""
                var lastUiEmitMs = 0L

                val onStreamChunk: suspend (String) -> Unit = { chunk ->
                    currentLiveChunk = chunk
                    val elapsed = SystemClock.uptimeMillis() - lastUiEmitMs
                    if (elapsed >= 24L || lastUiEmitMs == 0L) {
                        lastUiEmitMs = SystemClock.uptimeMillis()
                        _streamingMessage.value = _streamingMessage.value?.copy(text = chunk)
                    }
                }

                // Always search the Library books (5 bundled books + any uploaded Cloud PDFs)
                var retrievedPassages = cloudRagRepo.retrieveRelevantPassages(
                    query = userText,
                    cachedCloudFolders = _cloudRagFolders.value,
                    topFoldersLimit = if (isVoice) 3 else 6,
                    topChunksLimit = if (isVoice) 3 else 8
                )

                // Support short follow-up questions using the preceding user question
                if (retrievedPassages.isEmpty() && userText.split(Regex("\\s+")).size <= 6) {
                    val prevUserPrompt = historySnapshot.lastOrNull { it.sender == MessageSender.USER }?.text
                    if (!prevUserPrompt.isNullOrBlank()) {
                        retrievedPassages = cloudRagRepo.retrieveRelevantPassages(
                            query = "$prevUserPrompt $userText",
                            cachedCloudFolders = _cloudRagFolders.value,
                            topFoldersLimit = if (isVoice) 3 else 6,
                            topChunksLimit = if (isVoice) 3 else 8
                        )
                    }
                }

                val cloudRetrievedDocs = retrievedPassages.map { passage ->
                    RagDocument(
                        id = "cloud_chunk_${passage.chunk.chunkId}",
                        title = passage.toCitationTitle(),
                        content = passage.chunk.content,
                        category = "الصفحة: ${passage.chunk.formattedPageNumber()}",
                        isEnabled = true,
                        gcsUri = passage.toCitationUri()
                    )
                }

                // Only include custom user-added Room documents if they match the query (never leak generic seed docs into off-book queries)
                val queryStems = FirestoreRagRepository.extractSearchStems(userText)
                val matchingUserDocs = ragDocuments.value.filter { doc ->
                    doc.isEnabled && !isSeededEgyptologyDoc(doc) && run {
                        val docStems = FirestoreRagRepository.extractSearchStems("${doc.title} ${doc.content}")
                        queryStems.intersect(docStems).isNotEmpty()
                    }
                }

                // Allow questions asking directly about the Library book list / catalog
                val normalizedLower = userText.lowercase()
                val isLibraryCatalogQuery = listOf(
                    "الكتب الموجودة", "قائمة الكتب", "كم كتاب", "المكتبة", "أسماء الكتب",
                    "books in library", "list of books", "available books", "what books"
                ).any { normalizedLower.contains(it) } && _cloudRagBooks.value.isNotEmpty()

                val catalogDocs = if (cloudRetrievedDocs.isEmpty() && isLibraryCatalogQuery) {
                    _cloudRagBooks.value.map { book ->
                        RagDocument(
                            id = "catalog_${book.bookId}",
                            title = "${book.title} • الصفحة: 1 - ${book.pageCount}",
                            content = "الكتاب: ${book.title}، التصنيف: ${book.eraCategory}، عدد الصفحات: ${book.pageCount}. الملخص: ${book.summary}",
                            category = "فهرس المكتبة",
                            isEnabled = true,
                            gcsUri = "firestore://rag_books/${book.bookId}"
                        )
                    }
                } else {
                    emptyList()
                }

                val combinedRagDocs = cloudRetrievedDocs + matchingUserDocs + catalogDocs
                val cloudCitations = retrievedPassages.map { passage ->
                    GroundingSource(
                        title = passage.toCitationTitle(),
                        uri = passage.toCitationUri()
                    )
                }

                // Server-side RAG proxy only if configured AND relevant book chunks/PDFs exist
                val proxyConfigured = searchInstanceConfig.value.serverProxyBaseUrl.isNotBlank() &&
                    linkedSearchInstance.value?.linkType == SearchLinkType.SERVER_PROXY_INDEX &&
                    combinedRagDocs.any { it.isCloudPdfBook } &&
                    !isVoice && image == null

                val result = if (proxyConfigured) {
                    val sysInstruction = GeminiApiClient.buildEffectiveSystemInstruction(
                        gemPersona = effectivePersona,
                        appLanguage = _appLanguage.value,
                        isVoiceCall = false,
                        ragContext = combinedRagDocs,
                        prompt = userText,
                        hasImage = false
                    )
                    val proxyResp = fileSearchOrchestrator.queryLinkedKnowledgeBase(
                        prompt = userText,
                        systemInstruction = sysInstruction,
                        activeRagDocs = combinedRagDocs,
                        customApiKey = _customApiKey.value,
                        enableGoogleSearch = false,
                        onChunk = onStreamChunk
                    )
                    if (proxyResp != null && proxyResp.answerText.isNotBlank()) {
                        GeminiApiClient.GeminiResult(
                            text = proxyResp.answerText,
                            groundingSources = proxyResp.groundingSources,
                            isError = false
                        )
                    } else {
                        geminiClient.streamRamsesResponse(
                            prompt = userText,
                            history = historySnapshot,
                            imageBitmap = image,
                            enableSearchGrounding = false,
                            isVoiceCall = isVoice,
                            customApiKey = _customApiKey.value,
                            ragContext = combinedRagDocs,
                            appLanguage = _appLanguage.value,
                            gemPersona = effectivePersona,
                            onChunk = onStreamChunk
                        )
                    }
                } else {
                    geminiClient.streamRamsesResponse(
                        prompt = userText,
                        history = historySnapshot,
                        imageBitmap = image,
                        enableSearchGrounding = false,
                        isVoiceCall = isVoice,
                        customApiKey = _customApiKey.value,
                        ragContext = combinedRagDocs,
                        appLanguage = _appLanguage.value,
                        gemPersona = effectivePersona,
                        onChunk = onStreamChunk
                    )
                }

                val mergedGroundingSources = (cloudCitations + result.groundingSources)
                    .distinctBy { "${it.title}|${it.uri}" }

                val finalText = GeminiApiClient.stripHiddenControlTokens(
                    if (result.text.isNotBlank()) result.text else currentLiveChunk
                ).trim()
                val finalEntity = ChatMessageEntity(
                    id = tempRamsesId,
                    sessionId = sessionId,
                    sender = "RAMSES",
                    text = finalText,
                    timestamp = System.currentTimeMillis(),
                    isVoiceResponse = isVoice,
                    isError = result.isError,
                    groundingSourcesJson = GroundingSource.toJsonString(mergedGroundingSources),
                    responderPersona = effectivePersona
                )

                _streamingMessage.value = null
                _isLoading.value = false

                // Persist final AI message in background
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        chatDao.insertMessage(finalEntity)
                        if (uid != null) {
                            firestoreRepo.saveMessage(finalEntity, uid)
                        }
                    } catch (e: Exception) {
                        Log.w("RamsesVM", "Background AI message persistence notice: ${e.message}")
                    }
                }

                if (isVoice) {
                    speechHelper.stopListening()
                    _voiceCallState.value = VoiceCallState.SPEAKING
                    val langTag = if (_appLanguage.value == AppLanguage.ENGLISH) "en-US" else "ar-EG"

                    ttsHelper.speak(finalText, languageCode = langTag) {
                        viewModelScope.launch {
                            // Wait for loudspeaker reverb to decay before reopening SpeechRecognizer
                            delay(550)
                            if (
                                _isGeminiLiveActive.value &&
                                _usingFallbackLocalVoice.value &&
                                !ttsHelper.isSpeaking.value
                            ) {
                                _voiceCallState.value = VoiceCallState.LISTENING
                                speechHelper.startListening(langTag)
                            }
                        }
                    }
                } else if (_is3DViewMode.value && _isAutoSpeakEnabled.value && finalText.isNotBlank() && !result.isError) {
                    speechHelper.stopListening()
                    val langTag = if (_appLanguage.value == AppLanguage.ENGLISH) "en-US" else "ar-EG"
                    ttsHelper.speak(finalText.take(3500), languageCode = langTag)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    _streamingMessage.value = null
                    _isLoading.value = false
                    throw e
                }
                Log.e("RamsesVM", "Message generation error", e)
                _streamingMessage.value = null
                _isLoading.value = false

                val isArabic = _appLanguage.value == AppLanguage.ARABIC
                val safeDetail = SecureKeyStoreHelper.redactSecret(e.localizedMessage, _customApiKey.value)
                val errorMsg = if (isArabic) {
                    "حدث خطأ أثناء معالجة الطلب: ${safeDetail.ifBlank { "خطأ غير متوقع في الاتصال" }}"
                } else {
                    "An error occurred while processing your request: ${safeDetail.ifBlank { "Unexpected connection error" }}"
                }
                val errorEntity = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    sender = "RAMSES",
                    text = errorMsg,
                    timestamp = System.currentTimeMillis(),
                    isVoiceResponse = isVoice,
                    isError = true
                )
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        chatDao.insertMessage(errorEntity)
                    } catch (_: Exception) {}
                }
                if (isVoice) {
                    _voiceCallState.value = VoiceCallState.IDLE
                }
            }
        }
    }

    fun toggle3DViewMode() {
        _is3DViewMode.value = !_is3DViewMode.value
    }

    fun set3DViewMode(enabled: Boolean) {
        _is3DViewMode.value = enabled
    }

    fun toggleAutoSpeak() {
        _isAutoSpeakEnabled.value = !_isAutoSpeakEnabled.value
    }

    // --- Voice Typing (Mic Button in Chat) ---
    fun startVoiceTyping() {
        if (!_isVoiceTyping.value) {
            _isVoiceTyping.value = true
            ttsHelper.stop()
            val langTag = if (_appLanguage.value == AppLanguage.ENGLISH) "en-US" else "ar-EG"
            speechHelper.startListening(langTag)
        }
    }

    fun stopVoiceTyping() {
        if (_isVoiceTyping.value) {
            _isVoiceTyping.value = false
            speechHelper.stopListening()
        }
    }

    // --- Real Full-Duplex Gemini Live Voice Call ---
    fun startGeminiLiveCall() {
        _isGeminiLiveActive.value = true
        _currentVoiceTranscript.value = ""
        _liveModelSpeechTranscript.value = ""
        runCatching { ttsHelper.stop() }
        runCatching { speechHelper.stopListening() }

        try {
            val cloudBookSummaries = _cloudRagBooks.value.take(10).map { book ->
                RagDocument(
                    id = "live_book_${book.bookId}",
                    title = "${book.title} (${book.author})",
                    content = "${book.eraCategory} — ${book.summary}",
                    category = "مكتبة الكتب المفهرسة (${book.pageCount} ص)",
                    isEnabled = true
                )
            }
            val combinedLiveDocs = ragDocuments.value.filter { it.isEnabled } + cloudBookSummaries

            val wsStarted = geminiLiveClient.startSession(
                customApiKey = _customApiKey.value,
                appLanguage = _appLanguage.value,
                gemPersona = _selectedGem.value,
                ragDocs = combinedLiveDocs
            )

            if (wsStarted) {
                _usingFallbackLocalVoice.value = false
                _voiceCallState.value = VoiceCallState.THINKING
            } else {
                startLocalFallbackVoiceLoop()
            }
        } catch (t: Throwable) {
            Log.e("RamsesVM", "Gemini Live start notice, using safe local voice loop: ${t.message}")
            startLocalFallbackVoiceLoop()
        }
    }

    private fun startLocalFallbackVoiceLoop() {
        _usingFallbackLocalVoice.value = true
        speechHelper.stopListening()
        _voiceCallState.value = VoiceCallState.SPEAKING
        val isEnglish = _appLanguage.value == AppLanguage.ENGLISH
        val greeting = if (isEnglish) {
            "Go Alpha, welcome! I am listening—ask me anything from your Library books."
        } else {
            "جو ألفا، أهلاً بك! أنا أستمع إليك الآن، اسألني أي سؤال من داخل كتب المكتبة."
        }
        val langTag = if (isEnglish) "en-US" else "ar-EG"
        _liveModelSpeechTranscript.value = greeting

        val transitionedToListen = java.util.concurrent.atomic.AtomicBoolean(false)
        fun beginListeningSafely() {
            if (!transitionedToListen.compareAndSet(false, true)) return
            viewModelScope.launch {
                delay(450)
                if (_isGeminiLiveActive.value && _usingFallbackLocalVoice.value) {
                    _voiceCallState.value = VoiceCallState.LISTENING
                    speechHelper.startListening(langTag)
                }
            }
        }

        ttsHelper.speak(greeting, languageCode = langTag) {
            beginListeningSafely()
        }

        // Safety watchdog in case device OEM TTS engine lacks voice data or delays initialization
        viewModelScope.launch {
            delay(4500)
            if (_isGeminiLiveActive.value && _usingFallbackLocalVoice.value && !ttsHelper.isSpeaking.value) {
                beginListeningSafely()
            }
        }
    }

    fun endGeminiLiveCall() {
        _isGeminiLiveActive.value = false
        _usingFallbackLocalVoice.value = false
        _voiceCallState.value = VoiceCallState.IDLE
        _liveModelSpeechTranscript.value = ""
        geminiLiveClient.stopSession()
        ttsHelper.stop()
        speechHelper.stopListening()
    }

    fun toggleLiveMicMute() {
        if (!_usingFallbackLocalVoice.value) {
            geminiLiveClient.toggleMicMuted()
        }
    }

    fun toggleLiveSpeakerMute() {
        if (!_usingFallbackLocalVoice.value) {
            geminiLiveClient.toggleSpeakerMuted()
        } else {
            ttsHelper.stop()
        }
    }

    fun startListeningForGeminiLive() {
        if (!_usingFallbackLocalVoice.value) {
            geminiLiveClient.setMicMuted(false)
            geminiLiveClient.interruptModelSpeech()
        } else {
            _voiceCallState.value = VoiceCallState.LISTENING
            ttsHelper.stop()
            val langTag = if (_appLanguage.value == AppLanguage.ENGLISH) "en-US" else "ar-EG"
            speechHelper.startListening(langTag)
        }
    }

    fun stopListeningForGeminiLive() {
        if (!_usingFallbackLocalVoice.value) {
            geminiLiveClient.setMicMuted(true)
        } else {
            speechHelper.stopListening()
            if (_voiceCallState.value == VoiceCallState.LISTENING) {
                _voiceCallState.value = VoiceCallState.IDLE
            }
        }
    }

    fun sendLiveVoicePrompt(promptText: String) {
        if (_isGeminiLiveActive.value && !_usingFallbackLocalVoice.value) {
            geminiLiveClient.sendTextMessage(promptText)
        } else {
            _currentVoiceTranscript.value = promptText
            sendMessage(promptText, isVoice = true)
        }
    }

    // --- Helpers ---
    fun attachImage(bitmap: Bitmap) {
        _selectedImageBitmap.value = bitmap
    }

    fun clearAttachedImage() {
        _selectedImageBitmap.value = null
    }

    fun toggleSearchGrounding() {
        val newVal = !_isSearchGroundingEnabled.value
        _isSearchGroundingEnabled.value = newVal
        prefs.edit().putBoolean("search_grounding", newVal).apply()
    }

    fun speakMessage(text: String) {
        speechHelper.stopListening()
        val langTag = if (_appLanguage.value == AppLanguage.ENGLISH) "en-US" else "ar-EG"
        ttsHelper.speak(text, languageCode = langTag)
    }

    fun stopSpeaking() {
        ttsHelper.stop()
        if (_isGeminiLiveActive.value && !_usingFallbackLocalVoice.value) {
            geminiLiveClient.interruptModelSpeech()
        }
    }

    fun setCustomApiKey(key: String) {
        val cleanKey = GeminiApiClient.sanitizeApiKey(key)
        _customApiKey.value = cleanKey
        SecureKeyStoreHelper.saveEncryptedPref(
            prefs = prefs,
            encryptedKey = "custom_gemini_key_enc",
            legacyPlainKey = "custom_gemini_key",
            plainValue = cleanKey
        )
        _userProfile.value = resolveEffectiveUserProfile()
    }

    fun updateLocalUserProfile(displayName: String, avatarUri: Uri? = null) {
        val trimmedName = displayName.trim()
        if (trimmedName.isNotEmpty()) {
            prefs.edit().putString("custom_user_display_name", trimmedName).apply()
        }
        if (avatarUri == null) {
            _userProfile.value = resolveEffectiveUserProfile()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val destFile = File(app.filesDir, "user_profile_avatar_${System.currentTimeMillis()}.jpg")
                val copied = app.contentResolver.openInputStream(avatarUri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                    true
                } ?: false
                if (copied && destFile.exists() && destFile.length() > 0L) {
                    val prevPath = prefs.getString("custom_user_avatar_path", null)
                    if (!prevPath.isNullOrBlank() && prevPath != destFile.absolutePath) {
                        runCatching { File(prevPath).delete() }
                    }
                    prefs.edit().putString("custom_user_avatar_path", destFile.absolutePath).apply()
                }
            } catch (e: Exception) {
                Log.w("RamsesVM", "Could not save profile avatar photo: ${e.message}")
            }
            withContext(Dispatchers.Main) {
                _userProfile.value = resolveEffectiveUserProfile()
            }
        }
    }

    fun launchGoogleSignIn(activity: Activity, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authManager.signInWithGoogle(activity)
            _isLoading.value = false
            if (result.isSuccess) {
                val user = result.getOrThrow()
                _userProfile.value = resolveEffectiveUserProfile()
                firestoreRepo.syncUserProfile(user)
                onComplete(true, null)
            } else {
                val ex = result.exceptionOrNull()
                val msg = if (ex is GetCredentialCancellationException) {
                    if (_appLanguage.value == AppLanguage.ARABIC) "تم إلغاء تسجيل الدخول" else "Sign-in cancelled"
                } else {
                    ex?.localizedMessage ?: "Failed to sign in"
                }
                onComplete(false, msg)
            }
        }
    }

    fun signInWithGoogle(email: String = "", name: String = "") {
        val verifiedUser = authManager.currentUser.value ?: return
        if (name.isNotBlank()) {
            prefs.edit().putString("custom_user_display_name", name.trim()).apply()
        }
        _userProfile.value = resolveEffectiveUserProfile().copy(
            email = verifiedUser.email ?: email.ifBlank { null }
        )
    }

    fun signInWithApple(email: String = "", name: String = "") {
        // Disabled per Google Play policy - Google Sign-In via Credential Manager is the supported provider
    }

    fun signOut() {
        authManager.signOut()
        val prevAvatar = prefs.getString("custom_user_avatar_path", null)
        if (!prevAvatar.isNullOrBlank()) {
            runCatching { File(prevAvatar).delete() }
        }
        prefs.edit()
            .remove("user_email")
            .remove("custom_user_display_name")
            .remove("custom_user_avatar_path")
            .putString("user_provider", "GUEST")
            .remove("user_is_signed_in")
            .apply()
        _userProfile.value = resolveEffectiveUserProfile()
    }

    // --- Cloud Video Upload & Playback Management ---
    fun uploadVideo(
        uri: Uri,
        title: String,
        description: String,
        isShared: Boolean = true,
        onFinished: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (authManager.currentUser.value == null) {
            val msg = "يرجى تسجيل الدخول بحساب Google أولاً لرفع الفيديوهات إلى السحابة."
            _videoUploadState.value = CloudVideoUploadState.Error(msg)
            onFinished(false, msg)
            return
        }
        viewModelScope.launch {
            _videoUploadState.value = CloudVideoUploadState.Uploading(
                progressMessage = "جارٍ تجهيز الفيديو للرفع إلى Firebase Storage...",
                fraction = 0.05f
            )
            val result = cloudVideoRepo.uploadVideo(
                uri = uri,
                title = title,
                description = description,
                isShared = isShared,
                onProgress = { fraction, msg ->
                    _videoUploadState.value = CloudVideoUploadState.Uploading(
                        progressMessage = msg.ifBlank { "جارٍ رفع الفيديو... ${(fraction * 100).toInt()}%" },
                        fraction = fraction
                    )
                }
            )
            result.fold(
                onSuccess = { uploaded ->
                    val okMsg = "تم رفع الفيديو «${uploaded.title}» بنجاح!"
                    _videoUploadState.value = CloudVideoUploadState.Success(uploaded)
                    onFinished(true, okMsg)
                },
                onFailure = { err ->
                    val errMsg = err.localizedMessage ?: "تعذر رفع الفيديو إلى السحابة"
                    _videoUploadState.value = CloudVideoUploadState.Error(errMsg)
                    onFinished(false, errMsg)
                }
            )
        }
    }

    fun deleteVideo(
        video: CloudVideoItem,
        onFinished: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val res = cloudVideoRepo.deleteVideo(video)
            res.fold(
                onSuccess = { onFinished(true, "تم حذف الفيديو بنجاح.") },
                onFailure = { err -> onFinished(false, err.localizedMessage ?: "تعذر حذف الفيديو.") }
            )
        }
    }

    fun resetVideoUploadState() {
        _videoUploadState.value = CloudVideoUploadState.Idle
    }

    // --- Strictly Book-Grounded Quiz Generator ---
    fun selectQuizBookFilter(bookId: String?) {
        _selectedQuizBookId.value = bookId
        _quizGenerationError.value = null
        if (_cloudRagBooks.value.isNotEmpty()) {
            generateQuizFromBooks(bookId)
        } else {
            _quizQuestions.value = emptyList()
        }
    }

    fun generateQuizFromBooks(bookId: String? = _selectedQuizBookId.value) {
        if (_isGeneratingQuiz.value) return
        viewModelScope.launch {
            _isGeneratingQuiz.value = true
            _quizGenerationError.value = null
            try {
                val passages = cloudRagRepo.getBookPassagesForQuiz(bookId = bookId, limit = 8)
                if (passages.isEmpty()) {
                    _quizQuestions.value = emptyList()
                    _quizGenerationError.value = if (_appLanguage.value == AppLanguage.ARABIC) {
                        "لا توجد كتب مرفوعة بعد. يرجى رفع كتاب PDF أولاً من تبويب «المكتبة» لتوليد اختبارات من داخله."
                    } else {
                        "No uploaded books found yet. Please upload a PDF book in the Library tab first."
                    }
                    return@launch
                }

                val booksById = _cloudRagBooks.value.associateBy { it.bookId }
                val excerptsText = passages.take(6).joinToString("\n\n") { chunk ->
                    val bTitle = booksById[chunk.bookId]?.title ?: chunk.folderId
                    "=== كتاب: $bTitle | الصفحات: ${chunk.formattedPageNumber()} | المعرف: ${chunk.bookId} ===\n${chunk.content.take(900)}"
                }

                val aiGenerated = try {
                    withTimeout(60_000L) {
                        val quizSchema = Schema.obj(
                            mapOf(
                                "questions" to Schema.array(
                                    Schema.obj(
                                        mapOf(
                                            "questionAr" to Schema.string("Question in Arabic strictly from the book excerpt"),
                                            "questionEn" to Schema.string("Question in English strictly from the book excerpt"),
                                            "optionsAr" to Schema.array(Schema.string("4 Arabic answer choices")),
                                            "optionsEn" to Schema.array(Schema.string("4 English answer choices")),
                                            "correctIndex" to Schema.integer("0-based index of the single correct option"),
                                            "explanationAr" to Schema.string("Short explanation from the book citing the exact fact"),
                                            "explanationEn" to Schema.string("Short explanation in English citing the exact fact"),
                                            "bookId" to Schema.string("bookId matching the source excerpt"),
                                            "bookTitle" to Schema.string("Exact Arabic title of the source book"),
                                            "pageRange" to Schema.string("Page range from source e.g. ص 5 - 6")
                                        )
                                    )
                                )
                            )
                        )
                        val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                            modelName = "gemini-3.8-flash",
                            generationConfig = generationConfig {
                                responseMimeType = "application/json"
                                responseSchema = quizSchema
                                temperature = 0.2f
                            }
                        )
                        val prompt = """
                            أنت منشئ اختبارات أكاديمية لتطبيق Go Alpha.
                            أنشئ 4 أسئلة اختيار من متعدد (لكل سؤال 4 اختيارات) مستخرجة **حصرياً 100%** من مقتطفات الكتب التالية فقط، بحيث تكون كل من الأسئلة والإجابة الصحيحة والشرح مستندة حرفياً لما ورد في النص المرفق مع ذكر اسم الكتاب ورقم الصفحة الصحيحين.
                            يُمنع منعاً باتاً إدخال أي معلومة من خارج هذه المقتطفات.

                            المقتطفات من كتب المكتبة:
                            $excerptsText
                        """.trimIndent()

                        val response = withContext(Dispatchers.IO) { model.generateContent(prompt) }
                        val rawJson = response.text?.trim().orEmpty()
                        parseQuizJson(rawJson, passages, booksById)
                    }
                } catch (e: Exception) {
                    Log.w("RamsesVM", "AI quiz generator fallback notice: ${e.message}")
                    emptyList()
                }

                _quizQuestions.value = if (aiGenerated.isNotEmpty()) {
                    aiGenerated
                } else {
                    buildDeterministicBookQuiz(passages, booksById)
                }
            } finally {
                _isGeneratingQuiz.value = false
            }
        }
    }

    private fun parseQuizJson(
        rawJson: String,
        passages: List<CloudRagChunk>,
        booksById: Map<String, CloudRagBook>
    ): List<BookQuizQuestion> {
        if (rawJson.isBlank()) return emptyList()
        return runCatching {
            val root = JSONObject(rawJson)
            val arr = root.optJSONArray("questions") ?: return emptyList()
            val list = mutableListOf<BookQuizQuestion>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val qAr = obj.optString("questionAr").trim()
                val qEn = obj.optString("questionEn").trim().ifBlank { qAr }
                val optsArArr = obj.optJSONArray("optionsAr")
                val optsEnArr = obj.optJSONArray("optionsEn")
                val optsAr = buildList {
                    if (optsArArr != null) {
                        for (j in 0 until optsArArr.length()) {
                            optsArArr.optString(j).trim().takeIf { it.isNotEmpty() }?.let(::add)
                        }
                    }
                }
                val optsEn = buildList {
                    if (optsEnArr != null) {
                        for (j in 0 until optsEnArr.length()) {
                            optsEnArr.optString(j).trim().takeIf { it.isNotEmpty() }?.let(::add)
                        }
                    }
                }.ifEmpty { optsAr }

                if (qAr.isNotEmpty() && optsAr.size >= 2) {
                    val fallbackChunk = passages.getOrNull(i % passages.size)
                    val bId = obj.optString("bookId").ifBlank { fallbackChunk?.bookId.orEmpty() }
                    val bTitle = obj.optString("bookTitle")
                        .ifBlank { booksById[bId]?.title ?: "كتاب من المكتبة" }
                    val pRange = obj.optString("pageRange")
                        .ifBlank { fallbackChunk?.let { "ص ${it.formattedPageNumber()}" } ?: "ص 1" }
                    val correctIdx = obj.optInt("correctIndex", 0).coerceIn(0, optsAr.lastIndex)
                    val expAr = obj.optString("explanationAr").trim()
                        .ifBlank { "المصدر المعتمد: $bTitle ($pRange)." }
                    val expEn = obj.optString("explanationEn").trim().ifBlank { expAr }
                    list += BookQuizQuestion(
                        id = "ai_q_${System.currentTimeMillis()}_$i",
                        questionAr = qAr,
                        questionEn = qEn,
                        optionsAr = optsAr,
                        optionsEn = optsEn,
                        correctIndex = correctIdx,
                        explanationAr = expAr,
                        explanationEn = expEn,
                        bookId = bId,
                        bookTitle = bTitle,
                        pageRange = pRange
                    )
                }
            }
            list
        }.getOrDefault(emptyList())
    }

    private fun buildDeterministicBookQuiz(
        passages: List<CloudRagChunk>,
        booksById: Map<String, CloudRagBook>
    ): List<BookQuizQuestion> {
        if (passages.isEmpty()) return emptyList()
        return passages.take(4).mapIndexed { idx, chunk ->
            val bookTitle = booksById[chunk.bookId]?.title ?: "كتاب مرفوع في المكتبة"
            val pageLabel = "ص ${chunk.formattedPageNumber()}"
            val snippet = chunk.content.replace(Regex("\\s+"), " ").trim().take(180)
            val otherPages = passages
                .map { "ص ${it.formattedPageNumber()}" }
                .filter { it != pageLabel }
                .distinct()
                .take(3)
            val distractors = (otherPages + listOf("ص 999", "ص 888", "ص 777"))
                .distinct()
                .take(3)
            val options = listOf(pageLabel) + distractors
            BookQuizQuestion(
                id = "det_q_${chunk.chunkId}_$idx",
                questionAr = "في أي نطاق صفحات من كتاب «$bookTitle» ورد النص التالي؟\n«$snippet...»",
                questionEn = "In which page range of «$bookTitle» does this passage appear?\n«$snippet...»",
                optionsAr = options,
                optionsEn = options,
                correctIndex = 0,
                explanationAr = "ورد هذا النص حرفياً في كتاب «$bookTitle» ($pageLabel).",
                explanationEn = "This passage appears verbatim in «$bookTitle» ($pageLabel).",
                bookId = chunk.bookId,
                bookTitle = bookTitle,
                pageRange = pageLabel
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentMessageJob?.cancel()
        messageCollectorJob?.cancel()
        cloudBooksListenerJob?.cancel()
        cloudFoldersListenerJob?.cancel()
        geminiLiveClient.stopSession()
        ttsHelper.shutdown()
        speechHelper.stopListening()
    }
}
