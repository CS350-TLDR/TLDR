//package com.comp350.tldr.view.components
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.BoxScope
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.Button
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import com.comp350.tldr.view.theme.AppTheme
//import com.comp350.tldr.view.screens.SignupScreen
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.comp350.tldr.controller.navigation.NavigationController
//import com.comp350.tldr.view.components.PixelBackground
//import com.comp350.tldr.view.components.SignupButton
//import com.comp350.tldr.view.theme.AppTheme
//import com.google.firebase.auth.FirebaseAuth
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Shadow
//import com.comp350.tldr.view.theme.AppTheme.pixelTextStyle
//
//@Composable fun SignupButton() {
//    Button(
//        onClick = {
//            when {
//                email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
//                    errorMessage = "All fields are required"
//                }
//
//                password != confirmPassword -> {
//                    errorMessage = "Passwords do not match"
//                }
//
//                password.length < 6 -> {
//                    errorMessage = "Password must be at least 6 characters"
//                }
//
//                else -> {
//                    isLoading = true
//                    errorMessage = null
//
//                    auth.createUserWithEmailAndPassword(email, password)
//                        .addOnCompleteListener { task ->
//                            isLoading = false
//                            if (task.isSuccessful) {
//                                // Navigate to welcome screen
//                                navigationController.navigateToWelcome()
//                            } else {
//                                errorMessage = task.exception?.message ?: "Sign up failed"
//                            }
//                        }
//                }
//            }
//        },
//        enabled = !isLoading,
//        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(56.dp)
//    )
//
//    {
//        Text(
//            "Sign Up",
//            fontSize = 26.sp,
//            color = Color(0xFF1A237E),
//            style = pixelTextStyle.copy(
//                shadow = Shadow(
//                    color = Color.Black,
//                    blurRadius = 2f,
//                    offset = Offset(3f, 3f)
//                )
//            )
//        )
//    }
//}
