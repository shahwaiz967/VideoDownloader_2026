package com.codecx.videodownloader.data.models

import com.google.gson.annotations.SerializedName


data class AdItem(
    @SerializedName("id")
    val adId: String = "",
    @SerializedName("isEnable")
    val isAdEnable: Boolean = true,
    @SerializedName("adFormat")
    val adFormat: String = "",
    @SerializedName("adType")
    val adType: Int = 0,
    @SerializedName("adController")
    val adController: Int = 0,
)