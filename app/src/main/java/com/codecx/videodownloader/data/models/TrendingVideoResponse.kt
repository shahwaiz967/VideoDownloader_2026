package com.codecx.videodownloader.data.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrendingVideoResponse(
    @SerialName("success") val success: Int? = null,
     @SerialName("data")    val data:    List<TrendingVideo>? = null
)

@Serializable
data class TrendingVideo(
     @SerialName("id")         val id:        Int?    = null,
     @SerialName("url")        val url:       String? = null,
     @SerialName("name")       val name:      String? = null,
     @SerialName("thumbnail")  val thumbnail: String? = null,
     @SerialName("category")   val category:  String? = null,
     @SerialName("likes")      val likes:     Int?    = null,
     @SerialName("downloads")  val downloads: Int?    = null,
     @SerialName("class")      val videoClass:String? = null,
     @SerialName("created_at") val createdAt: String? = null,
     @SerialName("updated_at") val updatedAt: String? = null
)

sealed class ReelItem {
    data class VideoItem(
        val id:         Int    = 0,
        val url:        String = "",
        val name:       String = "",
        val thumbnail:  String = "",
        val category:   String = "",
        val likes:      Int    = 0,
        val downloads:  Int    = 0,
        val videoClass: String = "",
        val createdAt:  String = "",
        val updatedAt:  String = ""
    ) : ReelItem()
}