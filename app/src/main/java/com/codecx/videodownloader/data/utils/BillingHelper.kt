package com.codecx.videodownloader.data.utils

import com.codecx.videodownloader.data.models.PricesMenu
import com.codecx.videodownloader.data.models.PurchaseEvents
import kotlinx.coroutines.flow.StateFlow

interface BillingHelper {
    fun initBilling(subs: List<String>, products: List<String>)
    suspend fun restorePurchases(): Boolean
    fun launchBilling(pricesMenu: PricesMenu)
    fun closeConnection()
    val purchaseUpdateEventListener: StateFlow<PurchaseEvents>
    val offersUpdateListener: StateFlow<List<PricesMenu>>
}