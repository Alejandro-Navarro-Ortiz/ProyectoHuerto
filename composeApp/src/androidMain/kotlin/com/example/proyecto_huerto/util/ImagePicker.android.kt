package com.example.proyecto_huerto.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    if (bytes.isNotEmpty()) {
                        println("DEBUG_IMAGE: Bytes leídos correctamente (${bytes.size} bytes)")
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
                launcher.launch("image/*")
            }
        }
    }
}

actual class ImageStorageManager actual constructor() {
    // Usamos la instancia por defecto configurada en google-services.json
    private val storage = FirebaseStorage.getInstance()

    actual suspend fun uploadImage(userId: String, imageData: ByteArray): String? {
        return try {
            println("DEBUG_IMAGE: Intentando subida a Storage para usuario: $userId")

            // Referencia al archivo
            val ref = storage.reference.child("avatars/$userId.jpg")

            // Subida de bytes (putBytes es la forma nativa correcta)
            ref.putBytes(imageData).await()

            // Obtenemos la URL pública. El await() asegura que la URL esté lista.
            val url = ref.downloadUrl.await().toString()
            println("DEBUG_IMAGE: SUBIDA EXITOSA. URL: $url")
            url
        } catch (e: Exception) {
            println("DEBUG_IMAGE: ERROR EN STORAGE: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}