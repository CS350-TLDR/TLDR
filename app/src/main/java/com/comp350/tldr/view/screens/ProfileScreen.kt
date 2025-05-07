package com.comp350.tldr.view.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.comp350.tldr.R
import com.comp350.tldr.controller.navigation.NavigationController
import com.comp350.tldr.controller.viewmodels.ProfileViewModel
import com.comp350.tldr.view.RobotWithCustomization
import com.comp350.tldr.view.components.PixelBackground
import com.comp350.tldr.view.theme.AppTheme

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val navigationController = NavigationController(navController)

    val audioPlayer = ExoPlayer.Builder(navController.context).build()
    audioPlayer.volume = 1.0f // volume control from 1 is unchanged volume, 0.5 is half volume.
    var mediaItem = MediaItem.fromUri("android.resource://${navController.context.packageName}/raw/test_sound_a")
    audioPlayer.setMediaItem(mediaItem)
    audioPlayer.prepare()

    var audioExit = false
    // Set up a listener for the STATE_ENDED,
    // frees resources after completing sound and exiting screen.
    audioPlayer.addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED && audioExit) {
                Log.d("ExoPlayer", "Main menu Playback finished")
                audioPlayer.release()
            }
        }
    })

    // Collect states
    val nickname by viewModel.nickname.collectAsState()
    val gears by viewModel.gears.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasUnlockedSunglasses by viewModel.hasUnlockedSunglasses.collectAsState()
    val isWearingSunglasses by viewModel.isWearingSunglasses.collectAsState()

    // Local states
    var editingNickname by remember { mutableStateOf("") }
    var showPurchaseDialog by remember { mutableStateOf(false) }

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
            // Gears counter in top right
            GearsCounter(gears)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                ProfileScreenTitle()
                RobotWithOptionalSunglasses(isWearingSunglasses)
                NicknameSection(
                    nickname = nickname,
                    isEditing = isEditing,
                    editingNickname = editingNickname,
                    onEditingNicknameChange = { editingNickname = it },
                    onToggleEditing = { viewModel.toggleEditing() },
                    onUpdateNickname = { viewModel.updateNickname(context, editingNickname) }
                )

                SunglassesSection(
                    hasUnlockedSunglasses = hasUnlockedSunglasses,
                    isWearingSunglasses = isWearingSunglasses,
                    onToggleWearingSunglasses = {
                        if (!isWearingSunglasses){
                            mediaItem = MediaItem.fromUri("android.resource://${navController.context.packageName}/raw/cool_glasses_sound")
                            audioPlayer.setMediaItem(mediaItem)
                            audioPlayer.prepare() // restart the player
                            audioPlayer.play()
                            audioExit = false
                        }

                        viewModel.toggleWearingSunglasses(context) },
                    onPurchaseSunglasses = { showPurchaseDialog = true }
                )

                Spacer(modifier = Modifier.weight(1f))
                BackToMainMenuButton(navController, navigationController)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showPurchaseDialog) {
                PurchaseSunglassesDialog(
                    currentGears = gears,
                    onDismiss = { showPurchaseDialog = false },
                    onConfirmPurchase = {
                        mediaItem = MediaItem.fromUri("android.resource://${navController.context.packageName}/raw/gear_sound")
                        audioPlayer.setMediaItem(mediaItem)
                        audioPlayer.prepare() // restart the player
                        audioPlayer.play()
                        audioExit = false
                        viewModel.purchaseSunglasses(context)
                        showPurchaseDialog = false
                    }
                )
            }

            ProfileLoadingOverlay(isLoading)
        }
    }
}

@Composable
private fun GearsCounter(gears: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Gears: $gears",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontFamily = AppTheme.pixelFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
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
private fun RobotWithOptionalSunglasses(isWearingSunglasses: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        RobotWithCustomization(
            isWearingSunglasses = isWearingSunglasses,
            size = 120,
            sunglassesOffsetY = -12
        )
    }
}

