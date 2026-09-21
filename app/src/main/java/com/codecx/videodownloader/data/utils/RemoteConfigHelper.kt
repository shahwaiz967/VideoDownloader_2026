package com.codecx.videodownloader.data.utils

import android.content.Context
import android.util.Log
import com.codecx.videodownloader.BuildConfig
import com.codecx.videodownloader.data.models.AdItem
import com.codecx.videodownloader.data.models.RemoteAdsConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
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
        Log.i("RemoteConfigHelper", "init: Ad IDs initialized\n\n${Gson().toJson(adIds)}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseApp.initializeApp(application)
            } catch (e: Exception) {
                Log.e(TAG, "Firebase initialization failed: ${e.message}")
            }
        }
    }

    suspend fun checkRemoteConfig(onConfigLoaded: () -> Unit) {
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
            val json = if (BuildConfig.DEBUG) mFirebaseRemoteConfig.getString("ads_config_test")
            else mFirebaseRemoteConfig.getString("ads_config")

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
            onConfigLoaded()
        }.onFailure {
            Log.e(TAG, "checkRemoteConfig: Fetch Failure: ${it.message}")
            onConfigLoaded()
        }
    }

    private fun getId(id: Int): String {
        return application.getString(id)
    }

    fun getDefaultAdIds(): RemoteAdsConfig {
        return RemoteAdsConfig(
            resumeAppOpen = AdItem(BuildConfig.resume_open_id, true, OPEN_AD_TYPE),
            splashTopNative = AdItem(BuildConfig.splash_native_id, true, NATIVE_AD_TYPE),
            splashBottomNative = AdItem(BuildConfig.splash_native_id, true, NATIVE_AD_TYPE),
            splashFullScreenAd = AdItem(BuildConfig.splash_open_id, true, OPEN_AD_TYPE),

            languageTopNative = AdItem(BuildConfig.lang_native_id, true, NATIVE_AD_TYPE),
            languageBottomNative = AdItem(BuildConfig.lang_native_id, true, NATIVE_AD_TYPE),

            onboardingTopNative = AdItem(BuildConfig.onboarding_native_id, true, NATIVE_AD_TYPE),
            onboardingBottomNative = AdItem(BuildConfig.onboarding_native_id, true, NATIVE_AD_TYPE),
            onboardingFinishInter = AdItem(BuildConfig.onboarding_finish_inter_id, true, INTER_AD_TYPE),

            homeInter = AdItem(BuildConfig.home_inter_id, true, INTER_AD_TYPE),
            homeTopAd = AdItem(BuildConfig.home_native_id, true, NATIVE_AD_TYPE),
            homeMidAd = AdItem(BuildConfig.home_native_id, true, NATIVE_AD_TYPE),
            homeBottomAd = AdItem(BuildConfig.home_native_id, true, NATIVE_AD_TYPE),

            backPressInter = AdItem(BuildConfig.home_inter_id, true, INTER_AD_TYPE),

            premiumInter = AdItem(BuildConfig.home_inter_id, false, INTER_AD_TYPE),
            societyBanner = AdItem(BuildConfig.banner_id, false, COLLAPSIBLE_AD_TYPE),
            historyBanner = AdItem(BuildConfig.banner_id, false, COLLAPSIBLE_AD_TYPE),
            bookmarkBanner = AdItem(BuildConfig.banner_id, false, COLLAPSIBLE_AD_TYPE),
            settingBanner = AdItem(BuildConfig.banner_id, true, COLLAPSIBLE_AD_TYPE),
            adFrequency = 4, homeFirstClickInter = true
        )
    }
}