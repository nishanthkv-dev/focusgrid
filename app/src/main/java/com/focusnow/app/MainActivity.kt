package com.focusnow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.focusnow.app.data.datastore.ThemeMode
import com.focusnow.app.ui.navigation.FocusNowNavGraph
import com.focusnow.app.ui.theme.FocusNowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as FocusNowApplication

        setContent {
            val themeMode by app.preferencesRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val isOnboardingCompleted by app.preferencesRepository.isOnboardingCompleted.collectAsState(initial = false)

            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            FocusNowTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                FocusNowNavGraph(
                    navController = navController,
                    isOnboardingCompleted = isOnboardingCompleted
                )
            }
        }
    }
}
