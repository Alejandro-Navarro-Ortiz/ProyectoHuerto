package com.example.proyecto_huerto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class SignInViewModel: ViewModel() {

    private val auth = Firebase.auth

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSignInResult(result: SignInResult) {
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                signInError = result.errorMessage
            )
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val signInResult = SignInResult(
                    data = result.user?.let {
                        UserData(
                            userId = it.uid,
                            username = it.displayName ?: it.email,
                            profilePictureUrl = it.photoUrl?.toString()
                        )
                    },
                    errorMessage = null
                )
                onSignInResult(signInResult)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val signInResult = SignInResult(
                    data = null,
                    errorMessage = e.message
                )
                onSignInResult(signInResult)
            }
        }
    }

    fun signUpWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val signInResult = SignInResult(
                    data = result.user?.let {
                        UserData(
                            userId = it.uid,
                            username = it.displayName ?: it.email,
                            profilePictureUrl = it.photoUrl?.toString()
                        )
                    },
                    errorMessage = null
                )
                onSignInResult(signInResult)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val signInResult = SignInResult(
                    data = null,
                    errorMessage = e.message
                )
                onSignInResult(signInResult)
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(signInError = null) }
    }

    fun resetState() {
        _state.update {
            SignInState()
        }
    }
}