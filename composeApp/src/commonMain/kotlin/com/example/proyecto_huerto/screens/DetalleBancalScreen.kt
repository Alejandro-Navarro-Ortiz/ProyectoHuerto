package com.example.proyecto_huerto.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import proyectohuerto.composeapp.generated.resources.Res
import proyectohuerto.composeapp.generated.resources.*
import kotlin.time.ExperimentalTime

private sealed class DialogState {
    object Hidden : DialogState()
    object PlantSelection : DialogState()
    object Watering : DialogState()
    object Fertilizing : DialogState()
    object Harvesting : DialogState()
    data class CultivoDetail(val cultivo: Cultivo, val hortaliza: Hortaliza) : DialogState()
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
    onBack: () -> Unit,
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
                    onBack = onBack,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalleBancalContent(
    bancal: Bancal,
    hortalizasState: HuertoUiState<List<Hortaliza>>,
    currentLanguage: String,
    onBack: () -> Unit,
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

            val enModoSeleccion = posicionesSeleccionadas.isNotEmpty()

            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(bancal.nombre, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                },
                floatingActionButton = {
                    AnimatedVisibility(
                        visible = posicionesSeleccionadas.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            val todasOcupadas = posicionesSeleccionadas.all { bancal.cultivos[it] != null }
                            val todasVacias = posicionesSeleccionadas.all { bancal.cultivos[it] == null }

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
                    if (enModoSeleccion) {
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
                                language = currentLanguage,
                                onClick = {
                                    if (enModoSeleccion) {
                                        val todasVacias = posicionesSeleccionadas.all { bancal.cultivos[it] == null }
                                        val todasOcupadas = posicionesSeleccionadas.all { bancal.cultivos[it] != null }
                                        val esMismoTipo = (todasVacias && cultivo == null) || (todasOcupadas && cultivo != null)

                                        if (isSelected) {
                                            posicionesSeleccionadas = posicionesSeleccionadas - posicion
                                        } else if (esMismoTipo) {
                                            posicionesSeleccionadas = posicionesSeleccionadas + posicion
                                        }
                                    } else {
                                        if (cultivo != null) {
                                            val h = hortalizasMap[cultivo.hortalizaId]
                                            if (h != null) {
                                                dialogState = DialogState.CultivoDetail(cultivo, h)
                                            }
                                        } else {
                                            posicionesSeleccionadas = setOf(posicion)
                                        }
                                    }
                                },
                                onLongClick = {
                                    if (!enModoSeleccion) {
                                        posicionesSeleccionadas = setOf(posicion)
                                    }
                                }
                            )
                        }
                    }
                }

                when (val currentDialog = dialogState) {
                    is DialogState.CultivoDetail -> {
                        CultivoInfoDialog(
                            cultivo = currentDialog.cultivo,
                            hortaliza = currentDialog.hortaliza,
                            language = currentLanguage,
                            onDismiss = { dialogState = DialogState.Hidden }
                        )
                    }
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

@OptIn(ExperimentalTime::class)
@Composable
private fun CultivoInfoDialog(
    cultivo: Cultivo,
    hortaliza: Hortaliza,
    language: String,
    onDismiss: () -> Unit
) {
    val systemTZ = TimeZone.of("UTC+1") // Forzado para corregir desfase

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar", color = Color(0xFF98FB98)) }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(hortaliza.icono, fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Text(hortaliza.nombreEnIdiomaActual(language), color = Color.White)
            }
        },
        containerColor = Color(0xFF2C2C2E),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))

                InfoItem(
                    icon = Icons.Default.CalendarMonth,
                    label = "Plantado el",
                    value = cultivo.fechaPlantado?.let { instant ->
                        val localDateTime = instant.toLocalDateTime(systemTZ)
                        "${localDateTime.dayOfMonth}/${localDateTime.monthNumber}/${localDateTime.year} a las ${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
                    } ?: "N/A"
                )

                InfoItem(
                    icon = Icons.Default.WaterDrop,
                    label = "Último riego",
                    value = cultivo.ultimoRiego?.let { instant ->
                        val localDateTime = instant.toLocalDateTime(systemTZ)
                        "${localDateTime.dayOfMonth}/${localDateTime.monthNumber} a las ${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
                    } ?: "Nunca"
                )

                InfoItem(
                    icon = Icons.Default.Science,
                    label = "Frecuencia de riego",
                    value = "Cada ${cultivo.frecuenciaRiegoDias} días"
                )

                if (cultivo.necesitaRiego) {
                    Surface(
                        color = Color(0xFFB22222),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("¡Necesita agua!", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
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
            Button(onClick = onConfirm) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CeldaBancal(
    cultivo: Cultivo?,
    isSelected: Boolean,
    isJustUpdated: Boolean,
    hortalizasMap: Map<String, Hortaliza>,
    language: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val hortaliza = cultivo?.let { c ->
        hortalizasMap[c.hortalizaId]
    }

    val needsWater = cultivo?.necesitaRiego ?: false

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
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
                    hortalizasMap[vec.hortalizaId]?.let { h ->
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