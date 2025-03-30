// ui/theme/AppTheme.kt
package com.comp350.tldr.view.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.comp350.tldr.R
import androidx.compose.ui.geometry.Offset

object AppTheme {
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
            offset = Offset(6f, 6f)
        )
    )

    // Smaller text style with less dramatic shadow
    val pixelTextStyleSmall = TextStyle(
        fontFamily = pixelFontFamily,
        shadow = Shadow(
            color = Color.Black,
            blurRadius = 1f,
            offset = Offset(2f, 2f)
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

    // Button Colors
    val darkBlueButtonColor = Color(0xFF1A237E)
    val blueButtonColor = Color(0xFF1E88E5)
    val whiteColor = Color.White
}