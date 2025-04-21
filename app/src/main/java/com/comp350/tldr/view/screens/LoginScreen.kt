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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.comp350.tldr.controller.navigation.NavigationController
import com.comp350.tldr.controller.viewmodels.LoginViewModel
import com.comp350.tldr.view.components.PixelBackground
import com.comp350.tldr.view.theme.AppTheme
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth


@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = viewModel()
    val navigationController = NavigationController(navController)

    val email by loginViewModel.email.collectAsState()
    val password by loginViewModel.password.collectAsState()
    val errorMessage by loginViewModel.errorMessage.collectAsState()
    val isLoading by loginViewModel.isLoading.collectAsState()
    val savedEmails by loginViewModel.savedEmails.collectAsState()

    // for forgot password
    var showResetDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }


    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        loginViewModel.loadSavedEmails(context)
        loginViewModel.updateEmail("") // optional: clear old email
        loginViewModel.updatePassword("")
    }


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
                onEmailChange = loginViewModel::updateEmail,
                savedEmails = savedEmails
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordInputField(
                password = password,
                onPasswordChange = loginViewModel::updatePassword
            )

            Spacer(modifier = Modifier.height(32.dp))

            LoginButton(
                isLoading = isLoading,
                onClick = {
                    coroutineScope.launch {
                        loginViewModel.login(context) { success ->
                            if (success) {
                                navigationController.navigateToMainMenu()
                            }
                        }
                    }
                }
            )

            ErrorMessageDisplay(errorMessage)

            Spacer(modifier = Modifier.height(24.dp))

            SignupNavigationButton(navigationController)

            TextButton(onClick = { showResetDialog = true }) {
                TextButton(onClick = { showResetDialog = true }) {
                    Text(
                        "Forgot Password?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = AppTheme.pixelFontFamily
                    )
                }

            }

            ForgotPasswordDialog(
                showDialog = showResetDialog,
                onDismiss = { showResetDialog = false },
                onSendReset = { emailToReset ->
                    FirebaseAuth.getInstance().sendPasswordResetEmail(emailToReset)
                        .addOnCompleteListener { task ->
                            snackbarMessage = if (task.isSuccessful) {
                                "Password reset email sent"
                            } else {
                                task.exception?.message ?: "Failed to send reset email"
                            }
                        }
                }
            )

            snackbarMessage?.let { message ->
                Snackbar(
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("Dismiss", color = Color.Yellow)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(message)
                }
            }


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailInputField(
    email: String,
    onEmailChange: (String) -> Unit,
    savedEmails: List<String>
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val filteredEmails = remember(email, savedEmails) {
        savedEmails.filter {
            it.contains(email, ignoreCase = true) && it != email
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filteredEmails.isNotEmpty(),
        onExpandedChange = {
            // Only toggle expanded state when clicking the dropdown icon
            if (filteredEmails.isNotEmpty()) {
                expanded = !expanded
            }
        }
    ) {
        TextField(
            value = email,
            onValueChange = {
                onEmailChange(it)
                // Keep menu open if we have suggestions
                if (filteredEmails.isNotEmpty()) {
                    expanded = true
                }
            },
            label = { Text("Email", color = Color.Gray) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        // Only show dropdown when we have suggestions
        if (filteredEmails.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredEmails.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            onEmailChange(suggestion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
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
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
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

@Composable
fun ForgotPasswordDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSendReset: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Reset Password") },
            text = {
                Column {
                    Text("Enter your email to receive a password reset link.")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSendReset(email)
                    onDismiss()
                }) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
