package com.codecx.videodownloader.features.ui.viewmodel

import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codecx.videodownloader.data.models.MediaModel
import com.codecx.videodownloader.data.repo.PlayerRepositry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class PlayerViewModel(
    private val repo : PlayerRepositry
) : ViewModel() {

    private val _videos = MutableStateFlow<List<MediaModel>>(emptyList())
    val videos: StateFlow<List<MediaModel>> = _videos
    private val _audios = MutableStateFlow<List<MediaModel>>(emptyList())
    val audios: StateFlow<List<MediaModel>> = _audios

    private val _videoFolders = MutableStateFlow<List<String>>(emptyList())
    val videoFolders: StateFlow<List<String>> = _videoFolders
    private val _audioFolders = MutableStateFlow<List<String>>(emptyList())
    val audioFolders: StateFlow<List<String>> = _audioFolders
    var selectedFolder = "All"
    var showingVideos = true
    private var lastLoadedVideoFolder: String? = null
    private var lastLoadedAudioFolder: String? = null
    fun loadVideos(context: Context, folder: String = "All", forceRefresh: Boolean = false) {
        if (!forceRefresh && _videos.value.isNotEmpty() && lastLoadedVideoFolder == folder) return
        viewModelScope.launch {
            _videos.value = repo.getVideos(context,folder)
            lastLoadedVideoFolder = folder
        }
    }
    fun loadAudio(context: Context, folder: String = "All", forceRefresh: Boolean = false) {
        if (!forceRefresh && _audios.value.isNotEmpty() && lastLoadedAudioFolder == folder) return
        viewModelScope.launch {
            _audios.value = repo.getAudios(context,folder)
            lastLoadedAudioFolder = folder
        }
    }
    fun loadVideoFolders(context: Context, forceRefresh: Boolean = false) {
        if (!forceRefresh && _videoFolders.value.isNotEmpty()) return
        viewModelScope.launch {
            _videoFolders.value = repo.getVideoFolders(context)
        }
    }
    fun loadAudioFolder(context: Context, forceRefresh: Boolean = false) {
        if (!forceRefresh && _audioFolders.value.isNotEmpty()) return
        viewModelScope.launch {
            _audioFolders.value = repo.getAudioFolders(context)
        }
    }

    fun deleteMedia(context: Context, media: MediaModel, onIntentSenderNeeded: (PendingIntent) -> Unit, onSuccess: () -> Unit) {
        val contentResolver = context.contentResolver
        val uri = getMediaUri(media) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = MediaStore.createDeleteRequest(contentResolver, listOf(uri))
            onIntentSenderNeeded(pendingIntent)
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            try {
                contentResolver.delete(uri, null, null)
                onSuccess()
            } catch (e: SecurityException) {
                val recoverableSecurityException = e as? android.app.RecoverableSecurityException
                if (recoverableSecurityException != null) {
                    onIntentSenderNeeded(recoverableSecurityException.userAction.actionIntent)
                }
            }
        } else {
            try {
                contentResolver.delete(uri, null, null)
                val file = File(media.path)
                if (file.exists()) file.delete()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getMediaUri(media: MediaModel): Uri? {
        return try {
            if (media.path.startsWith("content://")) {
                Uri.parse(media.path)
            } else {
                val contentUri = if (media.isAudio) {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                Uri.withAppendedPath(contentUri, media.id.toString())
            }
        } catch (e: Exception) {
            null
        }
    }

}