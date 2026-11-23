package com.example.data.model

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    ARABIC("ar", "العربية", "🇪🇬"),
    ENGLISH("en", "English", "🇬🇧")
}

object AppStrings {
    fun appName(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "جو ألفا"
        AppLanguage.ENGLISH -> "Go Alpha"
    }

    fun homeWelcome(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "جو ألفا، أهلاً بك"
        AppLanguage.ENGLISH -> "Go Alpha, Welcome"
    }

    fun appSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مساعدك التعليمي من الكتب الموثقة"
        AppLanguage.ENGLISH -> "Book-Grounded Learning Assistant"
    }

    fun inputPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "...اسأل جو ألفا من محتوى الكتب"
        AppLanguage.ENGLISH -> "Ask Go Alpha from the books..."
    }

    fun personalKeyActive(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مفتاحك الشخصي نشط • 1.5M توكن"
        AppLanguage.ENGLISH -> "Personal Key Active • 1.5M Tokens"
    }

    fun freeTokensNotice(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "لكل مستخدم 1.5 مليون توكن مجاناً"
        AppLanguage.ENGLISH -> "1.5 Million Free Tokens per User"
    }

    fun liveCallTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مكالمة جو ألفا المباشرة"
        AppLanguage.ENGLISH -> "Go Alpha Live Call"
    }

    fun liveStatusListening(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "يستمع إليك الآن... اسأل من الكتب"
        AppLanguage.ENGLISH -> "Listening now... Ask from your books"
    }

    fun liveStatusThinking(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "جو ألفا يبحث في الكتب..."
        AppLanguage.ENGLISH -> "Go Alpha is searching the books..."
    }

    fun liveStatusSpeaking(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "جو ألفا يتحدث إليك مباشرة..."
        AppLanguage.ENGLISH -> "Go Alpha is speaking to you..."
    }

    fun liveStatusIdle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "انقر على الميكروفون للتحدث مع جو ألفا"
        AppLanguage.ENGLISH -> "Tap the microphone to speak with Go Alpha"
    }

    fun liveCallHint(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تحدث معي بصوتك حول موضوعات الكتب في المكتبة، وسأجيبك مباشرة من المراجع."
        AppLanguage.ENGLISH -> "Ask by voice about any topic covered in your Library books."
    }

    fun searchGroundingEnabled(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "البحث الموسّع داخل الكتب نشط"
        AppLanguage.ENGLISH -> "Deep Book Grounding Active"
    }

    fun searchGroundingDisabled(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "البحث الصارم من الكتب فقط نشط"
        AppLanguage.ENGLISH -> "Strict Book-Only Mode Active"
    }

    fun thinkingMessage(lang: AppLanguage, withSearch: Boolean): String = when (lang) {
        AppLanguage.ARABIC -> if (withSearch) "جو ألفا يراجع فصول الكتب..." else "جو ألفا يبحث في الكتب..."
        AppLanguage.ENGLISH -> if (withSearch) "Go Alpha is reviewing book chapters..." else "Go Alpha is searching the books..."
    }

    fun retryButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إعادة المحاولة"
        AppLanguage.ENGLISH -> "Retry"
    }

    fun changeApiKey(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تغيير المفتاح"
        AppLanguage.ENGLISH -> "Change API Key"
    }

    fun openAiStudio(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "فتح AI Studio"
        AppLanguage.ENGLISH -> "Open AI Studio"
    }

    fun copyText(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "نسخ"
        AppLanguage.ENGLISH -> "Copy"
    }

    fun speakText(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "استماع"
        AppLanguage.ENGLISH -> "Listen"
    }

    fun newSession(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "محادثة جديدة"
        AppLanguage.ENGLISH -> "New Chat"
    }

    fun drawerKnowledge(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مكتبة الكتب والمناهج"
        AppLanguage.ENGLISH -> "Curriculum & Books Library"
    }

    fun drawerKey(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مفتاح الذكاء الاصطناعي الخاص"
        AppLanguage.ENGLISH -> "Personal AI API Key"
    }

    fun drawerAccount(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "حسابي"
        AppLanguage.ENGLISH -> "My Account"
    }

    fun drawerVoiceCall(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مكالمة جو ألفا الصوتية"
        AppLanguage.ENGLISH -> "Interactive Voice Call (Go Alpha)"
    }

    fun clearAllChats(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مسح كافة المحادثات"
        AppLanguage.ENGLISH -> "Clear All Conversations"
    }

    fun endCall(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إنهاء المكالمة"
        AppLanguage.ENGLISH -> "End Call"
    }

    fun muteSpeaker(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "كتم الصوت"
        AppLanguage.ENGLISH -> "Mute Audio"
    }

    fun switchLanguage(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "English"
        AppLanguage.ENGLISH -> "العربية"
    }
}
