package com.codecx.videodownloader.data.states

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.codecx.videodownloader.data.models.DownloadModel
import com.codecx.videodownloader.data.repo.DownloadRepo
import com.codecx.videodownloader.data.utils.AppConstants
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DownloadWorker(
    val appContext: Context, workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters), KoinComponent {

    private val repository: DownloadRepo by inject()

    private var notificationId: Int = 0
    private var title: String = ""
    private val manager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun createForegroundInfo(item: DownloadModel): ForegroundInfo {
        notificationId = if (item.id == 0L) System.currentTimeMillis().toInt() else item.id.toInt()
        title = item.title.ifEmpty { "Downloading...." }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.apply {
                createNotificationChannel(
                    NotificationChannel(
                        AppConstants.NOTIFICATION_CHANNEL_ID,
                        AppConstants.NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_LOW
                    )
                )
            }
        }
        val notification =
            NotificationCompat.Builder(appContext, AppConstants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.stat_sys_download)
                .setContentTitle(title)
                .setProgress(100, 0, true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    private fun showCompletionNotification(title: String) {
        val notification =
            NotificationCompat.Builder(appContext, AppConstants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.stat_sys_download_done)
                .setContentTitle("Download Complete")
                .setContentText(title)
                .setAutoCancel(true)
                .setOngoing(false)
                .build()
        manager.notify(notificationId, notification)
    }

    private fun updateProgress(progress: Int) {
        val notification =
            NotificationCompat.Builder(appContext, AppConstants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.stat_sys_download)
                .setContentText("$progress%")
                .setProgress(100, progress, false)
                .setOngoing(true)
                .build()
        manager.notify(notificationId, notification)
    }


    override suspend fun doWork(): Result {
        val jsonString = inputData.getString(KEY_ITEM) ?: return Result.failure()
        val downloadItem = Json.decodeFromString<DownloadModel>(jsonString)

        setForeground(createForegroundInfo(downloadItem))
        val deffer = CompletableDeferred<DownloadModel?>()

        try {
            repository.startDownload(
                item = downloadItem,
                onProgress = { progress ->
                    updateProgress(progress)
                },
                onComplete = { completedItem ->
                    if (deffer.isActive) {
                        deffer.complete(completedItem)
                    }
                },
                onError = { error ->
                    Log.e("DownloadWorker", "Download error: $error")
                    if (deffer.isActive) {
                        deffer.complete(null)
                    }
                }
            )
        } catch (e: Exception) {
            if (deffer.isActive) {
                deffer.complete(null)
            }
        }

        val result = deffer.await()
        return if (result != null) {
            showCompletionNotification(result.title)
            Result.success()
        } else {
            Result.failure()
        }
    }

    companion object {
        const val KEY_ITEM = "key_ITEM"
        fun createInputData(item: DownloadModel): Data {
            return Data.Builder()
                .putString(KEY_ITEM, Json.encodeToString(item))
                .build()
        }
    }


}