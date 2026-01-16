package com.example.proyecto_huerto.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyecto_huerto.models.Bancal
import com.example.proyecto_huerto.models.Cultivo
import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.models.hortalizasDisponibles
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleBancalScreen(
    bancal: Bancal,
    onBack: () -> Unit,
    onUpdateCultivos: (List<String>, String) -> Unit,
    onRegarCultivos: (List<String>) -> Unit
) {
    var modoSeleccion by rememberSaveable { mutableStateOf(false) }
    var posicionesSeleccionadas by remember { mutableStateOf(emptySet<String>()) }
    var mostrarDialogoPlantas by remember { mutableStateOf(false) }
    var mostrarDialogoRiego by remember { mutableStateOf(false) }
    var justWateredPositions by remember { mutableStateOf(emptySet<String>()) }

    // Efecto para limpiar el feedback visual del riego después de un tiempo
    LaunchedEffect(justWateredPositions) {
        if (justWateredPositions.isNotEmpty()) {
            delay(2000L) // Muestra el feedback durante 2 segundos
            justWateredPositions = emptySet()
        }
    }

    // Comprueba si alguna celda seleccionada contiene un cultivo para mostrar el botón de riego
    val seleccionContieneCultivos = remember(posicionesSeleccionadas, bancal.cultivos) {
        posicionesSeleccionadas.any { bancal.cultivos[it] != null }
    }

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
            if (posicionesSeleccionadas.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // FAB para plantar, siempre visible si hay una selección
                    FloatingActionButton(
                        onClick = { mostrarDialogoPlantas = true },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Plantar Hortaliza")
                    }

                    // FAB para regar, solo visible si una celda seleccionada tiene un cultivo
                    if (seleccionContieneCultivos) {
                        FloatingActionButton(
                            onClick = { mostrarDialogoRiego = true },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Icon(Icons.Default.WaterDrop, contentDescription = "Regar Cultivos")
                        }
                    }
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
                            posicionesSeleccionadas = emptySet()
                        }
                    }
                )
            }

            Text("Dimensiones: ${bancal.ancho}x${bancal.largo}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))

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
                    val isJustWatered = posicion in justWateredPositions

                    CeldaBancal(cultivo, isSelected, isJustWatered) {
                        if (modoSeleccion) {
                            posicionesSeleccionadas = if (isSelected) {
                                posicionesSeleccionadas - posicion
                            } else {
                                posicionesSeleccionadas + posicion
                            }
                        } else {
                            posicionesSeleccionadas = setOf(posicion)
                            // Si la celda tiene cultivo, preguntamos para regar, si no, para plantar
                            if (bancal.cultivos[posicion] != null) {
                                mostrarDialogoRiego = true
                            } else {
                                mostrarDialogoPlantas = true
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoPlantas) {
        DialogoSeleccionHortaliza(
            hortalizas = hortalizasDisponibles,
            onDismiss = {
                mostrarDialogoPlantas = false
                posicionesSeleccionadas = emptySet()
            },
            onSelect = {
                onUpdateCultivos(posicionesSeleccionadas.toList(), it.nombre)
                mostrarDialogoPlantas = false
                posicionesSeleccionadas = emptySet()
            }
        )
    }

    if (mostrarDialogoRiego) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRiego = false },
            title = { Text("Confirmar Riego") },
            text = { Text("¿Deseas regar los cultivos seleccionados?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRegarCultivos(posicionesSeleccionadas.toList())
                        justWateredPositions = posicionesSeleccionadas // Activa el feedback visual
                        mostrarDialogoRiego = false
                        posicionesSeleccionadas = emptySet()
                    }
                ) { Text("Regar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoRiego = false; posicionesSeleccionadas = emptySet() }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun CeldaBancal(cultivo: Cultivo?, isSelected: Boolean, isJustWatered: Boolean, onClick: () -> Unit) {
    val seco = cultivo?.estaSeco ?: false
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        seco -> Color.Yellow.copy(alpha = 0.5f)
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
                    text = hortalizasDisponibles.find { it.nombre == cultivo.nombreHortaliza }?.icono ?: "❓",
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize
                )
            } else if (!isSelected) {
                Box(modifier = Modifier.size(12.dp).background(Color.Gray.copy(alpha = 0.3f), shape = MaterialTheme.shapes.small))
            }

            // Feedback visual para el riego
            if (isJustWatered) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Regado",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
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
