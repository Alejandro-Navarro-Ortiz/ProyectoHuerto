package com.example.proyecto_huerto.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyecto_huerto.models.Bancal
import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.models.hortalizasDisponibles
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleBancalScreen(
    bancal: Bancal,
    onBack: () -> Unit,
    onUpdateCultivos: (List<String>, String) -> Unit // Modificado para aceptar múltiples posiciones
) {
    var modoSeleccion by rememberSaveable { mutableStateOf(false) }
    var posicionesSeleccionadas by remember { mutableStateOf(emptySet<String>()) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(bancal.nombre, fontWeight = FontWeight.Bold) },
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
            // Mostrar el FAB solo si hay celdas seleccionadas
            if (posicionesSeleccionadas.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { mostrarDialogo = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Plantar Hortaliza")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Fila con el Switch para activar/desactivar el modo selección
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Selección Múltiple", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = modoSeleccion,
                    onCheckedChange = {
                        modoSeleccion = it
                        if (!it) {
                            // Al desactivar el modo, limpiamos la selección
                            posicionesSeleccionadas = emptySet()
                        }
                    }
                )
            }

            Text("Dimensiones: ${bancal.ancho}x${bancal.largo}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Cuadrícula del bancal
            LazyVerticalGrid(
                columns = GridCells.Fixed(bancal.ancho.coerceAtLeast(1)),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bancal.ancho * bancal.largo) { index ->
                    val fila = index / bancal.ancho
                    val columna = index % bancal.ancho
                    val posicion = "$fila-$columna"
                    val cultivo = bancal.cultivos[posicion]
                    val isSelected = posicion in posicionesSeleccionadas

                    CeldaBancal(cultivo, isSelected) {
                        if (modoSeleccion) {
                            // Si estamos en modo selección, añadimos o quitamos la celda del conjunto
                            posicionesSeleccionadas = if (isSelected) {
                                posicionesSeleccionadas - posicion
                            } else {
                                posicionesSeleccionadas + posicion
                            }
                        } else {
                            // Si no, abrimos el diálogo para una sola celda
                            posicionesSeleccionadas = setOf(posicion)
                            mostrarDialogo = true
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoSeleccionHortaliza(
            hortalizas = hortalizasDisponibles,
            onDismiss = {
                mostrarDialogo = false
                if (!modoSeleccion) {
                    posicionesSeleccionadas = emptySet()
                }
            },
            onSelect = {
                onUpdateCultivos(posicionesSeleccionadas.toList(), it.nombre)
                mostrarDialogo = false
                posicionesSeleccionadas = emptySet()
                // Opcional: desactivar el modo selección después de plantar
                // modoSeleccion = false
            }
        )
    }
}

@Composable
fun CeldaBancal(cultivo: String?, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    Card(
        modifier = Modifier.aspectRatio(1f).clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = border
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (cultivo != null) {
                Text(
                    text = hortalizasDisponibles.find { it.nombre == cultivo }?.icono ?: "❓",
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize
                )
            } else if (!isSelected) {
                // Muestra un punto gris solo si no está seleccionada
                Box(modifier = Modifier.size(12.dp).background(Color.Gray.copy(alpha = 0.3f), shape = MaterialTheme.shapes.small))
            }
        }
    }
}

@Composable
fun DialogoSeleccionHortaliza(
    hortalizas: List<Hortaliza>,
    onDismiss: () -> Unit,
    onSelect: (Hortaliza) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona una hortaliza") },
        text = {
            LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 80.dp)) {
                items(hortalizas.size) { index ->
                    val hortaliza = hortalizas[index]
                    Column(
                        modifier = Modifier.padding(8.dp).clickable { onSelect(hortaliza) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(hortaliza.icono, fontSize = MaterialTheme.typography.headlineMedium.fontSize)
                        Text(hortaliza.nombre, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}