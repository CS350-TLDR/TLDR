package com.comp350.tldr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Profile screen with back button
@Composable
fun ProfileScreen(navController: NavController) {
    // Define the pixel font family - same as other screens
    val pixelFontFamily = FontFamily(
        Font(R.font.rainyhearts, FontWeight.Normal)
    )

    // Text style with pixel font and outline
    val pixelTextStyle = TextStyle(
        fontFamily = pixelFontFamily,
        shadow = Shadow(
            color = Color.Black,
            blurRadius = 2f,
            offset = androidx.compose.ui.geometry.Offset(6f, 6f)
        )
    )

    // Create blue to dark blue gradient background - same as other screens
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF4B89DC),  // Light blue color
            Color(0xFF3568CC),  // Medium blue
            Color(0xFF1A237E)   // Dark blue color
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        // Pixelated overlay effect - same as other screens
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

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Title
            Text(
                text = "My Profile",
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = pixelTextStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )

            // Profile content placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF003048).copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Profile Screen\n(Work in Progress)",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontFamily = pixelFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Back button
            Button(
                onClick = {
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Back to Main Menu",
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
        }
    }
}