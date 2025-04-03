// MainActivity.kt
package com.comp350.tldr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.comp350.tldr.controller.navigation.NavRoutes
import com.comp350.tldr.ui.theme.screens.*
import com.comp350.tldr.view.screens.LoginScreen
import com.comp350.tldr.view.screens.MainMenuScreen
import com.comp350.tldr.view.screens.ProfileScreen
import com.comp350.tldr.view.screens.SignupScreen
import com.comp350.tldr.view.screens.WelcomeScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

@Composable fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.WELCOME
    ) {
        // Welcome Screen
        composable(NavRoutes.WELCOME) {
            WelcomeScreen(navController)
        }

        // Login Screen
        composable(NavRoutes.LOGIN) {
            LoginScreen(navController)
        }

        // Signup Screen
        composable(NavRoutes.SIGNUP) {
            SignupScreen(navController)
        }

        // Main Menu Screen
        composable(NavRoutes.MAIN_MENU) {
            MainMenuScreen(navController)
        }

        // Profile Screen
        composable(NavRoutes.PROFILE) {
            ProfileScreen(navController)
        }

    }
}