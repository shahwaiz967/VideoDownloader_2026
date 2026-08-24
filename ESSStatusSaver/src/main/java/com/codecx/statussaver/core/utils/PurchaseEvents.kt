package com.codecx.statussaver.core.utils

sealed interface PurchaseEvents {
    object PurchaseInit : PurchaseEvents
    object PurchaseDone : PurchaseEvents
    data class OldPurchaseUpdate(val isPro: Boolean) : PurchaseEvents
}