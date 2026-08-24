package com.codecx.videodownloader.data.interfce

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.codecx.videodownloader.data.models.BookMarkItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BookMarksInterface {
    @Insert
    suspend fun insert(item: BookMarkItem)

    @Query("SELECT * FROM bookmark ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<BookMarkItem>>

    @Query("DELETE FROM bookmark")
    suspend fun clearHistory()
}