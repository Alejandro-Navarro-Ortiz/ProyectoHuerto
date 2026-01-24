package com.example.proyecto_huerto.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

object UserRepository {

    private val firestore by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }

    suspend fun updateFcmToken(token: String) {
        val currentUser = auth.currentUser ?: return
        // Cambiado de 'users' a 'usuarios' para consistencia con BancalViewModel y ProfileViewModel
        val userDocRef = firestore.collection("usuarios").document(currentUser.uid)

        try {
            userDocRef.set(mapOf("fcmToken" to token), merge = true)
        } catch (e: Exception) {
            println("Error updating FCM token: ${e.message}")
        }
    }
}