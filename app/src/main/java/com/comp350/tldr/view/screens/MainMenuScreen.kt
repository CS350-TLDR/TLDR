package com.comp350.tldr.view.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.comp350.tldr.R
import com.comp350.tldr.controller.navigation.NavigationController
import com.comp350.tldr.controllers.QuizController
import com.comp350.tldr.view.components.PixelBackground
import com.comp350.tldr.view.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.comp350.tldr.controller.viewmodels.MainMenuViewModel

@Composable
fun MainMenuScreen(navController: NavController, vm: MainMenuViewModel = viewModel()) {
    val ctx = LocalContext.current
    val topic by vm.topic.collectAsState()
    val activity by vm.activity.collectAsState()
    val interval by vm.interval.collectAsState()
    val enabled by vm.popupEnabled.collectAsState()
    val timeRemaining by vm.timeRemaining.collectAsState()

    var topicOpen by remember { mutableStateOf(false) }
    var activityOpen by remember { mutableStateOf(false) }
    var intervalOpen by remember { mutableStateOf(false) }

    PixelBackground {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Robot header with smaller size and padding
                Image(
                    painterResource(R.drawable.robot),
                    null,
                    Modifier.size(160.dp).padding(16.dp)
                )
                Spacer(Modifier.height(16.dp))

                // Topic dropdown with disabled state
                DropdownSelector(
                    label = "Topic",
                    selected = topic,
                    expanded = topicOpen,
                    onExpandChange = { if (!enabled) topicOpen = !topicOpen },
                    options = vm.topics,
                    enabled = !enabled,
                    onSelect = {
                        vm.setTopic(it)
                        topicOpen = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Activity dropdown with disabled state
                DropdownSelector(
                    label = "Activity",
                    selected = activity,
                    expanded = activityOpen,
                    onExpandChange = { if (!enabled) activityOpen = !activityOpen },
                    options = vm.activities,
                    enabled = !enabled,
                    onSelect = {
                        vm.setActivity(it)
                        activityOpen = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Interval dropdown with disabled state
                DropdownSelector(
                    label = "Interval",
                    selected = interval,
                    expanded = intervalOpen,
                    onExpandChange = { if (!enabled) intervalOpen = !intervalOpen },
                    options = vm.intervals,
                    enabled = !enabled,
                    onSelect = {
                        vm.setInterval(it, ctx)
                        intervalOpen = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PopupToggle(enabled, activity) { toggled ->
                    if (toggled && !Settings.canDrawOverlays(ctx)) {
                        ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                        Toast.makeText(ctx, "Grant overlay permission for popups", Toast.LENGTH_LONG).show()
                    } else {
                        vm.togglePopup(toggled, ctx)
                    }
                }

                Spacer(Modifier.height(60.dp))
            }

            // Countdown timer in top right
            if (enabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                ) {
                    CountdownTimer(timeRemaining)
                }
            }

            ProfileButton(navController)
        }
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    selected: String,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    options: List<String>,
    enabled: Boolean,
    onSelect: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = AppTheme.pixelTextStyle
        )
        Spacer(Modifier.height(4.dp))
        Box {
            // Button with disabled state styling
            Button(
                onClick = onExpandChange,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color.Gray
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                enabled = enabled
            ) {
                Text(
                    selected,
                    fontSize = 24.sp,
                    color = if (enabled) AppTheme.darkBlueButtonColor else Color.DarkGray,
                    style = AppTheme.pixelTextStyleSmall
                )
            }

            // Only show dropdown if enabled
            if (enabled && expanded) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = onExpandChange,
                    modifier = Modifier.width(200.dp).background(Color.White)
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option,
                                    fontSize = 22.sp,
                                    fontFamily = AppTheme.pixelFontFamily
                                )
                            },
                            onClick = { onSelect(option) }
                        )
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}


// New composable for the countdown timer
@Composable
private fun CountdownTimer(timeRemaining: Long) {
    val minutes = timeRemaining / 60000
    val seconds = (timeRemaining % 60000) / 1000

    Text(
        text = String.format("%02d:%02d", minutes, seconds),
        color = Color.White,
        fontSize = 24.sp,
        fontFamily = AppTheme.pixelFontFamily,
        modifier = Modifier
            .background(
                color = AppTheme.darkBlueButtonColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}


@Composable
private fun PopupToggle(enabled: Boolean, activity: String, onToggle: (Boolean) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp) // Reduced vertical padding
    ) {
        Text(
            "Off/On",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = AppTheme.pixelFontFamily
        )
        Spacer(Modifier.height(8.dp)) // Reduced spacing
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4B89DC),
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color(0xFF444444)
            ),
            modifier = Modifier.size(80.dp, 48.dp)
        )
        if (enabled) {
            Spacer(Modifier.height(8.dp)) // Reduced spacing
            Text("glhf", fontSize = 18.sp, color = Color.White, fontFamily = AppTheme.pixelFontFamily)
        }
    }
}

@Composable
private fun ProfileButton(navController: NavController) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(end = 24.dp, bottom = 24.dp), // Reduced bottom padding
        contentAlignment = Alignment.BottomEnd
    ) {
        Button(
            onClick = { navController.navigate("profile") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(width = 120.dp, height = 50.dp)
        ) {
            Text(
                "Profile",
                fontSize = 22.sp,
                color = AppTheme.darkBlueButtonColor,
                style = AppTheme.pixelTextStyle.copy(
                    shadow = AppTheme.pixelTextStyle.shadow?.copy(
                        blurRadius = 1f,
                        offset = androidx.compose.ui.geometry.Offset(2f, 2f)
                    )
                )
            )
        }
    }
}