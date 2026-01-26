package com.example.proyecto_huerto.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.viewmodel.HuertoUiState
import org.jetbrains.compose.resources.stringResource
import proyectohuerto.composeapp.generated.resources.Res
import proyectohuerto.composeapp.generated.resources.*

private sealed class DialogState {
    object Hidden : DialogState()
    object PlantSelection : DialogState()
    object Watering : DialogState()
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
    onRegarCultivos: (List<String>) -> Unit
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
                    onRegarCultivos = onRegarCultivos
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
    onRegarCultivos: (List<String>) -> Unit
) {
    var posicionesSeleccionadas by remember { mutableStateOf(emptySet<String>()) }
    var justWateredPositions by remember { mutableStateOf(emptySet<String>()) }
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

            Scaffold {
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
                            isJustWatered = posicion in justWateredPositions,
                            hortalizasMap = hortalizasMap,
                            language = currentLanguage
                        ) {
                            posicionesSeleccionadas = setOf(posicion)
                            if (bancal.cultivos[posicion] != null) {
                                dialogState = DialogState.Watering
                            } else {
                                dialogState = DialogState.PlantSelection
                            }
                        }
                    }
                }

                when (val currentDialog = dialogState) {
                    is DialogState.PlantSelection -> {
                        DialogoSeleccionHortaliza(
                            hortalizas = hortalizasDisponibles,
                            language = currentLanguage,
                            onDismiss = {
                                dialogState = DialogState.Hidden
                                posicionesSeleccionadas = emptySet()
                            },
                            onSelect = { hortaliza ->
                                val enemigos = verificarIncompatibilidad(
                                    nueva = hortaliza,
                                    seleccionadas = posicionesSeleccionadas.toList(),
                                    bancal = bancal,
                                    hortalizasMap = hortalizasMap
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
                            onCancel = {
                                dialogState = DialogState.PlantSelection
                            }
                        )
                    }

                    is DialogState.Watering -> {
                        RiegoDialog(
                            onConfirm = {
                                onRegarCultivos(posicionesSeleccionadas.toList())
                                justWateredPositions = posicionesSeleccionadas
                                dialogState = DialogState.Hidden
                                posicionesSeleccionadas = emptySet()
                            },
                            onDismiss = {
                                dialogState = DialogState.Hidden
                                posicionesSeleccionadas = emptySet()
                            }
                        )
                    }

                    is DialogState.Hidden -> {
                        // No mostrar ningún diálogo
                    }
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
private fun AvisoIncompatibilidadDialog(
    pendingPlant: Hortaliza,
    enemies: Set<Hortaliza>,
    language: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp)) },
        title = {
            Text(
                stringResource(Res.string.bancal_conflict_title),
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
                    stringResource(Res.string.bancal_conflict_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(pendingPlant.icono, fontSize = 40.sp)
                        Text(pendingPlant.nombreEnIdiomaActual(language), style = MaterialTheme.typography.labelSmall)
                    }

                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp).size(32.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        enemies.forEach { enemigo ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(enemigo.icono, fontSize = 40.sp)
                                Text(enemigo.nombreEnIdiomaActual(language), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(Res.string.bancal_conflict_desc),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(Res.string.bancal_conflict_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(Res.string.bancal_conflict_cancel))
            }
        }
    )
}

@Composable
private fun RiegoDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.bancal_water_title)) },
        text = { Text(stringResource(Res.string.bancal_water_desc)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.bancal_water_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.bancales_cancel))
            }
        }
    )
}

private fun verificarIncompatibilidad(
    nueva: Hortaliza,
    seleccionadas: List<String>,
    bancal: Bancal,
    hortalizasMap: Map<String, Hortaliza>
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
                val nf = f + df
                val nc = c + dc
                val posVecina = "$nf-$nc"

                bancal.cultivos[posVecina]?.let { cultivoVecino ->
                    val nombreHortalizaVecina = cultivoVecino.nombreHortaliza["es"] ?: cultivoVecino.nombreHortaliza.values.firstOrNull() ?: ""
                    val hortalizaVecina = hortalizasMap[nombreHortalizaVecina]

                    hortalizaVecina?.let { vecina ->
                        if (vecina.incompatibles.contains(nombreNueva) || incompatiblesConNueva.contains(vecina.nombre)) {
                            enemigosEncontrados.add(vecina)
                        }
                    }
                }
            }
        }
    }
    return enemigosEncontrados
}

@Composable
fun CeldaBancal(
    cultivo: Bancal.Cultivo?,
    isSelected: Boolean,
    isJustWatered: Boolean,
    hortalizasMap: Map<String, Hortaliza>,
    language: String,
    onClick: () -> Unit
) {
    val hortaliza = cultivo?.let { c ->
        val nombre = c.nombreHortaliza[language] ?: c.nombreHortaliza["es"]
        hortalizasMap[nombre]
    }

    val isWatered = cultivo?.regado ?: false

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (hortaliza != null) {
                Text(hortaliza.icono, fontSize = 28.sp)
                if (isWatered) {
                    Icon(
                        Icons.Default.WaterDrop,
                        contentDescription = stringResource(Res.string.bancal_water_desc),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(16.dp)
                    )
                }
            } else {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.bancales_add),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
            this@Card.AnimatedVisibility(
                visible = isJustWatered,
                enter = fadeIn(animationSpec = spring()),
                exit = fadeOut(animationSpec = spring())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                )
            }
        }
    }
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.bancal_select_plant),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(hortalizas) { hortaliza ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    hortaliza.nombreEnIdiomaActual(language),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingContent = {
                                Text(hortaliza.icono, fontSize = 24.sp)
                            },
                            modifier = Modifier.clickable { onSelect(hortaliza) }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.bancales_cancel))
                    }
                }
            }
        }
    }
}
