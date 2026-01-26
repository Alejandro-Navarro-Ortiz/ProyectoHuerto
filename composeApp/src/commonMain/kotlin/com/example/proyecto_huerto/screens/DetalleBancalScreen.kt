package com.example.proyecto_huerto.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_huerto.models.Bancal
import com.example.proyecto_huerto.models.Cultivo
import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.viewmodel.HuertoUiState
import org.jetbrains.compose.resources.stringResource
import proyectohuerto.composeapp.generated.resources.Res
import proyectohuerto.composeapp.generated.resources.*

private sealed class DialogState {
    object Hidden : DialogState()
    object PlantSelection : DialogState()
    object Watering : DialogState()
    object Fertilizing : DialogState()
    object Harvesting : DialogState()
    data class Incompatibility(val pendingPlant: Hortaliza, val enemies: Set<Hortaliza>) : DialogState()
}

private fun Hortaliza.nombreEnIdiomaActual(language: String): String {
    return nombreMostrado[language] ?: nombreMostrado["es"] ?: nombre
}

@Composable
fun DetalleBancalScreen(
    bancalState: HuertoUiState<List<Bancal>>,
    hortalizasState: HuertoUiState<List<Hortaliza>>,
    bancalId: String?,
    currentLanguage: String,
    onUpdateCultivos: (List<String>, String) -> Unit,
    onRegarCultivos: (List<String>) -> Unit,
    onAbonarCultivos: (List<String>) -> Unit,
    onCosecharCultivos: (List<String>) -> Unit
) {
    if (bancalId == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bancal no encontrado")
        }
        return
    }

    when (bancalState) {
        is HuertoUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is HuertoUiState.Success -> {
            val bancal = bancalState.data.find { it.id == bancalId }
            if (bancal == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bancal no encontrado")
                }
            } else {
                DetalleBancalContent(
                    bancal = bancal,
                    hortalizasState = hortalizasState,
                    currentLanguage = currentLanguage,
                    onUpdateCultivos = onUpdateCultivos,
                    onRegarCultivos = onRegarCultivos,
                    onAbonarCultivos = onAbonarCultivos,
                    onCosecharCultivos = onCosecharCultivos
                )
            }
        }
        is HuertoUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${bancalState.message}")
            }
        }
    }
}

