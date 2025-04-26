package com.comp350.tldr.view.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.comp350.tldr.view.RobotWithCustomization
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import com.comp350.tldr.controller.navigation.NavigationController
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.comp350.tldr.R
import com.comp350.tldr.controller.viewmodels.MainMenuViewModel
import com.comp350.tldr.view.components.PixelBackground
import com.comp350.tldr.view.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainMenuScreen(navController: NavController, vm: MainMenuViewModel = viewModel()) {
    val ctx = LocalContext.current
    val topic by vm.topic.collectAsState()
    val activity by vm.activity.collectAsState()
    val interval by vm.interval.collectAsState()
    val enabled by vm.popupEnabled.collectAsState()
    val timeRemaining by vm.timeRemaining.collectAsState()
    val streak by vm.streak.collectAsState()
    val isWearingSunglasses by vm.isWearingSunglasses.collectAsState()

    var toggleCooldown by remember { mutableStateOf(false) }
    var showStreakDialog by remember { mutableStateOf(false) }
    var streakReward by remember { mutableStateOf(0) }

    var topicOpen by remember { mutableStateOf(false) }
    var activityOpen by remember { mutableStateOf(false) }
    var intervalOpen by remember { mutableStateOf(false) }
    var profileMenuOpen by remember { mutableStateOf(false) }

    val navigationController = remember { NavigationController(navController) }

    LaunchedEffect(Unit) {
        vm.loadUserData(ctx)
        vm.initStreakManager(ctx)
        vm.checkDailyStreak(ctx) { reward ->
            if (reward > 0) {
                streakReward = reward
                showStreakDialog = true
            }
        }
    }

    PixelBackground {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                PopupToggle(
                    enabled = enabled,
                    activity = activity,
                    cooldownActive = toggleCooldown,
                    onToggle = { toggled ->
                        if (toggled && !Settings.canDrawOverlays(ctx)) {
                            ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                            Toast.makeText(ctx, "Grant overlay permission for popups", Toast.LENGTH_LONG).show()
                        } else {
                            if (toggled) {
                                toggleCooldown = true
                                kotlinx.coroutines.MainScope().launch {
                                    delay(5000)
                                    toggleCooldown = false
                                }
                            }
                            vm.togglePopup(toggled, ctx)
                        }
                    }
                )

                if (enabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CountdownTimer(timeRemaining)
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 16.dp)
            ) {
                DailyStreakCounter(streak)
            }

            // Profile icon in top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
                    .zIndex(10f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White, shape = CircleShape)
                        .clickable {
                            profileMenuOpen = !profileMenuOpen
                        }
                ) {
                    RobotWithCustomization(
                        isWearingSunglasses = isWearingSunglasses,
                        size = 40,
                        sunglassesOffsetY = -9
                    )

                }

                DropdownMenu(
                    expanded = profileMenuOpen,
                    onDismissRequest = { profileMenuOpen = false },
                    modifier = Modifier
                        .width(150.dp)
                        .background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Profile",
                                fontSize = 18.sp,
                                fontFamily = AppTheme.pixelFontFamily
                            )
                        },
                        onClick = {
                            profileMenuOpen = false
                            navController.navigate("profile")
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Logout",
                                fontSize = 18.sp,
                                fontFamily = AppTheme.pixelFontFamily
                            )
                        },
                        onClick = {
                            profileMenuOpen = false
                            navigationController.navigateToWelcome()
                        }
                    )
                }
            }

            if (showStreakDialog) {
                StreakRewardDialog(
                    streak = streak,
                    reward = streakReward,
                    onDismiss = { showStreakDialog = false }
                )
            }
        }
    }
}

@Composable
private fun DailyStreakCounter(streak: Int) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .background(Color.White, shape = CircleShape)
            .clickable {
                showDialog = true
            }
    ) {
        Text(
            text = "$streak",
            color = Color.Black,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AppTheme.pixelFontFamily
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {},
            text = {
                Text(
                    "Daily Streak: Day $streak",
                    fontSize = 20.sp,
                    fontFamily = AppTheme.pixelFontFamily
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK", fontFamily = AppTheme.pixelFontFamily)
                }
            }
        )
    }
}

@Composable
private fun StreakRewardDialog(streak: Int, reward: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Daily Streak: Day $streak!",
                fontFamily = AppTheme.pixelFontFamily,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "You've logged in for $streak consecutive days!",
                    fontFamily = AppTheme.pixelFontFamily,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "+$reward GEARS",
                    fontFamily = AppTheme.pixelFontFamily,
                    fontSize = 32.sp,
                    color = Color(0xFFFFC107),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.blueButtonColor)
            ) {
                Text(
                    "Awesome!",
                    fontFamily = AppTheme.pixelFontFamily,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        },
        containerColor = Color(0xFF333333),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@Composable
private fun PopupToggle(
    enabled: Boolean,
    activity: String,
    cooldownActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            "Off/On",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = AppTheme.pixelFontFamily
        )
        Spacer(Modifier.height(8.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp, 48.dp)
        ) {
            Switch(
                checked = enabled,
                onCheckedChange = {
                    if (!enabled || !cooldownActive) {
                        onToggle(!enabled)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4B89DC),
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color(0xFF444444),
                    disabledCheckedThumbColor = Color.LightGray,
                    disabledCheckedTrackColor = Color(0xFF7AA8E8)
                ),
                enabled = !cooldownActive || !enabled,
                modifier = Modifier.size(80.dp, 48.dp)
            )

            if (cooldownActive && enabled) {
                val cooldownSeconds = remember { mutableStateOf(5) }

                LaunchedEffect(cooldownActive) {
                    for (i in 5 downTo 1) {
                        cooldownSeconds.value = i
                        delay(1000)
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp, 48.dp)
                        .background(Color(0x80000000), shape = RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = "${cooldownSeconds.value}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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

            if (enabled && expanded) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = onExpandChange,
                    modifier = Modifier
                        .width(200.dp)
                        .background(Color.White)
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