@Composable
private fun SunglassesSection(
    hasUnlockedSunglasses: Boolean,
    isWearingSunglasses: Boolean,
    onToggleWearingSunglasses: () -> Unit,
    onPurchaseSunglasses: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Shop",
                fontSize = 24.sp,
                color = Color.Black,
                fontFamily = AppTheme.pixelFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (hasUnlockedSunglasses) {
                EquippedSunglassesButton(
                    isWearingSunglasses = isWearingSunglasses,
                    onToggleWearingSunglasses = onToggleWearingSunglasses,
                    textColor = Color.Black
                )
            } else {
                LockedSunglassesButton(
                    onPurchaseSunglasses = onPurchaseSunglasses,
                    textColor = Color.Black
                )
            }
        }
    }
}

@Composable
private fun EquippedSunglassesButton(
    isWearingSunglasses: Boolean,
    onToggleWearingSunglasses: () -> Unit,
    textColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.sunglasses),
                contentDescription = "Sunglasses",
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = "Sunglasses",
                fontSize = 20.sp,
                color = textColor,
                fontFamily = AppTheme.pixelFontFamily,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Switch(
            checked = isWearingSunglasses,
            onCheckedChange = { onToggleWearingSunglasses() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4B89DC),
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color(0xFF444444)
            )
        )
    }
}

@Composable
private fun LockedSunglassesButton(
    onPurchaseSunglasses: () -> Unit,
    textColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.sunglasses),
                contentDescription = "Sunglasses",
                modifier = Modifier
                    .size(40.dp)
                    .alpha(0.5f)
            )

            Text(
                text = "Sunglasses (5 Gears)",
                fontSize = 20.sp,
                color = textColor.copy(alpha = 0.5f),
                fontFamily = AppTheme.pixelFontFamily,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Button(
            onClick = onPurchaseSunglasses,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4B89DC)
            )
        ) {
            Text(
                text = "Buy",
                fontSize = 16.sp,
                color = Color.White,
                fontFamily = AppTheme.pixelFontFamily
            )
        }
    }
}

@Composable
private fun PurchaseSunglassesDialog(
    currentGears: Int,
    onDismiss: () -> Unit,
    onConfirmPurchase: () -> Unit
) {
    val canAfford = currentGears >= 5

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Purchase Sunglasses",
                fontFamily = AppTheme.pixelFontFamily,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sunglasses),
                    contentDescription = "Sunglasses",
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Do you want to purchase cool sunglasses for 5 gears?",
                    fontFamily = AppTheme.pixelFontFamily,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (canAfford) "Current balance: $currentGears gears" else "Not enough gears! You have: $currentGears",
                    fontFamily = AppTheme.pixelFontFamily,
                    fontSize = 16.sp,
                    color = if (canAfford) Color.Green else Color.Red,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmPurchase,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.blueButtonColor,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text(
                    text = "Purchase",
                    fontFamily = AppTheme.pixelFontFamily,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = AppTheme.pixelFontFamily,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        },
        containerColor = Color(0xFF333333),
        titleContentColor = Color.White,
        textContentColor = Color.White
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
            Text(
                text = "Nickname",
                fontSize = 24.sp,
                color = Color.White,
                fontFamily = AppTheme.pixelFontFamily,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing) {
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

                Spacer(modifier = Modifier.height(8.dp))

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
            } else {
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
        }
    }
}

@Composable
private fun BackToMainMenuButton(
    navController: NavController, // Add NavController as a parameter for audio playback
    navigationController: NavigationController
) {

    val audioPlayer = ExoPlayer.Builder(navController.context).build()
    audioPlayer.volume = 0.8f // volume control from 1 is unchanged volume, 0.5 is half volume.
    var mediaItem = MediaItem.fromUri("android.resource://${navController.context.packageName}/raw/click_alt_sound_a")
    audioPlayer.setMediaItem(mediaItem)
    audioPlayer.prepare()

    var audioExit = false
    // Set up a listener for the STATE_ENDED,
    // frees resources after completing sound and exiting screen.
    audioPlayer.addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED && audioExit) {
                Log.d("ExoPlayer", "profile Playback finished")
                audioPlayer.release()
            }
        }
    })

    Button(
        onClick = {
            mediaItem = MediaItem.fromUri("android.resource://${navController.context.packageName}/raw/click_alt_sound_a")
            audioPlayer.setMediaItem(mediaItem)
            audioPlayer.prepare() // restart the player
            audioPlayer.play()
            audioExit = true
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

@Composable
private fun ProfileLoadingOverlay(isLoading: Boolean) {
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

