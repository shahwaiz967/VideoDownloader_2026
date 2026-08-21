package com.codecx.videodownloader.data

import com.codecx.videodownloader.data.repo.PlayerRepositry
import com.codecx.videodownloader.data.repo.VideoPlayerRepo
import com.codecx.videodownloader.data.viewmodel.PlayerViewModel
import com.codecx.videodownloader.data.viewmodel.VideoPlayerViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    singleOf(::PlayerRepositry)
    viewModelOf(::PlayerViewModel)


    viewModelOf(::VideoPlayerViewModel)
    singleOf(::VideoPlayerRepo)


   /* single {
        PlayerRepositry()
        VideoPlayerRepo()
    }
    viewModel {
        PlayerViewModel(get())
        VideoPlayerViewModel(get())
    }*/
}
