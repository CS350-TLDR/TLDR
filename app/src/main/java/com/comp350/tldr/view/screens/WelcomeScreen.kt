package com.comp350.tldr.view.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.comp350.tldr.R
import com.comp350.tldr.controller.navigation.NavigationController
import com.comp350.tldr.view.components.PixelBackground
import com.comp350.tldr.view.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset

@Composable
fun WelcomeScreen(navController: NavController) {
    // Controls whether the screen content is visible (used for fade animation)
    var isVisible by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val navigationController = NavigationController(navController)

    PixelBackground { // Invokes PixelBackground.kt from view/components
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
                    style = AppTheme.pixelTextStyle
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Robot mascot image
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
                    text = "Learn a little everyday",
                    color = Color.White,
                    fontSize = 40.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    style = AppTheme.pixelTextStyle
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
                                navigationController.navigateToLogin()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.blueButtonColor),
                        modifier = Modifier
                            .width(200.dp)
                            .height(50.dp)
                    ) {
                        Text(
                            "Log In",
                            fontSize = 26.sp,
                            color = Color.White,
                            style = AppTheme.pixelTextStyle
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
                                navigationController.navigateToSignup()
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
                            color = AppTheme.darkBlueButtonColor,
                            style = AppTheme.pixelTextStyle.copy(
                                shadow = AppTheme.pixelTextStyle.shadow?.copy(
                                    blurRadius = 2f,
                                    offset = Offset(3f, 3f)
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}