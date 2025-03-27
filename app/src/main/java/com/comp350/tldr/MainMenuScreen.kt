package com.comp350.tldr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
        mutableStateOf(sharedPrefs.getInt("selected_topic", 0))
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

    val coroutineScope = rememberCoroutineScope()

    var isVideoPlaying by remember { mutableStateOf(false) }

    // List of available topics
    val topics = listOf(
        "Python Basics" to "Learn fundamental Python programming concepts and syntax.",
        "Variables & Data Types" to "Understanding how to store and manipulate data in Python.",
        "Control Flow" to "Learn about conditionals and loops in Python.",
    )

    // List of available intervals (in minutes)
    val intervals = listOf(0.5, 2.0, 5.0, 10.0)
    // List of max question counts
    val questionCounts = listOf(3, 5, 10, 20)

    // Check service status on init
    LaunchedEffect(Unit) {
        // If service should be running but isn't, restart it
        if (isQuizModeEnabled) {
            startPopQuizService(
                context,
                topics[selectedTopic].first,
                intervals[selectedInterval],
                maxQuestions
            )
        }
    }

    Box {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            Text(
                text = "TLDR Learning",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isVideoPlaying) {
                VideoPlayer(modifier = Modifier.padding(vertical = 16.dp),
                    onVideoFinished = { isVideoPlaying = false })
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
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Topic selection section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF003048).copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select a Learning Topic",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Topic options
                    topics.forEachIndexed { index, (title, _) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = selectedTopic == index,
                                onClick = {
                                    selectedTopic = index
                                    // Save selection
                                    sharedPrefs.edit().putInt("selected_topic", index).apply()

                                    // Update service if running
                                    if (isQuizModeEnabled) {
                                        startPopQuizService(
                                            context,
                                            topics[selectedTopic].first,
                                            intervals[selectedInterval],
                                            maxQuestions
                                        )
                                    }
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF4CAF50)
                                )
                            )
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    // Topic description
                    Text(
                        text = topics[selectedTopic].second,
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 8.dp, start = 8.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Quiz settings section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF003048).copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Pop Quiz Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interval selection
                    Text(
                        text = "Quiz Interval:",
                        fontSize = 16.sp,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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
                                                topics[selectedTopic].first,
                                                intervals[selectedInterval],
                                                maxQuestions
                                            )
                                        }
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF4CAF50)
                                    )
                                )
                                Text(
                                    text = if (interval < 1) "${(interval * 60).toInt()} sec" else "${interval.toInt()} min",
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Question count selection
                    Text(
                        text = "Number of Questions:",
                        fontSize = 16.sp,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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
                                                topics[selectedTopic].first,
                                                intervals[selectedInterval],
                                                maxQuestions
                                            )
                                        }
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF4CAF50)
                                    )
                                )
                                Text(
                                    text = "$count",
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Enable/Disable Pop Quiz mode
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Pop Quiz Mode",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )

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
                                            topics[selectedTopic].first,
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
                                checkedThumbColor = Color(0xFF4CAF50),
                                checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                            )
                        )
                    }

                    if (isQuizModeEnabled) {
                        Text(
                            text = "Robot will ask you ${maxQuestions} questions every ${if (intervals[selectedInterval] < 1) "${(intervals[selectedInterval] * 60).toInt()} seconds" else "${intervals[selectedInterval].toInt()} minutes"}",
                            fontSize = 14.sp,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Practice button
            Button(
                onClick = {
                    navController.navigate("quiz_page")
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
                    text = "Practice Now",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
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