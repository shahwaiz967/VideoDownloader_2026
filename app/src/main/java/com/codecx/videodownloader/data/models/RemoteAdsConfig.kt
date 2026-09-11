package com.codecx.videodownloader.data.dataClasses

import com.codecx.videodownloader.data.models.AdItem
import com.google.gson.annotations.SerializedName

data class RemoteAdsConfig(
    // App Open
    @SerializedName("splash_app_open")
    val splashAppOpen: AdItem = AdItem(),
    @SerializedName("resume_app_open")
    val resumeAppOpen: AdItem = AdItem(),

    // Splash
    @SerializedName("splash_top_native")
    val splashTopNative: AdItem = AdItem(),
    @SerializedName("splash_bottom_native")
    val splashBottomNative: AdItem = AdItem(),
    @SerializedName("splash_inter")
    val splashInter: AdItem = AdItem(),

    // Language
    @SerializedName("language_top_native")
    val languageTopNative: AdItem = AdItem(),
    @SerializedName("language_bottom_native")
    val languageBottomNative: AdItem = AdItem(),
    @SerializedName("language_banner")
    val languageBanner: AdItem = AdItem(),

    // Onboarding
    @SerializedName("onboarding_top_native")
    val onboardingTopNative: AdItem = AdItem(),
    @SerializedName("onboarding_bottom_native")
    val onboardingBottomNative: AdItem = AdItem(),
    @SerializedName("onboarding_finish_inter")
    val onboardingFinishInter: AdItem = AdItem(),

    // Home
    @SerializedName("home_first_click_inter")
    val homeFirstClickInter: AdItem = AdItem(),
    @SerializedName("home_top_banner")
    val homeTopBanner: AdItem = AdItem(),
    @SerializedName("home_top_native")
    val homeTopNative: AdItem = AdItem(),
    @SerializedName("home_mid_native")
    val homeMidNative: AdItem = AdItem(),
    @SerializedName("home_bottom_banner")
    val homeBottomBanner: AdItem = AdItem(),
    @SerializedName("home_bottom_native")
    val homeBottomNative: AdItem = AdItem(),

    // Common/Other
    @SerializedName("clean_finish_inter")
    val cleanFinishInter: AdItem = AdItem(),
    @SerializedName("activity_finish_inter")
    val activityFinishInter: AdItem = AdItem(),
    @SerializedName("back_press_inter")
    val backPressInter: AdItem = AdItem(),
    @SerializedName("premium_inter")
    val premiumInter: AdItem = AdItem(),
    @SerializedName("society_banner")
    val societyBanner: AdItem = AdItem(),
    @SerializedName("history_banner")
    val historyBanner: AdItem = AdItem(),
    @SerializedName("bookmark_banner")
    val bookmarkBanner: AdItem = AdItem(),
    @SerializedName("main_banner")
    val mainBanner: AdItem = AdItem(),
    @SerializedName("setting_banner")
    val settingBanner: AdItem = AdItem(),
    
    // Frequency
    @SerializedName("ad_frequency")
    val adFrequency: Int = 4
)
