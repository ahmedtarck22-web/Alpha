package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.EverydayKnowledgeHelper
import com.example.data.api.GeminiApiClient
import com.example.data.model.AppLanguage
import com.example.data.repository.FirestoreRagRepository
import com.example.service.GeminiLiveWebSocketClient
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Alpha Go", appName)
    assertTrue(context.getString(R.string.scroll_to_bottom_desc).isNotBlank())
    assertTrue(context.getString(R.string.live_echo_guard_ar).isNotBlank())
    assertTrue(context.getString(R.string.live_echo_guard_en).isNotBlank())
  }

  @Test
  fun `legacy five seed book IDs are tracked for Firebase purge and exclusion`() {
    assertEquals(5, FirestoreRagRepository.LEGACY_SEED_BOOK_IDS.size)
    assertTrue(FirestoreRagRepository.LEGACY_SEED_BOOK_IDS.contains("book_greco_roman_hist_01"))
    assertTrue(FirestoreRagRepository.LEGACY_SEED_BOOK_IDS.contains("book_pharaonic_hist3_02"))
  }

  @Test
  fun `written text chat enforces 8192 maxOutputTokens and multi-paragraph directive while voice stays concise`() {
    val client = GeminiApiClient()

    val writtenBodyJson = JSONObject(
      client.buildRequestBody(
        prompt = "حدثني عن معبد أبو سمبل",
        history = emptyList(),
        imageBitmap = null,
        withSearch = false,
        isVoiceCall = false,
        ragContext = emptyList(),
        appLanguage = AppLanguage.ARABIC,
        gemPersona = "RAMSES"
      )
    )
    val writtenGenConfig = writtenBodyJson.getJSONObject("generationConfig")
    assertEquals(8192, writtenGenConfig.getInt("maxOutputTokens"))

    val systemText = writtenBodyJson
      .getJSONObject("systemInstruction")
      .getJSONArray("parts")
      .getJSONObject(0)
      .getString("text")
    assertTrue(systemText.contains("توجيه أساسي وملزم للردود النصية المكتوبة"))
    assertTrue(systemText.contains("جو ألفا"))

    val voiceBodyJson = JSONObject(
      client.buildRequestBody(
        prompt = "حدثني عن معبد أبو سمبل",
        history = emptyList(),
        imageBitmap = null,
        withSearch = false,
        isVoiceCall = true,
        ragContext = emptyList(),
        appLanguage = AppLanguage.ARABIC,
        gemPersona = "RAMSES"
      )
    )
    val voiceGenConfig = voiceBodyJson.getJSONObject("generationConfig")
    assertEquals(220, voiceGenConfig.getInt("maxOutputTokens"))
  }

  @Test
  fun `live voice acoustic echo gate blocks mic feedback during AI speech and echo tail then clears on manual interrupt`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val liveClient = GeminiLiveWebSocketClient(context)

    assertFalse(liveClient.isEchoGateActive(nowElapsedMs = 1_000L))

    // Active AI playback gates microphone echo
    liveClient.setEchoGateForTesting(aiPlaying = true, echoTailUntilMs = 0L)
    assertTrue(liveClient.isEchoGateActive(nowElapsedMs = 1_000L))

    // Post-playback reverb tail keeps gate active until cooldown elapses
    liveClient.setEchoGateForTesting(aiPlaying = false, echoTailUntilMs = 2_550L)
    assertTrue(liveClient.isEchoGateActive(nowElapsedMs = 2_200L))
    assertFalse(liveClient.isEchoGateActive(nowElapsedMs = 2_600L))

    // Manual interrupt button immediately clears echo gate so user can speak right away
    liveClient.setEchoGateForTesting(aiPlaying = true, echoTailUntilMs = 9_999L)
    liveClient.interruptModelSpeech()
    assertFalse(liveClient.isEchoGateActive(nowElapsedMs = 3_000L))
  }

  @Test
  fun `SecureKeyStoreHelper encrypts secrets with AES-GCM and migrates legacy cleartext preferences`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences("test_secure_keystore_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().commit()

    // Seed a simulated legacy cleartext key and verify automatic migration removes cleartext
    val sampleSecret = "AIzaSySimulatedUnitTestKey_1234567890"
    prefs.edit().putString("custom_api_key", sampleSecret).commit()

    val migratedRead = com.example.security.SecureKeyStoreHelper.readEncryptedPref(
      prefs = prefs,
      encryptedKey = "custom_api_key_enc_v1",
      legacyPlainKey = "custom_api_key"
    )
    assertEquals(sampleSecret, migratedRead)
    assertFalse("Legacy cleartext key must be deleted after migration", prefs.contains("custom_api_key"))

    val storedCipher = prefs.getString("custom_api_key_enc_v1", null).orEmpty()
    assertTrue("Encrypted payload must be non-empty iv:ciphertext", storedCipher.contains(":"))
    assertFalse("Stored ciphertext must never contain raw secret", storedCipher.contains(sampleSecret))

    // Subsequent read decrypts cleanly from ciphertext
    val secondRead = com.example.security.SecureKeyStoreHelper.readEncryptedPref(
      prefs = prefs,
      encryptedKey = "custom_api_key_enc_v1",
      legacyPlainKey = "custom_api_key"
    )
    assertEquals(sampleSecret, secondRead)
  }

  @Test
  fun `RAG passage citation formatting and deduplication preserve book page references`() {
    val sampleChunk = com.example.data.model.CloudRagChunk(
      chunkId = "chunk_p4_01",
      bookId = "user_book_01",
      bookTitle = "مذكرة الأحياء - الفصل الأول",
      folderId = "folder_01",
      folderTitle = "الفصل الأول: الدعامة والحركة",
      pageRange = "4-6",
      content = "تتكون الدعامة الفسيولوجية في النبات عندما يمتص الماء بالخاصية الأسموزية."
    )
    val passage = com.example.data.model.RetrievedRagPassage(
      chunk = sampleChunk,
      score = 12.5
    )

    val topCitationTitle = passage.toCitationTitle()
    val topCitationUri = passage.toCitationUri()
    assertTrue(topCitationTitle.contains("•"))
    assertTrue(topCitationTitle.contains("الصفحة:"))
    assertTrue(topCitationUri.startsWith("firestore://rag_books/user_book_01/"))

    val client = GeminiApiClient()
    val ragDoc = com.example.data.model.RagDocument(
      id = "cloud_chunk_${passage.chunk.chunkId}",
      title = topCitationTitle,
      content = passage.chunk.content,
      gcsUri = topCitationUri
    )
    val citations = client.buildRagContextCitations(listOf(ragDoc, ragDoc))
    assertEquals(1, citations.size)
    assertEquals(topCitationTitle, citations.first().title)
    assertEquals(topCitationUri, citations.first().uri)
  }

  @Test
  fun `multimodal image request downsamples oversized bitmap and encodes inlineData JPEG`() {
    val client = GeminiApiClient()
    val oversizedBitmap = android.graphics.Bitmap.createBitmap(
      2048,
      1536,
      android.graphics.Bitmap.Config.ARGB_8888
    )
    val bodyJson = JSONObject(
      client.buildRequestBody(
        prompt = "حلل هذا النقش الهيروغليفي",
        history = emptyList(),
        imageBitmap = oversizedBitmap,
        withSearch = false,
        isVoiceCall = false,
        ragContext = emptyList(),
        appLanguage = AppLanguage.ARABIC,
        gemPersona = "RAMSES"
      )
    )
    val parts = bodyJson.getJSONArray("contents").getJSONObject(0).getJSONArray("parts")
    var foundInlineJpeg = false
    for (i in 0 until parts.length()) {
      val inline = parts.getJSONObject(i).optJSONObject("inlineData")
      if (inline != null) {
        assertEquals("image/jpeg", inline.getString("mimeType"))
        assertTrue(inline.getString("data").length > 32)
        foundInlineJpeg = true
      }
    }
    assertTrue("Expected inlineData JPEG part in multimodal request", foundInlineJpeg)
  }

  @Test
  fun `all bundled 3D GLB avatar and artifact models have valid glTF 2 binary headers and lengths`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val glbPaths = listOf(
      "models/Ramses.final.glb",
      "models/Ramses.original.glb"
    ) + com.example.data.model.RamsesArtifact.ALL_ARTIFACTS.map { it.modelPath }

    assertEquals(7, glbPaths.distinct().size)

    for (assetPath in glbPaths.distinct()) {
      val bytes = context.assets.open(assetPath).use { it.readBytes() }
      assertTrue("GLB asset $assetPath is unexpectedly tiny", bytes.size > 128)
      val header = java.nio.ByteBuffer.wrap(bytes, 0, 12).order(java.nio.ByteOrder.LITTLE_ENDIAN)
      val magic = header.int
      val version = header.int
      val declaredLength = header.int
      assertEquals("Invalid glTF magic in $assetPath", 0x46546C67, magic)
      assertEquals("Expected glTF version 2 in $assetPath", 2, version)
      assertEquals("Declared GLB byte length mismatch in $assetPath", bytes.size, declaredLength)

      // Parse Chunk 0 (glTF 2.0 JSON chunk: type 0x4E4F534A = "JSON") to verify scene/nodes/meshes integrity
      val chunk0Header = java.nio.ByteBuffer.wrap(bytes, 12, 8).order(java.nio.ByteOrder.LITTLE_ENDIAN)
      val jsonChunkLen = chunk0Header.int
      val jsonChunkType = chunk0Header.int
      assertEquals("Expected JSON chunk0 in $assetPath", 0x4E4F534A, jsonChunkType)
      assertTrue("Invalid JSON chunk length in $assetPath", jsonChunkLen > 16 && 20 + jsonChunkLen <= bytes.size)
      val gltfJson = JSONObject(String(bytes, 20, jsonChunkLen, Charsets.UTF_8).trim { it <= ' ' || it == '\u0000' })
      assertEquals("2.0", gltfJson.getJSONObject("asset").getString("version"))
      assertTrue("Expected non-empty nodes in $assetPath", gltfJson.getJSONArray("nodes").length() > 0)
      assertTrue("Expected non-empty meshes in $assetPath", gltfJson.getJSONArray("meshes").length() > 0)
    }
  }

  @Test
  fun `Arabic search normalization and stem extraction tokenize user queries cleanly`() {
    val normalized = FirestoreRagRepository.normalizeSearchText("الإسكندرية والعمارة اليونانية؟")
    assertEquals("الاسكندرية والعمارة اليونانية", normalized)
    val stems = FirestoreRagRepository.extractSearchStems("الإسكندرية والعمارة اليونانية في مصر")
    assertTrue(stems.isNotEmpty())
    assertTrue(stems.any { it.contains("اسكندر") || it.contains("الاسكندرية") })
  }

  @Test
  fun `guest auth state voice fallback and offline chat session flow work cleanly without API key leaks`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    // Ensure FirebaseApp is initialized in Robolectric JVM before constructing RamsesViewModel
    createLocalTestFirestore(app)
    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

    val vm = com.example.ui.viewmodel.RamsesViewModel(app)
    try {
      // 1. Unauthenticated state defaults to GUEST without anonymous sign-in
      val profile = vm.userProfile.value
      assertFalse(profile.isSignedIn)
      assertEquals(com.example.data.model.AuthProvider.GUEST, profile.provider)

      // 2. Live WebSocket client safely falls back to local voice when no API key is configured and redacts secrets
      val liveClient = GeminiLiveWebSocketClient(app)
      val startedWithoutKey = liveClient.startSession(
        customApiKey = "",
        appLanguage = AppLanguage.ARABIC,
        gemPersona = "RAMSES",
        ragDocs = emptyList()
      )
      if (BuildConfig.GEMINI_API_KEY.isBlank() || BuildConfig.GEMINI_API_KEY == "MY_GEMINI_API_KEY") {
        assertFalse(startedWithoutKey)
        assertEquals(
          GeminiLiveWebSocketClient.LiveConnectionStatus.FALLBACK_LOCAL,
          liveClient.connectionStatus.value
        )
      }
      liveClient.stopSession()

      val leakedText = "Handshake error with token AIzaSySecretLiveToken987654"
      val redacted = com.example.security.SecureKeyStoreHelper.redactSecret(leakedText, "AIzaSySecretLiveToken987654")
      assertFalse(redacted.contains("AIzaSySecretLiveToken987654"))
      assertTrue(redacted.contains("[REDACTED_KEY]"))

      // 3. Language switching & TTS clean shutdown
      vm.setLanguage(AppLanguage.ENGLISH)
      assertEquals(AppLanguage.ENGLISH, vm.appLanguage.value)
      vm.setLanguage(AppLanguage.ARABIC)
      assertEquals(AppLanguage.ARABIC, vm.appLanguage.value)
    } finally {
      vm.ttsHelper.shutdown()
      vm.geminiLiveClient.stopSession()
    }
  }

  @Test
  fun `extreme aspect ratio and recycled bitmaps do not crash GeminiApiClient multimodal encoding`() {
    val client = GeminiApiClient()

    // 1. Extreme wide banner (2048 x 1) previously rounded height to 0 and threw IllegalArgumentException
    val wideBitmap = android.graphics.Bitmap.createBitmap(
      2048,
      1,
      android.graphics.Bitmap.Config.ARGB_8888
    )
    val wideBody = JSONObject(
      client.buildRequestBody(
        prompt = "حلل هذا الشريط الهيروغليفي",
        history = emptyList(),
        imageBitmap = wideBitmap,
        withSearch = false,
        isVoiceCall = false,
        ragContext = emptyList(),
        appLanguage = AppLanguage.ARABIC,
        gemPersona = "RAMSES"
      )
    )
    val wideParts = wideBody.getJSONArray("contents").getJSONObject(0).getJSONArray("parts")
    assertTrue("Expected image + text parts for extreme aspect ratio bitmap", wideParts.length() >= 2)

    // 2. Recycled bitmap is safely skipped instead of crashing Canvas/compress
    val recycledBitmap = android.graphics.Bitmap.createBitmap(
      256,
      256,
      android.graphics.Bitmap.Config.ARGB_8888
    )
    recycledBitmap.recycle()
    assertTrue(recycledBitmap.isRecycled)

    val recycledBody = JSONObject(
      client.buildRequestBody(
        prompt = "سؤال نصي بعد تدوير الصورة",
        history = emptyList(),
        imageBitmap = recycledBitmap,
        withSearch = false,
        isVoiceCall = false,
        ragContext = emptyList(),
        appLanguage = AppLanguage.ARABIC,
        gemPersona = "RAMSES"
      )
    )
    val recycledParts = recycledBody.getJSONArray("contents").getJSONObject(0).getJSONArray("parts")
    assertEquals("Recycled bitmap should be omitted cleanly, leaving prompt text part", 1, recycledParts.length())
  }

  @Test
  fun `cold RamsesViewModel and headless SceneView support guard against missing FirebaseApp and native GL crashes`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()

    // 1. Headless Robolectric JVM safely detects lack of Filament/GLES3 without UnsatisfiedLinkError
    assertFalse(com.example.ui.components.isFilament3DSupported(app))

    // 2. Clear any existing FirebaseApp instances to simulate cold uninitialized process startup
    com.google.firebase.FirebaseApp.clearInstancesForTest()

    val vm = com.example.ui.viewmodel.RamsesViewModel(app)
    try {
      assertEquals(com.example.data.model.AuthProvider.GUEST, vm.userProfile.value.provider)
      assertFalse(vm.userProfile.value.isSignedIn)
    } finally {
      vm.ttsHelper.shutdown()
      vm.geminiLiveClient.stopSession()
    }
  }

  @Test
  fun `Go Alpha strict system instruction enforces library books only and formats page citations`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val fakeDb = createLocalTestFirestore(context)
    val repo = FirestoreRagRepository(
      db = fakeDb,
      appContext = context,
      authProvider = { null }
    )
    val client = GeminiApiClient()

    // 1. Request body enforces Go Alpha Library-book-only grounding
    val body = JSONObject(
      client.buildRequestBody(
        prompt = "شرح اختصارات Edit Mode في برنامج Blender",
        history = emptyList(),
        imageBitmap = null,
        withSearch = false,
        isVoiceCall = false,
        ragContext = emptyList(),
        appLanguage = AppLanguage.ARABIC,
        gemPersona = "ALPHA_GO"
      )
    )
    val systemText = body
      .getJSONObject("systemInstruction")
      .getJSONArray("parts")
      .getJSONObject(0)
      .getString("text")
    assertTrue(systemText.contains("جو ألفا"))
    assertTrue(systemText.contains("مصدرك الوحيد للإجابة هو الكتب والمراجع المفهرسة في المكتبة"))

    // 2. Clean default Library starts with zero books and rejects legacy seed books
    val sharedBooks = repo.getSharedBooks().getOrDefault(emptyList())
    assertTrue("Expected default Library to start empty without legacy seed books", sharedBooks.isEmpty())
    val outOfBookHits = repo.retrieveRelevantPassages("ازاي اعمل كود بايثون او موديل في بلندر")
    assertTrue("Expected zero RAG hits when Library is empty", outOfBookHits.isEmpty())
  }

  @Test
  fun `hidden thinking and audio control tokens inline page mentions and Live WebSocket JSON payloads are validated`() {
    val dirtyModelOutput = "<thought>analyzing prompt</thought>[audio_token]<|eot_id|>هذا هو الكود المطلوب"
    assertEquals(
      "هذا هو الكود المطلوب",
      GeminiApiClient.stripHiddenControlTokens(dirtyModelOutput).trim()
    )
    assertEquals(
      "أهلاً بك",
      GeminiLiveWebSocketClient.stripHiddenVoiceTokens("<thinking>plan</thinking>[audio]أهلاً بك").trim()
    )

    val proseWithInlinePages = "قاد الملك رمسيس الثاني معركة قادش من صفحة 45 وحقق السلام (المصدر: موسوعة مصر القديمة، الصفحة: 41–55) ببراعة."
    val cleanedProse = GeminiApiClient.stripInlineSourceMentions(proseWithInlinePages)
    assertFalse("Inline 'من صفحة' must be stripped from historical prose", cleanedProse.contains("من صفحة"))
    assertFalse("Inline parenthetical source must be stripped from historical prose", cleanedProse.contains("المصدر:"))
    assertTrue(cleanedProse.contains("قاد الملك رمسيس الثاني معركة قادش"))

    val historicalInstruction = GeminiApiClient.buildEffectiveSystemInstruction(
      gemPersona = "RAMSES",
      appLanguage = AppLanguage.ARABIC,
      isVoiceCall = false,
      prompt = "حدثني عن معركة قادش"
    )
    assertTrue(historicalInstruction.contains("توجيه أساسي وملزم للردود النصية المكتوبة"))
    assertTrue(historicalInstruction.contains("نهاية الرسالة فقط"))

    val clientTextJson = JSONObject(GeminiLiveWebSocketClient.buildClientTextPayload("مرحبا يا رمسيس"))
    assertTrue("Text turn must use clientContent, never realtimeInput.text", clientTextJson.has("clientContent"))
    assertFalse(clientTextJson.has("realtimeInput"))
    val clientContent = clientTextJson.getJSONObject("clientContent")
    assertTrue(clientContent.getBoolean("turnComplete"))
    assertEquals(
      "مرحبا يا رمسيس",
      clientContent.getJSONArray("turns").getJSONObject(0).getJSONArray("parts").getJSONObject(0).getString("text")
    )

    val audioJson = JSONObject(GeminiLiveWebSocketClient.buildRealtimeAudioPayload("AAAA"))
    val mediaChunk = audioJson.getJSONObject("realtimeInput").getJSONArray("mediaChunks").getJSONObject(0)
    assertEquals("audio/pcm;rate=16000", mediaChunk.getString("mimeType"))
    assertEquals("AAAA", mediaChunk.getString("data"))

    val pastedKeyWithRtlMarks = "\u200F\u200E AIzaSyTestKey_987654321 \n\t"
    assertEquals("AIzaSyTestKey_987654321", GeminiApiClient.sanitizeApiKey(pastedKeyWithRtlMarks))
    assertEquals("AIzaSyTestKey_987654321", GeminiLiveWebSocketClient.sanitizeApiKey(pastedKeyWithRtlMarks))
  }

  @Test
  fun `everyday knowledge helper answers greetings date time weather cartoons and tech naturally`() {
    assertEquals("ازيك عامل ايه", EverydayKnowledgeHelper.normalizeArabic("إزيك عامل إيه؟"))

    val clockContext = EverydayKnowledgeHelper.buildCurrentDateTimeContext(AppLanguage.ARABIC)
    assertTrue(clockContext.contains("معلومات الوقت والتاريخ الحقيقية"))

    val instructionWithBooks = GeminiApiClient.buildEffectiveSystemInstruction(
      gemPersona = "ALPHA_GO",
      appLanguage = AppLanguage.ARABIC,
      isVoiceCall = false,
      prompt = "حدثني عن معركة قادش"
    )
    assertTrue(instructionWithBooks.contains("جو ألفا"))

    val greetingReply = EverydayKnowledgeHelper.generateNaturalEverydayResponse(
      prompt = "إزيك عامل إيه؟",
      appLanguage = AppLanguage.ARABIC,
      isVoiceCall = false,
      gemPersona = "ALPHA_GO"
    )
    assertTrue("Expected Go Alpha warm greeting reply, got: $greetingReply", greetingReply.contains("جو ألفا"))

    val outOfBookWeather = EverydayKnowledgeHelper.generateNaturalEverydayResponse(
      prompt = "الجو عامل إيه النهاردة؟",
      appLanguage = AppLanguage.ARABIC,
      isVoiceCall = false,
      gemPersona = "ALPHA_GO"
    )
    assertTrue("Expected strict out-of-book notice directing user to Library PDFs, got: $outOfBookWeather", outOfBookWeather.contains("المكتبة"))

    val outOfBookCartoon = EverydayKnowledgeHelper.generateNaturalEverydayResponse(
      prompt = "كلمني عن فيلم كرتون الأسد الملك",
      appLanguage = AppLanguage.ARABIC,
      isVoiceCall = false,
      gemPersona = "ALPHA_GO"
    )
    assertTrue("Expected strict out-of-book notice for off-book topic, got: $outOfBookCartoon", outOfBookCartoon.contains("PDF"))

    val outOfBookBlender = EverydayKnowledgeHelper.generateNaturalEverydayResponse(
      prompt = "اشرح اختصارات بلندر",
      appLanguage = AppLanguage.ARABIC,
      isVoiceCall = false,
      gemPersona = "ALPHA_GO"
    )
    assertTrue("Expected strict out-of-book notice for Blender topic, got: $outOfBookBlender", outOfBookBlender.contains("المكتبة"))
  }

  private fun createLocalTestFirestore(context: Context): com.google.firebase.firestore.FirebaseFirestore {
    if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
      com.google.firebase.FirebaseApp.initializeApp(
        context,
        com.google.firebase.FirebaseOptions.Builder()
          .setApplicationId("com.aistudio.ramsesai.kqzmpw")
          .setProjectId("demo-no-project")
          .setApiKey("fake-api-key-for-unit-test")
          .build()
      )
    }
    return com.google.firebase.firestore.FirebaseFirestore.getInstance(
      com.google.firebase.FirebaseApp.getInstance(),
      "demo-unit-db"
    )
  }
}



