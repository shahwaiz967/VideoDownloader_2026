package com.codecx.videodownloader.data.models

import com.google.gson.annotations.SerializedName

data class RemoteAdsConfig(
    // App Open
    @SerializedName("resume_app_open")
    val resumeAppOpen: AdItem = AdItem(),

    // Splash
    @SerializedName("splash_top_native")
    val splashTopNative: AdItem = AdItem(),
    @SerializedName("splash_bottom_native")
    val splashBottomNative: AdItem = AdItem(),
    @SerializedName("splash_fullScreen_ad")
    val splashFullScreenAd: AdItem = AdItem(),

    // Language
    @SerializedName("language_top_native")
    val languageTopNative: AdItem = AdItem(),
    @SerializedName("language_bottom_native")
    val languageBottomNative: AdItem = AdItem(),


    // Onboarding
    @SerializedName("onboarding_top_native")
    val onboardingTopNative: AdItem = AdItem(),
    @SerializedName("onboarding_bottom_native")
    val onboardingBottomNative: AdItem = AdItem(),
    @SerializedName("onboarding_finish_inter")
    val onboardingFinishInter: AdItem = AdItem(),

    // Home
    @SerializedName("home_inter")
    val homeInter: AdItem = AdItem(),
    @SerializedName("home_top_ad")
    val homeTopAd: AdItem = AdItem(),
    @SerializedName("home_mid_ad")
    val homeMidAd: AdItem = AdItem(),
    @SerializedName("home_bottom_ad")
    val homeBottomAd: AdItem = AdItem(),
    @SerializedName("exit_dialog_native")
    val exitDialogNative: AdItem = AdItem(),



    // Common/Other
    @SerializedName("back_press_inter")
    val backPressInter: AdItem = AdItem(),
    @SerializedName("premium_inter")
    val premiumInter: AdItem = AdItem(),
    @SerializedName("society_banner")
    val societyBanner: AdItem = AdItem(),
    @SerializedName("society_native_top")
    val societyNativeTop: AdItem = AdItem(),
    @SerializedName("history_banner")
    val historyBanner: AdItem = AdItem(),
    @SerializedName("bookmark_banner")
    val bookmarkBanner: AdItem = AdItem(),
    @SerializedName("setting_banner")
    val settingBanner: AdItem = AdItem(),
    @SerializedName("video_player_native")
    val videoPlayerNative: AdItem = AdItem(),

    // Frequency
    @SerializedName("ad_frequency")
    val adFrequency: Int = 4,
    @SerializedName("homeFirstClickInter")
    val homeFirstClickInter: Boolean = true,

    //ReelFrequency
    @SerializedName("reel_ad_frequency")
    val reelAdFrequency: Int = 5,
    @SerializedName("reel_full_Native")
    val reelFullNative: AdItem = AdItem(),
    @SerializedName("reel_bottom_native")
    val reelBottomNative: AdItem = AdItem(),


    //Player
    @SerializedName("player_inter")
    val playerInter: AdItem = AdItem(),
    @SerializedName("player_native_frequency")
    val playerNativeFrequency: Int = 10,
    @SerializedName("player_native_ad")
    val playerNativeAd: AdItem = AdItem(),
)
