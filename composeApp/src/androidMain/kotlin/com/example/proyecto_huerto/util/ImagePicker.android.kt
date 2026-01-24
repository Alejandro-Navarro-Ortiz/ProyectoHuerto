package com.example.proyecto_huerto.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Implementación Android para seleccionar imágenes de la galería.
 * Convierte la URI seleccionada en un ByteArray para su procesamiento multiplataforma.
 */
@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current

    // Launcher que abre el selector de archivos del sistema
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Abrimos el flujo de datos de la URI y lo convertimos a bytes
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    if (bytes.isNotEmpty()) {
                        onImagePicked(bytes)
                    }
                }
            } catch (e: Exception) {
                println("DEBUG_IMAGE: Error leyendo bytes: ${e.message}")
            }
        }
    }

    return remember {
        object : ImagePickerLauncher {
            override fun launch() {
                // Filtramos solo por imágenes
                launcher.launch("image/*")
            }
        }
    }
}

/**
 * Gestor de almacenamiento en la nube (Firebase Storage) específico para Android.
 */
actual class ImageStorageManager actual constructor() {
    private val storage = FirebaseStorage.getInstance()

    /**
     * Sube un array de bytes a la carpeta 'avatars' de Firebase Storage.
     * @return La URL pública de descarga o null si falla.
     */
    actual suspend fun uploadImage(userId: String, imageData: ByteArray): String? {
        return try {
            // Referencia única por usuario: sobreescribe la imagen anterior si existe
            val ref = storage.reference.child("avatars/$userId.jpg")

            // Subida asíncrona esperando el resultado con .await() de Coroutines
            ref.putBytes(imageData).await()

            // Obtenemos la URL de acceso público para guardarla en el perfil
            val url = ref.downloadUrl.await().toString()
            url
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}