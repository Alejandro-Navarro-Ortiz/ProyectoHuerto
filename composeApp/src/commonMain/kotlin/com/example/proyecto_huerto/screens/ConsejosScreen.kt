package com.example.proyecto_huerto.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Consejo(val titulo: String, val descripcion: String)

val consejosList = listOf(
    Consejo("Riego Inteligente", "Riega tus plantas temprano en la mañana o al atardecer para minimizar la evaporación. Asegúrate de que el agua llegue a las raíces."),
    Consejo("Compostaje", "Crea tu propio compost con restos de cocina y jardín. Es un excelente fertilizante natural y reduce los residuos."),
    Consejo("Rotación de Cultivos", "No plantes lo mismo en el mismo lugar cada año. La rotación ayuda a prevenir plagas y enfermedades del suelo."),
    Consejo("Control de Plagas Natural", "Utiliza insectos beneficiosos como mariquitas o plantas repelentes como la albahaca para controlar las plagas sin químicos."),
    Consejo("Mulching o Acolchado", "Cubre el suelo alrededor de tus plantas con paja, hojas secas o corteza. Ayuda a retener la humedad, suprime las malas hierbas y mejora la salud del suelo."),
    Consejo("Asociación de Cultivos", "Algunas plantas se benefician mutuamente cuando se plantan juntas. Por ejemplo, planta zanahorias cerca de tomates para repeler algunas plagas.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsejosScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consejos para tu Huerto") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onNavigate("home") }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Inicio"
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(consejosList) { consejo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = consejo.titulo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = consejo.descripcion,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}