package com.comp350.tldr.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.comp350.tldr.R

@Composable
fun RobotWithCustomization(
    isWearingSunglasses: Boolean,
    size: Int,
    sunglassesOffsetY: Int
) {
    Box(contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.robot),
            contentDescription = "Robot",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(size.dp)
        )

        if (isWearingSunglasses) {

            val calculatedOffset = when (size) {
                in 0..40 -> sunglassesOffsetY / 3
                in 41..80 -> sunglassesOffsetY / 2
                in 81..120 -> sunglassesOffsetY * 2 / 3
                else -> sunglassesOffsetY
            }

            Image(
                painter = painterResource(id = R.drawable.sunglasses),
                contentDescription = "Sunglasses",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(size.dp)
                    .offset(y = calculatedOffset.dp)
            )
        }
    }
}