package com.example.proyecto_huerto.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyecto_huerto.models.Bancal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleBancalScreen(
    bancal: Bancal,
    onSave: (Bancal) -> Unit,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf(bancal.nombre) }
    // Cambiamos a Double para coincidir con el modelo robusto
    var ancho by remember { mutableStateOf(if (bancal.ancho == 0.0) "" else bancal.ancho.toString()) }
    var largo by remember { mutableStateOf(if (bancal.largo == 0.0) "" else bancal.largo.toString()) }
    var cultivos by remember { mutableStateOf(bancal.cultivos.joinToString(", ")) }
    var notas by remember { mutableStateOf(bancal.notas) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CONFIGURAR BANCAL", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            onSave(
                                bancal.copy(
                                    nombre = nombre,
                                    // Usamos toDoubleOrNull para evitar el error de tipo
                                    ancho = ancho.toDoubleOrNull() ?: 0.0,
                                    largo = largo.toDoubleOrNull() ?: 0.0,
                                    cultivos = cultivos.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    notas = notas
                                )
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Guardar")
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
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Información Básica",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Bancal") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = ancho,
                    onValueChange = { ancho = it },
                    label = { Text("Ancho (m)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = largo,
                    onValueChange = { largo = it },
                    label = { Text("Largo (m)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "Cultivos Actuales",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = cultivos,
                onValueChange = { cultivos = it },
                label = { Text("Tipos de cultivo") },
                placeholder = { Text("Ej: Tomates, Albahaca, Pimientos") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                supportingText = { Text("Separa los cultivos por comas") }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "Planificación y Notas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas de planificación / Rotación") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            if (bancal.historico.isNotEmpty()) {
                Text("Histórico de Cultivos", style = MaterialTheme.typography.titleMedium)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        bancal.historico.forEach { item ->
                            Text("• $item", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}