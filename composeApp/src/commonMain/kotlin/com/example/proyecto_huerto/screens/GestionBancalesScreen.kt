package com.example.proyecto_huerto.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyecto_huerto.models.Bancal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionBancalesScreen(
    bancales: List<Bancal>,
    onAddBancal: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var nuevoBancalNombre by remember { mutableStateOf("") }
    var bancalSeleccionado by remember { mutableStateOf<Bancal?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Bancales") },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Bancal")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = { onNavigate("Inicio") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Bancales") },
                    label = { Text("Bancales") },
                    selected = true,
                    onClick = { onNavigate("Bancales") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = { onNavigate("Perfil") }
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            
            // Lista de Bancales en Cuadrícula
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f).padding(8.dp)
            ) {
                items(bancales) { bancal ->
                    BancalGridItem(
                        bancal = bancal,
                        onClick = { bancalSeleccionado = bancal }
                    )
                }
            }

            // Detalle del Bancal Seleccionado (Al final de la pantalla o en un BottomSheet)
            bancalSeleccionado?.let { bancal ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = bancal.nombre, style = MaterialTheme.typography.headlineSmall)
                        val infoCultivo = if (bancal.cultivos.isEmpty()) "Estado: Vacío" 
                                         else "Cultivando: ${bancal.cultivos.joinToString()}"
                        Text(text = infoCultivo, style = MaterialTheme.typography.bodyLarge)
                        TextButton(onClick = { bancalSeleccionado = null }, modifier = Modifier.align(Alignment.End)) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }

        // Diálogo para añadir Bancal (Solo Nombre)
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Añadir nuevo bancal") },
                text = {
                    OutlinedTextField(
                        value = nuevoBancalNombre,
                        onValueChange = { nuevoBancalNombre = it },
                        label = { Text("Nombre del Bancal") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (nuevoBancalNombre.isNotBlank()) {
                            onAddBancal(nuevoBancalNombre)
                            nuevoBancalNombre = ""
                            showAddDialog = false
                        }
                    }) { Text("Añadir") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun BancalGridItem(bancal: Bancal, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = bancal.nombre,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
