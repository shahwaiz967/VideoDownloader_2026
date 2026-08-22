package com.codecx.videodownloader.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DownloadRequest(
    val url: String
)