package com.codecx.videodownloader.features.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codecx.videodownloader.data.repo.HistoryBookRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookMarkViewModel(private val repository: HistoryBookRepo) : ViewModel() {

    val bookmarkList = repository.bookmarkList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteBookmarkItem(id: Int) = viewModelScope.launch {
        repository.deleteBookmark(id)
    }

    fun clearBookmarks() = viewModelScope.launch {
        repository.clearBookmarks()
    }
}
