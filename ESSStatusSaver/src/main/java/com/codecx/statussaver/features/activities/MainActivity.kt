package com.codecx.statussaver.features.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import com.codecx.statussaver.core.navigation.AppNavigationGraph
import com.codecx.statussaver.core.navigation.Route
import com.codecx.statussaver.core.ui.theme.StatusSaverTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        val isProStartUp = intent.extras?.getBoolean("isPro",false)?:false
        val route = if(isProStartUp) Route.Premium else Route.Home
        setContent {
            StatusSaverTheme {
                AppNavigationGraph(route)
            }
        }
    }
}
