package com.codecx.videodownloader.data.repo

import com.codecx.videodownloader.data.dao.BookMarksDao
import com.codecx.videodownloader.data.dao.HistoryDao
import com.codecx.videodownloader.data.models.BookMarkItem
import com.codecx.videodownloader.data.models.BrowserItem
import com.codecx.videodownloader.data.models.HistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryBookRepo(
    private val historyDao: HistoryDao,
    private val bookmarkDao: BookMarksDao
) {
    val historyList: Flow<List<BrowserItem>> = historyDao.getAll().map { list ->
        list.map { BrowserItem(it.id, it.title, it.url, it.timestamp) }
    }

    suspend fun addHistory(title: String, url: String) {
        historyDao.insert(HistoryItem(title = title, url = url))
    }

    suspend fun deleteHistory(id: Int) = historyDao.deleteById(id)
    suspend fun clearHistory() = historyDao.clearAll()

    val bookmarkList: Flow<List<BrowserItem>> = bookmarkDao.getAll().map { list ->
        list.map { BrowserItem(it.id, it.title, it.url, it.timestamp) }
    }

    suspend fun addBookmark(title: String, url: String) {
        bookmarkDao.insert(BookMarkItem(title = title, url = url))
    }

    suspend fun deleteBookmark(id: Int) = bookmarkDao.deleteById(id)
    suspend fun isBookmarked(url: String) = bookmarkDao.isBookmarked(url) > 0
}