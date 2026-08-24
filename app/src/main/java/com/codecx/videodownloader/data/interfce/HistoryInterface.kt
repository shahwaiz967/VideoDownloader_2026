package com.codecx.videodownloader.data.interfce

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.codecx.videodownloader.data.models.HistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryInterface {
    @Insert
    suspend fun insert(item: HistoryItem)

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}