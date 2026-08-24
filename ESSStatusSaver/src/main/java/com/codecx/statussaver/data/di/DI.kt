package com.codecx.statussaver.data.di

import com.codecx.statussaver.core.utils.BillingHelperNew
import com.codecx.statussaver.data.repos.UserPreferences
import com.codecx.statussaver.data.repos.appRepo.AppRepoImp
import com.codecx.statussaver.features.composes.mainBoarding.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val vmModule = module {


    viewModel {
        HomeViewModel(
            appRepoImp = AppRepoImp(
                appContext = androidContext(),
                userPreferences = get()
            )
        )
    }
}

val factoryModule = module {

}

val singleModule = module {
    single {
        UserPreferences(context = androidContext())
    }
    single {
        BillingHelperNew(context = androidContext())
    }
}