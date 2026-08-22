package com.codecx.videodownloader.data.repo

import com.codecx.videodownloader.data.models.TabModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TabRepository {

    private val _tabs = MutableStateFlow<List<TabModel>>(emptyList())
    val tabs: StateFlow<List<TabModel>> = _tabs.asStateFlow()

    private var nextId = 0L

    init {
        addTab()
    }

    fun addTab(): TabModel {


        val updatedTabs = _tabs.value.map {
            it.copy(isSelected = false)
        }

        val tab = TabModel(
            id = nextId++,
            title = "Home",
            url = "about:blank",
            isSelected = true
        )

        _tabs.value = updatedTabs + tab

        return tab
    }

    fun selectTab(tabId: Long) {

        _tabs.value = _tabs.value.map { tab ->

            tab.copy(
                isSelected = tab.id == tabId
            )
        }
    }

    fun removeTab(tabId: Long) {

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
        _tabs.value = emptyList()
    }

    fun updateTab(
        tabId: Long,
        title: String? = null,
        url: String? = null
    ) {

        _tabs.value = _tabs.value.map { tab ->

            if (tab.id == tabId) {

                tab.copy(
                    title = title ?: tab.title,
                    url = url ?: tab.url
                )

            } else {
                tab
            }
        }
    }

    fun getTab(tabId: Long): TabModel? {
        return _tabs.value.find {
            it.id == tabId
        }
    }
}