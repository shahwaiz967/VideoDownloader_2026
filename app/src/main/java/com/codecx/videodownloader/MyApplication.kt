package com.codecx.videodownloader

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.codecx.statussaver.data.di.factoryModule
import com.codecx.statussaver.data.di.singleModule
import com.codecx.statussaver.data.di.vmModule
import com.codecx.videodownloader.data.di.appModule
import com.codecx.videodownloader.data.utils.ThemeManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(vmModule, factoryModule, singleModule,appModule)
        }
        AppCompatDelegate.setDefaultNightMode(
            if (ThemeManager.isDarkMode(this)) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}