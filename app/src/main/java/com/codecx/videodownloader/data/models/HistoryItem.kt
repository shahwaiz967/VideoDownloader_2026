package com.codecx.videodownloader.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmark")
data class BookMarkItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class BrowserItem(
    val id: Int,
    val title: String,
    val url: String,
    val timestamp: Long
)
