package com.example.proyecto_huerto.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_huerto.auth.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userData: UserData?,
    onSignOut: () -> Unit,
    onBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Perfil
            Surface(
                modifier = Modifier.size(100.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        userData?.username?.firstOrNull()?.toString()?.uppercase() ?: "U",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(userData?.username ?: "Usuario", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(userData?.email ?: "Sin correo verificado", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)

            Spacer(Modifier.height(32.dp))

            // Secciones
            ProfileSectionTitle("Preferencias")
            SettingsItem(
                title = "Modo Oscuro",
                subtitle = if(isDarkMode) "Activado" else "Desactivado",
                icon = if(isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                trailing = {
                    Switch(checked = isDarkMode, onCheckedChange = { onToggleDarkMode() })
                }
            )

            Spacer(Modifier.height(16.dp))
            ProfileSectionTitle("Aplicación")
            SettingsItem(
                title = "Guía de inicio",
                subtitle = "Cómo usar el huerto",
                icon = Icons.Default.Info,
                onClick = onNavigateToAbout
            )
            SettingsItem(
                title = "Privacidad y Datos",
                subtitle = "Gestionar mi cuenta",
                icon = Icons.Default.Lock,
                onClick = {}
            )

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))

            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, null)
                Spacer(Modifier.width(8.dp))
                Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileSectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelMedium)
            }
            trailing?.invoke() ?: Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}