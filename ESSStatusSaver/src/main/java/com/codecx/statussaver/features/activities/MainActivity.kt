package com.codecx.statussaver.features.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.codecx.statussaver.core.navigation.AppNavigationGraph
import com.codecx.statussaver.core.navigation.Route
import com.codecx.statussaver.core.ui.theme.StatusSaverTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        val route =  Route.Home
        setContent {
            StatusSaverTheme {
                AppNavigationGraph(route)
            }
        }
    }
}
