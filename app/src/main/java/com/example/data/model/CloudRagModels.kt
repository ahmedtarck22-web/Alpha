package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

/**
 * Top-level book metadata stored in Cloud Firestore `/rag_books/{bookId}`.
 * Structured small folders & chunks live in Cloud Firestore while optional full PDFs live in Firebase Storage.
 */
@IgnoreExtraProperties
data class CloudRagBook(
    @DocumentId val docId: String = "",
    val bookId: String = "",
    val ownerId: String = "",
    @get:PropertyName("isShared")
    val isShared: Boolean = true,
    val title: String = "",
    val author: String = "",
    val eraCategory: String = "",
    val summary: String = "",
    val pageCount: Int = 0,
    val folderCount: Int = 0,
    val chunkCount: Int = 0,
    val keywords: List<String> = emptyList(),
    val pdfDownloadUrl: String = "",
    val storagePath: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(authenticatedUid: String): Map<String, Any> = buildMap {
        put("bookId", bookId)
        put("ownerId", authenticatedUid)
        put("isShared", isShared)
        put("title", title.take(300))
        put("author", author.take(200))
        put("eraCategory", eraCategory.ifBlank { "عام" }.take(120))
        put("summary", summary.ifBlank { title }.take(3000))
        put("pageCount", pageCount.coerceIn(0, 20000))
        put("folderCount", folderCount.coerceIn(0, 5000))
        put("chunkCount", chunkCount.coerceIn(0, 50000))
        put(
            "keywords",
            keywords.map { it.replace('\u001F', ' ').replace(Regex("\\s+"), " ").trim().take(100) }
                .filter { it.isNotEmpty() }
                .take(40)
        )
        if (pdfDownloadUrl.isNotBlank()) {
            put("pdfDownloadUrl", pdfDownloadUrl.trim().take(2048))
        }
        if (storagePath.isNotBlank()) {
            put("storagePath", storagePath.trim().take(512))
        }
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): CloudRagBook {
            val kws = (doc.get("keywords") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            return CloudRagBook(
                docId = doc.id,
                bookId = doc.getString("bookId") ?: doc.id,
                ownerId = doc.getString("ownerId") ?: "",
                isShared = doc.getBoolean("isShared") ?: true,
                title = doc.getString("title") ?: "",
                author = doc.getString("author") ?: "",
                eraCategory = doc.getString("eraCategory") ?: "",
                summary = doc.getString("summary") ?: "",
                pageCount = (doc.getLong("pageCount") ?: 0L).toInt(),
                folderCount = (doc.getLong("folderCount") ?: 0L).toInt(),
                chunkCount = (doc.getLong("chunkCount") ?: 0L).toInt(),
                keywords = kws,
                pdfDownloadUrl = doc.getString("pdfDownloadUrl") ?: "",
                storagePath = doc.getString("storagePath") ?: "",
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}

/**
 * Thematic small folder / chapter stored in Cloud Firestore `/rag_folders/{folderId}`.
 * Enables fast 2-stage retrieval across up to 1,000+ books without downloading irrelevant chunks.
 */
@IgnoreExtraProperties
data class CloudRagFolder(
    @DocumentId val docId: String = "",
    val folderId: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val ownerId: String = "",
    @get:PropertyName("isShared")
    val isShared: Boolean = true,
    val folderIndex: Int = 0,
    val folderTitle: String = "",
    val category: String = "",
    val pageRange: String = "",
    val topicSummary: String = "",
    val chunkCount: Int = 0,
    val keywords: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(authenticatedUid: String): Map<String, Any> = mapOf(
        "folderId" to folderId,
        "bookId" to bookId,
        "bookTitle" to bookTitle.take(300),
        "ownerId" to authenticatedUid,
        "isShared" to isShared,
        "folderIndex" to folderIndex.coerceIn(0, 5000),
        "folderTitle" to folderTitle.take(300),
        "category" to category.ifBlank { "فصل" }.take(120),
        "pageRange" to pageRange.take(80),
        "topicSummary" to topicSummary.ifBlank { folderTitle }.take(4000),
        "chunkCount" to chunkCount.coerceIn(0, 5000),
        "keywords" to keywords.map { it.replace('\u001F', ' ').replace(Regex("\\s+"), " ").trim().take(100) }.filter { it.isNotEmpty() }.take(50),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): CloudRagFolder {
            val kws = (doc.get("keywords") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            return CloudRagFolder(
                docId = doc.id,
                folderId = doc.getString("folderId") ?: doc.id,
                bookId = doc.getString("bookId") ?: "",
                bookTitle = doc.getString("bookTitle") ?: "",
                ownerId = doc.getString("ownerId") ?: "",
                isShared = doc.getBoolean("isShared") ?: true,
                folderIndex = (doc.getLong("folderIndex") ?: 0L).toInt(),
                folderTitle = doc.getString("folderTitle") ?: "",
                category = doc.getString("category") ?: "",
                pageRange = doc.getString("pageRange") ?: "",
                topicSummary = doc.getString("topicSummary") ?: "",
                chunkCount = (doc.getLong("chunkCount") ?: 0L).toInt(),
                keywords = kws,
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}

/**
 * Searchable knowledge chunk with exact book, folder, and page citation in `/rag_chunks/{chunkId}`.
 */
@IgnoreExtraProperties
data class CloudRagChunk(
    @DocumentId val docId: String = "",
    val chunkId: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val folderId: String = "",
    val folderTitle: String = "",
    val ownerId: String = "",
    @get:PropertyName("isShared")
    val isShared: Boolean = true,
    val chunkIndex: Int = 0,
    val pageRange: String = "",
    val title: String = "",
    val content: String = "",
    val keywords: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(authenticatedUid: String): Map<String, Any> = mapOf(
        "chunkId" to chunkId,
        "bookId" to bookId,
        "bookTitle" to bookTitle.take(300),
        "folderId" to folderId,
        "folderTitle" to folderTitle.take(300),
        "ownerId" to authenticatedUid,
        "isShared" to isShared,
        "chunkIndex" to chunkIndex.coerceIn(0, 50000),
        "pageRange" to pageRange.take(80),
        "title" to title.ifBlank { folderTitle }.take(300),
        "content" to content.ifBlank { title }.take(25000),
        "keywords" to keywords.map { it.replace('\u001F', ' ').replace(Regex("\\s+"), " ").trim().take(100) }.filter { it.isNotEmpty() }.take(40),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun formattedPageNumber(): String {
        val raw = pageRange.trim()
        if (raw.isNotEmpty() && raw.any { it.isDigit() }) {
            val cleaned = raw
                .replace(Regex("^(الصفحة|صفحات|صفحة|ص\\.?|pages?|pp?\\.?)\\s*:?\\s*", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\s+"), " ")
                .trim()
            if (cleaned.isNotEmpty()) return cleaned
        }
        val startPage = (chunkIndex.coerceAtLeast(0) * 6) + 1
        val endPage = startPage + 5
        return "$startPage–$endPage"
    }

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): CloudRagChunk {
            val kws = (doc.get("keywords") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            return CloudRagChunk(
                docId = doc.id,
                chunkId = doc.getString("chunkId") ?: doc.id,
                bookId = doc.getString("bookId") ?: "",
                bookTitle = doc.getString("bookTitle") ?: "",
                folderId = doc.getString("folderId") ?: "",
                folderTitle = doc.getString("folderTitle") ?: "",
                ownerId = doc.getString("ownerId") ?: "",
                isShared = doc.getBoolean("isShared") ?: true,
                chunkIndex = (doc.getLong("chunkIndex") ?: 0L).toInt(),
                pageRange = doc.getString("pageRange") ?: "",
                title = doc.getString("title") ?: "",
                content = doc.getString("content") ?: "",
                keywords = kws,
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}

/**
 * Complete structured bundle for a single book (metadata + small folders + chunks).
 */
data class SeedRagBookBundle(
    val book: CloudRagBook,
    val folders: List<CloudRagFolder>,
    val chunks: List<CloudRagChunk>
)

/**
 * Real-time UI state for Cloud Firebase RAG synchronization and PDF-to-RAG conversion.
 */
sealed interface CloudRagSyncState {
    data object Idle : CloudRagSyncState
    data class Syncing(
        val progressMessage: String,
        val currentStep: Int = 0,
        val totalSteps: Int = 1
    ) : CloudRagSyncState

    data class Ready(
        val syncedBooksCount: Int,
        val syncedFoldersCount: Int,
        val syncedChunksCount: Int,
        val isCloudConnected: Boolean,
        val statusMessage: String
    ) : CloudRagSyncState

    data class Error(val message: String) : CloudRagSyncState
}

/**
 * Retrieval result returned when matching a user prompt against the Cloud Firebase RAG corpus.
 */
data class RetrievedRagPassage(
    val chunk: CloudRagChunk,
    val score: Double
) {
    fun toCitationTitle(): String = "${chunk.bookTitle} • ${chunk.folderTitle} • الصفحة: ${chunk.formattedPageNumber()}"
    fun toCitationUri(): String = "firestore://rag_books/${chunk.bookId}/folders/${chunk.folderId}#${chunk.chunkId}?page=${chunk.formattedPageNumber()}"
}

/**
 * Educational video metadata stored in Cloud Firestore `/videos/{videoId}` while media lives in Firebase Storage.
 */
@IgnoreExtraProperties
data class CloudVideoItem(
    @DocumentId val docId: String = "",
    val videoId: String = "",
    val ownerId: String = "",
    @get:PropertyName("isShared")
    val isShared: Boolean = true,
    val title: String = "",
    val description: String = "",
    val videoDownloadUrl: String = "",
    val storagePath: String = "",
    val durationLabel: String = "",
    val fileSizeBytes: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(authenticatedUid: String): Map<String, Any> = mapOf(
        "videoId" to videoId,
        "ownerId" to authenticatedUid,
        "isShared" to isShared,
        "title" to title.ifBlank { "فيديو تعليمي" }.take(300),
        "description" to description.take(2000),
        "videoDownloadUrl" to videoDownloadUrl.take(2048),
        "storagePath" to storagePath.take(512),
        "durationLabel" to durationLabel.take(40),
        "fileSizeBytes" to fileSizeBytes.coerceIn(0L, 524_288_000L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): CloudVideoItem {
            return CloudVideoItem(
                docId = doc.id,
                videoId = doc.getString("videoId") ?: doc.id,
                ownerId = doc.getString("ownerId") ?: "",
                isShared = doc.getBoolean("isShared") ?: true,
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                videoDownloadUrl = doc.getString("videoDownloadUrl") ?: "",
                storagePath = doc.getString("storagePath") ?: "",
                durationLabel = doc.getString("durationLabel") ?: "",
                fileSizeBytes = doc.getLong("fileSizeBytes") ?: 0L,
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}

sealed interface CloudVideoUploadState {
    data object Idle : CloudVideoUploadState
    data class Uploading(val progressMessage: String, val fraction: Float = 0f) : CloudVideoUploadState
    data class Success(val video: CloudVideoItem) : CloudVideoUploadState
    data class Error(val message: String) : CloudVideoUploadState
}
