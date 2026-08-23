package com.codecx.videodownloader.data.di

import com.codecx.videodownloader.data.repo.DownloadRepo
import com.codecx.videodownloader.data.repo.PlayerRepositry
import com.codecx.videodownloader.data.repo.TabRepository
import com.codecx.videodownloader.data.repo.VideoPlayerRepo
import com.codecx.videodownloader.features.viewmodel.DownloadViewModel
import com.codecx.videodownloader.features.viewmodel.PlayerViewModel
import com.codecx.videodownloader.features.viewmodel.TabViewModel
import com.codecx.videodownloader.features.viewmodel.VideoPlayerViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val appModule = module {

    // API
    singleOf(::createWebClient)
    singleOf(::DownloadRepo)

    // Repositories
    factoryOf(::PlayerRepositry)
    singleOf(::TabRepository)
    factoryOf(::VideoPlayerRepo)

    // ViewModels
    viewModel { DownloadViewModel(get(), androidContext()) }
    viewModelOf(::TabViewModel)
    viewModelOf(::VideoPlayerViewModel)
    viewModelOf(::PlayerViewModel)


}

private fun createWebClient(): HttpClient {
    return HttpClient(OkHttp) {

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }

        install(Logging) {
            level = LogLevel.BODY
        }

        engine {
            config {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
            }
        }
    }
}