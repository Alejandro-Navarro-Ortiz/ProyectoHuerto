package com.example.proyecto_huerto.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import org.jetbrains.compose.resources.stringResource
import proyectohuerto.composeapp.generated.resources.*
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

    val selectedDate = datePickerState.selectedDateMillis ?: getCurrentEpochMillis()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.diario_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(Res.string.profile_back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.bancales_add))
            }
        }
    ) { padding ->
        val tareasDelDia = tareas.filter { isSameDay(it.fecha, selectedDate) }
        val actividadesDelDia = actividades.filter { isSameDay(it.fecha, selectedDate) }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        title = null,
                        headline = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }

            if (actividadesDelDia.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.home_recent_activity),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(actividadesDelDia) { actividad ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ActividadItem(
                            actividad = actividad,
                            onDelete = { id -> viewModel.deleteActividad(id) }
                        )
                    }
                }
            }

            if (tareasDelDia.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tareas Manuales",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(tareasDelDia) { tarea ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        TareaItem(
                            tarea = tarea,
                            onToggleCompletada = { viewModel.toggleTareaCompletada(tarea) },
                            onDelete = { viewModel.deleteTarea(tarea.id) }
                        )
                    }
                }
            }

            if (tareasDelDia.isEmpty() && actividadesDelDia.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, bottom = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.EventNote,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                stringResource(Res.string.diario_no_records),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                item {
                    Spacer(Modifier.height(80.dp))
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
fun ActividadItem(
    actividad: Actividad,
    onDelete: (String) -> Unit
) {
    val (icon, color) = when (actividad.tipo) {
        TipoActividad.RIEGO -> Icons.Default.WaterDrop to Color(0xFF2196F3)
        TipoActividad.SIEMBRA -> Icons.Default.Grass to Color(0xFF4CAF50)
        TipoActividad.ABONADO -> Icons.Default.Science to Color(0xFFFF9800)
        TipoActividad.COSECHA -> Icons.Default.Agriculture to Color(0xFF9C27B0)
        else -> Icons.Default.History to Color(0xFF757575)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    actividad.tipo.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = color,
                    letterSpacing = 1.2.sp
                )
                Text(
                    "Bancal: ${actividad.nombreBancal}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    actividad.detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { onDelete(actividad.id) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Borrar",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TareaItem(tarea: Tarea, onToggleCompletada: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tarea.completada) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = { onToggleCompletada() },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    tarea.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (tarea.completada) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                if (tarea.descripcion.isNotBlank()) {
                    Text(
                        tarea.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
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
        title = { Text("Nueva Tarea", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Tipo de tarea:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                Column(modifier = Modifier.fillMaxWidth()) {
                    tipos.forEach { tipo ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tipoSeleccionado = tipo }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = (tipo == tipoSeleccionado), onClick = { tipoSeleccionado = tipo })
                            Text(text = tipo, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (titulo.isNotBlank()) onConfirm(titulo, descripcion, tipoSeleccionado) },
                enabled = titulo.isNotBlank()
            ) {
                Text("Añadir")
            }
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