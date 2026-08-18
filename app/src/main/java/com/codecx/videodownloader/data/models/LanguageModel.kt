package com.codecx.videodownloader.data.models

data class LanguageModel(
    val name: String,
    val flagResId: Int,
    val code: String,
    var isSelected: Boolean = false
)