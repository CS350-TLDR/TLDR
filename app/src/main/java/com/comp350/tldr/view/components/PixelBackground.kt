package com.comp350.tldr.view.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.comp350.tldr.view.theme.AppTheme

@Composable
fun PixelBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.gradientBackground)
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
                        topLeft = Offset(left, top),
                        size = Size(pixelSize, pixelSize)
                    )
                }
            }
        }

        // Content
        content()
    }
}