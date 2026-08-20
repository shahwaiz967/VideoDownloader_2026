package com.codecx.videodownloader

import android.app.Application
import com.codecx.videodownloader.data.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication(): Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appModule)
            androidContext(this@MyApplication)
        }
    }
}