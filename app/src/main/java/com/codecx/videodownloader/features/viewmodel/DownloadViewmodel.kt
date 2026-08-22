package com.codecx.videodownloader.features.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codecx.videodownloader.data.models.DownloadApiResponse
import com.codecx.videodownloader.data.models.DownloadModel
import com.codecx.videodownloader.data.repo.DownloadRepo
import com.codecx.videodownloader.data.states.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadViewModel(
    private val repository: DownloadRepo
) : ViewModel() {

    private val _downloadInfo =
        MutableStateFlow<ResultState<DownloadApiResponse>>(ResultState.Init)

    val downloadInfo = _downloadInfo.asStateFlow()

    private val _downloadEvent = MutableSharedFlow<DownloadModel?>()
    val downloadEvent = _downloadEvent.asSharedFlow()


    private var downloadJob: Job? = null

    fun getDownloadInfo(url: String) {
        if (url.isBlank()) return
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
        viewModelScope.launch {
            repository.startDownload(downloadItem, onDone = { returnItem ->
                launch {
                    _downloadEvent.emit(returnItem)
                }
            })
        }
    }
}