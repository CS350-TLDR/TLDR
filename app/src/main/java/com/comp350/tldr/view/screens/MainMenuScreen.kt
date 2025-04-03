// view/screens/MainMenuScreen.kt
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
import android.provider.Settings
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewmodel.compose.viewModel
import com.comp350.tldr.R
import com.comp350.tldr.controller.navigation.NavigationController
import com.comp350.tldr.controller.viewmodels.MainMenuViewModel
import com.comp350.tldr.view.components.PixelBackground
import com.comp350.tldr.view.theme.AppTheme

@Composable
fun MainMenuScreen(
    navController: NavController,
    viewModel: MainMenuViewModel = viewModel()
) {
    // Use LocalContext to ensure valid context
    val localContext = LocalContext.current
    val navigationController = NavigationController(navController)

    // Collect states from ViewModel
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val selectedActivity by viewModel.selectedActivity.collectAsState()
    val isPopupEnabled by viewModel.isPopupEnabled.collectAsState()

    // State for dropdowns - since your ViewModel may not have this yet
    var topicExpanded by remember { mutableStateOf(false) }
    var activityExpanded by remember { mutableStateOf(false) }

    PixelBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Robot Header
                RobotHeader()

                // Topic Selector
                TopicSelector(
                    selectedTopic = selectedTopic,
                    isExpanded = topicExpanded,
                    onToggleDropdown = { topicExpanded = !topicExpanded },
                    onTopicSelected = { topic ->
                        viewModel.updateTopic(topic)
                        topicExpanded = false

                        // Update service if running
                        if (isPopupEnabled) {
                            viewModel.togglePopupService(true, localContext)
                        }
                    },
                    availableTopics = viewModel.availableTopics
                )

                // Activity Selector
                ActivitySelector(
                    selectedActivity = selectedActivity,
                    isExpanded = activityExpanded,
                    onToggleDropdown = { activityExpanded = !activityExpanded },
                    onActivitySelected = { activity ->
                        viewModel.updateActivity(activity)
                        activityExpanded = false

                        // Update service if running
                        if (isPopupEnabled) {
                            viewModel.togglePopupService(true, localContext)
                        }
                    },
                    availableActivities = viewModel.availableActivities
                )

                // On/Off Controls
                PopupControls(
                    isEnabled = isPopupEnabled,
                    onToggle = { enabled ->
                        if (enabled) {
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
                                viewModel.togglePopupService(true, localContext)
                            }
                        } else {
                            viewModel.togglePopupService(false, localContext)
                        }
                    },
                    selectedActivity = selectedActivity
                )

                // Test Button
                TestButton(
                    onTest = {
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
                            viewModel.testPopup(localContext)
                        }
                    }
                )

                // Extra space at the bottom to account for profile button
                Spacer(modifier = Modifier.height(80.dp))
            }

            ProfileButton(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.BottomEnd)  // Position in bottom right
                    .padding(end = 24.dp, bottom = 32.dp)  // Move slightly left (end padding) and up (bottom padding)
            )
        }
    }
}

// Smaller composables for better readability and maintainability
@Composable
private fun RobotHeader() {
    Image(
        painter = painterResource(id = R.drawable.robot),
        contentDescription = "Robot mascot",
        modifier = Modifier
            .size(180.dp)
            .padding(top = 24.dp, bottom = 24.dp)
    )

    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
private fun TopicSelector(
    selectedTopic: String,
    isExpanded: Boolean,
    onToggleDropdown: () -> Unit,
    onTopicSelected: (String) -> Unit,
    availableTopics: List<String>
) {
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
                onClick = onToggleDropdown,
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
                expanded = isExpanded,
                onDismissRequest = onToggleDropdown,
                modifier = Modifier
                    .width(200.dp)
                    .background(Color.White)
            ) {
                availableTopics.forEach { topic ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = topic,
                                fontSize = 24.sp,
                                fontFamily = AppTheme.pixelFontFamily
                            )
                        },
                        onClick = { onTopicSelected(topic) }
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun ActivitySelector(
    selectedActivity: String,
    isExpanded: Boolean,
    onToggleDropdown: () -> Unit,
    onActivitySelected: (String) -> Unit,
    availableActivities: List<String>
) {
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
                onClick = onToggleDropdown,
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
                expanded = isExpanded,
                onDismissRequest = onToggleDropdown,
                modifier = Modifier
                    .width(200.dp)
                    .background(Color.White)
            ) {
                availableActivities.forEach { activity ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = activity,
                                fontSize = 24.sp,
                                fontFamily = AppTheme.pixelFontFamily
                            )
                        },
                        onClick = { onActivitySelected(activity) }
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(64.dp))
}

@Composable
private fun PopupControls(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    selectedActivity: String
) {
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
                text = "Off/On",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = AppTheme.pixelFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4B89DC),
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color(0xFF444444)
                ),
                modifier = Modifier.size(width = 80.dp, height = 48.dp)
            )

            if (isEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "glhf",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontFamily = AppTheme.pixelFontFamily,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
private fun TestButton(onTest: () -> Unit) {
    Button(
        onClick = onTest,
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
}

@Composable
private fun ProfileButton(
    navController: NavController,
    modifier: Modifier = Modifier  // Default empty modifier if none provided
) {
    Button(
        onClick = {
            navController.navigate("profile")
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
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
}