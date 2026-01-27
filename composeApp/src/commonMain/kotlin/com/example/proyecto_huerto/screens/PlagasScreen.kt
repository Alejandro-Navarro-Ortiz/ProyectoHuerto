package com.example.proyecto_huerto.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyecto_huerto.models.Plaga
import com.example.proyecto_huerto.models.getLocalizedSanitized
import com.example.proyecto_huerto.viewmodel.HuertoUiState
import org.jetbrains.compose.resources.stringResource
import proyectohuerto.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlagasScreen(
    onPlagaClick: (String) -> Unit,
    onBack: () -> Unit,
    uiState: HuertoUiState<List<Plaga>>,
    language: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.guia_plagas_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.profile_back))
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
            when (val state = uiState) {
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
                            PlagaListItem(plaga, language) {
                                onPlagaClick(plaga.id)
                            }
                        }
                    }
                }
                is HuertoUiState.Error -> {
                    Text(
                        text = stringResource(Res.string.auth_error_generic) + ": ${state.message}",
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
private fun PlagaListItem(plaga: Plaga, language: String, onClick: () -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plaga.name.getLocalizedSanitized(language),
                    style = MaterialTheme.typography.titleLarge
                )
                val sciName = plaga.scientificName.getLocalizedSanitized(language)
                if (sciName.isNotBlank()) {
                    Text(
                        text = sciName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlagaDetailScreen(
    plagaId: String,
    onBack: () -> Unit,
    uiState: HuertoUiState<List<Plaga>>,
    language: String
) {
    val plaga = (uiState as? HuertoUiState.Success)?.data?.find { it.id == plagaId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plaga?.name?.getLocalizedSanitized(language) ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.profile_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (plaga != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        val scientificName = plaga.scientificName.getLocalizedSanitized(language)
                        val description = plaga.description.getLocalizedSanitized(language)
                        val symptoms = plaga.symptoms.getLocalizedSanitized(language)
                        val organicTreatment = plaga.organicTreatment.getLocalizedSanitized(language)

                        Column {
                            if (scientificName.isNotBlank()) {
                                Text(scientificName, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(16.dp))
                            }
                            PlagaDetailSection(stringResource(Res.string.pest_description), description)
                            PlagaDetailSection(stringResource(Res.string.pest_symptoms), symptoms)
                            PlagaDetailSection(stringResource(Res.string.pest_treatment), organicTreatment)
                        }
                    }
                }
            } else if (uiState is HuertoUiState.Loading) {
                CircularProgressIndicator()
            } else {
                Text("Plaga no encontrada", textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun PlagaDetailSection(title: String, content: String) {
    if (content.isNotBlank()) {
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
}