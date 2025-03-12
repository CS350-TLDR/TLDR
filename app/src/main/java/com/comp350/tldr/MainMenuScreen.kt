package com.comp350.tldr

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController

@Composable
fun MainMenuScreen(navController: NavController) {
    // State for the lesson popup
    var showLessonDialog by remember { mutableStateOf(false) }
    var selectedLesson by remember { mutableStateOf(0) }

    // Lesson titles and descriptions
    val lessons = listOf(
        "Variables & Data Types" to "Learn about different types of data and how to store them in variables.",

    )

    Box()
        {

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
                text = "Choose a Lesson",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center
            )

            // Lesson buttons grid
            lessons.forEachIndexed { index, (title, _) ->
                Button(
                    onClick = {
                        selectedLesson = index
                        showLessonDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Lesson ${index + 1}: $title",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(50.dp))
        }

        // Lesson info popup dialog
        if (showLessonDialog) {
            Dialog(onDismissRequest = { showLessonDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Lesson title
                        Text(
                            text = "Lesson ${selectedLesson + 1}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF003048)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Lesson name
                        Text(
                            text = lessons[selectedLesson].first,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF003048)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Lesson description
                        Text(
                            text = lessons[selectedLesson].second,
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Start lesson button
                        Button(
                            onClick = {
                                showLessonDialog = false
                                // Navigate to the lesson screen
                                navController.navigate("quiz_page")//${selectedLesson + 1}")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50) // Green
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Start Lesson",
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cancel button
                        TextButton(
                            onClick = { showLessonDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}