package com.codecx.videodownloader.data

import com.codecx.videodownloader.data.repo.PlayerRepositry
import com.codecx.videodownloader.data.viewmodel.PlayerViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::PlayerRepositry)
    viewModelOf(::PlayerViewModel)
}
