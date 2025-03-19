package com.shadow.camerasensor.pages

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.shadow.camerasensor.ui.theme.CameraSensorTheme

@Composable
fun PhotoTaken(bitmap: Bitmap, applicationContext: Context) {
    CameraSensorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                bottomBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {


                        Button(
                            onClick = {
                                saveImageToGallery(bitmap, applicationContext)

                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                applicationContext.startActivity(intent)
                            }
                        ) {
                            Row (
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon (
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
                            Row (
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon (
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
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        modifier = Modifier
                            .fillMaxSize(),
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Captured Photo"
                    )
                }
            }
        }
    }
}

fun saveImageToGallery(bitmap: Bitmap, applicationContext: Context) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/Camera")
    }

    val resolver = applicationContext.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        try {
            resolver.openOutputStream(uri).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
            Log.d("Camera", "Image saved to gallery.")
        } catch (e: Exception) {
            Log.e("Camera", "Error saving image: $e")
        }
    }
}