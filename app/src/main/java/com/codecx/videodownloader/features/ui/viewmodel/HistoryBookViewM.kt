package com.codecx.videodownloader.features.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codecx.videodownloader.data.repo.HistoryBookRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryBookViewM(private val repository: HistoryBookRepo) : ViewModel() {

    val historyList = repository.historyList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkList = repository.bookmarkList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHistory(title: String, url: String) = viewModelScope.launch {
        repository.addHistory(title, url)
    }

    fun addBookmark(title: String, url: String) = viewModelScope.launch {
        repository.addBookmark(title, url)
    }

    fun deleteHistoryItem(id: Int) = viewModelScope.launch {
        repository.deleteHistory(id)
    }

    fun deleteBookmarkItem(id: Int) = viewModelScope.launch {
        repository.deleteBookmark(id)
    }

    fun clearBookmarks() = viewModelScope.launch {
        repository.clearBookmarks()
    }

    fun clearHistory() = viewModelScope.launch {
        repository.clearHistory()
    }
}