package com.codecx.videodownloader.data.utils

import android.content.Context
import android.util.Log
import com.codecx.videodownloader.BuildConfig
import com.codecx.videodownloader.data.models.AdItem
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.codecx.videodownloader.data.models.AdSize
import com.codecx.videodownloader.data.dataClasses.RemoteAdsConfig
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

object RemoteConfigHelper {
    const val NATIVE_AD_TYPE = "native"
    const val INTER_AD_TYPE = "inter"
    const val OPEN_AD_TYPE = "open"
    const val BANNER_AD_TYPE = "banner"
    const val COLLAPSIBLE_AD_TYPE = "collapsible"



    val mFirebaseRemoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }

    val TAG = "RemoteConfigHelper"

    private lateinit var application: Context
    var adIds: RemoteAdsConfig = RemoteAdsConfig()

    fun init(application: Context) {
        this@RemoteConfigHelper.application = application
        adIds = getDefaultAdIds()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseApp.initializeApp(application)
            } catch (e: Exception) {
                Log.e(TAG, "Firebase initialization failed: ${e.message}")
            }
            checkRemoteConfig()
        }
    }

    suspend fun checkRemoteConfig() {
        runCatching {
            val configSettings: FirebaseRemoteConfigSettings =
                FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 2600)
                    .build()

            mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
            withTimeout(20.seconds) {
                mFirebaseRemoteConfig.fetchAndActivate().await()
            }
        }.onSuccess {
            val json = mFirebaseRemoteConfig.getString("ads_config")
            Log.d(TAG, "checkRemoteConfig: Fetched JSON: $json")
            if (json.isNotEmpty()) {
                try {
                    adIds = Gson().fromJson(json, RemoteAdsConfig::class.java)
                    Log.d(TAG, "checkRemoteConfig: Ad IDs updated successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "checkRemoteConfig: JSON Parsing Error: ${e.message}")
                }
            } else {
                Log.w(TAG, "checkRemoteConfig: 'ads_config' is empty in Remote Config")
            }
        }.onFailure {
            Log.e(TAG, "checkRemoteConfig: Fetch Failure: ${it.message}")
        }
    }

    private fun getId(id: Int): String {
        return application.getString(id)
    }

    fun getDefaultAdIds(): RemoteAdsConfig {
        return RemoteAdsConfig(
            splashAppOpen = AdItem(BuildConfig.open_ad_id, true, OPEN_AD_TYPE),
            resumeAppOpen = AdItem(BuildConfig.open_ad_id, true, OPEN_AD_TYPE),
            splashTopNative = AdItem(BuildConfig.native_id, true, NATIVE_AD_TYPE),
            splashBottomNative = AdItem(BuildConfig.native_id, true, NATIVE_AD_TYPE),
            splashInter = AdItem(BuildConfig.interstitial_id, true, INTER_AD_TYPE),
            
            languageTopNative = AdItem(BuildConfig.native_id, true, NATIVE_AD_TYPE),
            languageBottomNative = AdItem(BuildConfig.native_id, true, NATIVE_AD_TYPE),
            languageBanner = AdItem(BuildConfig.banner_id, true, BANNER_AD_TYPE, AdSize.LARGE),
            
            onboardingTopNative = AdItem(BuildConfig.native_id, true, NATIVE_AD_TYPE),
            onboardingBottomNative = AdItem(BuildConfig.native_id, true, NATIVE_AD_TYPE),
            onboardingFinishInter = AdItem(BuildConfig.interstitial_id, true, INTER_AD_TYPE),
            
            homeFirstClickInter = AdItem(BuildConfig.interstitial_id, true, INTER_AD_TYPE),
            homeTopBanner = AdItem(BuildConfig.banner_id, true, BANNER_AD_TYPE),
            homeTopNative = AdItem(BuildConfig.native_id, true, NATIVE_AD_TYPE),
            homeMidNative = AdItem(BuildConfig.native_id, true, NATIVE_AD_TYPE),
            homeBottomBanner = AdItem(BuildConfig.banner_id, true, BANNER_AD_TYPE),
            homeBottomNative = AdItem(BuildConfig.native_id, true, NATIVE_AD_TYPE),
            
            cleanFinishInter = AdItem(BuildConfig.interstitial_id, true, INTER_AD_TYPE),
            activityFinishInter = AdItem(BuildConfig.interstitial_id, true, INTER_AD_TYPE),
            backPressInter = AdItem(BuildConfig.interstitial_id, true, INTER_AD_TYPE),
            
            premiumInter = AdItem(BuildConfig.interstitial_id, false, INTER_AD_TYPE),
            societyBanner = AdItem(BuildConfig.collapsible_id, false, COLLAPSIBLE_AD_TYPE),
            historyBanner = AdItem(BuildConfig.collapsible_id, false, COLLAPSIBLE_AD_TYPE),
            bookmarkBanner = AdItem(BuildConfig.collapsible_id, false, COLLAPSIBLE_AD_TYPE),
            mainBanner = AdItem(BuildConfig.collapsible_id, true, COLLAPSIBLE_AD_TYPE),
            settingBanner = AdItem(BuildConfig.collapsible_id, true, COLLAPSIBLE_AD_TYPE),
            adFrequency = 4
        )
    }
}