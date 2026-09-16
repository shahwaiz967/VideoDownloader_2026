package com.codecx.videodownloader.data.models

import com.google.gson.annotations.SerializedName
import com.codecx.videodownloader.data.ads.enums.EnumAdType


data class AdItem(
    @SerializedName("id")
    val adId: String = "",
    @SerializedName("isEnable")
    val isAdEnable: Boolean = true,
    @SerializedName("adFormat")
    val adFormat: String = "",
    @SerializedName("adType")
    val adType: Int = 0,
)