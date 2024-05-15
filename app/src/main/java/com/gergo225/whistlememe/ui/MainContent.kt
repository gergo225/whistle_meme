package com.gergo225.whistlememe.ui

import android.Manifest
import android.graphics.Bitmap
import android.media.Image
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gergo225.whistlememe.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainContent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mediaPlayer = MediaPlayer.create(context, R.raw.whistle)

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    val memeVisible by remember { derivedStateOf { capturedImage != null } }

    Box(modifier = modifier) {
        if (cameraPermissionState.status.isGranted) {
            if (capturedImage == null) {
                CameraView(
                    modifier = Modifier.align(Alignment.Center),
                    onImageCaptured = { image ->
                        capturedImage = image
                    }
                )
            } else {
                Image(
                    painter = BitmapPainter(capturedImage!!.asImageBitmap()),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            LaunchedEffect(memeVisible) {
                if (memeVisible) {
                    mediaPlayer.start()
                }
            }

            AnimatedVisibility(
                visible = memeVisible,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 3000, easing = EaseIn)
                ),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.josh_hutcherson),
                    contentDescription = null,
                    modifier = Modifier
                        .width(300.dp)
                        .height(200.dp),
                    contentScale = ContentScale.FillBounds
                )
            }

        } else {
            Button(
                onClick = cameraPermissionState::launchPermissionRequest,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 16.dp)
            ) {
                Text("Enable Camera")
            }
        }
    }
}
