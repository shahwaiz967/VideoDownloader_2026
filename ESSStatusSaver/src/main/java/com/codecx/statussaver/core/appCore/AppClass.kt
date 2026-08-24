package com.codecx.statussaver.core.appCore

import android.app.Application
import com.codecx.statussaver.BuildConfig
import com.codecx.statussaver.core.utils.BillingHelperNew
import com.codecx.statussaver.data.di.factoryModule
import com.codecx.statussaver.data.di.singleModule
import com.codecx.statussaver.data.di.vmModule
import com.codecx.statussaver.data.repos.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

abstract class AppClass : Application() {
    private val userPreferences : UserPreferences by inject()
    private val billingHelperNew : BillingHelperNew by inject()
    override fun onCreate() {
        super.onCreate()
        startKoin {
            logger(AndroidLogger(Level.INFO))
            androidContext(this@AppClass)
            modules(vmModule, factoryModule, singleModule)
        }

        CoroutineScope(Dispatchers.IO).launch {
            supervisorScope {
                launch {
                    billingHelperNew.purchaseUpdateEventListener.collectLatest { event->
                        when(event){
                            is com.codecx.statussaver.core.utils.PurchaseEvents.PurchaseDone ->{
                                userPreferences.updatePremium(true)
                            }
                            is com.codecx.statussaver.core.utils.PurchaseEvents.OldPurchaseUpdate ->{
                                userPreferences.updatePremium(event.isPro)
                            }
                            else -> {}
                        }
                    }
                }
                launch {
                    billingHelperNew.initBilling(subs = listOf(BuildConfig.SUB_WEEKLY, BuildConfig.SUB_MONTHLY,
                        BuildConfig.SUB_YEARLY),listOf())
                }
            }

        }
    }
}