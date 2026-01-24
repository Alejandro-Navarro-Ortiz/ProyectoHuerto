package com.example.proyecto_huerto.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher

interface ImagePickerLauncher {
    fun launch()
}

expect class ImageStorageManager() {
    suspend fun uploadImage(userId: String, imageData: ByteArray): String?
}