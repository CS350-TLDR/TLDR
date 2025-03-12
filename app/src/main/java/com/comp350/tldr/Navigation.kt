package com.comp350.tldr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

// Central location for all navigation route names
// This helps avoid typos and makes refactoring easier
object NavRoutes {
    const val WELCOME = "welcome_screen"
    const val MAIN_MENU = "main_menu"
    const val COURSE_SELECTION = "course_selection"

    // const val LESSONS = "lessons_screen"
     const val PROFILE = "profile_screen"
}

// Helper function to get current route
@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
