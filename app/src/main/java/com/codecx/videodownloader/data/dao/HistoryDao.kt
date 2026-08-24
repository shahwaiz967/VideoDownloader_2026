package com.codecx.videodownloader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.codecx.videodownloader.data.models.HistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(item: HistoryItem)

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<HistoryItem>>

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM history")
    suspend fun clearAll()
}