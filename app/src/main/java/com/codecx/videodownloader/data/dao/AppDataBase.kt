package com.codecx.videodownloader.data.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.codecx.videodownloader.data.models.BookMarkItem
import com.codecx.videodownloader.data.models.HistoryItem

@Database(entities = [HistoryItem::class, BookMarkItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookMarksDao
}