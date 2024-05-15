package com.gergo225.whistlememe.ui

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    onImageCaptured: (Bitmap) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val backCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = Executors.newSingleThreadExecutor()

    Box(
        modifier = modifier
    ) {
        AndroidView(
            modifier = Modifier
                .align(Alignment.Center),
            factory = { context ->
                val previewView = PreviewView(context).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder()
                            .build()
                            .also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                        try {
                            cameraProvider.unbindAll()


                            val useCaseGroup = UseCaseGroup.Builder()
                                .addUseCase(preview)
                                .addUseCase(imageCapture)
                                .setViewPort(previewView.viewPort!!)
                                .build()

                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                backCameraSelector,
                                useCaseGroup
                            )
                        } catch (e: Exception) {
                            Log.e("CameraView", "Use case binding failed", e)
                        }
                    },
                    ContextCompat.getMainExecutor(context)
                )

                previewView
            }
        )

        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(vertical = 16.dp),
            onClick = {
                imageCapture.takePicture(
                    cameraExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            super.onCaptureSuccess(image)
                            val imageRotation = image.imageInfo.rotationDegrees
                            val imageWithOriginalRotation = image.toBitmap().rotate(imageRotation.toFloat())
                            onImageCaptured(imageWithOriginalRotation)
                        }
                    },
                )
            }
        ) {
            Text("Take Cool Picture")
        }
    }
}

fun Bitmap.rotate(degrees: Float): Bitmap =
    Bitmap.createBitmap(this, 0, 0, width, height, Matrix().apply { postRotate(degrees) }, true)