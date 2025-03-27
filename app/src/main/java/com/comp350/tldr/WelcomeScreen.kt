package com.comp350.tldr

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// Composable function for the Welcome screen (splash screen)
@Composable
fun WelcomeScreen(navController: NavController) {
    // Controls whether the screen content is visible (used for fade animation)
    var isVisible by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Main container for the screen content
    Box(modifier = Modifier.fillMaxSize()) {

        // Background image fills the entire screen
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Animated visibility for fade-in/out effects on the welcome content
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(1000)),    // fade in over 1 second
            exit = fadeOut(animationSpec = tween(1000))     // fade out over 1 second
        ) {
            // Column layout for vertical stacking of elements
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(100.dp)) // space from top

                // App title
                Text(
                    text = "TLDR",
                    color = Color.White,
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Robot mascot image
                Image(
                    painter = painterResource(id = R.drawable.robot),
                    contentDescription = "Robot character",
                    modifier = Modifier
                        .width(120.dp)
                        .height(120.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // App tagline
                Text(
                    text = "Learn to code...\na little everyday",
                    color = Color.White,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.weight(1f)) // push content to the top

                // Buttons section for login and signup
                Column(
                    modifier = Modifier
                        .padding(bottom = 200.dp), // space from bottom
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Log In Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                // Trigger fade-out before navigating
                                isVisible = false
                                delay(500)
                                navController.navigate("main_menu") //////////////login
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)), // blue
                        modifier = Modifier
                            .width(200.dp)
                            .height(50.dp)
                    ) {
                        Text("Log In", fontSize = 18.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sign Up Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                // Trigger fade-out before navigating
                                isVisible = false
                                delay(500)
                                navController.navigate("signup")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)), // green
                        modifier = Modifier
                            .width(200.dp)
                            .height(50.dp)
                    ) {
                        Text("Sign Up", fontSize = 18.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

