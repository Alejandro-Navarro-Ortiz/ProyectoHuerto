package com.example.proyecto_huerto.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_huerto.models.Tarea
import com.example.proyecto_huerto.models.Actividad
import com.example.proyecto_huerto.models.TipoActividad
import com.example.proyecto_huerto.util.getCurrentEpochMillis
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiarioScreen(
    viewModel: DiarioViewModel,
    onBack: () -> Unit
) {
    val tareas by viewModel.tareas.collectAsState()
    val actividades by viewModel.actividades.collectAsState()
    val datePickerState = rememberDatePickerState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Usamos nuestra utilidad para obtener el tiempo actual en millis
    val selectedDate = datePickerState.selectedDateMillis ?: getCurrentEpochMillis()

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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                title = null,
                headline = null,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            val tareasDelDia = tareas.filter { isSameDay(it.fecha, selectedDate) }
            val actividadesDelDia = actividades.filter { isSameDay(it.fecha, selectedDate) }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (actividadesDelDia.isNotEmpty()) {
                    item {
                        Text("Actividades Automáticas", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    items(actividadesDelDia) { actividad ->
                        ActividadItem(actividad)
                    }
                }

                if (tareasDelDia.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("Tareas Manuales", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    items(tareasDelDia) { tarea ->
                        TareaItem(
                            tarea = tarea,
                            onToggleCompletada = { viewModel.toggleTareaCompletada(tarea) },
                            onDelete = { viewModel.deleteTarea(tarea.id) }
                        )
                    }
                }

                if (tareasDelDia.isEmpty() && actividadesDelDia.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay registros para este día")
                        }
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
fun ActividadItem(actividad: Actividad) {
    val icon = when (actividad.tipo) {
        TipoActividad.RIEGO -> Icons.Default.WaterDrop
        TipoActividad.SIEMBRA -> Icons.Default.Grass
        else -> Icons.Default.History
    }
    val color = if (actividad.tipo == TipoActividad.RIEGO) Color(0xFF2196F3) else Color(0xFF4CAF50)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(actividad.tipo.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
                Text("Bancal: ${actividad.nombreBancal}", style = MaterialTheme.typography.bodyMedium)
                Text(actividad.detalle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun TareaItem(tarea: Tarea, onToggleCompletada: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (tarea.completada) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = tarea.completada, onCheckedChange = { onToggleCompletada() })
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(tarea.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (tarea.descripcion.isNotBlank()) {
                    Text(tarea.descripcion, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddTareaDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    val tipos = listOf("RIEGO", "SIEMBRA", "COSECHA", "TRATAMIENTO", "OTRA")
    var tipoSeleccionado by remember { mutableStateOf(tipos[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                TextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                Text("Tipo:", style = MaterialTheme.typography.labelLarge)
                tipos.forEach { tipo ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { tipoSeleccionado = tipo }) {
                        RadioButton(selected = (tipo == tipoSeleccionado), onClick = { tipoSeleccionado = tipo })
                        Text(text = tipo, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (titulo.isNotBlank()) onConfirm(titulo, descripcion, tipoSeleccionado) }) { Text("Añadir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalTime::class)
private fun isSameDay(millis1: Long, millis2: Long): Boolean {
    val instant1 = Instant.fromEpochMilliseconds(millis1)
    val instant2 = Instant.fromEpochMilliseconds(millis2)
    val date1 = instant1.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val date2 = instant2.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return date1 == date2
}