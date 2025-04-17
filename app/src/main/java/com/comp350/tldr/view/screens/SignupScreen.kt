package com.comp350.tldr.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.comp350.tldr.controller.navigation.NavigationController
import com.comp350.tldr.view.components.PixelBackground
import com.comp350.tldr.view.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextDecoration
import com.comp350.tldr.view.theme.AppTheme.pixelTextStyle

@Composable
fun SignupScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val navigationController = NavigationController(navController)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    PixelBackground {
        // Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Create Account",
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = pixelTextStyle,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email input field with white background
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password input field with white background
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.Gray) },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password input field with white background
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", color = Color.Gray) },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))


            Button(
                onClick = {
                    when {
                        email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            errorMessage = "All fields are required"
                        }

                        password != confirmPassword -> {
                            errorMessage = "Passwords do not match"
                        }

                        password.length < 6 -> {
                            errorMessage = "Password must be at least 6 characters"
                        }

                        else -> {
                            isLoading = true
                            errorMessage = null

                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        // Navigate to welcome screen
                                        navigationController.navigateToWelcome()
                                    } else {
                                        errorMessage = task.exception?.message ?: "Sign up failed"
                                    }
                                }
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            {
                Text(
                    "Sign Up",
                    fontSize = 26.sp,
                    color = Color(0xFF1A237E),
                    style = pixelTextStyle.copy(
                        shadow = Shadow(
                            color = Color.Black,
                            blurRadius = 2f,
                            offset = Offset(3f, 3f)
                        )
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom

        ) {
            // Error message
            errorMessage?.let {

                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 20.sp,
                    fontFamily = AppTheme.pixelFontFamily,
                )
            }

            // Link to login screen
            TextButton(onClick = { navigationController.navigateToLogin() },
                Modifier.offset(0.dp, (-180).dp)) {
                Text(
                    "Already have an account? Log in",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = AppTheme.pixelFontFamily,
                    textDecoration = TextDecoration.Underline,
                )
            }

        }

    }
}





