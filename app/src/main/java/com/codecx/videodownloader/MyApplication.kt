package com.codecx.videodownloader

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.codecx.videodownloader.data.appModule
import com.codecx.videodownloader.data.utils.ThemeManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication(): Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appModule)
            androidContext(this@MyApplication)
        }

        AppCompatDelegate.setDefaultNightMode(
            if (ThemeManager.isDarkMode(this)) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}