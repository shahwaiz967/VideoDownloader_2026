package com.codecx.statussaver.core.utils.exceptions

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.net.toUri
import com.codecx.statussaver.core.utils.PathUtils

class PathRequired : Exception("App Need Location Path to work properly") {
    fun requestForPath(pathLauncher: ManagedActivityResultLauncher<Uri?, Uri?>) {
        val authority = "com.android.externalstorage.documents"
        val documentId = PathUtils.WHATSAPP_DOCUMENT_ID
        val uri = "content://$authority/document/${documentId.replace("/", "%2F")}".toUri()
        pathLauncher.launch(uri)
    }
}

