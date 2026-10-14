package com.codecx.videodownloader.data.models

data class PricesMenu(
    val price: String,
    val microPrice: Long,
    val currencyCode: String,
    val id: String = "",
    val productId: String = "",
    val offerId: String? = null,
    val offerToken: String = "",
    val billingPeriod: String = ""
) {
    fun formateBillingPeriod(): String {
        return when (billingPeriod) {
            "P1W" -> "Week"
            "P1M" -> "Month"
            "P3M" -> "Three Months"
            "P4W"-> "Four Weeks"
            "P6M"-> "Six Months"
            "P1Y"-> "1 Year"
            "P3D"-> "3 Days"
            else -> billingPeriod
        }
    }
}