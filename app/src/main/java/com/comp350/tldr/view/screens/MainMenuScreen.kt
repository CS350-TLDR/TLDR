// ui/theme/screens/MainMenuScreen.kt
package com.comp350.tldr.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import com.comp350.tldr.R
import com.comp350.tldr.controllers.NavigationController
import com.comp350.tldr.view.components.PixelBackground
import com.comp350.tldr.view.theme.AppTheme
import com.comp350.tldr.model.services.PopQuizService

@Composable
fun MainMenuScreen(navController: NavController) {
    // Use LocalContext to ensure valid context
    val localContext = LocalContext.current
    val navigationController = NavigationController(navController)

    // State variables
    var selectedTopic by remember { mutableStateOf("Python") }
    var selectedActivity by remember { mutableStateOf("Trivia") }
    var isPopupEnabled by remember { mutableStateOf(false) }

    // Dropdown states
    var topicExpanded by remember { mutableStateOf(false) }
    var activityExpanded by remember { mutableStateOf(false) }

    // List of available topics
    val topics = listOf("Python")

    // List of available activities
    val activities = listOf("Trivia", "Video")

    PixelBackground {
        // Profile button in bottom right corner - Now larger with text
        Button(
            onClick = {
                navigationController.navigateToProfile()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .height(50.dp)
                .width(120.dp)
        ) {
            Text(
                text = "Profile",
                fontSize = 22.sp,
                color = AppTheme.darkBlueButtonColor,
                style = AppTheme.pixelTextStyle.copy(
                    shadow = AppTheme.pixelTextStyle.shadow?.copy(
                        blurRadius = 1f,
                        offset = Offset(2f, 2f)
                    )
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Robot at the top
            Image(
                painter = painterResource(id = R.drawable.robot),
                contentDescription = "Robot mascot",
                modifier = Modifier
                    .size(180.dp)
                    .padding(top = 24.dp, bottom = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Topic Dropdown
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Topic",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = AppTheme.pixelTextStyle,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    Button(
                        onClick = { topicExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(200.dp)
                            .height(56.dp)
                    ) {
                        Text(
                            text = selectedTopic,
                            fontSize = 26.sp,
                            color = AppTheme.darkBlueButtonColor,
                            style = AppTheme.pixelTextStyleSmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    DropdownMenu(
                        expanded = topicExpanded,
                        onDismissRequest = { topicExpanded = false },
                        modifier = Modifier
                            .width(200.dp)
                            .background(Color.White)
                    ) {
                        topics.forEach { topic ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = topic,
                                        fontSize = 24.sp,
                                        fontFamily = AppTheme.pixelFontFamily
                                    )
                                },
                                onClick = {
                                    selectedTopic = topic
                                    topicExpanded = false

                                    // Update service if running
                                    if (isPopupEnabled) {
                                        try {
                                            startPopupService(localContext, selectedTopic, selectedActivity)
                                        } catch (e: Exception) {
                                            Log.e("MainMenuScreen", "Error updating service", e)
                                            Toast.makeText(localContext, "Error updating service", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Activity Dropdown
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Activity",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = AppTheme.pixelTextStyle,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    Button(
                        onClick = { activityExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(200.dp)
                            .height(56.dp)
                    ) {
                        Text(
                            text = selectedActivity,
                            fontSize = 26.sp,
                            color = AppTheme.darkBlueButtonColor,
                            style = AppTheme.pixelTextStyleSmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    DropdownMenu(
                        expanded = activityExpanded,
                        onDismissRequest = { activityExpanded = false },
                        modifier = Modifier
                            .width(200.dp)
                            .background(Color.White)
                    ) {
                        activities.forEach { activity ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = activity,
                                        fontSize = 24.sp,
                                        fontFamily = AppTheme.pixelFontFamily
                                    )
                                },
                                onClick = {
                                    selectedActivity = activity
                                    activityExpanded = false

                                    // Update service if running
                                    if (isPopupEnabled) {
                                        try {
                                            startPopupService(localContext, selectedTopic, selectedActivity)
                                        } catch (e: Exception) {
                                            Log.e("MainMenuScreen", "Error updating service", e)
                                            Toast.makeText(localContext, "Error updating service", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // On/Off switch centered at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Off/On",  // Changed from "Enable Popups" to "Off/On"
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = AppTheme.pixelFontFamily,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Switch(
                        checked = isPopupEnabled,
                        onCheckedChange = { isEnabled ->
                            if (isEnabled) {
                                try {
                                    // Check for overlay permission before enabling
                                    if (!Settings.canDrawOverlays(localContext)) {
                                        // Request permission
                                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                        localContext.startActivity(intent)
                                        Toast.makeText(
                                            localContext,
                                            "Please grant overlay permission to enable popups",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        isPopupEnabled = true
                                        startPopupService(localContext, selectedTopic, selectedActivity)
                                    }
                                } catch (e: Exception) {
                                    Log.e("MainMenuScreen", "Error starting popup service", e)
                                    Toast.makeText(localContext, "Error starting popup service", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                isPopupEnabled = false
                                try {
                                    stopPopupService(localContext)
                                } catch (e: Exception) {
                                    Log.e("MainMenuScreen", "Error stopping popup service", e)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4B89DC),
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color(0xFF444444)
                        ),
                        modifier = Modifier.size(width = 80.dp, height = 48.dp)
                    )

                    if (isPopupEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Popups for $selectedActivity will appear every 60 seconds",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontFamily = AppTheme.pixelFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Test Button for direct popup
            Button(
                onClick = {
                    try {
                        // First, check permission
                        if (!Settings.canDrawOverlays(localContext)) {
                            // Request permission
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                            localContext.startActivity(intent)
                            Toast.makeText(
                                localContext,
                                "Please grant overlay permission first",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(localContext, "Testing popup...", Toast.LENGTH_SHORT).show()

                            // Start service with immediate popup
                            val intent = Intent(localContext, PopQuizService::class.java)
                            intent.putExtra("topic", selectedTopic)
                            intent.putExtra("activity", selectedActivity)
                            intent.putExtra("test_mode", true)
                            intent.action = "START_SERVICE"
                            localContext.startService(intent)
                        }
                    } catch (e: Exception) {
                        Log.e("MainMenuScreen", "Error testing popup", e)
                        Toast.makeText(
                            localContext,
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.blueButtonColor
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = "Test Popup Now",
                    fontSize = 22.sp,
                    color = Color.White,
                    style = AppTheme.pixelTextStyleSmall
                )
            }

            // Extra space at the bottom to account for profile button
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Helper function to start the popup service
private fun startPopupService(context: Context, topic: String, activity: String) {
    try {
        // Create intent with context and class
        val intent = Intent(context, PopQuizService::class.java)

        // Add extras and action
        intent.putExtra("topic", topic)
        intent.putExtra("activity", activity)
        intent.putExtra("interval", 60000L) // Fixed 60 seconds interval
        intent.action = "START_SERVICE"

        context.startService(intent)
        Log.d("MainMenuScreen", "Service start intent sent with topic: $topic, activity: $activity")
    } catch (e: Exception) {
        Log.e("MainMenuScreen", "Error in startPopupService", e)
        Toast.makeText(context, "Error starting service: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// Helper function to stop the popup service
private fun stopPopupService(context: Context) {
    try {
        // Create intent with context and class
        val intent = Intent(context, PopQuizService::class.java)

        // Set action
        intent.action = "STOP_SERVICE"

        context.startService(intent)
        Log.d("MainMenuScreen", "Service stop intent sent")
    } catch (e: Exception) {
        Log.e("MainMenuScreen", "Error in stopPopupService", e)
        Toast.makeText(context, "Error stopping service: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}