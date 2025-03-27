package com.comp350.tldr

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
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

    // Define the pixel font family using the rainyhearts font
    val pixelFontFamily = FontFamily(
        Font(R.font.rainyhearts, FontWeight.Normal)
    )

    // Text style with pixel font and much thicker black outline
    val pixelTextStyle = TextStyle(
        fontFamily = pixelFontFamily,
        shadow = Shadow(
            color = Color.Black,
            blurRadius = 2f,
            offset = androidx.compose.ui.geometry.Offset(6f, 6f)
        )
    )

    // Create a blue to dark blue gradient
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF4B89DC),  // Light blue color
            Color(0xFF3568CC),  // Medium blue
            Color(0xFF1A237E)   // Dark blue color
        )
    )

    // Main container for the screen content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        // Pixelated overlay effect
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val pixelSize = 20f
            val width = size.width
            val height = size.height

            // Draw pixelated grid
            for (x in 0 until (width / pixelSize).toInt()) {
                for (y in 0 until (height / pixelSize).toInt()) {
                    // Calculate position and size
                    val left = x * pixelSize
                    val top = y * pixelSize

                    // Create random opacity for each pixel to create texture
                    val opacity = if ((x + y) % 4 == 0) 0.1f else 0.05f

                    // Draw pixel square with slightly different color
                    drawRect(
                        color = Color.Black.copy(alpha = opacity),
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(pixelSize, pixelSize)
                    )
                }
            }
        }
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
                    fontSize = 85.sp,
                    fontWeight = FontWeight.Bold,
                    style = pixelTextStyle
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Robot mascot image without border
                Image(
                    painter = painterResource(id = R.drawable.robot),
                    contentDescription = "Robot character",
                    modifier = Modifier
                        .width(140.dp)
                        .height(140.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // App tagline
                Text(
                    text = "Learn to code...\na little everyday",
                    color = Color.White,
                    fontSize = 40.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    style = pixelTextStyle
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
                        Text(
                            "Log In",
                            fontSize = 26.sp,
                            color = Color.White,
                            style = pixelTextStyle
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sign Up Button (now white with dark text)
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                // Trigger fade-out before navigating
                                isVisible = false
                                delay(500)
                                navController.navigate("signup")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        modifier = Modifier
                            .width(200.dp)
                            .height(50.dp)
                    ) {
                        Text(
                            "Sign Up",
                            fontSize = 26.sp,
                            color = Color(0xFF1A237E), // Dark blue text on white button
                            style = pixelTextStyle.copy(
                                shadow = Shadow(
                                    color = Color.Black,
                                    blurRadius = 2f,
                                    offset = androidx.compose.ui.geometry.Offset(3f, 3f)
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}