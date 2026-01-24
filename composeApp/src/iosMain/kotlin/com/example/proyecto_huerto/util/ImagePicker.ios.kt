package com.example.proyecto_huerto.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.interop.LocalUIViewController
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.storage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy

/**
 * Implementación de iOS para el selector de imágenes usando PHPickerViewController.
 */
@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher {
    val viewController = LocalUIViewController.current
    return remember {
        iOSImagePickerLauncher(viewController, onImagePicked)
    }
}

/**
 * Clase auxiliar que implementa el delegado de iOS para manejar la selección de fotos.
 */
private class iOSImagePickerLauncher(
    private val viewController: UIViewController,
    private val onImagePicked: (ByteArray) -> Unit
) : ImagePickerLauncher, NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun launch() {
        // Configuración básica del selector de fotos de iOS
        val configuration = PHPickerConfiguration()
        configuration.filter = PHPickerFilter.imagesFilter
        configuration.selectionLimit = 1

        val picker = PHPickerViewController(configuration)
        picker.delegate = this
        // Presenta el controlador de forma modal
        viewController.presentViewController(picker, true, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        // Cierra el selector al terminar
        picker.dismissViewControllerAnimated(true, null)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return

        // Carga la representación de los datos de la imagen
        result.itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, error ->
            if (data != null) {
                val nsData = data as NSData
                val bytes = ByteArray(nsData.length.toInt())
                // Conversión de punteros de memoria C (NSData) a ByteArray de Kotlin
                if (nsData.length > 0u) {
                    nsData.bytes?.let { bytesPointer ->
                        bytes.usePinned { pinned ->
                            memcpy(pinned.addressOf(0), bytesPointer, nsData.length)
                        }
                    }
                }
                onImagePicked(bytes)
            }
        }
    }
}

/**
 * Gestor de almacenamiento en Firebase Storage para la plataforma iOS.
 */
actual class ImageStorageManager actual constructor() {
    private val storage = Firebase.storage

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun uploadImage(userId: String, imageData: ByteArray): String? {
        return try {
            val ref = storage.reference("avatars/$userId.jpg")

            // 1. Convertimos el ByteArray de KMP a NSData nativo de iOS para interoperabilidad
            val nsData = imageData.usePinned { pinned ->
                NSData.dataWithBytes(pinned.addressOf(0), imageData.size.toULong())
            }

            // 2. Envolvemos el NSData en la clase Data que la librería GitLive Firebase espera
            val firebaseData = Data(nsData)

            // 3. Sube y obtiene la URL
            ref.putData(firebaseData)
            ref.getDownloadUrl()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}