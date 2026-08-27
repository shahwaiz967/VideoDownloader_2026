package com.codecx.videodownloader

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.codecx.statussaver.data.di.singleModule
import com.codecx.statussaver.data.di.vmModule
import com.codecx.videodownloader.data.di.appModule
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.ThemeManager
import com.codecx.videodownloader.data.ads.CodecxAd
import com.codecx.videodownloader.data.ads.CodecxAdsConfig
import com.codecx.videodownloader.data.ads.openAd.OpenAdConfig
import com.codecx.videodownloader.data.ads.openAd.OpenApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RemoteConfigHelper.init(this)
        OpenApp(this, true)
        OpenAdConfig.enableResumeAd()
        CoroutineScope(Dispatchers.Main).launch {
            CodecxAd.initAds(CodecxAdsConfig.Builder().build(), this@MyApplication)
        }
        startKoin {
            androidContext(this@MyApplication)
            modules(vmModule, singleModule,appModule)
        }
        AppCompatDelegate.setDefaultNightMode(
            if (ThemeManager.isDarkMode(this)) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}