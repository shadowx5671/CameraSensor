package com.shadow.camerasensor.pages

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shadow.camerasensor.models.CameraPreview
import com.shadow.camerasensor.models.MainViewModel
import com.shadow.camerasensor.ui.theme.CameraSensorTheme
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class HomePage(
    val applicationContext: Context
) {
    @Composable
    fun StartScreen(navController: NavController)
    {
        CameraSensorTheme {
            val controller = remember {
                LifecycleCameraController(applicationContext).apply {
                    setEnabledUseCases(
                        CameraController.IMAGE_CAPTURE or
                                CameraController.VIDEO_CAPTURE
                    )
                }
            }
            val viewModel = viewModel<MainViewModel>()
            val showLoadingDialog = remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                    bottomBar = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .padding(bottom = 20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    applicationContext.startActivity(intent)
                                }) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Gallery"
                                )
                            }

                            IconButton(
                                onClick = {
                                    showLoadingDialog.value = true
                                    coroutineScope.launch {
                                        takePhoto(
                                            controller = controller,
                                            onPhotoTaken = viewModel::onTakePhoto,
                                            navController = navController,
                                            onLoadingComplete = { showLoadingDialog.value = false }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    )
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Camera,
                                    contentDescription = "Camera",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Button(onClick = {
                                controller.cameraSelector =
                                    if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else CameraSelector.DEFAULT_BACK_CAMERA
                            }) {
                                Icon(
                                    imageVector = Icons.Default.FlipCameraAndroid,
                                    contentDescription = "Flip Camera"
                                )
                            }
                        }
                    }
                ) { innerPadding ->

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            CameraPreview(
                                controller = controller,
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 55.dp)
                                    .wrapContentSize()
                                    .aspectRatio(4f / 3f)
                                    .background(Color.Red)
                            )
                        }

                        if (showLoadingDialog.value) {
                            AlertDialogExample(
                                onDismissRequest = {},
                                onConfirmation = {},
                                dialogTitle = "Image is being processed.",
                                dialogText = "Please wait. This may take a few seconds.",
                                icon = Icons.Default.Camera
                            )
                        }
                    }
                }
            }
        }
    }

    fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit,
        navController: NavController,
        onLoadingComplete: () -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true
                    )
                    onPhotoTaken(rotatedBitmap)

                    val outputStream = ByteArrayOutputStream()
                    rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    val byteArray = outputStream.toByteArray()
                    val encodedBitmap = Base64.encodeToString(byteArray, Base64.DEFAULT)

                    val safeEncodedBitmap = Uri.encode(encodedBitmap)
                    navController.navigate("photoTaken/$safeEncodedBitmap")
                    onLoadingComplete()
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Couldn't take photo: ", exception)
                    onLoadingComplete()
                }
            }
        )
    }

    @Composable
    fun AlertDialogExample(
        onDismissRequest: () -> Unit,
        onConfirmation: () -> Unit,
        dialogTitle: String,
        dialogText: String,
        icon: ImageVector,
    ) {
        AlertDialog(
            icon = {
                Icon(icon, contentDescription = "Example Icon")
            },
            title = {
                Text(text = dialogTitle)
            },
            text = {
                Text(text = dialogText)
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmation()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }
}