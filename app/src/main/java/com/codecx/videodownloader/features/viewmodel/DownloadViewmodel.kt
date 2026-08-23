package com.codecx.videodownloader.features.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.codecx.videodownloader.data.models.DownloadApiResponse
import com.codecx.videodownloader.data.models.DownloadModel
import com.codecx.videodownloader.data.repo.DownloadRepo
import com.codecx.videodownloader.data.states.DownloadWorker
import com.codecx.videodownloader.data.states.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadViewModel(
    private val repository: DownloadRepo,
    private val context: Context
) : ViewModel() {

    private val _downloadInfo =
        MutableStateFlow<ResultState<DownloadApiResponse>>(ResultState.Init)

    val downloadInfo = _downloadInfo.asStateFlow()

    private val _downloadEvent = MutableSharedFlow<DownloadModel?>()
    val downloadEvent = _downloadEvent.asSharedFlow()

    val activeDownloads: StateFlow<List<DownloadModel>> = repository.activeDownloads
    val completedDownloads: StateFlow<List<DownloadModel>> = repository.completedDownloads


    val downloadCompleteEvent: SharedFlow<DownloadModel> = repository.downloadCompleteEvent


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
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(DownloadWorker.createInputData(downloadItem))
            .build()


        WorkManager.getInstance(context).enqueueUniqueWork(
            "download_${downloadItem.id}",
            ExistingWorkPolicy.KEEP,
            downloadWorkRequest
        )
    }

    fun refreshDownloads() {
        repository.loadCompletedFromFolder(context)
    }
}