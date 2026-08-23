package com.codecx.videodownloader.data.repo

import android.R
import android.app.NotificationManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import com.codecx.videodownloader.data.models.DownloadApiResponse
import com.codecx.videodownloader.data.models.DownloadModel
import com.codecx.videodownloader.data.models.DownloadRequest
import com.codecx.videodownloader.data.models.DownloadStatus
import com.codecx.videodownloader.data.utils.AppConstants
import com.codecx.videodownloader.data.utils.FileUtils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.prepareGet
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.coroutines.cancellation.CancellationException

class DownloadRepo(
    private val webClient: HttpClient,
    private val context: Context
) {

    private val _activeDownloads = MutableStateFlow<List<DownloadModel>>(emptyList())
    val activeDownloads: StateFlow<List<DownloadModel>> = _activeDownloads.asStateFlow()

    private val _completedDownloads = MutableStateFlow<List<DownloadModel>>(emptyList())
    val completedDownloads: StateFlow<List<DownloadModel>> = _completedDownloads.asStateFlow()

    private val _downloadCompleteEvent = MutableSharedFlow<DownloadModel>()
    val downloadCompleteEvent: SharedFlow<DownloadModel> = _downloadCompleteEvent.asSharedFlow()

    // ── Internal mutable lists ────────────────────────────────────────────────
    private val activeList = mutableListOf<DownloadModel>()
    private val completedList = mutableListOf<DownloadModel>()

    private val TAG = "DownloadRepo"
    private val STORAGE_FOLDER = "VideoDownloader"

    suspend fun getDownloadInfo(
        url: String
    ): Result<DownloadApiResponse> {
        return runCatching {
            webClient.post(
                "${AppConstants.BASE_URL}${AppConstants.API_ENDPOINT}"
            ) {
                headers.append(
                    "x-rapidapi-key",
                    AppConstants.RAPID_API_KEY
                )

                headers.append(
                    "x-rapidapi-host",
                    AppConstants.RAPID_API_HOST
                )

                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)

                setBody(
                    DownloadRequest(url)
                )
            }.body<DownloadApiResponse>()
        }
    }


    private fun emitActive() {
        _activeDownloads.value = activeList.toList()
    }

    private fun emitCompleted() {
        _completedDownloads.value = completedList.toList()
    }

    suspend fun startDownload(
        item: DownloadModel,
        onProgress: (Int) -> Unit,
        onComplete: (DownloadModel) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        var mediaUri: Uri? = null
        try {
            Log.d(TAG, "Starting download: ${item.url}")

            webClient.prepareGet(item.url) {
                headers.append(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
                )
                headers.append("Accept", "*/*")
                headers.append("Accept-Encoding", "identity")
            }.execute { response ->
                val code = response.status.value
                val isSuccess = response.status.isSuccess()

                if (!isSuccess && code != 206) {
                    withContext(Dispatchers.Main) { onError("Server error: $code") }
                    return@execute
                }

                val totalBytes = response.contentLength() ?: -1L
                val channel = response.bodyAsChannel()

                var activeItem = item.copy(status = DownloadStatus.IN_PROGRESS, progress = 0)
                synchronized(activeList) {
                    activeList.add(activeItem)
                    emitActive()
                }

                val ext = if (item.isAudio) "mp3" else "mp4"
                val fileName = FileUtils.generateFileName(item.title, ext)
                var savedPath = ""

                val outputStream: OutputStream =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val mimeType = if (item.isAudio) "audio/mpeg" else "video/mp4"
                        val collection =
                            if (item.isAudio) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                            else MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                        val values = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                            put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                if (item.isAudio) "${Environment.DIRECTORY_MUSIC}/${STORAGE_FOLDER}"
                                else "${Environment.DIRECTORY_MOVIES}/${STORAGE_FOLDER}"
                            )
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }

                        val uri = context.contentResolver.insert(collection, values)
                            ?: throw Exception("Failed to create file in MediaStore")
                        mediaUri = uri
                        savedPath = uri.toString()
                        context.contentResolver.openOutputStream(uri)
                            ?: throw Exception("Failed to open output stream")
                    } else {
                        val dir =
                            if (item.isAudio) FileUtils.getAudioDirectory() else FileUtils.getDownloadDirectory()
                        val file = File(dir, fileName)
                        savedPath = file.absolutePath
                        FileOutputStream(file)
                    }

                var downloadedBytes = 0L
                var lastProgress = -1
                var lastNotificationTime = 0L

                outputStream.use { output ->
                    val buffer = ByteArray(16 * 1024)

                    while (isActive && !channel.isClosedForRead) {
                        val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                        if (bytesRead <= 0) break

                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        val progress = if (totalBytes > 0) {
                            ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 99)
                        } else {
                            ((downloadedBytes / 1024) % 99).toInt()
                        }

                        val currentTime = System.currentTimeMillis()
                        if (progress != lastProgress && (currentTime - lastNotificationTime > 500)) {
                            lastProgress = progress
                            lastNotificationTime = currentTime
                            activeItem = activeItem.copy(progress = progress)
                            synchronized(activeList) {
                                val index = activeList.indexOfFirst { it.id == activeItem.id }
                                if (index != -1) {
                                    activeList[index] = activeItem
                                    emitActive()
                                }
                            }

                            onProgress(progress)
                        }
                    }
                }

                ensureActive()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mediaUri != null) {
                    val values =
                        ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
                    context.contentResolver.update(mediaUri, values, null, null)
                }

                val fileSize = FileUtils.formatFileSize(downloadedBytes)
                val completed = activeItem.copy(
                    status = DownloadStatus.COMPLETED,
                    localPath = savedPath,
                    fileSize = fileSize,
                    progress = 100
                )
                synchronized(activeList) {
                    activeList.removeAll { it.id == activeItem.id }
                    emitActive()
                }
                synchronized(completedList) {
                    completedList.add(0, completed)
                    emitCompleted()
                }
                onComplete(completed)
                _downloadCompleteEvent.emit(completed)
            }

        } catch (e: CancellationException) {
            Log.d(TAG, "Download cancelled: ${item.url}")
            mediaUri?.let { context.contentResolver.delete(it, null, null) }
            synchronized(activeList) {
                activeList.removeAll { it.id == item.id }
                emitActive()
            }
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Write/Download failed: ${e.message}", e)
            mediaUri?.let { context.contentResolver.delete(it, null, null) }
            synchronized(activeList) {
                activeList.removeAll { it.id == item.id }
                emitActive()
            }
            onError("Download failed: ${e.message}")
        }
    }


    fun loadCompletedFromFolder(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val items = mutableListOf<DownloadModel>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val videoProjection = arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_ADDED
                )

                val selection = "${MediaStore.Video.Media.RELATIVE_PATH} = ?"
                val selectionArgs = arrayOf("${Environment.DIRECTORY_MOVIES}/${STORAGE_FOLDER}/")
                val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    videoProjection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->
                    Log.d(TAG, "Total videos: ${cursor.count}")

                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                    val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                    val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val name = cursor.getString(nameCol) ?: ""
                        val path = cursor.getString(dataCol) ?: ""
                        val size = cursor.getLong(sizeCol)
                        val timestamp = cursor.getLong(dateCol) * 1000

                        val thumbUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                        ).toString()

                        items.add(
                            DownloadModel(
                                id = id,
                                title = name.substringBeforeLast("."),
                                url = "",
                                localPath = path.ifEmpty { thumbUri },
                                thumbnailUrl = thumbUri,
                                fileSize = FileUtils.formatFileSize(size),
                                platform = "",
                                isAudio = false,
                                status = DownloadStatus.COMPLETED,
                                progress = 100,
                                timestamp = timestamp
                            )
                        )
                    }
                }

            } else {
                // ── Below Android 10 ─────────────────────────────────────────────
                FileUtils.getDownloadDirectory()
                    .listFiles()
                    ?.sortedByDescending { it.lastModified() }
                    ?.filter {
                        it.isFile && it.extension.lowercase() in listOf(
                            "mp4",
                            "mkv",
                            "avi",
                            "mov",
                            "webm"
                        )
                    }
                    ?.forEach { file ->
                        items.add(
                            DownloadModel(
                                id = file.lastModified(),
                                title = file.nameWithoutExtension,
                                url = "",
                                localPath = file.absolutePath,
                                thumbnailUrl = "",
                                fileSize = FileUtils.formatFileSize(file.length()),
                                platform = "",
                                isAudio = false,
                                status = DownloadStatus.COMPLETED,
                                progress = 100,
                                timestamp = file.lastModified()
                            )
                        )
                    }
            }

            Log.d(TAG, "Total videos loaded: ${items.size}")

            synchronized(completedList) {
                completedList.clear()
                completedList.addAll(items)
                emitCompleted()
            }
        }
    }
}