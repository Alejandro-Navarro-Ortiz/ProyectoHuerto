package com.example.proyecto_huerto.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.viewmodel.HuertoUiState
import com.example.proyecto_huerto.viewmodel.HuertoViewModel
import org.jetbrains.compose.resources.stringResource
import proyectohuerto.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuiaScreen(
    onHortalizaListClick: () -> Unit,
    onPlagaListClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.guia_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.profile_back))
                    }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(it),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MenuCard(
                    title = stringResource(Res.string.guia_hortalizas_title),
                    description = stringResource(Res.string.guia_hortalizas_desc),
                    onClick = onHortalizaListClick,
                    icon = { Icon(Icons.Default.Grass, contentDescription = null, modifier = Modifier.size(40.dp)) }
                )
            }
            item {
                MenuCard(
                    title = stringResource(Res.string.guia_plagas_title),
                    description = stringResource(Res.string.guia_plagas_desc),
                    onClick = onPlagaListClick,
                    icon = { Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(40.dp)) }
                )
            }
        }
    }
}

@Composable
fun MenuCard(title: String, description: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            icon()
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HortalizaListScreen(
    viewModel: HuertoViewModel,
    onHortalizaClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val hortalizasState by viewModel.hortalizasState.collectAsState()
    val currentLanguage = Locale.current.language

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.guia_hortalizas_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.profile_back))
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(it), contentAlignment = Alignment.Center) {
            when (val state = hortalizasState) {
                is HuertoUiState.Loading -> CircularProgressIndicator()
                is HuertoUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.data, key = { it.nombre }) { hortaliza ->
                            HortalizaListItem(hortaliza, currentLanguage) {
                                // Asegurarse de pasar el ID (nombre) y no el nombre mostrado.
                                onHortalizaClick(hortaliza.nombre)
                            }
                        }
                    }
                }
                is HuertoUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun HortalizaListItem(hortaliza: Hortaliza, language: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(hortaliza.icono, fontSize = MaterialTheme.typography.headlineMedium.fontSize)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hortaliza.nombreMostrado[language] ?: hortaliza.nombreMostrado["es"] ?: hortaliza.nombre,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}