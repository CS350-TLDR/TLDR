package com.comp350.tldr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.comp350.tldr.ui.theme.COMP350TLDRTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            COMP350TLDRTheme {
                // Remember the NavController
                val navController = rememberNavController()

                // Use Scaffold to create a layout with a bottom navigation bar
                Scaffold(
                    bottomBar = {
                        // Only show bottom bar after welcome screen
                        val currentRoute = currentRoute(navController)
                        if (currentRoute != "welcome_screen") {
                            BottomNavBar(navController)
                        }
                    }
                ) { paddingValues ->
                    // Navigation host inside scaffold with padding for the bottom bar
                    NavHost(
                        navController = navController,
                        startDestination = "welcome_screen",
                        modifier = Modifier.padding(paddingValues),
                        enterTransition = { fadeIn(animationSpec = tween(700)) },
                        exitTransition = { fadeOut(animationSpec = tween(700)) }
                    ) {
                        composable("welcome_screen") {
                            WelcomeScreen(navController)
                        }
                        composable("main_menu") {
                            MainMenuScreen(navController)
                        }
                        composable("profile") {
                            ProfileScreen(navController)
                        }
                        // Add more screens as needed
                    }
                }
            }
        }
    }
}






