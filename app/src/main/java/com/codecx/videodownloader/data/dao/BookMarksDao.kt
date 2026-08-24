package com.codecx.videodownloader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.codecx.videodownloader.data.models.BookMarkItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BookMarksDao {
    @Insert
    suspend fun insert(item: BookMarkItem)

    @Query("SELECT * FROM bookmark ORDER BY timestamp DESC")
    fun getAll(): Flow<List<BookMarkItem>>

    @Query("DELETE FROM bookmark WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM bookmark WHERE url = :url")
    suspend fun isBookmarked(url: String): Int
}