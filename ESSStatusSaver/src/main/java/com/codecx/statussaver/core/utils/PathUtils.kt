package com.codecx.statussaver.core.utils

import android.content.Context
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Environment

object PathUtils {
    private const val PATH_SEPARATOR = '/'


    val Context.USER_SAVED_STATUS
        get() =
            if (SDK_INT <= Q)
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + PATH_SEPARATOR + "WAStatuses"
            else
                this.getExternalFilesDir(null)?.absolutePath + PATH_SEPARATOR + "WAStatuses"

    val WHATSAPP_DOCUMENT_ID get() = "primary:Android/media/com.whatsapp/WhatsApp/Media/.Statuses"
    val WHATS_APP_STATUS_PATH
        get() = if (SDK_INT <= Q)
            Environment.getExternalStorageDirectory().absolutePath + PATH_SEPARATOR + "WhatsApp/Media/.Statuses"
        else
            Environment.getExternalStorageDirectory().absolutePath + PATH_SEPARATOR + "Android/Media/com.whatsapp/WhatsApp/Media/.Statuses"

    fun isRightDirectory(uri: Uri): Boolean {
        val path = uri.toString()
        return path.contains("com.whatsapp%2FWhatsApp%2FMedia%2F.Statuses") ||
                path.contains("com.whatsapp.w4b%2FWhatsApp%20Business%2FMedia%2F.Statuses")
    }

}