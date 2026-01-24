package com.example.proyecto_huerto.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.proyecto_huerto.models.Bancal
import org.jetbrains.compose.resources.stringResource
import proyectohuerto.composeapp.generated.resources.*

/**
 * Pantalla para la gestión de los bancales del usuario.
 * Permite listar los bancales existentes, crear nuevos con dimensiones personalizadas
 * y eliminar aquellos que ya no se necesiten.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionBancalesScreen(
    bancales: List<Bancal>,
    onAddBancal: (String, Int, Int) -> Unit,
    onDeleteBancal: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onBancalClick: (String) -> Unit
) {
    // Estados para el formulario de creación
    var nombreBancal by remember { mutableStateOf("") }
    var ancho by remember { mutableStateOf("") }
    var largo by remember { mutableStateOf("") }
    var errorDimension by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.bancales_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onNavigate("Inicio") }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = stringResource(Res.string.profile_back)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // SECCIÓN: FORMULARIO DE NUEVO BANCAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = nombreBancal,
                        onValueChange = { nombreBancal = it },
                        label = { Text(stringResource(Res.string.bancales_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = ancho,
                            onValueChange = { ancho = it },
                            label = { Text(stringResource(Res.string.bancales_width)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = errorDimension != null,
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = largo,
                            onValueChange = { largo = it },
                            label = { Text(stringResource(Res.string.bancales_height)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = errorDimension != null,
                            singleLine = true
                        )
                    }

                    // Visualización de errores de validación
                    if (errorDimension != null) {
                        Text(
                            text = errorDimension!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            val anchoInt = ancho.toIntOrNull()
                            val largoInt = largo.toIntOrNull()

                            // Validación de reglas de negocio: Máximo 10x10 para asegurar rendimiento visual
                            if (nombreBancal.isNotBlank() && anchoInt != null && largoInt != null) {
                                if (anchoInt > 10 || largoInt > 10) {
                                    errorDimension = "Máx 10x10"
                                } else {
                                    errorDimension = null
                                    onAddBancal(nombreBancal, anchoInt, largoInt)
                                    nombreBancal = ""; ancho = ""; largo = ""
                                }
                            } else {
                                errorDimension = "Campos obligatorios"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.bancales_add))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN: LISTADO DE BANCALES ACTUALES
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bancales) { bancal ->
                    Card(
                        onClick = { onBancalClick(bancal.id) },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bancal.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${bancal.ancho} x ${bancal.largo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            
                            IconButton(onClick = { onDeleteBancal(bancal.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(Res.string.bancales_delete_confirm),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
