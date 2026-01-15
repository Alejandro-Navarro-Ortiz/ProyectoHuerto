package com.example.proyecto_huerto.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.proyecto_huerto.models.Bancal
import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.models.hortalizasDisponibles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleBancalScreen(
    bancal: Bancal,
    onBack: () -> Unit,
    onUpdateCultivo: (String, String) -> Unit // Recibe la posición y la hortaliza
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    var posicionSeleccionada by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(bancal.nombre) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Dimensiones: ${bancal.ancho}x${bancal.largo}", style = MaterialTheme.typography.titleMedium)
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

                    CeldaBancal(cultivo) {
                        posicionSeleccionada = posicion
                        mostrarDialogo = true
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoSeleccionHortaliza(
            hortalizas = hortalizasDisponibles,
            onDismiss = { mostrarDialogo = false },
            onSelect = {
                onUpdateCultivo(posicionSeleccionada, it.nombre)
                mostrarDialogo = false
            }
        )
    }
}

@Composable
fun CeldaBancal(cultivo: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (cultivo != null) {
            Text(text = hortalizasDisponibles.find { it.nombre == cultivo }?.icono ?: "❓", fontSize = MaterialTheme.typography.headlineMedium.fontSize)
        } else {
            Box(modifier = Modifier.size(20.dp).background(Color.Gray.copy(alpha = 0.5f)))
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
