package com.shadow.camerasensor.logic

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shadow.camerasensor.pages.PhotoTaken
import com.shadow.camerasensor.pages.HomePage

@Composable
fun NavController(applicationContext: Context) {
    val navController = rememberNavController()

    Surface(color = MaterialTheme.colorScheme.background) {
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                val homePage = HomePage(applicationContext)
                homePage.StartScreen(navController)
            }
            composable("photoTaken/{bitmap}") { backStackEntry ->
                val encodedBitmap = backStackEntry.arguments?.getString("bitmap") ?: ""
                val decodedBytes = Base64.decode(encodedBitmap, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                PhotoTaken(bitmap, applicationContext)
            }
        }
    }
}


