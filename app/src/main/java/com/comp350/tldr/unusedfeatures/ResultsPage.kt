package com.comp350.tldr.unusedfeatures

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ResultsPage(navController: NavController, score: Int, totalQuestions: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quiz Completed!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Show Score
        Text(
            text = "You scored $score out of $totalQuestions",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Play Again Button
        Button(onClick = {
            navController.navigate("quiz_page") {
                popUpTo("main_menu") {inclusive = false}
                launchSingleTop = true
            }
        }) {
            Text(text = "Play Again")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Return to Main Menu Button
        Button(onClick = { navController.popBackStack() }) {
            Text(text = "Return to Main Menu")
        }
    }
}