@Composable
private fun DetalleBancalContent(
    bancal: Bancal,
    hortalizasState: HuertoUiState<List<Hortaliza>>,
    currentLanguage: String,
    onUpdateCultivos: (List<String>, String) -> Unit,
    onRegarCultivos: (List<String>) -> Unit,
    onAbonarCultivos: (List<String>) -> Unit,
    onCosecharCultivos: (List<String>) -> Unit
) {
    var posicionesSeleccionadas by remember { mutableStateOf(emptySet<String>()) }
    var justUpdatedPositions by remember { mutableStateOf(emptySet<String>()) }
    var dialogState by remember { mutableStateOf<DialogState>(DialogState.Hidden) }

    when (hortalizasState) {
        is HuertoUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is HuertoUiState.Success -> {
            val hortalizasDisponibles = hortalizasState.data
            val hortalizasMap = remember(hortalizasDisponibles) {
                hortalizasDisponibles.associateBy { it.nombre }
            }

            val todasVacias = posicionesSeleccionadas.isNotEmpty() && posicionesSeleccionadas.all { bancal.cultivos[it] == null }
            val todasOcupadas = posicionesSeleccionadas.isNotEmpty() && posicionesSeleccionadas.all { bancal.cultivos[it] != null }

            Scaffold(
                floatingActionButton = {
                    AnimatedVisibility(
                        visible = posicionesSeleccionadas.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            if (todasOcupadas) {
                                ExtendedFloatingActionButton(
                                    onClick = { dialogState = DialogState.Harvesting },
                                    icon = { Icon(Icons.Default.Agriculture, null) },
                                    text = { Text("Recoger") },
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                ExtendedFloatingActionButton(
                                    onClick = { dialogState = DialogState.Fertilizing },
                                    icon = { Icon(Icons.Default.Science, null) },
                                    text = { Text("Abonar") },
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                ExtendedFloatingActionButton(
                                    onClick = { dialogState = DialogState.Watering },
                                    icon = { Icon(Icons.Default.WaterDrop, null) },
                                    text = { Text("Regar") },
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            } else if (todasVacias) {
                                ExtendedFloatingActionButton(
                                    onClick = { dialogState = DialogState.PlantSelection },
                                    icon = { Icon(Icons.Default.Add, null) },
                                    text = { Text("Plantar (${posicionesSeleccionadas.size})") },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    if (posicionesSeleccionadas.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Seleccionados: ${posicionesSeleccionadas.size}",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                TextButton(onClick = { posicionesSeleccionadas = emptySet() }) {
                                    Text("Limpiar selección")
                                }
                            }
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(bancal.ancho),
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(bancal.ancho * bancal.largo) { index ->
                            val fila = index / bancal.ancho
                            val columna = index % bancal.ancho
                            val posicion = "$fila-$columna"
                            val cultivo = bancal.cultivos[posicion]
                            val isSelected = posicion in posicionesSeleccionadas

                            CeldaBancal(
                                cultivo = cultivo,
                                isSelected = isSelected,
                                isJustUpdated = posicion in justUpdatedPositions,
                                hortalizasMap = hortalizasMap,
                                language = currentLanguage
                            ) {
                                val yaHabiaSeleccion = posicionesSeleccionadas.isNotEmpty()
                                val esMismoTipo = if (!yaHabiaSeleccion) true
                                else (todasVacias && cultivo == null) || (todasOcupadas && cultivo != null)

                                if (isSelected) {
                                    posicionesSeleccionadas = posicionesSeleccionadas - posicion
                                } else if (esMismoTipo) {
                                    posicionesSeleccionadas = posicionesSeleccionadas + posicion
                                } else {
                                    posicionesSeleccionadas = setOf(posicion)
                                }
                            }
                        }
                    }
                }

                // --- GESTIÓN DE DIÁLOGOS ---
                when (val currentDialog = dialogState) {
                    is DialogState.PlantSelection -> {
                        DialogoSeleccionHortaliza(
                            hortalizas = hortalizasDisponibles,
                            language = currentLanguage,
                            onDismiss = { dialogState = DialogState.Hidden },
                            onSelect = { hortaliza ->
                                val enemigos = verificarIncompatibilidad(
                                    nueva = hortaliza,
                                    seleccionadas = posicionesSeleccionadas.toList(),
                                    bancal = bancal,
                                    hortalizasMap = hortalizasMap,
                                    language = currentLanguage
                                )
                                if (enemigos.isNotEmpty()) {
                                    dialogState = DialogState.Incompatibility(hortaliza, enemigos)
                                } else {
                                    onUpdateCultivos(posicionesSeleccionadas.toList(), hortaliza.nombre)
                                    dialogState = DialogState.Hidden
                                    posicionesSeleccionadas = emptySet()
                                }
                            }
                        )
                    }

                    is DialogState.Watering -> {
                        AccionConfirmDialog(
                            titulo = "Regar cultivos",
                            descripcion = "¿Deseas registrar el riego para las plantas seleccionadas?",
                            onConfirm = {
                                onRegarCultivos(posicionesSeleccionadas.toList())
                                justUpdatedPositions = posicionesSeleccionadas
                                dialogState = DialogState.Hidden
                                posicionesSeleccionadas = emptySet()
                            },
                            onDismiss = { dialogState = DialogState.Hidden }
                        )
                    }

                    is DialogState.Fertilizing -> {
                        AccionConfirmDialog(
                            titulo = "Abonar cultivos",
                            descripcion = "¿Deseas aplicar abono orgánico a estas plantas?",
                            onConfirm = {
                                onAbonarCultivos(posicionesSeleccionadas.toList())
                                justUpdatedPositions = posicionesSeleccionadas
                                dialogState = DialogState.Hidden
                                posicionesSeleccionadas = emptySet()
                            },
                            onDismiss = { dialogState = DialogState.Hidden }
                        )
                    }

                    is DialogState.Harvesting -> {
                        AccionConfirmDialog(
                            titulo = "Recoger cosecha",
                            descripcion = "Esta acción eliminará las plantas del bancal para registrarlas en tu diario de cosecha. ¿Continuar?",
                            onConfirm = {
                                onCosecharCultivos(posicionesSeleccionadas.toList())
                                dialogState = DialogState.Hidden
                                posicionesSeleccionadas = emptySet()
                            },
                            onDismiss = { dialogState = DialogState.Hidden }
                        )
                    }

                    is DialogState.Incompatibility -> {
                        AvisoIncompatibilidadDialog(
                            pendingPlant = currentDialog.pendingPlant,
                            enemies = currentDialog.enemies,
                            language = currentLanguage,
                            onConfirm = {
                                onUpdateCultivos(posicionesSeleccionadas.toList(), currentDialog.pendingPlant.nombre)
                                dialogState = DialogState.Hidden
                                posicionesSeleccionadas = emptySet()
                            },
                            onCancel = { dialogState = DialogState.PlantSelection }
                        )
                    }
                    else -> {}
                }
            }
        }
        is HuertoUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${hortalizasState.message}")
            }
        }
    }
}

@Composable
private fun AccionConfirmDialog(
    titulo: String,
    descripcion: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = { Text(descripcion) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun CeldaBancal(
    cultivo: Cultivo?,
    isSelected: Boolean,
    isJustUpdated: Boolean,
    hortalizasMap: Map<String, Hortaliza>,
    language: String,
    onClick: () -> Unit
) {
    val hortaliza = cultivo?.let { c ->
        val nombre = c.nombreHortaliza[language] ?: c.nombreHortaliza["es"]
        hortalizasMap[nombre]
    }

    val needsWater = cultivo?.necesitaRiego ?: false

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (hortaliza != null) {
                Text(hortaliza.icono, fontSize = 28.sp)
                if (!needsWater) {
                    Icon(
                        Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).size(16.dp)
                    )
                }
            } else {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.Done,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(12.dp)
                )
            }

            this@Card.AnimatedVisibility(
                visible = isJustUpdated,
                enter = fadeIn(animationSpec = spring()),
                exit = fadeOut(animationSpec = spring())
            ) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)))
            }
        }
    }
}

