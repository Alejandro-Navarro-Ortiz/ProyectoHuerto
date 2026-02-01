package com.example.proyecto_huerto.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_huerto.models.Bancal
import org.jetbrains.compose.resources.stringResource
import proyectohuerto.composeapp.generated.resources.*

/**
 * Pantalla para la gestión de los bancales del usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionBancalesScreen(
    viewModel: BancalViewModel,
    onNavigate: (String) -> Unit,
    onBancalClick: (String) -> Unit
) {
    val bancales by viewModel.bancales.collectAsState()

    var nombreBancal by remember { mutableStateOf("") }
    var ancho by remember { mutableStateOf("") }
    var largo by remember { mutableStateOf("") }
    var errorDimension by remember { mutableStateOf<String?>(null) }

    // Estado para controlar qué bancal tiene el diálogo de estadísticas abierto
    var bancalParaStats by remember { mutableStateOf<Bancal?>(null) }

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
            // Formulario para crear un nuevo bancal
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
                            onValueChange = { ancho = it.filter { c -> c.isDigit() } },
                            label = { Text(stringResource(Res.string.bancales_width)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = errorDimension != null,
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = largo,
                            onValueChange = { largo = it.filter { c -> c.isDigit() } },
                            label = { Text(stringResource(Res.string.bancales_height)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = errorDimension != null,
                            singleLine = true
                        )
                    }

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

                            if (nombreBancal.isNotBlank() && anchoInt != null && largoInt != null) {
                                if (anchoInt > 10 || largoInt > 10 || anchoInt == 0 || largoInt == 0) {
                                    errorDimension = "Dimensiones entre 1 y 10"
                                } else {
                                    errorDimension = null
                                    viewModel.addBancal(nombreBancal, anchoInt, largoInt)
                                    nombreBancal = ""; ancho = ""; largo = ""
                                }
                            } else {
                                errorDimension = "Rellena todos los campos"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.bancales_add))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Listado de bancales existentes
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bancales, key = { it.id }) {
                    BancalCard(
                        bancal = it,
                        onDelete = { viewModel.deleteBancal(it.id) },
                        onStatsClick = { bancalParaStats = it },
                        onCardClick = { onBancalClick(it.id) }
                    )
                }
            }
        }
    }

    // Diálogo de estadísticas
    bancalParaStats?.let {
        val stats = viewModel.getStatsForBancal(it)
        StatsDialog(
            bancalNombre = it.nombre,
            stats = stats,
            onDismiss = { bancalParaStats = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BancalCard(
    bancal: Bancal,
    onDelete: () -> Unit,
    onStatsClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        onClick = onCardClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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

            Row {
                // Botón para ver estadísticas
                IconButton(onClick = onStatsClick) {
                    Icon(
                        imageVector = Icons.Default.QueryStats,
                        contentDescription = "Ver estadísticas",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                // Botón para eliminar
                IconButton(onClick = onDelete) {
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

@Composable
private fun StatsDialog(bancalNombre: String, stats: BancalStats, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Estadísticas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(bancalNombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                StatItem(icon = Icons.Default.Grass, label = "Plantas actuales", value = stats.plantasActuales.toString(), color = Color(0xFF388E3C))
                StatItem(icon = Icons.Default.WaterDrop, label = "Riegos totales", value = stats.riegos.toString(), color = Color(0xFF1976D2))
                StatItem(icon = Icons.Default.Science, label = "Abonados totales", value = stats.abonados.toString(), color = Color(0xFFF57C00))
                StatItem(icon = Icons.Default.Agriculture, label = "Cosechas totales", value = stats.cosechas.toString(), color = Color(0xFF7B1FA2))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun StatItem(icon: ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}
