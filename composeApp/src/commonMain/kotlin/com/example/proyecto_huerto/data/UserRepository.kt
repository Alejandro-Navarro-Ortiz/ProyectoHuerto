package com.example.proyecto_huerto.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

/**
 * Repositorio encargado de gestionar datos persistentes del usuario en Firestore.
 * Centraliza operaciones como la actualización de tokens de notificación.
 */
object UserRepository {

    private val firestore by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }

    /**
     * Actualiza el token de Firebase Cloud Messaging (FCM) en el documento del usuario.
     * Esto permite enviar notificaciones push personalizadas desde el servidor.
     *
     * @param token El nuevo token generado por el servicio de mensajería.
     */
    suspend fun updateFcmToken(token: String) {
        val currentUser = auth.currentUser ?: return

        // Se utiliza la colección 'usuarios' para mantener consistencia con el resto de la app
        val userDocRef = firestore.collection("usuarios").document(currentUser.uid)

        try {
            // Utilizamos merge = true para no borrar otros datos del usuario (como su foto)
            userDocRef.set(mapOf("fcmToken" to token), merge = true)
        } catch (e: Exception) {
            println("Error updating FCM token: ${e.message}")
        }
    }
}