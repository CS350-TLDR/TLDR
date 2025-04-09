package com.comp350.tldr.classicstuff

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.comp350.tldr.R

@Composable
fun VideoPlayer(modifier: Modifier = Modifier, onVideoFinished: () -> Unit) {
    val context = LocalContext.current

    val videoUris = listOf(
        Uri.parse("android.resource://${context.packageName}/${R.raw.oop_vs_functional}"),
        Uri.parse("android.resource://${context.packageName}/${R.raw.pythonbasics}"),
        Uri.parse("android.resource://${context.packageName}/${R.raw.oop_spongeb}")
    )

    val videoUri = remember { videoUris.random() }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {

            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        onVideoFinished()
                    }
                }
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { PlayerView(context).apply { player = exoPlayer } },
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    )
}
