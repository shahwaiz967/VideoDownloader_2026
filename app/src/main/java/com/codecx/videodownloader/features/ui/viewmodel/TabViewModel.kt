package com.codecx.videodownloader.features.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.codecx.videodownloader.data.models.TabModel
import com.codecx.videodownloader.data.repo.TabRepository
import kotlinx.coroutines.flow.StateFlow

class TabViewModel(
    private val repository: TabRepository
) : ViewModel() {

    val tabs = repository.tabs

    fun addTab(): TabModel {
        return repository.addTab()
    }

    fun selectTab(tabId: Long) {
        repository.selectTab(tabId)
    }

    fun removeTab(tabId: Long) {
        repository.removeTab(tabId)
    }

    fun clearAll() {
        repository.clearAll()
    }

    fun updateTab(
        tabId: Long,
        title: String? = null,
        url: String? = null
    ) {
        repository.updateTab(
            tabId,
            title,
            url
        )
    }
}