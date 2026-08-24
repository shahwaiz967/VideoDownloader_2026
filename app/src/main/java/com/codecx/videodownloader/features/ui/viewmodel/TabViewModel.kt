package com.codecx.videodownloader.features.ui.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.codecx.videodownloader.data.models.TabModel
import com.codecx.videodownloader.data.repo.TabRepository

class TabViewModel(
    private val repository: TabRepository
) : ViewModel() {

    val tabs = repository.tabs

    fun addTab(): TabModel {
        return repository.addTab()
    }

    fun selectTab(tabId: String) {
        repository.selectTab(tabId)
    }

    fun removeTab(tabId: String) {
        repository.removeTab(tabId)
    }

    fun clearAll() {
        repository.clearAll()
    }

    fun updateTab(
        tabId: String,
        title: String? = null,
        url: String? = null,
        favicon: Int? = null
    ) {
        repository.updateTab(
            tabId = tabId,
            title = title,
            url = url,
            favicon = favicon
        )
    }

    fun getSelectedTab(): TabModel? {
        return repository.getSelectedTab()
    }

    fun getTabCount(): Int {
        return repository.getTabCount()
    }

    fun saveWebViewState(
        tabId: String,
        state: Bundle
    ) {
        repository.saveWebViewState(tabId, state)
    }

    fun getWebViewState(
        tabId: String
    ): Bundle? {
        return repository.getWebViewState(tabId)
    }

    fun removeWebViewState(
        tabId: String
    ) {
        repository.removeWebViewState(tabId)
    }
}