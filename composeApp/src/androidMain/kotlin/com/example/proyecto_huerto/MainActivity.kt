package com.example.proyecto_huerto

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.proyecto_huerto.auth.GoogleAuthUiClient
import com.example.proyecto_huerto.navigation.AppNavHost
import com.example.proyecto_huerto.ui.theme.ProyectoHuertoTheme
import com.google.android.gms.auth.api.identity.Identity

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    // Estado para controlar el modo oscuro
    private var isDarkMode by mutableStateOf(false)
    private var isFirstRun by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recuperar preferencia guardada
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        // Si no existe la llave "dark_mode", usamos un valor nulo para identificar que debemos mirar el sistema
        val hasPreference = prefs.contains("dark_mode")
        val savedDarkMode = prefs.getBoolean("dark_mode", false)

        setContent {
            // Si el usuario nunca ha tocado el switch, usamos lo que diga el sistema
            val systemInDark = isSystemInDarkTheme()

            if (isFirstRun) {
                isDarkMode = if (hasPreference) savedDarkMode else systemInDark
                isFirstRun = false
            }

            ProyectoHuertoTheme(darkTheme = isDarkMode) {
                AppNavHost(
                    googleAuthUiClient = googleAuthUiClient,
                    lifecycleScope = lifecycleScope,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = {
                        isDarkMode = !isDarkMode
                        // Guardar la preferencia
                        prefs.edit().putBoolean("dark_mode", isDarkMode).apply()
                    }
                )
            }
        }
    }
}