// ... Las funciones verificarIncompatibilidad, AvisoIncompatibilidadDialog y DialogoSeleccionHortaliza se mantienen igual que en la versión anterior ...

@Composable
private fun AvisoIncompatibilidadDialog(
    pendingPlant: Hortaliza,
    enemies: Set<Hortaliza>,
    language: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(stringResource(Res.string.bancal_conflict_title), textAlign = TextAlign.Center) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.bancal_conflict_subtitle), modifier = Modifier.padding(bottom = 16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(pendingPlant.icono, fontSize = 40.sp)
                        Text(pendingPlant.nombreEnIdiomaActual(language), style = MaterialTheme.typography.labelSmall)
                    }
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        enemies.forEach { enemigo ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(enemigo.icono, fontSize = 40.sp)
                                Text(enemigo.nombreEnIdiomaActual(language), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text(stringResource(Res.string.bancal_conflict_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(stringResource(Res.string.bancal_conflict_cancel)) }
        }
    )
}

private fun verificarIncompatibilidad(
    nueva: Hortaliza,
    seleccionadas: List<String>,
    bancal: Bancal,
    hortalizasMap: Map<String, Hortaliza>,
    language: String
): Set<Hortaliza> {
    val enemigosEncontrados = mutableSetOf<Hortaliza>()
    val nombreNueva = nueva.nombre
    val incompatiblesConNueva = nueva.incompatibles.toSet()

    for (pos in seleccionadas) {
        val partes = pos.split("-")
        val f = partes.getOrNull(0)?.toIntOrNull() ?: continue
        val c = partes.getOrNull(1)?.toIntOrNull() ?: continue
        for (df in -1..1) {
            for (dc in -1..1) {
                if (df == 0 && dc == 0) continue
                val posVecina = "${f + df}-${c + dc}"
                if (posVecina in seleccionadas) continue
                bancal.cultivos[posVecina]?.let { vec ->
                    val nom = vec.nombreHortaliza["es"] ?: vec.nombreHortaliza.values.firstOrNull() ?: ""
                    hortalizasMap[nom]?.let { h ->
                        if (h.incompatibles.contains(nombreNueva) || incompatiblesConNueva.contains(h.nombre)) {
                            enemigosEncontrados.add(h)
                        }
                    }
                }
            }
        }
    }
    return enemigosEncontrados
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoSeleccionHortaliza(
    hortalizas: List<Hortaliza>,
    language: String,
    onDismiss: () -> Unit,
    onSelect: (Hortaliza) -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, tonalElevation = 2.dp) {
            Column {
                Text(stringResource(Res.string.bancal_select_plant), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(hortalizas) { h ->
                        ListItem(
                            headlineContent = { Text(h.nombreEnIdiomaActual(language)) },
                            leadingContent = { Text(h.icono, fontSize = 24.sp) },
                            modifier = Modifier.clickable { onSelect(h) }
                        )
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End).padding(8.dp)) { Text("Cancelar") }
            }
        }
    }
}