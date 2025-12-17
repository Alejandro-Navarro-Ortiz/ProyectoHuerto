package com.example.proyecto_huerto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.proyecto_huerto.navigation.AppNavHost // La importación principal ahora es AppNavHost
import com.example.proyecto_huerto.ui.theme.ProyectoHuertoTheme // Mantenemos la importación del tema
import com.google.android.gms.auth.api.identity.Identity

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Se envuelve toda la navegación en el tema de la aplicación
            ProyectoHuertoTheme {
                // Se restaura la llamada original al sistema de navegación (AppNavHost)
                // que gestiona el login y el resto de pantallas.
                AppNavHost(
                    googleAuthUiClient = googleAuthUiClient,
                    lifecycleScope = lifecycleScope
                )
            }
        }
    }
}
