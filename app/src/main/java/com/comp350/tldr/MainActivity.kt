package com.comp350.tldr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.comp350.tldr.ui.theme.COMP350TLDRTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Main entry point for the app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            COMP350TLDRTheme {
                // Set up navigation with fade transitions between screens
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "welcome_screen",
                    enterTransition = { fadeIn(animationSpec = tween(700)) },
                    exitTransition = { fadeOut(animationSpec = tween(700)) }
                ) {
                    // Define app screens here
                    composable("welcome_screen") {
                        WelcomeScreen(navController)
                    }
                    composable("main_menu") {
                        MainMenuScreen(navController)
                    }
                    // Add your screen here using the pattern above
                }
            }
        }
    }
}




// Preview for welcome screen in Android Studio
@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    COMP350TLDRTheme {
        val dummyNavController = rememberNavController()
        WelcomeScreen(dummyNavController)
    }
}