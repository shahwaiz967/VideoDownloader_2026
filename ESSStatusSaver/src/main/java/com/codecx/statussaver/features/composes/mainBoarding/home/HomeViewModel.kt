package com.codecx.statussaver.features.composes.mainBoarding.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codecx.statussaver.core.ui.states.ResultData
import com.codecx.statussaver.data.models.StatusItem
import com.codecx.statussaver.data.repos.appRepo.AppRepoImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val appRepoImp: AppRepoImp) : ViewModel() {
    private val _statuses = MutableStateFlow<ResultData<List<StatusItem>>>(value = ResultData.Init)
    val statuses = _statuses.asStateFlow().onStart {
        loadData()
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ResultData.Init
    )


    fun loadData() = viewModelScope.launch {
        _statuses.value = ResultData.Loading
        runCatching {
            withContext(Dispatchers.IO) {
                appRepoImp.loadStatus()
            }
        }.onFailure {
            _statuses.value = ResultData.Error(it)
        }.onSuccess {
            _statuses.value = ResultData.Success(it)
        }
    }

    fun updateSelectedDirPath(userDirPath: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                appRepoImp.userPreferences.updateSelectedDirPath(userDirPath)
            }
            loadData()
        }
    }

    fun saveStatus(item: StatusItem, onSuccess: () -> Unit, onError: () -> Unit) =
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    appRepoImp.saveStatus(item)
                }
            }.onFailure {
                onError.invoke()
            }.onSuccess { savedStatusUri ->
                val successData = (statuses.value as? ResultData.Success)?.data
                if (successData != null) {
                    val updatedData = successData.map {
                        if (it.path == item.path) {
                            it.copy(isSaved = true)
                        } else {
                            it
                        }
                    }.toMutableList()
                    updatedData.add(
                        item.copy(
                            isSaved = true,
                            isUserSaved = true,
                            path = savedStatusUri.toString()
                        )
                    )
                    _statuses.value = ResultData.Success(updatedData)
                    onSuccess.invoke()
                }
            }

        }

}