package com.example.proyecto_huerto

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.proyecto_huerto.auth.GoogleAuthUiClient
import com.example.proyecto_huerto.navigation.AppNavHost
import com.example.proyecto_huerto.ui.theme.ProyectoHuertoTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

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
    ) { isGranted: Boolean ->
        // Permiso concedido o denegado
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val hasPreference = prefs.contains("dark_mode")
        val savedDarkMode = prefs.getBoolean("dark_mode", false)

        setContent {
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
                        prefs.edit().putBoolean("dark_mode", isDarkMode).apply()
                    }
                )
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