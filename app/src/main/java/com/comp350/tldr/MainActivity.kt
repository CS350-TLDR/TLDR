package com.comp350.tldr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.comp350.tldr.lessonScreens.LessonPresentation_1
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
                            //BottomNavBar(navController)
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
                        composable("welcome_screen") {  // input: navigation route name
                            WelcomeScreen(navController) // input: associated kt file name, Note: Must match with files' primary function name
                        }
                        composable("main_menu") {
                            MainMenuScreen(navController)
                        }
                        composable("profile") {
                            ProfileScreen(navController)
                        }


                        //------------------ Lessons
                        composable("lesson_presentation_1") {
                            LessonPresentation_1(navController)
                        }
                        //------------------ Quizzes
                        composable("quiz_page") {
                            QuizPage(navController)
                        }
                        composable("results_page/{score}/{totalQuestions}") { backStackEntry ->
                            val score =
                                backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
                            val totalQuestions =
                                backStackEntry.arguments?.getString("totalQuestions")?.toIntOrNull()
                                    ?: 0
                            ResultsPage(navController, score, totalQuestions)
                        }

                        // Add more screens as needed
                    }
                }
            }
        }
    }
}






