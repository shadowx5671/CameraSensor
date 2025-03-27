package com.shadow.camerasensor.pages

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.shadow.camerasensor.ui.theme.CameraSensorTheme
import kotlin.math.roundToInt
import android.graphics.Paint
import androidx.compose.ui.layout.ContentScale

data class DraggableObject(
    var x: MutableState<Float>,
    var y: MutableState<Float>
)

private var objects = mutableStateListOf<DraggableObject>()

@Composable
fun PhotoTaken(bitmap: Bitmap, applicationContext: Context) {
    CameraSensorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var selectedIndex by remember { mutableStateOf<Int?>(null) }
            val localView = LocalView.current

            Scaffold(
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 50.dp, top = 50.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                objects.add(DraggableObject(mutableFloatStateOf(0f),
                                    mutableFloatStateOf(0f)
                                ))
                            }
                        ) {
                            Text(text = "Rechteck erstellen")
                        }
                    }
                },
                bottomBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 50.dp, top = 50.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                saveScreenshotToGallery(applicationContext, localView)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Save to Gallery")
                            }
                        }

                        Button(
                            onClick = {
                                TODO()
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Send to Server")
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Captured Photo"
                    )

                    objects.forEachIndexed { index, _ ->
                        MultipleDraggableObject(
                            index,
                            onClick = { selectedIndex = index },
                            onDrag = { selectedIndex = null },
                        )
                    }

                    selectedIndex?.let { index ->
                        if (index in objects.indices) {
                            val position = objects[index]
                            Button(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            x = position.x.value.roundToInt() - 50,
                                            y = position.y.value.roundToInt() - 50
                                        )
                                    },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                onClick = {
                                    objects.removeAt(index)
                                    selectedIndex = null
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}

fun captureScreenshot(view: View): Bitmap {
    val bitmap = createBitmap(view.width, view.height)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}

fun saveScreenshotToGallery(applicationContext: Context, view: View) {
    val screenshot = captureScreenshot(view)

    val cropWidth = (screenshot.width).toInt()
    val cropHeight = (screenshot.height * 0.6).toInt()
    val startX = (screenshot.width - cropWidth) / 2
    val startY = (screenshot.height - cropHeight) / 2

    val croppedBitmap = Bitmap.createBitmap(screenshot, startX, startY, cropWidth, cropHeight)

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/Camera")
    }

    val resolver = applicationContext.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            Log.d("Camera", "Zugeschnittenes Bild gespeichert.")
        } catch (e: Exception) {
            Log.e("Camera", "Fehler beim Speichern des Bildes: $e")
        }
    }
}


@Composable
fun MultipleDraggableObject(index: Int, onClick: () -> Unit, onDrag: () -> Unit) {
    val draggableObject = objects[index]
    val offsetX = draggableObject.x
    val offsetY = draggableObject.y
    val scale = remember { mutableFloatStateOf(1f) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = offsetX.value.roundToInt(), y = offsetY.value.roundToInt()
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    isDragging = true
                    onDrag()
                    offsetX.value += pan.x
                    offsetY.value += pan.y
                    scale.floatValue = (scale.floatValue * zoom).coerceIn(0.5f, 3f)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (!isDragging) {
                            onClick()
                        }
                        isDragging = false
                    }
                )
            }
            .size((80 * scale.floatValue + 40).dp)
            .background(Color.Transparent)
            .border(color = MaterialTheme.colorScheme.primary, width = (7 * scale.floatValue).dp),
        contentAlignment = Alignment.Center
    ) {}
}