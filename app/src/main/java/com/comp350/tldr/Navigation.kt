package com.comp350.tldr

import androidx.navigation.compose.composable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraphBuilder

// Central location for all navigation route names
// This helps avoid typos and makes refactoring easier
object NavRoutes {
    const val WELCOME = "welcome_screen"
    const val MAIN_MENU = "main_menu"
    const val COURSE_SELECTION = "course_selection"
    const val RESULTS_PAGE = "results_page/{score}/{totalQuestions}"
    const val PROFILE = "profile_screen"

    const val LESSON_1 = "lesson_presentation_1"

    const val QUIZ_1 = "quiz_page" // update this later to be numbered
}

// Helper function to get current route
@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

fun NavGraphBuilder.addResultsPage(navController: NavController) {
    composable("results_page/{score}/{totalQuestions}") { backStackEntry ->
        val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
        val totalQuestions = backStackEntry.arguments?.getString("totalQuestions")?.toIntOrNull() ?: 0
        ResultsPage(navController, score, totalQuestions)
    }
}