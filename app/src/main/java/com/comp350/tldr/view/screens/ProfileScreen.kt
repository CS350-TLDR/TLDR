package com.comp350.tldr.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.comp350.tldr.R
import com.comp350.tldr.controller.navigation.NavigationController
import com.comp350.tldr.controller.viewmodels.ProfileViewModel
import com.comp350.tldr.view.components.PixelBackground
import com.comp350.tldr.view.theme.AppTheme
import androidx.compose.ui.geometry.Offset

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val navigationController = NavigationController(navController)

    // Collect states
    val nickname by viewModel.nickname.collectAsState()
    val questionsAnswered by viewModel.questionsAnswered.collectAsState()
    val gears by viewModel.gears.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Local state for editing
    var editingNickname by remember { mutableStateOf("") }

    // Load user data
    LaunchedEffect(key1 = true) {
        viewModel.loadUserData(context)
    }

    // Update local state when editing starts
    LaunchedEffect(key1 = isEditing) {
        if (isEditing) {
            editingNickname = nickname
        }
    }

    PixelBackground {
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
                text = "Profile",
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = AppTheme.pixelTextStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )

            // Robot image
            Image(
                painter = painterResource(id = R.drawable.robot),
                contentDescription = "Robot character",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            // Nickname section
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nickname",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontFamily = AppTheme.pixelFontFamily,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditing) {
                        // Editing mode
                        TextField(
                            value = editingNickname,
                            onValueChange = { editingNickname = it },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = AppTheme.pixelFontFamily,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = { viewModel.toggleEditing() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Blue.copy(alpha = 0.7f)
                                )
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = Color.White,
                                    fontFamily = AppTheme.pixelFontFamily
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Button(
                                onClick = { viewModel.updateNickname(context, editingNickname) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Green.copy(alpha = 0.7f)
                                )
                            ) {
                                Text(
                                    text = "Save",
                                    color = Color.White,
                                    fontFamily = AppTheme.pixelFontFamily
                                )
                            }
                        }
                    } else {
                        // Display mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = nickname,
                                fontSize = 28.sp,
                                color = Color.White,
                                fontFamily = AppTheme.pixelFontFamily,
                                textAlign = TextAlign.Center
                            )

                            IconButton(
                                onClick = { viewModel.toggleEditing() },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Nickname",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Stats Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Questions Answered Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.blueButtonColor
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = questionsAnswered.toString(),
                            fontSize = 40.sp,
                            color = Color.White,
                            fontFamily = AppTheme.pixelFontFamily,
                            style = AppTheme.pixelTextStyle,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Questions Answered",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontFamily = AppTheme.pixelFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Gears Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800)  // Orange color for contrast
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = gears.toString(),
                            fontSize = 40.sp,
                            color = Color.White,
                            fontFamily = AppTheme.pixelFontFamily,
                            style = AppTheme.pixelTextStyle,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Gears",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontFamily = AppTheme.pixelFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Placeholder for future stats or achievements
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
                        text = "Experience (WIP)",
                        fontSize = 28.sp,
                        color = Color.White,
                        fontFamily = AppTheme.pixelFontFamily,
                        style = AppTheme.pixelTextStyle,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = questionsAnswered / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                        color = AppTheme.blueButtonColor,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${questionsAnswered}/100 Questions to next level",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontFamily = AppTheme.pixelFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Back button
            Button(
                onClick = {
                    navigationController.navigateBack()
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
                    color = AppTheme.darkBlueButtonColor,
                    style = AppTheme.pixelTextStyle.copy(
                        shadow = AppTheme.pixelTextStyle.shadow?.copy(
                            blurRadius = 1f,
                            offset = Offset(2f, 2f)
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}