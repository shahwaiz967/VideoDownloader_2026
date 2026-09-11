package com.codecx.videodownloader.data.dataClasses

import com.google.gson.annotations.SerializedName

data class RemoteAdsConfig(
    @SerializedName("appOpen")
    val appOpen: AdItem = AdItem(),

    @SerializedName("main_banner")
    val mainBanner: AdItem = AdItem(),
    @SerializedName("setting_banner")
    val settingBanner: AdItem = AdItem(),
    @SerializedName("splash_inter")
    val splashInter: AdItem = AdItem(),
    @SerializedName("premium_inter")
    val premiumInter: AdItem = AdItem(),
    @SerializedName("society_banner")
    val societyBanner: AdItem = AdItem(),
   @SerializedName("history_banner")
   val historyBanner: AdItem = AdItem(),
   @SerializedName("bookmark_banner")
   val bookmarkBanner: AdItem = AdItem(),
   @SerializedName("language_banner")
   val languageBanner: AdItem = AdItem(),
   @SerializedName("languageOnBoarding_banner")
   val languageOnBoardingBanner: AdItem = AdItem(),
    @SerializedName("backPress_inter")
    val backPressInter: AdItem = AdItem(),




/*    @SerializedName("main_inter")
    val mainInter: AdItem = AdItem(),
    @SerializedName("splash_noti_inter")
    val splashNotiInter: AdItem = AdItem(),
    @SerializedName("splash_banner")
    val splashBanner: AdItem = AdItem(),
    @SerializedName("wifi_user_native")
    val wifiUserNative: AdItem = AdItem(),
    @SerializedName("test_mobile_native")
    val testMobileNative: AdItem = AdItem(),

    @SerializedName("test_ear_native")
    val testEarNative: AdItem = AdItem(),
    @SerializedName("test_loud_speaker_native")
    val testLoudSpeakerNative: AdItem = AdItem(),
    @SerializedName("test_micro_phone_banner")
    val testMicroPhoneBanner: AdItem = AdItem(),
    @SerializedName("test_flash_banner")
    val testFlashBanner: AdItem = AdItem(),
    @SerializedName("test_vibration_banner")
    val testVibrationBanner: AdItem = AdItem(),
    @SerializedName("settings_native")
    val settingsNative: AdItem = AdItem(),
    @SerializedName("language_native")
    val languageNative: AdItem = AdItem(),
    @SerializedName("language_noti_native")
    val languageNotiNative: AdItem = AdItem(),

    @SerializedName("languageDone_onBoarding_inter")
    val languageDoneInterOnBoarding: AdItem = AdItem(),
    @SerializedName("languageDone2_onBoarding_inter")
    val languageDone2InterOnBoarding: AdItem = AdItem(),
    @SerializedName("getStart_ad")
    val getStartAd: AdItem = AdItem(),
    @SerializedName("intro1_ad")
    val intro1Ad: AdItem = AdItem(),
    @SerializedName("intro2_ad")
    val intro2Ad: AdItem = AdItem(),
    @SerializedName("intro3_ad")
    val intro3Ad: AdItem = AdItem(),
    @SerializedName("intro4_ad")
    val intro4Ad: AdItem = AdItem(),
    @SerializedName("intro_fullScreen_native1")
    val introFullScreenNative1: AdItem = AdItem(),
    @SerializedName("intro_fullScreen_native2")
    val introFullScreenNative2: AdItem = AdItem(),
    @SerializedName("intro_noti_fullScreen_native1")
    val introNotiFullScreenNative1: AdItem = AdItem(),
    @SerializedName("intro_noti_fullScreen_native2")
    val introNotiFullScreenNative2: AdItem = AdItem(),

    @SerializedName("speed_native")
    val speedNative: AdItem = AdItem(),
    @SerializedName("history_native")
    val historyNative: AdItem = AdItem(),
    @SerializedName("exit_native")
    val exitNative: AdItem = AdItem(),
    @SerializedName("userAppsList_native")
    val userAppsListNative: AdItem = AdItem(),
    @SerializedName("systemAppsList_native")
    val systemAppsListNative: AdItem = AdItem(),
    @SerializedName("appInfo_native")
    val appInfoNative: AdItem = AdItem(),
    @SerializedName("sensor_native")
    val sensorNative: AdItem = AdItem(),
    @SerializedName("updateAppDetail_native")
    val updateAppDetailNative: AdItem = AdItem(),
    @SerializedName("scanner_native")
    val scannerNative: AdItem = AdItem(),
    @SerializedName("mobileUsage_native")
    val mobileUsageNative: AdItem = AdItem(),
    @SerializedName("appDataUsage_native")
    val appDataUsageNative: AdItem = AdItem(),
    @SerializedName("appUsage_native")
    val appUsageNative: AdItem = AdItem(),
    @SerializedName("manageAppList_native")
    val manageAppListNative: AdItem = AdItem(),
    @SerializedName("permissions_native")
    val permissionsNative: AdItem = AdItem(),
    @SerializedName("extraScanning_native")
    val extraScanningNative: AdItem = AdItem(),
    @SerializedName("extraFiles_native")
    val extraFilesNative: AdItem = AdItem(),
    @SerializedName("deviceInfo_native")
    val deviceInfoNative: AdItem = AdItem(),
    @SerializedName("deviceInfo_ad")
    val deviceInfoAd: AdItem = AdItem(),
    @SerializedName("systemUpdate_ad")
    val systemUpdateAd: AdItem = AdItem(),
    @SerializedName("operatingSystem_ad")
    val operatingSystemAd: AdItem = AdItem(),
    @SerializedName("root_ad")
    val rootAd: AdItem = AdItem(),
    @SerializedName("memoryAd")
    val memoryAd: AdItem = AdItem(),
    @SerializedName("ram_ad")
    val ramAd: AdItem = AdItem(),
    @SerializedName("rom_ad")
    val romAd: AdItem = AdItem(),
    @SerializedName("battery_ad")
    val batteryAd: AdItem = AdItem(),
    @SerializedName("displayAd")
    val displayAd: AdItem = AdItem(),
    @SerializedName("cpuAd")
    val cpuAd: AdItem = AdItem(),
    @SerializedName("test_mobile_screen_ad")
    val testMobileScreenAd: AdItem = AdItem(),
    @SerializedName("sensorInfoAd")
    val sensorInfoAd: AdItem = AdItem(),
    @SerializedName("displayInfo_native")
    val displayInfoNative: AdItem = AdItem(),
    @SerializedName("processorInfo_native")
    val processorInfoNative: AdItem = AdItem(),
    @SerializedName("systemInfo_native")
    val systemInfoNative: AdItem = AdItem(),
    @SerializedName("unInstallApps_native")
    val unInstallAppsNative: AdItem = AdItem(),
    @SerializedName("duplicatePhotos_native")
    val duplicatePhotosNative: AdItem = AdItem(),



    @SerializedName("deviceInfo_inter")
    val deviceInfoInter: AdItem = AdItem(),
    @SerializedName("userApps_inter")
    val userAppsInter: AdItem = AdItem(),
    @SerializedName("wifiUsers_inter")
    val wifiUsersInter: AdItem = AdItem(),
    @SerializedName("internetSpeed_inter")
    val internetSpeedInter: AdItem = AdItem(),
    @SerializedName("mobileUsage_inter")
    val mobileUsageInter: AdItem = AdItem(),
    @SerializedName("extraFiles_inter")
    val extraFilesInter: AdItem = AdItem(),
    @SerializedName("manageApps_inter")
    val manageAppsInter: AdItem = AdItem(),
    @SerializedName("sensors_inter")
    val sensorsInter: AdItem = AdItem(),
    @SerializedName("testMobile_inter")
    val testMobileInter: AdItem = AdItem(),
    @SerializedName("appDataUsage_inter")
    val appDataUsageInter: AdItem = AdItem(),
    @SerializedName("duplicatePhotos_inter")
    val duplicatePhotosInter: AdItem = AdItem(),
    @SerializedName("systemUpdates_inter")
    val systemUpdatesInter: AdItem = AdItem(),
    @SerializedName("unInstallApps_inter")
    val unInstallAppsInter: AdItem = AdItem(),
    @SerializedName("historyDetails_inter")
    val historyDetailsInter: AdItem = AdItem(),
    @SerializedName("premiumCancelNoti_inter")
    val premiumCancelNotiInter: AdItem = AdItem(),
    @SerializedName("premiumCancelFirst_inter")
    val premiumCancelFirstInter: AdItem = AdItem(),
    @SerializedName("premiumCancelSecond_inter")
    val premiumCancelSecondInter: AdItem = AdItem(),
    @SerializedName("premiumOpenCancelInter")
    val premiumOpenCancelInter: AdItem = AdItem(),
    @SerializedName("testResultDoneAd")
    val testResultDoneAd: AdItem = AdItem(),
    @SerializedName("testResultCancelAd")
    val testResultCancelAd: AdItem = AdItem(),
    @SerializedName("testResultRetryAd")
    val testResultRetryAd: AdItem = AdItem(),
    @SerializedName("testActionNoAd")
    val testActionNoAd: AdItem = AdItem(),
    @SerializedName("testActionYesAd")
    val testActionYesAd: AdItem = AdItem(),

    @SerializedName("deviceInfoBackPress")
    val deviceInfoBackPress: AdItem = AdItem(),
    @SerializedName("testMobileBackPress")
    val testMobileBackPress: AdItem = AdItem(),
    @SerializedName("sensorBackPress")
    val sensorBackPress: AdItem = AdItem(),
    @SerializedName("appDataBackPress")
    val appDataBackPress: AdItem = AdItem(),
    @SerializedName("mobileUsageBackPress")
    val mobileUsageBackPress: AdItem = AdItem()*/

    )