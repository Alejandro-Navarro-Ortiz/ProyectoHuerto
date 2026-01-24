package com.example.proyecto_huerto.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_huerto.auth.UserData
import com.example.proyecto_huerto.util.ImageStorageManager
import com.example.proyecto_huerto.util.getCurrentEpochMillis
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storageManager by lazy { ImageStorageManager() }

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private val _user = MutableStateFlow<UserData?>(null)
    val user = _user.asStateFlow()

    init {
        refreshUser()
    }

    fun refreshUser() {
        val currentUser = auth.currentUser
        _user.value = currentUser?.let {
            UserData(
                userId = it.uid,
                username = it.displayName ?: it.email,
                profilePictureUrl = it.photoURL?.let { url ->
                    if (url.contains("googleusercontent.com")) {
                        url.replace("s96-c", "s400-c")
                    } else {
                        url
                    }
                },
                email = it.email
            )
        }
    }

    fun uploadProfilePicture(imageData: ByteArray) {
        val userObj = auth.currentUser ?: return
        viewModelScope.launch {
            _isUploading.value = true
            println("DEBUG_IMAGE: Iniciando proceso de subida en ViewModel...")
            try {
                // 1. Subida a Storage
                val downloadUrl = storageManager.uploadImage(userObj.uid, imageData)

                if (downloadUrl != null) {
                    // 2. Actualizar Firebase Auth
                    userObj.updateProfile(photoUrl = downloadUrl)

                    // 3. Actualizar Firestore
                    db.collection("usuarios").document(userObj.uid)
                        .set(mapOf("profilePictureUrl" to downloadUrl), merge = true)

                    // 4. Forzar recarga de datos de Auth
                    try {
                        userObj.reload()
                    } catch (e: Exception) {
                        println("DEBUG_IMAGE: Error recargando usuario: ${e.message}")
                    }

                    // 5. Notificar a la UI con timestamp para evitar caché de imagen
                    val separator = if (downloadUrl.contains("?")) "&" else "?"
                    val uniqueUrl = "$downloadUrl${separator}t=${getCurrentEpochMillis()}"

                    _user.value = _user.value?.copy(profilePictureUrl = uniqueUrl)
                    println("DEBUG_IMAGE: Proceso completado. UI notificada con: $uniqueUrl")
                } else {
                    println("DEBUG_IMAGE: El proceso se detuvo porque la URL fue nula")
                }
            } catch (e: Exception) {
                println("DEBUG_IMAGE: Error fatal en ViewModel: ${e.message}")
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }
}