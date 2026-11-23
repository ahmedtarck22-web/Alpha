package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.R
import com.example.data.model.CloudVideoItem
import com.example.data.model.FileSearchDefaults
import com.example.firebase.OperationType
import com.example.firebase.ensureFirebaseInitialized
import com.example.firebase.handleFirestoreError
import com.example.service.CloudStoragePdfService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
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
import java.util.Locale
import java.util.UUID

class FirestoreVideoRepository(
    private val db: FirebaseFirestore,
    private val appContext: Context,
    private val storageService: CloudStoragePdfService = CloudStoragePdfService(),
    private val authProvider: () -> String? = {
        runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
    }
) {
    constructor(context: Context) : this(
        db = resolveDefaultFirestore(context),
        appContext = context.applicationContext ?: context
    )

    companion object {
        private const val TAG = "FirestoreVideoRepo"
        private const val MAX_VIDEO_BYTES = 524_288_000L // 500 MB matching firestore.rules

        private fun resolveDefaultFirestore(context: Context): FirebaseFirestore {
            val appCtx = context.applicationContext ?: context
            ensureFirebaseInitialized(appCtx)
            return FirebaseFirestore.getInstance(
                appCtx.getString(R.string.firestore_database_id)
            )
        }
    }

    private fun requireAuthenticatedUid(): String {
        val uid = authProvider()?.trim().orEmpty()
        if (uid.isBlank()) {
            throw SecurityException("يجب تسجيل الدخول بحساب Google أولاً لرفع أو إدارة الفيديوهات السحابية.")
        }
        return uid
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeAccessibleVideos(authUidFlow: Flow<String?>): Flow<List<CloudVideoItem>> {
        return authUidFlow.flatMapLatest { uid ->
            if (uid.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                val ownedFlow = db.collection("videos")
                    .whereEqualTo("ownerId", uid)
                    .snapshots()
                    .map { snap -> snap.documents.map { CloudVideoItem.fromSnapshot(it) } }
                    .catch { err ->
                        if (err is CancellationException) throw err
                        handleFirestoreError(err as? Exception ?: Exception(err), OperationType.LIST, "videos?ownerId=$uid")
                        emit(emptyList())
                    }

                val sharedFlow = db.collection("videos")
                    .whereEqualTo("isShared", true)
                    .snapshots()
                    .map { snap -> snap.documents.map { CloudVideoItem.fromSnapshot(it) } }
                    .catch { err ->
                        if (err is CancellationException) throw err
                        val isPerm = (err as? FirebaseFirestoreException)?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                        if (!isPerm) {
                            handleFirestoreError(err as? Exception ?: Exception(err), OperationType.LIST, "videos?isShared=true")
                        }
                        emit(emptyList())
                    }

                combine(ownedFlow, sharedFlow) { owned, shared ->
                    (owned + shared)
                        .distinctBy { it.videoId }
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
            }
        }
    }

    suspend fun uploadVideo(
        uri: Uri,
        title: String,
        description: String,
        durationLabel: String = "",
        isShared: Boolean = true,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Result<CloudVideoItem> = withContext(Dispatchers.IO) {
        try {
            val uid = requireAuthenticatedUid()
            val fileSize = storageService.resolveFileSizeBytes(appContext, uri)
            if (fileSize > MAX_VIDEO_BYTES) {
                return@withContext Result.failure(
                    IllegalArgumentException("حجم الفيديو يتجاوز الحد الأقصى المسموح به (500 ميجابايت).")
                )
            }

            val mimeType = appContext.contentResolver.getType(uri)?.takeIf { it.startsWith("video/") }
                ?: "video/mp4"
            val ext = when {
                mimeType.contains("webm") -> "webm"
                mimeType.contains("3gpp") -> "3gp"
                mimeType.contains("quicktime") -> "mov"
                else -> "mp4"
            }

            val videoId = "vid_" + UUID.randomUUID().toString().replace("-", "").take(14)
            val storagePath = "videos/$uid/$videoId.$ext"
            val displayName = resolveDisplayName(uri)
            val cleanTitle = title.trim()
                .ifBlank { displayName.substringBeforeLast(".").replace("_", " ").trim() }
                .ifBlank { "درس فيديو تعليمي" }
                .take(300)

            onProgress(0.05f, "جارٍ رفع الفيديو «$cleanTitle» إلى سحابة Firebase...")
            val downloadUrl = storageService.uploadMediaUri(
                context = appContext,
                uri = uri,
                bucketName = FileSearchDefaults.DEFAULT_STORAGE_BUCKET,
                storagePath = storagePath,
                contentType = mimeType,
                customMetadata = mapOf(
                    "videoId" to videoId,
                    "ownerId" to uid,
                    "title" to cleanTitle.take(120)
                ),
                onProgress = { fraction ->
                    val pct = (fraction * 100).toInt().coerceIn(0, 100)
                    onProgress(fraction, "جارٍ رفع الفيديو «$cleanTitle» ($pct%)...")
                }
            )

            if (!downloadUrl.startsWith("https://")) {
                return@withContext Result.failure(
                    IllegalStateException("لم يُرجع Firebase Storage رابط تشغيل HTTPS صالح للفيديو.")
                )
            }

            val item = CloudVideoItem(
                docId = videoId,
                videoId = videoId,
                ownerId = uid,
                isShared = isShared,
                title = cleanTitle,
                description = description.trim().take(2000),
                videoDownloadUrl = downloadUrl,
                storagePath = storagePath,
                durationLabel = durationLabel.ifBlank { "فيديو مرفوع" }.take(40),
                fileSizeBytes = fileSize.coerceAtLeast(0L)
            )

            onProgress(0.96f, "جارٍ حفظ بيانات الفيديو في سحابة Firestore...")
            db.collection("videos")
                .document(videoId)
                .set(item.toCreateMap(uid))
                .await()

            onProgress(1f, "تم رفع الفيديو بنجاح وأصبح جاهزاً للمشاهدة أونلاين.")
            Result.success(item)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "videos")
            Log.e(TAG, "Video upload failed", e)
            Result.failure(e)
        }
    }

    suspend fun deleteVideo(video: CloudVideoItem): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = requireAuthenticatedUid()
            if (video.ownerId != uid) {
                return@withContext Result.failure(SecurityException("فقط صاحب الفيديو يمكنه حذفه."))
            }
            db.collection("videos").document(video.videoId).delete().await()
            if (video.storagePath.isNotBlank()) {
                runCatching {
                    val bucket = FileSearchDefaults.DEFAULT_STORAGE_BUCKET.removePrefix("gs://").trimEnd('/')
                    FirebaseStorage.getInstance("gs://$bucket")
                        .reference
                        .child(video.storagePath.trimStart('/'))
                        .delete()
                        .await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "videos/${video.videoId}")
            Result.failure(e)
        }
    }

    private fun resolveDisplayName(uri: Uri): String {
        return try {
            appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
            } ?: uri.lastPathSegment ?: "video.mp4"
        } catch (_: Exception) {
            "video.mp4"
        }
    }
}
