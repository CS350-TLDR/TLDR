package com.comp350.tldr.view.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.comp350.tldr.controller.navigation.NavigationController
import com.comp350.tldr.view.components.PixelBackground
import com.comp350.tldr.view.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SignupScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val navigationController = NavigationController(navController)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }


    val audioPlayer = ExoPlayer.Builder(navController.context).build()
    audioPlayer.volume = 1.0f // volume control from 1 is unchanged volume, 0.5 is half volume.
    var mediaItem = MediaItem.fromUri("android.resource://${navController.context.packageName}/raw/test_sound_a")
    audioPlayer.setMediaItem(mediaItem)
    audioPlayer.prepare()

    var audioExit = false
    // Set up a listener for the STATE_ENDED,
    // frees resources after completing sound and exiting screen.
    audioPlayer.addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED && audioExit) {
                Log.d("ExoPlayer", "SignUp Playback finished")
                audioPlayer.release()
            }
        }
    })



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
                style = AppTheme.pixelTextStyle,
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

            // Sign Up Button (white button with dark blue text)
            Button(
                onClick = {
                    when {
                        email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            errorMessage = "All fields are required"
                            mediaItem = MediaItem.fromUri("android.resource://${navController.context.packageName}/raw/wrong_sound")
                        }
                        password != confirmPassword -> {
                            errorMessage = "Passwords do not match"
                            mediaItem = MediaItem.fromUri("android.resource://${navController.context.packageName}/raw/wrong_sound")
                        }
                        password.length < 6 -> {
                            errorMessage = "Password must be at least 6 characters"
                            mediaItem = MediaItem.fromUri("android.resource://${navController.context.packageName}/raw/wrong_sound")
                        }
                        else -> {
                            isLoading = true
                            errorMessage = null

                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        mediaItem = MediaItem.fromUri("android.resource://${navController.context.packageName}/raw/account_created_sound")
                                        audioPlayer.setMediaItem(mediaItem)
                                        audioPlayer.prepare() // restart the player
                                        audioPlayer.play()
                                        audioExit = true
                                        // Save the signed-up email to SharedPreferences
                                        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                        val editor = prefs.edit()
                                        val currentSet = prefs.getStringSet("saved_emails", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                                        currentSet.add(email)
                                        editor.putStringSet("saved_emails", currentSet)
                                        editor.apply()

                                        // Navigate to welcome screen
                                        navigationController.navigateToWelcome()
                                    }
                                }
                        }
                    }
                    audioPlayer.setMediaItem(mediaItem) // for "wrong" sound
                    audioPlayer.prepare() // restart the player
                    audioPlayer.play()
                    audioExit = false
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = AppTheme.darkBlueButtonColor, modifier = Modifier.size(24.dp))
                } else {
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

            // Error message
            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontFamily = AppTheme.pixelFontFamily
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Link to login screen
            TextButton(onClick = {
                mediaItem = MediaItem.fromUri("android.resource://${navController.context.packageName}/raw/click_sound_b")
                audioPlayer.setMediaItem(mediaItem)
                audioPlayer.prepare() // restart the player
                audioPlayer.play()
                audioExit = true
                navigationController.navigateToLogin()
            }) {
                Text(
                    "Already have an account? Log in",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = AppTheme.pixelFontFamily
                )
            }
        }
    }
}
