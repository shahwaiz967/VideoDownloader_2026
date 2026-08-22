package com.codecx.videodownloader.data.utils

import android.os.Environment
import java.io.File

object FileUtils {

    fun getDownloadDirectory(): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "VideoDownloader"
        )
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getAudioDirectory(): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "VideoDownloader"
        )
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_000_000 -> String.format("%.1f MB", bytes / 1_000_000.0)
            bytes >= 1_000 -> String.format("%.1f KB", bytes / 1_000.0)
            else -> "$bytes B"
        }
    }

    fun formatDuration(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = ms / (1000 * 60 * 60)
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun generateFileName(title: String, extension: String): String {
        val cleaned = title.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(50)
        return "${cleaned}_${System.currentTimeMillis()}.$extension"
    }
}
