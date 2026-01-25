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
import org.jetbrains.compose.resources.stringResource
import proyectohuerto.composeapp.generated.resources.*

data class Consejo(val titulo: String, val descripcion: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsejosScreen(onNavigate: (String) -> Unit) {
    val consejosList = listOf(
        Consejo(stringResource(Res.string.consejos_riego_title), stringResource(Res.string.consejos_riego_desc)),
        Consejo(stringResource(Res.string.consejos_compost_title), stringResource(Res.string.consejos_compost_desc)),
        Consejo(stringResource(Res.string.consejos_rotacion_title), stringResource(Res.string.consejos_rotacion_desc)),
        Consejo(stringResource(Res.string.consejos_plagas_title), stringResource(Res.string.consejos_plagas_desc)),
        Consejo(stringResource(Res.string.consejos_mulching_title), stringResource(Res.string.consejos_mulching_desc)),
        Consejo(stringResource(Res.string.consejos_asociacion_title), stringResource(Res.string.consejos_asociacion_desc))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.consejos_screen_title)) },
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
                        contentDescription = stringResource(Res.string.home_diario_title) // Reutilizamos un string existente para "Inicio"
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
                ConsejoCard(consejo)
            }
        }
    }
}

@Composable
private fun ConsejoCard(consejo: Consejo) {
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
