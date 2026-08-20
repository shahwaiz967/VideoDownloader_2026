package com.codecx.videodownloader.data.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codecx.videodownloader.data.models.MediaModel
import com.codecx.videodownloader.data.repo.PlayerRepositry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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


    fun loadVideos(context: Context, folder: String = "All") {
        if (_videos.value.isNotEmpty() && lastLoadedVideoFolder == folder) return
        viewModelScope.launch {
            _videos.value = repo.getVideos(context,folder)
            lastLoadedVideoFolder = folder
        }
    }
    fun loadAudio(context: Context, folder: String = "All") {
        if (_audios.value.isNotEmpty() && lastLoadedAudioFolder == folder) return
        viewModelScope.launch {
            _audios.value = repo.getAudios(context,folder)
            lastLoadedAudioFolder = folder
        }
    }
    fun loadVideoFolders(context: Context) {
        if (_videoFolders.value.isNotEmpty()) return
        viewModelScope.launch {
            _videoFolders.value = repo.getVideoFolders(context)
        }
    }
    fun loadAudioFolder(context: Context) {
        if (_audioFolders.value.isNotEmpty()) return
        viewModelScope.launch {
            _audioFolders.value = repo.getAudioFolders(context)
        }
    }

}