package com.codecx.statussaver.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    object Splash : Route

    @Serializable
    object Home : Route

    @Serializable
    object Language : Route

    @Serializable
    object Intro : Route

    @Serializable
    object Settings : Route

    @Serializable
    object Premium : Route

    @Serializable
    data class MediaView(val selectedPath: String, val tabId: Int) : Route

    @Serializable
    object About : Route

}