package com.codecx.videodownloader.data.ads.openAd

import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd

object OpenAdConfig {
    private var appOpenAd: AppOpenAd? = null
    private var isOpenAdAllowed: Boolean = false
    var isOpenAdLoading: Boolean = false
    var isOpenAdShowing: Boolean = false
    var isOpenAdStop: Boolean = false
    fun enableResumeAd() {
        isOpenAdAllowed = true
    }

    fun openActionDisable() {
        isOpenAdStop = true
    }

    fun disableResumeAd() {
        isOpenAdAllowed = false
    }

    fun isAdEnable() = isOpenAdAllowed





    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }

}