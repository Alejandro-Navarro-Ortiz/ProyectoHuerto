package com.example.proyecto_huerto.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_huerto.models.hortalizasDisponibles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuiaHortalizasScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Biblioteca Botánica", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Selecciona un cultivo para ver su ficha técnica técnica y consejos de expertos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(hortalizasDisponibles) { hortaliza ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDetail(hortaliza.nombre) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(hortaliza.icono, fontSize = 28.sp)
                            }
                        }

                        Spacer(Modifier.width(20.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = hortaliza.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ficha técnica disponible",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward, // Asegúrate de tener este import o usa ChevronRight
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// Nota: He añadido ArrowForward pero si no compila por falta de import usa Icons.Default.ChevronRight
// import androidx.compose.material.icons.automirrored.filled.ArrowForward