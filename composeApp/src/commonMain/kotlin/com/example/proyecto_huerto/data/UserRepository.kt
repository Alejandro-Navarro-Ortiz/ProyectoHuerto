package com.example.proyecto_huerto.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

object UserRepository {

    private val firestore by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }

    suspend fun updateFcmToken(token: String) {
        val currentUser = auth.currentUser ?: return
        val userDocRef = firestore.collection("users").document(currentUser.uid)

        try {
            userDocRef.set(mapOf("fcmToken" to token), merge = true)
        } catch (e: Exception) {
            // Manejar la excepción, por ejemplo, logueándola
            println("Error updating FCM token: ${e.message}")
        }
    }
}
