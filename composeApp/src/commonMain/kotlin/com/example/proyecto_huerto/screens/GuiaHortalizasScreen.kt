package com.example.proyecto_huerto.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.models.hortalizasDisponibles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuiaHortalizasScreen(onBack: () -> Unit) {
    var hortalizaSeleccionada by remember { mutableStateOf<Hortaliza?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Guía de Cultivos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(hortalizasDisponibles) { hortaliza ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { hortalizaSeleccionada = hortaliza },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(hortaliza.icono, fontSize = 40.sp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(hortaliza.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Toca para ver detalles", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (hortalizaSeleccionada != null) {
        DetalleHortalizaDialog(
            hortaliza = hortalizaSeleccionada!!,
            onDismiss = { hortalizaSeleccionada = null }
        )
    }
}

@Composable
fun DetalleHortalizaDialog(hortaliza: Hortaliza, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(hortaliza.icono, fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Text(hortaliza.nombre)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoSection("Descripción", hortaliza.descripcion, Icons.Default.Info, MaterialTheme.colorScheme.primary)
                InfoSection("Consejos", hortaliza.consejos, Icons.Default.Info, Color(0xFFF44336))

                if (hortaliza.compatibles.isNotEmpty()) {
                    ListSection("Plantas Amigas", hortaliza.compatibles, Icons.Default.ThumbUp, Color(0xFF4CAF50))
                }

                if (hortaliza.incompatibles.isNotEmpty()) {
                    ListSection("Plantas Enemigas", hortaliza.incompatibles, Icons.Default.ThumbDown, Color(0xFFE91E63))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
fun InfoSection(titulo: String, texto: String, icono: ImageVector, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(titulo, fontWeight = FontWeight.Bold, color = color)
        }
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ListSection(titulo: String, items: List<String>, icono: ImageVector, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(titulo, fontWeight = FontWeight.Bold, color = color)
        }
        Text(items.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
    }
}