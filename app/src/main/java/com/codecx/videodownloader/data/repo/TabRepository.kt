package com.codecx.videodownloader.data.repo

import android.os.Bundle
import com.codecx.videodownloader.data.models.TabModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TabRepository {

    private val _tabs = MutableStateFlow<List<TabModel>>(emptyList())
    val tabs: StateFlow<List<TabModel>> = _tabs.asStateFlow()

    private val webViewStates =
        mutableMapOf<String, Bundle>()

    init {
        addTab()
    }

    fun getSelectedTab(): TabModel? {
        return _tabs.value.find { it.isSelected }
    }

    fun addTab(): TabModel {

        val updatedTabs = _tabs.value.map {
            it.copy(isSelected = false)
        }

        val tab = TabModel(
            title = "Home",
            url = "about:blank",
            isSelected = true
        )

        _tabs.value = updatedTabs + tab

        return tab
    }

    fun selectTab(tabId: String) {

        _tabs.value = _tabs.value.map { tab ->
            tab.copy(
                isSelected = tab.id == tabId
            )
        }
    }

    fun removeTab(tabId: String) {

        webViewStates.remove(tabId)

        val currentTabs = _tabs.value

        val removedTab = currentTabs.find {
            it.id == tabId
        }

        val remainingTabs = currentTabs.filter {
            it.id != tabId
        }

        if (remainingTabs.isEmpty()) {
            addTab()
            return
        }

        if (removedTab?.isSelected == true) {

            val newSelectedTab = remainingTabs.last()

            _tabs.value = remainingTabs.map {
                it.copy(
                    isSelected = it.id == newSelectedTab.id
                )
            }

        } else {
            _tabs.value = remainingTabs
        }
    }

    fun clearAll() {

        val homeTab = TabModel(
            title = "Home",
            url = "about:blank",
            isSelected = true
        )

        _tabs.value = listOf(homeTab)
    }
    fun updateTab(
        tabId: String,
        title: String? = null,
        url: String? = null,
        favicon: Int? = null
    ) {

        _tabs.value = _tabs.value.map { tab ->

            if (tab.id == tabId) {
                tab.copy(
                    title = title ?: tab.title,
                    url = url ?: tab.url,
                    favicon = favicon ?: tab.favicon
                )
            } else {
                tab
            }
        }
    }

    fun getTab(tabId: String): TabModel? {
        return _tabs.value.find {
            it.id == tabId
        }
    }

    fun saveWebViewState(
        tabId: String,
        state: Bundle
    ) {
        webViewStates[tabId] = state
    }

    fun getWebViewState(
        tabId: String
    ): Bundle? {
        return webViewStates[tabId]
    }

    fun removeWebViewState(
        tabId: String
    ) {
        webViewStates.remove(tabId)
    }

    fun getTabCount(): Int {
        return _tabs.value.size
    }
}