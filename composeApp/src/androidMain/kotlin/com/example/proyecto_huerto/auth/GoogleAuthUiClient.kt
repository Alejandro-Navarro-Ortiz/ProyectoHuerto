package com.example.proyecto_huerto.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.proyecto_huerto.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

/**
 * Cliente encargado de gestionar la autenticación con Google (One Tap) y Firebase en Android.
 * Encapsula la lógica de inicio de sesión, registro y cierre de sesión.
 */
class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {

    private val auth = Firebase.auth

    /**
     * Inicia el flujo de 'One Tap Sign-In' de Google.
     * @return Un IntentSender para lanzar la UI de Google o null si falla.
     */
    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    /**
     * Procesa el resultado devuelto por la actividad de Google.
     */
    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            // Vincula la credencial de Google con el sistema de Auth de Firebase
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = fixImageUrlQuality(photoUrl?.toString()),
                        email = email
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    /**
     * Inicia sesión con correo electrónico y contraseña tradicionales.
     */
    suspend fun signInWithEmail(email: String, password: String): SignInResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            SignInResult(
                data = result.user?.let {
                    UserData(
                        userId = it.uid,
                        username = it.displayName ?: it.email,
                        profilePictureUrl = fixImageUrlQuality(it.photoUrl?.toString()),
                        email = it.email
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            SignInResult(null, e.message)
        }
    }

    /**
     * Crea una nueva cuenta de usuario con email y contraseña.
     */
    suspend fun signUpWithEmail(email: String, password: String): SignInResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            SignInResult(
                data = result.user?.let {
                    UserData(
                        userId = it.uid,
                        username = it.displayName ?: it.email,
                        profilePictureUrl = fixImageUrlQuality(it.photoUrl?.toString()),
                        email = it.email
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            SignInResult(null, e.message)
        }
    }

    /**
     * Envía un correo de recuperación de contraseña.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    /**
     * Cierra la sesión tanto en Google One Tap como en Firebase.
     */
    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    /**
     * Recupera el usuario actualmente autenticado.
     */
    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName ?: email,
            profilePictureUrl = fixImageUrlQuality(photoUrl?.toString()),
            email = email
        )
    }

    /**
     * Modifica la URL de la foto de Google para obtener una resolución decente (400px en lugar de 96px).
     */
    private fun fixImageUrlQuality(url: String?): String? {
        return url?.replace("s96-c", "s400-c")
    }

    /**
     * Construye la solicitud de inicio de sesión de Google con el Client ID del proyecto.
     */
    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}