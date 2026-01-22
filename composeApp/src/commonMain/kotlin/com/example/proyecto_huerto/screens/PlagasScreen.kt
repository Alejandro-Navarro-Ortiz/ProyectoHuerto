package com.example.proyecto_huerto.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyecto_huerto.models.Plaga
import com.example.proyecto_huerto.viewmodel.HuertoUiState
import com.example.proyecto_huerto.viewmodel.HuertoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlagasScreen(
    onPlagaClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: HuertoViewModel
) {
    val plagasState by viewModel.plagasState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guía de Plagas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = plagasState) {
                is HuertoUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is HuertoUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.data) { plaga ->
                            PlagaListItem(plaga = plaga, onClick = { onPlagaClick(plaga.id) })
                        }
                    }
                }
                is HuertoUiState.Error -> {
                    Text(
                        text = "Error al cargar las plagas: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlagaListItem(plaga: Plaga, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = plaga.name, style = MaterialTheme.typography.titleLarge)
                Text(text = plaga.scientificName, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlagaDetailScreen(
    plagaId: String,
    onBack: () -> Unit,
    viewModel: HuertoViewModel
) {
    val plagasState by viewModel.plagasState.collectAsState()
    var plaga: Plaga? = null

    if (plagasState is HuertoUiState.Success) {
        plaga = (plagasState as HuertoUiState.Success<List<Plaga>>).data.find { it.id == plagaId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plaga?.name ?: "Cargando...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (plagasState) {
                is HuertoUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is HuertoUiState.Success -> {
                    if (plaga != null) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            item {
                                Column {
                                    Text(plaga.scientificName, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(16.dp))

                                    PlagaDetailSection("Descripción", plaga.description)
                                    PlagaDetailSection("Síntomas y Daños", plaga.symptoms)
                                    PlagaDetailSection("Tratamiento Ecológico", plaga.organicTreatment)
                                }
                            }
                        }
                    } else {
                        Text("No se encontró la información de la plaga.")
                    }
                }
                is HuertoUiState.Error -> {
                    Text(
                        text = "Error al cargar la plaga.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PlagaDetailSection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Justify
        )
    }
}