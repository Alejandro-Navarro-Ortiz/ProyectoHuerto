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

/**
 * ViewModel encargado de la lógica de la pantalla de perfil.
 * Gestiona la obtención de datos del usuario actual y la actualización
 * de su foto de perfil tanto en Firebase Auth como en Firestore.
 */
class ProfileViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storageManager by lazy { ImageStorageManager() }

    // Estado para controlar si hay una subida de imagen en curso
    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    // Estado que contiene la información del usuario actual
    private val _user = MutableStateFlow<UserData?>(null)
    val user = _user.asStateFlow()

    init {
        refreshUser()
    }

    /**
     * Sincroniza el estado local con la información del usuario autenticado en Firebase.
     * Ajusta la calidad de la imagen si proviene de Google.
     */
    fun refreshUser() {
        val currentUser = auth.currentUser
        _user.value = currentUser?.let {
            UserData(
                userId = it.uid,
                username = it.displayName ?: it.email,
                profilePictureUrl = it.photoURL?.let { url ->
                    // Si es de Google, solicitamos una versión de mayor tamaño
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

    /**
     * Proceso completo de actualización de foto de perfil:
     * 1. Sube el array de bytes a Firebase Storage.
     * 2. Actualiza la URL en el perfil de Firebase Auth.
     * 3. Guarda la URL en la colección 'usuarios' de Firestore.
     * 4. Actualiza el estado de la UI.
     */
    fun uploadProfilePicture(imageData: ByteArray) {
        val userObj = auth.currentUser ?: return
        viewModelScope.launch {
            _isUploading.value = true
            try {
                // 1. Subida física al almacenamiento de Firebase
                val downloadUrl = storageManager.uploadImage(userObj.uid, imageData)

                if (downloadUrl != null) {
                    // 2. Vinculación con el perfil de autenticación
                    userObj.updateProfile(photoUrl = downloadUrl)

                    // 3. Persistencia en la base de datos de usuarios
                    db.collection("usuarios").document(userObj.uid)
                        .set(mapOf("profilePictureUrl" to downloadUrl), merge = true)

                    // 4. Refrescar el objeto de usuario local
                    try {
                        userObj.reload()
                    } catch (e: Exception) {
                        println("DEBUG_IMAGE: Error recargando usuario: ${e.message}")
                    }

                    // 5. Cache busting: añadimos un timestamp para que el cargador de imágenes
                    // (Kamel) detecte un cambio de URL y no use la imagen antigua de la caché.
                    val separator = if (downloadUrl.contains("?")) "&" else "?"
                    val uniqueUrl = "$downloadUrl${separator}t=${getCurrentEpochMillis()}"

                    _user.value = _user.value?.copy(profilePictureUrl = uniqueUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }
}