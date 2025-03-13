package com.comp350.tldr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
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
import com.comp350.tldr.Question





@Composable
fun QuizPage(navController: NavController) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    //Initializes the string as empty, meaning that no answer has been selected
    var selectedAnswerIndex by remember { mutableStateOf(-1) }
    //Hides the dialog at the start of the quiz
    var showDialog by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false)}
    var score by remember { mutableStateOf(0) }

    // Empty list to be filled with quiz content later
    val questions = listOf(
        Question(
            "What are variables used for?",
            listOf("To store data", "To print messages", "To create loops", "To define classes"),
            0
        ),
        Question(
            "What is the correct form to name a variable with multiple words?",
            listOf("snake_case", "PascalCase", "camelCase", "UPPER_CASE"),
            0
        ),
        Question(
            "What's wrong with this variable name: myvariable?",
            listOf("It's too short", "It does not have an underscore", "It has spaces", "It uses lowercase letters"),
            1
        ),
        Question(
            "How do you create a variable and store information inside of it?",
            listOf(
                "let x = 10",
                "var x = 10",
                "variable x = 10",
                "x = 10"
            ),
            3
        ),
        Question(
            "What is the correct way to store a string in a variable?",
            listOf(
                "name = 'John'",
                "name = \"John\"",
                "name = John",
                "name = var 'John'"
            ),
            1
        ),
        Question(
            "Create a job variable and give it the value \"Plumber\".",
            listOf(
                "var job = Plumber",
                "job = \"Plumber\"",
                "var job = \"Plumber\"",
                "job: var = \"Plumber\""
            ),
            1
        ),
        // Python questions added below
        Question(
            "What keyword is used to define a function in Python?",
            listOf("function", "def", "define", "func"),
            1
        ),
        Question(
            "How do you print text in Python?",
            listOf(
                "echo(\"Hello\")",
                "console.log(\"Hello\")",
                "System.out.println(\"Hello\")",
                "print(\"Hello\")"
            ),
            3
        ),
        Question(
            "Which of the following is a valid variable name in Python?",
            listOf("2name", "my_variable", "my-variable", "my variable"),
            1
        ),
        Question(
            "What is the correct way to start a comment in Python?",
            listOf(
                "// This is a comment",
                "<!-- This is a comment -->",
                "# This is a comment",
                "/* This is a comment */"
            ),
            2
        )
    )

    Image(
        painter = painterResource(id = R.drawable.splash_background),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )

    if (currentQuestionIndex >= questions.size) {
        //End of quiz
        LaunchedEffect(Unit) {
            navController.navigate("results_page/$score/${questions.size}") {
                popUpTo("quiz_page") { inclusive = true}
            }
        }
    } else {
        val question = questions[currentQuestionIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = question.text,
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            question.options.forEachIndexed{ index, answer ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedAnswerIndex = index }
                        .padding(8.dp)
                        .background(if (selectedAnswerIndex == index) Color.LightGray else Color.Transparent)
                        .padding(8.dp)
                ) {
                    RadioButton(
                        selected = (selectedAnswerIndex == index),
                        onClick = { selectedAnswerIndex = index },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.Blue,     // Selected option turns Blue
                            unselectedColor = Color.LightGray // Unselected options are Light Gray
                        )
                    )
                    Text(
                        text = answer,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isCorrect = selectedAnswerIndex == question.correctAnswerIndex
                    if(isCorrect) score++
                    showDialog = true
                },
                enabled = selectedAnswerIndex != -1
            ) {
                Text(text = "Submit Answer")
            }

            if(showDialog) {
                Dialog(onDismissRequest = { showDialog = false}) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isCorrect) "Correct!" else "Wrong!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = if(isCorrect) Color(0xFF009933) else Color(0xFFcc0000)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                showDialog = false
                                selectedAnswerIndex = -1
                                currentQuestionIndex++
                            }) {
                                Text("Next Question")
                            }
                        }
                    }
                }
            }
        }
    }

}
