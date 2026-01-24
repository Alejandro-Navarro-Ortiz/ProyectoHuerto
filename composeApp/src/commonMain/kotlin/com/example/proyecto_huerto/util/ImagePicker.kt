package com.example.proyecto_huerto.util

import androidx.compose.runtime.Composable

/**
 * Interfaz común para el lanzador de selección de imágenes.
 * Define el contrato que deben cumplir las implementaciones nativas.
 */
@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher

interface ImagePickerLauncher {
    /**
     * Abre la interfaz de usuario del sistema para seleccionar una imagen.
     */
    fun launch()
}

/**
 * Clase compartida para gestionar la subida de imágenes a Firebase Storage.
 * Las implementaciones específicas se encuentran en androidMain e iosMain.
 */
expect class ImageStorageManager() {
    /**
     * Sube una imagen y devuelve su URL.
     * @param userId ID del usuario para organizar el almacenamiento.
     * @param imageData Imagen en formato binario.
     */
    suspend fun uploadImage(userId: String, imageData: ByteArray): String?
}