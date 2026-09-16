package com.codecx.videodownloader.data.ads.enums

import androidx.annotation.LayoutRes
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.AdItem


enum class EnumAdType {
    BannerNative,MediumNative,MediumMediaNative,LargeNative;

}

@LayoutRes
fun AdItem.getNativeLayout(): Int {
    return when (adType) {
        // 5 = Large Native Bottom Button
        5 -> R.layout.large_native_with_top_media

        // 6 = Large Native with Middle Media
        6 -> R.layout.large_native

        // 7 = Medium Native - Video LEFT / Buttons RIGHT
        7 -> R.layout.medium_native_left_video

        // 8 = Medium Native - Video RIGHT / Buttons LEFT
        8 -> R.layout.medium_media_native

        // 9 = Medium Native without Video - Bottom Button
        9 -> R.layout.medium_native

        // 10 = Medium Native - Video RIGHT / Bottom Buttons
        10 -> R.layout.mediun_native_bottom_button

        // 11 = Small Native - LEFT Icon (Banner size)
        11 -> R.layout.small_native_left_icon

        // 12 = Small Native - RIGHT Icon (Banner size)
        12 -> R.layout.small_native_right_icon

        // 13 = Medium Native without media right side Button
        13 -> R.layout.native_banner

        // Default
        else -> {
            R.layout.native_banner
        }
    }
}

@LayoutRes
fun AdItem.getShimmerLayout(): Int {
    return when (adType) {
        5, 6 -> R.layout.large_native_shimmer
        7, 8, 9, 10 -> R.layout.medium_shimmer
        11, 12 -> R.layout.native_banner_shimmer
        13 -> R.layout.native_banner_shimmer
        else -> {
            R.layout.native_banner
        }
    }
}
