package com.codecx.videodownloader.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "downloads")
data class DownloadModel(
    @PrimaryKey
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val url: String,
    val localPath: String = "",
    val thumbnailUrl: String = "",
    val fileSize: String = "",
    val platform: String = "",
    val isAudio: Boolean = false,
    val status: DownloadStatus = DownloadStatus.IN_PROGRESS,
    val progress: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

enum class DownloadStatus {
    IN_PROGRESS, COMPLETED, FAILED, PAUSED
}


//data class DownloadApiResponse(
//    @SerializedName("url") val url: String?,
//    @SerializedName("source") val source: String?,
//    @SerializedName("id") val id: String?,
//    @SerializedName("author") val author: String?,
//    @SerializedName("title") val title: String?,
//    @SerializedName("thumbnail") val thumbnail: String?,
//    @SerializedName("duration") val duration: Double?,
//    @SerializedName("statistics") val statistics: Statistics?,
//    @SerializedName("medias") val medias: List<MediaItem>?,
//    @SerializedName("error") val error: Boolean?
//)

@Serializable
data class DownloadApiResponse(

    @SerialName("url")
    val url: String? = null,

    @SerialName("source")
    val source: String? = null,

    @SerialName("id")
    val id: String? = null,

    @SerialName("author")
    val author: String? = null,

    @SerialName("title")
    val title: String? = null,

    @SerialName("thumbnail")
    val thumbnail: String? = null,

    @SerialName("duration")
    val duration: Double? = null,

    @SerialName("statistics")
    val statistics: Statistics? = null,

    @SerialName("medias")
    val medias: List<MediaItem>? = null,

    @SerialName("error")
    val error: Boolean? = null
)

/*data class Statistics(
    @SerializedName("play_count") val playCount: Long?,
    @SerializedName("digg_count") val diggCount: Long?,
    @SerializedName("comment_count") val commentCount: Long?,
    @SerializedName("share_count") val shareCount: Long?,
    @SerializedName("collect_count") val collectCount: Long?,
    @SerializedName("download_count") val downloadCount: Long?
)*/

@Serializable
data class Statistics(

    @SerialName("play_count")
    val playCount: Long? = null,

    @SerialName("digg_count")
    val diggCount: Long? = null,

    @SerialName("comment_count")
    val commentCount: Long? = null,

    @SerialName("share_count")
    val shareCount: Long? = null,

    @SerialName("collect_count")
    val collectCount: Long? = null,

    @SerialName("download_count")
    val downloadCount: Long? = null
)

/*data class MediaItem(
    @SerializedName("url") val url: String?,
    @SerializedName("quality") val quality: String?,
    @SerializedName("extension") val extension: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("data_size") val dataSize: Long?,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?,
    @SerializedName("duration") val duration: Double?,
    @SerializedName("size_short") val size: String?
)*/

@Serializable
data class MediaItem(

    @SerialName("url")
    val url: String? = null,

    @SerialName("quality")
    val quality: String? = null,

    @SerialName("extension")
    val extension: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("data_size")
    val dataSize: Long? = null,

    @SerialName("width")
    val width: Int? = null,

    @SerialName("height")
    val height: Int? = null,

    @SerialName("duration")
    val duration: Double? = null,

    @SerialName("size_short")
    val size: String? = null
) {

    fun isVideo(): Boolean =
        type == "video"

    fun isAudio(): Boolean =
        type == "audio"

    fun formattedSize(): String {

        val bytes = dataSize ?: 0L

        return when {
            bytes >= 1_000_000 ->
                String.format("%.1f MB", bytes / 1_000_000.0)

            bytes >= 1_000 ->
                String.format("%.1f KB", bytes / 1_000.0)

            else ->
                "$bytes B"
        }
    }

    fun resolutionLabel(): String {

        return when (quality) {

            "hd_no_watermark" -> "HD"

            "no_watermark" -> "SD"

            "watermark" -> "Watermark"

            "audio" -> "Audio"

            else -> quality ?: ""
        }
    }
}

