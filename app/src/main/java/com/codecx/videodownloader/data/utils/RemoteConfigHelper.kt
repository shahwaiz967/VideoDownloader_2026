package com.codecx.videodownloader.data.utils

import android.content.Context
import android.util.Log
import com.codecx.videodownloader.BuildConfig
import com.codecx.videodownloader.data.dataClasses.AdItem
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.codecx.videodownloader.data.dataClasses.AdSize
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
    lateinit var adIds: RemoteAdsConfig

    fun init(application: Context) {
        this@RemoteConfigHelper.application = application
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseApp.initializeApp(application)
            adIds = getDefaultAdIds()
            checkRemoteConfig()
        }
    }

    suspend fun checkRemoteConfig() {
        runCatching {
            val configSettings: FirebaseRemoteConfigSettings =
                FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600)
                    .build()

            mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
            withTimeout(20.seconds) {
                mFirebaseRemoteConfig.fetchAndActivate().await()
            }
        }.onSuccess {
            val json = mFirebaseRemoteConfig.getString("ads_config")
            if (json.isNotEmpty()) {
                adIds = Gson().fromJson(json, RemoteAdsConfig::class.java)
            }
        }.onFailure {
            Log.e("RemoteConfigHelper", "checkRemoteConfig: ${it.message}")
        }

    }

    private fun getId(id: Int): String {
        return application.getString(id)
    }

    fun getDefaultAdIds(): RemoteAdsConfig {
        return RemoteAdsConfig(
            appOpen = AdItem(BuildConfig.open_ad_id),
            mainBanner = AdItem(
                BuildConfig.collapsible_id,
                true,
                COLLAPSIBLE_AD_TYPE
            ),
            settingBanner = AdItem(
                BuildConfig.collapsible_id,
                true,
                COLLAPSIBLE_AD_TYPE
            ),
            splashInter = AdItem(
                adId = BuildConfig.interstitial_id,
                adFormat = INTER_AD_TYPE,
                isAdEnable = true
            ),
            premiumInter = AdItem(
                adId = BuildConfig.interstitial_id,
                adFormat = INTER_AD_TYPE,
                isAdEnable = true
            ),
            societyBanner = AdItem(
                BuildConfig.collapsible_id,
                true,
                COLLAPSIBLE_AD_TYPE
            ),
            historyBanner = AdItem(
                BuildConfig.collapsible_id,
                true,
                COLLAPSIBLE_AD_TYPE
            ),
            bookmarkBanner = AdItem(
                BuildConfig.collapsible_id,
                true,
                COLLAPSIBLE_AD_TYPE
            ),
            languageBanner = AdItem(
                BuildConfig.banner_id,
                true,
                BANNER_AD_TYPE,
                AdSize.LARGE
            ),
            languageOnBoardingBanner = AdItem(
                BuildConfig.banner_id,
                true,
                BANNER_AD_TYPE,
                AdSize.LARGE
            ),
            backPressInter = AdItem(
                adId = BuildConfig.interstitial_id,
                adFormat = INTER_AD_TYPE,
                isAdEnable = true
            )
        )
    }
}