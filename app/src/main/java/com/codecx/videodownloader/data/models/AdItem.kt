package com.codecx.videodownloader.data.models

import com.google.gson.annotations.SerializedName
import com.codecx.videodownloader.data.ads.enums.EnumAdType

object AdSize {
    const val MEDIUM = "medium"
    const val LARGE = "large"
    const val BANNER = "banner"
    const val MEDIUM_MEDIA = "mediumMedia"
}

data class AdItem(
    @SerializedName("id")
    val adId: String = "",
    @SerializedName("isEnable")
    val isAdEnable: Boolean = true,
    @SerializedName("adFormat")
    val adFormat: String = "",
    @SerializedName("adSize")
    val adSize: String = "",
    @SerializedName("ctrColor")
    val ctrColor: String = "#00704C",
    @SerializedName("adTagColor")
    val adTagColor: String = "#FB923C",
    @SerializedName("adType")
    val adType: Int = 0,
) {


    fun toAdSize(default: EnumAdType = EnumAdType.MediumNative): EnumAdType {
        return when (adSize) {
            AdSize.MEDIUM -> EnumAdType.MediumNative
            AdSize.BANNER -> EnumAdType.BannerNative
            AdSize.LARGE -> EnumAdType.LargeNative
            AdSize.MEDIUM_MEDIA -> EnumAdType.MediumMediaNative
            else -> default
        }
    }
}