package com.example.proyecto_huerto.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.viewmodel.HuertoUiState
import org.jetbrains.compose.resources.stringResource
import proyectohuerto.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleHortalizaScreen(
    hortalizaId: String,
    onBack: () -> Unit,
    uiState: HuertoUiState<List<Hortaliza>>
) {
    val currentLanguage = Locale.current.language

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.detail_sheet_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.profile_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is HuertoUiState.Loading -> CircularProgressIndicator()
                is HuertoUiState.Success -> {
                    val hortaliza = state.data.find { it.nombre == hortalizaId }

                    if (hortaliza != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        shape = MaterialTheme.shapes.extraLarge,
                                        color = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 4.dp,
                                        modifier = Modifier.size(100.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(hortaliza.icono, fontSize = 50.sp)
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = hortaliza.nombreMostrado[currentLanguage] ?: hortaliza.nombreMostrado["es"] ?: hortaliza.nombre,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(20.dp)) {
                                SectionCard(
                                    title = stringResource(Res.string.detail_description),
                                    icon = Icons.Default.Description,
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = hortaliza.descripcion[currentLanguage] ?: hortaliza.descripcion["es"] ?: "",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                SectionCard(
                                    title = stringResource(Res.string.detail_tips),
                                    icon = Icons.Default.TipsAndUpdates,
                                    color = Color(0xFFF57C00)
                                ) {
                                    Text(
                                        text = hortaliza.consejos[currentLanguage] ?: hortaliza.consejos["es"] ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            stringResource(Res.string.detail_allies),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color(0xFF388E3C),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                                        )
                                        hortaliza.compatibles.forEach { amigoId ->
                                            val amigoHortaliza = state.data.find { it.nombre == amigoId }
                                            val amigoNombre = amigoHortaliza?.let {
                                                it.nombreMostrado[currentLanguage] ?: it.nombreMostrado["es"] ?: it.nombre
                                            } ?: amigoId
                                            CompactTag(amigoNombre, Color(0xFFE8F5E9), Color(0xFF2E7D32))
                                        }
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            stringResource(Res.string.detail_avoid),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                                        )
                                        hortaliza.incompatibles.forEach { enemigoId ->
                                            val enemigoHortaliza = state.data.find { it.nombre == enemigoId }
                                            val enemigoNombre = enemigoHortaliza?.let {
                                                it.nombreMostrado[currentLanguage] ?: it.nombreMostrado["es"] ?: it.nombre
                                            } ?: enemigoId
                                            CompactTag(enemigoNombre, Color(0xFFFFEBEE), Color(0xFFC62828))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(40.dp))
                            }
                        }
                    } else {
                        Text(stringResource(Res.string.detail_not_found))
                    }
                }
                is HuertoUiState.Error -> {
                    Text(
                        text = "Error al cargar los detalles: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = color,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun CompactTag(text: String, bgColor: Color, textColor: Color) {
    Surface(
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
        color = bgColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}