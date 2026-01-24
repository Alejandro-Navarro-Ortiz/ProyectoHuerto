package com.example.proyecto_huerto

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.proyecto_huerto.auth.GoogleAuthUiClient
import com.example.proyecto_huerto.navigation.AppNavHost
import com.example.proyecto_huerto.ui.theme.ProyectoHuertoTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Actividad principal de la aplicación.
 * Heredamos de AppCompatActivity para asegurar la compatibilidad total con
 * el sistema de cambio de idioma dinámico (AppCompatDelegate).
 */
class MainActivity : AppCompatActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private var isDarkMode by mutableStateOf(false)
    private var isFirstRun by mutableStateOf(true)

    // Lanzador para el permiso de notificaciones en Android 13+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()

        // Persistencia manual de preferencias del tema
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val hasPreference = prefs.contains("dark_mode")
        val savedDarkMode = prefs.getBoolean("dark_mode", false)

        setContent {
            val systemInDark = isSystemInDarkTheme()

            // Inicialización del tema en el primer arranque
            if (isFirstRun) {
                isDarkMode = if (hasPreference) savedDarkMode else systemInDark
                isFirstRun = false
            }

            // Clave única basada en el idioma actual para forzar la recomposición completa 
            // de Compose Multiplatform cuando el idioma cambia.
            val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: Locale.getDefault().language

            ProyectoHuertoTheme(darkTheme = isDarkMode) {
                // Usamos key(currentLocale) para que si el idioma cambia, Compose
                // destruya y vuelva a crear el grafo de navegación con los nuevos recursos.
                key(currentLocale) {
                    AppNavHost(
                        googleAuthUiClient = googleAuthUiClient,
                        lifecycleScope = lifecycleScope,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = {
                            isDarkMode = !isDarkMode
                            prefs.edit().putBoolean("dark_mode", isDarkMode).apply()
                        }
                    )
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}