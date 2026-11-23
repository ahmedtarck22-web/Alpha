package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.CloudRagBook
import com.example.data.model.CloudRagChunk
import com.example.data.model.CloudRagFolder
import com.example.data.model.RetrievedRagPassage
import com.example.data.model.SeedRagBookBundle
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreRagRepository(
    private val db: FirebaseFirestore,
    private val appContext: Context? = null,
    private val authProvider: () -> String? = {
        runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
    }
) {
    constructor(context: Context) : this(
        db = resolveDefaultFirestore(context),
        appContext = context.applicationContext ?: context
    )

    companion object {
        private const val TAG = "FirestoreRagRepo"

        val LEGACY_SEED_BOOK_IDS = setOf(
            "book_greco_roman_hist_01",
            "book_pharaonic_hist3_02",
            "book_pharaonic_hist2_03",
            "book_ancient_lang3_04",
            "book_greco_roman_arch_05"
        )

        private fun resolveDefaultFirestore(context: Context): FirebaseFirestore {
            val appCtx = context.applicationContext ?: context
            com.example.firebase.ensureFirebaseInitialized(appCtx)
            return FirebaseFirestore.getInstance(
                appCtx.getString(R.string.firestore_database_id)
            )
        }

        private const val MAX_FOLDER_BATCH_OPS = 15
        private const val MAX_CHUNK_BATCH_OPS = 9
        private const val MAX_DELETE_BATCH_OPS = 380
        private const val MAX_BOOK_IN_QUERY_SIZE = 8

        private val ARABIC_STOPWORDS = setOf(
            "من", "في", "فى", "على", "علي", "الى", "إلى", "عن", "مع", "هو", "هي",
            "هم", "هذا", "هذه", "ذلك", "تلك", "التي", "الذي", "الذين", "ما", "ماذا",
            "كيف", "متى", "أين", "اين", "هل", "كان", "كانت", "بين", "كل", "بعد",
            "قبل", "عند", "او", "أو", "ثم", "يا", "حدثني", "اشرح", "اذكر", "قولي",
            "عايز", "اعرف", "the", "of", "and", "in", "to", "what", "who", "how", "tell", "me", "about"
        )

        private val DIACRITICS_REGEX = Regex("[\u064B-\u065F\u0670\u0640]")
        private val ALEF_REGEX = Regex("[أإآٱ]")
        private val NON_ALNUM_REGEX = Regex("[^\\p{L}\\p{N}\\s]")
        private val WHITESPACE_REGEX = Regex("\\s+")

        private data class IndexedFolder(
            val folder: CloudRagFolder,
            val titleNorm: String,
            val summaryNorm: String,
            val bookNorm: String,
            val kwNorm: List<String>,
            val stems: Set<String>,
            val trigrams: Set<String>
        )

        private data class IndexedChunk(
            val chunk: CloudRagChunk,
            val titleNorm: String,
            val contentNorm: String,
            val kwNorm: List<String>,
            val stems: Set<String>
        )

        private fun indexFolder(folder: CloudRagFolder): IndexedFolder {
            val titleNorm = normalizeSearchText(folder.folderTitle)
            val summaryNorm = normalizeSearchText(folder.topicSummary)
            val bookNorm = normalizeSearchText("${folder.bookTitle} ${folder.category}")
            val kwNorm = folder.keywords.map { normalizeSearchText(it) }
            val combined = "$titleNorm $summaryNorm ${kwNorm.joinToString(" ")} $bookNorm"
            return IndexedFolder(
                folder = folder,
                titleNorm = titleNorm,
                summaryNorm = summaryNorm,
                bookNorm = bookNorm,
                kwNorm = kwNorm,
                stems = extractSearchStems(combined),
                trigrams = buildCharTrigrams("$titleNorm ${kwNorm.joinToString(" ")}")
            )
        }

        private fun indexChunk(chunk: CloudRagChunk): IndexedChunk {
            val titleNorm = normalizeSearchText(chunk.title)
            val contentNorm = normalizeSearchText(chunk.content)
            val kwNorm = chunk.keywords.map { normalizeSearchText(it) }
            return IndexedChunk(
                chunk = chunk,
                titleNorm = titleNorm,
                contentNorm = contentNorm,
                kwNorm = kwNorm,
                stems = extractSearchStems("$titleNorm ${kwNorm.joinToString(" ")} $contentNorm")
            )
        }

        fun normalizeSearchText(input: String): String {
            if (input.isBlank()) return ""
            return input
                .lowercase()
                .replace(DIACRITICS_REGEX, "")
                .replace(ALEF_REGEX, "ا")
                .replace('ة', 'ه')
                .replace('ى', 'ي')
                .replace('ؤ', 'و')
                .replace('ئ', 'ي')
                .replace(NON_ALNUM_REGEX, " ")
                .replace(WHITESPACE_REGEX, " ")
                .trim()
        }

        fun stemArabicToken(rawToken: String): String {
            var t = rawToken
            when {
                t.length >= 5 && (t.startsWith("وال") || t.startsWith("بال") || t.startsWith("كال") || t.startsWith("فال")) ->
                    t = t.drop(3)
                t.length >= 4 && (t.startsWith("ال") || t.startsWith("لل")) ->
                    t = t.drop(2)
                t.length >= 5 && (t.startsWith("و") || t.startsWith("ف") || t.startsWith("ب") || t.startsWith("ل")) ->
                    t = t.drop(1)
            }
            when {
                t.length >= 6 && (t.endsWith("ات") || t.endsWith("ون") || t.endsWith("ين") || t.endsWith("ها") || t.endsWith("هم") || t.endsWith("كم") || t.endsWith("هن") || t.endsWith("ان")) ->
                    t = t.dropLast(2)
                t.length >= 5 && (t.endsWith("يه") || t.endsWith("ها")) ->
                    t = t.dropLast(2)
                t.length >= 5 && (t.endsWith("ه") || t.endsWith("ي")) ->
                    t = t.dropLast(1)
            }
            return t
        }

        fun extractSearchTokens(input: String): List<String> {
            val normalized = normalizeSearchText(input)
            if (normalized.isBlank()) return emptyList()
            return normalized.split(" ")
                .map { token ->
                    when {
                        token.length >= 5 && (token.startsWith("وال") || token.startsWith("بال") || token.startsWith("كال") || token.startsWith("فال")) ->
                            token.drop(3)
                        token.length >= 4 && (token.startsWith("ال") || token.startsWith("لل")) ->
                            token.drop(2)
                        else -> token
                    }
                }
                .filter { it.length >= 2 && it !in ARABIC_STOPWORDS }
                .distinct()
        }

        fun extractSearchStems(input: String): Set<String> {
            return extractSearchTokens(input)
                .map { stemArabicToken(it) }
                .filter { it.length >= 2 && it !in ARABIC_STOPWORDS }
                .toSet()
        }

        private fun buildCharTrigrams(normalized: String): Set<String> {
            val compact = normalized.replace(" ", "")
            if (compact.length < 3) return if (compact.isEmpty()) emptySet() else setOf(compact)
            val result = HashSet<String>(compact.length)
            for (i in 0..compact.length - 3) {
                result.add(compact.substring(i, i + 3))
            }
            return result
        }

        private fun diceSimilarity(a: Set<String>, b: Set<String>): Double {
            if (a.isEmpty() || b.isEmpty()) return 0.0
            var overlap = 0
            val smaller = if (a.size <= b.size) a else b
            val larger = if (a.size <= b.size) b else a
            for (gram in smaller) {
                if (gram in larger) overlap++
            }
            return (2.0 * overlap) / (a.size + b.size)
        }

        private fun countOccurrences(haystack: String, needle: String): Int {
            if (haystack.isEmpty() || needle.isEmpty()) return 0
            var count = 0
            var idx = haystack.indexOf(needle, 0)
            while (idx >= 0) {
                count++
                idx = haystack.indexOf(needle, idx + needle.length)
            }
            return count
        }
    }

    private fun requireAuthenticatedUid(): String {
        return authProvider()
            ?: throw IllegalStateException("يجب تسجيل الدخول بحساب Google أولاً للوصول إلى سحابة Firebase.")
    }

    /**
     * Real-time stream of shared Cloud Firebase books (`/rag_books` where `isShared == true`),
     * excluding legacy seed book IDs so only user-uploaded books appear.
     */
    fun observeSharedBooks(): Flow<List<CloudRagBook>> =
        db.collection("rag_books")
            .whereEqualTo("isShared", true)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .map { CloudRagBook.fromSnapshot(it) }
                    .filter { it.bookId !in LEGACY_SEED_BOOK_IDS }
            }

    /**
     * Real-time stream of book-scoped shared small folders (`/rag_folders` where `isShared == true`
     * AND parent `bookId` is in currently shared `/rag_books`).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeSharedFolders(): Flow<List<CloudRagFolder>> =
        observeSharedBooks().flatMapLatest { sharedBooks ->
            val sharedBookIds = sharedBooks
                .filter { it.isShared && it.bookId !in LEGACY_SEED_BOOK_IDS }
                .map { it.bookId }
                .filter { it.isNotBlank() }
                .distinct()
            if (sharedBookIds.isEmpty()) {
                flowOf(emptyList())
            } else {
                val sliceFlows = sharedBookIds.chunked(MAX_BOOK_IN_QUERY_SIZE).map { slice ->
                    db.collection("rag_folders")
                        .whereEqualTo("isShared", true)
                        .whereIn("bookId", slice)
                        .snapshots()
                        .map { snapshot -> snapshot.documents.map { CloudRagFolder.fromSnapshot(it) } }
                        .catch { err ->
                            val fsEx = err as? FirebaseFirestoreException
                                ?: (err.cause as? FirebaseFirestoreException)
                            if (fsEx?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED && authProvider() != null) {
                                emit(emptyList())
                            } else {
                                throw err
                            }
                        }
                }
                combine(sliceFlows) { slices ->
                    slices.flatMap { it }
                        .filter { it.bookId !in LEGACY_SEED_BOOK_IDS }
                        .distinctBy { it.folderId }
                }
            }
        }

    /**
     * Real-time stream of all Cloud RAG books accessible to the current user (owned private + shared).
     */
    fun observeAccessibleBooks(): Flow<List<CloudRagBook>> {
        val uid = authProvider()
        if (uid.isNullOrBlank()) return observeSharedBooks()
        val ownedFlow = db.collection("rag_books")
            .whereEqualTo("ownerId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .map { CloudRagBook.fromSnapshot(it) }
                    .filter { it.bookId !in LEGACY_SEED_BOOK_IDS }
            }
        return combine(observeSharedBooks(), ownedFlow) { shared, owned ->
            (owned + shared).distinctBy { it.bookId }
        }
    }

    /**
     * Real-time stream of all Cloud RAG folders accessible to the current user (owned private + shared).
     */
    fun observeAccessibleFolders(): Flow<List<CloudRagFolder>> {
        val uid = authProvider()
        if (uid.isNullOrBlank()) return observeSharedFolders()
        val ownedFlow = db.collection("rag_folders")
            .whereEqualTo("ownerId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .map { CloudRagFolder.fromSnapshot(it) }
                    .filter { it.bookId !in LEGACY_SEED_BOOK_IDS }
            }
        return combine(observeSharedFolders(), ownedFlow) { shared, owned ->
            (owned + shared).distinctBy { it.folderId }
        }
    }

    suspend fun getSharedBooks(): Result<List<CloudRagBook>> {
        return try {
            val sharedSnap = db.collection("rag_books")
                .whereEqualTo("isShared", true)
                .get()
                .await()
            val sharedList = sharedSnap.documents
                .map { CloudRagBook.fromSnapshot(it) }
                .filter { it.bookId !in LEGACY_SEED_BOOK_IDS }
            val uid = authProvider()
            val ownedList = if (!uid.isNullOrBlank()) {
                try {
                    db.collection("rag_books")
                        .whereEqualTo("ownerId", uid)
                        .get()
                        .await()
                        .documents
                        .map { CloudRagBook.fromSnapshot(it) }
                        .filter { it.bookId !in LEGACY_SEED_BOOK_IDS }
                } catch (_: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
            Result.success((ownedList + sharedList).distinctBy { it.bookId })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "rag_books")
            Result.failure(e)
        }
    }

    suspend fun getSharedFolders(): Result<List<CloudRagFolder>> {
        return try {
            val accessibleBooks = getSharedBooks().getOrThrow()
            val uid = authProvider()
            val ownedList = if (!uid.isNullOrBlank()) {
                try {
                    db.collection("rag_folders")
                        .whereEqualTo("ownerId", uid)
                        .get()
                        .await()
                        .documents
                        .map { CloudRagFolder.fromSnapshot(it) }
                } catch (_: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }

            val otherSharedBookIds = accessibleBooks
                .filter { it.isShared && it.ownerId != uid }
                .map { it.bookId }
                .filter { it.isNotBlank() }
                .distinct()

            val sharedOtherList = mutableListOf<CloudRagFolder>()
            for (slice in otherSharedBookIds.chunked(MAX_BOOK_IN_QUERY_SIZE)) {
                try {
                    val snap = db.collection("rag_folders")
                        .whereEqualTo("isShared", true)
                        .whereIn("bookId", slice)
                        .get()
                        .await()
                    sharedOtherList.addAll(snap.documents.map { CloudRagFolder.fromSnapshot(it) })
                } catch (e: FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        // A book in this slice was concurrently revoked; query remaining books individually
                        for (bid in slice) {
                            try {
                                val singleSnap = db.collection("rag_folders")
                                    .whereEqualTo("isShared", true)
                                    .whereEqualTo("bookId", bid)
                                    .get()
                                    .await()
                                sharedOtherList.addAll(singleSnap.documents.map { CloudRagFolder.fromSnapshot(it) })
                            } catch (inner: FirebaseFirestoreException) {
                                if (inner.code != FirebaseFirestoreException.Code.PERMISSION_DENIED) throw inner
                            }
                        }
                    } else {
                        throw e
                    }
                }
            }

            Result.success((ownedList + sharedOtherList).distinctBy { it.folderId })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "rag_folders")
            Result.failure(e)
        }
    }

    suspend fun getBookById(bookId: String): Result<CloudRagBook?> {
        return try {
            val snap = db.collection("rag_books").document(bookId).get().await()
            Result.success(if (snap.exists()) CloudRagBook.fromSnapshot(snap) else null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "rag_books/$bookId")
            Result.failure(e)
        }
    }

    suspend fun getChunksForFolder(folderId: String): Result<List<CloudRagChunk>> {
        return try {
            val uid = authProvider()
            if (uid.isNullOrBlank()) {
                // Unauthenticated callers must hit Firestore rules directly and fail with PERMISSION_DENIED
                val unauthSnap = db.collection("rag_chunks")
                    .whereEqualTo("isShared", true)
                    .whereEqualTo("folderId", folderId)
                    .get()
                    .await()
                return Result.success(
                    unauthSnap.documents.map { CloudRagChunk.fromSnapshot(it) }.sortedBy { it.chunkIndex }
                )
            }

            // 1. Query owner's chunks first (0 rule get() lookups; succeeds for both shared and private owner folders)
            val ownedChunks = try {
                db.collection("rag_chunks")
                    .whereEqualTo("ownerId", uid)
                    .whereEqualTo("folderId", folderId)
                    .get()
                    .await()
                    .documents
                    .map { CloudRagChunk.fromSnapshot(it) }
            } catch (_: Exception) {
                emptyList()
            }
            if (ownedChunks.isNotEmpty()) {
                return Result.success(ownedChunks.distinctBy { it.chunkId }.sortedBy { it.chunkIndex })
            }

            // 2. Otherwise query shared chunks for non-owned folder (rules enforce parent folder & parent book isShared == true)
            val rawSharedChunks = try {
                db.collection("rag_chunks")
                    .whereEqualTo("isShared", true)
                    .whereEqualTo("folderId", folderId)
                    .get()
                    .await()
                    .documents
                    .map { CloudRagChunk.fromSnapshot(it) }
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    emptyList()
                } else {
                    throw e
                }
            }

            val nonOwnerBookIds = rawSharedChunks
                .filter { it.ownerId != uid }
                .map { it.bookId }
                .distinct()
            val verifiedSharedBookIds = if (nonOwnerBookIds.isEmpty()) {
                emptySet()
            } else {
                nonOwnerBookIds.filter { bid ->
                    val book = getBookById(bid).getOrNull()
                    book != null && book.isShared
                }.toSet()
            }
            val sharedChunks = rawSharedChunks.filter { chunk ->
                chunk.ownerId == uid || chunk.bookId in verifiedSharedBookIds
            }
            Result.success(sharedChunks.distinctBy { it.chunkId }.sortedBy { it.chunkIndex })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "rag_chunks?folderId=$folderId")
            Result.failure(e)
        }
    }

    /**
     * Uploads a structured book (metadata + small folders + cited chunks) to Cloud Firestore
     * in parent-first batches sized within Firestore's 20-getAfter-calls-per-batch rule ceiling.
     */
    suspend fun uploadBookBundle(bundle: SeedRagBookBundle): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = requireAuthenticatedUid()
            val normalizedShared = bundle.book.isShared
            val bookPayload = bundle.book.copy(isShared = normalizedShared).toCreateMap(uid)
            val folderEntries = bundle.folders.map { folder ->
                folder.folderId to folder.copy(
                    bookId = bundle.book.bookId,
                    isShared = normalizedShared
                ).toCreateMap(uid)
            }
            val chunkEntries = bundle.chunks.map { chunk ->
                chunk.chunkId to chunk.copy(
                    bookId = bundle.book.bookId,
                    isShared = normalizedShared
                ).toCreateMap(uid)
            }

            // Stage 1: Commit parent book + first slice of folders (<= 15 getAfter calls per batch)
            val folderSlices = folderEntries.chunked(MAX_FOLDER_BATCH_OPS)
            if (folderSlices.isEmpty()) {
                val bookBatch = db.batch()
                bookBatch.set(db.collection("rag_books").document(bundle.book.bookId), bookPayload)
                bookBatch.commit().await()
            } else {
                for ((idx, slice) in folderSlices.withIndex()) {
                    val batch = db.batch()
                    if (idx == 0) {
                        batch.set(db.collection("rag_books").document(bundle.book.bookId), bookPayload)
                    }
                    for ((docId, payload) in slice) {
                        batch.set(db.collection("rag_folders").document(docId), payload)
                    }
                    batch.commit().await()
                }
            }

            // Stage 2: Commit chunks in slices of <= 9 (2 getAfter calls/chunk = <= 18 calls/batch)
            for (slice in chunkEntries.chunked(MAX_CHUNK_BATCH_OPS)) {
                val batch = db.batch()
                for ((docId, payload) in slice) {
                    batch.set(db.collection("rag_chunks").document(docId), payload)
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "rag_books/${bundle.book.bookId}")
            Result.failure(e)
        }
    }

    /**
     * Updates sharing state (`isShared`) consistently across a book, all its folders, and all its chunks
     * owned by the currently authenticated user.
     */
    suspend fun setBookShared(bookId: String, isShared: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = requireAuthenticatedUid()
            val bookRef = db.collection("rag_books").document(bookId)
            val bookSnap = bookRef.get().await()
            if (!bookSnap.exists()) {
                return@withContext Result.failure(NoSuchElementException("Book '$bookId' does not exist."))
            }
            val ownerId = bookSnap.getString("ownerId").orEmpty()
            if (ownerId != uid) {
                return@withContext Result.failure(SecurityException("Only the book owner can change sharing permissions."))
            }

            val folderRefs = db.collection("rag_folders")
                .whereEqualTo("ownerId", uid)
                .whereEqualTo("bookId", bookId)
                .get()
                .await()
                .documents
                .map { it.reference }

            val chunkRefs = db.collection("rag_chunks")
                .whereEqualTo("ownerId", uid)
                .whereEqualTo("bookId", bookId)
                .get()
                .await()
                .documents
                .map { it.reference }

            val updatePayload = mapOf(
                "isShared" to isShared,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            // Update book + folders first so getAfter(book) & getAfter(folder) match incoming isShared
            val folderSlices = folderRefs.chunked(MAX_FOLDER_BATCH_OPS)
            if (folderSlices.isEmpty()) {
                val batch = db.batch()
                batch.update(bookRef, updatePayload)
                batch.commit().await()
            } else {
                for ((idx, slice) in folderSlices.withIndex()) {
                    val batch = db.batch()
                    if (idx == 0) {
                        batch.update(bookRef, updatePayload)
                    }
                    for (ref in slice) {
                        batch.update(ref, updatePayload)
                    }
                    batch.commit().await()
                }
            }

            for (slice in chunkRefs.chunked(MAX_CHUNK_BATCH_OPS)) {
                val batch = db.batch()
                for (ref in slice) {
                    batch.update(ref, updatePayload)
                }
                batch.commit().await()
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "rag_books/$bookId")
            Result.failure(e)
        }
    }

    /**
     * Deletes a book and cascades deletion across all child chunks and folders so no orphaned records remain.
     */
    suspend fun deleteBookBundle(bookId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = requireAuthenticatedUid()
            val bookRef = db.collection("rag_books").document(bookId)
            val bookSnap = bookRef.get().await()
            if (!bookSnap.exists()) {
                return@withContext Result.success(Unit)
            }
            if (bookSnap.getString("ownerId") != uid) {
                return@withContext Result.failure(SecurityException("Only the book owner can delete this book."))
            }

            val chunkRefs = db.collection("rag_chunks")
                .whereEqualTo("ownerId", uid)
                .whereEqualTo("bookId", bookId)
                .get()
                .await()
                .documents
                .map { it.reference }

            val folderRefs = db.collection("rag_folders")
                .whereEqualTo("ownerId", uid)
                .whereEqualTo("bookId", bookId)
                .get()
                .await()
                .documents
                .map { it.reference }

            // Delete leaves first (chunks -> folders -> book) so non-owners never see dangling children
            val allDeletes = chunkRefs + folderRefs + bookRef
            for (slice in allDeletes.chunked(MAX_DELETE_BATCH_OPS)) {
                val batch = db.batch()
                for (ref in slice) {
                    batch.delete(ref)
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "rag_books/$bookId")
            Result.failure(e)
        }
    }

    /**
     * Cascade-deletes the 5 legacy seed books (and their folders & chunks) owned by the authenticated user
     * from Cloud Firestore so the user starts with a clean library for their own books.
     */
    suspend fun purgeLegacyFiveSeedBooks(): Int = withContext(Dispatchers.IO) {
        val uid = authProvider()?.takeIf { it.isNotBlank() } ?: return@withContext 0
        var deletedCount = 0
        try {
            val ownedLegacyIds = runCatching {
                db.collection("rag_books")
                    .whereEqualTo("ownerId", uid)
                    .get()
                    .await()
                    .documents
                    .map { CloudRagBook.fromSnapshot(it) }
                    .filter { it.bookId in LEGACY_SEED_BOOK_IDS || it.docId in LEGACY_SEED_BOOK_IDS }
                    .map { it.docId.ifBlank { it.bookId } }
            }.getOrDefault(emptyList())

            val candidateIds = (LEGACY_SEED_BOOK_IDS + ownedLegacyIds).distinct()
            for (seedBookId in candidateIds) {
                val res = runCatching {
                    val snap = db.collection("rag_books").document(seedBookId).get().await()
                    if (snap.exists() && snap.getString("ownerId") == uid) {
                        deleteBookBundle(seedBookId)
                    } else {
                        Result.success(Unit)
                    }
                }.getOrElse { Result.failure(it) }
                if (res.isSuccess) deletedCount++
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Non-fatal notice purging legacy 5 seed books: ${e.message}")
        }
        deletedCount
    }

    /**
     * Two-stage Small-Folder -> Cited-Chunk hybrid lexical + Arabic morphological stem + IDF + 3-gram RAG retrieval.
     * Queries live Cloud Firestore books uploaded by users (never bundled seed books).
     */
    suspend fun retrieveRelevantPassages(
        query: String,
        cachedCloudFolders: List<CloudRagFolder> = emptyList(),
        topFoldersLimit: Int = 4,
        topChunksLimit: Int = 5
    ): List<RetrievedRagPassage> = withContext(Dispatchers.IO) {
        val tokens = extractSearchTokens(query)
        val queryStems = extractSearchStems(query)
        val normQuery = normalizeSearchText(query)
        if (tokens.isEmpty() && normQuery.length < 3) return@withContext emptyList()
        val queryTrigrams = buildCharTrigrams(normQuery)

        val uid = authProvider()
        val isSignedIn = !uid.isNullOrBlank()
        val cloudFolders = (if (cachedCloudFolders.isNotEmpty()) {
            cachedCloudFolders
        } else if (isSignedIn) {
            getSharedFolders().getOrNull().orEmpty()
        } else {
            emptyList()
        }).filter { it.bookId !in LEGACY_SEED_BOOK_IDS }

        val mergedIndexedFolders = cloudFolders.map { indexFolder(it) }
        if (mergedIndexedFolders.isEmpty()) return@withContext emptyList()

        // Compute inverse document frequency (IDF) weights per query token across folders
        val totalFolders = mergedIndexedFolders.size.coerceAtLeast(1)
        val tokenIdf = tokens.associateWith { tok ->
            val df = mergedIndexedFolders.count { idxF ->
                idxF.titleNorm.contains(tok) ||
                    idxF.summaryNorm.contains(tok) ||
                    idxF.kwNorm.any { it.contains(tok) }
            }
            1.0 + kotlin.math.ln((totalFolders + 1.0) / (df + 1.0))
        }

        // Stage 1: Score & rank small thematic folders
        val rankedFolders = mergedIndexedFolders
            .map { idxF ->
                var score = 0.0
                val exactPhraseHit = normQuery.length >= 4 && (idxF.titleNorm.contains(normQuery) || idxF.summaryNorm.contains(normQuery))
                if (exactPhraseHit) {
                    score += 8.5
                }
                var lexicalHits = 0
                for (tok in tokens) {
                    val idf = tokenIdf[tok] ?: 1.0
                    var matchedToken = false
                    if (idxF.titleNorm.contains(tok)) {
                        score += 3.2 * idf
                        matchedToken = true
                    }
                    if (idxF.kwNorm.any { it.contains(tok) || tok.contains(it) }) {
                        score += 2.9 * idf
                        matchedToken = true
                    }
                    if (idxF.summaryNorm.contains(tok)) {
                        score += 1.6 * idf
                        matchedToken = true
                    }
                    if (idxF.bookNorm.contains(tok)) {
                        score += 1.0 * idf
                        matchedToken = true
                    }
                    if (matchedToken) lexicalHits++
                }
                val stemHits = queryStems.count { it in idxF.stems }
                if (stemHits > 0) {
                    score += stemHits * 2.1
                }
                if (exactPhraseHit || lexicalHits > 0 || stemHits > 0) {
                    val trigramSim = diceSimilarity(queryTrigrams, idxF.trigrams)
                    if (trigramSim >= 0.18) {
                        score += trigramSim * 4.5
                    }
                }
                idxF.folder to score
            }
            .filter { it.second > 0.0 }
            .sortedByDescending { it.second }
            .take(topFoldersLimit)

        if (rankedFolders.isEmpty()) return@withContext emptyList()

        // Stage 2: Fetch & score chunks only from the winning small folders in Cloud Firestore
        val candidateChunks = mutableListOf<Pair<CloudRagChunk, Double>>()
        for ((folder, folderScore) in rankedFolders) {
            val cloudChunks = if (isSignedIn) {
                getChunksForFolder(folder.folderId).getOrNull().orEmpty()
            } else {
                emptyList()
            }
            val indexedChunks = cloudChunks.map { indexChunk(it) }

            for (idxC in indexedChunks) {
                var cScore = folderScore * 0.35
                val exactChunkHit = normQuery.length >= 4 && idxC.contentNorm.contains(normQuery)
                if (exactChunkHit) {
                    cScore += 9.5
                }
                var chunkLexicalHits = 0
                for (tok in tokens) {
                    val idf = tokenIdf[tok] ?: 1.0
                    var hit = false
                    if (idxC.titleNorm.contains(tok)) {
                        cScore += 3.0 * idf
                        hit = true
                    }
                    if (idxC.kwNorm.any { it.contains(tok) }) {
                        cScore += 2.5 * idf
                        hit = true
                    }
                    val occurrences = countOccurrences(idxC.contentNorm, tok)
                    if (occurrences > 0) {
                        cScore += minOf(occurrences * 1.6 * idf, 7.5)
                        hit = true
                    }
                    if (hit) chunkLexicalHits++
                }
                val chunkStemHits = queryStems.count { it in idxC.stems }
                if (chunkStemHits > 0) {
                    cScore += chunkStemHits * 1.8
                }
                if ((exactChunkHit || chunkLexicalHits > 0 || chunkStemHits > 0) && cScore > 1.2) {
                    candidateChunks.add(idxC.chunk to cScore)
                }
            }
        }

        candidateChunks
            .sortedByDescending { it.second }
            .distinctBy { it.first.chunkId }
            .take(topChunksLimit)
            .map { (chunk, score) -> RetrievedRagPassage(chunk = chunk, score = score) }
    }

    /**
     * Collects representative knowledge chunks strictly from live user-uploaded Library books in Firestore
     * so interactive quizzes are strictly grounded inside the user's books with page citations.
     */
    suspend fun getBookPassagesForQuiz(
        bookId: String? = null,
        limit: Int = 12
    ): List<CloudRagChunk> = withContext(Dispatchers.IO) {
        val uid = authProvider()
        if (uid.isNullOrBlank()) return@withContext emptyList()

        val collected = mutableListOf<CloudRagChunk>()
        val books = getSharedBooks().getOrNull().orEmpty()
            .filter { it.bookId !in LEGACY_SEED_BOOK_IDS }
            .let { all -> if (bookId.isNullOrBlank()) all else all.filter { it.bookId == bookId } }
        val bookIds = books.map { it.bookId }.distinct()
        if (bookIds.isNotEmpty()) {
            val bookIdSet = bookIds.toSet()
            val folders = getSharedFolders().getOrNull().orEmpty()
                .filter { it.bookId in bookIdSet }
            val sampledFolders = if (bookId.isNullOrBlank()) {
                folders.groupBy { it.bookId }.values.flatMap { it.take(2) }.take(8)
            } else {
                folders.take(6)
            }
            for (folder in sampledFolders) {
                val chunks = getChunksForFolder(folder.folderId).getOrNull().orEmpty()
                collected.addAll(chunks.take(3))
                if (collected.size >= limit * 2) break
            }
        }

        collected
            .distinctBy { it.chunkId }
            .shuffled()
            .take(limit)
    }
}
