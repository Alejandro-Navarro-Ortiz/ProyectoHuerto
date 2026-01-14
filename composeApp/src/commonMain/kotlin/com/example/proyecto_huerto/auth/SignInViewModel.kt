package com.example.proyecto_huerto.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignInViewModel: ViewModel() {

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

    // Nota: La implementación real de Firebase debe ir aquí 
    // o inyectarse mediante un repositorio para ser Multiplatform.
    
    fun clearError() {
        _state.update { it.copy(signInError = null) }
    }

    fun resetState() {
        _state.update {
            SignInState()
        }
    }
}
