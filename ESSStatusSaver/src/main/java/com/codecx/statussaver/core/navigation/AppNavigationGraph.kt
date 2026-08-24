package com.codecx.statussaver.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codecx.statussaver.data.repos.UserPreferences
import com.codecx.statussaver.features.composes.mainBoarding.home.HomeScreen
import com.codecx.statussaver.features.composes.mainBoarding.mediaView.MediaViewScreen
import com.codecx.statussaver.features.composes.mainBoarding.premium.PremiumScreen
import org.koin.compose.koinInject
import java.util.Locale

val LocalNavController = compositionLocalOf<NavController> { error("No NavController found!") }


@Composable
fun AppNavigationGraph(startUpRoute:Route) {
    val navController = rememberNavController()
    val userPref = koinInject<UserPreferences>()
    val context = LocalContext.current
    val currentLanguage by userPref.userLanguage.collectAsStateWithLifecycle("en")
    LaunchedEffect(currentLanguage) {
        val locale = Locale(currentLanguage)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

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
            composable<Route.Premium> {
                PremiumScreen()
            }
            composable<Route.MediaView> {
                MediaViewScreen(it)
            }
        }
    }

}