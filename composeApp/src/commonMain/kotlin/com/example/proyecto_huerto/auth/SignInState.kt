package com.example.proyecto_huerto.auth

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)
