package com.codecx.statussaver.data.models

data class PricesMenu(
    val price: String,
    val microPrice: Long,
    val currencyCode: String,
    val id: String = "",
    val productId: String = "",
    val offerId: String? = null,
    val offerToken: String = ""
)