package com.codecx.videodownloader.data.models

import java.util.UUID


data class TabModel(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Home",
    val url: String = "about:blank",
    val favicon: Int = android.R.drawable.ic_menu_compass,
    val isSelected: Boolean = false
)