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

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher {
    val viewController = LocalUIViewController.current
    return remember {
        iOSImagePickerLauncher(viewController, onImagePicked)
    }
}

private class iOSImagePickerLauncher(
    private val viewController: UIViewController,
    private val onImagePicked: (ByteArray) -> Unit
) : ImagePickerLauncher, NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun launch() {
        val configuration = PHPickerConfiguration()
        configuration.filter = PHPickerFilter.imagesFilter
        configuration.selectionLimit = 1

        val picker = PHPickerViewController(configuration)
        picker.delegate = this
        viewController.presentViewController(picker, true, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, null)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return

        result.itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, error ->
            if (data != null) {
                val nsData = data as NSData
                val bytes = ByteArray(nsData.length.toInt())
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

actual class ImageStorageManager actual constructor() {
    private val storage = Firebase.storage

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun uploadImage(userId: String, imageData: ByteArray): String? {
        return try {
            val ref = storage.reference("avatars/$userId.jpg")

            // 1. Convertimos el ByteArray de KMP a NSData de iOS
            val nsData = imageData.usePinned { pinned ->
                NSData.dataWithBytes(pinned.addressOf(0), imageData.size.toULong())
            }

            // 2. Envolvemos el NSData en la clase Data de GitLive que espera putData
            val firebaseData = Data(nsData)

            ref.putData(firebaseData)
            ref.getDownloadUrl()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}