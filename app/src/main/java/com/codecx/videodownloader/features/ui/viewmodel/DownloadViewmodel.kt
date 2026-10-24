package com.codecx.videodownloader.features.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codecx.videodownloader.data.models.DownloadApiResponse
import com.codecx.videodownloader.data.models.DownloadModel
import com.codecx.videodownloader.data.repo.DownloadRepo
import com.codecx.videodownloader.data.states.ResultState
import com.codecx.videodownloader.data.utils.ReelApiService
import com.codecx.videodownloader.data.utils.isAdultUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadViewModel(
    private val repository: DownloadRepo,
    reelApiService: ReelApiService
) : ViewModel() {

    val trendingVideos = reelApiService.getTrendingVideos().stateIn(
        viewModelScope, SharingStarted.Eagerly,
        ResultState.Loading
    )
    private val _downloadInfo =
        MutableStateFlow<ResultState<DownloadApiResponse>>(ResultState.Init)

    val downloadInfo = _downloadInfo.asStateFlow()

    private val _downloadEvent = MutableSharedFlow<DownloadModel?>()
    val downloadEvent = _downloadEvent.asSharedFlow()

    val activeDownloads: StateFlow<List<DownloadModel>> = repository.activeDownloads
    val completedDownloads: StateFlow<List<DownloadModel>> = repository.completedDownloads


    val downloadCompleteEvent: SharedFlow<DownloadModel> = repository.downloadCompleteEvent

    private val _downloadStartedEvent = MutableSharedFlow<Unit>()
    val downloadStartedEvent = _downloadStartedEvent.asSharedFlow()

    var giftX: Float? = null
    var giftY: Float? = null
    var isGiftVisible: Boolean = true


    private var downloadJob: Job? = null

    fun getDownloadInfo(url: String) {
        if (url.isBlank()) return
        if (url.isAdultUrl()) {
            _downloadInfo.value = ResultState.Error("Downloading from adult sites is restricted")
            return
        }
        if (downloadJob?.isActive == true) return
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _downloadInfo.value = ResultState.Loading
            withContext(Dispatchers.IO) {
                repository.getDownloadInfo(url)
            }.onSuccess { response ->
                _downloadInfo.value = ResultState.Success(response)
            }.onFailure { error ->
                _downloadInfo.value = ResultState.Error(error.message ?: "Something went wrong")
            }

        }
    }

    fun clearResult() {
        _downloadInfo.value = ResultState.Init
    }

    fun startDownload(downloadItem: DownloadModel) {
        repository.startDownload(downloadItem)
        viewModelScope.launch {
            _downloadStartedEvent.emit(Unit)
        }
    }

    fun refreshDownloads() {
        repository.loadCompletedFromFolder()
    }
}