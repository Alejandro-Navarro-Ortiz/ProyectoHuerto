package com.example.proyecto_huerto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.proyecto_huerto.navigation.AppNavHost
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
            AppNavHost(
                googleAuthUiClient = googleAuthUiClient,
                lifecycleScope = lifecycleScope
            )
        }
    }
}
