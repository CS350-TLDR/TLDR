package com.comp350.tldr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
fun QuizPage(navController: NavController) {
    //Initializes the string as empty, meaning that no answer has been selected
    var selectedAnswer by remember { mutableStateOf("") }
    //Hides the dialog at the start of the quiz
    var showDialog by remember { mutableStateOf(false) }

    // Empty list to be filled with quiz content later
    val options = listOf<String>()

        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Quiz Title
            Text(
                text = "Welcome to the Quiz!",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center
            )

            // Empty Quiz Placeholder
            if (options.isEmpty()) {
                Text(
                    text = "No quiz content available.",
                    fontSize = 16.sp,
                    color = Color(0xFF003048),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Quiz Options
                options.forEach { answer ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAnswer = answer }
                            .padding(8.dp)
                            .background(if (selectedAnswer == answer) Color.LightGray else Color.Transparent)
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = (selectedAnswer == answer),
                            onClick = { selectedAnswer = answer }
                        )
                        Text(
                            text = answer,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = options.isNotEmpty()  // Only enable if options exist
            ) {
                Text(text = "Submit Answer")
            }

            // Dialog for Quiz Feedback
            if (showDialog) {
                Dialog(onDismissRequest = { showDialog = false }) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Quiz submission recorded.",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { showDialog = false }) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }

