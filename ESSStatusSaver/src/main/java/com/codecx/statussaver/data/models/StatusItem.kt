package com.codecx.statussaver.data.models

import android.content.Intent
import androidx.core.net.toUri

data class StatusItem(
    val title: String = "",
    val path: String = "",
    val mimeType: String = "",
    val dateTime: Long = 0L,
    val isSaved: Boolean = false,
    val isUserSaved: Boolean = false
) {
    fun getPlayIntent(): Intent? {
        return if (isVideo) {
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(path.toUri(), mimeType)
            }
        } else {
            null
        }
    }

    fun getShareIntent(): Intent? {
        return Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, path.toUri())
            type = mimeType
        }
    }


    val isVideo get() = mimeType.startsWith("video/")
}