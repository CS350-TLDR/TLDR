package com.comp350.tldr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Define the pixel font family
    val pixelFontFamily = FontFamily(
        Font(R.font.rainyhearts, FontWeight.Normal)
    )

    // Text style with pixel font and thick black outline
    val pixelTextStyle = TextStyle(
        fontFamily = pixelFontFamily,
        shadow = Shadow(
            color = Color.Black,
            blurRadius = 2f,
            offset = androidx.compose.ui.geometry.Offset(6f, 6f)
        )
    )

    // Create blue to dark blue gradient
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF4B89DC),  // Light blue color
            Color(0xFF3568CC),  // Medium blue
            Color(0xFF1A237E)   // Dark blue color
        )
    )

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

        // Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Log In",
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = pixelTextStyle,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = Color.White.copy(alpha = 0.8f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedLabelColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.White.copy(alpha = 0.8f)) },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedLabelColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Blue Button
            Button(
                onClick = {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                navController.navigate("main_menu") {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                }
                            } else {
                                errorMessage = task.exception?.message
                            }
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "Login",
                    fontSize = 26.sp,
                    color = Color.White,
                    style = pixelTextStyle
                )
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontFamily = pixelFontFamily
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = {
                    navController.navigate("signup")
                }
            ) {
                Text(
                    "Don't have an account? Sign up",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = pixelFontFamily
                )
            }
        }
    }
}