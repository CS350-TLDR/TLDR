package com.comp350.tldr.view.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.comp350.tldr.R
import com.comp350.tldr.controller.navigation.NavigationController
import com.comp350.tldr.controllers.QuizController
import com.comp350.tldr.view.components.PixelBackground
import com.comp350.tldr.view.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.comp350.tldr.controller.viewmodels.MainMenuViewModel

// -- MainScreen
@Composable
fun MainMenuScreen(navController: NavController, vm: MainMenuViewModel = viewModel()) {
    val ctx = LocalContext.current
    val topic by vm.topic.collectAsState()
    val activity by vm.activity.collectAsState()
    val enabled by vm.popupEnabled.collectAsState()

    var topicOpen by remember { mutableStateOf(false) }
    var activityOpen by remember { mutableStateOf(false) }

    PixelBackground {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RobotHeader()

                DropdownSelector("Topic", topic, topicOpen, { topicOpen = !topicOpen }, vm.topics) {
                    vm.setTopic(it)
                    topicOpen = false
                    if (enabled) vm.togglePopup(true, ctx)
                }

                DropdownSelector("Activity", activity, activityOpen, { activityOpen = !activityOpen }, vm.activities) {
                    vm.setActivity(it)
                    activityOpen = false
                    if (enabled) vm.togglePopup(true, ctx)
                }

                PopupToggle(enabled, activity) { toggled ->
                    if (toggled && !Settings.canDrawOverlays(ctx)) {
                        ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                        Toast.makeText(ctx, "Grant overlay permission for popups", Toast.LENGTH_LONG).show()
                    } else {
                        vm.togglePopup(toggled, ctx)
                    }
                }

                Spacer(Modifier.height(80.dp))
            }

            ProfileButton(navController)
        }
    }
}

// -- Reusable Composables
@Composable private fun RobotHeader() {
    Image(painterResource(R.drawable.robot), null, Modifier.size(180.dp).padding(24.dp))
    Spacer(Modifier.height(32.dp))
}

@Composable
private fun DropdownSelector(
    label: String,
    selected: String,
    expanded: Boolean,
    toggle: () -> Unit,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White, style = AppTheme.pixelTextStyle)
        Spacer(Modifier.height(8.dp))
        Box {
            Button(onClick = toggle, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                Text(selected, fontSize = 26.sp, color = AppTheme.darkBlueButtonColor, style = AppTheme.pixelTextStyleSmall)
            }
            DropdownMenu(expanded, toggle, Modifier.width(200.dp).background(Color.White)) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option, fontSize = 24.sp, fontFamily = AppTheme.pixelFontFamily) }, onClick = { onSelect(option) })
                }
            }
        }
    }
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun PopupToggle(enabled: Boolean, activity: String, onToggle: (Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Off/On", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = AppTheme.pixelFontFamily)
        Spacer(Modifier.height(12.dp))
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4B89DC),
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color(0xFF444444)
            ),
            modifier = Modifier.size(80.dp, 48.dp)
        )
        if (enabled) {
            Spacer(Modifier.height(12.dp))
            Text("glhf", fontSize = 18.sp, color = Color.White, fontFamily = AppTheme.pixelFontFamily)
        }
    }
}

@Composable
private fun ProfileButton(navController: NavController) {
    Box(Modifier.fillMaxSize().padding(end = 24.dp, bottom = 32.dp), contentAlignment = Alignment.BottomEnd) {
        Button(
            onClick = { navController.navigate("profile") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(width = 120.dp, height = 50.dp)
        ) {
            Text("Profile", fontSize = 22.sp, color = AppTheme.darkBlueButtonColor, style = AppTheme.pixelTextStyle.copy(
                shadow = AppTheme.pixelTextStyle.shadow?.copy(blurRadius = 1f, offset = androidx.compose.ui.geometry.Offset(2f, 2f))
            ))
        }
    }
}
