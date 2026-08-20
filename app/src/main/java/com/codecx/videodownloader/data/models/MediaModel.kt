package com.codecx.videodownloader.data.models

data class MediaModel(
    val id: Long,
    val title: String,
    val path: String,
    val duration: Long,
    val size: Long,
    val dateAdded: Long,
    val platform: String = "All",
    val isAudio: Boolean = false
)
