package com.comp350.tldr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

@Composable
fun MainMenuScreen(navController: NavController, context: Context) {
    // Shared preferences for persistence
    val sharedPrefs = remember { context.getSharedPreferences("tldr_prefs", MODE_PRIVATE) }

    // State variables with persisted values
    var selectedTopic by remember {
        mutableStateOf("Python")
    }
    var selectedInterval by remember {
        mutableStateOf(sharedPrefs.getInt("selected_interval", 2))
    }
    var isQuizModeEnabled by remember {
        mutableStateOf(sharedPrefs.getBoolean("quiz_mode_enabled", false))
    }
    var maxQuestions by remember {
        mutableStateOf(sharedPrefs.getInt("max_questions", 5))
    }

    // Dropdown state
    var expanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var isVideoPlaying by remember { mutableStateOf(false) }

    // List of available activities
    val activities = listOf("Python")

    // List of available intervals (in minutes)
    val intervals = listOf(0.5, 2.0, 5.0, 10.0)
    // List of max question counts
    val questionCounts = listOf(3, 5, 10, 20)

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

    // Smaller text style with less dramatic shadow
    val pixelTextStyleSmall = TextStyle(
        fontFamily = pixelFontFamily,
        shadow = Shadow(
            color = Color.Black,
            blurRadius = 1f,
            offset = androidx.compose.ui.geometry.Offset(2f, 2f)
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

    // Check service status on init
    LaunchedEffect(Unit) {
        // If service should be running but isn't, restart it
        if (isQuizModeEnabled) {
            startPopQuizService(
                context,
                selectedTopic,
                intervals[selectedInterval],
                maxQuestions
            )
        }
    }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            Text(
                text = "TLDR Learning",
                fontSize = 70.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = pixelTextStyle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center
            )

            // Profile Button at the top
            Button(
                onClick = {
                    navController.navigate("profile")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text(
                    text = "My Profile",
                    fontSize = 26.sp,
                    color = Color(0xFF1A237E),
                    style = pixelTextStyle.copy(
                        shadow = Shadow(
                            color = Color.Black,
                            blurRadius = 1f,
                            offset = androidx.compose.ui.geometry.Offset(2f, 2f)
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isVideoPlaying) {
                VideoPlayer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    onVideoFinished = { isVideoPlaying = false }
                )
            }

            // Watch Video button
            Button(
                onClick = {
                    isVideoPlaying = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Python Intro Lesson",
                    fontSize = 26.sp,
                    color = Color.White,
                    style = pixelTextStyleSmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Activities dropdown in the middle of the screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Activities",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = pixelTextStyle,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box {
                        Button(
                            onClick = { expanded = true },
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
                                color = Color(0xFF1A237E),
                                style = pixelTextStyleSmall,
                                textAlign = TextAlign.Center
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
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
                                            fontFamily = pixelFontFamily
                                        )
                                    },
                                    onClick = {
                                        selectedTopic = activity
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Preferences section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF003048).copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Preferences",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = pixelTextStyle,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Interval selection
                    Text(
                        text = "Interval:",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontFamily = pixelFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        intervals.forEachIndexed { index, interval ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                RadioButton(
                                    selected = selectedInterval == index,
                                    onClick = {
                                        selectedInterval = index
                                        // Save selection
                                        sharedPrefs.edit().putInt("selected_interval", index).apply()

                                        // Update service if running
                                        if (isQuizModeEnabled) {
                                            startPopQuizService(
                                                context,
                                                selectedTopic,
                                                intervals[selectedInterval],
                                                maxQuestions
                                            )
                                        }
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color.White
                                    )
                                )
                                Text(
                                    text = if (interval < 1) "${(interval * 60).toInt()} sec" else "${interval.toInt()} min",
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    fontFamily = pixelFontFamily
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question count selection
                    Text(
                        text = "Quantity:",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontFamily = pixelFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        questionCounts.forEachIndexed { index, count ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                RadioButton(
                                    selected = maxQuestions == count,
                                    onClick = {
                                        maxQuestions = count
                                        // Save selection
                                        sharedPrefs.edit().putInt("max_questions", count).apply()

                                        // Update service if running
                                        if (isQuizModeEnabled) {
                                            startPopQuizService(
                                                context,
                                                selectedTopic,
                                                intervals[selectedInterval],
                                                maxQuestions
                                            )
                                        }
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color.White
                                    )
                                )
                                Text(
                                    text = "$count",
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    fontFamily = pixelFontFamily
                                )
                            }
                        }
                    }
                }
            }

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
                        text = "On/Off",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = pixelFontFamily,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Switch(
                        checked = isQuizModeEnabled,
                        onCheckedChange = { isEnabled ->
                            if (isEnabled) {
                                // Check for overlay permission before enabling
                                if (!Settings.canDrawOverlays(context)) {
                                    // Request permission
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                                    )
                                    context.startActivity(intent)
                                    Toast.makeText(
                                        context,
                                        "Please grant overlay permission to enable Pop Quiz mode",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    isQuizModeEnabled = true
                                    // Save state
                                    sharedPrefs.edit().putBoolean("quiz_mode_enabled", true).apply()

                                    startPopQuizService(
                                        context,
                                        selectedTopic,
                                        intervals[selectedInterval],
                                        maxQuestions
                                    )
                                }
                            } else {
                                isQuizModeEnabled = false
                                // Save state
                                sharedPrefs.edit().putBoolean("quiz_mode_enabled", false).apply()

                                stopPopQuizService(context)
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

                    if (isQuizModeEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Robot will ask you ${maxQuestions} questions every ${if (intervals[selectedInterval] < 1) "${(intervals[selectedInterval] * 60).toInt()} seconds" else "${intervals[selectedInterval].toInt()} minutes"}",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontFamily = pixelFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Extra space at the bottom
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Helper function to start the pop quiz service
private fun startPopQuizService(context: Context, topic: String, intervalMinutes: Double, maxQuestions: Int) {
    val intent = Intent(context, PopQuizService::class.java).apply {
        putExtra("topic", topic)
        putExtra("interval", (intervalMinutes * 60 * 1000).toLong()) // Convert to milliseconds
        putExtra("maxQuestions", maxQuestions)
        action = "START_SERVICE"
    }
    context.startService(intent)
}

// Helper function to stop the pop quiz service
private fun stopPopQuizService(context: Context) {
    val intent = Intent(context, PopQuizService::class.java).apply {
        action = "STOP_SERVICE"
    }
    context.startService(intent)
}