package com.example.proyecto_huerto.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HomeOption(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(recentActivities: List<String>, onNavigate: (String) -> Unit) {
    val navOptions = listOf(
        HomeOption("Bancales", Icons.Filled.Yard, "gestion_bancales", "Organiza tus zonas"),
        HomeOption("Diario", Icons.Filled.Book, "diario_cultivo", "Bitácora de tareas"),
        HomeOption("Guía", Icons.Filled.LocalFlorist, "guia_hortalizas", "Manual de cultivo"),
        HomeOption("Plagas", Icons.Filled.BugReport, "plagas", "Control biológico")
    )

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Mi Huerto", style = MaterialTheme.typography.headlineLarge)
                        Text("Gestión Sostenible", style = MaterialTheme.typography.labelMedium)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate("profile") },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Perfil",
                                modifier = Modifier.padding(8.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Servicios",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Grid de 2x2 Profesional
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HomeServiceCard(navOptions[0], Modifier.weight(1f)) { onNavigate(navOptions[0].route) }
                        HomeServiceCard(navOptions[1], Modifier.weight(1f)) { onNavigate(navOptions[1].route) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HomeServiceCard(navOptions[2], Modifier.weight(1f)) { onNavigate(navOptions[2].route) }
                        HomeServiceCard(navOptions[3], Modifier.weight(1f)) { onNavigate(navOptions[3].route) }
                    }
                }
            }

            item {
                Card(
                    onClick = { onNavigate("consejos") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lightbulb, null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Consejo del día", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            Text("Mejora tu compost hoy", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            if (recentActivities.isNotEmpty()) {
                item {
                    Text(
                        "Actividad Reciente",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(recentActivities.take(5)) { activity ->
                    TimelineActivityItem(activity)
                }
            }
        }
    }
}

@Composable
fun HomeServiceCard(option: HomeOption, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text(option.title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(option.description, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun TimelineActivityItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        Spacer(Modifier.width(16.dp))
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}