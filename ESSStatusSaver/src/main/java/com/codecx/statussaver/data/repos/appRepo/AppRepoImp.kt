package com.codecx.statussaver.data.repos.appRepo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.R
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.codecx.statussaver.core.utils.PathUtils
import com.codecx.statussaver.core.utils.exceptions.PathRequired
import com.codecx.statussaver.core.utils.exceptions.PermissionRequired
import com.codecx.statussaver.data.models.StatusItem
import kotlinx.coroutines.flow.first
import java.io.File
import androidx.core.net.toUri
import com.codecx.statussaver.core.utils.PathUtils.USER_SAVED_STATUS

class AppRepoImp(private val appContext: Context, val userPreferences: UserPreferences) :
    AppRepo {

    private fun isPermissionGranted(): Boolean {
        return if (SDK_INT >= R) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override suspend fun loadStatus(): List<StatusItem> {
        val statuses = mutableListOf<StatusItem>()

        if (!isPermissionGranted()) throw PermissionRequired()
        val savedStatus = getUserSavedStatuses()
        if (SDK_INT >= R) {
            val treePathUri = userPreferences.userSelectedDirPath
            if (treePathUri.isEmpty()) throw PathRequired()
            val rootFolder = DocumentFile.fromTreeUri(appContext, treePathUri.toUri())
            if (rootFolder == null || !rootFolder.exists()) throw PathRequired()
            rootFolder.listFiles().forEach { file ->
                if (file.isFile && file.name?.startsWith(".") == false) {
                    val mimeType = getMimeType(file.uri)
                    statuses.add(
                        StatusItem(
                            title = file.name ?: "",
                            path = file.uri.toString(),
                            mimeType = mimeType,
                            dateTime = file.lastModified(),
                            isSaved = savedStatus.any {
                                it.title == file.name && it.mimeType == mimeType
                            }
                        )
                    )

                }
            }
        } else {
            val wpFolder = File(PathUtils.WHATS_APP_STATUS_PATH)
            if (wpFolder.exists()) {
                wpFolder.listFiles()?.forEach { file ->
                    if (file.isFile && !file.name.startsWith(".")) {
                        val mimeType = MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(file.extension) ?: ""
                        statuses.add(
                            StatusItem(
                                title = file.name,
                                path = file.absolutePath,
                                mimeType = mimeType,
                                dateTime = file.lastModified(),
                                isSaved =savedStatus.any {
                                    it.title == file.name && it.mimeType == mimeType
                                }
                            )
                        )
                    }
                }
            }
        }
        statuses.addAll(savedStatus)
        return statuses.sortedByDescending { it.dateTime }
    }


    private fun getMimeType(uri: Uri): String {
        return appContext.contentResolver.getType(uri) ?: ""
    }

    private fun getUserSavedStatuses(): List<StatusItem> {
        val folder = File(appContext.USER_SAVED_STATUS)
        if (!folder.exists()) return emptyList()
        return folder.listFiles()?.map {
            StatusItem(
                title = it.name,
                path = it.path,
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension) ?: "",
                dateTime = it.lastModified(),
                isSaved = true,
                isUserSaved = true
            )
        } ?: emptyList()
    }

    fun saveStatus(item: StatusItem): Uri {
        val folder = File(appContext.USER_SAVED_STATUS)
        if (!folder.exists()) folder.mkdir()

        val outputFile = File(folder, item.title)

        val uri = item.path.toUri()

        appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return outputFile.toUri()
    }
}