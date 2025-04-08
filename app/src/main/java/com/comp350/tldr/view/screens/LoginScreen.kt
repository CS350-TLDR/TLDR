package com.comp350.tldr.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.comp350.tldr.controller.navigation.NavigationController
import com.comp350.tldr.view.components.PixelBackground
import com.comp350.tldr.view.theme.AppTheme

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val navigationController = NavigationController(navController)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    PixelBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LoginScreenTitle()

            Spacer(modifier = Modifier.height(40.dp))

            EmailInputField(
                email = email,
                onEmailChange = { email = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordInputField(
                password = password,
                onPasswordChange = { password = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            LoginButton(
                isLoading = isLoading,
                email = email,
                password = password,
                auth = auth,
                navigationController = navigationController,
                onLoadingChange = { isLoading = it },
                onErrorMessageChange = { errorMessage = it }
            )

            ErrorMessageDisplay(errorMessage)

            Spacer(modifier = Modifier.height(24.dp))

            SignupNavigationButton(navigationController)
        }
    }
}

@Composable
private fun LoginScreenTitle() {
    Text(
        "Log In",
        fontSize = 60.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        style = AppTheme.pixelTextStyle,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun EmailInputField(
    email: String,
    onEmailChange: (String) -> Unit
) {
    TextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email", color = Color.Gray) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordInputField(
    password: String,
    onPasswordChange: (String) -> Unit
) {
    TextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password", color = Color.Gray) },
        visualTransformation = PasswordVisualTransformation(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LoginButton(
    isLoading: Boolean,
    email: String,
    password: String,
    auth: FirebaseAuth,
    navigationController: NavigationController,
    onLoadingChange: (Boolean) -> Unit,
    onErrorMessageChange: (String?) -> Unit
) {
    Button(
        onClick = {
            onLoadingChange(true)
            onErrorMessageChange(null)

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    onLoadingChange(false)
                    if (task.isSuccessful) {
                        // Navigate to main menu on success
                        navigationController.navigateToMainMenu()
                    } else {
                        onErrorMessageChange(task.exception?.message)
                    }
                }
        },
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.blueButtonColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
            Text(
                "Login",
                fontSize = 26.sp,
                color = Color.White,
                style = AppTheme.pixelTextStyle
            )
        }
    }
}

@Composable
private fun ErrorMessageDisplay(errorMessage: String?) {
    errorMessage?.let {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = it,
            color = Color.Red,
            fontSize = 16.sp,
            fontFamily = AppTheme.pixelFontFamily
        )
    }
}

@Composable
private fun SignupNavigationButton(navigationController: NavigationController) {
    TextButton(
        onClick = {
            navigationController.navigateToSignup()
        }
    ) {
        Text(
            "Don't have an account? Sign up",
            color = Color.White,
            fontSize = 18.sp,
            fontFamily = AppTheme.pixelFontFamily
        )
    }
}