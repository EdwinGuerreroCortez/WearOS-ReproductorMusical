package com.kronnoz.reproductormusica.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.kronnoz.reproductormusica.presentation.navigation.AppNavigation
import com.kronnoz.reproductormusica.presentation.theme.ReproductorMusicaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            ReproductorMusicaTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}
