package com.codecx.videodownloader.data.di

import androidx.room.Room
import com.codecx.videodownloader.data.dao.AppDatabase
import com.codecx.videodownloader.data.repo.DownloadRepo
import com.codecx.videodownloader.data.repo.HistoryBookRepo
import com.codecx.videodownloader.data.repo.PlayerRepositry
import com.codecx.videodownloader.data.repo.TabRepository
import com.codecx.videodownloader.data.repo.VideoPlayerRepo
import com.codecx.videodownloader.data.utils.AppConstants
import com.codecx.videodownloader.data.utils.ReelApiService
import com.codecx.videodownloader.features.ui.viewmodel.DownloadViewModel
import com.codecx.videodownloader.features.ui.viewmodel.HistoryBookViewM
import com.codecx.videodownloader.features.ui.viewmodel.PlayerViewModel
import com.codecx.videodownloader.features.ui.viewmodel.TabViewModel
import com.codecx.videodownloader.features.ui.viewmodel.VideoPlayerViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
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
    singleOf(::ReelApiService)


    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "history_bookmark_database"
        ).build()
    }

    // DAOs
    single { get<AppDatabase>().historyDao() }
    single { get<AppDatabase>().bookmarkDao() }

    // Repositories
    factoryOf(::PlayerRepositry)
    singleOf(::DownloadRepo)
    singleOf(::TabRepository)
    factoryOf(::VideoPlayerRepo)
    factoryOf(::HistoryBookRepo)

    // ViewModels
    viewModel { DownloadViewModel(get(), androidContext()) }
    viewModelOf(::TabViewModel)
    viewModelOf(::VideoPlayerViewModel)
    viewModelOf(::PlayerViewModel)
    viewModelOf(::HistoryBookViewM)


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

        install(DefaultRequest) {
            header("x-access-token", AppConstants.REEL_ACCESS_TOKEN)
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
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

