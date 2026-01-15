package com.example.proyecto_huerto.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyecto_huerto.models.Tarea
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiarioScreen(
    viewModel: DiarioViewModel,
    onBack: () -> Unit
) {
    val tareas by viewModel.tareas.collectAsState()
    val datePickerState = rememberDatePickerState()
    var showAddDialog by remember { mutableStateOf(false) }

    val selectedDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Diario de Cultivo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Tarea")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                title = null,
                headline = null,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            val tareasDelDia = tareas.filter { tarea ->
                isSameDay(tarea.fecha, selectedDate)
            }

            if (tareasDelDia.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay tareas para este día", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tareasDelDia) { tarea ->
                        TareaItem(
                            tarea = tarea,
                            onToggleCompletada = { viewModel.toggleTareaCompletada(tarea) },
                            onDelete = { viewModel.deleteTarea(tarea.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTareaDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { titulo, descripcion, tipo ->
                viewModel.addTarea(titulo, descripcion, selectedDate, tipo)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TareaItem(
    tarea: Tarea,
    onToggleCompletada: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (tarea.completada) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = { onToggleCompletada() }
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = tarea.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (tarea.descripcion.isNotBlank()) {
                    Text(text = tarea.descripcion, style = MaterialTheme.typography.bodySmall)
                }
                Text(text = "Tipo: ${tarea.tipo}", style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddTareaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    val tipos = listOf("RIEGO", "SIEMBRA", "COSECHA", "TRATAMIENTO", "OTRA")
    var tipoSeleccionado by remember { mutableStateOf(tipos[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") })
                TextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
                Text("Tipo:", style = MaterialTheme.typography.labelLarge)
                tipos.forEach { tipo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (tipo == tipoSeleccionado),
                            onClick = { tipoSeleccionado = tipo }
                        )
                        Text(text = tipo, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (titulo.isNotBlank()) onConfirm(titulo, descripcion, tipoSeleccionado) }) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun isSameDay(millis1: Long, millis2: Long): Boolean {
    // Simple comparison for demo purposes. 
    // In a real KMP app, use kotlinx-datetime for proper time zone handling.
    val day1 = millis1 / (24 * 60 * 60 * 1000)
    val day2 = millis2 / (24 * 60 * 60 * 1000)
    return day1 == day2
}
