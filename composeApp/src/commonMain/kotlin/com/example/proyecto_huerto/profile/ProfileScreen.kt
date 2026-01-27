package com.example.proyecto_huerto.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_huerto.util.rememberImagePicker
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.jetbrains.compose.resources.stringResource
import proyectohuerto.composeapp.generated.resources.Res
import proyectohuerto.composeapp.generated.resources.*

/**
 * Pantalla de perfil de usuario mejorada con selectores de personalización.
 * Permite gestionar:
 * 1. Foto de perfil (Subida a Firebase).
 * 2. Datos personales (Nombre y Email).
 * 3. Preferencias visuales (Modo Oscuro).
 * 4. Preferencias de idioma (Español/Inglés) mediante un menú desplegable con banderas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    viewModel: ProfileViewModel
) {
    val userData by viewModel.user.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()

    // Estado para el diálogo de edición de nombre
    var showEditNameDialog by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }

    // Inicializador del selector de imágenes nativo (Android/iOS)
    val imagePicker = rememberImagePicker { bytes ->
        viewModel.uploadProfilePicture(bytes)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.profile_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.profile_back))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(Icons.Default.Info, stringResource(Res.string.profile_info))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // SECCIÓN: FOTO DE PERFIL CON GESTIÓN DE CARGA
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .clickable { if (!isUploading) imagePicker.launch() },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 2.dp
                    ) {
                        val photoUrl = userData?.profilePictureUrl
                        if (!photoUrl.isNullOrBlank()) {
                            key(photoUrl) {
                                KamelImage(
                                    resource = asyncPainterResource(photoUrl),
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    onLoading = { CircularProgressIndicator(modifier = Modifier.padding(25.dp)) },
                                    onFailure = { DefaultAvatar(userData?.username) }
                                )
                            }
                        } else {
                            DefaultAvatar(userData?.username)
                        }
                    }

                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }

                    Surface(
                        onClick = { if (!isUploading) imagePicker.launch() },
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        tonalElevation = 8.dp
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = stringResource(Res.string.profile_change_photo),
                            modifier = Modifier.padding(8.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // SECCIÓN: INFORMACIÓN DE CUENTA (Nombre y Correo)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Fila de Nombre (USER) - Ahora es clicable para editar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    nuevoNombre = userData?.username ?: ""
                                    showEditNameDialog = true
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ProfileInfoRow(
                                Icons.Default.Person,
                                stringResource(Res.string.profile_user),
                                userData?.username ?: stringResource(Res.string.profile_guest)
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar nombre",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                        ProfileInfoRow(
                            Icons.Default.Email,
                            stringResource(Res.string.profile_email),
                            userData?.email ?: stringResource(Res.string.profile_no_email)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECCIÓN: PREFERENCIAS (MODO OSCURO E IDIOMA)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(stringResource(Res.string.profile_dark_mode), style = MaterialTheme.typography.bodyMedium)
                            }
                            Switch(checked = isDarkMode, onCheckedChange = { onToggleDarkMode() })
                        }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(stringResource(Res.string.profile_language), style = MaterialTheme.typography.bodyMedium)
                            }

                            var expanded by remember { mutableStateOf(false) }
                            val currentLangLabel = if (currentLanguage == "es") stringResource(Res.string.profile_lang_es) else stringResource(Res.string.profile_lang_en)

                            Box {
                                Surface(
                                    onClick = { expanded = true },
                                    modifier = Modifier.height(40.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(currentLangLabel, style = MaterialTheme.typography.bodyMedium)
                                        Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(20.dp))
                                    }
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.profile_lang_es)) },
                                        onClick = {
                                            onLanguageChange("es")
                                            expanded = false
                                        },
                                        leadingIcon = { Text("🇪🇸", fontSize = 18.sp) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.profile_lang_en)) },
                                        onClick = {
                                            onLanguageChange("en")
                                            expanded = false
                                        },
                                        leadingIcon = { Text("🇺🇸", fontSize = 18.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(Res.string.profile_sign_out), fontWeight = FontWeight.Bold)
                }
            }
        }

        // DIÁLOGO PARA EDITAR EL NOMBRE
        if (showEditNameDialog) {
            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                title = { Text("Cambiar nombre de usuario") },
                text = {
                    OutlinedTextField(
                        value = nuevoNombre,
                        onValueChange = { nuevoNombre = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (nuevoNombre.isNotBlank()) {
                                viewModel.updateDisplayName(nuevoNombre)
                                showEditNameDialog = false
                            }
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNameDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

/**
 * Muestra una fila de información con un diseño consistente.
 */
@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/**
 * Representación visual cuando el usuario no tiene una foto establecida.
 */
@Composable
fun DefaultAvatar(username: String?) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
        ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = username?.take(1)?.uppercase() ?: "U",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )
    }
}