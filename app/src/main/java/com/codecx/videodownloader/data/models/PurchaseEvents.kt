package com.codecx.videodownloader.data.models

sealed interface PurchaseEvents {
    object PurchaseInit : PurchaseEvents
    object PurchaseDone : PurchaseEvents
    data class OldPurchaseUpdate(val isPro: Boolean) : PurchaseEvents
}