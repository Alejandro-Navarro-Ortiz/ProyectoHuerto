package com.example.proyecto_huerto.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_huerto.viewmodel.HuertoUiState
import com.example.proyecto_huerto.viewmodel.HuertoViewModel

/**
 * Pantalla de Detalle de Hortaliza (Ficha Técnica).
 * Muestra información exhaustiva sobre una planta: descripción, consejos de cultivo y
 * compatibilidades (aliados y enemigos en el huerto).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleHortalizaScreen(
    nombreHortaliza: String,
    onBack: () -> Unit,
    viewModel: HuertoViewModel
) {
    val hortalizasState by viewModel.hortalizasState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ficha Técnica") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = hortalizasState) {
                is HuertoUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is HuertoUiState.Success -> {
                    val hortaliza = state.data.find { it.nombre == nombreHortaliza }
                    if (hortaliza != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // CABECERA VISUAL: Icono grande y nombre destacado
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        shape = MaterialTheme.shapes.extraLarge,
                                        color = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 4.dp,
                                        modifier = Modifier.size(100.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(hortaliza.icono, fontSize = 50.sp)
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = hortaliza.nombre,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(20.dp)) {
                                // SECCIÓN: DESCRIPCIÓN GENERAL
                                SectionCard(
                                    title = "Descripción General",
                                    icon = Icons.Default.Description,
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = hortaliza.descripcion,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                // SECCIÓN: CONSEJOS DE CULTIVO
                                SectionCard(
                                    title = "Consejos Pro",
                                    icon = Icons.Default.TipsAndUpdates,
                                    color = Color(0xFFF57C00)
                                ) {
                                    Text(
                                        text = hortaliza.consejos,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                // SECCIÓN: COMPATIBILIDAD (Asociación de Cultivos)
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    // Listado de aliados (Plantas compañeras beneficiosas)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Aliados",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color(0xFF388E3C),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                                        )
                                        hortaliza.compatibles.forEach { amigo ->
                                            CompactTag(amigo, Color(0xFFE8F5E9), Color(0xFF2E7D32))
                                        }
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    // Listado de enemigos (Plantas que se deben evitar cerca)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Evitar",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                                        )
                                        hortaliza.incompatibles.forEach { enemigo ->
                                            CompactTag(enemigo, Color(0xFFFFEBEE), Color(0xFFC62828))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(40.dp))
                            }
                        }
                    } else {
                        Text("No se encontró la información de la hortaliza.")
                    }
                }
                is HuertoUiState.Error -> {
                    Text(
                        text = "Error al cargar los detalles: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta contenedora para las diferentes secciones de la ficha técnica.
 */
@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = color,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Etiqueta compacta utilizada para listar hortalizas compatibles o incompatibles.
 */
@Composable
fun CompactTag(text: String, bgColor: Color, textColor: Color) {
    Surface(
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
        color = bgColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}