package com.comp350.tldr.controllers

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.comp350.tldr.controller.navigation.NavRoutes

class NavigationController(private val navController: NavController) {

    fun navigateToWelcome() {
        navController.navigate(NavRoutes.WELCOME) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
        }
    }

    fun navigateToLogin() {
        navController.navigate(NavRoutes.LOGIN)
    }

    fun navigateToSignup() {
        navController.navigate(NavRoutes.SIGNUP)
    }

    fun navigateToMainMenu() {
        navController.navigate(NavRoutes.MAIN_MENU) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
        }
    }

    fun navigateToProfile() {
        navController.navigate(NavRoutes.PROFILE)
    }

    fun navigateToResults(score: Int, totalQuestions: Int) {
        navController.navigate("${NavRoutes.RESULTS_PAGE.substringBefore("{")}/$score/$totalQuestions")
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}