package com.codecx.statussaver.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codecx.statussaver.features.composes.mainBoarding.home.HomeScreen
import com.codecx.statussaver.features.composes.mainBoarding.mediaView.MediaViewScreen

val LocalNavController = compositionLocalOf<NavController> { error("No NavController found!") }


@Composable
fun AppNavigationGraph(startUpRoute:Route) {
    val navController = rememberNavController()

    CompositionLocalProvider(
        LocalNavController provides navController
    )
    {
        NavHost(
            navController = navController,
            startDestination = startUpRoute,
            modifier = Modifier.fillMaxSize()
        ) {
            composable<Route.Home> {
                HomeScreen()
            }
            composable<Route.MediaView> {
                MediaViewScreen(it)
            }
        }
    }

}