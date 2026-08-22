package com.codecx.videodownloader.data.models


data class TabModel (
    val id: Long,
    var title: String = "Home",
    var url: String = "about:blank",
    var isSelected: Boolean = false
)