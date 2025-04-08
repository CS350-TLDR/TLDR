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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                ProfileScreenTitle()
                ProfileRobotImage()
                NicknameSection(
                    nickname = nickname,
                    isEditing = isEditing,
                    editingNickname = editingNickname,
                    onEditingNicknameChange = { editingNickname = it },
                    onToggleEditing = { viewModel.toggleEditing() },
                    onUpdateNickname = { viewModel.updateNickname(context, editingNickname) }
                )
                GearsStatCard(gears = gears)
                Spacer(modifier = Modifier.weight(1f))
                BackToMainMenuButton(navigationController)
                Spacer(modifier = Modifier.height(16.dp))
            }

            ProfileLoadingOverlay(isLoading)
        }
    }
}

@Composable
private fun ProfileScreenTitle() {
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
}

@Composable
private fun ProfileRobotImage() {
    Image(
        painter = painterResource(id = R.drawable.robot),
        contentDescription = "Robot character",
        modifier = Modifier
            .size(120.dp)
            .padding(bottom = 16.dp)
    )
}

@Composable
private fun NicknameSection(
    nickname: String,
    isEditing: Boolean,
    editingNickname: String,
    onEditingNicknameChange: (String) -> Unit,
    onToggleEditing: () -> Unit,
    onUpdateNickname: () -> Unit
) {
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
            NicknameSectionTitle()
            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing) {
                NicknameEditingMode(
                    editingNickname = editingNickname,
                    onEditingNicknameChange = onEditingNicknameChange,
                    onToggleEditing = onToggleEditing,
                    onUpdateNickname = onUpdateNickname
                )
            } else {
                NicknameDisplayMode(
                    nickname = nickname,
                    onToggleEditing = onToggleEditing
                )
            }
        }
    }
}

@Composable
private fun NicknameSectionTitle() {
    Text(
        text = "Nickname",
        fontSize = 24.sp,
        color = Color.White,
        fontFamily = AppTheme.pixelFontFamily,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun NicknameEditingMode(
    editingNickname: String,
    onEditingNicknameChange: (String) -> Unit,
    onToggleEditing: () -> Unit,
    onUpdateNickname: () -> Unit
) {
    NicknameTextField(
        editingNickname = editingNickname,
        onEditingNicknameChange = onEditingNicknameChange
    )

    Spacer(modifier = Modifier.height(8.dp))

    NicknameEditButtons(
        onToggleEditing = onToggleEditing,
        onUpdateNickname = onUpdateNickname
    )
}

@Composable
private fun NicknameTextField(
    editingNickname: String,
    onEditingNicknameChange: (String) -> Unit
) {
    TextField(
        value = editingNickname,
        onValueChange = onEditingNicknameChange,
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
}

@Composable
private fun NicknameEditButtons(
    onToggleEditing: () -> Unit,
    onUpdateNickname: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onToggleEditing,
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
            onClick = onUpdateNickname,
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
}

@Composable
private fun NicknameDisplayMode(
    nickname: String,
    onToggleEditing: () -> Unit
) {
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
            onClick = onToggleEditing,
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

@Composable
private fun GearsStatCard(gears: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .height(140.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF)
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
                    color = Color.Black,
                    fontFamily = AppTheme.pixelFontFamily,
                    style = AppTheme.pixelTextStyle,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Gears",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontFamily = AppTheme.pixelFontFamily,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BackToMainMenuButton(navigationController: NavigationController) {
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
}

@Composable private fun ProfileLoadingOverlay(isLoading: Boolean) {
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