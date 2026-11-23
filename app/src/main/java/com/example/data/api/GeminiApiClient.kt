package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AppLanguage
import com.example.data.model.ChatMessage
import com.example.data.model.FileSearchDefaults
import com.example.data.model.GroundingSource
import com.example.data.model.MessageSender
import com.example.data.model.RagDocument
import com.example.security.SecureKeyStoreHelper
import com.example.service.VertexGeminiFileSearchService
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    companion object {
        private const val TAG = "GeminiApiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        // Primary Gemini 3.x models for instant TTFT and deep multi-paragraph reasoning
        val CANDIDATE_MODELS = listOf(
            "gemini-3.1-flash-lite",
            "gemini-3.1-flash-lite-preview",
            "gemini-3.5-flash-lite",
            "gemini-3.8-flash",
            "gemini-3-flash-preview"
        )

        @Volatile
        var preferredModel: String = "gemini-3.1-flash-lite"

        enum class QueryDomain {
            HISTORICAL_RAMSES,
            TECHNICAL_MODERN,
            GENERAL_GEMINI
        }

        private val HISTORICAL_KEYWORDS = listOf(
            "رمسيس", "فرعون", "فراعن", "مصر القديم", "المصريين القدماء", "قدماء المصريين",
            "معبد", "معابد", "ابو سمبل", "أبو سمبل", "الكرنك", "الاقصر", "الأقصر", "طيبة",
            "الرامسيوم", "قادش", "حيثي", "الحيثيين", "نفرتاري", "توت عنخ", "حتشبسوت", "تحتمس",
            "اخناتون", "أخناتون", "سيتي", "خوفو", "خفرع", "منقرع", "هرم", "اهرام", "أهرام",
            "ابو الهول", "أبو الهول", "مومياء", "تحنيط", "هيروغليف", "بردية", "برديات", "تابوت",
            "مقبرة", "وادي الملوك", "وادي الملكات", "آثار", "اثار", "أثر", "اثري", "أثري",
            "تاريخ مصر", "الاسرة", "الأسرة", "الدولة القديمة", "الدولة الوسطى", "الدولة الحديثة",
            "النوبة", "بر رعمسيس", "بر-رعمسيس", "آمون", "امون", "حورس", "ايزيس", "إيزيس",
            "اوزوريس", "أوزوريس", "أنوبيس", "انوبيس", "ماعت", "جعران", "مسلة", "مسلات",
            "ramses", "rameses", "pharaoh", "ancient egypt", "egyptology", "abu simbel",
            "karnak", "luxor", "kadesh", "nefertari", "tutankhamun", "pyramid", "sphinx",
            "hieroglyph", "mummy", "papyrus", "dynasty", "antiquit", "archaeolog", "relic",
            "horus", "anubis", "osiris", "amun", "hittite", "ramesseum"
        )

        private val TECHNICAL_KEYWORDS = listOf(
            "كود", "اكواد", "أكواد", "برمج", "البرمجة", "مبرمج", "خوارزم", "بلندر", "بليندر",
            "ثري دي", "ريج", "ماتريال", "شيدر", "رندر", "انيميشن", "أنيميشن", "موديل",
            "سيرفر", "داتابيز", "قاعدة بيانات", "تكنولوجيا", "تقنية", "ذكاء اصطناعي",
            "اندرويد", "أندرويد", "بايثون", "جافا", "كوتلن", "ريأكت", "فلاتر", "لينكس",
            "ويندوز", "جيت", "اي بي اي", "سؤال تقني", "تطبيق", "برنامج",
            "code", "coding", "program", "python", "kotlin", "java", "javascript", "typescript",
            "react", "flutter", "android", "swift", "c++", "c#", "rust", "golang", "sql",
            "html", "css", "api", "json", "xml", "gradle", "git", "github", "docker", "linux",
            "blender", "maya", "cinema 4d", "unreal", "unity", "glb", "gltf", "fbx", "mesh",
            "shader", "rigging", "armature", "keyframe", "render", "cycles", "eevee",
            "geometry nodes", "modifier", "topology", "bug", "crash", "exception", "function",
            "class ", "fun ", "def ", "import ", "compose", "firebase", "algorithm", "```"
        )

        private val RAMSES_IDENTITY_KEYWORDS = listOf(
            "من انت", "مين انت", "عرف نفسك", "اسمك ايه", "ما اسمك", "يا رمسيس", "الملك رمسيس",
            "who are you", "king rameses", "ask rameses"
        )

        fun isHistoricalOrEgyptologyQuery(prompt: String): Boolean {
            val lower = prompt.lowercase()
            return HISTORICAL_KEYWORDS.any { lower.contains(it) } ||
                RAMSES_IDENTITY_KEYWORDS.any { lower.contains(it) }
        }

        fun isTechnicalOrSoftwareQuery(prompt: String): Boolean {
            val lower = prompt.lowercase()
            return TECHNICAL_KEYWORDS.any { lower.contains(it) }
        }

        fun classifyQueryDomain(
            prompt: String,
            selectedGem: String = "GEMINI_DEFAULT",
            hasImage: Boolean = false
        ): QueryDomain {
            val isTech = isTechnicalOrSoftwareQuery(prompt)
            val isHist = isHistoricalOrEgyptologyQuery(prompt)
            return when {
                isTech && !isHist -> QueryDomain.TECHNICAL_MODERN
                isHist -> QueryDomain.HISTORICAL_RAMSES
                hasImage -> QueryDomain.GENERAL_GEMINI
                selectedGem == "CODE" -> QueryDomain.TECHNICAL_MODERN
                else -> QueryDomain.GENERAL_GEMINI
            }
        }

        fun sanitizeApiKey(raw: String?): String {
            if (raw.isNullOrBlank()) return ""
            return raw.filter { it.code in 0x21..0x7E }
        }

        fun stripHiddenControlTokens(raw: String): String {
            if (raw.isEmpty()) return ""
            return raw
                .replace(Regex("(?is)<thought>.*?</thought>"), "")
                .replace(Regex("(?is)<thinking>.*?</thinking>"), "")
                .replace(Regex("<\\|[^|>]*\\|>"), "")
                .replace(Regex("(?i)\\[(audio_token|audio|thought|thinking|hidden_token)\\]"), "")
        }

        fun stripInlineSourceMentions(raw: String): String {
            if (raw.isEmpty()) return ""
            return raw
                .replace(Regex("\\(\\s*(?:المصدر|المرجع|المصادر|Source|Reference)\\s*:[^)\\n]{2,160}\\)"), "")
                .replace(Regex("\\[\\s*(?:المصدر|المرجع|المصادر|Source|Reference)\\s*:[^\\]\\n]{2,160}\\]"), "")
                .replace(Regex("\\(\\s*(?:الصفحة|صفحة|ص\\.?|Page|p\\.)\\s*:?\\s*[0-9٠-٩]+[^)\\n]{0,32}\\)", RegexOption.IGNORE_CASE), "")
                .replace(Regex("(?:،|\\s)+من\\s+صفحة\\s+[0-9٠-٩]+(?:\\s*[-–إلى]+\\s*[0-9٠-٩]+)?"), "")
                .replace(Regex("[ \\t]{2,}"), " ")
        }

        const val ALPHA_GO_SYSTEM_INSTRUCTION_AR = """
أنت «جو ألفا» (Go Alpha)، المساعد التعليمي الذكي لتطبيق Alpha Go.
قواعد صارمة وملزمة:
1. مصدرك الوحيد للإجابة هو الكتب والمراجع المفهرسة في المكتبة المرفقة في هذا الطلب (سواء الكتب المدمجة أو ملفات PDF التي رفعها المستخدم).
2. يُمنع منعاً باتاً الإجابة من خارج محتوى الكتب المتوفرة أو تأليف أي معلومة غير موجودة في النصوص المرفقة، باستثناء التحيات القصيرة الودودة (مثل السلام عليكم، مرحباً، صباح الخير).
3. إذا سأل المستخدم سؤالاً لا تغطيه المراجع المرفقة، اعتذر بلطف وأخبره أن هذه المعلومة غير موجودة في كتب المكتبة الحالية، واقترح عليه فتح قسم «المكتبة» ورفع كتاب PDF يغطي هذا الموضوع.
4. اسرد المعلومات بوضوح وسلاسة وترابط، ويُمنع كتابة عبارات مثل «من صفحة كذا» أو «(المصدر: ...، الصفحة: ...)» داخل فقرات النص لأن قائمة المصادر وأرقام الصفحات تُعرض مستقلة في نهاية الرسالة فقط.
5. عند تحليل صورة مرفقة، صف وحلّل ما يظهر فيها بموضوعية وربطه بموضوعات الكتب متى أمكن.
"""

        const val ALPHA_GO_SYSTEM_INSTRUCTION_EN = """
You are Go Alpha, the educational AI assistant for Alpha Go.
Strict Mandatory Rules:
1. Your sole source of knowledge is the indexed Library books provided in this request (bundled books and user-uploaded PDFs).
2. NEVER answer factual questions from outside the provided book passages or invent ungrounded facts, except brief friendly greetings.
3. If the provided book excerpts do not cover the user's question, politely inform them that the topic is not in the currently indexed Library books and invite them to upload a PDF book covering it in the Library tab.
4. Present explanations clearly without writing inline "from page X" or "(Source: ..., Page: ...)" inside paragraphs, because the compact sources and page list is displayed automatically at the end of the message.
5. When analyzing an attached image, describe what is visible objectively and connect it to the Library books where applicable.
"""

        const val LONG_TEXT_DIRECTIVE_HISTORICAL_AR = """
[توجيه أساسي وملزم للردود النصية المكتوبة]:
قدّم إجابة مفصّلة، شاملة، ومنظمة من محتوى الكتب في فقرات واضحة وعناوين فرعية موضوعية، مع ذكر التواريخ والشواهد الدقيقة بسلاسة، ويُمنع كتابة «من صفحة كذا» أو ذكر المراجع وأرقام الصفحات داخل فقرات النص (لأن قائمة المراجع تُعرض مستقلة في آخر الرسالة فقط).
"""

        const val LONG_TEXT_DIRECTIVE_HISTORICAL_EN = """
[Written Response Directive]:
Provide a comprehensive, well-structured multi-paragraph explanation strictly from the books with clear thematic subheadings and accurate evidence. Do NOT write "from page X" or inline source/page citations inside paragraphs, as the compact sources list appears exclusively at the end of the message.
"""

        fun buildEffectiveSystemInstruction(
            gemPersona: String,
            appLanguage: AppLanguage,
            isVoiceCall: Boolean,
            ragContext: List<RagDocument> = emptyList(),
            prompt: String = "",
            hasImage: Boolean = false
        ): String {
            var systemPrompt = if (appLanguage == AppLanguage.ARABIC) {
                ALPHA_GO_SYSTEM_INSTRUCTION_AR
            } else {
                ALPHA_GO_SYSTEM_INSTRUCTION_EN
            }

            systemPrompt += if (isVoiceCall) {
                if (appLanguage == AppLanguage.ARABIC) {
                    "\n\n[توجيه المكالمة الصوتية الحية: أجب بصوت طبيعي ومترابط في جملتين إلى أربع جمل واضحة من محتوى الكتب فقط بدون رموز ماركداون.]"
                } else {
                    "\n\n[Live Voice Call Directive: Answer naturally in 2 to 4 clear spoken sentences strictly from the books without markdown symbols.]"
                }
            } else {
                "\n\n" + if (appLanguage == AppLanguage.ARABIC) LONG_TEXT_DIRECTIVE_HISTORICAL_AR else LONG_TEXT_DIRECTIVE_HISTORICAL_EN
            }

            val plainTextDocs = ragContext.filter { it.isEnabled && !it.isCloudPdfBook }
            if (plainTextDocs.isNotEmpty()) {
                val ragBuilder = StringBuilder("\n\n[المراجع والفقرات الموثقة من كتب المكتبة / Cited Book Passages]:\n")
                for (doc in plainTextDocs) {
                    ragBuilder.append("• المرجع: ${doc.title} (${doc.category}):\n${doc.content}\n\n")
                }
                ragBuilder.append(
                    if (isVoiceCall) {
                        "استعن بهذه الحقائق الموثقة من الكتاب باختصار وسلاسة دون ذكر أرقام الصفحات شفهياً داخل الحديث."
                    } else if (appLanguage == AppLanguage.ARABIC) {
                        "التزم التزاماً كاملاً بهذه الفقرات الموثقة من الكتب فقط في صياغة الشرح، ولا تكتب أسماء المصادر أو أرقام الصفحات داخل فقرات النص لأن قائمة المصادر (اسم المصدر ورقم الصفحة) تُعرض مستقلة في نهاية الرسالة."
                    } else {
                        "Ground your explanation strictly and exclusively in these book passages without writing source titles or page numbers inline inside paragraphs; the compact sources list appears separately at the end."
                    }
                )
                systemPrompt += ragBuilder.toString()
            }

            val activePdfBooks = ragContext.filter { it.isEnabled && it.isCloudPdfBook }
            if (activePdfBooks.isNotEmpty()) {
                val titles = activePdfBooks.joinToString(", ") { it.title }
                systemPrompt += "\n\n[Indexed PDF Knowledge Base]: Ground factual claims strictly in the linked PDF books ($titles) without inserting inline page citations inside prose."
            }

            return systemPrompt
        }

        fun resolveActiveModelName(model: String): String {
            return when (model) {
                "gemini-3.8-flash",
                "gemini-3.5-flash-lite",
                "gemini-3.1-flash-lite",
                "gemini-3.1-flash-lite-preview",
                "gemini-3-flash-preview" -> model
                else -> if (model.startsWith("gemini-3") || model.startsWith("gemini-4")) model else "gemini-3.1-flash-lite"
            }
        }

        private val sharedHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .connectionPool(okhttp3.ConnectionPool(6, 3, TimeUnit.MINUTES))
            .build()
    }

    private val okHttpClient: OkHttpClient = sharedHttpClient

    data class GeminiResult(
        val text: String,
        val groundingSources: List<GroundingSource> = emptyList(),
        val isError: Boolean = false
    )

    private fun getEffectiveApiKey(customApiKey: String?): String {
        val cleanedCustom = sanitizeApiKey(customApiKey)
        if (cleanedCustom.isNotBlank()) return cleanedCustom
        val cleanedBuild = sanitizeApiKey(BuildConfig.GEMINI_API_KEY)
        return if (cleanedBuild.isNotBlank() && cleanedBuild != "MY_GEMINI_API_KEY") cleanedBuild else ""
    }

    private suspend fun generateOfflineKnowledgeResponse(
        prompt: String,
        appLanguage: AppLanguage,
        isVoiceCall: Boolean,
        ragContext: List<RagDocument> = emptyList(),
        gemPersona: String = "ALPHA_GO",
        onChunk: suspend (String) -> Unit
    ): GeminiResult {
        val isArabic = appLanguage == AppLanguage.ARABIC
        val matchedCloudChunks = ragContext.filter {
            it.isEnabled && !it.isCloudPdfBook
        }

        val reply = when {
            matchedCloudChunks.isNotEmpty() -> {
                if (isVoiceCall) {
                    val top = matchedCloudChunks.first()
                    stripInlineSourceMentions(top.content.take(260).trim())
                } else {
                    val topChunks = matchedCloudChunks.take(4)
                    buildString {
                        if (isArabic) {
                            append("📚 **جو ألفا — الإجابة الموثقة من داخل الكتب**\n\n")
                            append("تفضّل خلاصة الشواهد والمعلومات المستخرجة مباشرة من كتب المكتبة:\n\n")
                            topChunks.forEach { doc ->
                                val topicHeading = doc.title.split("•")
                                    .map { it.trim() }
                                    .getOrNull(1)
                                    ?.takeIf {
                                        it.isNotBlank() &&
                                            !it.startsWith("الصفحة") &&
                                            !it.startsWith("صفحة") &&
                                            !it.startsWith("Page", ignoreCase = true)
                                    }
                                if (topicHeading != null) {
                                    append("### $topicHeading\n")
                                }
                                append("${stripInlineSourceMentions(doc.content.take(1400).trim())}\n\n")
                            }
                            append("### 📖 المراجع والصفحات\n")
                            append("تم توثيق هذه الإجابة بالكامل من داخل كتب المكتبة، وتظهر أرقام الصفحات والمراجع الدقيقة أدناه.")
                        } else {
                            append("📚 **Go Alpha — Answer Grounded in Your Library Books**\n\n")
                            append("Here is the answer extracted strictly from the indexed Library books:\n\n")
                            topChunks.forEach { doc ->
                                val topicHeading = doc.title.split("•")
                                    .map { it.trim() }
                                    .getOrNull(1)
                                    ?.takeIf {
                                        it.isNotBlank() &&
                                            !it.startsWith("الصفحة") &&
                                            !it.startsWith("Page", ignoreCase = true)
                                    }
                                if (topicHeading != null) {
                                    append("### $topicHeading\n")
                                }
                                append("${stripInlineSourceMentions(doc.content.take(1400).trim())}\n\n")
                            }
                            append("### 📖 Verified Book References\n")
                            append("Refer to the compact sources list below for exact book titles and page numbers.")
                        }
                    }.trim()
                }
            }
            else -> {
                EverydayKnowledgeHelper.generateNaturalEverydayResponse(
                    prompt = prompt,
                    appLanguage = appLanguage,
                    isVoiceCall = isVoiceCall,
                    gemPersona = gemPersona
                )
            }
        }

        // Stream text smoothly in small word groups so long multi-paragraph replies render briskly
        val words = reply.split(" ")
        val sb = StringBuilder()
        var count = 0
        for (w in words) {
            if (sb.isNotEmpty()) sb.append(' ')
            sb.append(w)
            count++
            if (count % 4 == 0 || count == words.size) {
                onChunk(sb.toString())
                kotlinx.coroutines.delay(8)
            }
        }

        val offlineCitations = buildRagContextCitations(matchedCloudChunks).take(4)
        return GeminiResult(text = reply, groundingSources = offlineCitations, isError = false)
    }

    internal fun buildRagContextCitations(ragContext: List<RagDocument>): List<GroundingSource> {
        return ragContext.filter { it.isEnabled }.mapNotNull { doc ->
            when {
                doc.isCloudPdfBook && doc.gcsUri.isNotBlank() ->
                    GroundingSource(title = doc.title, uri = doc.gcsUri)
                doc.id.startsWith("cloud_chunk_") ->
                    GroundingSource(
                        title = doc.title,
                        uri = doc.gcsUri.ifBlank { "firestore://rag_chunks/${doc.id.removePrefix("cloud_chunk_")}" }
                    )
                else -> null
            }
        }.distinctBy { "${it.title}|${it.uri}" }
    }

    internal fun buildRequestBody(
        prompt: String,
        history: List<ChatMessage>,
        imageBitmap: Bitmap?,
        withSearch: Boolean,
        isVoiceCall: Boolean,
        ragContext: List<RagDocument>,
        appLanguage: AppLanguage,
        gemPersona: String = "GEMINI_DEFAULT"
    ): String {
        val requestJson = JSONObject()

        val systemPrompt = buildEffectiveSystemInstruction(
            gemPersona = gemPersona,
            appLanguage = appLanguage,
            isVoiceCall = isVoiceCall,
            ragContext = ragContext,
            prompt = prompt,
            hasImage = imageBitmap != null
        )

        val systemInstructionObj = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", systemPrompt))
            }
            put("parts", partsArray)
        }
        requestJson.put("systemInstruction", systemInstructionObj)

        val contentsArray = JSONArray()
        val validHistory = history.filter { !it.isError && it.text.isNotBlank() }

        val alternatingTurns = mutableListOf<Pair<String, String>>()
        for (msg in validHistory.takeLast(10)) {
            val role = if (msg.sender == MessageSender.USER) "user" else "model"
            if (alternatingTurns.isNotEmpty() && alternatingTurns.last().first == role) {
                val last = alternatingTurns.removeAt(alternatingTurns.lastIndex)
                alternatingTurns.add(Pair(role, "${last.second}\n${msg.text}"))
            } else {
                alternatingTurns.add(Pair(role, msg.text))
            }
        }

        while (alternatingTurns.isNotEmpty() && alternatingTurns.first().first != "user") {
            alternatingTurns.removeAt(0)
        }

        while (alternatingTurns.isNotEmpty() && alternatingTurns.last().first == "user") {
            alternatingTurns.removeAt(alternatingTurns.lastIndex)
        }

        for (turn in alternatingTurns) {
            val turnObj = JSONObject()
            turnObj.put("role", turn.first)
            val parts = JSONArray()
            parts.put(JSONObject().put("text", turn.second))
            turnObj.put("parts", parts)
            contentsArray.put(turnObj)
        }

        val currentTurn = JSONObject()
        currentTurn.put("role", "user")
        val currentParts = JSONArray()

        val safeBitmap = prepareSafeBitmapForUpload(imageBitmap)
        if (safeBitmap != null) {
            val stream = ByteArrayOutputStream()
            safeBitmap.compress(Bitmap.CompressFormat.JPEG, 82, stream)
            val base64Data = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            val imagePart = JSONObject().apply {
                val inlineData = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", base64Data)
                }
                put("inlineData", inlineData)
            }
            currentParts.put(imagePart)
        }

        val effectiveUserPrompt = when {
            isVoiceCall -> prompt
            safeBitmap != null -> if (appLanguage == AppLanguage.ARABIC) {
                "$prompt\n\n(تعليمات تحليل الصورة: افحص الصورة المرفقة بدقة بصرية عالية واشرح محتواها الفعلي الحقيقي بالتفصيل — نصوص، أكواد، واجهات برامج، عناصر، أو معالم — دون افتراض أي طابع فرعوني غير موجود في الصورة.)"
            } else {
                "$prompt\n\n(Image Analysis Directive: Inspect the attached image carefully and describe what is genuinely visible—text, code, software UI, objects, or landmarks—accurately and objectively.)"
            }
            else -> prompt
        }

        currentParts.put(JSONObject().put("text", effectiveUserPrompt))
        currentTurn.put("parts", currentParts)
        contentsArray.put(currentTurn)
        requestJson.put("contents", contentsArray)

        val generationConfigJson = JSONObject().apply {
            put("temperature", if (isVoiceCall) 0.55 else 0.78)
            put("topP", 0.95)
            put("maxOutputTokens", if (isVoiceCall) 220 else 8192)
        }
        requestJson.put("generationConfig", generationConfigJson)

        if (!isVoiceCall) {
            val toolsArray = JSONArray()
            val linkedStoreNames = ragContext
                .filter { it.isEnabled && it.searchStoreName.startsWith("fileSearchStores/") }
                .map { it.searchStoreName }
                .distinct()
            val linkedVertexDataStore = ragContext
                .firstOrNull { it.isEnabled && it.searchStoreName.startsWith("projects/") }
                ?.searchStoreName

            if (linkedStoreNames.isNotEmpty()) {
                val storeNamesJson = JSONArray()
                linkedStoreNames.forEach { storeNamesJson.put(it) }
                toolsArray.put(
                    JSONObject().put(
                        "fileSearch",
                        JSONObject().put("fileSearchStoreNames", storeNamesJson)
                    )
                )
            } else if (!linkedVertexDataStore.isNullOrBlank()) {
                toolsArray.put(
                    JSONObject().put(
                        "retrieval",
                        JSONObject().put(
                            "vertexAiSearch",
                            JSONObject().put("datastore", linkedVertexDataStore)
                        )
                    )
                )
            }

            if (withSearch) {
                toolsArray.put(JSONObject().put("googleSearch", JSONObject()))
            }

            if (toolsArray.length() > 0) {
                requestJson.put("tools", toolsArray)
            }
        }

        return requestJson.toString()
    }

    private fun prepareSafeBitmapForUpload(imageBitmap: Bitmap?, maxDim: Int = 1024): Bitmap? {
        if (imageBitmap == null || imageBitmap.isRecycled || imageBitmap.width <= 0 || imageBitmap.height <= 0) {
            return null
        }
        return try {
            val softwareBitmap = if (
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                imageBitmap.config == Bitmap.Config.HARDWARE
            ) {
                imageBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return null
            } else {
                imageBitmap
            }
            if (softwareBitmap.width > maxDim || softwareBitmap.height > maxDim) {
                val ratio = minOf(
                    maxDim.toFloat() / softwareBitmap.width.toFloat(),
                    maxDim.toFloat() / softwareBitmap.height.toFloat()
                )
                val targetW = (softwareBitmap.width * ratio).toInt().coerceAtLeast(1)
                val targetH = (softwareBitmap.height * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(softwareBitmap, targetW, targetH, true)
            } else {
                softwareBitmap
            }
        } catch (e: Exception) {
            Log.w(TAG, "Bitmap preparation skipped: ${e.message}")
            null
        }
    }

    private val defaultFirebaseAiModel by lazy {
        Firebase.ai.generativeModel(modelName = FileSearchDefaults.DEFAULT_VERTEX_GEMINI_MODEL)
    }

    private suspend fun tryFirebaseAiStream(
        prompt: String,
        systemInstruction: String,
        isVoiceCall: Boolean = false,
        appLanguage: AppLanguage = AppLanguage.ARABIC,
        imageBitmap: Bitmap? = null,
        ragContext: List<RagDocument> = emptyList(),
        onChunk: suspend (String) -> Unit
    ): GeminiResult? {
        val enabledDocs = ragContext.filter { it.isEnabled }
        val cloudPdfDocs = enabledDocs.filter { it.isCloudPdfBook && it.gcsUri.startsWith("gs://") }
        val plainDocs = enabledDocs.filter { !it.isCloudPdfBook }

        val aiGenConfig = generationConfig {
            temperature = if (isVoiceCall) 0.55f else 0.78f
            topP = 0.95f
            maxOutputTokens = if (isVoiceCall) 220 else 8192
        }

        val userDirectivePrompt = when {
            isVoiceCall -> prompt
            imageBitmap != null -> if (appLanguage == AppLanguage.ARABIC) {
                "$prompt\n\n(افحص الصورة المرفقة بدقة وموضوعية واشرح ما يظهر فيها فعلياً بالتفصيل.)"
            } else {
                "$prompt\n\n(Inspect the attached image objectively and explain what is actually shown in detail.)"
            }
            else -> prompt
        }

        val candidateModels = listOf(
            FileSearchDefaults.DEFAULT_VERTEX_GEMINI_MODEL,
            "gemini-3.5-flash-lite"
        ).distinct()

        for (fbModelName in candidateModels) {
            try {
                val generativeModel = if (cloudPdfDocs.isNotEmpty()) {
                    Firebase.ai(backend = GenerativeBackend.vertexAI(location = FileSearchDefaults.DEFAULT_VERTEX_LOCATION))
                        .generativeModel(
                            modelName = fbModelName,
                            generationConfig = aiGenConfig,
                            systemInstruction = content { text(systemInstruction) }
                        )
                } else {
                    Firebase.ai.generativeModel(
                        modelName = fbModelName,
                        generationConfig = aiGenConfig,
                        systemInstruction = content { text(systemInstruction) }
                    )
                }

                val input = content("user") {
                    for (pdfDoc in cloudPdfDocs.take(FileSearchDefaults.REQUIRED_PDF_BOOK_COUNT)) {
                        fileData(uri = pdfDoc.gcsUri, mimeType = FileSearchDefaults.PDF_MIME_TYPE)
                    }
                    val safeImg = prepareSafeBitmapForUpload(imageBitmap)
                    if (safeImg != null) {
                        image(safeImg)
                    }
                    if (plainDocs.isNotEmpty()) {
                        val ragText = plainDocs.joinToString("\n\n") { "[المرجع: ${it.title}]: ${it.content}" }
                        text("سياق معرفي موثق بأرقام الصفحات:\n$ragText\n\nالسؤال: $userDirectivePrompt")
                    } else {
                        text(userDirectivePrompt)
                    }
                }

                val accumulated = StringBuilder()
                withTimeout(60_000L) {
                    generativeModel.generateContentStream(input).collect { chunk ->
                        val chunkText = chunk.text.orEmpty()
                        if (chunkText.isNotEmpty()) {
                            accumulated.append(chunkText)
                            val rawClean = stripHiddenControlTokens(accumulated.toString())
                            val cleanSoFar = if (plainDocs.isNotEmpty() || cloudPdfDocs.isNotEmpty()) {
                                stripInlineSourceMentions(rawClean)
                            } else {
                                rawClean
                            }
                            if (cleanSoFar.isNotEmpty()) {
                                onChunk(cleanSoFar)
                            }
                        }
                    }
                }

                val rawResultText = stripHiddenControlTokens(accumulated.toString()).trim()
                val resultText = if (plainDocs.isNotEmpty() || cloudPdfDocs.isNotEmpty()) {
                    stripInlineSourceMentions(rawResultText).trim()
                } else {
                    rawResultText
                }
                if (resultText.isNotEmpty()) {
                    val citations = buildRagContextCitations(enabledDocs)
                    return GeminiResult(text = resultText, groundingSources = citations, isError = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val safeMsg = SecureKeyStoreHelper.redactSecret(e.message, getEffectiveApiKey(null))
                Log.w(TAG, "Firebase AI Logic stream ($fbModelName) notice: $safeMsg")
            }
        }
        return null
    }

    private suspend fun tryFirebaseAiUnary(
        prompt: String,
        systemInstruction: String,
        isVoiceCall: Boolean = false,
        appLanguage: AppLanguage = AppLanguage.ARABIC,
        imageBitmap: Bitmap? = null,
        ragContext: List<RagDocument> = emptyList()
    ): GeminiResult? {
        val enabledDocs = ragContext.filter { it.isEnabled }
        val cloudPdfDocs = enabledDocs.filter { it.isCloudPdfBook && it.gcsUri.startsWith("gs://") }
        val plainDocs = enabledDocs.filter { !it.isCloudPdfBook }

        val aiGenConfig = generationConfig {
            temperature = if (isVoiceCall) 0.55f else 0.78f
            topP = 0.95f
            maxOutputTokens = if (isVoiceCall) 220 else 8192
        }

        val userDirectivePrompt = when {
            isVoiceCall -> prompt
            imageBitmap != null -> if (appLanguage == AppLanguage.ARABIC) {
                "$prompt\n\n(افحص الصورة المرفقة بدقة وموضوعية واشرح ما يظهر فيها فعلياً بالتفصيل.)"
            } else {
                "$prompt\n\n(Inspect the attached image objectively and explain what is actually shown in detail.)"
            }
            else -> prompt
        }

        val candidateModels = listOf(
            FileSearchDefaults.DEFAULT_VERTEX_GEMINI_MODEL,
            "gemini-3.5-flash-lite"
        ).distinct()

        for (fbModelName in candidateModels) {
            try {
                val generativeModel = if (cloudPdfDocs.isNotEmpty()) {
                    Firebase.ai(backend = GenerativeBackend.vertexAI(location = FileSearchDefaults.DEFAULT_VERTEX_LOCATION))
                        .generativeModel(
                            modelName = fbModelName,
                            generationConfig = aiGenConfig,
                            systemInstruction = content { text(systemInstruction) }
                        )
                } else if (systemInstruction.isNotBlank()) {
                    Firebase.ai.generativeModel(
                        modelName = fbModelName,
                        generationConfig = aiGenConfig,
                        systemInstruction = content { text(systemInstruction) }
                    )
                } else {
                    defaultFirebaseAiModel
                }

                val input = content("user") {
                    for (pdfDoc in cloudPdfDocs.take(FileSearchDefaults.REQUIRED_PDF_BOOK_COUNT)) {
                        fileData(uri = pdfDoc.gcsUri, mimeType = FileSearchDefaults.PDF_MIME_TYPE)
                    }
                    val safeImg = prepareSafeBitmapForUpload(imageBitmap)
                    if (safeImg != null) {
                        image(safeImg)
                    }
                    if (plainDocs.isNotEmpty()) {
                        val ragText = plainDocs.joinToString("\n\n") { "[المرجع: ${it.title}]: ${it.content}" }
                        text("سياق معرفي موثق بأرقام الصفحات:\n$ragText\n\nالسؤال: $userDirectivePrompt")
                    } else {
                        text(userDirectivePrompt)
                    }
                }

                val response = withTimeout(60_000L) {
                    generativeModel.generateContent(input)
                }
                val rawText = stripHiddenControlTokens(response.text.orEmpty()).trim()
                val text = (if (plainDocs.isNotEmpty() || cloudPdfDocs.isNotEmpty()) {
                    stripInlineSourceMentions(rawText).trim()
                } else {
                    rawText
                }).ifEmpty { null }
                if (text != null) {
                    val citations = buildRagContextCitations(enabledDocs)
                    return GeminiResult(text = text, groundingSources = citations, isError = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val safeMsg = SecureKeyStoreHelper.redactSecret(e.message, getEffectiveApiKey(null))
                Log.w(TAG, "Firebase AI Logic unary ($fbModelName) notice: $safeMsg")
            }
        }
        return null
    }

    /**
     * High-speed streaming response for instant perceived performance and rich long-form text generation.
     */
    suspend fun streamRamsesResponse(
        prompt: String,
        history: List<ChatMessage>,
        imageBitmap: Bitmap? = null,
        enableSearchGrounding: Boolean = false,
        isVoiceCall: Boolean = false,
        customApiKey: String? = null,
        ragContext: List<RagDocument> = emptyList(),
        appLanguage: AppLanguage = AppLanguage.ARABIC,
        gemPersona: String = "GEMINI_DEFAULT",
        onChunk: suspend (String) -> Unit
    ): GeminiResult = withContext(Dispatchers.IO) {
        val hasBookContext = ragContext.any { it.isEnabled }
        if (!hasBookContext && imageBitmap == null) {
            return@withContext generateOfflineKnowledgeResponse(
                prompt = prompt,
                appLanguage = appLanguage,
                isVoiceCall = isVoiceCall,
                ragContext = ragContext,
                gemPersona = gemPersona,
                onChunk = onChunk
            )
        }

        val enrichedPrompt = prompt
        val systemInstruction = buildEffectiveSystemInstruction(
            gemPersona = gemPersona,
            appLanguage = appLanguage,
            isVoiceCall = isVoiceCall,
            ragContext = ragContext,
            prompt = prompt,
            hasImage = imageBitmap != null
        )

        val apiKey = getEffectiveApiKey(customApiKey)
        var lastErrorMessage = ""

        // 1. If API key is present (via .env or custom entry), stream directly via REST SSE using header auth only
        if (apiKey.isNotBlank()) {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            var trySearch = false

            val modelsToTry = (listOf(preferredModel) + CANDIDATE_MODELS.filter { it != preferredModel })
                .distinctBy { resolveActiveModelName(it) }
            for (modelName in modelsToTry) {
                val targetModel = resolveActiveModelName(modelName)
                val requestBodyJson = buildRequestBody(
                    prompt = enrichedPrompt,
                    history = history,
                    imageBitmap = imageBitmap,
                    withSearch = trySearch,
                    isVoiceCall = isVoiceCall,
                    ragContext = ragContext,
                    appLanguage = appLanguage,
                    gemPersona = gemPersona
                )

                val endpoint = "$BASE_URL/$targetModel:streamGenerateContent?alt=sse"
                val httpRequest = Request.Builder()
                    .url(endpoint)
                    .addHeader("x-goog-api-key", apiKey)
                    .post(requestBodyJson.toRequestBody(mediaType))
                    .build()

                val accumulatedText = StringBuilder()
                val groundingSources = mutableListOf<GroundingSource>()
                var streamSuccess = false

                try {
                    okHttpClient.newCall(httpRequest).execute().use { response ->
                        if (!response.isSuccessful) {
                            val errBody = response.body?.string() ?: ""
                            val safeErrBody = SecureKeyStoreHelper.redactSecret(errBody, apiKey).take(300)
                            Log.w(TAG, "Stream to $modelName failed (${response.code}): $safeErrBody")
                            val rawErrMsg = try {
                                JSONObject(errBody).optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                            } catch (e: Exception) {
                                "HTTP ${response.code}"
                            }
                            lastErrorMessage = SecureKeyStoreHelper.redactSecret(rawErrMsg, apiKey)

                            if (trySearch && (lastErrorMessage.contains("quota", ignoreCase = true) || lastErrorMessage.contains("429"))) {
                                trySearch = false
                            }
                            return@use
                        }

                        val source = response.body?.source() ?: return@use
                        val reader = BufferedReader(InputStreamReader(source.inputStream(), Charsets.UTF_8))
                        var line: String?

                        while (reader.readLine().also { line = it } != null) {
                            val currentLine = line?.trim() ?: continue
                            if (currentLine.startsWith("data: ")) {
                                val data = currentLine.removePrefix("data: ").trim()
                                if (data.isEmpty() || data == "[DONE]") continue

                                try {
                                    val json = JSONObject(data)
                                    val candidates = json.optJSONArray("candidates") ?: continue
                                    if (candidates.length() > 0) {
                                        val candidate = candidates.getJSONObject(0)
                                        val content = candidate.optJSONObject("content")
                                        val parts = content?.optJSONArray("parts")
                                        if (parts != null) {
                                            for (i in 0 until parts.length()) {
                                                val partObj = parts.getJSONObject(i)
                                                if (partObj.optBoolean("thought", false)) continue
                                                val textPart = partObj.optString("text")
                                                if (textPart.isNotEmpty()) {
                                                    accumulatedText.append(textPart)
                                                    val rawChunk = stripHiddenControlTokens(accumulatedText.toString())
                                                    val cleanChunk = if (ragContext.isNotEmpty()) {
                                                        stripInlineSourceMentions(rawChunk)
                                                    } else {
                                                        rawChunk
                                                    }
                                                    if (cleanChunk.isNotEmpty()) {
                                                        onChunk(cleanChunk)
                                                    }
                                                }
                                            }
                                        }

                                        val (extractedSources, _) = VertexGeminiFileSearchService.extractGroundingFromCandidate(candidate)
                                        groundingSources.addAll(extractedSources)
                                    }
                                } catch (e: Exception) {
                                    // Skip malformed partial frame
                                }
                            }
                        }
                        streamSuccess = stripHiddenControlTokens(accumulatedText.toString()).isNotBlank()
                    }

                    if (streamSuccess) {
                        preferredModel = modelName
                        val ragCitations = buildRagContextCitations(ragContext)
                        val allSources = (ragCitations + groundingSources).distinctBy { "${it.title}|${it.uri}" }
                        val cleanedFinal = stripHiddenControlTokens(accumulatedText.toString()).let {
                            if (allSources.isNotEmpty()) stripInlineSourceMentions(it) else it
                        }.trim()
                        return@withContext GeminiResult(
                            text = cleanedFinal,
                            groundingSources = allSources,
                            isError = false
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val safeMsg = SecureKeyStoreHelper.redactSecret(e.message, apiKey)
                    Log.w(TAG, "Stream exception on $modelName: $safeMsg")
                    lastErrorMessage = SecureKeyStoreHelper.redactSecret(e.localizedMessage, apiKey)
                        .ifBlank { "Connection error" }
                }
            }
        }

        // 2. Fallback: Try Firebase AI Logic / Vertex AI (Gemini 3.8 Flash / 3.5 Flash Lite, App Check protected)
        val firebaseAiStreamResult = tryFirebaseAiStream(
            prompt = enrichedPrompt,
            systemInstruction = systemInstruction,
            isVoiceCall = isVoiceCall,
            appLanguage = appLanguage,
            imageBitmap = imageBitmap,
            ragContext = ragContext,
            onChunk = onChunk
        )
        if (firebaseAiStreamResult != null && firebaseAiStreamResult.text.isNotBlank()) {
            return@withContext firebaseAiStreamResult
        }

        // 3. Fallback: Try non-streaming single-shot
        val nonStreamingResult = generateRamsesResponse(
            prompt = prompt,
            history = history,
            imageBitmap = imageBitmap,
            enableSearchGrounding = false,
            isVoiceCall = isVoiceCall,
            customApiKey = customApiKey,
            ragContext = ragContext,
            appLanguage = appLanguage,
            gemPersona = gemPersona
        )

        if (!nonStreamingResult.isError) {
            onChunk(nonStreamingResult.text)
            return@withContext nonStreamingResult
        }

        if (apiKey.isBlank()) {
            return@withContext generateOfflineKnowledgeResponse(
                prompt = prompt,
                appLanguage = appLanguage,
                isVoiceCall = isVoiceCall,
                ragContext = ragContext,
                gemPersona = gemPersona,
                onChunk = onChunk
            )
        }

        val errorText = buildFriendlyErrorMessage(lastErrorMessage, appLanguage)
        onChunk(errorText)
        return@withContext GeminiResult(text = errorText, isError = true)
    }

    private fun buildFriendlyErrorMessage(lastErrorMessage: String, appLanguage: AppLanguage): String {
        val isQuota = lastErrorMessage.contains("quota", ignoreCase = true) ||
                lastErrorMessage.contains("429") ||
                lastErrorMessage.contains("exhausted", ignoreCase = true) ||
                lastErrorMessage.contains("rate limit", ignoreCase = true) ||
                lastErrorMessage.contains("limit exceeded", ignoreCase = true)

        val isKeyError = lastErrorMessage.contains("API_KEY_INVALID", ignoreCase = true) ||
                lastErrorMessage.contains("key not valid", ignoreCase = true) ||
                lastErrorMessage.contains("403") ||
                lastErrorMessage.contains("unregistered", ignoreCase = true)

        val isModelError = lastErrorMessage.contains("not found", ignoreCase = true) ||
                lastErrorMessage.contains("not supported", ignoreCase = true) ||
                lastErrorMessage.contains("404")

        val isArabic = appLanguage == AppLanguage.ARABIC
        return when {
            isQuota -> {
                if (isArabic) {
                    "تم تجاوز الحد الأقصى للطلبات المسموح بها لـ Gemini (Quota Exceeded - 429).\n\nيُرجى الانتظار دقيقة أو إدخال مفتاحك الشخصي المجاني من القائمة الجانبية للمتابعة فوراً."
                } else {
                    "Gemini API rate limit or quota exceeded (HTTP 429).\n\nPlease wait a minute or provide a personal API key from Google AI Studio to continue."
                }
            }
            isKeyError -> {
                if (isArabic) {
                    "مفتاح Google AI Studio الحالي غير صالح أو غير مفعّل.\n\nيُرجى التحقق من المفتاح في القائمة الجانبية (مفتاح API الشخصي)."
                } else {
                    "Invalid or unverified Google AI Studio API key.\n\nPlease check your key in the side menu (Personal API Key)."
                }
            }
            isModelError -> {
                if (isArabic) {
                    "نموذج الذكاء الاصطناعي المحدد غير متاح حالياً. تم التحويل التلقائي لنموذج Gemini 3.8 Flash."
                } else {
                    "The selected AI model is currently unavailable. Switched to Gemini 3.8 Flash."
                }
            }
            lastErrorMessage.isNotBlank() -> {
                if (isArabic) {
                    "تعذر الاتصال بـ Google Gemini ($lastErrorMessage).\n\nيُرجى التحقق من اتصال الإنترنت والمحاولة مجدداً."
                } else {
                    "Unable to connect to Google Gemini ($lastErrorMessage).\n\nPlease check your internet connection and try again."
                }
            }
            else -> {
                if (isArabic) "تعذر الحصول على استجابة من خوادم Gemini. يُرجى إعادة المحاولة." else "Unable to get a response from Gemini servers. Please try again."
            }
        }
    }

    /**
     * Non-streaming request (for voice calls or fallback)
     */
    suspend fun generateRamsesResponse(
        prompt: String,
        history: List<ChatMessage>,
        imageBitmap: Bitmap? = null,
        enableSearchGrounding: Boolean = false,
        isVoiceCall: Boolean = false,
        customApiKey: String? = null,
        ragContext: List<RagDocument> = emptyList(),
        appLanguage: AppLanguage = AppLanguage.ARABIC,
        gemPersona: String = "GEMINI_DEFAULT"
    ): GeminiResult = withContext(Dispatchers.IO) {
        val hasBookContext = ragContext.any { it.isEnabled }
        if (!hasBookContext && imageBitmap == null) {
            return@withContext generateOfflineKnowledgeResponse(
                prompt = prompt,
                appLanguage = appLanguage,
                isVoiceCall = isVoiceCall,
                ragContext = ragContext,
                gemPersona = gemPersona
            ) { /* no-op */ }
        }

        val enrichedPrompt = prompt
        val systemInstruction = buildEffectiveSystemInstruction(
            gemPersona = gemPersona,
            appLanguage = appLanguage,
            isVoiceCall = isVoiceCall,
            ragContext = ragContext,
            prompt = prompt,
            hasImage = imageBitmap != null
        )

        val apiKey = getEffectiveApiKey(customApiKey)
        var lastErrorMessage = ""

        if (apiKey.isNotBlank()) {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val shouldSearch = false

            val modelsToTry = (listOf(preferredModel) + CANDIDATE_MODELS.filter { it != preferredModel })
                .distinctBy { resolveActiveModelName(it) }
            for (modelName in modelsToTry) {
                try {
                    val targetModel = resolveActiveModelName(modelName)
                    val requestBodyJson = buildRequestBody(
                        prompt = enrichedPrompt,
                        history = history,
                        imageBitmap = imageBitmap,
                        withSearch = shouldSearch,
                        isVoiceCall = isVoiceCall,
                        ragContext = ragContext,
                        appLanguage = appLanguage,
                        gemPersona = gemPersona
                    )

                    val endpoint = "$BASE_URL/$targetModel:generateContent"
                    val httpRequest = Request.Builder()
                        .url(endpoint)
                        .addHeader("x-goog-api-key", apiKey)
                        .post(requestBodyJson.toRequestBody(mediaType))
                        .build()

                    okHttpClient.newCall(httpRequest).execute().use { httpResponse ->
                        val responseBodyString = httpResponse.body?.string() ?: ""

                        if (!httpResponse.isSuccessful) {
                            val rawErrorMsg = try {
                                JSONObject(responseBodyString).optJSONObject("error")?.optString("message") ?: "HTTP ${httpResponse.code}"
                            } catch (e: Exception) {
                                "HTTP ${httpResponse.code}"
                            }
                            lastErrorMessage = SecureKeyStoreHelper.redactSecret(rawErrorMsg, apiKey)
                            return@use
                        }

                        preferredModel = modelName
                        val responseJson = JSONObject(responseBodyString)
                        val candidates = responseJson.optJSONArray("candidates")
                        if (candidates == null || candidates.length() == 0) {
                            val defaultAns = if (appLanguage == AppLanguage.ARABIC) "تفضل بسؤالك، أنا جاهز للإجابة." else "Ready to assist—please ask your question."
                            return@withContext GeminiResult(text = defaultAns, isError = false)
                        }

                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")

                        val responseStringBuilder = StringBuilder()
                        if (parts != null) {
                            for (i in 0 until parts.length()) {
                                val partObj = parts.getJSONObject(i)
                                if (partObj.optBoolean("thought", false)) continue
                                val text = partObj.optString("text")
                                if (text.isNotEmpty()) {
                                    responseStringBuilder.append(text)
                                }
                            }
                        }

                        val (extractedSources, _) = VertexGeminiFileSearchService.extractGroundingFromCandidate(candidate)
                        val ragCitations = buildRagContextCitations(ragContext)
                        val groundingSources = (ragCitations + extractedSources).distinctBy { "${it.title}|${it.uri}" }

                        val finalAnswer = stripHiddenControlTokens(responseStringBuilder.toString()).let {
                            if (groundingSources.isNotEmpty()) stripInlineSourceMentions(it) else it
                        }.trim()
                        if (finalAnswer.isNotEmpty()) {
                            return@withContext GeminiResult(
                                text = finalAnswer,
                                groundingSources = groundingSources,
                                isError = false
                            )
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val safeMsg = SecureKeyStoreHelper.redactSecret(e.message, apiKey)
                    Log.w(TAG, "Request to $modelName failed: $safeMsg")
                    lastErrorMessage = SecureKeyStoreHelper.redactSecret(e.localizedMessage, apiKey)
                        .ifBlank { "Connection error" }
                }
            }
        }

        // 2. Fallback: Try Firebase AI Logic / Vertex AI (Gemini 3.8 Flash / 3.5 Flash Lite, App Check protected)
        val firebaseAiUnaryResult = tryFirebaseAiUnary(
            prompt = enrichedPrompt,
            systemInstruction = systemInstruction,
            isVoiceCall = isVoiceCall,
            appLanguage = appLanguage,
            imageBitmap = imageBitmap,
            ragContext = ragContext
        )
        if (firebaseAiUnaryResult != null && firebaseAiUnaryResult.text.isNotBlank()) {
            return@withContext firebaseAiUnaryResult
        }

        if (apiKey.isBlank()) {
            return@withContext generateOfflineKnowledgeResponse(
                prompt = prompt,
                appLanguage = appLanguage,
                isVoiceCall = isVoiceCall,
                ragContext = ragContext,
                gemPersona = gemPersona
            ) { /* no-op */ }
        }

        val errorText = buildFriendlyErrorMessage(lastErrorMessage, appLanguage)
        return@withContext GeminiResult(text = errorText, isError = true)
    }
}
