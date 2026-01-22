package com.example.proyecto_huerto.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_huerto.models.hortalizasDisponibles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleHortalizaScreen(
    nombreHortaliza: String,
    onBack: () -> Unit
) {
    val hortaliza = hortalizasDisponibles.find { it.nombre == nombreHortaliza } ?: return

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Cabecera Visual
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

                // Sección: Descripción
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

                // Sección: Consejos de Cultivo
                SectionCard(
                    title = "Consejos Pro",
                    icon = Icons.Default.TipsAndUpdates,
                    color = Color(0xFFF57C00) // Naranja profesional
                ) {
                    Text(
                        text = hortaliza.consejos,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Compatibilidad
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Amigas
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

                    // Enemigas
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
    }
}

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
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
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