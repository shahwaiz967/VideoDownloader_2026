package com.codecx.videodownloader.data.utils

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.codecx.videodownloader.data.dataClasses.RemoteAdsConfig
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
    const val COLLAPSABLE_AD_TYPE = "collaps"

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

        }.onFailure {
            Log.e("RemoteConfigHelper", "checkRemoteConfig: ${it.message}")
        }

    }

    private fun getId(id: Int): String {
        return application.getString(id)
    }

}