package com.example.proyecto_huerto.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_huerto.models.Bancal
import com.example.proyecto_huerto.models.Cultivo
import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.viewmodel.HuertoUiState
import com.example.proyecto_huerto.viewmodel.HuertoViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleBancalScreen(
    bancal: Bancal,
    onBack: () -> Unit,
    onUpdateCultivos: (List<String>, String) -> Unit,
    onRegarCultivos: (List<String>) -> Unit,
    viewModel: HuertoViewModel
) {
    val hortalizasState by viewModel.hortalizasState.collectAsState()

    var modoSeleccion by rememberSaveable { mutableStateOf(false) }
    var posicionesSeleccionadas by remember { mutableStateOf(emptySet<String>()) }
    var mostrarDialogoPlantas by remember { mutableStateOf(false) }
    var mostrarDialogoRiego by remember { mutableStateOf(false) }
    var justWateredPositions by remember { mutableStateOf(emptySet<String>()) }

    var mostrarAvisoIncompatibilidad by remember { mutableStateOf(false) }
    var plantaPendiente by remember { mutableStateOf<Hortaliza?>(null) }
    var enemigosDetectados by remember { mutableStateOf<List<Hortaliza>>(emptyList()) }

    LaunchedEffect(justWateredPositions) {
        if (justWateredPositions.isNotEmpty()) {
            delay(2000L)
            justWateredPositions = emptySet()
        }
    }

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
                    FloatingActionButton(
                        onClick = { mostrarDialogoPlantas = true },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Plantar Hortaliza")
                    }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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

            when (val state = hortalizasState) {
                is HuertoUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is HuertoUiState.Success -> {
                    val hortalizasDisponibles = state.data
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

                            CeldaBancal(
                                cultivo = cultivo,
                                isSelected = isSelected,
                                isJustWatered = isJustWatered,
                                hortalizas = hortalizasDisponibles
                            ) {
                                if (modoSeleccion) {
                                    posicionesSeleccionadas = if (isSelected) {
                                        posicionesSeleccionadas - posicion
                                    } else {
                                        posicionesSeleccionadas + posicion
                                    }
                                } else {
                                    posicionesSeleccionadas = setOf(posicion)
                                    if (bancal.cultivos[posicion] != null) {
                                        mostrarDialogoRiego = true
                                    } else {
                                        mostrarDialogoPlantas = true
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
                            onSelect = { hortaliza ->
                                val nombresEnemigos = verificarIncompatibilidad(
                                    nueva = hortaliza,
                                    seleccionadas = posicionesSeleccionadas.toList(),
                                    bancal = bancal
                                )

                                if (nombresEnemigos.isNotEmpty()) {
                                    plantaPendiente = hortaliza
                                    enemigosDetectados = hortalizasDisponibles.filter { it.nombre in nombresEnemigos }
                                    mostrarAvisoIncompatibilidad = true
                                    mostrarDialogoPlantas = false
                                } else {
                                    onUpdateCultivos(posicionesSeleccionadas.toList(), hortaliza.nombre)
                                    mostrarDialogoPlantas = false
                                    posicionesSeleccionadas = emptySet()
                                }
                            }
                        )
                    }
                }
                is HuertoUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}")
                    }
                }
            }
        }
    }

    if (mostrarAvisoIncompatibilidad && plantaPendiente != null) {
        AlertDialog(
            onDismissRequest = { mostrarAvisoIncompatibilidad = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp)) },
            title = {
                Text(
                    "Conflicto de Cultivo",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Estas plantas no se llevan bien:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Planta que quieres poner
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(plantaPendiente!!.icono, fontSize = 40.sp)
                            Text(plantaPendiente!!.nombre, style = MaterialTheme.typography.labelSmall)
                        }

                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp).size(32.dp)
                        )

                        // Plantas enemigas encontradas
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            enemigosDetectados.forEach { enemigo ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(enemigo.icono, fontSize = 40.sp)
                                    Text(enemigo.nombre, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Plantar hortalizas incompatibles puede reducir la cosecha o atraer plagas.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        plantaPendiente?.let { onUpdateCultivos(posicionesSeleccionadas.toList(), it.nombre) }
                        mostrarAvisoIncompatibilidad = false
                        posicionesSeleccionadas = emptySet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Plantar de todos modos")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarAvisoIncompatibilidad = false
                    mostrarDialogoPlantas = true
                }) {
                    Text("Cambiar planta")
                }
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
                        justWateredPositions = posicionesSeleccionadas
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

private fun verificarIncompatibilidad(nueva: Hortaliza, seleccionadas: List<String>, bancal: Bancal): Set<String> {
    val enemigosEncontrados = mutableSetOf<String>()

    seleccionadas.forEach { pos ->
        val partes = pos.split("-")
        val f = partes[0].toInt()
        val c = partes[1].toInt()

        for (df in -1..1) {
            for (dc in -1..1) {
                if (df == 0 && dc == 0) continue
                val nf = f + df
                val nc = c + dc
                val posVecina = "$nf-$nc"

                bancal.cultivos[posVecina]?.let { cultivoVecino ->
                    if (nueva.incompatibles.contains(cultivoVecino.nombreHortaliza)) {
                        enemigosEncontrados.add(cultivoVecino.nombreHortaliza)
                    }
                }
            }
        }
    }
    return enemigosEncontrados
}

@Composable
fun CeldaBancal(
    cultivo: Cultivo?,
    isSelected: Boolean,
    isJustWatered: Boolean,
    hortalizas: List<Hortaliza>,
    onClick: () -> Unit
) {
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
                    text = hortalizas.find { it.nombre == cultivo.nombreHortaliza }?.icono ?: "❓",
                    fontSize = 24.sp
                )
            } else if (!isSelected) {
                Box(modifier = Modifier.size(12.dp).background(Color.Gray.copy(alpha = 0.3f), shape = MaterialTheme.shapes.small))
            }

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
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 80.dp),
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(hortalizas) { hortaliza ->
                    Column(
                        modifier = Modifier
                            .clickable { onSelect(hortaliza) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(hortaliza.icono, fontSize = 32.sp)
                        Text(hortaliza.nombre, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
